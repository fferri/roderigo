package roderigo.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import roderigo.Controller;
import roderigo.struct.BoardCell;

/**
 * GUI representation of <code>Board</code>
 * 
 * @author Federico Ferri
 *
 */
public class JBoard extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 5041436229495994615L;
	
	private Controller controller;
	
	private JBoardColorScheme colors = new JBoardColorScheme();
	
	private BoardCell hoverCell = null;
	private BoardCell clickedCell = null;
	private BoardCell lastMove = null;
	private BoardCell bestMove = null;
	
	// metrics used by paint and mouse operations:
	public class Metrics {
		public int width = 0, height = 0, side = 0, cell = 0;
	}
	
	private Boolean locked = false;
	
	public JBoard(Controller controller) {
		this.controller = controller;

		setPreferredSize(new Dimension(512, 512));
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void lock() {
		synchronized(locked) {
			locked = true;
			
			hoverCell = null;
			
			Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(hourglassCursor);
		}
	}
	
	public void unlock() {
		synchronized(locked) {
			locked = false;
			
			Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
			setCursor(normalCursor);
		}
	}
	
	public Metrics getMetrics() {
		Metrics m = new Metrics();
		m.width = getWidth();
		m.height = getHeight();
		m.side = Math.min(m.width, m.height);
		m.cell = m.side / 8;
		return m;
	}
	
	public BoardCell mouseCoordsToBoardCell(int x, int y) {
		Metrics m = getMetrics();
		return controller.getBoard().get(y / m.cell, x / m.cell);
	}
	
	public void setBestMove(BoardCell cell) {
		bestMove = cell;
		lastMove = null;
	}
	
	public void setLastMove(BoardCell cell) {
		bestMove = null;
		lastMove = cell;
	}
	
	public synchronized void asyncRepaint() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});
	}
	
	@Override
	public Point getToolTipLocation(MouseEvent event) {
		return new Point(event.getX()/8*8 + 16, event.getY()/8*8 + 24);
	}
	
	@Override
	public void paint(Graphics g) {
		Metrics m = getMetrics();
		
		g.setColor(colors.bg);
		g.fillRect(0, 0, m.width, m.height);
		
		
		for(BoardCell c : controller.getBoard().getAllCells())
			paintCell(g, m, c);
	}
	
	private void paintCell(Graphics g, Metrics m, BoardCell c) {
		int x = 1 + c.col * m.cell,
		    y = 1 + c.row * m.cell;
		
		boolean clear = c.isClear();
		boolean validMove = controller.getTurn() != null && c.isValidMove(controller.getTurn());
		boolean hover = c == hoverCell;
		
		if(c.visitedFlag)
			g.setColor(colors.bgWorking);
		else
			g.setColor(clear && validMove ? colors.bgHilight : colors.bg);
		g.fill3DRect(x, y, m.cell - 1, m.cell - 1, !(hover && validMove));
		
		if(validMove && c == bestMove) {
			g.setColor(colors.bestMove);
			g.fillRect(x + m.cell / 2 - 2, y + m.cell / 2 - 2, 4, 4);
		}
		
		if(c.isClear()) return;
		
		g.setColor(c.getColor().awtColor());
		g.fillOval(x + 3, y + 3, m.cell - 7, m.cell - 7);
		
		if(c == lastMove) {
			g.setColor(colors.lastMove);
			g.fillRect(x + m.cell / 2 - 2, y + m.cell / 2 - 2, 4, 4);
		}
	}
	
	// CellListener observer
	
	private List<CellListener> cellListeners = new ArrayList<CellListener>();
	
	public static interface CellListener extends EventListener {
		public void cellClicked(BoardCell cell);
	}
	
	public void addCellListener(CellListener listener) {
		if(!cellListeners.contains(listener))
			cellListeners.add(listener);
	}
	
	public void removeCellListener(CellListener listener) {
		cellListeners.remove(listener);
	}
	
	private void notifyCellListeners(BoardCell cell) {
		for(CellListener l : cellListeners)
			l.cellClicked(cell);
	}
	
	// MouseListener
	
	@Override public void mouseReleased(MouseEvent e) {
		if(locked) return;
		
		if(clickedCell == null) return;
		if(!clickedCell.equals(mouseCoordsToBoardCell(e.getX(), e.getY()))) return;
		
		synchronized(locked) {
			if(!locked) {
				notifyCellListeners(clickedCell);
			}
		}
	}
	
	@Override public void mousePressed(MouseEvent e) {
		clickedCell = mouseCoordsToBoardCell(e.getX(), e.getY());
	}
	
	@Override public void mouseExited(MouseEvent e) {}
	
	@Override public void mouseEntered(MouseEvent e) {}
	
	@Override public void mouseClicked(MouseEvent e) {}
	
	// MouseMotionListener
	
	@Override public void mouseDragged(MouseEvent e) {}
	
	@Override public void mouseMoved(MouseEvent e) {
		if(locked) return;
		
		BoardCell oldHover = hoverCell;
		hoverCell = mouseCoordsToBoardCell(e.getX(), e.getY());
		
		if(hoverCell != oldHover) {
			repaint();
		}
	}
}
