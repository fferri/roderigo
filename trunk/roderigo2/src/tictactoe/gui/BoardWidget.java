package tictactoe.gui;

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

import tictactoe.TicTacToeBoard;

public class BoardWidget extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;

	public static final Color colorBackground = Color.decode("#ffffff");
	public static final Color colorValidMove = Color.decode("#ffffff");
	public static final Color colorHoverMove = Color.decode("#ffff00");
	public static final Color colorLines = Color.decode("#000000");
	public static final Color colorLastMove = Color.decode("#cc0000");
		
	private TicTacToeBoard board;
	private List<TicTacToeBoard.Position> availableMoves;
	private TicTacToeBoard.Position hoveringMove, lastMove;

	public BoardWidget() {
		setPreferredSize(new Dimension(165, 165));
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TicTacToeBoard.Position p = pointToPosition(e.getPoint());
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
	
	public void setBoard(TicTacToeBoard board) {
		if(this.board != null)
			this.board.deleteObserver(this);
		this.board = board;
		this.board.addObserver(this);
		
		availableMoves = board == null ? null : board.getValidMoves();
		lastMove = board == null ? null : (board.getLastAction() != null ? board.getLastAction().getPosition() : null);
		
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(colorBackground);
		Dimension d = getSize();
		g.fillRect(0, 0, d.width, d.height);
		
		g.setColor(colorLines);
		g.drawRect(0, 0, d.width, d.height);
		
		if(board == null) return;
		
		availableMoves = board.getValidMoves();
		lastMove = board.getLastAction() != null ? board.getLastAction().getPosition() : null;

		for(Iterator<TicTacToeBoard.Position> i = board.positionIterator(); i.hasNext(); ) {
			TicTacToeBoard.Position p = i.next();
			Rectangle r = cellRect(p, d);
			
			if(availableMoves != null && availableMoves.contains(p)) {
				g.setColor(p.equals(hoveringMove) ? colorHoverMove : colorValidMove);
				g.fillRect(r.x, r.y, r.width, r.height);
			}
			
			g.setColor(colorLines);
			g.drawRect(r.x, r.y, r.width, r.height);
			
			final int border = 3;
			TicTacToeBoard.Color state = board.get(p);
			if(state == null) {
			} else if(state.equals(TicTacToeBoard.CIRCLE)) {
				g.drawOval(r.x + border, r.y + border, r.width - 2 * border, r.height - 2 * border);
			} else if(state.equals(TicTacToeBoard.CROSS)) {
				int x1 = r.x + border, x2 = r.x + r.width - border;
				int y1 = r.y + border, y2 = r.y + r.height - border;
				g.drawLine(x1, y1, x2, y2);
				g.drawLine(x2, y1, x1, y2);
			}
			
			if(p.equals(lastMove)) {
				g.setColor(colorLastMove);
				g.fillRect(r.x + r.width - border, r.y + r.height - border, border - 1, border - 1);
			}
		}
	}
	
	private Rectangle cellRect(TicTacToeBoard.Position p, Dimension d) {
		int x1 = p.getCol() * d.width / board.getNumCols();
		int y1 = p.getRow() * d.height / board.getNumRows();
		int x2 = (p.getCol() + 1) * d.width / board.getNumCols();
		int y2 = (p.getRow() + 1) * d.height / board.getNumRows();
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}
	
	private TicTacToeBoard.Position pointToPosition(Point pt) {
		Dimension d = getSize();
		return board.position(pt.y * board.getNumRows() / d.height, pt.x * board.getNumCols() / d.width);
	}
	
	private void cellClicked(TicTacToeBoard.Position position, boolean leftButton) {
		for(CellClickListener listener : cellClickListeners)
			listener.cellClicked(position, leftButton);
	}
	
	public static interface CellClickListener {
		public void cellClicked(TicTacToeBoard.Position position, boolean leftButton);
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
