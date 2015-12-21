package torcs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.neuroph.core.data.DataSet;

import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {

	private static final long serialVersionUID = 6534186543165341653L;
	
	/* File name */
	public static String fileName = "training/fast3-2_steer_accel.csv";
	
	/* Neural Networks */
	private NeuralNetwork myNN;
	private NeuralNetwork nnAccel;
	private NeuralNetwork nnSteer;
	private NeuralNetwork nnBrake;

	/*
	 * Constructor
	 * loading nets
	 */
	public DefaultDriverGenome() {
		
		String dummyUrl = NeuralNetwork.dummyNet;
		String steerUrl = NeuralNetwork.steerNet;
		String accelUrl = NeuralNetwork.accelNet;
		String brakeUrl = NeuralNetwork.brakeNet;

		myNN    = new NeuralNetwork(dummyUrl);
		nnSteer = new NeuralNetwork(steerUrl);
		nnAccel = new NeuralNetwork(accelUrl);
		nnBrake = new NeuralNetwork(brakeUrl);
	}
	

	/*
	 * Passing NN objects
	 */
	public NeuralNetwork getMyNN()    {return myNN;}
	public NeuralNetwork getNNAccel() {return nnAccel;}
	public NeuralNetwork getNNSteer() {return nnSteer;}
	public NeuralNetwork getNNBrake() {return nnBrake;	}

	/*
	 * Helper function
	 */
	public static double[] toPrimitive(Double[] array) {
		double[] out = new double[array.length];
		for (int i = 0; i < array.length; ++i)
			out[i] = array[i];
		return out;
	}

	/* 
	 * Main Function
	 * 1. Construct data set
	 * 2. Train the a NN with the data set
	 * 3. Save the NN in memory
	 */
	public static void main(String[] args) throws IOException {
		
		// Setting it up
		DataSet trainingSet = new DataSet(NeuralNetwork.inputNeurons, NeuralNetwork.outputNeurons);
		String line;
		BufferedReader reader;

		// Loading training data from file
		try {
			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(" ");
				ArrayList<Double> row = Arrays.stream(tokens).map(Double::parseDouble)
						.collect(Collectors.toCollection(ArrayList::new));

				double[] input = toPrimitive(row.subList(0, NeuralNetwork.inputNeurons).toArray(new Double[0]));
				double[] output = toPrimitive(row
						.subList(NeuralNetwork.inputNeurons, (NeuralNetwork.inputNeurons + NeuralNetwork.outputNeurons))
						.toArray(new Double[0]));
				trainingSet.addRow(input, output);

			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		// Train and save the net
		NeuralNetwork neuralNetwork = new NeuralNetwork(trainingSet);
		neuralNetwork.save(NeuralNetwork.dummyNet);
	}
}
