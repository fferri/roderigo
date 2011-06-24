package roderigo.ai.genetic;

import java.util.Arrays;

public final class GeneticOperator {
	private static final int DOMAIN_MIN = -100;
	private static final int DOMAIN_MAX = 100;
	
	public static void main(String[] args) {
		Genome a = new Genome(1, 8, 4, 3, 7, 9, 2, 5, 1, 3, 2, 6, 7, 4, 4, 6);
		Genome b = new Genome(7, 9, 4, 5, 5, 5, 8, 6, 3, 7, 8, 4, 4, 5, 6, 9);
		int crossoverPoints[] = randomLengthRandomOrderedList(1, Genome.Bit.values().length, 0, Genome.Bit.values().length);
		System.out.println("A = " + a);
		System.out.println("B = " + b);
		System.out.println("crossoverPoints = " + Arrays.toString(crossoverPoints));
		System.out.println("X = " + crossover(a, b, crossoverPoints));
		System.out.println("-");
		Genome r = fullyRandomGenome();
		System.out.println("Rand = " + r);
		System.out.println("Mutation1 = " + randomMutations(r));
		System.out.println("Mutation2 = " + randomMutations(r));
	}
	
	public static Genome randomMutations(Genome g) {
		return randomMutations(g, randomLengthRandomOrderedList(1, Genome.Bit.values().length, 0, Genome.Bit.values().length), DOMAIN_MIN / 4, DOMAIN_MAX / 4);
	}
	
	public static Genome randomMutations(Genome g, int mutationPoints[], int minDelta, int maxDelta) {
		Genome r = new Genome(g);
		for(int p = 0; p < mutationPoints.length; p++) {
			int delta = randomInt(minDelta, maxDelta);
			int i = mutationPoints[p];
			r.set(i, r.get(i) + delta);
		}
		return r;
	}
	
	public static Genome fullyRandomGenome() {
		return new Genome(randomList(Genome.Bit.values().length, DOMAIN_MIN, DOMAIN_MAX));
	}
	
	public static Genome crossover(Genome a, Genome b) {
		int crossoverPoints[] = randomLengthRandomOrderedList(1, Genome.Bit.values().length, 0, Genome.Bit.values().length);
		return crossover(a, b, crossoverPoints);
	}
	
	public static Genome crossover(Genome a, Genome b, int crossoverPoints[]) {
		assert crossoverPoints.length > 0;
		Genome c = new Genome();
		int z = 0;
		boolean h = true;
		boolean flipped = false;
		for(Genome.Bit bit : Genome.Bit.values()) {
			flipped = false;
			while(z < crossoverPoints.length && bit.ordinal() >= crossoverPoints[z]) {
				if(!flipped) {
					h = !h;
					flipped = true;
				}
				z++;
			}
			c.set(bit, h ? a.get(bit) : b.get(bit));
		}
		return c;
	}
	
	public static int randomInt(int minValue, int maxValue) {
		assert maxValue >= minValue;
		
		return minValue + (int) (Math.random() * (maxValue - minValue));
	}
	
	public static int[] randomList(int numElements, int minValue, int maxValue) {
		assert numElements > 0;
		assert maxValue >= minValue;
		
		int result[] = new int[numElements];
		
		for(int i = 0; i < numElements; i++)
			result[i] = randomInt(minValue, maxValue);
		
		return result;
	}
	
	public static int[] randomOrderedList(int numElements, int minValue, int maxValue) {
		int l[] = randomList(numElements, minValue, maxValue);
		Arrays.sort(l);
		return l;
	}
	
	public static int[] randomLengthRandomList(int minLength, int maxLength, int minValue, int maxValue) {
		int len = randomInt(minLength, maxLength);
		return randomList(len, minValue, maxValue);
	}
	
	public static int[] randomLengthRandomOrderedList(int minLength, int maxLength, int minValue, int maxValue) {
		int l[] = randomLengthRandomList(minLength, maxLength, minValue, maxValue);
		Arrays.sort(l);
		return l;
	}
	
	public static boolean allDifferent(int l[]) {
		int m[] = Arrays.copyOf(l, l.length);
		Arrays.sort(m);
		int last = m[0];
		for(int i = 1; i < m.length; i++) {
			if(m[i] == last) return false;
			last = m[i];
		}
		return true;
	}
}
