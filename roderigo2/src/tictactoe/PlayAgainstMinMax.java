package tictactoe;

import java.util.concurrent.SynchronousQueue;

import game.GameController;
import game.GameController.GameListener;
import game.ai.rl.QLearning;
import game.ai.rl.QTable;
import game.player.AIMinMaxPlayer;
import game.player.AIQPolicyPlayer;
import game.player.AbstractPlayer;
import game.player.HumanPlayer;

public class PlayAgainstMinMax {
	public static void main(String[] args) {
		TicTacToeGame game = new TicTacToeGame();
		QLearning<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> qlearning = new QLearning<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>(game, TicTacToeBoard.CROSS, new QTable<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>());
		AbstractPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> minmax = new AIMinMaxPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>(TicTacToeBoard.CROSS, "minmax", 999);
		AbstractPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> rl = new AIQPolicyPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>("q", qlearning);
		AbstractPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> human = new HumanPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>(TicTacToeBoard.CROSS, "human", new SynchronousQueue<TicTacToeBoard.Action>());
		final GameController<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> controller = new GameController<>(game);
		//controller.addPlayer(minmax);
		controller.addPlayer(human);
		controller.addPlayer(rl);
		controller.addGameListener(new GameListener<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>() {
			public void stateChanged(TicTacToeBoard newState) {
				System.out.println(newState.toString());
			}

			public void movePlayed(TicTacToeBoard state, TicTacToeBoard.Action action, TicTacToeBoard.Color player) {
				System.out.println(controller.getPlayer(player).getName() + " plays " + state.actionToString(action));
			}

			public void gameOver(TicTacToeBoard state) {
				System.out.println("Game over.");
			}
		});
		controller.play(game.getInitialState());
	}
}
