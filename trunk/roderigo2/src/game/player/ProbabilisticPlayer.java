package game.player;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;

/**
 * A probabilistic player is a wrapper for two players.
 * It chooses player 1 with probability p, and player 2 with probability 1-p
 * 
 * @author Federico Ferri
 *
 * @param <S> class representing a board (the state)
 * @param <A> class representing an action
 * @param <C> class representing a color
 * @param <P> class representing a position
 */
public class ProbabilisticPlayer<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> extends AbstractPlayer<S, A, C, P> {
	private final AbstractPlayer<S, A, C, P> player1;
	private final AbstractPlayer<S, A, C, P> player2;
	private double p;
	
	public ProbabilisticPlayer(String name, AbstractPlayer<S, A, C, P> player1, AbstractPlayer<S, A, C, P> player2, double prob1) {
		super(name, player1.getColor());
		if(!player1.getColor().equals(player2.getColor()))
			throw new IllegalArgumentException("players must have the same color");
		this.player1 = player1;
		this.player2 = player2;
		setProbability(prob1);
	}
	
	public void setProbability(double p) {
		if(p < 0 || p > 1)
			throw new IllegalArgumentException("p must be between 0 and 1");
		this.p = p;
	}
	
	@Override
	public A getMove(Game<S, A, C, P> game, S state) {
		if(Math.random() < p)
			return player1.getMove(game, state);
		else
			return player2.getMove(game, state);
	}
}
