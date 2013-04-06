package roderigo2.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import othello.OthelloBoard;

public class BoardEquality {
	private static final int b[][][] = {
		{
				{-1,  1,  0,  0,  1,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
		},
		{
				{-1,  0,  0,  0,  0,  0,  0,  0},
				{ 1,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 1,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
		},
		{
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{ 0,  0,  0,  0,  0,  0,  0,  0},
				{-1,  1,  0,  0,  1,  0,  0,  0},
		},
	};
	
	@Test
	public void testEqualsTransformInvariant() {
		for(int i = 0; i < b.length; i++) {
			for(int j = 0; j < b.length; j++) {
				OthelloBoard bi = new OthelloBoard(), bj = new OthelloBoard();
				bi.set(b[i]);
				bj.set(b[j]);
				if(!bi.equalsTransformInvariant(bj))
					fail("b[" + i + "] != b[" + j + "]");
			}
		}
	}

	@Test
	public void testGetCanonicalTransform() {
		final int c[] = {3,0,7};
		for(int i = 0; i < b.length; i++) {
			OthelloBoard bi = new OthelloBoard();
			bi.set(b[i]);
			assertEquals("canonical rotation of b[" + i + "]", c[i], bi.getCanonicalTransform());
		}
	}
}
