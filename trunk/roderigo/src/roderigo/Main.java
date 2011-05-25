package roderigo;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import roderigo.ai.AlphaBetaPlayer;
import roderigo.gui.JBoardWithBorder;
import roderigo.gui.JToolbox;
import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.GameState;

public class Main {
	private static Main instance = null;
	
	public static synchronized Main getInstance() {
		if(instance == null) {
			instance = new Main();
		}
		return instance;
	}
	
	public final Frame mainWindow = new Frame();
	
	public Main() {
		mainWindow.setVisible(true);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	@SuppressWarnings("serial")
	public class Frame extends JFrame {
		public final JBoardWithBorder board;
		public final JToolbox toolbox;
		public final GameState game;
		
		public Frame() {
			super("roderigo");
			game = new GameState();
			board = new JBoardWithBorder(game);
			toolbox = new JToolbox();
			
			board.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "doSomething");
			board.getActionMap().put("doSomething", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AlphaBetaPlayer.abort();
				}
			});
			
			setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			add(board);
			add(toolbox);
			pack();
		}
	}
	
	public static void print(BoardCell c) {
		if(c.isClear())
			System.out.print(".");
		else if(c.isBlack())
			System.out.print("X");
		else if(c.isWhite())
			System.out.print("O");
		else
			System.out.print("?");
	}
	
	public static void print(Board b) {
		for(int row = 0; row < b.getNumRows(); row++) {
			System.out.print(String.format("%03d", row));
			for(int col = 0; col < b.getNumColumns(); col++) {
				System.out.print(" ");
				print(b.get(row, col));
			}
			System.out.println("");
		}
	}
	
	public static void printType(BoardCell c) {
		switch(c.getType()) {
		case A: System.out.print("A"); break;
		case B: System.out.print("B"); break;
		case C: System.out.print("C"); break;
		case X: System.out.print("X"); break;
		case CORNER: System.out.print("*"); break;
		case NONE: System.out.print("."); break;
		default: System.out.print(" "); break;
		}
	}
	
	public static void printType(Board b) {
		for(int row = 0; row < b.getNumRows(); row++) {
			System.out.print(String.format("%03d", row));
			for(int col = 0; col < b.getNumColumns(); col++) {
				System.out.print(" ");
				printType(b.get(row, col));
			}
			System.out.println("");
		}
	}
	
	public static void main(String args[]) {
		Main.getInstance();
	}
}
