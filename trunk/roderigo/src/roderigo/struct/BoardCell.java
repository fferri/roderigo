package roderigo.struct;

public class BoardCell {
	private final transient Board board;
	public final int row, col;
	
	private BoardCellColor color;
	
	public static enum Type { NONE, A, B, C, X, CORNER };
	
	public boolean visitedFlag = false;
	public boolean stableFlag = false;
	
	public BoardCell(Board.BoardManager m, int r, int c) {
		board = m.getBoard();
		row = r;
		col = c;
		color = null; // null means cell empty
	}
	
	public static String getRowString(int row) {
		return String.format("%d", (row + 1));
	}
	
	public static String getColumnString(int col) {
		return String.format("%c", (col + 'a'));
	}
	
	public String toString() {
		String rowcol = getColumnString(col) + getRowString(row);
		
		if(color == null)
			return rowcol;
		else
			return rowcol + ":" + color.toString().substring(0, 1);
	}
	
	public void copyFrom(BoardCell c) {
		color = c.color;
	}
	
	public void clear() {
		color = null;
	}
	
	public void setWhite() {
		color = BoardCellColor.WHITE;
	}
	
	public void setBlack() {
		color = BoardCellColor.BLACK;
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

	public Type getType() {
		int rows = board.getNumRows();
		int cols = board.getNumColumns();
		
		if((row == 0 && col == 0)
			|| (row == 0 && col == cols - 1)
			|| (row == rows - 1 && col == 0)
			|| (row == rows - 1 && col == cols - 1))
			return Type.CORNER;
		
		int r1 = rows - 1, c1 = cols - 1;
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
		return Type.NONE; // unreachable
	}
	
	public void flipColor() {
		color = color.opposite();
	}
	
	public BoardCell adjacentCell(Direction d) {
		return board.get(row + d.getDr(), col + d.getDc());
	}
	
	public BoardCellSet adjacentCells() {
		BoardCellSet result = new BoardCellSet(8);
		
		for(Direction d : Direction.allDirections) {
			BoardCell cell = adjacentCell(d);
			if(cell != null)
				result.add(cell);
		}
		
		return result;
	}
	
	public boolean isValidMove(BoardCellColor color) {
		return board.isValidMove(this, color);
	}
	
	// ------------------------------------------------------------------------
	
	// low level api:
	
	public void setColor(BoardCellColor c) {
		color = c;
	}
	
	public BoardCellColor getColor() {
		return color;
	}
}
