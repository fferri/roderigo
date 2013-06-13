package game;

import game.player.AbstractPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible of the game flow
 * 
 * @author Federico Ferri
 *
 * @param <S> class representing a board (the state)
 * @param <A> class representing an action
 * @param <C> class representing a color
 * @param <P> class representing a position
 */
public class GameController<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> {
	private final Game<S, A, C, P> game;
	private final Map<C, AbstractPlayer<S, A, C, P>> players = new HashMap<>();

	public GameController(Game<S, A, C, P> game) {
		this.game = game;
	}
	
	public void addPlayer(AbstractPlayer<S, A, C, P> p) {
		if(players.containsKey(p.getColor()))
			throw new RuntimeException("already present one player of that color");
		players.put(p.getColor(), p);
	}
	
	public void removePlayer(P p) {
		players.remove(p);
	}
	
	public AbstractPlayer<S, A, C, P> getPlayer(C player) {
		return players.get(player);
	}
	
	public void play(S state) {
		fireStateChangedEvent(state);
		while(!game.isGameOver(state)) {
			AbstractPlayer<S, A, C, P> player = players.get(game.getTurn(state));
			A action = player.getMove(game, state);
			fireMovePlayedEvent(state, action, player.getColor());
			if(!game.executeInPlace(state, action))
				throw new RuntimeException(player.getName() + " made invalid move");
			fireStateChangedEvent(state);
		}
		fireGameOverEvent(state);
	}
	
	private final List<GameListener<S, A, C, P>> gameListeners = new ArrayList<>();
	{
		gameListeners.add(new GameListener<S, A, C, P>() {
			public void stateChanged(S newState) {
				//System.out.println(newState);
				//System.out.println("available actions: " + newState.getValidActions());
			}
			
			public void movePlayed(S state, A action, C player) {
				//System.out.println(player + " plays " + action);
			}
			
			public void gameOver(S state) {
				//C winner = state.getWinner();
				//System.out.println("game over. " + (winner == null ? "draw." : (winner + " wins.")));
			}
		});
	}
	
	public static interface GameListener<S, A, C, P> {
		public void stateChanged(S newState);
		
		public void movePlayed(S state, A action, C player);
		
		public void gameOver(S state);
	}
	
	public void addGameListener(GameListener<S, A, C, P> listener) {
		gameListeners.add(listener);
	}
	
	public void removeGameListener(GameController<S, A, C, P> listener) {
		gameListeners.remove(listener);
	}
	
	protected void fireStateChangedEvent(S newState) {
		for(GameListener<S, A, C, P> listener : gameListeners)
			listener.stateChanged(newState);
	}
	
	protected void fireMovePlayedEvent(S state, A action, C player) {
		for(GameListener<S, A, C, P> listener : gameListeners)
			listener.movePlayed(state, action, player);
	}
	
	protected void fireGameOverEvent(S state) {
		for(GameListener<S, A, C, P> listener : gameListeners)
			listener.gameOver(state);
	}
}
