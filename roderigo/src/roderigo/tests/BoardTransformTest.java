package roderigo.tests;

import java.io.PrintWriter;
import java.util.Random;

import roderigo.struct.Board;

public class BoardTransformTest {
	public static void main(String[] args) throws Exception {
		testBoardTransform();
		testInvariance();
		System.out.println("All tests OK.");
	}
	
	private static void testBoardTransform() throws Exception
	{
		PrintWriter pw = new PrintWriter(System.out);
		
		final int n = 4;

		Board b = new Board(new int[][] {
			{0, 1, 0, 0},
			{0, 1, 2, 0},
			{2, 2, 1, 2},
			{1, 0, 0, 0}
		});
		
		int expectedBoardData[][][] = new int[][][] {
				{// B0 - identity
					{0, 1, 0, 0},
					{0, 1, 2, 0},
					{2, 2, 1, 2},
					{1, 0, 0, 0}
				},
				{// BR - rotate 90
					{1, 2, 0, 0},
					{0, 2, 1, 1},
					{0, 1, 2, 0},
					{0, 2, 0, 0}
				},
				{// BRR - rotate 180
					{0, 0, 0, 1},
					{2, 1, 2, 2},
					{0, 2, 1, 0},
					{0, 0, 1, 0}
				},
				{// BRRR - rotate 270
					{0, 0, 2, 0},
					{0, 2, 1, 0},
					{1, 1, 2, 0},
					{0, 0, 2, 1}
				},
				{// BH - flip-x
					{0, 0, 1, 0},
					{0, 2, 1, 0},
					{2, 1, 2, 2},
					{0, 0, 0, 1}
				},
				{// BRH - flip-x + rotate 90
					{0, 2, 0, 0},
					{0, 1, 2, 0},
					{0, 2, 1, 1},
					{1, 2, 0, 0}
				},
				{// BV - flip-x + rotate 180
					{1, 0, 0, 0},
					{2, 2, 1, 2},
					{0, 1, 2, 0},
					{0, 1, 0, 0}
				},
				{// BRV - flip-x + rotate 270
					{0, 0, 2, 1},
					{1, 1, 2, 0},
					{0, 2, 1, 0},
					{0, 0, 2, 0}
				}
		};
		
		String transformName[] = {
				"B0", "BR", "BRR", "BRRR", "BH", "BRH", "BV", "BRV"
		};
		
		for(int i = 0; i < 8; i++) {
			Board b1 = new Board(n, n);
			b1.copyFrom(b, i);
			Board bref = new Board(expectedBoardData[i]);
			if(!b1.equals(bref)) {
				String err = "Failed transform T" + i + " (" + transformName[i] + ")";
				System.out.println(err);
				System.out.println("SOURCE:");
				b.print(pw);
				System.out.println("REFERENCE RESULT:");
				bref.print(pw);
				System.out.println("ACTUAL RESULT:");
				b1.print(pw);
				throw new RuntimeException(err);
			}
		}
		
		int c1, c2;
		for(int t1 = 0; t1 < 8; t1++) {
			for(int t2 = t1; t2 < 8; t2++) {
				c1 = b.compareTransform(t1, t2);
				c2 = b.compareTransform(t2, t1);
				
				if(t1 == t2 && (c1 != 0 || c2 != 0))
					throw new RuntimeException("Failed compareTransform property c(t1,t1)=0");
				else if((c1 + c2) != 0)
					throw new RuntimeException("Failed compareTransform property c(t1,t2)=-c(t2,t1)");
			}
		}
	}
	
	private static void testInvariance() throws Exception {
		final int n = 8;
		
		Random rnd = new Random(0xdeadbeef ^ System.currentTimeMillis());
		
		// make a random board
		Board b = new Board(n, n);
		for(int rand = 100; rand > 0; rand--) {
			b.get(rnd.nextInt(n), rnd.nextInt(n)).setWhite();
			b.get(rnd.nextInt(n), rnd.nextInt(n)).setBlack();
			b.get(rnd.nextInt(n), rnd.nextInt(n)).clear();
		}
		
		// invariant board
		Board binv = new Board(n, n);
		binv.copyFrom(b, b.getPreferredTransform());
		
		for(int t = 0; t < 8; t++) {
			Board bt = new Board(n, n);
			bt.copyFrom(b, t);
			Board bt1 = new Board(n, n);
			bt1.copyFrom(bt, bt.getPreferredTransform());
			if(!bt1.equals(binv))
				throw new RuntimeException("Failed invariance test");
		}
	}
}
