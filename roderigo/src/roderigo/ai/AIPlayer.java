package roderigo.ai;

import roderigo.struct.BoardCell;
import roderigo.struct.GameState;

/**
 * Abstract class of an AI Player
 * 
 * @author Federico Ferri
 *
 */
public interface AIPlayer {
	public void abort();
	
	/**
	 * From the given starting state, compute the best move
	 * @param presentState
	 * @return
	 * @throws AbortException
	 */
	public BoardCell getBestMove(GameState presentState) throws AbortException;
}
