package roderigo.robot;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.BoardCellSet;

/**
 * Utility class for computing the difference (in terms of moves)
 * between two boards.
 * 
 * @author Federico Ferri
 *
 */
public class BoardDiff {
	/**
	 * Given two Boards, find a sequence of moves that turns the board a
	 * into board b
	 * 
	 * @param a
	 * @param b
	 * @return The sequence of moves that, applied to board a, makes it equal to board b,
	 * 		or <code>null</code> if it is not possible to do so.
	 */
	public static List<BoardCell> diff(Board a, Board b) throws BoardDiffException {
		List<BoardCell> movesToDo = unorderedDiff(a, b);
		
		if(movesToDo.isEmpty()) return movesToDo;
		
		// now try all possible permutations
		PermutationGenerator p = new PermutationGenerator(movesToDo.size());
		for(int perm[] : p) {
			// using all color combinations
			nextColorComb:
			for(int colorComb = 0 /* all black */; colorComb < (1 << perm.length); colorComb++) {
				List<BoardCell> moveSequence = new ArrayList<BoardCell>();
				Board b1 = a.clone();
				
				for(int i = 0; i < perm.length; i++) {
					BoardCellColor curCol = ((colorComb >> i) & 1) > 0
						? BoardCellColor.WHITE
						: BoardCellColor.BLACK;
					BoardCell move = movesToDo.get(perm[i]);
					if(!b1.makeMove(move, curCol)) {
						continue nextColorComb; /*nextPerm;*/
					} else {
						moveSequence.add(move);
					}
				}
				return moveSequence;
			}
		}
		
		throw new BoardDiffException("unable to figure out a move sequence; bug?");
	}

	/**
	 * Given two Boards, find a sequence of moves of the given color,
	 * that turns the board a into board b
	 * (faster version than previous method, to be used when one player is
	 *  directly in control of the Board) 
	 * 
	 * @param a
	 * @param b
	 * @param color The forced color
	 * @return The sequence of moves that, applied to board a, makes it equal to board b,
	 * 		or <code>null</code> if it is not possible to do so.
	 */
	public static List<BoardCell> diff(Board a, Board b, BoardCellColor color) throws BoardDiffException {
		List<BoardCell> movesToDo = unorderedDiff(a, b);
		
		if(movesToDo.isEmpty()) return movesToDo;
		
		// now try all possible permutations
		PermutationGenerator p = new PermutationGenerator(movesToDo.size());
		nextPerm: for(int perm[] : p) {
			List<BoardCell> moveSequence = new ArrayList<BoardCell>();
			Board b1 = a.clone();
			
			for(int i = 0; i < perm.length; i++) {
				BoardCell move = movesToDo.get(perm[i]);
				if(!b1.makeMove(move, color)) {
					continue nextPerm;
				} else {
					moveSequence.add(move);
				}
			}
			return moveSequence;
		}
		
		throw new BoardDiffException("unable to figure out a move sequence; bug?");
	}
	
	private static List<BoardCell> unorderedDiff(Board a, Board b) throws BoardDiffException {
		if(a == null)
			throw new BoardDiffException("board A is null");
		if(b == null)
			throw new BoardDiffException("board B is null");
		
		// find the difference in empty cells
		BoardCellSet emptyA = a.getAllCells().emptyCells();
		BoardCellSet emptyB = b.getAllCells().emptyCells();
		
		// if B has more empty cells than A, clearly it is not possible
		// to provide a result
		if(emptyB.size() > emptyA.size())
			throw new BoardDiffException("board B has more empty cells than board A");
		
		List<BoardCell> result = new ArrayList<BoardCell>();
		
		// if B and A have the same number of empty cells, must be already
		// in the same configuration
		if(emptyB.size() == emptyA.size()) {
			if(equal(a, b)) return result; // empty
			throw new BoardDiffException("board A and B have same number of empty cells, but are not equal");
		}
		
		// every non empty cell of A must have a corresponding non empty cell in B
		for(BoardCell c : a.getAllPieces())
			if(b.get(c.row, c.col).isClear())
				throw new BoardDiffException("board B has an empty cell which A has not");
		
		// for every non empty cell in B, those having a corresponding empty cell in A
		// are the moves to do
		List<BoardCell> movesToDo = new ArrayList<BoardCell>();
		for(BoardCell c : b.getAllPieces())
			if(a.get(c.row, c.col).isClear())
				movesToDo.add(c);
		
		return movesToDo;
	}
	
	/**
	 * Check if two boards are equal (same pieces, of same color in same position
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equal(Board a, Board b) {
		for(BoardCell c : a.getAllCells())
			if(b.get(c.row, c.col).getColor() != c.getColor())
				return false;
		return true;
	}
	
	public static class BoardDiffException extends Exception {
		private static final long serialVersionUID = 7154933856508210156L;

		public BoardDiffException(String reason) {
			super("Cannot compute board difference (" + (reason == null || reason.trim().equals("") ? "unknown reason" : reason) + ")");
		}
	}
	
	public static void main(String args[]) throws Exception {
		Board b = new Board(8, 8);
		BoardCellColor c = BoardCellColor.BLACK;
		
		c = makeNRandomMoves(b, c, 2 + (int) Math.random() * 5);
		Board b1 = b.clone();
		c = makeNRandomMoves(b1, c, 4);
		
		PrintWriter pw = new PrintWriter(System.out, true);
		b.print(pw);
		b1.print(pw);
		System.out.println("diff(b,b1) -> " + diff(b,b1));
	}
	
	private static BoardCell getRandomMove(Board b, BoardCellColor c) {
		while(true) {
			for(BoardCell m : b.getValidMoves(c)) {
				if(Math.random() > 0.6)
					return m;
			}
		}
	}
	
	private static BoardCellColor makeNRandomMoves(Board b, BoardCellColor c, int n) {
		while(n > 0) {
			n--;
			b.makeMove(getRandomMove(b, c), c);
			c = c.opposite();
		}
		return c;
	}
}
