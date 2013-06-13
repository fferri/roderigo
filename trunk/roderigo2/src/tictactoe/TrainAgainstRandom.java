package tictactoe;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;
import game.ai.Evaluator;
import game.ai.rl.AbstractQTable;
import game.ai.rl.QLearning;
import game.ai.rl.QTable;
import game.ai.rl.QTableNeuralNet;
import game.player.AIMinMaxPlayer;
import game.player.AIQPolicyPlayer;
import game.player.AbstractPlayer;
import game.player.ProbabilisticPlayer;
import game.player.RandomPlayer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Jama.Matrix;

import tictactoe.TicTacToeBoard.Color;

/**
 * Program for training the agent
 * 
 * @author Federico Ferri
 *
 */
public class TrainAgainstRandom {
	static double maxIterations = 1000000000;
	static double learningRate = 0.6;
	static int memorySize = 10000;
	static double momentum = 0.2;
	static double pRand = 0.999;
	static double gamma = 0.8;
	static int numHiddenNeurons = 30;
	
	public static <S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> void train(Game<S, A, C, P> game, C[] colors) throws IOException {
		final boolean useNeuralQTable = false;
		
		final File qtableFile = new File("tictactoe.dat");
		
		final AbstractPlayer<S, A, C, P> me = new RandomPlayer<S, A, C, P>(colors[0], "rand");
		final AbstractPlayer<S, A, C, P> opponentRand = new RandomPlayer<S, A, C, P>(colors[1], "opp-rand");
		final AbstractPlayer<S, A, C, P> opponentMinMax = new AIMinMaxPlayer<S, A, C, P>(colors[1], "opp-ai", 9);
		final ProbabilisticPlayer<S, A, C, P> opponent = new ProbabilisticPlayer<S, A, C, P>("opp", opponentRand, opponentMinMax, pRand);
		
		final QTableNeuralNet<S, A, C, P> qtableNN = new QTableNeuralNet<>(9, numHiddenNeurons);
		qtableNN.setLearningRate(learningRate);
		qtableNN.setMomentum(momentum);
		qtableNN.setMemorySize(memorySize);
		final AbstractQTable<S, A, C, P> qtable = useNeuralQTable ? qtableNN : new QTable<S, A, C, P>();
		
		final QLearning<S, A, C, P> qlearning = new QLearning<S, A, C, P>(game, me.getColor(), qtable);
		qlearning.setMyPlayer(me);
		qlearning.setOpponentPlayer(opponent);
		try {qlearning.load(qtableFile);} catch(IOException ex) {}
		/*qlearning.addTrainingEventListener(new TrainingEventListener() {
			public void trainingFinishged(int totEpochs) {}
			public void epochFinished(int epoch, int totEpochs) {
				int percent = epoch * 100 / totEpochs;
				if(percent == (epoch - 1) * 100 / totEpochs) return;
				System.out.println(String.format("Training... %d%%", percent));
			}
		});*/
		final int batchSize = 200;
		List<List<Double>> plot = new LinkedList<>();
		int numok = 0;
		for(int b = 0; b < maxIterations; b += batchSize) {
			qlearning.train(batchSize, gamma, true);
			AbstractPlayer<S, A, C, P> p1a = new RandomPlayer<S, A, C, P>(colors[1], "opp-rand");
			AbstractPlayer<S, A, C, P> p1b = new AIMinMaxPlayer<S, A, C, P>(colors[1], "opp-rand", 9);
			AbstractPlayer<S, A, C, P> p2 = new AIQPolicyPlayer<S, A, C, P>("opp-Q", qlearning);
			final int z1 = 100;
			double e1[] = Evaluator.evaluate(game, p1a, p2, z1);
			final int z2 = 1;
			double e2[] = Evaluator.evaluate(game, p1b, p2, z2);
			List<Double> plotLine = new ArrayList<>(10);
			plotLine.add(new Double(b));
			for(int i = 0; i < e1.length; i++) plotLine.add(e1[i]);
			for(int i = 0; i < e2.length; i++) plotLine.add(e2[i]);
			plot.add(plotLine);
			writeMatlabDataMatrix(plot, "X", 1, "trainingPlot.m");
			System.out.println("batch num " + b + ", e1[0] = " + e1[0]);
			if(e1[0] < 0.05) numok++; else numok = 0;
			if(numok > 10) break;
		}

		qlearning.save(qtableFile);
	}
	
	static void writeMatlabDataMatrix(List<List<Double>> data, String name, int column, String fileName) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
		bw.append("% PERFORMANCE RESULTS\n");
		bw.append("% learningRate = " + learningRate + "\n");
		bw.append("% memorySize = " + memorySize + "\n");
		bw.append("% momentum = " + momentum + "\n");
		bw.append("% pRand = " + pRand + "\n");
		bw.append("% gamma = " + gamma + "\n");
		bw.append("% numHiddenNeurons = " + numHiddenNeurons + "\n");

		bw.append(name);
		bw.append(" = [\n");
		for(List<Double> line : data) {
			boolean first = true;
			for(Double x : line) {
				if(first) first = false;
				else bw.append(", ");
				bw.append(x.toString());
			}
			bw.append("\n");
		}
		bw.append("];\n\n");
		bw.append("plot(" + name + "(:," + (column + 1) + "));\n");
		bw.close();
	}
	
	public static void main(String[] args) throws IOException {
		TicTacToeGame game = new TicTacToeGame() {
			@Override
			public TicTacToeBoard getInitialState() {
				TicTacToeBoard b = super.getInitialState();
				//if(Math.random() < 0.5)
				//	b.flipTurn();
				return b;
			}
		};
		train(game, new Color[]{TicTacToeBoard.CIRCLE, TicTacToeBoard.CROSS});
	}
}
