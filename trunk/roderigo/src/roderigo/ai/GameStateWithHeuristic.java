package roderigo.ai;

import roderigo.struct.BoardCell;
import roderigo.struct.GameState;

/**
 * GameState with things used by AlphaBetaPlayer
 * 
 * @author Federico Ferri
 *
 */
public class GameStateWithHeuristic extends GameState implements Comparable<GameStateWithHeuristic> {
	private BoardCell lastMove;
	private GameStateWithHeuristic next;
	
	public int h;
	
	public GameStateWithHeuristic(GameState s) {
		super(s);
		if(s instanceof GameStateWithHeuristic)
			lastMove = ((GameStateWithHeuristic) s).lastMove;
	}

	@Override
	public boolean move(BoardCell c) {
		if(!super.move(c))
			return false;
		
		lastMove = c;
		return true;
	}
	
	public BoardCell getLastMove() {
		return lastMove;
	}

	public GameStateWithHeuristic getNext() {
		return next;
	}
	
	public void setNext(GameStateWithHeuristic n) {
		next = n;
	}

	@Override
	public int compareTo(GameStateWithHeuristic o) {
		Integer mine = new Integer(h);
		return mine.compareTo(o.h);
	}
}
