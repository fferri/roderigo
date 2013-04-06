package neuralnet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class NeuralNetwork implements Serializable {
	private static final long serialVersionUID = 7526472295622776147L;
	
	transient Random rand = new Random();
	
	ArrayList<Neuron> inputLayer = new ArrayList<Neuron>();
	ArrayList<Neuron> hiddenLayer = new ArrayList<Neuron>();
	ArrayList<Neuron> outputLayer = new ArrayList<Neuron>();
	Neuron bias = new Neuron();

	private static final double epsilon = 0.00000000001;

	public NeuralNetwork(int input, int hidden, int output) {

		// input layer
		for(int j = 0; j < input; j++) {
			Neuron neuron = new Neuron();
			inputLayer.add(neuron);
		}

		// hidden layer
		for(int j = 0; j < hidden; j++) {
			Neuron neuron = new Neuron();
			neuron.addInConnectionsS(inputLayer);
			neuron.addBiasConnection(bias);
			hiddenLayer.add(neuron);
		}
		
		// output layer
		for(int j = 0; j < output; j++) {
			Neuron neuron = new Neuron();
			neuron.addInConnectionsS(hiddenLayer);
			neuron.addBiasConnection(bias);
			outputLayer.add(neuron);
		}

		initializeRandomWeights(0.9);
		
		// reset id counters
		Neuron.counter = 0;
		Connection.counter = 0;
	}
	
	public int getNumInputs() {
		return inputLayer.size();
	}
	
	public int getNumHidden() {
		return hiddenLayer.size();
	}
	
	public int getNumOutputs() {
		return outputLayer.size();
	}

	public void initializeRandomWeights(double mult) {
		for(Neuron neuron : hiddenLayer) {
			ArrayList<Connection> connections = neuron.getAllInConnections();
			for(Connection conn : connections) {
				double newWeight = mult * (rand.nextDouble() * 2 - 1);
				conn.setWeight(newWeight);
			}
		}
		for(Neuron neuron : outputLayer) {
			ArrayList<Connection> connections = neuron.getAllInConnections();
			for(Connection conn : connections) {
				double newWeight = mult * (rand.nextDouble() * 2 - 1);
				conn.setWeight(newWeight);
			}
		}
	}

	/**
	 * 
	 * @param inputs
	 *            There is equally many neurons in the input layer as there are
	 *            in input variables
	 */
	public void setInput(double inputs[]) {
		for(int i = 0; i < inputLayer.size(); i++) {
			inputLayer.get(i).setOutput(inputs[i]);
		}
	}

	public double[] getOutput() {
		double[] outputs = new double[outputLayer.size()];
		for(int i = 0; i < outputLayer.size(); i++)
			outputs[i] = outputLayer.get(i).getOutput();
		return outputs;
	}

	/**
	 * Calculate the output of the neural network based on the input The forward
	 * operation
	 */
	public void activate() {
		for(Neuron n : hiddenLayer)
			n.calculateOutput();
		for(Neuron n : outputLayer)
			n.calculateOutput();
	}

	/**
	 * all output propagate back
	 * 
	 * @param expectedOutput
	 *            first calculate the partial derivative of the error with
	 *            respect to each of the weight leading into the output neurons
	 *            bias is also updated here
	 */
	public void applyBackpropagation(double expectedOutput[], double learningRate, double momentum) {

		// error check, normalize value ]0;1[
		for(int i = 0; i < expectedOutput.length; i++) {
			double d = expectedOutput[i];
			if(d < 0 || d > 1) {
				if(d < 0)
					expectedOutput[i] = 0 + epsilon;
				else
					expectedOutput[i] = 1 - epsilon;
			}
		}

		int i = 0;
		for(Neuron n : outputLayer) {
			ArrayList<Connection> connections = n.getAllInConnections();
			for(Connection con : connections) {
				double ak = n.getOutput();
				double ai = con.leftNeuron.getOutput();
				double desiredOutput = expectedOutput[i];

				double partialDerivative = -ak * (1 - ak) * ai * (desiredOutput - ak);
				double deltaWeight = -learningRate * partialDerivative;
				double newWeight = con.getWeight() + deltaWeight;
				con.setDeltaWeight(deltaWeight);
				con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
			}
			i++;
		}

		// update weights for the hidden layer
		for(Neuron n : hiddenLayer) {
			ArrayList<Connection> connections = n.getAllInConnections();
			for(Connection con : connections) {
				double aj = n.getOutput();
				double ai = con.leftNeuron.getOutput();
				double sumKoutputs = 0;
				int j = 0;
				for(Neuron out_neu : outputLayer) {
					double wjk = out_neu.getConnection(n.id).getWeight();
					double desiredOutput = (double)expectedOutput[j];
					double ak = out_neu.getOutput();
					j++;
					sumKoutputs = sumKoutputs + (-(desiredOutput - ak) * ak * (1 - ak) * wjk);
				}

				double partialDerivative = aj * (1 - aj) * ai * sumKoutputs;
				double deltaWeight = -learningRate * partialDerivative;
				double newWeight = con.getWeight() + deltaWeight;
				con.setDeltaWeight(deltaWeight);
				con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
			}
		}
	}

	public void train(double[][] inputs, double[][] expectedOutputs, int maxEpochs, double maxError) {
		train(inputs, expectedOutputs, maxEpochs, maxError, 0.9);
	}
	
	public void train(double[][] inputs, double[][] expectedOutputs, int maxEpochs, double maxError, double learningRate) {
		train(inputs, expectedOutputs, maxEpochs, maxError, learningRate, 0.7);
	}
	
	public void train(double[][] inputs, double[][] expectedOutputs, int maxEpochs, double maxError, double learningRate, double momentum) {
		for(int epoch = 0; epoch < maxEpochs; epoch++) {
			double mse = getMeanSquareError(inputs, expectedOutputs);
			if(mse < maxError) break;
			epoch(inputs, expectedOutputs, learningRate, momentum);
		}
	}
	
	public void epoch(double[][] inputs, double[][] expectedOutputs, double learningRate, double momentum) {
		for(int p = 0; p < inputs.length; p++) {
			setInput(inputs[p]);
			activate();
			applyBackpropagation(expectedOutputs[p], learningRate, momentum);
		}
	}
	
	public double getMeanSquareError(double[][] inputs, double[][] expectedOutputs) {
		double error = 0, output[];
		for(int p = 0; p < inputs.length; p++) {
			setInput(inputs[p]);
			activate();
			output = getOutput();
			for(int j = 0; j < expectedOutputs[p].length; j++)
				error += Math.pow(output[j] - expectedOutputs[p][j], 2);
		}
		return error / inputs.length;
	}

	private void readObject(ObjectInputStream inputStream) throws ClassNotFoundException, IOException {
		inputStream.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		outputStream.defaultWriteObject();
	}
}