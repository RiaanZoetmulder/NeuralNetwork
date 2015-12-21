package torcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
//import java.net.ConnectException;
import java.util.ArrayList;
//import java.util.Arrays;

//import org.neuroph.core.Neuron;
//import org.neuroph.core.Layer;
//import org.neuroph.core.Connection;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

import cicontest.torcs.client.SensorModel;

public class NeuralNetwork {
	
	/* File names */
	public static final String dummyNet = "memory/nnAccelSteer.mem";
	public static final String steerNet = "memory/nnSteerNew2.mem";
	public static final String accelNet = "memory/nnAccelNew.mem";
	public static final String brakeNet = "memory/nnBrakeNew.mem";

	/* Neural Network specs */
	public MultiLayerPerceptron network;
	public static int inputNeurons = 3;
	public static int hiddenNeurons = 1;
	public static int outputNeurons = 2;
		
	/* Iterations for training */
	private int iterations = 100;

	/* Normalizing constants */
	public static double[] maxValuesS = {9.14728, 10.4267, 87.7221, 200.0, 200.0, 200.0, 
										 200.0, 200.0, 200.0, 200.0, 200.0, 200.0, 200.0,
										 200.0, 200.0, 59.2154, 12.9906, 11.7074, 11.4047 };
	public static double[] maxValuesA = {10.9};
	public static double[] maxValuesM = {200.,200.,200.,200.};
	public static double[] maxValuesB = {150.,150.,150.,150.,200.,200.,200.,200.};


	/* 
	 * Training and making the Neural Network
	 */
	NeuralNetwork(DataSet trainingset) {
		normalize(trainingset);
		network = new MultiLayerPerceptron(TransferFunctionType.TANH, inputNeurons, outputNeurons);
		
//		network.removeLayerAt(1);
		
		System.out.println(network.getWeights().length);
		
//		Neuron inputAngle  = network.getInputNeurons()[0];
//		Neuron inputEdge   = network.getInputNeurons()[1];
//		Neuron inputSpeed  = network.getInputNeurons()[2];
//		Neuron outputAccel = network.getOutputNeurons()[0];
//		Neuron outputSteer = network.getOutputNeurons()[1];

//		inputAngle.removeAllConnections();
//		inputEdge.removeAllConnections();
//		inputSpeed.removeAllConnections();
//		outputAccel.removeAllConnections();
//		outputSteer.removeAllConnections();
		
//		network.createConnection(inputEdge,  outputAccel, 0.1);
//		network.createConnection(inputSpeed, outputAccel, 0.1);
//		network.createConnection(inputAngle, outputSteer, 0.1);
//		network.createConnection(outputAccel,  inputEdge, 0.1);
//		network.createConnection(outputAccel, inputSpeed, 0.1);
//		network.createConnection(outputSteer, inputAngle, 0.1);
		
//		network.reset();
		
//		network.randomizeWeights();

		System.out.println(network.getWeights().length);
//		network.connectInputsToOutputs();
		
		
		for (int i = 0; i < iterations; i++) {
			errorNeuralNetwork(network, trainingset);
			network.learn(trainingset, new BackPropagation());
		}
		errorNeuralNetwork(network, trainingset);
	}

	/* 
	 * Normalizing 
	 */
	public void normalize(DataSet testSet) {
		for (DataSetRow dataRow : testSet.getRows()) {
			double[] row = dataRow.getInput();
			row[1] = row[1] / 200.;

			row[2] = row[2] / 200.;
//			for (int i = 0; i < inputNeurons; i++)
//				row[i] = row[i] / 200.;
			dataRow.setInput(row);
		}
	}

	/* 
	 * Compute Error 
	 */
	public static void errorNeuralNetwork(MultiLayerPerceptron nnet, DataSet testSet) {

		float error = 0;

		for (DataSetRow dataRow : testSet.getRows()) {
			nnet.setInput(dataRow.getInput());
			nnet.calculate();
			double[] networkOutput = nnet.getOutput();

			for (int i = 0; i < networkOutput.length; i++)
				error += Math.abs(networkOutput[i] - dataRow.getDesiredOutput()[i]);
		}
		System.out.println("Total error:" + error);
	}

