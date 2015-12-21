package torcs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Optional;

//import org.neuroph.util.io.OutputAdapter;

import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.genome.IGenome;

public class DefaultDriver extends AbstractDriver {

	/* Driver Selection */
	final Boolean nnetDriver = true;
	final Boolean fastDriver = true;
	final Boolean makingTest = false;
	
	/* TORCS settings */
	public final static Boolean withGIU  = true;
	public final static int trackNumber  = 3;
	public final static int numberOfLaps = 4;

	/* Neural Networks */
	@SuppressWarnings("unused")
	private NeuralNetwork MyNN;
	private NeuralNetwork MyNNSteer;
	private NeuralNetwork MyNNAccel;
	@SuppressWarnings("unused")
	private NeuralNetwork MyNNBrake;

	/* Saving Track Data */
	public ArrayList<ArrayList<Double>> table;

	/* Gear Changing Constants */
	final int[] gearUp   = {8500, 8900, 9000, 9000, 9000,    0 };
	final int[] gearDown = {   0, 5000, 6000, 7000, 7500, 8000 };

	/* Stuck constants */
	final int stuckTime = 25;
	final float stuckAngle = (float) 0.523598775; // PI/6

	/* Accel and Brake Constants */
	final float maxSpeedDist = 70;
	final float maxSpeed = 150;
	final float sin5 = (float) 0.08716;
	final float cos5 = (float) 0.99619;

	/* Steering constants */
	final float steerLock = (float) 0.785398;
	final float steerSensitivityOffset = (float) 80.0;
	final float wheelSensitivityCoeff = 1;

	/* ABS Filter Constants */
	final float wheelRadius[] = { (float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276 };
	final float absSlip = (float) 2.0;
	final float absRange = (float) 3.0;
	final float absMinSpeed = (float) 3.0;

	/* Clutching Constants */
	final float clutchMax = (float) 0.5;
	final float clutchDelta = (float) 0.05;
	final float clutchRange = (float) 0.82;
	final float clutchDeltaTime = (float) 0.02;
	final float clutchDeltaRaced = 10;
	final float clutchDec = (float) 0.01;
	final float clutchMaxModifier = (float) 1.3;
	final float clutchMaxTime = (float) 1.5;

	private int stuck = 0;

	/* Current clutch */
	private float clutch = 0;

	public DefaultDriver() {
		table = new ArrayList<>();
	}

	@Override
	public void loadGenome(IGenome genome) {
		if (genome instanceof DefaultDriverGenome) {
			DefaultDriverGenome MyGenome = (DefaultDriverGenome) genome;
			MyNN = MyGenome.getMyNN();
			MyNNAccel = MyGenome.getNNAccel();
			MyNNSteer = MyGenome.getNNSteer();
			MyNNBrake = MyGenome.getNNBrake();
			System.out.println("Genome loaded");
		} else {
			System.err.println("Invalid Genome assigned");
		}
	}
	
	/*
	 * Race controllers
	 * 1. Neural Net
	 * 2. Simple fast driver controller
	 */
	public void controlNeuralNet(Action action, SensorModel sensors) {
		
//		double[] output = MyNN.getAccelSteer(sensors);
//		action.accelerate = output[0];
//		action.steering   = output[1];
		
		action.accelerate = MyNNAccel.getAccel(sensors);
//		action.brake	  = MyNNBrake.getBrake(sensors);
		action.steering   = MyNNSteer.getSteer(sensors);
	}

	@Override
	public void controlQualification(Action action, SensorModel sensors) {
		// super.controlQualification(action, sensors);
		controlFast(action, sensors);
	}

	@Override
	public void defaultControl(Action action, SensorModel sensors) {
		// super.defaultControl(action, sensors);
		controlFast(action, sensors);
	}

	@Override
	public void controlRace(Action action, SensorModel sensors) {

		if (makingTest) {
			
			super.controlWarmUp(action, sensors); // use for collecting data
			
			/* Set up */ 
			ArrayList<Double> row = new ArrayList<>();
			
			/* Input */
			row.add(sensors.getAngleToTrackAxis());
//			row.add(sensors.getTrackPosition());
//			Arrays.stream(sensors.getFocusSensors()).forEach(row::add);
//			Arrays.stream(sensors.getTrackEdgeSensors()).forEach(row::add);
//			Arrays.stream(sensors.getWheelSpinVelocity()).forEach(row::add);			
//			row.add(sensors.getTrackEdgeSensors()[10]);
			row.add(sensors.getTrackEdgeSensors()[9]);
//			row.add(sensors.getTrackEdgeSensors()[8]);
			row.add(sensors.getSpeed());
			
			/* Ouput */
			row.add(action.accelerate);
			row.add(action.steering);
//			row.add(action.brake);
			
			/* Add row */
			table.add(row);
		}

		/* Select Driver */
		if (nnetDriver)
			controlNeuralNet(action, sensors);
		else if (fastDriver)
			controlFast(action, sensors);
		else {
			DriversUtils utils = new DriversUtils();
			utils.calm(action, sensors);
		}
	}
	
	public void controlFast(Action action, SensorModel sensors) {
		// check if car is currently stuck
		if (Math.abs(sensors.getAngleToTrackAxis()) > stuckAngle) {
			// update stuck counter
			stuck++;
		} else {
			// if not stuck reset stuck counter
			stuck = 0;
		}

		// after car is stuck for a while apply recovering policy
		if (stuck > stuckTime) {
			/*
			 * set gear and steering command assuming car is pointing in a
			 * direction out of track
			 */

			// to bring car parallel to track axis
			float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
			int gear = -1; // gear R

			// if car is pointing in the correct direction revert gear and steer
			if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
				gear = 1;
				steer = -steer;
			}
			clutch = clutching(sensors, clutch);
			// build a CarControl variable and return it
			action.gear = gear;
			action.steering = MyNNSteer.getSteer(sensors);
			action.accelerate = 1.0;
			action.brake = 0;
			action.clutch = clutch;
		}

		else // car is not stuck
		{
			// compute accel/brake command
			double accel_and_brake = getAcceleration(sensors);
			// compute gear
			int gear = getGear(sensors);
			// compute steering
			double steer = getSteering(sensors);

			// normalize steering
			if (steer < -1)
				steer = -1;
			if (steer > 1)
				steer = 1;

			double accel;
			// set accel and brake from the joint accel/brake command
			double brake;
			if (accel_and_brake > 0) {
				accel = accel_and_brake;
				brake = 0;
			} else {
				accel = 0;
				// apply ABS to brake
				brake = filterABS(sensors, -accel_and_brake);
			}

			clutch = clutching(sensors, clutch);

			// build a CarControl variable and return it
			action.gear = gear;
			action.steering = MyNNSteer.getSteer(sensors);
			action.accelerate = accel;
			action.brake = brake;
			action.clutch = clutch;
		}
	}

