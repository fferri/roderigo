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
	
	public static void main(String args[]) {
		Main.getInstance();
	}
}
