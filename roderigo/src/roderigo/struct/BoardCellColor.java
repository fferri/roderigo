package roderigo.struct;

import java.awt.Color;

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
}
