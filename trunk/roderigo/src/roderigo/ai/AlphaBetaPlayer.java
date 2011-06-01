package roderigo.ai;

import java.util.ArrayList;
import java.util.List;

import roderigo.Controller;
import roderigo.Main;
import roderigo.gui.JBoard;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.BoardCellSet;
import roderigo.struct.GameState;

public class AlphaBetaPlayer implements AIPlayer {
	private GameState presentState;
	
	private int maxDepth;
	private boolean visualFeedback = false;
	
	private JBoard jboard;
	
	private boolean abort = false;
	
	public synchronized void abort() {
		abort = true;
	}
	
	public AlphaBetaPlayer(Controller controller) {
		this.presentState = controller.getGameState();
		
		this.maxDepth = controller.getSearchDepth();
		
		this.visualFeedback = controller.isShowSearchAnim();
		
		// FIXME: controlled is now decoupled from GUI
		this.jboard = null; //controller.getMainWindow().jboard; // anim tricks
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
		BoardCellColor turn = state.getTurn();
		BoardEvaluation h = new BoardEvaluation(state.getBoard(), null, turn);
		int value = h.getValue();
		if(turn == BoardCellColor.BLACK)
			return value;
		if(turn == BoardCellColor.WHITE)
			return -value;
		throw new RuntimeException("illegal state");
	}

	private boolean terminalTest(GameState state) {
		return state.getTurn() == null;
	}

	private int maxValue(GameState state, AlphaBeta ab, int depth) throws AbortException {
		int v = Integer.MIN_VALUE;
		if(terminalTest(state) || depth >= maxDepth) {
			return computeUtility(state);
		} else {
			List<GameState> successorList = getSuccessorStates(state);
			for(int i = 0; i < successorList.size(); i++) {
				GameState successor = (GameState) successorList.get(i);
				//int minimumValueOfSuccessor = minValue(successor, ab != null ? ab.clone() : null, depth + 1);
				int minimumValueOfSuccessor = minmaxValue(successor, ab != null ? ab.clone() : null, depth + 1);
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
		int v = Integer.MAX_VALUE;
		if(terminalTest(state) || depth >= maxDepth) {
			return computeUtility(state);
		} else {
			List<GameState> successorList = getSuccessorStates(state);
			for(int i = 0; i < successorList.size(); i++) {
				GameState successor = successorList.get(i);
				//int maximumValueOfSuccessor = maxValue(successor, ab != null ? ab.clone() : null, depth + 1);
				int maximumValueOfSuccessor = minmaxValue(successor, ab != null ? ab.clone() : null, depth + 1);
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
	
	private int minmaxValue(GameState state, AlphaBeta ab, int depth) throws AbortException {
		if(abort) throw new AbortException();
		
		visuallyMarkVisitedCell(state.getLastMove(), true);
		
		// since a player may have no moves, we rely on GameState
		// to know which player has the turn, and choose the right
		// action (min or max)
		int retVal = 0;
		BoardCellColor turn = state.getTurn();
		
		if(turn == BoardCellColor.WHITE)
			retVal = minValue(state, ab, depth);
		else if(turn == BoardCellColor.BLACK)
			retVal = maxValue(state, ab, depth);
		
		visuallyMarkVisitedCell(state.getLastMove(), false);
		
		return retVal;
	}
	
	private void visuallyMarkVisitedCell(BoardCell cell, boolean flag) {
		if(!visualFeedback) return;
		if(cell == null) return;
		presentState.getBoard().get(cell.row, cell.col).visitedFlag = flag;
		if(jboard != null) jboard.asyncRepaint();
	}
	
	public BoardCell getBestMove() throws AbortException {
		Main main = Main.getInstance();
		
		main.aiTask = this;
		
		BoardCellSet moves = presentState.getBoard().getValidMoves(presentState.getTurn());
		if(moves.size() == 1) return moves.iterator().next();
		
		abort = false;
		BoardCell bestMove = null;
		try {
			minmaxValue(presentState, new AlphaBeta(Integer.MIN_VALUE, Integer.MAX_VALUE), 0);
			GameState nextState = presentState.getNext();
			if(nextState == null)
				throw new RuntimeException("AlphaBetaPlayer made a BOO-BOO");
			
			bestMove = nextState.getLastMove();
		} catch(AbortException e) {
			System.out.println("ALPHABETA-PLAYER ABORTED.");
		}
		
		if(visualFeedback) {
			for(BoardCell cell : presentState.getBoard().getAllCells())
				cell.visitedFlag = false;
			if(jboard != null) jboard.asyncRepaint();
		}
		
		main.aiTask = null;
		
		if(abort) throw new AbortException();
		
		return bestMove;
	}
}