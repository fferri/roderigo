package roderigo.struct;

/**
 * Represent the state of a game, which is composed by a <code>Board</code> and
 * the <code>BoardCellColor</code> of the player that has to move now.
 * 
 * When the game is finished, the color indicating the turn will be <code>null</code>
 * 
 * @author Federico Ferri
 *
 */
public class GameState {
	private Board board;
	
	private BoardCellColor turn;
	
	private int depth;
	
	public GameState() {
		newGame(); // creates a new Board object as well
	}
	
	public GameState(GameState s) {
		board = s.getBoard().clone();
		turn = s.getTurn();
		depth = s.getDepth();
	}
	
	public Board getBoard() {
		return board;
	}
	
	public BoardCellColor getTurn() {
		return turn;
	}
	
	public void newGame() {
		board = new Board(8, 8);
		turn = BoardCellColor.BLACK;
		depth = 0;
	}

	public boolean move(BoardCell c) {
		if(!board.makeMove(c, turn))
			return false;
		
		switchTurn();
		
		if(board.getAllPieces().size() == board.getNumRows() * board.getNumColumns()) {
			// the board is full
			// means the game is finished
			turn = null;
			return true;
		} else if(board.getValidMoves(turn).isEmpty()) {
			// current player has to pass
			switchTurn();
			if(board.getValidMoves(turn).isEmpty()) {
				// also the other player has to pass
				// means the game is finished
				turn = null;
				return true;
			}
		}
		
		depth++;
		
		return true;
	}
	
	public void switchTurn() {
		turn = turn.opposite();
	}
	
	public int getDepth() {
		return depth;
	}
}
