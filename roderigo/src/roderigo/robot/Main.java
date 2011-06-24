package roderigo.robot;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import roderigo.Controller;
import roderigo.ai.AlphaBetaPlayer;
import roderigo.ai.genetic.Genome;
import roderigo.robot.AbstractRobot.BoardReadException;
import roderigo.robot.BoardDiff.BoardDiffException;
import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;

public class Main {
	private final AbstractRobot robot1;
	private final Controller controller;
	private BoardCellColor remotePlayer = BoardCellColor.BLACK;
	
	public Main() throws Exception {
		robot1 = new RobotType1();
		AlphaBetaPlayer ai1 = new AlphaBetaPlayer(Genome.DEFAULT);
		ai1.setMaxDepth(5);
		AlphaBetaPlayer ai2 = new AlphaBetaPlayer(Genome.DEFAULT);
		ai2.setMaxDepth(5);
		controller = Controller.newController(ai1, ai2);
		controller.setAiPlaysBlack(false);
		controller.setAiPlaysWhite(true);
		controller.setRunAiTaskInBackground(false);
		
		controller.addGameMoveListener(new Controller.GameMoveListener() {
			@Override public void pass(BoardCellColor color) {}
			@Override public void hint(BoardCell cell, BoardCellColor color) {}
			
			@Override public void move(BoardCell cell, BoardCellColor color, long time) {
				if(color == remotePlayer.opposite()) {
					// when ai makes a move, forward it to remote
					robot1.click(cell.row, cell.col);
				}
			}
		});
		final JFrame f = new JFrame("BOARD-ROBOT");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(new Rectangle(64, 64));
		f.setResizable(false);
		f.setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
		JButton btnSwitch = new JButton("switch");
		btnSwitch.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				remotePlayer = remotePlayer.opposite();
				controller.setAiPlaysBlack(remotePlayer == BoardCellColor.WHITE);
				controller.setAiPlaysWhite(remotePlayer == BoardCellColor.BLACK);
				controller.continueGame();
			}
		});
		f.add(btnSwitch);
		JButton btnDrag = new JButton("select") {
			private static final long serialVersionUID = 7934391937615031693L;

			{
				addMouseListener(new MouseListener() {
					@Override public void mousePressed(MouseEvent e) {}
					@Override public void mouseExited(MouseEvent e) {}
					@Override public void mouseEntered(MouseEvent e) {}
					@Override public void mouseClicked(MouseEvent e) {}
					@Override public void mouseReleased(MouseEvent e) {
						Point p = e.getPoint();
						SwingUtilities.convertPointToScreen(p, e.getComponent());
						robot1.findBoard((int) p.getX(), (int) p.getY());
						Rectangle r = robot1.getBoardRect();
						if(r != null) {
							System.out.println("Found board at " + r);
							synchronized(controller) {
								controller.newGame();
							}
							System.out.println("Started new game... turn is of " + controller.getTurn() + " (" + (controller.isAI(controller.getTurn()) ? "AI" : "RemoteHuman") + ")");
						} else {
							System.out.println("Found nothing at " + p);
						}
					}
				});
				
				addActionListener(new ActionListener() {
					@Override public void actionPerformed(ActionEvent e) {
						JOptionPane.showMessageDialog(f, "Drag from this button to the border of the board you want to capture", "Usage:", JOptionPane.ERROR_MESSAGE);
					}
				});
			}
		};
		f.add(btnDrag);
		//f.validate();
		f.pack();
		f.setVisible(true);
	}
	
	public void run() {
		playLoop: while(true) {
			// try with AI
			synchronized(controller) {
				if(controller.getTurn() == null) {
					controller.newGame();
					continue playLoop;
				} else {
					controller.continueGame();
				}
			}
			
			// try reading board:
			if(robot1.getBoardRect() == null) {
				// the user hasn't selected a board yet.
				// tell him
				System.out.println("Waiting for a board being selected...");
				// take a nap!
				try {Thread.sleep(1000);} catch(InterruptedException e1) {}
				continue playLoop;
			}			
			else { /* robot knows board location */ }
			
			Board actualBoard = null;
			try {
				actualBoard = robot1.readBoard();
			} catch(BoardReadException e) {
				System.out.println(e.toString());
				// not good :-(
				// take a nap and try again
				try {Thread.sleep(1000);} catch(InterruptedException e1) {}
				continue playLoop;
			}
			
			// if we are here, is remotePlayer's turn.
			// if he has already moved, we'll get a move sequence here
			// otherwise an empty sequence, means he hasn't moved yet, and
			// we have to wait
			if(BoardDiff.equal(controller.getBoard(), actualBoard)) {
				try {Thread.sleep(1000);} catch(InterruptedException e1) {}
				continue playLoop;
			}
			List<BoardCell> moves = null;
			try {
				moves = BoardDiff.diff(controller.getBoard(), actualBoard);
			} catch(BoardDiffException e) {
				// if is unable to diff, maybe there is a chance that a new game
				// is started, so let's check it:
				if(isNewGame(actualBoard)) {
					// okay, let's re-start, and try again
					// (BoardDiff will do its job on next cycle)
					synchronized(controller) {
						controller.newGame();
					}
					System.out.println("New game detected!");
					continue playLoop;
				} else {
					// that was not the case
					System.out.println(e.toString());
					// not good :-(
					// take a nap and try again
					try {Thread.sleep(1000);} catch(InterruptedException e1) {}
					continue playLoop;
				}
			}
			if(moves == null || moves.isEmpty()) {
				// XXX: moves == null not really good
				try {Thread.sleep(1000);} catch(InterruptedException e1) {}
				continue playLoop;
			}
			
			// very good if we are here
			// apply to our internal state the moves we've seen, in the order
			//   BoardDiff has guessed for us
			for(BoardCell move : moves) {
				if(controller.getTurn() != remotePlayer) {
					// very bad. what happened? out of sync... bailing out
					System.out.println("out of sync :: bailing out");
					break playLoop;
				}
				synchronized(controller) {
					controller.move(move);
				}
			}
			
			// now our board and remote board are in sync
		}
	}
	
	/**
	 * Check whether the board is in it initial configuration
	 * or 1 ply away from it, indicating that a new game has
	 * just been started.
	 *  
	 * @param b The Board to check
	 */
	private static boolean isNewGame(Board b) {
		final BoardCellColor whoMovesFirst = BoardCellColor.BLACK;
		final Board initBoard = new Board(b.getNumRows(), b.getNumColumns());
		if(BoardDiff.equal(b, initBoard))
			return true;
		for(BoardCell move : b.getValidMoves(whoMovesFirst)) {
			Board b1 = initBoard.clone();
			assert b1.makeMove(move, whoMovesFirst);
			if(BoardDiff.equal(b, b1))
				return true;
		}
		return false;
	}
	
	public static void main(String args[]) throws Exception {
		Main t = new Main();
		
		t.run();
	}

}
