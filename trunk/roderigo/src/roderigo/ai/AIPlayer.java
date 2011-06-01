package roderigo.ai;

import roderigo.struct.BoardCell;

public interface AIPlayer {
	public void abort();
	
	public BoardCell getBestMove() throws AbortException;
}
