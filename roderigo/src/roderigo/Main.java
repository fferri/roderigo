package roderigo;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import roderigo.ai.AIPlayer;
import roderigo.ai.AlphaBetaPlayer;
import roderigo.ai.genetic.Genome;
import roderigo.gui.JBoard;
import roderigo.gui.JRodrigoMainWindow;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.GameState;

/**
 * Entry point of the application (GUI)
 * 
 * @author Federico Ferri
 *
 */
public class Main {
	private final Controller controller;
	
	public final JRodrigoMainWindow mainWindow;
	
	// remember current ai task (so we can eventually stop it):
	public AIPlayer aiTask = null;
	
	// Constructor
	private Main() {
		controller = Controller.newController(
				new AlphaBetaPlayer(Genome.DEFAULT),
				new AlphaBetaPlayer(Genome.DEFAULT)
		);
		
		mainWindow = new JRodrigoMainWindow(controller);
		
		controller.addAiTaskListener(new Controller.AiTaskListener() {
			@Override public void computationStart(AIPlayer aiPlayer) {
				aiTask = aiPlayer;
				mainWindow.jboard.lock();
			}
			
			@Override public void computationEnd(AIPlayer aiPlayer) {
				mainWindow.jboard.unlock();
				aiTask = null;
			}
			
			@Override public void computationAborted(AIPlayer aiPlayer) {
				mainWindow.jboard.unlock();
				aiTask = null;
			}
		});
		
		controller.addSettingsListener(new Controller.SettingsListener() {
			@Override public void settingsChanged() {
				// sync stateful menu items
				mainWindow.menuItemUseDynamicDepth.setSelected(controller.isUsingDynamicDepth());
				mainWindow.menuItemAIPlaysBlack.setSelected(controller.isAiPlaysBlack());
				mainWindow.menuItemAIPlaysWhite.setSelected(controller.isAiPlaysWhite());
				mainWindow.menuItemDontMakeMoves.setSelected(controller.isDontMakeMoves());
				
				mainWindow.toolbox.searchDepth.setEnabled(!controller.isUsingDynamicDepth());
			}
		});
		
		controller.addGameListener(new Controller.GameListener() {
			@Override public void newGame(GameState s) {}
			
			@Override public void gameEnd(GameState s) {
				int answer = JOptionPane.showConfirmDialog(mainWindow, controller.getEndGameMessage() + "\n\nPlay again?", "Play again?", JOptionPane.YES_NO_OPTION);
				if(answer == JOptionPane.YES_OPTION) {
					controller.newGame(); // unnecessary, but clearer
					controller.startGame();
				}
			}
		});
		
		controller.addGameMoveListener(new Controller.GameMoveListener() {
			@Override public void pass(BoardCellColor color) {
				if(!controller.isAiPlaysBlack() || !controller.isAiPlaysWhite())
					JOptionPane.showMessageDialog(mainWindow, color + " has to pass.", null, JOptionPane.INFORMATION_MESSAGE);
			}
			
			@Override public void move(BoardCell cell, BoardCellColor color, long time) {
				mainWindow.jboard.setLastMove(cell);
				mainWindow.jboard.asyncRepaint();
				
				if(controller.isUsingDynamicDepth()) {
					mainWindow.toolbox.searchDepth.setValue(controller.getDynamicSearchDepth());
				}
			}
			
			@Override public void hint(BoardCell cell, BoardCellColor color) {
				mainWindow.jboard.setBestMove(cell);
				mainWindow.jboard.asyncRepaint();
			}
		});
		
		mainWindow.jboard.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "abortAiTask");
		mainWindow.jboard.getActionMap().put("abortAiTask", new AbstractAction() {
			private static final long serialVersionUID = 7906203027073311035L;

			@Override public void actionPerformed(ActionEvent evt) {
				if(aiTask != null)
					aiTask.abort();
			}
		});
		
		mainWindow.jboard.addCellListener(new JBoard.CellListener() {
			@Override public void cellClicked(BoardCell cell) {
				BoardCellColor turn = controller.getTurn();
				
				if(turn == null) return;
				
				if(!controller.isAITurn()) {
					// human turn
					boolean valid = controller.move(cell);
					
					if(valid) {
						// following two already happen in the move listener:
						//mainWindow.jboard.setLastMove(cell);
						//mainWindow.jboard.asyncRepaint();
						controller.continueGame();
					} else {
						//JOptionPane.showMessageDialog(mainWindow, "Invalid move", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		controller.setUsingDynamicDepth(false);
	}
	
	public void run() {
		controller.startGame();
	}
	
	public static void main(String args[]) {
		Main main = new Main();

		main.run();
	}
}
