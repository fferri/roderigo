package roderigo.struct;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of an othello board.
 * This is responsible also of the game rules/logic, like enumerating valid moves
 * or counting pieces.
 * 
 * @author Federico Ferri
 *
 */
public class Board {
	private BoardCell boardCell[][];
	
	// static cells sets (created first time they are needed)
	private BoardCellSet allCells = null;
	private Map<BoardCell.Type, BoardCellSet> cellsByType = null;
	
	// cached [dynamic] cell sets
	private BoardCellSet allPieces = null;
	private BoardCellSet border = null;
	private BoardCellSet fringe = null;
	private BoardCellSet validMovesW = null;
	private BoardCellSet validMovesB = null;
	
	static class BoardManager {
		private Board board;
		
		private BoardManager(Board b) {
			board = b;
		}
		
		public Board getBoard() {
			return board;
		}
	}
	
	private final BoardManager manager = new BoardManager(this);
	
	public Board(int rows, int cols) {
		boardCell = new BoardCell[rows][cols];
		
		for(int row = 0; row < getNumRows(); row++) {
			for(int col = 0; col < getNumColumns(); col++) {
				boardCell[row][col] = new BoardCell(manager, row, col);
			}
		}
		
		//reset();
		boardCell[3][3].setWhite();
		boardCell[4][4].setWhite();
		boardCell[3][4].setBlack();
		boardCell[4][3].setBlack();
	}
	
	/**
	 * @return The number of columns
	 */
	public int getNumRows() {
		return boardCell.length;
	}
	
	/**
	 * @return The number of rows
	 */
	public int getNumColumns() {
		return boardCell[0].length;
	}
	
	/**
	 * Perform a bound check
	 * 
	 * @param row
	 * @param col
	 * @return <code>true</code> if row, col is inside bounds,
	 *         that is: 0 <= row < rows and 0 <= col < cols
	 */
	public boolean boundCheck(int row, int col) {
		return (row >= 0 && row < getNumRows() && col >= 0 && col < getNumColumns());
	}
	
	/**
	 * Method to access a specific cell
	 * 
	 * @param row
	 * @param col
	 * @return The cell at row, col; or null if the position is out of bounds
	 */
	public BoardCell get(int row, int col) {
		return boundCheck(row, col) ? boardCell[row][col] : null;
	}
	
	/**
	 * Used when dealing with a foreign cell; it retrieves the corresponding cell
	 * in *this* board.
	 * 
	 * @param cell The foreign cell
	 * @return The corresponding cell in this board
	 */
	public BoardCell conformCell(BoardCell cell) {
		return get(cell.row, cell.col);
	}
	
	/**
	 * Return the set of all cells
	 * 
	 * @return
	 */
	public BoardCellSet getAllCells() {
		if(allCells == null) {
			allCells = new BoardCellSet(manager, getNumRows() * getNumColumns());
			
			cellsByType = new HashMap<BoardCell.Type, BoardCellSet>();
			for(BoardCell.Type t : BoardCell.Type.values()) cellsByType.put(t, new BoardCellSet(manager));
			
			for(int row = 0; row < getNumRows(); row++) {
				for(int col = 0; col < getNumColumns(); col++) {
					allCells.add(manager, boardCell[row][col]);
					cellsByType.get(boardCell[row][col].getType()).add(manager, boardCell[row][col]);
				}
			}
		}
		
		return allCells;
	}
	
	/**
	 * Return the sets of cells having the given type
	 * 
	 * @param type
	 * @return The above mentioned set
	 */
	public BoardCellSet getCellsOfType(BoardCell.Type type) {
		if(cellsByType == null) {
			getAllCells();
		}
		
		return cellsByType.get(type);
	}
	
	/**
	 * Return the set of all cells not empty (i.e. occupied by a piece)
	 * 
	 * @return The above mentioned set
	 */
	public BoardCellSet getAllPieces() {
		if(allPieces == null) {
			BoardCellSet result = new BoardCellSet(manager);
			
			for(BoardCell cell : getAllCells())
				if(!cell.isClear())
					result.add(manager, cell);
			
			allPieces = result;
		}
		
		return allPieces;
	}
	
