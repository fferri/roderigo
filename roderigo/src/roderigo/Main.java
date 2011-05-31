package roderigo;

import roderigo.ai.AlphaBetaPlayer;
import roderigo.gui.JRodrigoMainWindow;
import roderigo.struct.GameState;

public class Main {
	private static Main instance = null;
	
	public static synchronized Main getInstance() {
		if(instance == null) {
			instance = new Main();
		}
		return instance;
	}
	
	private final GameState game;
	private final Controller controller;
	
	public final JRodrigoMainWindow mainWindow;
	
	// remember current ai task (so we can eventually stop it):
	public AlphaBetaPlayer aiTask = null;
	
	// Constructor
	private Main() {
		controller = new Controller();
		
		game = new GameState();
		mainWindow = new JRodrigoMainWindow(controller);
		
		controller.setGameState(game);
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
