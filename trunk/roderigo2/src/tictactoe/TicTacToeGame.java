package tictactoe;

import java.math.BigInteger;
import java.util.List;

import tictactoe.TicTacToeBoard.Position;

import game.Game;

public class TicTacToeGame implements Game<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>{
	private final TicTacToeBoard s0 = getInitialState();
	
	@Override
	public TicTacToeBoard getInitialState() {
		return new TicTacToeBoard();
	}

	@Override
	public TicTacToeBoard cloneState(TicTacToeBoard state) {
		return new TicTacToeBoard(state);
	}
	
	@Override
	public TicTacToeBoard.Color getOpponent(TicTacToeBoard.Color player) {
		return s0.colorFlip(player);
	}

	@Override
	public List<TicTacToeBoard.Action> getAvailableActions(TicTacToeBoard state) {
		return state.getValidActions();
	}

	@Override
	public TicTacToeBoard.Color getTurn(TicTacToeBoard state) {
		return state.getTurn();
	}

	@Override
	public TicTacToeBoard execute(TicTacToeBoard state, TicTacToeBoard.Action action) {
		TicTacToeBoard newState = new TicTacToeBoard(state);
		if(newState.makeAction(action))
			return newState;
		else
			return null;
	}

	@Override
	public boolean executeInPlace(TicTacToeBoard state, TicTacToeBoard.Action action) {
		return state.makeAction(action);
	}

	@Override
	public TicTacToeBoard invert(TicTacToeBoard state) {
		TicTacToeBoard newState = new TicTacToeBoard(state);
		newState.flipColors();
		newState.flipTurn();
		return newState;
	}

	@Override
	public boolean isGameOver(TicTacToeBoard state) {
		return state.isGameOver();
	}

	@Override
	public TicTacToeBoard.Color getWinner(TicTacToeBoard state) {
		return state.getWinner();
	}

	@Override
	public double evaluate(TicTacToeBoard state, TicTacToeBoard.Color player) {
		TicTacToeBoard.Color w = getWinner(state);
		if(w != null)
			return w.equals(player) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		
		double h = 0;
		for(List<Position> line : state.lines())
			h += Math.pow(3, state.linePotential(line, player));
		return h;
	}

	@Override
	public TicTacToeBoard stateFromString(String str) {
		TicTacToeBoard b = new TicTacToeBoard();
		b.set(new BigInteger(str, 16));
		return b;
	}
	
	@Override
	public String stateToString(TicTacToeBoard state) {
		return state.get().toString(16);
	}
}
