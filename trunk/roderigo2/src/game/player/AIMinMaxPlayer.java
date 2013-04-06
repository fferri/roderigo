package game.player;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;
import game.ai.MinMaxSearch;

public class AIMinMaxPlayer<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> extends AbstractPlayer<S, A, C, P> {
	private int maxDepth;
	
	public AIMinMaxPlayer(C player, String name, int maxDepth) {
		super(name, player);
		this.maxDepth = maxDepth;
	}
	
	@Override
	public A getMove(Game<S, A, C, P> game, S state) {
		if(!game.getTurn(state).equals(getColor()))
			throw new IllegalStateException("Asking move when it is not " + getName() + "'s turn");
		MinMaxSearch<S, A, C, P> minMax = new MinMaxSearch<>(game);
		return minMax.decide(state, maxDepth);
	}
}
