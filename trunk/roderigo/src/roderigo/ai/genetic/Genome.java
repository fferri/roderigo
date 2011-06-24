package roderigo.ai.genetic;

import java.util.Arrays;

public class Genome {
	public static enum Bit {
		ownMobility,
		opponentMobility,
		ownBorderPieceCount,
		opponentBorderPieceCount,
		ownPieceCount,
		opponentPieceCount,
		ownStablePieceCount,
		opponentStablePieceCount,
		ownCorners,
		opponentCorners,
		ownXcells,
		opponentXcells,
		ownCcells,
		opponentCcells,
		ownABcells,
		opponentABcells
	};
	
	public static final Genome DEFAULT = new Genome(10, -86, -30, 25, 0, 0, 0, 0, 30000, -30000, -200, 200, -190, 10, 50, -50);
	public static final Genome EVO1 = new Genome(92, 20, -75, 2, 48, 68, -100, -45, -67, -4, -54, 31, 83, 73, 99, -81);
	public static final Genome EVO2 = new Genome(91, -97, 8, 29, 76, 1, -94, 36, 9, 22, 41, 88, 92, -75, -64, -44);
	public static final Genome EVO6 = new Genome(92, -97, 8, 29, 76, 1, -63, 36, 9, -4, -54, 88, 83, 73, 99, -44);
	public static final Genome EVO7 = new Genome(91, -97, 8, 29, 50, 1, -63, 36, 9, -4, -54, 36, 53, 73, 99, -81);
	public static final Genome EVO8a = new Genome(91, -97, 8, 29, 76, 1, -63, 36, 9, -4, -54, 15, 83, 73, 99, -81);
	public static final Genome EVO8b = new Genome(94, -97, 8, 29, 96, 22, -45, 27, 9, 12, -54, -17, 108, 51, 80, -81);
	public static final Genome EVO8c = new Genome(91, -97, 8, 29, 76, 1, 36, 36, 9, -4, -54, 36, 53, 73, 99, -44);
	public static final Genome EVO8d = new Genome(91, -97, 8, 29, 76, 1, -94, 36, 9, 22, -54, 88, 92, -75, 86, -81);
	
	private int weights[];
	
	public Genome() {
		weights = new int[Bit.values().length];
	}
	
	public Genome(int... weights) {
		this();
		assert this.weights.length == weights.length;
		for(int i = 0; i < weights.length; i++)
			this.weights[i] = weights[i];
	}
	
	public Genome(Genome g) {
		this();
		for(int i = 0; i < weights.length; i++)
			this.weights[i] = g.weights[i];
	}
	
	public int get(Bit bit) {
		return weights[bit.ordinal()];
	}
	
	public void set(Bit bit, int value) {
		weights[bit.ordinal()] = value;
	}
	
	public int get(int bit) {
		return weights[bit];
	}
	
	public void set(int bit, int value) {
		weights[bit] = value;
	}
	
	public String toString() {
		return "<" + Arrays.toString(weights) + ">";
	}
}
