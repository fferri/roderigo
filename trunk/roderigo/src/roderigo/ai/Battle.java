package roderigo.ai;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import roderigo.Controller;
import roderigo.ai.genetic.GeneticOperator;
import roderigo.ai.genetic.Genome;
import roderigo.struct.Board;
import roderigo.struct.BoardCellColor;
import roderigo.struct.GameState;

/**
 * Take two AIs and make them play one against the other, for two matches,
 * and determine the winner, if possible.
 * 
 * @author Federico Ferri
 *
 */
public class Battle {
	private static final class Result {
		public final int black;
		public final int white;
		public final int c;
		public final long blackTime;
		public final long whiteTime;
		
		public Result(int b, int w, long tb, long tw) {
			black = b;
			white = w;
			c = new Integer(b).compareTo(new Integer(w));
			blackTime = tb;
			whiteTime = tw;
		}
	}
	
	public static Result battle(GameState s, AIPlayer black, AIPlayer white) {
		Controller controller = Controller.newController(s, black, white);
		
		// no separate thread here
		controller.setRunAiTaskInBackground(false);
		
		// AI vs AI
		controller.setAiPlaysBlack(true);
		controller.setAiPlaysWhite(true);

		//controller.setSearchDepth(3);
		
		// start!
		controller.startGame();

		// end game, check board status:
		Board endBoard = controller.getBoard();
		
		return new Result(endBoard.getAllCells().blackPieces().size(), endBoard.getAllCells().whitePieces().size(), controller.getTotalTime(BoardCellColor.BLACK), controller.getTotalTime(BoardCellColor.WHITE));
	}
	
	public static List<AIPlayer> getKBestPlayers(final Map<AIPlayer, Integer> score, int k) {
		assert k > 0;

		List<AIPlayer> playersOrderedByScore = new ArrayList<AIPlayer>();
		playersOrderedByScore.addAll(score.keySet());
		Collections.sort(playersOrderedByScore, new Comparator<AIPlayer>() {
			public int compare(AIPlayer p1, AIPlayer p2) {
				Integer v1 = score.get(p1);
				Integer v2 = score.get(p2);
				return -v1.compareTo(v2);
			}
		});
		
		List<AIPlayer> kBest = new ArrayList<AIPlayer>();
		for(AIPlayer p : playersOrderedByScore) {
			if(--k < 0) break;
			kBest.add(p);
		}

		return kBest;
	}
	
	public static Set<AIPlayer> evolve(List<AIPlayer> kBest, int numMutations, int numCrossovers, int numRandoms) {
		int k = kBest.size();
		
		assert k > 0;

		assert numMutations <= k;
		
		Set<AIPlayer> result = new HashSet<AIPlayer>();
		
		if(numMutations > 0) {
			boolean used[] = new boolean[k];
			int i;
			while(numMutations-- > 0) {
				do {
					i = GeneticOperator.randomInt(0, k);
				} while(used[i]);
				AlphaBetaPlayer pick = (AlphaBetaPlayer) kBest.get(i);
				Genome newGenome = GeneticOperator.randomMutations(pick.getGenome());
				result.add(new AlphaBetaPlayer(newGenome));
				used[i] = true;
			}
		}
		
		if(numCrossovers > 0) {
			boolean used[][] = new boolean[k][k];
			int a, b;
			while(numCrossovers-- > 0) {
				do {
					a = GeneticOperator.randomInt(0, k);
					b = GeneticOperator.randomInt(0, k);
				} while(a == b || used[a][b] || used[b][a]);
				AlphaBetaPlayer pickA = (AlphaBetaPlayer) kBest.get(a);
				AlphaBetaPlayer pickB = (AlphaBetaPlayer) kBest.get(b);
				Genome newGenome = GeneticOperator.crossover(pickA.getGenome(), pickB.getGenome());
				result.add(new AlphaBetaPlayer(newGenome));
				used[a][b] = used[b][a] = true;
			}
		}
		
		while(numRandoms-- > 0) {
			Genome randomGenome = GeneticOperator.fullyRandomGenome();
			result.add(new AlphaBetaPlayer(randomGenome));
		}
		
		return result;
	}
	
