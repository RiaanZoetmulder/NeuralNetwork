package NeuralNetworks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Jama.Matrix;
import NeuralNetworks.Layer;
import cicontest.torcs.client.SensorModel;

public class NeuralNetwork implements Serializable  {
	
	// Identifier
	private static final long serialVersionUID = 1L;
	
	
	// Arraylists for Neuralnet and vector of in and outputs
	ArrayList<Layer> Net = new ArrayList<Layer>();
	ArrayList<double[][]> inputOutput = new ArrayList<double[][]>();
	List<Matrix> weights = new ArrayList<Matrix>();
	ArrayList<double[][]> testInputOutput = new ArrayList<double[][]>();
	
	// Arguments to make neuralnetwork adaptable to different sizes.
	int outputArgs;
	int inputArgs;
	int hiddenArgs;
	
	// Definitions for trainingdata
	// NOTE: Change according to your data's needs
	final int[] inputcols = {2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22};
	final int[] outputcols = {0,1};
	final int dataSize = inputcols.length + outputcols.length + 1;
	
	
	// class constructor
	public NeuralNetwork(int inputs, int hidden, int outputs){
		outputArgs = outputs;
		inputArgs  = inputs;
		hiddenArgs = hidden;
	}
		
	// Function that initializes the entire neural network
	public void initializeNN(){
		
		// initialize layers
		Layer hiddenLayer = new Layer(hiddenArgs, inputArgs);
		Layer outputLayer = new Layer(outputArgs, hiddenArgs);
		
		// add layers to the NeuralNet.
		Net.add(0,hiddenLayer);
		Net.add(1,outputLayer);
		
	}
	
	public Matrix feedForward(Matrix input){
		Matrix inputMatrix;
		
		// define a matrix for the input and outputs
		inputMatrix = input;
		//System.out.println(inputMatrix.getColumnDimension() +" "+ inputMatrix.getRowDimension());
		Matrix output;
		
		// iterate through the net
		// get output and give it to the next layer
		for(int i = 0; i < Net.size() ; i++ ){
			
			// create a matrix with the output from the previous layer
			output = new Matrix(Net.get(i).getNeuronNumber(), 1);
			
			double[][] test;
			test = Net.get(i).getOutput(inputMatrix);
			output = new Matrix(test);
			output = output.transpose();
			//System.out.println("OutputMatrix Dimenions: "+ output.getColumnDimension() + " " + output.getRowDimension() );
			
			
			
			// output becomes input for the next layer 
			// you only add a bias term once
			//if(i == 0){
			//inputMatrix = addBias(output);
			//}
			//else{
			inputMatrix = output;
				//System.out.println("OutputMatrix Dimenions: "+ inputMatrix.getColumnDimension() + " " + inputMatrix.getRowDimension() );
			//}
		}
		return inputMatrix;	
	}
	
	// Add bias term to matrix
	public Matrix addBias(Matrix input){
		
		// Firstly define some necessary variables
		double[][] inputArray = input.getArray();
		double[][] augmentedArray = new double[1][inputArray[0].length + 1];
				
		// add a row of 1's to the vector 
		for(int i = 0; i < augmentedArray[0].length;i ++){
			if(i == 0)
				augmentedArray[0][i]= 1;
			else
				augmentedArray[0][i]= inputArray[0][i -1];
		}
		
		// return as a matrix
		Matrix inputMatrix = new Matrix(augmentedArray);
		return inputMatrix;
	}
	
	// Method for training the Neural Network
	public void trainNeuralNetwork(int iterations, double learningRate, double threshold){
	
		// set learning rate for each neuron
		for(Layer a:Net)
			a.setLearningRate(learningRate);
		
		// store total Error and the iteration
		double TotalError = 500;
		int iteration = 0;
		
		// repeat until convergence
		while(iteration < 1000){
			
		TotalError = 0;
		iteration++;
		
			// Perform stochastic gradient descent for a number of times
			for(int i = 0; i < iterations; i++){
				
				// separate the input and output
				Matrix input = parseInput(i, false);
				
				Matrix output = parseOutput(i, false);
				
				// Predict output
				// Change dimensions if input changes
				Matrix outputNetwork = this.feedForward(input);
				
				// function to calculate error
				TotalError += Error(output, outputNetwork, false); // works
	
				// get weights outputData (for hidden layer calculation)
				weights = Net.get(1).getWeights(); // works
				
				// perform Backpropagation output Layer
				Net.get(1).backPropagationOutput(output.getArray()[0]); //
				
				//System.out.println(weights.get(0).getColumnDimension() +" "+ weights.get(0).getRowDimension());
				// get Delta's outputLayer
				Matrix deltas = Net.get(1).getDeltas();
				deltas = deltas.transpose();
				//System.out.println("delta's: " +deltas.getColumnDimension() + " " + deltas.getRowDimension());
			
				
				// perform Backpropagation on Hidden Layer
				// Net.get(0).backPropagationHidden(deltas.getArray()[0], weights);
				
			}
			
			// Every 50th iteration print error and iteration
			if(iteration % 50 == 0){
				System.out.println("Iteration: " + iteration);
				System.out.println("TotalError: " + TotalError);
			}
		}
	}
	
