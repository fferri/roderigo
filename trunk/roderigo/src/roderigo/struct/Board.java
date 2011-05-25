package roderigo.struct;

import java.util.HashMap;
import java.util.Map;

public class Board {
	private BoardCell boardCell[][];
	
	// cells sets
	private BoardCellSet allCells;
	private Map<BoardCell.Type, BoardCellSet> cellsByType;
	
	static class BoardManager {
		private Board board;
		
		private BoardManager(Board b) {
			board = b;
		}
		
		public Board getBoard() {
			return board;
		}
	}
	
	public Board(int rows, int cols) {
		boardCell = new BoardCell[rows][cols];
		
		allCells = new BoardCellSet(rows * cols);
		cellsByType = new HashMap<BoardCell.Type, BoardCellSet>();
		for(BoardCell.Type t : BoardCell.Type.values()) cellsByType.put(t, new BoardCellSet());
		
		for(int row = 0; row < getNumRows(); row++) {
			for(int col = 0; col < getNumColumns(); col++) {
				boardCell[row][col] = new BoardCell(new BoardManager(this), row, col);
				allCells.add(boardCell[row][col]);
				cellsByType.get(boardCell[row][col].getType()).add(boardCell[row][col]);
			}
		}
		
		reset();
	}
	
	public int getNumRows() {
		return boardCell.length;
	}
	
	public int getNumColumns() {
		return boardCell[0].length;
	}
	
	public boolean boundCheck(int row, int col) {
		return (row >= 0 && row < getNumRows() && col >= 0 && col < getNumColumns());
	}
	
	public BoardCell get(int row, int col) {
		return boundCheck(row, col) ? boardCell[row][col] : null;
	}
	
	public BoardCellSet getAllCells() {
		return allCells;
	}
	
	public BoardCellSet getCellsOfType(BoardCell.Type type) {
		return cellsByType.get(type);
	}
	
	public BoardCellSet getAllPieces() {
		BoardCellSet result = new BoardCellSet();
		
		for(BoardCell cell : allCells)
			if(!cell.isClear())
				result.add(cell);
		
		return result;
	}
	
	public void copyFrom(Board b) {
		if(b.getNumRows() != getNumRows() || b.getNumColumns() != getNumColumns())
			throw new RuntimeException("Board size mismatch trying to do a copy");
		
		for(int row = 0; row < getNumRows(); row++) {
			for(int col = 0; col < getNumColumns(); col++) {
				boardCell[row][col].copyFrom(b.boardCell[row][col]);
			}
		}
	}
	
	public Board clone() {
		Board b = new Board(getNumRows(), getNumColumns());
		
		b.copyFrom(this);
		
		return b;
	}
	
	public void clearAll() {
		for(BoardCell cell : allCells) {
			cell.clear();
		}
	}
	
	public void reset() {
		clearAll();
		
		boardCell[3][3].setWhite();
		boardCell[4][4].setWhite();
		boardCell[3][4].setBlack();
		boardCell[4][3].setBlack();
	}
	
	public BoardCellSet between(BoardCell a, BoardCell b) {
		BoardCellSet result = new BoardCellSet();
		
		int dr = (b.row - a.row);
		int dc = (b.col - a.col);
		
		if(!(dr == 0 || dc == 0) && !(Math.abs(dr) == Math.abs(dc)))
			return null; // two pieces not in position able to enclose any pieces
		
		if(Math.abs(dr) == 1 || Math.abs(dc) == 1 || a == b)
			return result;
		
		// normalize direction
		dr = (dr == 0) ? 0 : dr / Math.abs(dr);
		dc = (dc == 0) ? 0 : dc / Math.abs(dc);

		int row = a.row + dr;
		int col = a.col + dc;
		
		while(true) {
			result.add(boardCell[row][col]);
			row += dr;
			col += dc;
			if(b.row == row && b.col == col) break;
		}
		
		return result;
	}
	
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
	
	public int getMobility(BoardCellColor color) {
		return getValidMoves(color).size();
	}

	public BoardCellSet getFringe() {
		BoardCellSet result = new BoardCellSet();
		
		getFringe(get(4, 4), new BoardCellSet(), result);
		
		return result;
	}
	
	private void getFringe(BoardCell cell, BoardCellSet visited, BoardCellSet result) {
		visited.add(cell);
		
		BoardCellSet a = cell.adjacentCells();
		
		for(BoardCell a0 : a) {
			if(a0.isClear()) {
				result.add(a0);
			} else {
				if(!visited.contains(a0)) {
					getFringe(a0, visited, result);
				}					
			}
		}
	}
	
	public BoardCellSet getBorder() {
		BoardCellSet result = new BoardCellSet();
		
		getBorder(get(4, 4), new BoardCellSet(), result);
		
		return result;
	}
	
	private void getBorder(BoardCell cell, BoardCellSet visited, BoardCellSet result) {
		if(cell.isClear()) return;
		visited.add(cell);
		
		BoardCellSet a = cell.adjacentCells();
		
		if(containsAnEmptyCell(a)) {
			result.add(cell);
		}
		
		for(BoardCell a0 : a) {
			if(!visited.contains(a0)) {
				getBorder(a0, visited, result);
			}					
		}
	}
	
	private boolean containsAnEmptyCell(BoardCellSet s) {
		for(BoardCell cell : s) {
			if(cell.isClear())
				return true;
		}
		return false;
	}

	public boolean isValidMove(BoardCell cell, BoardCellColor color) {
		if(!cell.isClear())
			return false;
		
		for(Direction dir : Direction.allDirections) {
			BoardCell cell2 = findEnclosingPiece(cell, dir, color);
			if(cell2 != null) return true;
		}
		
		return false;
	}
	
	public BoardCellSet getValidMoves(BoardCellColor color) {
		BoardCellSet r = new BoardCellSet();
		
		BoardCellSet fringe = getFringe();
		for(BoardCell cell : fringe)
			if(isValidMove(cell, color))
				r.add(cell);
		
		return r;
	}

	public boolean makeMove(BoardCell cell, BoardCellColor color) {
		if(!cell.isClear()) return false;
		
		BoardCellSet enclosingPieces = new BoardCellSet();
		
		for(Direction dir : Direction.allDirections) {
			BoardCell e = findEnclosingPiece(cell, dir, color);
			if(e != null) enclosingPieces.add(e);
		}
		
		if(enclosingPieces.isEmpty()) return false;

		for(BoardCell cell1 : enclosingPieces) {
			between(cell, cell1).flipColor();			
		}
		
		cell.setColor(color);
		
		return true;
	}
}
