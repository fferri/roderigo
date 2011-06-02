package roderigo.ai;
import java.io.PrintWriter;

import roderigo.Controller;
import roderigo.struct.Board;

/**
 * Take two AIs and make them play one against the other, for two matches,
 * and determine the winner, if possible.
 * 
 * @author Federico Ferri
 *
 */
public class Battle {
	public static int battle(AIPlayer black, AIPlayer white) {
		Controller controller = Controller.newController(black, white);
		
		// no separate thread here
		controller.setRunAiTaskInBackground(false);
		
		// AI vs AI
		controller.setAiPlaysBlack(true);
		controller.setAiPlaysWhite(true);
	
		// start!
		controller.startGame();

		// end game, check board status:
		Board endBoard = controller.getBoard();
		Integer w = endBoard.getAllCells().whitePieces().size();
		Integer b = endBoard.getAllCells().blackPieces().size();
		
		endBoard.print(new PrintWriter(System.out));
		System.out.println("finished " + b + "/" + w);
		System.out.flush();
		
		return b.compareTo(w);
	}
	
	public static void main(String args[]) {
		AIPlayer a = new AlphaBetaPlayer(),
			b = new AlphaBetaPlayer();
		System.out.println(a + " VS " + b + " -> " + battle(a, b));
		System.out.println(b + " VS " + a + " -> " + battle(b, a));
	}
}
