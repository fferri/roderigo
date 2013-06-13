package othello;

import game.Game;

import java.math.BigInteger;
import java.util.List;

/**
 * Implementation of the othello game
 * 
 * @author Federico Ferri
 *
 */
public class OthelloGame implements Game<OthelloBoard, OthelloBoard.Action, OthelloBoard.Color, OthelloBoard.Position> {
	public OthelloBoard getInitialState() {
		return new OthelloBoard();
	}
	
	@Override
	public OthelloBoard cloneState(OthelloBoard state) {
		return new OthelloBoard(state);
	}

	public OthelloBoard.Color getOpponent(OthelloBoard.Color player) {
		if(player == null)
			return null;
		if(player.equals(OthelloBoard.BLACK))
			return OthelloBoard.WHITE;
		if(player.equals(OthelloBoard.WHITE))
			return OthelloBoard.BLACK;
		return null;
	}

	public List<OthelloBoard.Action> getAvailableActions(OthelloBoard state) {
		return state.getValidActions();
	}

	public OthelloBoard.Color getTurn(OthelloBoard state) {
		return state.getTurn();
	}

	public OthelloBoard execute(OthelloBoard state, OthelloBoard.Action action) {
		OthelloBoard newState = new OthelloBoard(state);
		if(newState.makeAction(action))
			return newState;
		else
			return null;
	}
	
	@Override
	public boolean executeInPlace(OthelloBoard state, OthelloBoard.Action action) {
		return state.makeAction(action);
	}

	public boolean isGameOver(OthelloBoard state) {
		return state.isGameOver();
	}

	public OthelloBoard.Color getWinner(OthelloBoard state) {
		return state.getWinner();
	}

	public double evaluate(OthelloBoard state, OthelloBoard.Color player) {		
		int d = state.getDiscDifferential();
		int p = player.getValue();
		
		if(state.isGameOver())
			return d == 0 ? 0 : (d * p) > 0 ? 10000 : -10000;
			
		int stable = state.getStableDiscDifferential();
		//int empty = state.count(OthelloBoard.EMPTY);
		int mobility = state.getMobilityDifferential();

		return p * (mobility + 100 * stable - d);
	}

	@Override
	public OthelloBoard invert(OthelloBoard state) {
		OthelloBoard newState = new OthelloBoard(state);
		newState.flipColors();
		newState.flipTurn();
		return newState;
	}
	
	@Override
	public OthelloBoard stateFromString(String str) {
		OthelloBoard b = new OthelloBoard();
		b.set(new BigInteger(str, 16));
		return b;
	}
	
	@Override
	public String stateToString(OthelloBoard state) {
		return state.get().toString(16);
	}
}
