package game.player;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;

import java.util.List;

/**
 * A random player selects a random move among those available.
 * 
 * @author Federico Ferri
 *
 * @param <S> class representing a board (the state)
 * @param <A> class representing an action
 * @param <C> class representing a color
 * @param <P> class representing a position
 */
public class RandomPlayer<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> extends AbstractPlayer<S, A, C, P> {
	public RandomPlayer(C player, String name) {
		super(name, player);
	}

	@Override
	public A getMove(Game<S, A, C, P> game, S state) {
		if(!game.getTurn(state).equals(getColor()))
			throw new IllegalStateException("Asking move when it is not " + getName() + "'s turn");
		List<A> m = game.getAvailableActions(state);
		A ret = m.get((int)(Math.random() * m.size()));
		return ret;
	}
}
