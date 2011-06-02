package roderigo.ai;

import roderigo.struct.BoardCell;
import roderigo.struct.GameState;

/**
 * Abstract class of an AI Player
 * 
 * @author Federico Ferri
 *
 */
public abstract class AIPlayer {
	protected GameState presentState;

	public AIPlayer() {
	}
	
	public void setGameState(GameState gameState) {
		this.presentState = gameState;
	}
	
	public abstract void abort();
	
	public abstract BoardCell getBestMove() throws AbortException;
}
