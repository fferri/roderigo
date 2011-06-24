package roderigo.robot;

import java.awt.Rectangle;

/**
 * Robot implementation, for a famous online gaming site
 * 
 * @author Federico Ferri
 *
 */
public class RobotType1 extends AbstractRobot {
	public RobotType1() throws Exception {
	}

	@Override
	public void findBoard(int startX, int startY) {
		// assuming we were dropped at top left corner of border
		// otherwise return null
		boardRect = null;
		
		final int step = 50;
		int x = startX, y = startY;
		Rectangle r = new Rectangle();
		if(!identify(x,y).equals(Part.BORDER)) return;

		while(identify(x,y).equals(Part.BORDER)) x--;
		r.x = ++x;

		while(identify(x,y).equals(Part.BORDER)) y--;
		r.y = ++y;

		while(identify(x,y).equals(Part.BORDER)) x+=step;
		x-=step;
		while(identify(x,y).equals(Part.BORDER)) x++;
		r.width = --x - r.x;

		while(identify(x,y).equals(Part.BORDER)) y+=step;
		y-=step;
		while(identify(x,y).equals(Part.BORDER)) y++;
		r.height = --y - r.y;

		// find border size and real rect size:
		x = r.x + r.width*7/16;
		y = r.y;
		int borderSize = 0;
		while(identify(x,y).equals(Part.BORDER)) {
			y++; borderSize++;
		}

		boardRect = new Rectangle();
		boardRect.x = r.x + borderSize;
		boardRect.y = r.y + borderSize;
		boardRect.width = r.width - 2 * borderSize;
		boardRect.height = r.height - 2 * borderSize;

		// find cell size:
		cellSize = boardRect.width / 8;
	}

	@Override
	public Part identify(int colorRGB) {
		colorRGB = colorRGB & 0xFFFFFF;
		switch(colorRGB) {
			case 0x587A4D: return Part.BORDER;
			case 0x7FAF6F: return Part.BACKGROUND;
			case 0x000000: return Part.BLACK;
			case 0xFFFFFF: return Part.WHITE;
			case 0xFF0000: return Part.LAST;
			default:       return Part.UNKNOWN;
		}
	}
}
