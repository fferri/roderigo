package roderigo.struct;

import java.util.HashSet;

/**
 * A <code>Set</code> of <code>BoardCell</code> elements with some additional utility
 * functions.
 * 
 * @author Federico Ferri
 *
 */
public class BoardCellSet extends HashSet<BoardCell> {
	private static final long serialVersionUID = -3949663115732496052L;

	public BoardCellSet() {
	}
	
	public BoardCellSet(int initialCapacity) {
		super(initialCapacity);
	}
	
	// subsets
	
	public BoardCellSet piecesOfColor(BoardCellColor color) {
		BoardCellSet result = new BoardCellSet(size());
		
		for(BoardCell cell : this)
			if(cell.getColor() == color)
				result.add(cell);
		
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
}
