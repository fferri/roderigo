package roderigo.ai;
import java.io.PrintWriter;

import roderigo.Controller;
import roderigo.struct.Board;
import roderigo.struct.BoardCellColor;

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

		controller.setSearchDepth(3);
		
		// start!
		controller.startGame();

		// end game, check board status:
		Board endBoard = controller.getBoard();
		Integer w = endBoard.getAllCells().whitePieces().size();
		Integer b = endBoard.getAllCells().blackPieces().size();
		
		endBoard.print(new PrintWriter(System.out));
		System.out.println("finished " + b + "/" + w);
		System.out.println("total time: " + controller.getTotalTime(BoardCellColor.BLACK) + "/" + controller.getTotalTime(BoardCellColor.WHITE));
		System.out.flush();
		
		return b.compareTo(w);
	}
	
	public static void main(String args[]) {
		/*
		 * first run:
		 * population:
				new AlphaBetaPlayer(new int[] {10, -86, -30, 25, 0, 0, 0, 0, 30000, -30000, -200, 200, -190, 10, 50, -50}),
				new AlphaBetaPlayer(new int[] {90, -10, 0, 0, 10, -5, 0, 0, 300, -30000, -200, 200, -190, 10, 50, -50}),
				new AlphaBetaPlayer(new int[] {0, 0, -30, 30, 10, -10, 0, 0, 30, -30, -200, 200, -190, 10, 50, -50}),
				new AlphaBetaPlayer(new int[] {10, -86, -30, 25, 0, 0, 0, 0, 0, 0, -200, 200, -190, 10, 50, -50}),
		 * match results: {6, 1, -4, -3}  (sum of wins - sum of losses)

		 */
		AIPlayer players[] = {
				// weights pairs are: mobility, border, pieces, stablePieces, corners, X, C, A+B  (16)
				new AlphaBetaPlayer(new int[] {10, -86, -30, 25, 0, 0, 0, 0, 30000, -30000, -200, 200, -190, 10, 50, -50}),
				new AlphaBetaPlayer(new int[] {90, -10, 0, 0, 10, -5, 0, 0, 300, -30000, -200, 200, -190, 10, 50, -50}),
				new AlphaBetaPlayer(new int[] {10, -20, -10, 5, 0, 2, 0, 0, 150, -50, -50, 50, 0, 10, 0, 0}),
				new AlphaBetaPlayer(new int[] {20, -10, -10, 5, 0, 2, 0, 0, 150, -50, -50, 50, 0, 10, 0, 0}),
				new AlphaBetaPlayer(new int[] {40, -30, -30, 25, 0, 0, 0, 0, 10000, -100, -200, 200, -100, 10, 50, -50}),
		};
		int score[] = new int[players.length];
		
		for(int a = 0; a < players.length; a++) {
			for(int b = 0; b < players.length; b++) {
				if(a == b) continue;
				AIPlayer black = players[a], white = players[b];
				System.out.println("match: Player" + a + " VS Player" + b + "...");
				int d = battle(black, white);
				score[a] += d;
				score[b] -= d;
			}
		}
		System.out.println("Results:");
		for(int i = 0; i < players.length; i++) {
			System.out.println("Player" + i + ": " + score[i]);
		}
		System.out.flush();
		//System.out.println(a + " VS " + b + " -> " + battle(a, b));
		//System.out.println(b + " VS " + a + " -> " + battle(b, a));
	}
}
