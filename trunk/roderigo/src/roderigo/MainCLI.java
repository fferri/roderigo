package roderigo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.GameState;

/**
 * Entry point of the application (CLI)
 * 
 * @author Federico Ferri
 *
 */
public class MainCLI {
	private final Controller controller;
	
	private final PrintWriter out = new PrintWriter(System.out, true);
	private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	// Constructor
	private MainCLI() {
		controller = new Controller(new GameState());
		
		controller.addGameListener(new Controller.GameListener() {
			@Override public void newGame(GameState s) {}
			
			@Override public void gameEnd(GameState s) {
				out.println(controller.getEndGameMessage() + "\n\nPlay again? [y/n] ");
				String answer = readLine();
				if(answer.equals("y")) {
					controller.newGame(); // unnecessary, but clearer
					controller.startGame();
				}
			}
		});
		
		controller.addGameMoveListener(new Controller.GameMoveListener() {
			@Override public void pass(BoardCellColor color) {
				if(!controller.isAiPlaysBlack() || !controller.isAiPlaysWhite())
					System.out.println(color + " has to pass.");
			}
			
			@Override public void move(BoardCell cell, BoardCellColor color, long time) {
				out.println("Computer moves to " + cell.toString());

				controller.getBoard().print(out);
			}
			
			@Override public void hint(BoardCell cell, BoardCellColor color) {
				out.println("best move for " + color + " would be " + cell);
				
				controller.getBoard().print(out);
			}
		});
	}
	
	private String readLine() {
		try {
			return in.readLine();
		} catch(Exception e) {
			return "";
		}
	}
	
	private BoardCell readPosition() {
		BoardCell pos = null;
		while(pos == null) {
			out.print("Enter your move: ");
			out.flush();
			pos = BoardCell.fromString(controller.getBoard(), readLine());
		}
		return pos;
	}
	
	public void run() {
		while(controller.getTurn() != null) {
			controller.continueGame();
			controller.getBoard().print(out);
			controller.move(readPosition());
		}
	}
	
	public static void main(String args[]) {
		MainCLI main = new MainCLI();

		main.run();
	}
}
