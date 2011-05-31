package roderigo.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

import roderigo.Controller;
import roderigo.Main;

@SuppressWarnings("serial")
public class JRodrigoMainWindow extends JFrame {
	public final JMenuBar menuBar;
	public final JMenu menuTopGame;
	public final JMenuItem menuItemNewGame;
	public final JMenuItem menuItemWakeUpAI;
	public final JMenuItem menuItemQuit;
	public final JMenu menuTopOptions;
	public final JCheckBoxMenuItem menuItemShowSearchAnim;
	public final JCheckBoxMenuItem menuItemDontMakeMoves;
	public final JCheckBoxMenuItem menuItemAIPlaysBlack;
	public final JCheckBoxMenuItem menuItemAIPlaysWhite;
	
	public final JBoardWithBorder jboardBorder;
	public final JBoard jboard;
	public final JToolbox toolbox;
	
	private Controller controller;
	
	public JRodrigoMainWindow(Controller controller) {
		super("roderigo");
		
		this.controller = controller;
		
		menuBar = new JMenuBar();
		menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
		
		menuTopGame = new JMenu("Game");
		menuTopGame.setMnemonic(KeyEvent.VK_G);
		menuTopGame.getAccessibleContext().setAccessibleDescription("Game menu");
		menuBar.add(menuTopGame);

		menuTopGame.add(menuItemNewGame = new JMenuItem(new ActionNewGame()));
		menuTopGame.add(menuItemWakeUpAI = new JMenuItem(new ActionWakeUpAI()));
		menuTopGame.add(menuItemQuit = new JMenuItem(new ActionQuit()));
		
		menuTopOptions = new JMenu("Options");
		menuTopOptions.setMnemonic(KeyEvent.VK_O);
		menuTopOptions.getAccessibleContext().setAccessibleDescription("Options menu");
		menuBar.add(menuTopOptions);
		
		menuTopOptions.add(menuItemShowSearchAnim = new JCheckBoxMenuItem(new ActionToggleOption("Show search animation", 0)));
		menuItemShowSearchAnim.setSelected(controller.isShowSearchAnim());
		menuTopOptions.add(menuItemDontMakeMoves = new JCheckBoxMenuItem(new ActionToggleOption("Don't make moves", 1)));
		menuItemDontMakeMoves.setSelected(controller.isDontMakeMoves());
		menuTopOptions.add(menuItemAIPlaysBlack = new JCheckBoxMenuItem(new ActionToggleOption("AI plays black", 10)));
		menuItemAIPlaysBlack.setSelected(controller.isAiPlaysBlack());
		menuTopOptions.add(menuItemAIPlaysWhite = new JCheckBoxMenuItem(new ActionToggleOption("AI plays white", 11)));
		menuItemAIPlaysWhite.setSelected(controller.isAiPlaysWhite());

		menuBar.add(menuTopGame);
		menuBar.add(menuTopOptions);
		
		setJMenuBar(menuBar);
		
		jboardBorder = new JBoardWithBorder(controller);
		jboard = jboardBorder.jboard;
		
		toolbox = new JToolbox(controller);
		
		jboardBorder.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "abortAiTask");
		jboardBorder.getActionMap().put("abortAiTask", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main main = Main.getInstance();
				
				if(main.aiTask != null)
					main.aiTask.abort();
			}
		});
		
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(jboardBorder);
		add(toolbox);
		pack();
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public class ActionNewGame extends AbstractAction {
		public ActionNewGame() {
			super("New game", null);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(controller.getTurn() == null || JOptionPane.showConfirmDialog(JRodrigoMainWindow.this, "Are you sure?", "Abandon current game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				controller.newGame();
				controller.startGame();
			}
		}
	}
	
	public class ActionWakeUpAI extends AbstractAction {
		public ActionWakeUpAI() {
			super("Wake up AI", null);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			controller.continueGame();
		}
	}
	
	public class ActionQuit extends AbstractAction {
		public ActionQuit() {
			super("Quit game", null);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	public class ActionToggleOption extends AbstractAction {
		public final int i;
		
		public ActionToggleOption(String name, int i) {
			super(name, null);
			this.i = i;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(i) {
			case 0:
				controller.setShowSearchAnim(menuItemShowSearchAnim.isSelected());
				break;
			case 1:
				controller.setDontMakeMoves(menuItemDontMakeMoves.isSelected());
				break;
			case 10:
				controller.setAiPlaysBlack(menuItemAIPlaysBlack.isSelected());
				break;
			case 11:
				controller.setAiPlaysWhite(menuItemAIPlaysWhite.isSelected());
				break;
			}
		}
	}
}
