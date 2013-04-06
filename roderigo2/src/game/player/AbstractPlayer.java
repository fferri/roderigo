package game.player;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;

public abstract class AbstractPlayer<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> {
	private String name;
	private C color;
	
	public AbstractPlayer(String name, C color) {
		this.name = name;
		this.color = color;
	}
	
	public String getName() {
		return name;
	}
	
	public C getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return String.format("Player[%s,%s]", name, color);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() + color.hashCode();
	}
	
	public abstract A getMove(Game<S, A, C, P> game, S state);
}
