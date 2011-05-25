package roderigo.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import roderigo.Main;
import roderigo.ai.AlphaBetaPlayer;
import roderigo.ai.BoardEvaluation;
import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.BoardCellSet;
import roderigo.struct.GameState;

public class JBoard extends JPanel {
	private static final long serialVersionUID = 5041436229495994615L;
	
	//private Board board = null;
	private GameState game = null;
	
	private JBoardColorScheme colors = new JBoardColorScheme();
	
	private BoardCell hoverCell = null;
	private BoardCell clickedCell = null;
	private BoardCell lastMove = null;
	private BoardCell bestMove = null;
	
	Map<BoardCell, BoardEvaluation> allHeuristics = new HashMap<BoardCell, BoardEvaluation>();
	
	// metrics used by paint and mouse operations:
	public class Metrics {
		public int width = 0, height = 0, side = 0, cell = 0;
	}
	
	private Boolean locked = false;
	
	public JBoard(GameState gameState) {
		this.game = gameState;

		setPreferredSize(new Dimension(512, 512));
		
		addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {
				if(clickedCell == null) return;
				if(!clickedCell.equals(mouseCoordsToBoardCell(e.getX(), e.getY()))) return;
				synchronized(locked) {
					if(!locked.booleanValue()) {
						if(game.move(clickedCell)) {
							repaint();
							update(); // asynchronous move & repaint
						}
					}
				}
				checkEndGame();
			}
			
			@Override public void mousePressed(MouseEvent e) {
				clickedCell = mouseCoordsToBoardCell(e.getX(), e.getY());
			}
			
			@Override public void mouseExited(MouseEvent e) {}
			
			@Override public void mouseEntered(MouseEvent e) {}
			
			@Override public void mouseClicked(MouseEvent e) {}
		});
		
		addMouseMotionListener(new MouseMotionListener() {
			@Override public void mouseDragged(MouseEvent e) {}
			
			@Override public void mouseMoved(MouseEvent e) {
				BoardCell oldHover = hoverCell;
				hoverCell = mouseCoordsToBoardCell(e.getX(), e.getY());
				
				if(hoverCell != oldHover) {
					repaint();
					
					BoardEvaluation hh = allHeuristics.get(hoverCell);
					if(hh != null) {
						setToolTipText(hh.getHTMLString());
					} else {
						setToolTipText(null);
					}
				}
			}
		});
	}
	
	public Board getBoard() {
		return game.getBoard();
	}
	
	private void lock() {
		synchronized(locked) {
			locked = true;
			
			Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(hourglassCursor);
		}
	}
	
	private void unlock() {
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
		return game.getBoard().get(y / m.cell, x / m.cell);
	}
	
	public void update() {
		// to call on init and after each move
		// updates the heuristics, the best move, and such things
		
		allHeuristics = BoardEvaluation.evaluateAllMoves(game.getBoard(), game.getTurn());
		
		final int searchDepth = Main.getInstance().mainWindow.toolbox.searchDepth.getValue();
		
		new Thread() {
			@Override
			public void run() {
				lock();
				if(Main.getInstance().mainWindow.toolbox.dontMakeMoves.isSelected()) {
					if(game.getTurn() == BoardCellColor.WHITE) {
						bestMove = new AlphaBetaPlayer(game, searchDepth, JBoard.this).getBestMove();
						asyncRepaint();
					}
				} else {
					while(game.getTurn() == BoardCellColor.WHITE) {
						bestMove = new AlphaBetaPlayer(game, searchDepth, JBoard.this).getBestMove();
						if(bestMove != null) {
							game.move(bestMove);
							lastMove = bestMove;
							bestMove = null;
							allHeuristics = BoardEvaluation.evaluateAllMoves(game.getBoard(), game.getTurn());
							asyncRepaint();
						} else break;
					}
				}
				unlock();
				checkEndGame();
			}
		}.start();
	}
	
	private void checkEndGame() {
		synchronized(locked) {
			if(locked.booleanValue()) return;
			if(game.getTurn() != null) return;
			
			BoardCellSet pieces = game.getBoard().getAllPieces();
			int w = pieces.whitePieces().size();
			int b = pieces.blackPieces().size();
			String message = "Game finished.\n\n";
			if(w == b) {
				message += "TIE! (" + w + " to " + b + ")";
			} else {
				BoardCellColor winner = BoardCellColor.WHITE;
				if(b > w) winner = winner.opposite();
				message += winner + " wins " + Math.max(w, b) + " to " + Math.min(w, b) + ".";
				if(winner == BoardCellColor.BLACK) // human
					message += "\nCongratulations!";
				else
					message += "\n\nHUMAN BEATEN BY MACHINE! (singularity still is far away though)";
				message += "\n\nPlay again?";
			}
			int answer = JOptionPane.showConfirmDialog(getTopLevelAncestor(), message);
			if(answer == JOptionPane.YES_OPTION) {
				game.newGame();
			}
		}
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
		
		
		for(BoardCell c : game.getBoard().getAllCells())
			paintCell(g, m, c);
	}
	
	private void paintCell(Graphics g, Metrics m, BoardCell c) {
		int x = c.col * m.cell,
		    y = c.row * m.cell;
		
		boolean clear = c.isClear();
		boolean validMove = game.getTurn() != null && c.isValidMove(game.getTurn());
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
		
		BoardEvaluation h = allHeuristics.get(c);
		if(h != null) {
			g.setColor(Color.black);
			g.drawString("" + h.getValue(), x + m.cell / 2 - 2, y + m.cell / 2 - 2);
		}
		
		if(c.isClear()) return;
		
		g.setColor(c.getColor().awtColor());
		g.fillOval(x + 3, y + 3, m.cell - 7, m.cell - 7);
		
		if(c == lastMove) {
			g.setColor(colors.lastMove);
			g.fillRect(x + m.cell / 2 - 2, y + m.cell / 2 - 2, 4, 4);
		}
	}
}