	@Override
	public String getDriverName() {
		return "Henkie";
	}

	public double getLowAcce(SensorModel sensors) {
		if (sensors.getSpeed() > 60)
			return 0;
		return 1;
	}

	@Override
	public double getAcceleration(SensorModel sensors) {
		// checks if car is out of track
		if (sensors.getTrackPosition() < 1 && sensors.getTrackPosition() > -1) {
			// reading of sensor at +5 degree w.r.t. car axis
			float rxSensor = (float) sensors.getTrackEdgeSensors()[10];
			// reading of sensor parallel to car axis
			float sensorsensor = (float) sensors.getTrackEdgeSensors()[9];
			// reading of sensor at -5 degree w.r.t. car axis
			float sxSensor = (float) sensors.getTrackEdgeSensors()[8];

			float targetSpeed;

			// track is straight and enough far from a turn so goes to max speed
			if (sensorsensor > maxSpeedDist || (sensorsensor >= rxSensor && sensorsensor >= sxSensor))
				targetSpeed = maxSpeed;
			else {
				// approaching a turn on right
				if (rxSensor > sxSensor) {
					// computing approximately the "angle" of turn
					float h = sensorsensor * sin5;
					float b = rxSensor - sensorsensor * cos5;
					float sinAngle = b * b / (h * h + b * b);
					// estimate the target speed depending on turn and on how
					// close it is
					targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
				}
				// approaching a turn on left
				else {
					// computing approximately the "angle" of turn
					float h = sensorsensor * sin5;
					float b = sxSensor - sensorsensor * cos5;
					float sinAngle = b * b / (h * h + b * b);
					// estimate the target speed depending on turn and on how
					// close it is
					targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
				}

			}

			// accel/brake command is exponentially scaled w.r.t. the difference
			// between target speed and current one
			return 2 / (1 + Math.exp(sensors.getSpeed() - targetSpeed)) - 1;
		} else
			return 0.3; // when out of track returns a moderate
									// acceleration command
	}

