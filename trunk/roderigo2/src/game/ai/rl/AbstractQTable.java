package game.ai.rl;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 * Abstract Q-Table
 * 
 * @author Federico Ferri
 *
 * @param <S> class representing a board (the state)
 * @param <A> class representing an action
 * @param <C> class representing a color
 * @param <P> class representing a position
 */
public abstract class AbstractQTable<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> {
	public abstract double get(S state, A action);
	
	public abstract void set(S state, A action, double value);

	public abstract void load(File f, Game<S, A, C, P> game) throws IOException;
	
	public abstract void load(BufferedReader r, Game<S, A, C, P> game) throws IOException;
	
	public abstract void save(File f, Game<S, A, C, P> game) throws IOException;
	
	public abstract void save(BufferedWriter w, Game<S, A, C, P> game) throws IOException;
}
