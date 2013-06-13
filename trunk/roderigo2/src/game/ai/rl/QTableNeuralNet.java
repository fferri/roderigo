package game.ai.rl;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import neuralnetwork.NeuralNetwork;

/**
 * A Q-Table approximation using a neural network.
 * 
 * @author Federico Ferri
 *
 * @param <S> class representing a board (the state)
 * @param <A> class representing an action
 * @param <C> class representing a color
 * @param <P> class representing a position
 */
public class QTableNeuralNet<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> extends AbstractQTable<S, A, C, P> {
	private final QTable<S, A, C, P> memory = new QTable<>();
	
	private NeuralNetwork neuralNet;
	private double learningRate = 1;
	private double momentum = 0.1;
	private int memorySize = 200;
	
	public QTableNeuralNet(int stateSize, int hiddenSize) {
		neuralNet = new NeuralNetwork(stateSize, hiddenSize, stateSize);
	}
	
	public void setLearningRate(double lr) {
		learningRate = lr;
	}
	
	public void setMomentum(double m) {
		momentum = m;
	}
	
	public void setMemorySize(int sz) {
		memorySize = sz;
	}

	private double[] stateToInputVector(S state) {
		List<Double> values = new ArrayList<>();
		for(Iterator<P> i = state.positionIterator(); i.hasNext(); ) {
			C color = state.get(i.next());
			values.add(color == null ? 0.0 : (double)color.getValue());
		}
		double ret[] = new double[values.size()];
		for(int i = 0; i < ret.length; i++) ret[i] = values.get(i);
		return ret;
	}
	
	private int actionIndex(S state, A action) {
		P p = action.getPosition();
		int idx = p == null ? -1 : p.getIndex();
		return idx;
	}
	
	/*private double[] actionToOutputVector(S state, A action) {
		int idx = actionIndex(state, action);
		double in[] = stateToInputVector(state);
		double ret[] = new double[in.length];
		for(int i = 0; i < in.length; i++) ret[i] = i == idx ? 1 : -1;
		return ret;
	}*/
	
	@Override
	public double get(S state, A action) {
		neuralNet.setInput(stateToInputVector(state));
		neuralNet.feedForward();
		return neuralNet.getOutput()[actionIndex(state, action)];
	}

	@Override
	public void set(S state, A action, double value) {
		memory.set(state, action, value);
		while(memory.size() > memorySize) memory.removeFirst();
		
		double in[][] = new double[memory.size()][neuralNet.getNumInputs()];
		double out[][] = new double[memory.size()][neuralNet.getNumOutputs()];
		for(int i = 0; i < memory.size(); i++) {
			in[i] = stateToInputVector(state);
			neuralNet.setInput(in[i]);
			neuralNet.feedForward();
			out[i] = neuralNet.getOutput();
			out[i][actionIndex(state, action)] = value;
		}
		neuralNet.epoch(in, out, learningRate, momentum);
	}

	@Override
	public void load(File f, Game<S, A, C, P> game) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(f));
		load(r, game);
		r.close();
	}

	@Override
	public void load(BufferedReader r, Game<S, A, C, P> game) throws IOException {
		String filename = r.readLine();
		if(filename == null)
			throw new IllegalStateException();
		FileInputStream fileInputStream = new FileInputStream(filename);
		ObjectInputStream os = new ObjectInputStream(fileInputStream);
		try {
			Object o;
			o = os.readObject();
			neuralNet = (NeuralNetwork)o;
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		os.close();
	}

	@Override
	public void save(File f, Game<S, A, C, P> game) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		save(w, game);
		w.close();
	}

	@Override
	public void save(BufferedWriter w, Game<S, A, C, P> game) throws IOException {
		String filename = "net";
		w.append(filename + "\n");
		FileOutputStream fileStream = new FileOutputStream(filename);
		ObjectOutputStream os = new ObjectOutputStream(fileStream);
		os.writeObject(neuralNet);
		os.close();
	}
}
