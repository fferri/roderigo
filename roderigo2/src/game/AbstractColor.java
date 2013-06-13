package game;

/**
 * Abstract generic class representing the player color
 * 
 * @author Federico Ferri
 *
 */
public abstract class AbstractColor {
	protected final int value;
	
	protected AbstractColor(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof AbstractColor)
			return ((AbstractColor)obj).value == value;
		else
			return false;
	}
	
	@Override
	public String toString() {
		return String.format("Color[%d]", value);
	}
	
	@Override
	public int hashCode() {
		return new Integer(value).hashCode();
	}
}
