package roderigo.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import roderigo.Main;
import roderigo.struct.GameState;

@SuppressWarnings("serial")
public class JRodrigoMainWindow extends JFrame {
	public final JBoardWithBorder board;
	public final JToolbox toolbox;
	
	public JRodrigoMainWindow(GameState game) {
		super("roderigo");
		board = new JBoardWithBorder(game);
		toolbox = new JToolbox();
		
		board.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "abortAiTask");
		board.getActionMap().put("abortAiTask", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main main = Main.getInstance();
				
				if(main.aiTask != null)
					main.aiTask.abort();
			}
		});
		
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(board);
		add(toolbox);
		pack();
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
