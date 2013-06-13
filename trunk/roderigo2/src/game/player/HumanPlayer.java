package game.player;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;

import java.util.concurrent.SynchronousQueue;

/**
 * A human player.
 * The moves are read from the actual human via a synchronous queue.
 * 
 * @author Federico Ferri
 *
 * @param <S> class representing a board (the state)
 * @param <A> class representing an action
 * @param <C> class representing a color
 * @param <P> class representing a position
 */
public class HumanPlayer<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> extends AbstractPlayer<S, A, C, P> {
	private SynchronousQueue<A> queue;
	
	public HumanPlayer(C player, String name, SynchronousQueue<A> queue) {
		super(name, player);
		this.queue = queue;
	}
	
	@Override
	public A getMove(Game<S, A, C, P> game, S state) {
		if(!game.getTurn(state).equals(getColor()))
			throw new IllegalStateException("Asking move when it is not " + getName() + "'s turn");
		while(true) try {return queue.take();} catch(InterruptedException e) {}
	}
}
