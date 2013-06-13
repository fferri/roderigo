package game;

/**
 * Abstract generic class representing a position on a board
 * 
 * @author Federico Ferri
 *
 */
public abstract class AbstractPosition {
	protected final int index;
	
	protected AbstractPosition(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public abstract int getRow();
	
	public abstract int getCol();
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof AbstractPosition)
			return ((AbstractPosition)obj).index == index;
		else
			return false;
	}
	
	@Override
	public String toString() {
		return String.format("Position[%d,%d]", getRow(), getCol());
	}
	
	@Override
	public int hashCode() {
		return new Integer(index).hashCode();
	}
}
