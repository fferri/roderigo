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
	
	public final GameState game = new GameState();
	public final JRodrigoMainWindow mainWindow = new JRodrigoMainWindow(game);
	public AlphaBetaPlayer aiTask = null;
	
	public static void main(String args[]) {
		Main.getInstance();
	}
}
