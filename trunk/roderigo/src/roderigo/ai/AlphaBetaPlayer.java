package roderigo.ai;

import java.util.ArrayList;
import java.util.List;

import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellSet;
import roderigo.struct.GameState;

public class AlphaBetaPlayer implements AIPlayer {
	private GameState presentState;
	
	private int maxDepth;

	private boolean abort = false;
	
	public synchronized void abort() {
		abort = true;
	}
	
	public AlphaBetaPlayer(GameState gameState, int maxDepth) {
		this.presentState = gameState;
		this.maxDepth = maxDepth;
	}

	private List<GameStateWithHeuristic> getSuccessorStates(GameState state) {
		List<GameStateWithHeuristic> result = new ArrayList<GameStateWithHeuristic>();
		
		BoardCellSet moves = state.getBoard().getValidMoves(state.getTurn());
		
		for(BoardCell move : moves) {
			GameStateWithHeuristic newState = new GameStateWithHeuristic(state);
			newState.move(move);
			//newState.h = newState.getTurn() != null ? new BoardEvaluation(newState.getBoard(), null, newState.getTurn()).getValue() : 0;
			result.add(newState);
		}
		
		//Collections.sort(result);
		//Collections.reverse(result);
		
		return result;
	}
	
	private int computeUtility(GameState state) {
		return new BoardEvaluation(state.getBoard(), null, presentState.getTurn()).getValue();
	}

	private boolean terminalTest(GameState state) {
		return state.getTurn() == null;
	}

	/**
	 * Do a MAX move. If <param>ab</param> is null, behaves like classical MIN-MAX;
	 * otherwise it uses alpha-beta pruning.
	 * 
	 * @param state Starting point
	 * @param ab Alpha-beta bean
	 * @param depth Param used to limit depth
	 * @return The computed max value
	 * @throws AbortException
	 */
	private int maxValue(GameStateWithHeuristic state, AlphaBeta ab, int depth) throws AbortException {
		if(abort) throw new AbortException();
		int v = Integer.MIN_VALUE;
		if(terminalTest(state) || depth >= maxDepth) {
			return computeUtility(state);
		} else {
			List<GameStateWithHeuristic> successorList = getSuccessorStates(state);
			for(int i = 0; i < successorList.size(); i++) {
				GameStateWithHeuristic successor = successorList.get(i);
				int minimumValueOfSuccessor = minValue(successor, ab != null ? ab.clone() : null, depth + 1);
				if(minimumValueOfSuccessor > v) {
					v = minimumValueOfSuccessor;
					state.setNext(successor);
				}
				if(ab != null) {
					// use alpha-beta pruning
					if(v >= ab.getBeta()) {
						return v;
					}
					ab.setAlpha(Math.max(ab.getAlpha(), v));
				}
			}
			return v;
		}
	}
	
	/**
	 * Do a MIN move. If <param>ab</param> is null, behaves like classical MIN-MAX;
	 * otherwise it uses alpha-beta pruning.
	 * 
	 * @param state Starting point
	 * @param ab Alpha-beta bean
	 * @param depth Param used to limit depth
	 * @return The computed min value
	 * @throws AbortException
	 */
	private int minValue(GameStateWithHeuristic state, AlphaBeta ab, int depth) throws AbortException {
		if(abort) throw new AbortException();
		int v = Integer.MAX_VALUE;
		if(terminalTest(state) || depth >= maxDepth) {
			return computeUtility(state);
		} else {
			List<GameStateWithHeuristic> successorList = getSuccessorStates(state);
			for(int i = 0; i < successorList.size(); i++) {
				GameStateWithHeuristic successor = successorList.get(i);
				int maximumValueOfSuccessor = maxValue(successor, ab != null ? ab.clone() : null, depth + 1);
				if(maximumValueOfSuccessor < v) {
					v = maximumValueOfSuccessor;
					state.setNext(successor);
				}
				if(ab != null) {
					// use alpha-beta pruning
					if(v <= ab.getAlpha()) {
						return v;
					}
					ab.setBeta(Math.min(ab.getBeta(), v));
				}
			}
			return v;
		}
	}
	
	public BoardCell getBestMove() throws AbortException {
		Board board = presentState.getBoard(); // the original board
		BoardCellSet moves = board.getValidMoves(presentState.getTurn());
		if(moves.size() == 1) return moves.iterator().next();
		
		abort = false;

		GameStateWithHeuristic presentStateH = new GameStateWithHeuristic(presentState);
		maxValue(presentStateH, new AlphaBeta(Integer.MIN_VALUE, Integer.MAX_VALUE), 0);
		GameStateWithHeuristic nextState = presentStateH.getNext();
		if(nextState == null)
			throw new RuntimeException("AlphaBetaPlayer made a BOO-BOO");
		
		/* introducing GameStateWithHeuristic, I introduced a cloning
		 * of the Board, which implies cloning the BoardCells
		 * 
		 * thus, simply returning nextState.getLastMove() would return
		 * a cell of another board, not the original board
		 * 
		 * (caused a bug in paint, which checked for lastMove using ==)
		 */
		return board.conformCell(nextState.getLastMove());
	}
}