package game;

import java.util.List;

/**
 * Interface for describing a board game
 * 
 * @author Federico Ferri
 *
 * @param <S> class representing a board (the state)
 * @param <A> class representing an action
 * @param <C> class representing a color
 * @param <P> class representing a position
 */
public interface Game<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> {
	/**
	 * Return the initial state of the game.
	 * 
	 * @return The initial state of the game.
	 */
	public S getInitialState();
	
	public S cloneState(S state);
	
	/**
	 * Return the opponent of player
	 * 
	 * @param player The actual player
	 * @return The opponent player
	 */
	public C getOpponent(C player);
	
	/**
	 * Get the available actions in the given state
	 * 
	 * @param state The state
	 * @return The list of actions
	 */
	public List<A> getAvailableActions(S state);
	
	/**
	 * Return whose player has to play in the given state
	 * 
	 * @param state The state
	 * @return The player who has to play
	 */
	public C getTurn(S state);
	
	/**
	 * Execute the given action in the given state
	 * 
	 * @param state The state
	 * @param action The action
	 * @return The new state, or null if the action was not valid
	 */
	public S execute(S state, A action);
	
	public boolean executeInPlace(S state, A action);
	
	/**
	 * Invert board colors and turn
	 * 
	 * @param state The state
	 */
	public S invert(S state);
	
	/**
	 * Check if the game is over
	 * 
	 * @param state The state
	 * @return True if the game is over
	 */
	public boolean isGameOver(S state);
	
	/**
	 * Check the winner in a state
	 * 
	 * @param state The state
	 * @return The winning player, or null if draw or not game over
	 */
	public C getWinner(S state);
	
	public double evaluate(S state, C player);
	
	public String stateToString(S state);
	
	public S stateFromString(String str);
}
