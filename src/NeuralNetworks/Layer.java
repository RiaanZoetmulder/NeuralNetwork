
package NeuralNetworks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

public class Layer implements Serializable {
	
	private static final long serialVersionUID = 1L;

	// list of neurons in layer
	ArrayList<Neuron> NeuronLayer = new ArrayList<Neuron>();
	
	// list of weights in each neuron (Jama matrices)
	ArrayList<Matrix> weightsVecList = new ArrayList<Matrix>();
	
	// Amount of neurons in layer.
	public int NeuronNumber;
	
	// store the output in the layer
	double[][] outputArray;
	
	// constructor
	public Layer(int Neurons,int inputArgs){
		
		// create every neuron in layer
		for(int i = 0; i < Neurons; i++){
			Neuron neuron = new Neuron(i);
			neuron.initializeWeights(inputArgs);
			NeuronLayer.add(neuron);
		}
		NeuronNumber = Neurons;	
	}
	
	// Get size of Layer
	public int getNeuronNumber() {
		return NeuronNumber;
	}
	
	// Pass learning rate to each neuron
	public void setLearningRate(double learningRate){
		for(Neuron n: NeuronLayer){
			n.setLearningRate(learningRate);
		}
	}
	
	// Backpropagation for output layer
	public void backPropagationOutput(double[] outputData){
		
		// Main Loop to update all neurons/ skip over bias 
		for(int i = 0; i < NeuronLayer.size();i++){
			
			 NeuronLayer.get(i).setGradient(outputData[i]);
			 NeuronLayer.get(i).performUpdate();
			 
		}
		
	}
	
	// Backpropagation for hidden layer
	public void backPropagationHidden(double[] deltaPrevious, List<Matrix> weights){
		
		// convert to double array
		double[][] convertedWeights = new double[weights.get(0).getRowDimension()][weights.size()];
		for(int j = 0; j < weights.size();j++){
			for(int k  = 0; k < weights.get(0).getRowDimension();k++){
				convertedWeights[k][j] = weights.get(j).get(k, 0);
			}
		}
		
		// Main Loop to update all neurons
		for(int i = 0; i < NeuronLayer.size();i++){
			
			NeuronLayer.get(i).setGradient(deltaPrevious, convertedWeights[i]);
			NeuronLayer.get(i).performUpdate();
			
		}
	}
	
	// Method to collect all the weights from the output layer
	public List<Matrix> getWeights(){
		
		weightsVecList = new ArrayList<Matrix>();
		for(int i = 0; i< NeuronLayer.size(); i++){
			weightsVecList.add(NeuronLayer.get(i).getWeights());
		}
		
		return weightsVecList;
	}
	
	// Return output
	public double[][] getOutput(Matrix input){
		outputArray = new double[NeuronLayer.size()][1];
		
		// Set input and calculate activation output
		for(int i = 0; i < NeuronLayer.size();i++){
			
			NeuronLayer.get(i).setInput(input);
			outputArray[i][0] = NeuronLayer.get(i).sigmoid();
			
		}
		
		return outputArray;
	}

	// Collect all delta's from Neurons and return as matrix
	public Matrix getDeltas() {
		
		Matrix deltas = new Matrix(NeuronLayer.size(), 1);
		
		for(int i = 0; i < NeuronLayer.size();i++){
			double temp = NeuronLayer.get(i).getDelta();
			deltas.set(i,0, temp);
		}
		return deltas;
	}
	
}
