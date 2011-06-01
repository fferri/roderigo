package roderigo.ai;

import java.util.ArrayList;
import java.util.List;

import roderigo.Controller;
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
	
	public AlphaBetaPlayer(Controller controller) {
		this.presentState = controller.getGameState();
		
		this.maxDepth = controller.getSearchDepth();
	}

	private List<GameState> getSuccessorStates(GameState state) {
		List<GameState> result = new ArrayList<GameState>();
		
		BoardCellSet moves = state.getBoard().getValidMoves(state.getTurn());
		
		for(BoardCell move : moves) {
			GameState newState = new GameState(state);
			newState.move(move);
			result.add(newState);
		}
		
		return result;
	}
	
	private int computeUtility(GameState state) {
		return new BoardEvaluation(state.getBoard(), null, presentState.getTurn()).getValue();
	}

	private boolean terminalTest(GameState state) {
		return state.getTurn() == null;
	}

	private int maxValue(GameState state, AlphaBeta ab, int depth) throws AbortException {
		if(abort) throw new AbortException();
		int v = Integer.MIN_VALUE;
		if(terminalTest(state) || depth >= maxDepth) {
			return computeUtility(state);
		} else {
			List<GameState> successorList = getSuccessorStates(state);
			for(int i = 0; i < successorList.size(); i++) {
				GameState successor = (GameState) successorList.get(i);
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
	
	private int minValue(GameState state, AlphaBeta ab, int depth) throws AbortException {
		if(abort) throw new AbortException();
		int v = Integer.MAX_VALUE;
		if(terminalTest(state) || depth >= maxDepth) {
			return computeUtility(state);
		} else {
			List<GameState> successorList = getSuccessorStates(state);
			for(int i = 0; i < successorList.size(); i++) {
				GameState successor = successorList.get(i);
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
		BoardCellSet moves = presentState.getBoard().getValidMoves(presentState.getTurn());
		if(moves.size() == 1) return moves.iterator().next();
		
		abort = false;
		BoardCell bestMove = null;
		try {
			maxValue(presentState, new AlphaBeta(Integer.MIN_VALUE, Integer.MAX_VALUE), 0);
			GameState nextState = presentState.getNext();
			if(nextState == null)
				throw new RuntimeException("AlphaBetaPlayer made a BOO-BOO");
			
			bestMove = nextState.getLastMove();
		} catch(AbortException e) {
			System.out.println("ALPHABETA-PLAYER ABORTED.");
		}
		
		if(abort) throw new AbortException();
		
		return bestMove;
	}
}