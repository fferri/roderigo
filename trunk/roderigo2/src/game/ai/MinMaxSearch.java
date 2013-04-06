package game.ai;

import java.util.List;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;


/**
 * MinMaxSearch algorithm for adversarial search.
 * (basically, a more elegant implementation of min-max search).
 * 
 * @author Federico Ferri
 *
 */
public class MinMaxSearch<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> {
	private final Game<S, A, C, P> game;
	
	public MinMaxSearch(Game<S, A, C, P> game) {
		this.game = game;
	}
	
	/**
	 * Decide the best move by performing a min-max search at the
	 * given max depth.
	 * 
	 * @param b The board.
	 * @param maxDepth The maximum depth.
	 * 
	 * @return The best move.
	 */
	public A decide(S state, int maxDepth) {
		List<A> actions = game.getAvailableActions(state);
		double bestScore = Double.NEGATIVE_INFINITY, score;
		A bestAction = actions.get(0);
		for(A action : actions) {
			S newState = game.execute(state, action);
			score = minValue(newState, game.getTurn(state), maxDepth, maxDepth);
			if(score > bestScore) {
				bestScore = score;
				bestAction = action;
			}
		}
		return bestAction;
	}
	
	private double maxValue(S state, C player, int maxDepth, int depth) {
		if(depth == 0 || game.isGameOver(state)) return game.evaluate(state, player);
		double v = Double.NEGATIVE_INFINITY;
		for(A action : game.getAvailableActions(state))
			v = Math.max(v, minValue(game.execute(state, action), player, maxDepth, depth - 1));
		return v;
	}
	
	private double minValue(S state, C player, int maxDepth, int depth) {
		if(depth == 0 || game.isGameOver(state)) return game.evaluate(state, player);
		double v = Double.POSITIVE_INFINITY;
		for(A action : game.getAvailableActions(state))
			v = Math.min(v, maxValue(game.execute(state, action), player, maxDepth, depth - 1));
		return v;
	}
}