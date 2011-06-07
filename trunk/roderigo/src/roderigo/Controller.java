package roderigo;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import roderigo.ai.AIPlayer;
import roderigo.ai.AbortException;
import roderigo.ai.AlphaBetaPlayer;
import roderigo.ai.BoardEvaluation;
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
	private boolean aiPlaysWhite = true;
	private boolean aiPlaysBlack = false;
	
	private boolean dontMakeMoves = false;
	
	private boolean runAiTaskInBackground = true;
	
	private int searchDepth = 5;
	
	private final GameState gameState;
	private final AIPlayer blackPlayer;
	private final AIPlayer whitePlayer;
	
	// time measurement
	private long startTime[] = new long[2];
	private long totalTime[] = new long[2];
	
	// Controller factory
	public static Controller newController() {
		return newController(
				new AlphaBetaPlayer(BoardEvaluation.defaultWeights),
				new AlphaBetaPlayer(BoardEvaluation.defaultWeights));
	}
	
	public static Controller newController(AIPlayer blackPlayer, AIPlayer whitePlayer) {
		GameState gameState = new GameState();
		// blackPlayer.setGameState(gameState);
		// whitePlayer.setGameState(gameState); // done by runAITask
		return new Controller(gameState, blackPlayer, whitePlayer);
	}
	
	private Controller(GameState gameState, AIPlayer blackPlayer, AIPlayer whitePlayer) {
		this.gameState = gameState;
		this.blackPlayer = blackPlayer;
		this.whitePlayer = whitePlayer;
	}
	
	public void startGame() {
		if(gameState.getTurn() == null)
			newGame();
		
		continueGame();
	}
	
	public void continueGame() {
		startMeasuringTime(getTurn());
		
		if(isAITurn())
			runAITask();
		else
			checkEndGame();
	}
	
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
	
	public void switchTurn() {
		gameState.switchTurn();
	}
	
	public Board getBoard() {
		return gameState.getBoard();
	}
	
	public BoardCellColor getTurn() {
		return gameState.getTurn();
	}
	
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
	
	public void newGame() {
		resetMeasureTime();
		
		gameState.newGame();
		
		notifyGameListeners_newGame(gameState);
	}
	
	public void runAITask() {
		if(runAiTaskInBackground) {
			Thread thread = new Thread() { @Override public void run() { runAITask_forReal(); } };
			thread.start();
		} else {
			runAITask_forReal();
		}
	}
	
	private AIPlayer getAIPlayer(BoardCellColor turn) {
		if(turn == BoardCellColor.WHITE)
			return whitePlayer;
		if(turn == BoardCellColor.BLACK)
			return blackPlayer;
		return null;
	}
	
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
	
	public void checkEndGame() {
		if(getTurn() != null) return;
		
		stopMeasuringTime(BoardCellColor.BLACK);
		stopMeasuringTime(BoardCellColor.WHITE);
		
		notifyGameListeners_gameEnd(gameState);
	}

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
