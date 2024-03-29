package roderigo;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import roderigo.ai.AIPlayer;
import roderigo.ai.AbortException;
import roderigo.ai.AlphaBetaPlayer;
import roderigo.ai.genetic.Genome;
import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.BoardCellSet;
import roderigo.struct.GameState;

/**
 * Controller handles the flow of the game.
 * Makes heavy use of Observer pattern (all the listener boilerplate code at the end
 * of this class) to achieve decoupling between GUI classes and logic.
 * 
 * @author Federico Ferri
 *
 */
public class Controller {
	/**
	 * Indicates if AI plays BLACK
	 */
	private boolean aiPlaysWhite = true;
	
	/**
	 * Indicates if AI plays WHITE
	 */
	private boolean aiPlaysBlack = false;
	
	/**
	 * Indicates AI will only mark the move it would do,
	 * but won't do it (human will make the move for AI) 
	 */
	private boolean dontMakeMoves = false;
	
	/**
	 * Run AI computation in a separate thread (for GUI)
	 * instead of running in current thread (for GA)
	 */
	private boolean runAiTaskInBackground = true;
	
	/**
	 * Search depth (if static)
	 */
	private int searchDepth = 5;
	
	/**
	 * The GameState object
	 */
	private final GameState gameState;
	
	/**
	 * Black player
	 */
	private final AIPlayer blackPlayer;
	
	/**
	 * White player
	 */
	private final AIPlayer whitePlayer;
	
	/**
	 * Start time measurement
	 */
	private long startTime[] = new long[2];
	
	/**
	 * Total time counters
	 */
	private long totalTime[] = new long[2];
	
	/**
	 * Factory for building a Controller object
	 * @return A controller instance
	 */
	public static Controller newController() {
		return newController(
				new AlphaBetaPlayer(Genome.DEFAULT),
				new AlphaBetaPlayer(Genome.DEFAULT));
	}
	
	/**
	 * Factory for building a Controller object
	 * @return A controller instance
	 */
	public static Controller newController(AIPlayer blackPlayer, AIPlayer whitePlayer) {
		return newController(new GameState(), blackPlayer, whitePlayer);
	}
	
	/**
	 * Factory for building a Controller object
	 * @return A controller instance
	 */
	public static Controller newController(GameState startingState, AIPlayer blackPlayer, AIPlayer whitePlayer) {
		GameState gameState = new GameState(startingState);
		// blackPlayer.setGameState(gameState);
		// whitePlayer.setGameState(gameState); // done by runAITask
		return new Controller(gameState, blackPlayer, whitePlayer);
	}
	
	private Controller(GameState gameState, AIPlayer blackPlayer, AIPlayer whitePlayer) {
		this.gameState = gameState;
		this.blackPlayer = blackPlayer;
		this.whitePlayer = whitePlayer;
	}

	/**
	 * Reset game state (i.e. start a new game).
	 */
	public void newGame() {
		resetMeasureTime();
		
		gameState.newGame();
		
		notifyGameListeners_newGame(gameState);
	}
	
	/**
	 * Start game.
	 * 
	 * This causes a new game to be created if the current game
	 * has no valid moves for no player (i.e. it's finished).
	 * 
	 * Otherwise causes just a call to continueGame 
	 */
	public void startGame() {
		if(gameState.getTurn() == null)
			newGame();
		
		continueGame();
	}
	
	/**
	 * Continue game.
	 * 
	 * This causes the AI to "wake up" if it is its turn.
	 * 
	 * Also takes care of time measurement, and of doing
	 * end game check.
	 */
	public void continueGame() {
		startMeasuringTime(getTurn());
		
		if(isAITurn())
			runAITask();
		else
			checkEndGame();
	}
	
