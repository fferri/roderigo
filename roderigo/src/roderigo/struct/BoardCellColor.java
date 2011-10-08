package roderigo.struct;

import java.awt.Color;

/**
 * Represents the color of a piece on the <code>Board</code>.
 * Also used for indicating the turn, that is, the player that has
 * to move.
 * 
 * @author Federico Ferri
 *
 */
public class BoardCellColor {
	private BoardCellColor() {}
	
	public String toString() {
		if(this == BLACK) return "BLACK";
		if(this == WHITE) return "WHITE";
		return "???";
	}
	
	public Color awtColor() {
		if(this == BLACK) return Color.black;
		if(this == WHITE) return Color.white;
		return Color.gray;
	}
	
	public int ordinal() {
		if(this == BLACK) return 0;
		if(this == WHITE) return 1;
		return -1;
	}
	
	public BoardCellColor opposite() {
		return flip(this);
	}
	
	public static final BoardCellColor BLACK = new BoardCellColor();
	public static final BoardCellColor WHITE = new BoardCellColor();
	
	private static BoardCellColor flip(BoardCellColor c) {
		if(c == BLACK) return WHITE;
		if(c == WHITE) return BLACK;
		return c;
	}
	
	public static BoardCellColor fromInt(int i) {
		if(i == 1) return BLACK;
		if(i == 2) return WHITE;
		return null;
	}
	
	public static int toInt(BoardCellColor c) {
		if(c == BLACK) return 1;
		if(c == WHITE) return 2;
		return 0;
	}
}
