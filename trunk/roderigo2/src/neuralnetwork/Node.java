package neuralnetwork;

// The following java code is based on a multi-layer 
// Back Propagation Neural Network Class (NeuralNetwork.class)
//
// Created by Anthony J. Papagelis & Dong Soo Kim
//
//  DateCreated:	15 September, 2001
// Last Update:	14 October, 2001

public class Node {
	public double output;		
	// output signal from current node

	public double weight[];		
	// Vector of weights from previous nodes to current node

	public double threshold;	
	// node threshold /Bias

	public double weightDiff[];	
	// weight difference between the nth and the (n-1) iteration

	public double thresholdDiff;	
	// threshold difference between the nth and the (n-1) iteration

	public double signalError;	
	// output signal error

	public Node(int numberOfNodes) {
		weight = new double[numberOfNodes];		
		// Create an array of weight with the same 
		// size as the vector of inputs to the node

		weightDiff = new double[numberOfNodes];	
		// Create an array of weightDiff with the same 
		// size as the vector of inputs to the node
	}

	// InitialiseWeights function assigns a randomly 
	// generated number, between -1 and 1, to the 
	// threshold and Weights to the current node
	public void initializeRandomWeights(double mult) {
		threshold = (2 * Math.random() - 1) * mult;	    	
		// Initialise threshold nodes with a random 
		// number between -1 and 1

		thresholdDiff = 0;				
		// Initially, thresholdDiff is assigned to 0 so 
		// that the Momentum term can work during the 1st 
		// iteration

		for(int i = 0; i < weight.length; i++) {
			weight[i]= (2 * Math.random() - 1) * mult;	
			// Initialise all weight inputs with a 
			// random number between -1 and 1

			weightDiff[i] = 0;			
			// Initially, weightDiff is assigned to 0 
			// so that the Momentum term can work during 
			// the 1st iteration
		}
	}

	public double[] getWeights() {
		return weight;
	}
	
	public double getOutput() {
		return output;
	}
}
