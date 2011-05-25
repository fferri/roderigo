package roderigo.struct;

import java.util.Arrays;
import java.util.Collection;

public class Direction {
	private int dr;
	private int dc;
	
	private Direction(int dr, int dc) {
		this.dr = dr;
		this.dc = dc;
	}
	
	public int getDr() {
		return dr;
	}
	
	public int getDc() {
		return dc;
	}

	public String toString() {
		return String.format("<%d,%d>", dr, dc);
	}
	
	public static final Direction N = new Direction(-1, 0);
	public static final Direction S = new Direction(1, 0);
	public static final Direction E = new Direction(0, 1);
	public static final Direction W = new Direction(0, -1);
	public static final Direction NE = new Direction(-1, 1);
	public static final Direction NW = new Direction(-1, -1);
	public static final Direction SE = new Direction(1, 1);
	public static final Direction SW = new Direction(1, -1);
	
	public static final Collection<Direction> allDirections = Arrays.asList(new Direction[] {N, S, E, W, NE, NW, SE, SW});
	public static final Collection<Direction> cardinalDirections = Arrays.asList(new Direction[] {N, S, E, W});
}
