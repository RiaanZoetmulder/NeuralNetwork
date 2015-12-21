package NeuralNetworks;
import java.io.Serializable;
import java.lang.Math;
import Jama.Matrix;

public class Neuron implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// Variables to store the output, learning rate, position in the layer and delta
	int positionLayer;
	double learnRate;
	double output;
	double delta;
	
	// Matrices for the input, weights and gradient of the neuron
	Matrix input;
	Matrix weights;
	Matrix gradient;

	Neuron(int numInLayer){		
		positionLayer = numInLayer;
	}
	
	// function to set the learning rate
	public void setLearningRate(double learningRate) {
		
		learnRate = learningRate;
		
	}
	
	// Should get a vector with term 0 as bias term and 
	// other inputs
	public void setInput(Matrix inputMatrix) {
		
		input = inputMatrix;
		
	}
	
	// Initializes the correct number of weights
	public void initializeWeights(int numberOfWeights) {
		
		weights = Matrix.random(numberOfWeights, 1);
		Matrix temp = new Matrix(numberOfWeights, 1, -0.5 );
		weights.plusEquals(temp);
		weights.set(0, 0, 1.0);
		
	}
	
	
	// Sigmoid function
	public double sigmoid(){
		
		Matrix power = input.times(weights);
		output = (1.0/(1.0 + Math.exp(-power.get(0, 0))));
		return output;
		
	}

	
    // Calculates the gradient for this neuron
	// Use only if neuron is in OUTPUT Layer
	public void setGradient(double outputData) {
		
		delta = (output - outputData)*output* (1 - output);
		gradient = input.times(learnRate* delta);
		
	}
	
	
	// Calculates the gradient for this neuron
	// Use only if neuron is in the HIDDEN Layer
	public void setGradient(double[] deltaPrev, double[] weightsPrev) {
		
		//System.out.println("weights and deltaprev:"+ deltaPrev.length +" "+ weightsPrev.length);
		
		double sum = 0;
		for(int i = 0; i < deltaPrev.length; i++){
			sum += deltaPrev[i]*weightsPrev[i];
		}
		
		delta = output*(1-output)*sum;
		gradient = input.times(learnRate*delta);
		
	}
	
	// Getter method for getting delta
	public double getDelta(){
		return delta;
	}
		
	// returns the Matrix of weights
	public Matrix getWeights(){
		
		return weights;
		
	}
	
	// perform sgd
	public void performUpdate(){
		
		weights = weights.minus(gradient.transpose());
		
	}
	
}
