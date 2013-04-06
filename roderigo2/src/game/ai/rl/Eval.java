package game.ai.rl;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;
import game.ai.Evaluator;
import game.player.AIQPolicyPlayer;
import game.player.RandomPlayer;

import java.io.File;
import java.io.IOException;

import tictactoe.TicTacToeBoard;
import tictactoe.TicTacToeGame;
import tictactoe.TicTacToeBoard.Color;

public class Eval {
	public static <S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> int[] eval(Game<S, A, C, P> game, C[] colors, File ftable) throws IOException {
		RandomPlayer<S, A, C, P> p1 = new RandomPlayer<>(colors[1], "rand");
		QLearning<S, A, C, P> qlearning = new QLearning<S, A, C, P>(game, colors[0], new QTableNeuralNet<S, A, C, P>(9, 6));
		qlearning.load(ftable);
		AIQPolicyPlayer<S, A, C, P> p2 = new AIQPolicyPlayer<>("q", qlearning);
		return Evaluator.evaluate(game, p1, p2, 100);
	}
	
	public static void main(String[] args) throws IOException {
		TicTacToeGame game = new TicTacToeGame();
		int e[] = eval(game, new Color[]{TicTacToeBoard.CIRCLE, TicTacToeBoard.CROSS}, new File("tictactoe.dat"));
		System.out.println(e[0] + " " + e[1] + " " + e[2]);
	}
}
