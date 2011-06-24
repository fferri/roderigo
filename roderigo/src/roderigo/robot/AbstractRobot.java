package roderigo.robot;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;

import roderigo.struct.Board;

/**
 * Acquire a board from screen
 * 
 * @author Federico Ferri
 *
 */
public abstract class AbstractRobot {
	protected Robot robot; // java.awt.Robot
	protected Rectangle boardRect; // board pos or null
	protected int cellSize; // square cell assumption
	
	public static enum Part {
		BORDER, BACKGROUND, BLACK, WHITE, LAST,
		UNKNOWN
	}

	public AbstractRobot() throws Exception {
		robot = new Robot();
		boardRect = null;
		cellSize = 0;
	}

	/**
	 * Assuming each color uniquely identifies a Part of the board
	 * @param colorRGB
	 * @return Identified Part of the board
	 */
	abstract public Part identify(int colorRGB);

	public Part identify(int screenX, int screenY) {
		int rgb = robot.getPixelColor(screenX, screenY).getRGB() & 0xFFFFFF;
		return identify(rgb);
	}

	abstract public void findBoard(int startX, int startY);

	public Board readBoard() throws BoardReadException {
		if(boardRect == null || boardRect.width < 20 || boardRect.width != boardRect.height) return null;

		Part p;
		Board board = new Board(8, 8);

		for(int row = 0; row < 8; row++) {
			for(int col = 0; col < 8; col++) {
				p = identify(boardRect.x + col*cellSize + cellSize/2, boardRect.y + row*cellSize + cellSize/2);
				if(p.equals(Part.LAST))
					p = identify(boardRect.x + col*cellSize + cellSize/2 + cellSize/4, boardRect.y + row*cellSize + cellSize/2);

				switch(p) {
					case BLACK: board.get(row, col).setBlack(); break;
					case WHITE: board.get(row, col).setWhite(); break;
					case BACKGROUND: board.get(row, col).clear(); break;
					default:
						throw new BoardReadException("unrecognized part: " + p);
				}
			}
		}
		
		return board;
	}
	
	public static class BoardReadException extends Exception {
		private static final long serialVersionUID = -3133012496528511932L;

		public BoardReadException(String reason) {
			super("Cannot read board (" + (reason == null || reason.trim().equals("") ? "unknown reason" : reason) + ")");
		}
	}

	public void focus() {
		if(boardRect == null || cellSize < 1) return;
		clickXY(boardRect.x - 5, boardRect.y - 6);
	}

	public void click(int row, int col) {
		if(boardRect == null || cellSize < 1) return;
		clickXY(boardRect.x + col*cellSize + cellSize/2, boardRect.y + row*cellSize + cellSize/2);
	}

	public void clickXY(int x, int y) {
		robot.mouseMove(x, y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}

	public Rectangle getBoardRect() {
		return boardRect;
	}
}
