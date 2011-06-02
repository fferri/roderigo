package roderigo.ai;

import roderigo.struct.BoardCell;

/**
 * Interface of an AI Player
 * 
 * @author Federico Ferri
 *
 */
public interface AIPlayer {
	public void abort();
	
	public BoardCell getBestMove() throws AbortException;
}
