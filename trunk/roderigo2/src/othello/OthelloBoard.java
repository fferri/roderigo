package othello;

import java.util.Iterator;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;

/**
 * Model of an othello board.
 * 
 * @author Federico Ferri
 *
 */
public class OthelloBoard extends AbstractBoard<OthelloBoard.Position, OthelloBoard.Action, OthelloBoard.Color> {
	// values for cells:
	public static final Color BLACK = new Color(-1);
	public static final Color WHITE = new Color(1);
	
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
			if(equals(BLACK)) return "black";
			if(equals(WHITE)) return "white";
			return super.toString();
		}
	}
	
	@Override
	public Action actionPass() {
		return new Action();
	}
	
	@Override
	public Action action(Position position) {
		return new Action(position);
	}
	
	@Override
	public Position position(int row, int col) {
		if(row >= 0 && row < rows && col >= 0 && col < cols)
			return new Position(row * OthelloBoard.this.cols + col);
		else
			return null;
	}
	
	@Override
	public Iterator<Position> positionIterator() {
		return new Iterator<Position>() {
			int p = 0;
			
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
	public Color color(int value) {
		return new Color(value);
	}
	
	private StableDiscs stableDiscs = new StableDiscs(this);
	
	public OthelloBoard() {
		super(8, 8);
		initBoard();
	}
	
	public OthelloBoard(OthelloBoard othelloBoard) {
		super(othelloBoard);
	}
	
	@Override
	protected Color[] alloc() {
		return new Color[rows * cols];
	}
	
	@Override
	public Object clone() {
		return new OthelloBoard(this);
	}
	
	@Override
	protected void initBoard() {
		set(position(3, 3), WHITE);
		set(position(3, 4), BLACK);
		set(position(4, 3), BLACK);
		set(position(4, 4), WHITE);
		setTurn(BLACK);
	}
	
	@Override
	public Position positionFromString(String s) {
		if(s.length() != 2) return null;
		return position(s.charAt(0) - 'a', s.charAt(1) - '1');
	}
	
	@Override
	public String positionToString(Position position) {
		return String.format("%c%c", 'a' + position.getCol(), '1' + position.getRow());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = -1; i < getNumRows(); i++) {
			if(i < 0) sb.append("   a b c d e f g h");
			else for(int j = -1; j < getNumCols(); j++) {
				if(j < 0) sb.append((i + 1) + "  ");
				else sb.append(colorChar(get(position(i, j))) + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	@Override
	protected Color[] getColors() {
		return new Color[]{BLACK, WHITE};
	}
	
	@Override
	public char colorChar(Color playerColor) {
		if(playerColor == null) return '.';
		if(playerColor.equals(BLACK)) return '#';
		if(playerColor.equals(WHITE)) return 'O';
		return '.';
	}
	
	@Override
	public String colorToString(Color playerColor) {
		if(playerColor.equals(BLACK))
			return "black";
		if(playerColor.equals(WHITE))
			return "white";
		return "???";
	}
	
	@Override
	public Color colorFromString(String s) {
		if(s.equals("black"))
			return BLACK;
		if(s.equals("white"))
			return WHITE;
		return null;
	}
	
	@Override
	protected Color colorFlip(Color color) {
		if(color == null) return null;
		return new Color(-color.getValue());
	}
	
	@Override
	protected Color[] getValueMapping() {
		return new Color[]{null, BLACK, WHITE};
	}
	
	@Override
	public boolean isValidMove(Color player, Position position) {
		if(!positionIsValid(position))
			return false;
		
		// cell must be empty:
		if(get(position) != null) return false;
		
		// must find at least one opponent's piece followed by
		// own piece, in at least one direction:
		for(int direction[] : getAllDirections()) {
			if(isValidMove(player, position, direction))
				return true;
		}
		
		return false;
	}
	
	private boolean isValidMove(Color player, Position position, int direction[]) {
		boolean first = true;
		while(true) {
			position = positionIncr(position, direction);
			
			if(!positionIsValid(position)) return false;
			
			Color curState = get(position);
			
			if(first) {
				first = false;
				if(curState == null) return false;
				if(curState.equals(player)) return false;
			} else {
				if(curState == null) return false;
				if(curState.equals(player)) return true;
			}
		}
	}
	
	/**
	 * Play a disc in the specified position for the specified player.
	 * 
	 * @param player The player.
	 * @param p Position to be played.
	 * 
	 * @return true if the move was valid.
	 */
	@Override
	public boolean makeAction(Color player, Action action) {
		if(action == null)
			throw new IllegalArgumentException();
		
		boolean ret = false;
			
		if(action.isPass()) {
			if(!isValidAction(player, action)) return false;
			ret = true;
		} else {
			Position position = action.getPosition();
			
			if(get(position) != null) return false;
				
			for(int direction[] : getAllDirections()) {
				if(isValidMove(player, position, direction))
					if(makeMove(player, position, direction))
						ret = true;
			}
			
			if(ret) {
				set(position, player);
				stableDiscs.update();
			}
		}
		
		if(ret) {
			setLastAction(action);
			flipTurn();
		}
		
		return ret;
	}
	
	private boolean makeMove(Color player, Position position, int direction[]) {
		boolean first = true;
		while(true) {
			position = positionIncr(position, direction);
			
			if(!positionIsValid(position)) return false;
			
			Color curState = get(position);
			
			if(first) {
				first = false;
				if(curState == null) return false;
				if(curState.equals(player)) return false;
			} else {
				if(curState == null) return false;
				if(curState.equals(player)) return true;
			}
			set(position, player);
		}
	}

	@Override
	public boolean isGameOver() {
		return getValidMoves(getTurn()).isEmpty()
				&& getValidMoves(colorFlip(getTurn())).isEmpty();
	}

	@Override
	public Color getWinner() {
		if(!isGameOver()) return null;
		int white = count(WHITE), black = count(BLACK);
		if(white > black) return WHITE;
		if(white < black) return BLACK;
		return null;
	}

	public int getDiscDifferential() {
		return count(WHITE) - count(BLACK);
	}

	public int getMobility(Color player) {
		return getValidActions(player).size();
	}

	public int getMobilityDifferential() {
		return getMobility(WHITE) - getMobility(BLACK);
	}
	
	public int getStableDiscDifferential() {
		int diff = 0;
		for(Iterator<Position> i = positionIterator(); i.hasNext(); ) {
			Position position = i.next();
			if(stableDiscs.get(WHITE, position)) diff++;
			if(stableDiscs.get(BLACK, position)) diff--;
		}
		return diff;
	}

	public boolean positionIsStable(Position position) {
		return stableDiscs.get(BLACK, position) || stableDiscs.get(WHITE, position);
	}
	
	public boolean positionIsCorner(Position position) {
		return positionIsHSide(position) && positionIsVSide(position);
	}

	public boolean positionIsHSide(Position position) {
		int row = position.getRow();
		return row == 0 || row == (getNumRows() - 1);
	}

	public boolean positionIsVSide(Position position) {
		int col = position.getCol();
		return col == 0 || col == (getNumCols() - 1);
	}
}