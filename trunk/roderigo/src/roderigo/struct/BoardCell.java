package roderigo.struct;

/**
 * A cell on the board.
 * Can be only created by <code>Board</code>.
 * 
 * Each cell is unique and belong to its board.
 * 
 * There will be no two cells with same row, column and board,
 * thus cells can be compared (for equality) with ==
 * 
 * @author Federico Ferri
 *
 */
public class BoardCell {
	private final transient Board board;
	private final transient Board.BoardManager manager;
	
	public final int row, col;
	
	private BoardCellColor color;

	public boolean visitedFlag;
	private boolean stableFlag;
	
	public static enum Type { NONE, A, B, C, X, CORNER };
	
	/**
	 * A cell can only be created by its board.
	 * You cannot create a cell directly.
	 * 
	 * @param m
	 * @param r
	 * @param c
	 */
	public BoardCell(Board.BoardManager m, int r, int c) {
		assert m != null;
		manager = m;
		board = m.getBoard();
		row = r;
		col = c;
		color = null; // null means cell empty
		visitedFlag = false;
		stableFlag = false;
	}
	
	public static String getRowString(int row) {
		return String.format("%d", (row + 1));
	}
	
	public static String getColumnString(int col) {
		return String.format("%c", (col + 'a'));
	}
	
	public static String getRowColumnString(int row, int col) {
		return getColumnString(col) + getRowString(row);
	}
	
	public String toString() {
		return getRowColumnString(row, col) + (color != null ? (":" + color.toString().substring(0, 1)) : "");
	}
	
	public static BoardCell fromString(Board board, String rowcolString) {
		char c = rowcolString.charAt(0);
		if(!(c >= 'a' && c <= 'z')) return null;
		int row = Integer.parseInt(rowcolString.substring(1)) - 1;
		int col = c - 'a';
		return board.get(row, col);
	}
	
	public void copyFrom(BoardCell c) {
		color = c.color;
		
		board.invalidateCache();
	}
	
	public void clear() {
		color = null;
		
		board.invalidateCache();
	}
	
	public void setWhite() {
		color = BoardCellColor.WHITE;
		
		board.invalidateCache();
	}
	
	public void setBlack() {
		color = BoardCellColor.BLACK;
		
		board.invalidateCache();
	}
	
	public boolean isClear() {
		return color == null;
	}
	
	public boolean isBlack() {
		return color == BoardCellColor.BLACK;
	}
	
	public boolean isWhite() {
		return color == BoardCellColor.WHITE;
	}
	
	public boolean sameColor(BoardCell c) {
		return color == c.color;
	}

	public boolean isCorner() {
		int rows = board.getNumRows();
		int cols = board.getNumColumns();
		
		return (row == 0 && col == 0)
			|| (row == 0 && col == cols - 1)
			|| (row == rows - 1 && col == 0)
			|| (row == rows - 1 && col == cols - 1);
	}
	
	public boolean isSide() {
		return (row == 0)
			|| (row == board.getNumRows() - 1)
			|| (col == 0)
			|| (col == board.getNumColumns() - 1);
	}
	
	public boolean isSideTop() {
		return row == 0;
	}
	
	public boolean isSideBottom() {
		return row == board.getNumRows() - 1;
	}
	
	public boolean isSideLeft() {
		return col == 0;
	}
	
	public boolean isSideRight() {
		return col == board.getNumColumns() - 1;
	}
	
	public Type getType() {
		if(isCorner()) return Type.CORNER;

		int r1 = board.getNumRows() - 1, c1 = board.getNumColumns() - 1;
		if((row == 1 || row == r1 - 1) && (col == 1 || col == c1 - 1))
			return Type.X;
		if(row > 0 && row < r1 && col > 0 && col < c1)
			return Type.NONE;
		if(row == 1 || row == r1 - 1 || col == 1 || col == c1 - 1)
			return Type.C;
		if(row == 2 || row == r1 - 2 || col == 2 || col == c1 - 2)
			return Type.A;
		if(row == 3 || row == r1 - 3 || col == 3 || col == c1 - 3)
			return Type.B;
		
		System.out.println("WTF!? BoardCell.java, getType()");
		return Type.NONE; // should be unreachable
	}
	
	public void flipColor() {
		color = color.opposite();
		
		board.invalidateCache();
	}
	
	public BoardCell adjacentCell(Direction d) {
		return board.get(row + d.getDr(), col + d.getDc());
	}
	
	public BoardCellSet adjacentCells() {
		BoardCellSet result = new BoardCellSet(manager, 8);
		
		for(Direction d : Direction.allDirections) {
			BoardCell cell = adjacentCell(d);
			if(cell != null)
				result.add(manager, cell);
		}
		
		return result;
	}
	
	public boolean isValidMove(BoardCellColor color) {
		return board.isValidMove(this, color);
	}
	
	public boolean isStable() {
		if(isClear()) return false;
		
		if(stableFlag) {
			// once a piece is stable, its stable state does not change anymore
			// (unless board is reset)
			return true;
		}
		
		// corner pieces are always stable
		if(isCorner()) {
			stableFlag = true;
			return true;
		}
		
		// sides have a simpler check
		
		BoardCell h = adjacentCell(Direction.W);
		BoardCell d = adjacentCell(Direction.E);
		
		if(isSideTop() || isSideBottom()) {
			if((h.sameColor(this) && h.isStable()) || (d.sameColor(this) && d.isStable())) {
				stableFlag = true;
				return true;
			} else {
				return false;
			}
		}
		
		BoardCell b = adjacentCell(Direction.N);
		BoardCell f = adjacentCell(Direction.S);
		
		if(isSideLeft() || isSideRight()) {
			if((b.sameColor(this) && b.isStable()) || (f.sameColor(this) && f.isStable())) {
				return true;
			} else {
				return false;
			}
		}
		
		/*
		 * A B C
		 * H ? D
		 * G F E
		 * 
		 * 8 cases possible:
		 * ABCH, ABHG, ABCD, BCDE, CDEF, DEFG, AFGH, EFGH
		 * (each letter indicating piece is same color and is stable)
		 */
		
		BoardCell a = adjacentCell(Direction.NW);
		BoardCell c = adjacentCell(Direction.NE);
		BoardCell g = adjacentCell(Direction.SW);
		BoardCell e = adjacentCell(Direction.SE);
		
		boolean A = a.sameColor(this) && a.isStable(),
			B = b.sameColor(this) && b.isStable(),
			C = c.sameColor(this) && c.isStable(),
			D = d.sameColor(this) && d.isStable(),
			E = e.sameColor(this) && e.isStable(),
			F = f.sameColor(this) && f.isStable(),
			G = g.sameColor(this) && g.isStable(),
			H = h.sameColor(this) && h.isStable();
		
		if(
				(A && B && H && (C || G)) ||
				(B && C && D && (A || E)) ||
				(D && E && F && (C || G)) ||
				(F && G && H && (A || E))
		) {
			stableFlag = true;
			return true;
		} else {
			return false;
		}
	}
	
	// ------------------------------------------------------------------------
	
	// low level api:
	
	public void setColor(BoardCellColor c) {
		color = c;
		
		board.invalidateCache();
	}
	
	public BoardCellColor getColor() {
		return color;
	}
	
	void setFromInt(int i) {
		setColor(BoardCellColor.fromInt(i));
	}
	
	int getInt() {
		return BoardCellColor.toInt(getColor());
	}
}