	public static GameState generateGameAtPercent(double p) {
		GameState s = new GameState();
		Board b = s.getBoard();
		AlphaBetaPlayer pl = new AlphaBetaPlayer(Genome.DEFAULT);
		while(b.getAllPieces().size() < p * 64) {
			try {
				s.move(pl.getBestMove(s));
			} catch(AbortException e) {}
		}
		return s;
	}
	
	public static void main(String args[]) {
		final double px = 0.68;
		System.out.print("Generating a game at " + (px * 100) + "% stage...");
		System.out.flush();
		GameState s = generateGameAtPercent(px);
		System.out.println(" done.");
		s.getBoard().print(new PrintWriter(System.out));
		
		final int populationSize = 16;
		// summ of the following must be equal to populationSize
		// in order to keep populationSize constant:
		final int kBest = 7;
		final int numMutations = 4;
		final int numCrossovers = 4;
		final int numRandoms = 1;
		// ----------------------------------------------------
		//assert populationSize == (kBest + numMutations + numCrossovers + numRandoms);
		
		System.out.print("Generating random startup population of " + populationSize + " players...");
		System.out.flush();
		Set<AIPlayer> players = new HashSet<AIPlayer>();
		// weights pairs are: mobility, border, pieces, stablePieces, corners, X, C, A+B  (16)
		//players.add(new AlphaBetaPlayer(new Genome(-86, -30, 25, 0, 0, 0, 0, 30000, -30000, -200, 200, -190, 10, 50, -50)));
		//players.add(new AlphaBetaPlayer(new Genome(90, -10, 0, 0, 10, -5, 0, 0, 300, -30000, -200, 200, -190, 10, 50, -50)));
		//players.add(new AlphaBetaPlayer(new Genome(40, -30, -30, 25, 0, 0, 0, 0, 10000, -100, -200, 200, -100, 10, 50, -50)));
		// add some random genomes
		for(int i = 0; i < populationSize; i++)
			players.add(new AlphaBetaPlayer(GeneticOperator.fullyRandomGenome()));
		System.out.println(" done.");
		
		final int numRuns = 10;
		
		for(int currentRun = 1; currentRun <= numRuns; currentRun++) {
			System.out.println("######### RUN " + currentRun + "/" + numRuns + " BEGIN ###########################");
			
			for(AIPlayer p : players) {
				System.out.println("" + p + ": " + ((AlphaBetaPlayer) p).getGenome());
			}
			
			long runStart = System.currentTimeMillis();
			
			Map<AIPlayer, Integer> score = new HashMap<AIPlayer, Integer>();
			
			int numMatches = players.size() * (players.size() - 1);
			int currentMatch = 0;
			
			for(AIPlayer black : players) {
				for(AIPlayer white : players) { if(white == black) continue;
				System.out.print("Match " + ++currentMatch + "/" + numMatches + ": " + black + " VS " + white + "... ");
				System.out.flush();
				
				Result r = battle(s, black, white);
				Integer blackScore = score.get(black);
				if(blackScore == null) blackScore = 0;
				Integer whiteScore = score.get(white);
				if(whiteScore == null) whiteScore = 0;
				score.put(black, blackScore + r.c);
				score.put(white, whiteScore - r.c);
				
				System.out.println(r.black + "/" + r.white + ", time=" + r.blackTime + "/" + r.whiteTime);
				System.out.flush();
				}
			}
			
			System.out.println("Results:");
			for(AIPlayer p : players) {
				System.out.println("" + p + ": " + score.get(p));
			}
			
			List<AIPlayer> bestPlayers = getKBestPlayers(score, kBest);
			System.out.println(kBest + " best players: " + bestPlayers);
			
			players = new HashSet<AIPlayer>();
			players.addAll(bestPlayers);
			System.out.print("Evolving population...");
			System.out.flush();
			players.addAll(evolve(bestPlayers, numMutations, numCrossovers, numRandoms));
			System.out.println(" done.");
			System.out.println("New population: " + players);
			
			System.out.println("######### RUN " + currentRun + "/" + numRuns + " END #############################");
			
			System.out.println("time: " + (System.currentTimeMillis() - runStart) + "ms");
			System.out.flush();
		}
	}
}