	/**
	 * Copy board content from b
	 * 
	 * @param b
	 */
	public void copyFrom(Board b) {
		if(b.getNumRows() != getNumRows() || b.getNumColumns() != getNumColumns())
			throw new RuntimeException("Board size mismatch trying to do a copy");
		
		for(int row = 0; row < getNumRows(); row++) {
			for(int col = 0; col < getNumColumns(); col++) {
				boardCell[row][col].copyFrom(b.boardCell[row][col]);
			}
		}
		
		// maintain static and cached sets:
		allCells = b.allCells;
		cellsByType = b.cellsByType;
		allPieces = b.allPieces;
		border = b.border;
		fringe = b.fringe;
		validMovesW = b.validMovesW;
		validMovesB = b.validMovesB;
	}
	
	/**
	 * Clone the board
	 */
	public Board clone() {
		Board b = new Board(getNumRows(), getNumColumns());
		
		b.copyFrom(this);
		
		return b;
	}
	
	/**
	 * Clear the board (all cells empty)
	 */
	public void clearAll() {
		for(BoardCell cell : getAllCells()) {
			cell.clear();
		}
	}
	
	/**
	 * Reset the board to the initial game position (2 blacks and 2 whites)
	 */
	public void reset() {
		clearAll();
		
		boardCell[3][3].setWhite();
		boardCell[4][4].setWhite();
		boardCell[3][4].setBlack();
		boardCell[4][3].setBlack();
	}
	
	/**
	 * Return the set of cells enclosed between a and b, either
	 * vertically, horizontally, or diagonally
	 * 
	 * @param a
	 * @param b
	 * @return The above mentioned set
	 */
	public BoardCellSet between(BoardCell a, BoardCell b) {
		BoardCellSet result = new BoardCellSet(manager);
		
		int dr = (b.row - a.row);
		int dc = (b.col - a.col);
		
		// two pieces not in position able to enclose any pieces
		if(!(dr == 0 || dc == 0) && !(Math.abs(dr) == Math.abs(dc)))
			return null;
		
		// adjacent or overlapping cells -> return empty
		if(Math.abs(dr) == 1 || Math.abs(dc) == 1 || a == b)
			return result;
		
		// normalize direction
		dr = (dr == 0) ? 0 : dr / Math.abs(dr);
		dc = (dc == 0) ? 0 : dc / Math.abs(dc);

		int row = a.row + dr;
		int col = a.col + dc;
		
		while(true) {
			result.add(manager, boardCell[row][col]);
			row += dr;
			col += dc;
			if(b.row == row && b.col == col) break;
		}
		
		return result;
	}
	
	/**
	 * Starting from cell, and always proceeding in the given direction, try to find
	 * one or more pieces of opposite color, enclosed by a piece of given <code>color</code>, and
	 * return the enclosing piece.
	 * 
	 * <code>cell</code> is usually empty. If not, it overrides <code>color</code>.
	 * 
	 * @param cell The cell to start the search from
	 * @param dir The direction of the search
	 * @param color Color of the enclosing piece
	 * @return The enclosing piece
	 */
	private BoardCell findEnclosingPiece(BoardCell cell, Direction dir, BoardCellColor color) {
		if(!cell.isClear())
			color = cell.getColor();
		
		BoardCellColor color1;
		boolean foundOpp = false;
		boolean foundMine = false;
		
		cell = cell.adjacentCell(dir);
		while(cell != null && !cell.isClear()) {
			color1 = cell.getColor();
			
			if(color1 == color) {
				if(foundOpp) foundMine = true;
				break;
			} else {
				if(!foundMine) foundOpp = true;
			}
			
			cell = cell.adjacentCell(dir);
		}
		
		if(foundOpp && foundMine) return cell;
		else return null;
	}

	/**
	 * Fringe is the contour of empty cells surrounding
	 * the occupied cells.
	 * 
	 * It is used internally for enumerating valid moves,
	 * since valid moves must necessarily be in the fringe.
	 * 
	 * @return The set of fringe cells
	 */
	public BoardCellSet getFringe() {
		if(fringe == null) {
			BoardCellSet result = new BoardCellSet(manager);
			
			getFringe(get(4, 4), new BoardCellSet(manager), result);
			
			fringe = result;
		}
		
		return fringe;
	}
	
	private void getFringe(BoardCell cell, BoardCellSet visited, BoardCellSet result) {
		visited.add(manager, cell);
		
		BoardCellSet a = cell.adjacentCells();
		
		for(BoardCell a0 : a) {
			if(a0.isClear()) {
				result.add(manager, a0);
			} else {
				if(!visited.contains(a0)) {
					getFringe(a0, visited, result);
				}					
			}
		}
	}
	
