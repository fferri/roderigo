package roderigo.robot;

import java.util.Iterator;

public class PermutationGenerator implements Iterable<int[]> {
	private int[] a;
	private long numLeft;
	private long total;

	public PermutationGenerator(int n) {
		if(n < 1) {
			throw new IllegalArgumentException("Min 1");
		}
		a = new int[n];
		
		// total = n!
		total = 1;
		for(int i = n; i > 1; i--) total = total * i;

		reset();
	}
	
	public void reset() {
		for(int i = 0; i < a.length; i++) {
			a[i] = i;
		}
		numLeft = total;
	}
	
	public Iterator<int[]> iterator() {
		reset();
		return new Iterator<int[]>() {
			@Override public boolean hasNext() {
				return numLeft > 0;
			}

			@Override public int[] next() {
				if(numLeft == total) {
					numLeft = numLeft - 1;
					return a;
				}

				int temp;

				// Find largest index j with a[j] < a[j+1]
				int j = a.length - 2;
				while(a[j] > a[j + 1]) {
					j--;
				}

				// Find index k such that a[k] is smallest integer
				// greater than a[j] to the right of a[j]
				int k = a.length - 1;
				while(a[j] > a[k]) {
					k--;
				}

				// Interchange a[j] and a[k]
				temp = a[k];
				a[k] = a[j];
				a[j] = temp;

				// Put tail end of permutation after jth position in increasing order
				int r = a.length - 1;
				int s = j + 1;

				while(r > s) {
					temp = a[s];
					a[s] = a[r];
					a[r] = temp;
					r--;
					s++;
				}

				numLeft = numLeft - 1;
				return a;
			}

			@Override public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
