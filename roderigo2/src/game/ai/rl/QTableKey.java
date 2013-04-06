package game.ai.rl;

import game.AbstractAction;
import game.AbstractBoard;

public class QTableKey<S extends AbstractBoard<?, A, ?>, A extends AbstractAction<?>> {
	private S state;
	private A action;
	
	@SuppressWarnings("unchecked")
	public QTableKey(S state, A action) {
		// key must be immutable. clone objects for safety
		this.state = (S)state.clone();
		this.action = (A)action.clone();
	}
	
	public S getState() {
		return state;
	}
	
	public A getAction() {
		return action;
	}
	
	public int hashCode() {
		return state.hashCode() << 6 + action.hashCode();
	}
	
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o instanceof QTableKey) {
			@SuppressWarnings("unchecked")
			QTableKey<S, A> k = (QTableKey<S, A>)o;
			S ostate = k.getState();
			A oaction = k.getAction();
			return state.equals(ostate) && action.equals(oaction);
		} else return false;
	}
}
