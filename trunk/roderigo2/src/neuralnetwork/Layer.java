package neuralnetwork;

// The following java code is based on a multi-layer 
// Back Propagation Neural Network Class (NeuralNetwork.class)
//
// Created by Anthony J. Papagelis & Dong Soo Kim
//
//  DateCreated:	15 September, 2001
//  Last Update:	14 October, 2001

public class Layer {
	private double net;
	
	public double input[];		
	// Vector of inputs signals from previous 
	// layer to the current layer

	public Node node[];		
	// Vector of nodes in current layer

	public Layer(int numberOfNodes, int numberOfInputs) {
		node = new Node[numberOfNodes];

		for (int i = 0; i < numberOfNodes; i++)
			node[i] = new Node(numberOfInputs);

		input = new double[numberOfInputs];
	}

	public void initializeRandomWeights(double mult) {
		double epsilon = mult * Math.sqrt(6.0) / Math.sqrt(node.length + input.length);
		for(Node n : node)
			n.initializeRandomWeights(epsilon);
	}

	// The FeedForward function is called so that 
	// the outputs for all the nodes in the current 
	// layer are calculated
	public void feedForward() {
		for(int i = 0; i < node.length; i++) {
			net = node[i].threshold;

			for(int j = 0; j < node[i].weight.length; j++)
				net += input[j] * node[i].weight[j];

			node[i].output = Math.pow(1 + Math.exp(-net), -1);
		}
	}

	public void setInput(double input[]) {
		for(int i = 0; i < this.input.length; i++)
			this.input[i] = input[i];
	}
	
	// Return the output from all node in the layer
	// in a vector form
	public double[] getOutput() {
		double ret[] = new double[node.length];
		for(int i = 0; i < node.length; i++)
			ret[i] = node[i].output;
		return ret;
	}

	// added by DSK
	public Node[] getNodes() {
		return node;
	}
}
