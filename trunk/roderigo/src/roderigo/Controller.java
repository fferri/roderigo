package roderigo;

import javax.swing.JOptionPane;

import roderigo.ai.AlphaBetaPlayer;
import roderigo.gui.JBoard;
import roderigo.gui.JRodrigoMainWindow;
import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.BoardCellSet;
import roderigo.struct.GameState;

public class Controller implements JBoard.CellListener {
	private boolean aiPlaysWhite = true;
	private boolean aiPlaysBlack = false;
	
	private boolean dontMakeMoves = false;
	private boolean showSearchAnim = true;
	
	private boolean evaluateValidMoves = false;
	
	private int searchDepth = 3;
	
	private GameState gameState;
	private JRodrigoMainWindow mainWindow;
	
	public Controller() {
	}
	
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
	
	public GameState getGameState() {
		return gameState;
	}
	
	public void setMainWindow(JRodrigoMainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	public JRodrigoMainWindow getMainWindow() {
		return mainWindow;
	}
	
	public void startGame() {
		// newGame ?
		continueGame();
	}
	
	public void continueGame() {
		if(isAITurn())
			runAITask();
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
	}
	
	public void runAITask() {
		new Thread() {
			@Override
			public void run() {
				mainWindow.jboard.lock();
				while(isAITurn()) {
					BoardCell bestMove = new AlphaBetaPlayer(Controller.this).getBestMove();
					
					if(bestMove == null) {
						// aborted by user [ESC] (or some problem?)
						break;
					}
					
					if(dontMakeMoves) {
						mainWindow.jboard.setBestMove(bestMove);
						mainWindow.jboard.asyncRepaint();
						return;
					}

					if(gameState.move(bestMove)) {
						mainWindow.jboard.setLastMove(bestMove);
						if(evaluateValidMoves)
							mainWindow.jboard.evaluateValidMoves();
						mainWindow.jboard.asyncRepaint();
						// sleep... ?
					}
				}
				mainWindow.jboard.unlock();
				checkEndGame();
			}
		}.start();
	}
	
	public void checkEndGame() {
		if(getTurn() != null) return;
		
		BoardCellSet pieces = getBoard().getAllPieces();
		int w = pieces.whitePieces().size();
		int b = pieces.blackPieces().size();
		
		String message = "Game finished.\n\n";
		if(w == b) {
			message += "TIE! (" + w + " to " + b + ")";
		} else {
			BoardCellColor winner = (w > b) ? BoardCellColor.WHITE : BoardCellColor.BLACK;
			boolean humanVSmachine = (aiPlaysBlack || aiPlaysWhite) && !(aiPlaysBlack && aiPlaysWhite);
			boolean humanWinner = (winner == BoardCellColor.WHITE && !aiPlaysWhite) ||
				(winner == BoardCellColor.BLACK && !aiPlaysBlack);

			message += winner + " wins " + Math.max(w, b) + " to " + Math.min(w, b) + ".";
			
			if(humanWinner) // human
				message += "\nCongratulations!";
			else if(humanVSmachine)
				message += "\n\nHUMAN BEATEN BY MACHINE! (singularity still is far away though)";
			
			message += "\n\nPlay again?";
		}
		
		int answer = JOptionPane.showConfirmDialog(mainWindow, message, "Play again?", JOptionPane.YES_NO_OPTION);
		if(answer == JOptionPane.YES_OPTION) {
			newGame();
			startGame();
		}
	}

	public boolean isAiPlaysWhite() {
		return aiPlaysWhite;
	}

	public void setAiPlaysWhite(boolean aiPlaysWhite) {
		this.aiPlaysWhite = aiPlaysWhite;

		// reflect status in menu item:
		mainWindow.menuItemAIPlaysWhite.setSelected(aiPlaysWhite);
	}

	public boolean isAiPlaysBlack() {
		return aiPlaysBlack;
	}

	public void setAiPlaysBlack(boolean aiPlaysBlack) {
		this.aiPlaysBlack = aiPlaysBlack;
		
		// reflect status in menu item:
		mainWindow.menuItemAIPlaysBlack.setSelected(aiPlaysBlack);
	}

	public boolean isDontMakeMoves() {
		return dontMakeMoves;
	}

	public void setDontMakeMoves(boolean dontMakeMoves) {
		this.dontMakeMoves = dontMakeMoves;
		
		// reflect status in menu item:
		mainWindow.menuItemDontMakeMoves.setSelected(dontMakeMoves);
	}

	public boolean isShowSearchAnim() {
		return showSearchAnim;
	}

	public void setShowSearchAnim(boolean showSearchAnim) {
		this.showSearchAnim = showSearchAnim;
		
		// reflect status in menu item:
		mainWindow.menuItemShowSearchAnim.setSelected(showSearchAnim);
	}

	public boolean isEvaluateValidMoves() {
		return evaluateValidMoves;
	}

	public void setEvaluateValidMoves(boolean evaluateValidMoves) {
		this.evaluateValidMoves = evaluateValidMoves;
	}

	public int getSearchDepth() {
		return searchDepth;
	}

	public void setSearchDepth(int searchDepth) {
		this.searchDepth = searchDepth;
	}

	@Override
	public void cellClicked(BoardCell cell) {
		BoardCellColor turn = gameState.getTurn();
		
		if(turn == null) return;
		
		if(!isAITurn()) {
			// human turn
			boolean valid = gameState.move(cell);
			
			if(valid) {
				mainWindow.jboard.asyncRepaint();
				continueGame();
			} else {
				//JOptionPane.showMessageDialog(mainWindow, "Invalid move", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
