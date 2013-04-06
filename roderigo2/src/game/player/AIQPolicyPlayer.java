package game.player;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;
import game.ai.rl.QLearning;

public class AIQPolicyPlayer<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> extends AbstractPlayer<S, A, C, P> {
	private final QLearning<S, A, C, P> qlearning;
	
	public AIQPolicyPlayer(String name, QLearning<S, A, C, P> qlearning) {
		super(name, qlearning.getColor());
		this.qlearning = qlearning;
	}

	@Override
	public A getMove(Game<S, A, C, P> game, S state) {
		if(!game.getTurn(state).equals(getColor()))
			throw new IllegalStateException("Asking move when it is not " + getName() + "'s turn");
		A ret = qlearning.getBestMove(state);
		return ret;
	}
}
