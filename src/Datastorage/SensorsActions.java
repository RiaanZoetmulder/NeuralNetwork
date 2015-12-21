package Datastorage;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/* 
 * Class: Sensor Actions
 * Object with a selection of different data from sensors and 
 * actions taken with them
 */
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;

public class SensorsActions {
	
	
	private SensorModel sensorObject;
	private Action actionObject;
	private double[] focusSensors;
	private double[] allSensors = new double[21];
	private double[] actions = new double[2];
	
	
	public SensorsActions(SensorModel sensors, Action actions){
		sensorObject = sensors;
		actionObject = actions;
	}
	public void Parse(){
		
		// Get a whole bunch of different sensors
		focusSensors = sensorObject.getTrackEdgeSensors();
		
		
		// truncate all the values of the sensors
		for(int i = 0; i < focusSensors.length + 1; i++){
			if(i < focusSensors.length){
				
				double temp = normalizeTrackSensors(focusSensors[i]);
				allSensors[i] = temp;
				
			}else{
				
				allSensors[i] = normalizeSpeed(sensorObject.getSpeed());
				allSensors[i + 1] = normalizeAngleSensors(sensorObject.getAngleToTrackAxis());
				
			}
			
		}
//		allSensors[0] = normalizeSpeed(sensorObject.getSpeed());
//		allSensors[1] = normalizeAngleSensors(sensorObject.getAngleToTrackAxis());
//		allSensors[2] = sensorObject.getTrackPosition();
//		allSensors[0] = normalizeTrackSensors(focusSensors[9]);
//		allSensors[1] = normalizeTrackSensors(focusSensors[11]);
//		allSensors[2] = normalizeSpeed(sensorObject.getSpeed());
//		allSensors[3] = normalizeAngleSensors(sensorObject.getAngleToTrackAxis());
//		allSensors[4] = normalizeTrackSensors(focusSensors[5]);
		
		// Get all actions
		actions[1] = truncate(0.2 +(actionObject.accelerate/1.6667));
		//actions[1] = truncate(actionObject.brake);
		actions[0] = truncate(normalize(actionObject.steering));
		//actions[3] = truncate(actionObject.clutch);
		
	}
	
	private double normalize(double steering) {
		// Steering requires normalization 
		// Neural Network cannot deal with values below 0
		double normalizedValue = 0.1 + (((steering + 1.0) / 2.0)/ 1.25);
		
		
		return normalizedValue;
	}
	// function to truncate doubles
	// otherwise storing them will cause scientific notation in .txt file
	private static double truncate(double x)
	{
		DecimalFormat df = new DecimalFormat("##.######");
		df.setRoundingMode(RoundingMode.DOWN);
	    //float y= (float) (x*100000);
		
	    return Double.parseDouble( df.format(x));
	}
	private static double normalizeTrackSensors(double x)
	{
		
		DecimalFormat df = new DecimalFormat("##.######");
		df.setRoundingMode(RoundingMode.DOWN);
	    
	    return Double.parseDouble( df.format(x/200));
	}
	private static double normalizeSpeed(double x)
	{
		DecimalFormat df = new DecimalFormat("##.######");
		df.setRoundingMode(RoundingMode.DOWN);
	    return Double.parseDouble(df.format(x/500));
	}
	
	private static double normalizeAngleSensors(double x)
	{
		
		DecimalFormat df = new DecimalFormat("##.######");
		df.setRoundingMode(RoundingMode.DOWN);
	    
	    return Double.parseDouble( df.format((((x) + Math.PI) /(2 *Math.PI) )));
	}
	
	private static double truncateTrackPosition(double x){
		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		
		return Double.parseDouble(df.format(x));
	}
	public double[] getSensors() {
		return allSensors;
	}
	public double[] getActions() {
		return actions;
	}
	
	

}