	// Calculate the error created by the neural network
	private double Error(Matrix output, Matrix outputNetwork, boolean test) {
		//System.out.println("Dimensions output: " + output.getColumnDimension() +  " " + output.getRowDimension());
		//System.out.println("Dimensions outputNetwork: " + outputNetwork.getColumnDimension() +  " " + outputNetwork.getRowDimension());
		
		double[][] ErrorRaw =output.minus(outputNetwork).getArray();
		double SSE= 0.0;
		for(int i = 0; i < ErrorRaw[0].length;i++){
			SSE +=  Math.pow(ErrorRaw[0][i], 2);
		}
		if(test==true){
			System.out.print("Error: " + SSE + "  ");
			System.out.print("output: " + output.getArray()[0][0]);
			System.out.println(" Networkoutput: " + outputNetwork.getArray()[0][0]);
		}
		return 0.5 * SSE;
	}

	private Matrix parseOutput(int index, boolean test) {
		
		// get the appropriate datapoint, convert to outputmatrix
		int[] row = {0};
		Matrix temp;
		
		if (test == false){
			temp = new Matrix(inputOutput.get(index));
		}else{
			temp = new Matrix(testInputOutput.get(index));
		}
		Matrix output = temp.getMatrix(row, outputcols);
				
		return output;
	}

	private Matrix parseInput(int index, boolean test) {
		
		// get the appropriate datapoint, convert to inputmatrix
		int[] row = {0};
		Matrix temp;
		
		if (test == false){
			temp = new Matrix(inputOutput.get(index));
		}else{
			temp = new Matrix(testInputOutput.get(index));
		}
		Matrix input = temp.getMatrix(row, inputcols);
		return input;
	}

	// Method for reading the data from textfile
	public void readTrainingData(String path, boolean test){
		
		inputOutput.clear();
		
		try{
			// iterate over text file
			BufferedReader in = new BufferedReader(new FileReader(path));
			
			// variables to store each line in and increment rows.
			String str;
			
			// So long as string is not null
			while((str = in.readLine()) != null){
				
				// variable to temporarily store data
				double[][] Data = new double[1][dataSize];
				
				// Pattern match for doubles and parse
				Matcher m = Pattern.compile("-?\\d+(\\.\\d+)?").matcher(str);
				
				int j = 0;
		        while(m.find()) {
		        	
		        	// parse from line of text and add to object
		            Data[0][j] = Double.parseDouble(m.group());
		            j++;
		        }
		        inputOutput.add(Data);
		        testInputOutput.add(Data);
		        
			}
			Collections.shuffle(inputOutput);
			
			in.close();
			
		}catch(Exception E){
			System.out.println("Something Went wrong Parsing the data");
			System.out.println(E);
			E.printStackTrace();
		}
		if (test == false){
			Collections.shuffle(inputOutput);
		}
	}
	
	// Test function to see how the neural net may perform on other data sets
	public void compareOutputs(String path, boolean test){
		
		readTrainingData(path, test);
		
		System.out.println("*******************The Test ********************** ");
		for(int i = 0; i < inputOutput.size();i++){
			Matrix output = parseOutput(i, true);
			Matrix input = parseInput(i, true);
			
			Matrix outputNet = this.feedForward(input);
			Error(output,outputNet, test);
			
			outputNet = null;

		}
		
	}
	
	// Method that runs the data through the Neural network and gets output
	public double[] getOutput(SensorModel a) {
		
		// Parse the sensormodel
		double trackPos[] = a.getTrackEdgeSensors();
		
		
		// System.out.println("Sensor "+ 1 + " :" + trackEdgeSensor[0] +" " + trackEdgeSensor[1]);
		double speed = a.getSpeed();
		double angle = a.getAngleToTrackAxis();
		double [][] inputArray =  new double[1][21];
		
		
		// truncate all the values of the sensors
		for(int i = 0; i < trackPos.length + 2; i++){
			if(i < trackPos.length){
						
				double temp = normalizeTrackSensors(trackPos[i]);
				inputArray[0][i] = temp;
						
			}else{
						
				inputArray[0][i] = speed/500;
				inputArray[0][i + 1] = ((angle + Math.PI) / (2 *Math.PI));
				break;
						
			}
		}
		
//		allSensors[0][0] = normalizeTrackSensors(trackPos[9]);
//		allSensors[0][1] = normalizeTrackSensors(trackPos[11]);
//		allSensors[0][2] = normalizeSpeed(a.getSpeed());
//		allSensors[0][3] = normalizeAngleSensors(a.getAngleToTrackAxis());
//		allSensors[0][4] = a.getTrackPosition();
		Matrix inputMatrix = new Matrix(inputArray);
		Matrix outputMatrix = this.feedForward(inputMatrix);
		System.out.println("outputMatrix value C/R:" + outputMatrix.get(0,0));
		
		return outputMatrix.getArray()[0];
		
		
		
	}
		private static double normalizeTrackSensors(double x)
		{
		    long y= (long) (x*100000);
		    return (double) (y/100000)/200;
		}
		private static double normalizeSpeed(double x)
		{
			DecimalFormat df = new DecimalFormat("##.#######");
			df.setRoundingMode(RoundingMode.DOWN);
		    return Double.parseDouble(df.format(x));
		}
		
		private static double normalizeAngleSensors(double x)
		{
		    float y= (float) (x*100000);
		    return (double) (((y/100000) + Math.PI) /(2 *Math.PI) );
		}
}
