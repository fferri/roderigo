package neuralnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Layer {
	protected Layer prevLayer;
	protected Neuron biasNeuron;
	protected List<Neuron> neurons;
	
	/**
	 * Create a layer without connections
	 * (useful for creating the input layer)
	 * 
	 * @param size the number of units in this layer
	 */
	public Layer(int size) {
		this(size, null);
	}
	
	/**
	 * Create a layer fully connected to the specified previous layer
	 * 
	 * @param size the number of units in this layer
	 * @param prevLayer the previous layer
	 */
	public Layer(int size, Layer prevLayer) {
		this.prevLayer = prevLayer;
		this.biasNeuron = prevLayer != null ? new Neuron() : null;
		this.neurons = new ArrayList<>();
		for(int j = 0; j < size; j++) {
			Neuron neuron = new Neuron();
			this.neurons.add(neuron);
		}
		if(prevLayer != null) {
			for(Neuron neuron : neurons) {
				neuron.addInConnectionsS(prevLayer);
				neuron.addBiasConnection(biasNeuron);
			}
		}
	}
	
	public int size() {
		return neurons.size();
	}
	
	public Collection<Neuron> getNeurons() {
		return Collections.unmodifiableCollection(neurons);
	}
	
	public void initializeRandomWeights(double mult) {
		for(Neuron neuron : neurons) {
			ArrayList<Connection> connections = neuron.getAllInConnections();
			for(Connection conn : connections) {
				double newWeight = mult * (Math.random() * 2 - 1);
				conn.setWeight(newWeight);
			}
		}
	}
	
	public void setInput(double inputs[]) {
		for(int i = 0; i < neurons.size(); i++) {
			neurons.get(i).setOutput(inputs[i]);
		}
	}
	
	public double[] getOutput() {
		double[] outputs = new double[neurons.size()];
		for(int i = 0; i < neurons.size(); i++)
			outputs[i] = neurons.get(i).getOutput();
		return outputs;
	}
	
	public double[] getError(double expectedOutput[]) {
		double[] error = new double[neurons.size()];
		for(int i = 0; i < error.length; i++)
			error[i] = expectedOutput[i] - neurons.get(i).getOutput();
		return error;
	}
	
	public void activate() {
		for(Neuron n : neurons)
			n.calculateOutput();
	}
}
