package tictactoe;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;
import game.ai.Evaluator;
import game.ai.rl.AbstractQTable;
import game.ai.rl.QLearning;
import game.ai.rl.QTableNeuralNet;
import game.player.AIMinMaxPlayer;
import game.player.AIQPolicyPlayer;
import game.player.AbstractPlayer;
import game.player.ProbabilisticPlayer;
import game.player.RandomPlayer;

import java.io.File;
import java.io.IOException;

import tictactoe.TicTacToeBoard.Color;

public class TrainAgainstRandom {
	public static <S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> void train(Game<S, A, C, P> game, C[] colors, int numIter) throws IOException {
		final File qtableFile = new File("tictactoe.dat");
		final AbstractPlayer<S, A, C, P> me = new RandomPlayer<S, A, C, P>(colors[0], "rand");
		final AbstractPlayer<S, A, C, P> opponent1 = new RandomPlayer<S, A, C, P>(colors[1], "opp-rand");
		final AbstractPlayer<S, A, C, P> opponent2 = new AIMinMaxPlayer<S, A, C, P>(colors[1], "opp-ai", 9);
		final ProbabilisticPlayer<S, A, C, P> opponent = new ProbabilisticPlayer<S, A, C, P>("opp", opponent1, opponent2, 0.5);
		final AbstractQTable<S, A, C, P> qtable = new QTableNeuralNet<>(9, 6);
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
		double p;
		System.out.println("X = [");
		int numok = 0;
		for(int b = 0; true; b += batchSize) {
			p = 1 - Math.exp(-10*(numIter - b)/(double)numIter);
			opponent.setProbability(p);
			qlearning.train(batchSize, 0.8, true);
			AbstractPlayer<S, A, C, P> p1a = new RandomPlayer<S, A, C, P>(colors[1], "opp-rand");
			AbstractPlayer<S, A, C, P> p1b = new AIMinMaxPlayer<S, A, C, P>(colors[1], "opp-rand", 9);
			AbstractPlayer<S, A, C, P> p2 = new AIQPolicyPlayer<S, A, C, P>("opp-Q", qlearning);
			int e1[] = Evaluator.evaluate(game, p1a, p2, 100);
			int e2[] = Evaluator.evaluate(game, p1b, p2, 1);
			System.out.println(String.format("%d, %f,  %d, %d, %d,  %d, %d, %d", b, p, e1[0], e1[1], e1[2], e2[0], e2[1], e2[2]));
			if(e1[0] < 5) numok++; else numok = 0;
			if(numok > 10) break;
		}
		System.out.println("]; plot(X(:,2))");

		qlearning.save(qtableFile);
	}
	
	public static void main(String[] args) throws IOException {
		final int numIter = 50000;
		TicTacToeGame game = new TicTacToeGame() {
			@Override
			public TicTacToeBoard getInitialState() {
				TicTacToeBoard b = super.getInitialState();
				//if(Math.random() < 0.5)
				//	b.flipTurn();
				return b;
			}
		};
		train(game, new Color[]{TicTacToeBoard.CIRCLE, TicTacToeBoard.CROSS}, numIter);
	}
}