	/* 
	 * Loading the Neural Network from memory 
	 * 1. Check if NN comes from within JAR
	 * 2. If not get it with FileInputStream
	 */
	NeuralNetwork(String url) {
		load(url);
	}

	public boolean inJar() {
		String classname = getClass().getName();
		classname = classname.split("\\.")[classname.split("\\.").length - 1];
		String bp = (new File(getClass().getResource(classname + ".class").getPath())).getParent();
		return bp.contains("!");
	}

	public void load(String url) {
        if (inJar()) {
            InputStream in = getClass().getClassLoader().getResourceAsStream(url);
            this.network = (MultiLayerPerceptron) org.neuroph.core.NeuralNetwork.load(in);
        } else {
        	InputStream in;
			try {
				in = new FileInputStream(url);
                this.network = (MultiLayerPerceptron) org.neuroph.core.NeuralNetwork.load(in);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
        }
    }

	/* 
	 * Saving the Neural Network 
	 */
	public void save(String url) {
		this.network.save(url);
	}

	/* 
	 * Getting output
	 * 1. Acceleration
	 * 2. Steering
	 * 3. Braking
	 */
	public double getAccel(SensorModel sensors) {
		ArrayList<Double> vector = new ArrayList<>();
//		vector.add(sensors.getTrackEdgeSensors()[10]);
		vector.add(sensors.getTrackEdgeSensors()[9]);
//		vector.add(sensors.getTrackEdgeSensors()[8]);
		vector.add(sensors.getSpeed());
		double[] networkInput = DefaultDriverGenome.toPrimitive(vector.toArray(new Double[0]));
		for (int i = 0; i < networkInput.length; i++) networkInput[i] = networkInput[i] / 200.;
		network.setInput(networkInput);
		network.calculate();
		return network.getOutput()[0];
	}
	

	public double getSteer(SensorModel sensors) {
		ArrayList<Double> vector = new ArrayList<>();
		for (double v : sensors.getTrackEdgeSensors()) vector.add(v);
		double[] networkInput = DefaultDriverGenome.toPrimitive(vector.toArray(new Double[0]));
		for (int i = 0; i < 19; i++) networkInput[i] = networkInput[i] / maxValuesS[i];
//		network.setInput(networkInput);
		double[] angle = {sensors.getAngleToTrackAxis()};
		network.setInput(angle);
		network.calculate();
		return network.getOutput()[0];
	}

	public double getBrake(SensorModel sensors) {
		ArrayList<Double> vector = new ArrayList<>();
//		Arrays.stream(sensors.getWheelSpinVelocity()).forEach(vector::add);			
//		vector.add(sensors.getTrackEdgeSensors()[10]);
		vector.add(sensors.getTrackEdgeSensors()[9]);
//		vector.add(sensors.getTrackEdgeSensors()[8]);
//		vector.add(sensors.getSpeed());
		double[] networkInput = DefaultDriverGenome.toPrimitive(vector.toArray(new Double[0]));
		for (int i = 0; i < networkInput.length; i++) networkInput[i] = networkInput[i] / 200.;
		network.setInput(networkInput);
		network.calculate();
		return network.getOutput()[0];
	}
	
	public double[] getAccelSteer(SensorModel sensors) {
		ArrayList<Double> vector = new ArrayList<>();
		vector.add(sensors.getAngleToTrackAxis());
		vector.add(sensors.getTrackEdgeSensors()[9]);
		vector.add(sensors.getSpeed());
		double[] networkInput = DefaultDriverGenome.toPrimitive(vector.toArray(new Double[0]));
		networkInput[1] = networkInput[1] / 200.;
		networkInput[2] = networkInput[2] / 200.;
		network.setInput(networkInput);
		network.calculate();
		return network.getOutput();
	}
	
}
