package tictactoe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;

public class TicTacToeBoard extends AbstractBoard<TicTacToeBoard.Position, TicTacToeBoard.Action, TicTacToeBoard.Color> {
	public static final Color CROSS = new Color(-1);
	public static final Color CIRCLE = new Color(1);

	public class Position extends AbstractPosition {
		protected Position(int index) {
			super(index);
		}

		@Override
		public int getRow() {
			return index / cols;
		}

		@Override
		public int getCol() {
			return index % cols;
		}
		
		@Override
		public String toString() {
			return positionToString(this);
		}
	}
	
	public class Action extends AbstractAction<Position> {
		public Action() {
			this(null);
		}
		
		public Action(Position position) {
			super(position);
		}
		
		@Override
		public String toString() {
			return actionToString(this);
		}
		
		@Override
		public Object clone() {
			return new Action(position);
		}
	}
	
	public static class Color extends AbstractColor {
		protected Color(int value) {
			super(value);
		}
		
		@Override
		public String toString() {
			if(equals(CROSS)) return "X";
			if(equals(CIRCLE)) return "O";
			return super.toString();
		}
	}
	
	public TicTacToeBoard() {
		super(3, 3);
	}
	
	public TicTacToeBoard(TicTacToeBoard b) {
		super(b);
	}

	@Override
	protected Color[] alloc() {
		return new Color[rows * cols];
	}
	
	@Override
	public Object clone() {
		return new TicTacToeBoard(this);
	}
	
	@Override
	protected Color[] getValueMapping() {
		return new Color[]{null, CROSS, CIRCLE};
	}

	@Override
	protected Color[] getColors() {
		return new Color[]{CROSS, CIRCLE};
	}

	@Override
	public Position position(int row, int col) {
		return new Position(row * cols + col);
	}
	
	@Override
	public Iterator<Position> positionIterator() {
		return new Iterator<Position>() {
			private int p = 0;
			
			@Override
			public boolean hasNext() {
				return p < cell.length;
			}

			@Override
			public Position next() {
				return new Position(p++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public Action action(Position position) {
		return new Action(position);
	}
	
	@Override
	public Color color(int value) {
		return new Color(value);
	}
	
	@Override
	public char colorChar(Color player) {
		if(player == null)
			return '_';
		if(player.equals(CROSS))
			return 'X';
		if(player.equals(CIRCLE))
			return 'O';
		return '_';
	}

	@Override
	public String colorToString(Color player) {
		if(player == null)
			return "_";
		if(player.equals(CROSS))
			return "X";
		if(player.equals(CIRCLE))
			return "O";
		return "???";
	}

	@Override
	public Color colorFromString(String s) {
		if(s == null)
			throw new IllegalArgumentException();
		
		if(s.equals("X"))
			return CROSS;
		if(s.equals("O"))
			return CIRCLE;
		return null;
	}
	
	@Override
	protected Color colorFlip(Color color) {
		if(color == null) return null;
		return new Color(-color.getValue());
	}
	
	@Override
	public Action actionPass() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void initBoard() {
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int row = 0; row < getNumRows(); row++) {
			for(int col = 0; col < getNumCols(); col++) {
				sb.append(' ').append(colorChar(get(position(row, col))));
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	@Override
	public boolean makeAction(Color player, Action action) {
		if(player == null || action == null)
			throw new IllegalArgumentException();
		
		if(action.isPass() || !isValidMove(player, action.getPosition()))
			return false;
		set(action.getPosition(), player);
		flipTurn();
		setLastAction(action);
		return true;
	}

	@Override
	public boolean isValidMove(Color player, Position position) {
		return positionIsValid(position) && get(position) == null;
	}

	@Override
	public boolean isGameOver() {
		if(getWinner() != null)
			return true;
		
		for(int row = 0; row < getNumRows(); row++) {
			for(int col = 0; col < getNumCols(); col++) {
				if(get(position(row, col)) == null)
					return false;
			}
		}
		return true;
	}

	public List<List<Position>> lines() {
		int sz = Math.min(rows, cols);
		List<List<Position>> ret = new ArrayList<>(2 * sz + 2);
		// rows:
		for(int row = 0; row < sz; row++) {
			List<Position> line = new ArrayList<>(sz);
			for(int col = 0; col < sz; col++)
				line.add(position(row, col));
			ret.add(line);
		}
		// cols:
		for(int col = 0; col < sz; col++) {
			List<Position> line = new ArrayList<>(sz);
			for(int row = 0; row < sz; row++)
				line.add(position(row, col));
			ret.add(line);
		}
		// indices of diagonal 1
		{
			List<Position> line = new ArrayList<>(sz);
			for(int i = 0; i < sz; i++)
				line.add(position(i, i));
			ret.add(line);
		}
		// indices of diagonal 2
		{
			List<Position> line = new ArrayList<>(sz);
			for(int i = 0; i < sz; i++)
				line.add(position(sz - i - 1, i));
			ret.add(line);
		}
		return ret;
	}
	
	public Color lineAllEqual(List<Position> line) {
		Color first = get(line.get(0));
		if(first == null) return null;
		int sz = Math.min(rows, cols);
		return linePotential(line, first) >= sz ? first : null;
	}
	
	public int linePotential(List<Position> line, Color color) {
		int numColor = 0;
		for(Position p : line) {
			Color c = get(p);
			if(c == null) continue;
			if(c.equals(color)) numColor++;
			else return -1;
		}
		return numColor;
	}
	
	@Override
	public Color getWinner() {
		List<List<Position>> lines = lines();
		for(List<Position> line : lines) {
			Color c = lineAllEqual(line);
			if(c != null) return c;
		}
		return null;
	}

	@Override
	public List<Action> getValidActions(Color player) {
		List<Position> validMoves = super.getValidMoves(player);
		List<Action> ret = new ArrayList<>(validMoves.size());
		for(Position position : validMoves)
			ret.add(action(position));
		return ret;
	}
}