	@Override
	public double getSteering(SensorModel sensors) {
		// steering angle is compute by correcting the actual car angle w.r.t.
		// to track
		// axis [sensors.getAngle()] and to adjust car position w.r.t to middle
		// of track [sensors.getTrackPos()*0.5]
		double targetAngle = -sensors.getAngleToTrackAxis() - sensors.getTrackPosition() * .5;

		// at high speed reduce the steering command to avoid loosing the
		// control
		if (sensors.getSpeed() > steerSensitivityOffset)
			return targetAngle
					/ (steerLock * (sensors.getSpeed() - steerSensitivityOffset) * wheelSensitivityCoeff);
		else
			return (targetAngle) / steerLock;
	}

	private int getGear(SensorModel sensors) {
		int gear = sensors.getGear();
		double rpm = sensors.getRPM();

		// if gear is 0 (N) or -1 (R) just return 1
		if (gear < 1)
			return 1;
		// check if the RPM value of car is greater than the one suggested
		// to shift up the gear from the current one
		if (gear < 6 && rpm >= gearUp[gear - 1])
			return gear + 1;
		else
		// check if the RPM value of car is lower than the one suggested
		// to shift down the gear from the current one
		if (gear > 1 && rpm <= gearDown[gear - 1])
			return gear - 1;
		else // otherwhise keep current gear
			return gear;
	}

	private double filterABS(SensorModel sensors, double brake) {
		// convert speed to m/s
		float speed = (float) (sensors.getSpeed() / 3.6);
		// when spedd lower than min speed for abs do nothing
		if (speed < absMinSpeed)
			return brake;

		// compute the speed of wheels in m/s
		float slip = 0.0f;
		for (int i = 0; i < 4; i++) {
			slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
		}
		// slip is the difference between actual speed of car and average speed
		// of wheels
		slip = speed - slip / 4.0f;
		// when slip too high applu ABS
		if (slip > absSlip) {
			brake = brake - (slip - absSlip) / absRange;
		}

		// check brake is not negative, otherwise set it to zero
		if (brake < 0)
			return 0;
		else
			return brake;
	}

	float clutching(SensorModel sensors, float clutch) {

		float maxClutch = clutchMax;

		// Check if the current situation is the race start
		if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
				&& sensors.getDistanceRaced() < clutchDeltaRaced)
			clutch = maxClutch;

		// Adjust the current value of the clutch
		if (clutch > 0) {
			double delta = clutchDelta;
			if (sensors.getGear() < 2) {
				// Apply a stronger clutch output when the gear is one and the
				// race is just started
				delta /= 2;
				maxClutch *= clutchMaxModifier;
				if (sensors.getCurrentLapTime() < clutchMaxTime)
					clutch = maxClutch;
			}

			// check clutch is not bigger than maximum values
			clutch = Math.min(maxClutch, clutch);

			// if clutch is not at max value decrease it quite quickly
			if (clutch != maxClutch) {
				clutch -= delta;
				clutch = Math.max((float) 0.0, clutch);
			}
			// if clutch is at max value decrease it very slowly
			else
				clutch -= clutchDec;
		}
		return clutch;
	}

	@Override
	public float[] initAngles() {

		float[] angles = new float[19];

		/*
		 * set angles as
		 * {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90}
		 */
		for (int i = 0; i < 5; i++) {
			angles[i] = -90 + i * 15;
			angles[18 - i] = 90 - i * 15;
		}

		for (int i = 5; i < 9; i++) {
			angles[i] = -20 + (i - 5) * 5;
			angles[18 - i] = 20 - (i - 5) * 5;
		}
		angles[9] = 0;
		return angles;
	}

	@Override
	public void shutdown() {
		super.shutdown();

		if (makingTest) {
			StringBuilder output = new StringBuilder();

			/* Add rows from table to stringbuilder */
			for (ArrayList<Double> row : table) {
				Optional<String> row_str = row.stream().map(Object::toString).reduce((x, y) -> x + " " + y);
				output.append(row_str.get());
				output.append('\n');
			}

			/* Write them to a csv file */
			System.out.println("Saving track data");
			try {
				Files.write(Paths.get(DefaultDriverGenome.fileName), output.toString().getBytes(),
						StandardOpenOption.APPEND, StandardOpenOption.CREATE);
			} catch (IOException e) {
				System.out.println("Could not open file: training.csv");
			}
		}
	}
}