package game.ai;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;
import game.GameController;
import game.GameController.GameListener;
import game.player.AbstractPlayer;

/**
 * Class for evaluating a player
 * 
 * @author Federico Ferri
 *
 */
public class Evaluator {
	/**
	 * Evaluate two players by sampling a number of games outcomes
	 * 
	 * @param game
	 * @param p1
	 * @param p2
	 * @param numMatches
	 * @return
	 */
	public static <S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> double[] evaluate(Game<S, A, C, P> game, final AbstractPlayer<S, A, C, P> p1, final AbstractPlayer<S, A, C, P> p2, int numMatches) {
		final double ret[] = {0, 0, 0};
		if(numMatches <= 0) return ret;
		for(int i = 0; i < numMatches; i++) {
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
		for(int i = 0; i < ret.length; i++)
			ret[i] /= (double)numMatches;
		return ret;
	}
}
