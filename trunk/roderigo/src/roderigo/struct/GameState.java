package roderigo.struct;

public class GameState {
	private Board board;
	
	private BoardCellColor turn;
	
	private BoardCell lastMove;
	private GameState next; // used by AlphaBetaPlayer
	
	private int depth;
	
	float hMin = 0, hMax = 0;
	
	public GameState() {
		newGame(); // creates a new Board object as well
	}
	
	public GameState(GameState s) {
		board = s.getBoard().clone();
		turn = s.getTurn();
		lastMove = s.getLastMove();
		depth = s.getDepth();
	}
	
	public Board getBoard() {
		return board;
	}
	
	public BoardCellColor getTurn() {
		return turn;
	}
	
	public BoardCell getLastMove() {
		return lastMove;
	}
	
	public void newGame() {
		board = new Board(8, 8);
		turn = BoardCellColor.BLACK;
		depth = 0;
	}

	public boolean move(BoardCell c) {
		if(!board.makeMove(board.get(c.row, c.col), turn))
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
		
		lastMove = c;
		depth++;
		
		return true;
	}
	
	private void switchTurn() {
		turn = turn.opposite();
	}
	
	public GameState getNext() {
		return next;
	}
	
	public void setNext(GameState n) {
		next = n;
	}
	
	public int getDepth() {
		return depth;
	}
}
