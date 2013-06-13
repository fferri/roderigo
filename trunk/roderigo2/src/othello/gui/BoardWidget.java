package othello.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import othello.OthelloBoard;
import othello.OthelloBoard.Position;

public class BoardWidget extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;

	public static final Color colorBackground = Color.decode("#1e8a28");
	public static final Color colorValidMove = Color.decode("#5dc166");
	public static final Color colorHoverMove = Color.decode("#7de186");
	public static final Color colorLines = Color.decode("#17591d");
	public static final Color colorDiscWhite = Color.decode("#ffffff");
	public static final Color colorDiscBlack = Color.decode("#000000");
	public static final Color colorLastMove = Color.decode("#cc0000");
	
	public static final boolean markStableDiscs = true;
	
	private OthelloBoard board;
	private List<OthelloBoard.Position> availableMoves;
	private OthelloBoard.Position hoveringMove, lastMove;

	public BoardWidget() {
		setPreferredSize(new Dimension(496, 496));
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				OthelloBoard.Position p = pointToPosition(e.getPoint());
				if(p != null)
					cellClicked(p, e.getButton() == MouseEvent.BUTTON1);
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				hoveringMove = pointToPosition(e.getPoint());
				repaint();
			}
		});
		
		addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				Dimension d = getSize();
				int minsz = Math.max(96, Math.min(d.width, d.height));
				d.width = minsz;
				d.height = minsz;
				setSize(d);
			}
		});
	}
	
	public void setBoard(OthelloBoard board) {
		if(this.board != null)
			this.board.deleteObserver(this);
		this.board = board;
		this.board.addObserver(this);
		
		availableMoves = board == null ? null : board.getValidMoves();
		if(board == null || board.getLastAction() == null) {
			lastMove = null;
		} else {
			lastMove = board.getLastAction().getPosition();
		}
		
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(board != null) {
			availableMoves = board.getValidMoves();
			lastMove = board.getLastAction() != null ? board.getLastAction().getPosition() : null;
		}
		
		g.setColor(colorBackground);
		Dimension d = getSize();
		g.fillRect(0, 0, d.width, d.height);
		
		g.setColor(colorLines);
		g.drawRect(0, 0, d.width, d.height);

		for(Iterator<OthelloBoard.Position> i = board.positionIterator(); i.hasNext(); ) {
			OthelloBoard.Position p = i.next();
			Rectangle r = cellRect(p, d);
			
			if(availableMoves != null && availableMoves.contains(p)) {
				g.setColor(p.equals(hoveringMove) ? colorHoverMove : colorValidMove);
				g.fillRect(r.x, r.y, r.width, r.height);
			}
			
			g.setColor(colorLines);
			g.drawRect(r.x, r.y, r.width, r.height);

			if(board == null) continue;
			
			OthelloBoard.Color state = board.get(p);
			if(state == null) {
			} else if(state.equals(OthelloBoard.WHITE)) {
				g.setColor(colorDiscWhite);
			} else if(state.equals(OthelloBoard.BLACK)) {
				g.setColor(colorDiscBlack);
			}
			
			final int border = 3;
			if(state != null)
				g.fillOval(r.x + border, r.y + border, r.width - 2 * border, r.height - 2 * border);
			
			if(p.equals(lastMove)) {
				final int b1 = 7;
				g.setColor(colorLastMove);
				g.drawOval(r.x + b1, r.y + b1, r.width - 2 * b1, r.height - 2 * b1);
				g.drawOval(r.x + b1 + 1, r.y + b1 + 1, r.width - 2 * b1 - 2, r.height - 2 * b1 - 2);
			} else if(markStableDiscs && board.positionIsStable(p)) {
				final int b2 = 7;
				g.setColor(colorBackground);
				g.drawOval(r.x + b2, r.y + b2, r.width - 2 * b2, r.height - 2 * b2);
			}
		}
	}
	
	private Rectangle cellRect(Position p, Dimension d) {
		int x1 = p.getCol() * d.width / board.getNumCols();
		int y1 = p.getRow() * d.height / board.getNumRows();
		int x2 = (p.getCol() + 1) * d.width / board.getNumCols();
		int y2 = (p.getRow() + 1) * d.height / board.getNumRows();
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}
	
	private OthelloBoard.Position pointToPosition(Point pt) {
		Dimension d = getSize();
		return board.position(pt.y * board.getNumRows() / d.height, pt.x * board.getNumCols() / d.width);
	}
	
	private void cellClicked(OthelloBoard.Position position, boolean leftButton) {
		for(CellClickListener listener : cellClickListeners)
			listener.cellClicked(position, leftButton);
	}
	
	public static interface CellClickListener {
		public void cellClicked(OthelloBoard.Position position, boolean leftButton);
	}
	
	private List<CellClickListener> cellClickListeners = new ArrayList<CellClickListener>();
	
	public void addCellClickListener(CellClickListener listener) {
		cellClickListeners.add(listener);
	}
	
	public void removeCellClickListener(CellClickListener listener) {
		cellClickListeners.remove(listener);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		repaint();
	}
}
