package roderigo;

import roderigo.ai.AlphaBetaPlayer;
import roderigo.gui.JBoard;
import roderigo.gui.JRodrigoMainWindow;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.GameState;

public class Main {
	private static Main instance = null;
	
	public static synchronized Main getInstance() {
		if(instance == null) {
			instance = new Main();
		}
		return instance;
	}
	
	private final GameState gameState;
	private final Controller controller;
	
	public final JRodrigoMainWindow mainWindow;
	
	// remember current ai task (so we can eventually stop it):
	public AlphaBetaPlayer aiTask = null;
	
	// Constructor
	private Main() {
		controller = new Controller();
		
		gameState = new GameState();
		mainWindow = new JRodrigoMainWindow(controller);
		
		mainWindow.jboard.addCellListener(new JBoard.CellListener() {
			@Override
			public void cellClicked(BoardCell cell) {
				BoardCellColor turn = gameState.getTurn();
				
				if(turn == null) return;
				
				if(!controller.isAITurn()) {
					// human turn
					boolean valid = gameState.move(cell);
					
					if(valid) {
						mainWindow.jboard.asyncRepaint();
						controller.continueGame();
					} else {
						//JOptionPane.showMessageDialog(mainWindow, "Invalid move", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		controller.setGameState(gameState);
		controller.setMainWindow(mainWindow);
	}
	
	public void run() {
		controller.startGame();
	}
	
	public static void main(String args[]) {
		Main main = Main.getInstance();
		
		main.run();
	}
}
