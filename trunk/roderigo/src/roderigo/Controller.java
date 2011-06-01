package roderigo;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import roderigo.ai.AbortException;
import roderigo.ai.AlphaBetaPlayer;
import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.BoardCellSet;
import roderigo.struct.GameState;

public class Controller {
	private boolean aiPlaysWhite = true;
	private boolean aiPlaysBlack = false;
	
	private boolean dontMakeMoves = false;
	private boolean showSearchAnim = true;
	
	private boolean evaluateValidMoves = false;
	
	private boolean runAiTaskInBackground = true;
	
	private int searchDepth = 3;
	
	private final GameState gameState;
	
	public Controller(GameState gameState) {
		this.gameState = gameState;
	}
	
	public GameState getGameState() {
		return gameState;
	}
	
	public void startGame() {
		if(gameState.getTurn() == null)
			newGame();
		
		continueGame();
	}
	
	public void continueGame() {
		if(isAITurn())
			runAITask();
		else
			checkEndGame();
	}
	
	public Board getBoard() {
		return gameState.getBoard();
	}
	
	public BoardCellColor getTurn() {
		return gameState.getTurn();
	}
	
	public boolean isAITurn() {
		BoardCellColor turn = gameState.getTurn();
		if(turn == BoardCellColor.BLACK)
			return aiPlaysBlack;
		if(turn == BoardCellColor.WHITE)
			return aiPlaysWhite;
		return false;
	}
	
	public void newGame() {
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
	
	private void runAITask_forReal() {
		notifyAiTaskListeners_computationStart();

		boolean aborted = false;
		
		while(isAITurn()) {
			BoardCell bestMove = null;
			try {
				bestMove = new AlphaBetaPlayer(Controller.this).getBestMove();
			} catch(AbortException e) {
				aborted = true;
			}
			
			if(bestMove == null) {
				// something wrong with the AIPlayer...
				break;
			}
			
			if(dontMakeMoves) {
				notifyGameMoveListeners_hint(bestMove, getTurn());
				break;
			}

			BoardCellColor oldTurn = getTurn();
			
			if(gameState.move(bestMove)) {
				notifyGameMoveListeners_move(bestMove, getTurn());
				
				if(getTurn() == oldTurn)
					notifyGameMoveListeners_pass(oldTurn.opposite());
				
				continue;
			}
		}
		
		if(aborted) notifyAiTaskListeners_computationAborted();
		else notifyAiTaskListeners_computationEnd();

		checkEndGame();
	}
	
	public void checkEndGame() {
		if(getTurn() != null) return;
		
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

	public boolean isShowSearchAnim() {
		return showSearchAnim;
	}

	public void setShowSearchAnim(boolean showSearchAnim) {
		this.showSearchAnim = showSearchAnim;
		
		notifySettingsListeners_settingsChanged();	}

	public boolean isEvaluateValidMoves() {
		return evaluateValidMoves;
	}

	public void setEvaluateValidMoves(boolean evaluateValidMoves) {
		this.evaluateValidMoves = evaluateValidMoves;
		
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
	
	// GameMoveListener observer
	
	private List<GameMoveListener> gameMoveListeners = new ArrayList<GameMoveListener>();
	
	public static interface GameMoveListener extends EventListener {
		/**
		 * A move has been made
		 * 
		 * @param cell Cell indicating the move made
		 * @param color The player which has made the move
		 */
		public void move(BoardCell cell, BoardCellColor color);
		
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
	
	private void notifyGameMoveListeners_move(BoardCell cell, BoardCellColor color) {
		for(GameMoveListener l : gameMoveListeners)
			l.move(cell, color);
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
		public void computationStart();
		
		/**
		 * An AI computation has ended
		 */
		public void computationEnd();

		/**
		 * An AI computation has been aborted
		 */
		public void computationAborted();
	}
	
	public void addAiTaskListener(AiTaskListener listener) {
		if(!aiTaskListeners.contains(listener))
			aiTaskListeners.add(listener);
	}
	
	public void removeAiTaskListener(AiTaskListener listener) {
		aiTaskListeners.remove(listener);
	}
	
	private void notifyAiTaskListeners_computationStart() {
		for(AiTaskListener l : aiTaskListeners)
			l.computationStart();
	}

	private void notifyAiTaskListeners_computationEnd() {
		for(AiTaskListener l : aiTaskListeners)
			l.computationEnd();
	}

	private void notifyAiTaskListeners_computationAborted() {
		for(AiTaskListener l : aiTaskListeners)
			l.computationAborted();
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
