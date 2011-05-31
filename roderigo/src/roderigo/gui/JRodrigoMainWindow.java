package roderigo.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

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
	public final JMenuItem menuItemSwapTurn;
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
		menuTopGame.add(menuItemSwapTurn = new JMenuItem(new ActionSwapTurn()));
		menuTopGame.add(menuItemWakeUpAI = new JMenuItem(new ActionWakeUpAI()));
		menuTopGame.add(menuItemQuit = new JMenuItem(new ActionQuit()));
		
		menuTopOptions = new JMenu("Options");
		menuTopOptions.setMnemonic(KeyEvent.VK_O);
		menuTopOptions.getAccessibleContext().setAccessibleDescription("Options menu");
		menuBar.add(menuTopOptions);
		
		menuTopOptions.add(menuItemShowSearchAnim = new JCheckBoxMenuItem());
		menuItemShowSearchAnim.setAction(new ActionToggleOption("Show search animation", menuItemShowSearchAnim, "setShowSearchAnim"));
		menuItemShowSearchAnim.setSelected(controller.isShowSearchAnim());
		
		menuTopOptions.add(menuItemDontMakeMoves = new JCheckBoxMenuItem());
		menuItemDontMakeMoves.setAction(new ActionToggleOption("Don't make moves", menuItemDontMakeMoves, "setDontMakeMoves"));
		menuItemDontMakeMoves.setSelected(controller.isDontMakeMoves());
		
		menuTopOptions.add(menuItemAIPlaysBlack = new JCheckBoxMenuItem());
		menuItemAIPlaysBlack.setAction(new ActionToggleOption("AI plays black", menuItemAIPlaysBlack, "setAiPlaysBlack"));
		menuItemAIPlaysBlack.setSelected(controller.isAiPlaysBlack());
		
		menuTopOptions.add(menuItemAIPlaysWhite = new JCheckBoxMenuItem());
		menuItemAIPlaysWhite.setAction(new ActionToggleOption("AI plays white", menuItemAIPlaysWhite, "setAiPlaysWhite"));
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
			public void actionPerformed(ActionEvent evt) {
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
			putValue(SHORT_DESCRIPTION, "Abandon current game and start a new game");
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
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
			putValue(SHORT_DESCRIPTION, "Wakes up AI. If move computation was interrupted (ESC) can be restarted with this command");
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			controller.continueGame();
		}
	}
	
	public class ActionSwapTurn extends AbstractAction {
		public ActionSwapTurn() {
			super("Swap turn", null);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
			putValue(SHORT_DESCRIPTION, "Swap turn (if used during game, has the effect of passing move, which is not a legal move in standard Othello)");
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			controller.getGameState().switchTurn();
			controller.continueGame();
		}
	}
	
	public class ActionQuit extends AbstractAction {
		public ActionQuit() {
			super("Quit game", null);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
			putValue(SHORT_DESCRIPTION, "Quit game and terminate the program.");
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			System.exit(0);
		}
	}

	public class ActionToggleOption extends AbstractAction {
		private JCheckBoxMenuItem menuItem;
		private Method controllerMethod;
		
		public ActionToggleOption(String name, JCheckBoxMenuItem menuItem, String methodName) {
			super(name, null);
			this.menuItem = menuItem;
			try {
				controllerMethod = Controller.class.getMethod(methodName, boolean.class);
			} catch(Exception e) {
				controllerMethod = null;
				e.printStackTrace();
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			if(controllerMethod == null) return;
			try {
				controllerMethod.invoke(controller, menuItem.isSelected());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
