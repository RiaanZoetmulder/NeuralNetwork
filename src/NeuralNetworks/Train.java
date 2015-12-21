package NeuralNetworks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import Jama.Matrix;
import NeuralNetworks.Neuron;

public class Train { 
	public static void main(String[] args){
		
		boolean train = true;
		
		// Train the Neural network
		if(train == true){
			
			NeuralNetwork myNN = new NeuralNetwork(21, 45, 2);
			myNN.initializeNN();
			
			// Read the trainingdata and train
			myNN.readTrainingData("TrainingData.txt", false);
			
			// record time
			final long startTime = System.currentTimeMillis();
			
			// train the neural network
			myNN.trainNeuralNetwork(1000, 0.1, 1);
			
			// record time
			final long endTime = System.currentTimeMillis();

			System.out.println("Total execution time: " + (endTime - startTime) );


			// Show the output of a test set
			// myNN.compareOutputs("TrainingData.txt", true);
			
			
			// save
			save(myNN);
		}
		
		// Load the neural network 
		else{
			NeuralNetwork  myNN = load();
			
			// show that the output is thesame
			// myNN.compareOutputs("TrainingData.txt", true);
			
		}
	}
	
	/*
	 * Functions to respectively load and save the Neural Network
	 * Change the path to where your memfile is saved on your computer
	 */
	private static NeuralNetwork load() {
		
		System.out.println("Attempting to load!");
		
		try {
			
			// open file where network is stored in load and return
			FileInputStream f_in = new FileInputStream("memory/NeuralNetwork.mem");

			ObjectInputStream obj_in = new ObjectInputStream (f_in);
			return (NeuralNetwork) obj_in.readObject();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Object input stream problem");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Cannot find the class stored in the mem file!");
		}
		return null;
	}

	private static void save(NeuralNetwork NN) {
		
		// Saves the state of the neural network
		try{
			
			// open the .mem file
			ObjectOutputStream out = new ObjectOutputStream(new
					FileOutputStream("memory/NeuralNetwork.mem"));
		    out.writeObject(NN);
		    
		    System.out.println("Saving!");
			
		}catch(Exception E){
			E.printStackTrace();
		}
		
	}

}
