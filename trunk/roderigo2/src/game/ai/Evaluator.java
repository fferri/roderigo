package game.ai;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;
import game.GameController;
import game.GameController.GameListener;
import game.player.AbstractPlayer;

public class Evaluator {
	public static <S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> int[] evaluate(Game<S, A, C, P> game, final AbstractPlayer<S, A, C, P> p1, final AbstractPlayer<S, A, C, P> p2, int numMatches) {
		final int ret[] = {0, 0, 0};
		while(numMatches-- > 0) {
			GameController<S, A, C, P> controller = new GameController<>(game);
			controller.addPlayer(p1);
			controller.addPlayer(p2);
			controller.addGameListener(new GameListener<S, A, C, P>() {
				public void stateChanged(S newState) {}
				public void movePlayed(S state, A action, C player) {}
				public void gameOver(S state) {
					C winner = state.getWinner();
					if(p1.getColor().equals(winner)) ret[0]++;
					else if(p2.getColor().equals(winner)) ret[2]++;
					else ret[1]++;
				}
			});
			controller.play(game.getInitialState());
		}
		return ret;
	}
}
