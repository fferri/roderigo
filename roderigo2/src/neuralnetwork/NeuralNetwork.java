package neuralnetwork;

// The following java code is based on a multi-layer 
// Back Propagation Neural Network Class (NeuralNetwork.class)
//
// Created by Anthony J. Papagelis & Dong Soo Kim
//
//  DateCreated:	15 September, 2001
//  Last Update:	24 October, 2001

public class NeuralNetwork {
	public Layer layer[];

	public NeuralNetwork(int...numUnits) {
		layer = new Layer[numUnits.length];

		layer[0] = new Layer(numUnits[0], numUnits[0]);

		for(int i = 1; i < numUnits.length; i++) 
			layer[i] = new Layer(numUnits[i], numUnits[i - 1]);
	}
	
	public void initializeRandomWeights(double mult) {
		for(Layer l : layer)
			l.initializeRandomWeights(mult);
	}
	
	public Layer getInputLayer() {
		return layer[0];
	}
	
	public Layer getOutputLayer() {
		return layer[layer.length - 1];
	}
	
	public int getNumInputs() {
		return getInputLayer().node.length;
	}
	
	public int getNumOutputs() {
		return getOutputLayer().node.length;
	}
	
	public void setInput(double input[]) {
		getInputLayer().setInput(input);
	}
	
	public double[] getOutput() {
		return getOutputLayer().getOutput();
	}

	public void feedForward(){
		// since no weights contribute to the output 
		// vector from the input layer,
		// assign the input vector from the input layer 
		// to all the node in the first hidden layer
		for(int i = 0; i < layer[0].node.length; i++)
			layer[0].node[i].output = layer[0].input[i];

		layer[1].input = layer[0].input;
		for(int i = 1; i < layer.length; i++) {
			layer[i].feedForward();

			// unless we have reached the last layer, assign the layer i's output vector
			// to the (i+1) layer's input vector
			if(i != layer.length - 1)
				layer[i + 1].input = layer[i].getOutput();
		}
	}

	// back propagated the network outputy error through 
	// the network to update the weight values
	public void updateWeights(double expectedOutput[], double learningRate, double momentum) {
		double sum;
		Layer outputLayer = layer[layer.length - 1];
		
		// calculate signal errors in the output layer
		for(int i = 0; i < outputLayer.node.length; i++) {
			Node n = outputLayer.node[i];
			n.signalError = (expectedOutput[i] - n.output) * n.output * (1 - n.output);
		}

		// calculate signal errors in the hidden layers
		// (back propagate the errors)
		for(int i = layer.length - 2; i > 0; i--) {
			Layer l = layer[i];
			Layer l1 = layer[i + 1];
			for(int j = 0; j < l.node.length; j++) {
				Node n = l.node[j];
				sum = 0;
				for(int k = 0; k < l1.node.length; k++) {
					Node n1 = l1.node[k];
					sum += n1.weight[j] * n1.signalError;
				}
				n.signalError = n.output * (1 - n.output) * sum;
			}
		}
		
		// backpropagation (weight update)
		for(int i = layer.length - 1; i > 0; i--) {
			Layer l = layer[i];
			for(int j = 0; j < l.node.length; j++) {
				Node n = l.node[j];
				// calculate bias weight difference to node j
				n.thresholdDiff = learningRate * n.signalError + momentum * n.thresholdDiff;

				// update bias weight to node j
				n.threshold += n.thresholdDiff;

				// update weights
				for(int k = 0; k < l.input.length; k++) {
					// calculate weight difference between node j and k
					n.weightDiff[k] = learningRate * n.signalError * layer[i - 1].node[k].output + momentum * n.weightDiff[k];

					// update weight between node j and k
					n.weight[k] += n.weightDiff[k];
				}
			}
		}
	}

	public void epoch(double inputs[][], double outputs[][], double learningRate, double momentum) {
		for(int sampleNumber = 0; sampleNumber < inputs.length; sampleNumber++) {
			getInputLayer().setInput(inputs[sampleNumber]);
			feedForward();
			updateWeights(outputs[sampleNumber], learningRate, momentum);
		}
	}
	
	public void train(double inputs[][], double outputs[][], double learningRate, double momentum, double minimumError, long maximumNumberOfIterations) {
		long k = 0;
		double overallError;
		do {
			for(int sampleNumber = 0; sampleNumber < inputs.length; sampleNumber++) {
				getInputLayer().setInput(inputs[sampleNumber]);
				feedForward();
				updateWeights(outputs[sampleNumber], learningRate, momentum);
			}

			k++;
			
			overallError = 0;
			for(int i = 0; i < inputs.length; i++) {
				getInputLayer().setInput(inputs[i]);
				feedForward();
				double actualOutput[] = getOutput();
				for(int j = 0; j < layer[layer.length - 1].node.length; j++) {
					overallError += 0.5 * (Math.pow(outputs[i][j] - actualOutput[j], 2));
				}
			}
		} while((overallError > minimumError) && (k < maximumNumberOfIterations));
	}

	// needed to implement the drawing of the network.
	public Layer[] getLayers() {
		return layer;
	}
}

