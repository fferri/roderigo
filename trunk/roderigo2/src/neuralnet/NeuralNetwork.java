package neuralnet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class NeuralNetwork implements Serializable {
	private static final long serialVersionUID = 7526472295622776147L;
	
	transient Random rand = new Random();

	List<Layer> layers;
	
	private static final double epsilon = 0.00000000001;

	public NeuralNetwork(int...numUnits) {
		layers = new ArrayList<>(numUnits.length);
		
		Layer prevLayer = null;
		for(int i = 0; i < numUnits.length; i++) {
			Layer layer = new Layer(numUnits[i], prevLayer);
			layers.add(layer);
			prevLayer = layer;
		}

		initializeRandomWeights(0.9);
		
		// reset id counters
		Neuron.counter = 0;
		Connection.counter = 0;
	}
	
	public Layer getInputLayer() {
		return layers.get(0);
	}
	
	public Layer getOutputLayer() {
		return layers.get(layers.size() - 1);
	}
	
	public int getNumInputs() {
		return getInputLayer().size();
	}
	
	public int getNumOutputs() {
		return getOutputLayer().size();
	}
	
	public void initializeRandomWeights(double mult) {
		for(int layer = 1; layer < layers.size(); layer++) {
			layers.get(layer).initializeRandomWeights(mult);
		}
	}

	/**
	 * 
	 * @param inputs
	 *            There is equally many neurons in the input layer as there are
	 *            in input variables
	 */
	public void setInput(double inputs[]) {
		getInputLayer().setInput(inputs);
	}

	public double[] getOutput() {
		return getOutputLayer().getOutput();
	}

	/**
	 * Calculate the output of the neural network based on the input The forward
	 * operation
	 */
	public void activate() {
		for(int layer = 1; layer < layers.size(); layer++) {
			layers.get(layer).activate();
		}
	}

	/**
	 * all output propagate back
	 * call this after NN activation (aka forward pass)
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
		for(Neuron n : getOutputLayer().getNeurons()) {
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

		for(int layer = layers.size() - 1; layer > 0; layer--) {
			Layer rightLayer = layers.get(layer);
			Layer leftLayer = layers.get(layer - 1);
			
			// leftLayer is the hidden layer in the classic 3 layers network
			for(Neuron n : leftLayer.getNeurons()) {
				ArrayList<Connection> connections = n.getAllInConnections();
				for(Connection con : connections) {
					double aj = n.getOutput();
					double ai = con.leftNeuron.getOutput();
					double sumKoutputs = 0;
					int j = 0;
					// rightLayer is the output layer in the classic 3 layers network
					for(Neuron out_neu : rightLayer.getNeurons()) {
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