	/**
	 * The border is the set of cells adjacent
	 * to the empty cells.
	 * 
	 * @return The set of border cells
	 */
	public BoardCellSet getBorder() {
		if(border == null) {
			BoardCellSet result = new BoardCellSet(manager);
			
			getBorder(get(4, 4), new BoardCellSet(manager), result);
			
			border = result;
		}
		
		return border;
	}
	
	private void getBorder(BoardCell cell, BoardCellSet visited, BoardCellSet result) {
		if(cell.isClear()) return;
		visited.add(manager, cell);
		
		BoardCellSet a = cell.adjacentCells();
		
		if(a.containsAnEmptyCell()) {
			result.add(manager, cell);
		}
		
		for(BoardCell a0 : a) {
			if(!visited.contains(a0)) {
				getBorder(a0, visited, result);
			}					
		}
	}
	
	/**
	 * Tells if a move is valid for a given player
	 * (identified by <code>color</code>)
	 * 
	 * @param cell
	 * @param color
	 * @return
	 */
	public boolean isValidMove(BoardCell cell, BoardCellColor color) {
		if(!cell.isClear())
			return false;
		
		for(Direction dir : Direction.allDirections) {
			BoardCell cell2 = findEnclosingPiece(cell, dir, color);
			if(cell2 != null) return true;
		}
		
		return false;
	}
	
	/**
	 * Return the set of valid moves for a given player
	 * (identified by <code>color</code>)
	 * 
	 * @param color
	 * @return
	 */
	public BoardCellSet getValidMoves(BoardCellColor color) {
		if(color == BoardCellColor.WHITE && validMovesW == null) {
			validMovesW = getValidMoves_noCache(color);
		}
		
		if(color == BoardCellColor.BLACK && validMovesB == null) {
			validMovesB = getValidMoves_noCache(color);
		}
		
		if(color == BoardCellColor.WHITE)
			return validMovesW;
		
		if(color == BoardCellColor.BLACK)
			return validMovesB;
		
		return null;
	}
	
	private BoardCellSet getValidMoves_noCache(BoardCellColor color) {
		BoardCellSet result = new BoardCellSet(manager);
		
		BoardCellSet fringe = getFringe();
		for(BoardCell cell : fringe)
			if(isValidMove(cell, color))
				result.add(manager, cell);
		
		return result;
	}

	/**
	 * Perform a move for the given player
	 * (identified by <code>color</code>)
	 * 
	 * @param cell
	 * @param color
	 * @return <code>true</code> if the move was valid,
	 *         <code>false</code> otherwise
	 */
	public boolean makeMove(BoardCell cell, BoardCellColor color) {
		cell = conformCell(cell);
		
		if(!cell.isClear()) return false;
		
		BoardCellSet enclosingPieces = new BoardCellSet(manager);
		
		for(Direction dir : Direction.allDirections) {
			BoardCell e = findEnclosingPiece(cell, dir, color);
			if(e != null) enclosingPieces.add(manager, e);
		}
		
		if(enclosingPieces.isEmpty()) return false;

		for(BoardCell cell1 : enclosingPieces) {
			between(cell, cell1).flipColor();			
		}
		
		cell.setColor(color);
		
		return true;
	}
	
	/**
	 * Print the board.
	 * 
	 * @param pw The <code>PrintWriter</code> used for printing the board
	 */
	public void print(PrintWriter pw) {
		print(pw, true);
	}
	
	public void print(PrintWriter pw, boolean wide) {
		// header:
		pw.print("   ");
		String hdr[] = {"a", "b", "c", "d", "e", "f", "g", "h"};
		for(String h : hdr) pw.print(h + (wide ? " " : ""));
		pw.println("");
		
		// rows:
		for(int row = 0; row < getNumRows(); row++) {
			pw.print("" + (row+1) + ": ");
			for(int col = 0; col < getNumColumns(); col++) {
				BoardCell cell = get(row, col);
				if(cell.isClear())
					pw.print(".");
				else if(cell.isBlack())
					pw.print("*");
				else if(cell.isWhite())
					pw.print("o");
				if(wide)
					pw.print(" ");
			}
			pw.println("");
		}
		pw.flush();
	}
	
	/**
	 * Invalidate the cache
	 */
	void invalidateCache() {
		allPieces = null;
		border = null;
		fringe = null;
		validMovesW = null;
		validMovesB = null;
	}
}
