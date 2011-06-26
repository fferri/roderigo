package roderigo.struct;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A <code>Set</code> of <code>BoardCell</code> elements with some additional utility
 * functions.
 * 
 * NOTE: cell sets should be immutable, except for Board which produces them;
 *       only Board can create a new BoardCellSet, or BoardCellSet itself can.
 *       
 * @author Federico Ferri
 *
 */
public class BoardCellSet implements Iterable<BoardCell> {
	private static final long serialVersionUID = -3949663115732496052L;
	
	private final HashSet<BoardCell> cellSet;
	
	private final Board.BoardManager manager; // to allow create other BoardCellSets
	
	public BoardCellSet(Board.BoardManager m) {
		assert m != null;
		manager = m;
		cellSet = new HashSet<BoardCell>();
	}
	
	public BoardCellSet(Board.BoardManager m, int initialCapacity) {
		assert m != null;
		manager = m;
		cellSet = new HashSet<BoardCell>(initialCapacity);
	}
	
	public void add(Board.BoardManager m, BoardCell cell) {
		assert m == manager;
		cellSet.add(cell);
	}
	
	public int size() {
		return cellSet.size();
	}
	
	public boolean isEmpty() {
		return cellSet.isEmpty();
	}
	
	public Iterator<BoardCell> iterator() {
		return Collections.unmodifiableSet(cellSet).iterator();
	}
	
	public boolean contains(BoardCell cell) {
		return cellSet.contains(cell);
	}
	
	// subsets
	
	public BoardCellSet piecesOfColor(BoardCellColor color) {
		BoardCellSet result = new BoardCellSet(manager, size());
		
		for(BoardCell cell : this)
			if(cell.getColor() == color)
				result.add(manager, cell);
		
		return result;
	}
	
	public BoardCellSet emptyCells() {
		return piecesOfColor(null);
	}
	
	public BoardCellSet whitePieces() {
		return piecesOfColor(BoardCellColor.WHITE);
	}
	
	public BoardCellSet blackPieces() {
		return piecesOfColor(BoardCellColor.BLACK);
	}
	
	public int numPieces() {
		return size() - emptyCells().size();
	}
	
	public void flipColor() {
		for(BoardCell cell : this)
			cell.flipColor();
	}
	
	public boolean containsAnEmptyCell() {
		for(BoardCell cell : this) {
			if(cell.isClear())
				return true;
		}
		return false;
	}
	
	public static BoardCellSet union(BoardCellSet a, BoardCellSet b) {
		assert a.manager == b.manager;
		BoardCellSet c = new BoardCellSet(a.manager, a.size() + b.size());
		for(BoardCell cell : a) c.add(a.manager, cell);
		for(BoardCell cell : b) c.add(b.manager, cell);
		return c;
	}
}
