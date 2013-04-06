package game.ai.rl;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;
import game.player.AbstractPlayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QLearning<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> {
	private final Game<S, A, C, P> game;
	
	private AbstractPlayer<S, A, C, P> me;
	private AbstractPlayer<S, A, C, P> opponent;
	private final C myColor;
	
	private AbstractQTable<S, A, C, P> qTable; // the Q : (s,a) -> q mapping
	private QTable<S, A, C, P> visits; // used for ND worlds
	
	public QLearning(Game<S, A, C, P> game, C myColor, AbstractQTable<S, A, C, P> qTable) {
		this.game = game;
		this.myColor = myColor;
		this.qTable = qTable;
		this.visits = new QTable<S, A, C, P>();
		
		if(game == null || myColor == null)
			throw new IllegalArgumentException();
	}
	
	public C getColor() {
		return myColor;
	}
	
	public void setMyPlayer(AbstractPlayer<S, A, C, P> p) {
		if(!p.getColor().equals(myColor))
			throw new IllegalArgumentException("does not match my color");
		me = p;
	}
	
	public void setOpponentPlayer(AbstractPlayer<S, A, C, P> p) {
		if(p.getColor().equals(myColor))
			throw new IllegalArgumentException("cannot be of my color");
		opponent = p;
	}
	
	public AbstractPlayer<S, A, C, P> getMyPlayer() {
		return me;
	}
	
	public AbstractPlayer<S, A, C, P> getOpponentPlayer() {
		return opponent;
	}

	public A getBestMove(S state) {
		if(!game.getTurn(state).equals(myColor)) {
			state = game.invert(state);
		}
		List<A> validMoves = game.getAvailableActions(state);
		A bestAction = validMoves.get(0);
		double maxQ = Double.NEGATIVE_INFINITY, newQ;
		for(A action : validMoves) {
			newQ = qTable.get(state, action);
			if(newQ > maxQ) {
				maxQ = newQ;
				bestAction = action;
			}
		}
		return bestAction;
	}
	
	public void load(File f) throws IOException {
		S dummy = game.getInitialState();
		BufferedReader r = new BufferedReader(new FileReader(f));
		try {
			String s = r.readLine();
			if(s == null) throw new RuntimeException("bad format");
			int version = Integer.parseInt(s);
			if(version > 1) throw new RuntimeException("bad version");
			s = r.readLine();
			if(s == null) throw new RuntimeException("bad format");
			C c = dummy.colorFromString(s);
			if(!myColor.equals(c)) throw new RuntimeException("bad color");
			qTable.load(r, game);
			visits.load(r, game);
			r.close();
		} finally {
			r.close();
		}
	}
	
	public void save(File f) throws IOException {
		S dummy = game.getInitialState();
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		w.write("1\n");
		w.write(dummy.colorToString(myColor) + "\n");
		qTable.save(w, game);
		visits.save(w, game);
		w.close();
	}
	
	public static interface TrainingEventListener {
		void epochFinished(int epoch, int totEpochs);
		
		void trainingFinishged(int totEpochs);
	}
	
	private List<TrainingEventListener> trainingEventListeners = new ArrayList<>();
	
	public void addTrainingEventListener(TrainingEventListener listener) {
		trainingEventListeners.add(listener);
	}
	
	public void removeTrainingEventListener(TrainingEventListener listener) {
		trainingEventListeners.remove(listener);
	}
	
	protected void fireTrainingEvent(Integer epochOrNullIfFinished, int totEpochs) {
		for(TrainingEventListener listener : trainingEventListeners) {
			if(epochOrNullIfFinished == null)
				listener.trainingFinishged(totEpochs);
			else
				listener.epochFinished(1 + epochOrNullIfFinished.intValue(), totEpochs);
		}
	}
	
	public void train(int numEpochs, double gamma, boolean ND) {
		for(int i = 0; i < numEpochs; i++) {
			trainForOneGame(gamma, ND);
			fireTrainingEvent(i, numEpochs);
		}
		fireTrainingEvent(null, numEpochs);
	}
	
	public void trainForOneGame(double gamma, boolean ND) {
		S state = game.getInitialState();
		
		if(game.getTurn(state).equals(opponent.getColor()))
			state = game.execute(state, opponent.getMove(game, state));
		
		while(!game.isGameOver(state))
			state = trainForOnePly(state, gamma, ND);
	}
	
	private S trainForOnePly(S s, double gamma, boolean ND) {
		if(me == null || opponent == null)
			throw new IllegalStateException("missing players");
		if(game.isGameOver(s))
			throw new IllegalStateException("board in gameover state");

		A a;
		S s1;
		double r;
		
		//   select an action (randomly)
		List<A> validMoves = game.getAvailableActions(s);
		
		a = validMoves.get((int)(Math.random() * validMoves.size()));
		
		//   and execute it
		s1 = game.execute(s, a);
		
		//   observe new state
		if(!game.isGameOver(s1)) {
			// opponent's turn
			s1 = game.execute(s1, opponent.getMove(game, s1));
		}
		// receive reward
		C winner = game.getWinner(s1);
		if(winner != null && winner.equals(myColor)) r = 1000;
		else if(winner != null && !winner.equals(myColor)) r = -1000;
		else r = 0;
		
		//   update Q table using Q Learning for deterministic worlds
		double maxQ = 0;
		for(A a1 : game.getAvailableActions(s)) // XXX: really no need to getValidActions? never reward PASS
			maxQ = Math.max(maxQ, qTable.get(s1, a1));
		
		if(ND) {
			double oldQ = qTable.get(s, a);
			double alpha = Math.pow(1 + visits.get(s, a), -1);
			qTable.set(s, a, (1 - alpha) * oldQ + alpha * (r + gamma * maxQ));
			visits.set(s, a, 1 + visits.get(s, a));
		} else {
			qTable.set(s, a, r + gamma * maxQ);
		}
		
		return s1;
	}
	
	public AbstractQTable<S, A, C, P> getQTable() {
		return qTable;
	}
}
