package roderigo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.Border;

import roderigo.Controller;
import roderigo.struct.Board;
import roderigo.struct.BoardCell;

public class JBoardWithBorder extends JPanel {
	private static final long serialVersionUID = 649882899923586387L;
	
	private Controller controller;
	
	public final JBoard jboard;
	
	private static final int TOTAL_BORDER_SIZE = 26;
	private static final int INNER_BORDER_SIZE = 5;
	
	public JBoardWithBorder(Controller controller) {
		this.controller = controller;
		jboard = new JBoard(controller);
		
		setLayout(new BorderLayout());
		setBorder(new Border() {

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				g.setColor(Color.darkGray);
				g.fillRoundRect(x + INNER_BORDER_SIZE, y + INNER_BORDER_SIZE, width - 2 * INNER_BORDER_SIZE, height - 2 * INNER_BORDER_SIZE, TOTAL_BORDER_SIZE, TOTAL_BORDER_SIZE);
				g.setColor(Color.white);
				
				Board b = JBoardWithBorder.this.controller.getBoard();
				JBoard.Metrics m = jboard.getMetrics();
				int fh = g.getFontMetrics().getHeight();
				int fw = g.getFontMetrics().stringWidth("m");
				for(int row = 0; row < b.getNumRows(); row++) {
					for(int off = 0; off < 2; off++)
						g.drawString(BoardCell.getRowString(row), INNER_BORDER_SIZE + (TOTAL_BORDER_SIZE - INNER_BORDER_SIZE) / 2 - fw / 2 + off * (TOTAL_BORDER_SIZE + m.width), TOTAL_BORDER_SIZE + row * m.cell + m.cell / 2 + fh / 2);
				}
				for(int col = 0; col < b.getNumColumns(); col++) {
					for(int off = 0; off < 2; off++)
						g.drawString(BoardCell.getColumnString(col), TOTAL_BORDER_SIZE + col * m.cell + m.cell / 2 - fw / 2, INNER_BORDER_SIZE + (TOTAL_BORDER_SIZE - INNER_BORDER_SIZE) / 2 + fh / 2 + off * (TOTAL_BORDER_SIZE + m.height - 5));
				}
			}

			@Override
			public Insets getBorderInsets(Component c) {
				return new Insets(TOTAL_BORDER_SIZE, TOTAL_BORDER_SIZE, TOTAL_BORDER_SIZE, TOTAL_BORDER_SIZE);
			}

			@Override
			public boolean isBorderOpaque() {
				return true;
			}
		});
		
		add(jboard);
	}
	
	@SuppressWarnings("deprecation")
	public void reshape(int x, int y, int width, int height) {
		int b2 = 2 * TOTAL_BORDER_SIZE;
		int minSize = (Math.min(width, height) - b2) / 8 * 8 + b2;
		if(minSize > 0) {
			width = minSize;
			height = minSize;
		}
		super.reshape(x, y, width, height);
	}
}
