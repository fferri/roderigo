package game;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Model of an abstract NxM board.
 * 
 * @author Federico Ferri
 *
 * @param <P> class representing the position
 * @param <A> class representing the action
 * @param <C> class representing the player color
 */
public abstract class AbstractBoard<P extends AbstractPosition, A extends AbstractAction<P>, C extends AbstractColor> extends Observable {
	protected final int rows;
	protected final int cols;
	
	protected C cell[];
	
	protected C turn;
	protected A lastAction = null;
	
	public AbstractBoard(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.cell = alloc();
		this.turn = getColors()[0];
		
		initBoard();
	}
	
	public AbstractBoard(AbstractBoard<P, A, C> board) {
		this.rows = board.rows;
		this.cols = board.cols;
		this.cell = Arrays.copyOf(board.cell, board.cell.length);
		this.turn = board.getTurn();
	}
	
	protected abstract C[] alloc();
	
	@Override
	public int hashCode() {
		byte b[] = get().toByteArray();
		int code = 0;
		for(int i = 0; i < b.length; i++)
			code = code ^ (b[i] << (8 * (i % 4)));
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof AbstractBoard)) return false;
		@SuppressWarnings("unchecked")
		AbstractBoard<P, A, C> b = (AbstractBoard<P, A, C>)obj;
		for(Iterator<P> i = positionIterator(); i.hasNext(); ) {
			P position = i.next();
			if(!colorEquals(get(position), b.get(position)))
				return false;
		}
		return true;
	}
	
	@Override
	public abstract Object clone();
	
	public int getNumRows() {
		return rows;
	}
	
	public int getNumCols() {
		return cols;
	}
	
	public abstract P position(int row, int col);
	
	public abstract Iterator<P> positionIterator();
	
	public boolean positionIsValid(P position) {
		return position != null && position.getIndex() >= 0 && position.getIndex() < cell.length;
	}
	
	public boolean positionIsValid(int row, int col) {
		return !(row < 0 || row >= rows || col < 0 || col >= cols);
	}
	
	public P positionFromString(String s) {
		String t[] = s.split(",");
		if(t.length != 2) return null;
		return position(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
	}
	
	public String positionToString(P position) {
		return String.format("%d,%d", position.getRow(), position.getCol());
	}
	
	public P positionIncr(P position, int direction[]) {
		int newRow = position.getRow() + direction[0];
		int newCol = position.getCol() + direction[1];
		if(!positionIsValid(newRow, newCol)) return null;
		return position(newRow, newCol);
	}

	public P positionTransform(P position, int transform) {
		if(transform < 0 || transform > 7) throw new IllegalArgumentException();
		boolean rotate = (transform & 1) > 0;
		boolean flipHoriz = (transform & 2) > 0;
		boolean flipVert = (transform & 4) > 0;
		int row = position.getRow(), col = position.getCol();
		if(rotate) {
			int oldcol = col;
			col = cols - 1 - row;
			row = oldcol;
		}
		if(flipHoriz) {
			col = cols - 1 - col;
		}
		if(flipVert) {
			row = rows - 1 - row;
		}
		return position(row, col);
	}
	
	public abstract A actionPass();
	
	public abstract A action(P position);
	
	public A actionFromString(String s) {
		return s.equals("PASS") ? actionPass() : action(positionFromString(s));
	}
	
	public String actionToString(A action) {
		return action.isPass() ? "PASS" : positionToString(action.getPosition());
	}
	
	public String actionListToString(List<A> l) {
		StringBuilder sb = new StringBuilder();
		for(A i : l) sb.append(sb.length() > 0 ? ", " : "").append(actionToString(i));
		return "[" + sb.toString() + "]";
	}
	
	public abstract C color(int value);
	
	public abstract char colorChar(C color);
	
	public abstract String colorToString(C color);

	public abstract C colorFromString(String s);
	
	protected abstract C colorFlip(C player);

	public boolean colorEquals(C color1, C color2) {
		if(color1 == null) return color2 == null;
		if(color2 == null) return color1 == null;
		return color1.equals(color2);
	}
	
	protected abstract C[] getColors();
	
	protected abstract C[] getValueMapping();
	
	public int colorIndex(C color) {
		C colors[] = getColors();
		for(int i = 0; i < colors.length; i++)
			if(color.equals(colors[i]))
				return i;
		return -1;
	}
	
	public int[][] getAllDirections() {
		// directions must be adjacent!
		return new int[][]{{-1,-1}, {-1,0}, {-1,1}, {0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}};
	}
	
	public boolean equalsTransformInvariant(AbstractBoard<P, A, C> b) {
		for(int transform = 0; transform < 8; transform++)
			if(equals(b, transform))
				return true;
		return false;
	}
	
	public boolean equals(AbstractBoard<P, A, C> b, int transform) {
		for(Iterator<P> i = positionIterator(); i.hasNext(); ) {
			P position = i.next(), position2 = positionTransform(position, transform);
			if(!colorEquals(get(position), b.get(position2)))
				return false;
		}
		return true;
	}

	protected abstract void initBoard();

	public void reset() {
		initBoard();
	}

	public abstract String toString();

	public C get(P position) {
		return cell[position.getIndex()];
	}

	public C get(P position, int transform) {
		position = positionTransform(position, transform);
		return cell[position.getIndex()];
	}

	public BigInteger get() {
		BigInteger v = BigInteger.valueOf(1);
		C mapping[] = getValueMapping();
		Map<C, Integer> rmapping = new HashMap<>();
		for(int i = 0; i < mapping.length; i++) rmapping.put(mapping[i], i);
		for(int position = 0; position < cell.length; position++) {
			v = v.multiply(BigInteger.valueOf(mapping.length));
			v = v.add(BigInteger.valueOf(rmapping.get(cell[position])));
		}
		return v;
	}

	public void set(P position, C color) {
		cell[position.getIndex()] = color;
		
		setChanged();
		notifyObservers();
	}

	public void set(P position, int transform, C color) {
		cell[positionTransform(position, transform).getIndex()] = color;
		
		setChanged();
		notifyObservers();
	}

	public void set(int values[][]) {
		if(this.rows != values.length || this.cols != values[0].length)
			throw new IllegalArgumentException("dimensions mismatch");

		for(int row = 0; row < rows; row++)
			for(int col = 0; col < cols; col++)
				cell[position(row, col).getIndex()] = color(values[row][col]);
		
		setChanged();
		notifyObservers();
	}

	public void set(C color) {
		for(int position = 0; position < cell.length; position++)
			cell[position] = color;
		
		setChanged();
		notifyObservers();
	}

	public void set(BigInteger v) {
		C mapping[] = getValueMapping();
		for(int position = 0; position < cell.length; position++) {
			int t = v.mod(BigInteger.valueOf(mapping.length)).intValue();
			v = v.divide(BigInteger.valueOf(mapping.length));
			cell[cell.length - position - 1] = mapping[t];
		}
		
		if(v.intValue() != 1) throw new IllegalArgumentException();
		
		setChanged();
		notifyObservers();
	}

	public C getTurn() {
		return turn;
	}

	public void setTurn(C turn) {
		this.turn = turn;
	}

	public A getLastAction() {
		return lastAction;
	}

	public void setLastAction(A action) {
		lastAction = action;
		
		setChanged();
		notifyObservers();
	}

	public boolean isValidAction(A action) {
		return isValidAction(getTurn(), action);
	}

	public boolean isValidAction(C player, A action) {
		if(action.isPass())
			return getValidMoves(player).isEmpty();
		
		return isValidMove(player, action.getPosition());
	}

	public boolean makeAction(A action) {
		return makeAction(getTurn(), action);
	}

	public abstract boolean makeAction(C player, A action);

	public List<A> getValidActions() {
		return getValidActions(getTurn());
	}
	
	public List<A> getValidActions(C player) {
		List<A> ret = new ArrayList<>();
		
		for(P p : getValidMoves(player))
			ret.add(action(p));
		
		if(ret.isEmpty()) ret.add(actionPass());
		
		return ret;
	}
	
	public boolean isValidMove(P position) {
		return isValidMove(getTurn(), position);
	}
	
	public abstract boolean isValidMove(C player, P position);
	
	public List<P> getValidMoves() {
		return getValidMoves(getTurn());
	}

	public List<P> getValidMoves(C player) {
		List<P> ret = new ArrayList<P>();
		
		for(Iterator<P> i = positionIterator(); i.hasNext(); ) {
			P position = i.next();
			if(isValidMove(player, position))
				ret.add(position);
		}
		
		return ret;
	}
	
	public abstract boolean isGameOver();

	public abstract C getWinner();

	public int count(C value) {
		int count = 0;
		
		for(int position = 0; position < cell.length; position++)
			if(colorEquals(cell[position], value))
				count++;
		
		return count;
	}
	
	public void flipColors() {
		for(int position = 0; position < cell.length; position++)
			cell[position] = colorFlip(cell[position]);
		
		setChanged();
		notifyObservers();
	}

	public void flipTurn() {
		turn = colorFlip(turn);
		
		setChanged();
		notifyObservers();
	}

	public int getCanonicalTransform() {
		final Integer transform[] = {0,1,2,3,4,5,6,7};
		Arrays.sort(transform, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				for(Iterator<P> i = positionIterator(); i.hasNext(); ) {
					P position = i.next();
					Integer v1 = get(position, transform[o1]).getValue(),
							v2 = get(position, transform[o2]).getValue();
					if(v1 < v2) return -1;
					if(v1 > v2) return 1;
				}
				return 0;
			}
		});
		return transform[0];
	}
}
