package game;

/**
 * An abstract generic representation of an action in a board game
 * 
 * @author Federico Ferri
 *
 * @param <P> class representing the position
 */
public abstract class AbstractAction<P extends AbstractPosition> {
	protected final P position;
	
	protected AbstractAction() {
		this(null);
	}
	
	protected AbstractAction(P position) {
		this.position = position;
	}
	
	public boolean isPass() {
		return position == null;
	}
	
	public P getPosition() {
		return position;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof AbstractAction) {
			@SuppressWarnings("unchecked")
			AbstractAction<P> a = (AbstractAction<P>)obj;
			if(isPass()) return a.isPass();
			return position.equals(a.position);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return String.format("Action[%s]", isPass() ? "PASS" : position);
	}
	
	@Override
	public int hashCode() {
		return position != null ? position.hashCode() : 0;
	}
	
	@Override
	public abstract Object clone();
}