	/**
	 * Make a move for the current player.
	 * 
	 * @param cell The move
	 * @return wether the move has been done (i.e. was valid)
	 */
	public boolean move(BoardCell cell) {
		BoardCellColor oldTurn = getTurn();
		
		if(gameState.move(cell)) {
			long time = stopMeasuringTime(oldTurn);
			notifyGameMoveListeners_move(cell, oldTurn, time);
			
			if(getTurn() == oldTurn)
				notifyGameMoveListeners_pass(oldTurn.opposite());
			
			startMeasuringTime(getTurn());
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Switch turn (i.e. force a pass)
	 */
	public void switchTurn() {
		gameState.switchTurn();
	}
	
	/**
	 * Getter for the Board object (of GameState)
	 * @return the Board object
	 */
	public Board getBoard() {
		return gameState.getBoard();
	}
	
	/**
	 * Getter for the BoardCellColor (turn) object (of GameState)
	 * @return the turn
	 */
	public BoardCellColor getTurn() {
		return gameState.getTurn();
	}
	
	/**
	 * Check if for current turn, AI has to play
	 * @return true if AI has to play, false otherwise
	 */
	public boolean isAITurn() {
		BoardCellColor turn = gameState.getTurn();
		AIPlayer aiPlayer = getAIPlayer(turn);
		
		if(aiPlayer == null) return false;
		
		if(turn == BoardCellColor.BLACK && aiPlaysBlack)
			return true;
		if(turn == BoardCellColor.WHITE && aiPlaysWhite)
			return true;
		
		return false;
	}
	
	/**
	 * Run the AI task, either in background or in foreground,
	 * depending on {runAiTaskInBackground} setting.
	 */
	private void runAITask() {
		if(runAiTaskInBackground) {
			Thread thread = new Thread() { @Override public void run() { runAITask_forReal(); } };
			thread.start();
		} else {
			runAITask_forReal();
		}
	}
	
	/**
	 * Real run of the AI task (in foreground).
	 */
	private void runAITask_forReal() {
		while(isAITurn()) {
			// Get the AI which has to play now
			AIPlayer aiPlayer = getAIPlayer(getTurn());
			
			assert aiPlayer != null;
			
			if(aiPlayer instanceof AlphaBetaPlayer)
				((AlphaBetaPlayer) aiPlayer).setMaxDepth(searchDepth);
			
			BoardCell bestMove = null;
			notifyAiTaskListeners_computationStart(aiPlayer);
			try {
				bestMove = aiPlayer.getBestMove(gameState);
				notifyAiTaskListeners_computationEnd(aiPlayer);
			} catch(AbortException e) {
				notifyAiTaskListeners_computationAborted(aiPlayer);
				startTime[getTurn().ordinal()] = 0;
			}
			
			BoardCellColor oldTurn = getTurn();
			long time = stopMeasuringTime(oldTurn);
			
			assert bestMove != null;
			
			if(dontMakeMoves) {
				notifyGameMoveListeners_hint(bestMove, getTurn());
				break;
			}
			
			if(gameState.move(bestMove)) {
				notifyGameMoveListeners_move(bestMove, oldTurn, time);
				
				if(getTurn() == oldTurn)
					notifyGameMoveListeners_pass(oldTurn.opposite());
				
				startMeasuringTime(getTurn());
				continue;
			}
		}
		
		checkEndGame();
	}
	
	/**
	 * Get the AIPlayer associated with the specified turn
	 * @param turn
	 * @return
	 */
	public AIPlayer getAIPlayer(BoardCellColor turn) {
		if(turn == BoardCellColor.WHITE)
			return whitePlayer;
		if(turn == BoardCellColor.BLACK)
			return blackPlayer;
		return null;
	}
	
	/**
	 * Is the game in a finished state?
	 * (i.e. no player has valid moves)
	 */
	public void checkEndGame() {
		if(getTurn() != null) return;
		
		stopMeasuringTime(BoardCellColor.BLACK);
		stopMeasuringTime(BoardCellColor.WHITE);
		
		notifyGameListeners_gameEnd(gameState);
	}

	/**
	 * Create a nifty end game message
	 * @return the message String
	 */
	public String getEndGameMessage() {
		StringBuilder message = new StringBuilder();
		
		BoardCellSet pieces = getBoard().getAllPieces();
		int w = pieces.whitePieces().size();
		int b = pieces.blackPieces().size();
		
		message.append("Game finished.\n\n");
		
		if(w == b) {
			message.append("TIE! (" + w + " to " + b + ")");
		} else {
			BoardCellColor winner = (w > b) ? BoardCellColor.WHITE : BoardCellColor.BLACK;
			boolean humanVSmachine = (aiPlaysBlack || aiPlaysWhite) && !(aiPlaysBlack && aiPlaysWhite);
			boolean humanWinner = (winner == BoardCellColor.WHITE && !aiPlaysWhite) ||
				(winner == BoardCellColor.BLACK && !aiPlaysBlack);

			message.append(winner + " wins " + Math.max(w, b) + " to " + Math.min(w, b) + ".");
			
			if(humanWinner) // human
				message.append("\nCongratulations!");
			else if(humanVSmachine)
				message.append("\n\nHUMAN BEATEN BY MACHINE!");
			
			message.append("\n\nTotal BLACK time: " + String.format("%.1f", totalTime[BoardCellColor.BLACK.ordinal()] / 1000.0));
			message.append("\nTotal WHITE time: " + String.format("%.1f", totalTime[BoardCellColor.WHITE.ordinal()] / 1000.0));
		}
		
		return message.toString();
	}
	
	public boolean isAiPlaysWhite() {
		return aiPlaysWhite;
	}

	public void setAiPlaysWhite(boolean aiPlaysWhite) {
		this.aiPlaysWhite = aiPlaysWhite;

		notifySettingsListeners_settingsChanged();
	}

	public boolean isAiPlaysBlack() {
		return aiPlaysBlack;
	}

	public void setAiPlaysBlack(boolean aiPlaysBlack) {
		this.aiPlaysBlack = aiPlaysBlack;
		
		notifySettingsListeners_settingsChanged();
	}

	public boolean isDontMakeMoves() {
		return dontMakeMoves;
	}

	public void setDontMakeMoves(boolean dontMakeMoves) {
		this.dontMakeMoves = dontMakeMoves;
		
		notifySettingsListeners_settingsChanged();
	}
	
	public boolean isRunAiTaskInBackground() {
		return runAiTaskInBackground;
	}
	
	public void setRunAiTaskInBackground(boolean runAiTaskInBackground) {
		this.runAiTaskInBackground = runAiTaskInBackground;
	}

	public int getSearchDepth() {
		return searchDepth;
	}

	public void setSearchDepth(int searchDepth) {
		this.searchDepth = searchDepth;
		
		notifySettingsListeners_settingsChanged();
	}
	
	public boolean isUsingDynamicDepth() {
		if(isAiPlaysBlack() && blackPlayer instanceof AlphaBetaPlayer) {
			AlphaBetaPlayer p = (AlphaBetaPlayer) blackPlayer;
			return p.isUsingDynamicDepth();
		}
		
		if(isAiPlaysWhite() && whitePlayer instanceof AlphaBetaPlayer) {
			AlphaBetaPlayer p = (AlphaBetaPlayer) whitePlayer;
			return p.isUsingDynamicDepth();
		}
		
		return false;
	}
	
	public void setUsingDynamicDepth(boolean usingDynamicDepth) {
		if(blackPlayer instanceof AlphaBetaPlayer) {
			AlphaBetaPlayer p = (AlphaBetaPlayer) blackPlayer;
			p.setUsingDynamicDepth(usingDynamicDepth);
		}
		
		if(whitePlayer instanceof AlphaBetaPlayer) {
			AlphaBetaPlayer p = (AlphaBetaPlayer) whitePlayer;
			p.setUsingDynamicDepth(usingDynamicDepth);
		}
		
		notifySettingsListeners_settingsChanged();
	}
	
	public int getDynamicSearchDepth() {
		int sd1 = 0, sd2 = 0;
		
		if(isAiPlaysBlack() && blackPlayer instanceof AlphaBetaPlayer) {
			AlphaBetaPlayer p = (AlphaBetaPlayer) blackPlayer;
			sd1 = p.getDynamicMaxDepth();
		}
		
		if(isAiPlaysWhite() && whitePlayer instanceof AlphaBetaPlayer) {
			AlphaBetaPlayer p = (AlphaBetaPlayer) whitePlayer;
			sd2 = p.getDynamicMaxDepth();
		}
		
		return Math.max(sd1, sd2);
	}
	
	public boolean isAI(BoardCellColor color) {
		if(color == BoardCellColor.BLACK) {
			return isAiPlaysBlack();
		} else if(color == BoardCellColor.WHITE) {
			return isAiPlaysWhite();
		} else {
			// XXX:
			return false;
		}
	}
	
	private void resetMeasureTime() {
		startTime[0] = startTime[1] = totalTime[0] = totalTime[1] = 0;
	}
	
	private void startMeasuringTime(BoardCellColor color) {
		if(color == null) return;
		
		int i = color.ordinal();
		if(startTime[i] == 0) {
			startTime[i] = System.currentTimeMillis();
		} else {
			//System.err.println("WARNING: startMeasuringTime(" + color + ") called multiple times");
			//System.err.flush();
		}
	}
	
	private long stopMeasuringTime(BoardCellColor color) {
		if(color == null) return 0;
		
		int i = color.ordinal();
		if(startTime[i] != 0) {
			long delta = System.currentTimeMillis() - startTime[i];
			totalTime[i] += delta;
			startTime[i] = 0;
			return delta;
		} else {
			return 0;
		}
	}
	
	public long getTotalTime(BoardCellColor color) {
		return totalTime[color.ordinal()];
	}
	
	// GameMoveListener observer
	
	private List<GameMoveListener> gameMoveListeners = new ArrayList<GameMoveListener>();
	
	public static interface GameMoveListener extends EventListener {
		/**
		 * A move has been made
		 * 
		 * @param cell Cell indicating the move made
		 * @param color The player which has made the move
		 * @param time Time used by player in milliseconds
		 */
		public void move(BoardCell cell, BoardCellColor color, long time);
		
		/**
		 * Player had to pass move
		 * 
		 * @param color Color of the player which had to pass
		 */
		public void pass(BoardCellColor color);
		
		/**
		 * Hint (a.k.a. best move) has been suggested by A.I.
		 * 
		 * @param cell Cell indicating the move to make
		 * @param color The player which has to make the move
		 */
		public void hint(BoardCell cell, BoardCellColor color);
	}
	
	public void addGameMoveListener(GameMoveListener listener) {
		if(!gameMoveListeners.contains(listener))
			gameMoveListeners.add(listener);
	}
	
	public void removeGameMoveListener(GameMoveListener listener) {
		gameMoveListeners.remove(listener);
	}
	
	private void notifyGameMoveListeners_move(BoardCell cell, BoardCellColor color, long time) {
		for(GameMoveListener l : gameMoveListeners)
			l.move(cell, color, time);
	}
	
	private void notifyGameMoveListeners_pass(BoardCellColor color) {
		for(GameMoveListener l : gameMoveListeners)
			l.pass(color);
	}
	
	private void notifyGameMoveListeners_hint(BoardCell cell, BoardCellColor color) {
		for(GameMoveListener l : gameMoveListeners)
			l.hint(cell, color);
	}
	
	// GameListener observer
	
	private List<GameListener> gameListeners = new ArrayList<GameListener>();
	
	public static interface GameListener extends EventListener {
		/**
		 * A new game has started
		 */
		public void newGame(GameState s);
		
		/**
		 * Game is finished
		 */
		public void gameEnd(GameState s);
	}
	
	public void addGameListener(GameListener listener) {
		if(!gameListeners.contains(listener))
			gameListeners.add(listener);
	}
	
	public void removeGameListener(GameListener listener) {
		gameListeners.remove(listener);
	}
	
	private void notifyGameListeners_newGame(GameState s) {
		for(GameListener l : gameListeners)
			l.newGame(s);
	}

	private void notifyGameListeners_gameEnd(GameState s) {
		for(GameListener l : gameListeners)
			l.gameEnd(s);
	}
	
	// AiTaskListener observer
	
	private List<AiTaskListener> aiTaskListeners = new ArrayList<AiTaskListener>();
	
	public static interface AiTaskListener extends EventListener {
		/**
		 * An AI computation has started
		 */
		public void computationStart(AIPlayer aiPlayer);
		
		/**
		 * An AI computation has ended
		 */
		public void computationEnd(AIPlayer aiPlayer);

		/**
		 * An AI computation has been aborted
		 */
		public void computationAborted(AIPlayer aiPlayer);
	}
	
	public void addAiTaskListener(AiTaskListener listener) {
		if(!aiTaskListeners.contains(listener))
			aiTaskListeners.add(listener);
	}
	
	public void removeAiTaskListener(AiTaskListener listener) {
		aiTaskListeners.remove(listener);
	}
	
	private void notifyAiTaskListeners_computationStart(AIPlayer aiPlayer) {
		for(AiTaskListener l : aiTaskListeners)
			l.computationStart(aiPlayer);
	}

	private void notifyAiTaskListeners_computationEnd(AIPlayer aiPlayer) {
		for(AiTaskListener l : aiTaskListeners)
			l.computationEnd(aiPlayer);
	}

	private void notifyAiTaskListeners_computationAborted(AIPlayer aiPlayer) {
		for(AiTaskListener l : aiTaskListeners)
			l.computationAborted(aiPlayer);
	}
	
	// SettingsListener observer
	
	private List<SettingsListener> settingsListeners = new ArrayList<SettingsListener>();
	
	public static interface SettingsListener extends EventListener {
		/**
		 * Some setting has changed
		 */
		// TODO: *WHICH* setting has changed???
		public void settingsChanged();
	}
	
	public void addSettingsListener(SettingsListener listener) {
		if(!settingsListeners.contains(listener))
			settingsListeners.add(listener);
	}
	
	public void removeSettingsListener(SettingsListener listener) {
		settingsListeners.remove(listener);
	}
	
	private void notifySettingsListeners_settingsChanged() {
		for(SettingsListener l : settingsListeners)
			l.settingsChanged();
	}
}
