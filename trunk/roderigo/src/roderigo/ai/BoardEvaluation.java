package roderigo.ai;

import java.util.HashMap;
import java.util.Map;

import roderigo.struct.Board;
import roderigo.struct.BoardCell;
import roderigo.struct.BoardCellColor;
import roderigo.struct.BoardCellSet;

/**
 * Evaluation of board, based on the following heuristics:
 * <ul>
 * <li>Mobility
 * <li>Corners
 * <li>Edges
 * <li>Partial influence map (only A, B, C, X cells)
 * <li>Piece count
 * </ul>
 * 
 * @author Federico Ferri
 *
 */
public class BoardEvaluation {
	private int ownMobility;
	private int opponentMobility;
	private int ownBorderPieceCount;
	private int opponentBorderPieceCount;
	private int ownPieceCount;
	private int opponentPieceCount;
	private int ownStablePieceCount;
	private int opponentStablePieceCount;
	private int ownCorners;
	private int opponentCorners;
	private int ownXcells;
	private int opponentXcells;
	private int ownCcells;
	private int opponentCcells;
	private int ownABcells;
	private int opponentABcells;
	private boolean gameEnd;

	public BoardEvaluation(Board board, BoardCellColor color) {
		ownMobility = board.getValidMoves(color).size();
		opponentMobility = board.getValidMoves(color.opposite()).size();
		gameEnd = ownMobility == 0 && opponentMobility == 0;
		
		BoardCellSet border = board.getBorder();
		ownBorderPieceCount = border.piecesOfColor(color).size();
		opponentBorderPieceCount = border.piecesOfColor(color.opposite()).size();
		
		BoardCellSet allPieces = board.getAllPieces();
		ownPieceCount = allPieces.piecesOfColor(color).size();
		opponentPieceCount = allPieces.piecesOfColor(color.opposite()).size();
		
		BoardCellSet corners = board.getCellsOfType(BoardCell.Type.CORNER);
		ownCorners = corners.piecesOfColor(color).size();
		opponentCorners = corners.piecesOfColor(color.opposite()).size();
		
		BoardCellSet xcells = board.getCellsOfType(BoardCell.Type.X);
		ownXcells = xcells.piecesOfColor(color).size();
		opponentXcells = xcells.piecesOfColor(color.opposite()).size();

		BoardCellSet ccells = board.getCellsOfType(BoardCell.Type.C);
		ownCcells = ccells.piecesOfColor(color).size();
		opponentCcells = ccells.piecesOfColor(color.opposite()).size();
		
		BoardCellSet acells = board.getCellsOfType(BoardCell.Type.A);
		BoardCellSet bcells = board.getCellsOfType(BoardCell.Type.B);
		bcells.addAll(acells);
		ownABcells = bcells.piecesOfColor(color).size();
		opponentABcells = bcells.piecesOfColor(color.opposite()).size();
	}
	
	public int getValue() {
		if(gameEnd) return 10000000 * (ownPieceCount - opponentPieceCount);
		
		return 10 * ownMobility - 86 * opponentMobility
			- 30 * ownBorderPieceCount + 25 * opponentBorderPieceCount +
			(ownPieceCount - opponentPieceCount) * 2 * (38 - (ownPieceCount + opponentPieceCount)) +
			30000 * ownCorners - 30000 * opponentCorners +
			200 * opponentXcells - 200 * ownXcells +
			10 * opponentCcells - 190 * ownCcells + 
			50 * ownABcells - 50 * opponentABcells;
	}
	
	public String getHTMLString() {
		return "<html>" +
			String.format("MOBILITY: own=%d, opp=%d<br>", ownMobility, opponentMobility) +
			String.format("BORDER PIECES: own=%d, opp=%d<br>", ownBorderPieceCount, opponentBorderPieceCount) +
			String.format("PIECE COUNT: own=%d, opp=%d<br>", ownPieceCount, opponentPieceCount) +
			String.format("CORNERS: own=%d, opp=%d<br>", ownCorners, opponentCorners) +
			String.format("STABLE PIECES: own=%d, opp=%d<br>", ownStablePieceCount, opponentStablePieceCount) +
			"<br>" +
			"<b>Heuristic value: " + getValue() + "</b>" + 
			"</html>";
	}
	
	public static Map<BoardCell, BoardEvaluation> evaluateAllMoves(Board board, BoardCellColor turn) {
		BoardCellSet validMoves = board.getValidMoves(turn);
		
		Map<BoardCell, BoardEvaluation> allHeuristics = new HashMap<BoardCell, BoardEvaluation>();
		
		Integer hMin = null, hMax = null;
		
		for(BoardCell move : validMoves) {
			Board b = board.clone();
			if(b.makeMove(move, turn)) {
				BoardEvaluation eval = new BoardEvaluation(b, turn);
				allHeuristics.put(move, eval);
				
				if(hMin == null) hMin = eval.getValue();
				else if(eval.getValue() < hMin) hMin = eval.getValue();
				if(hMax == null) hMax = eval.getValue();
				else if(eval.getValue() > hMax) hMax = eval.getValue();
			}
		}
		
		if(!validMoves.isEmpty()) {
			int treshold = hMin + (hMax - hMin) * 20 / 100;
			for(BoardEvaluation eval : allHeuristics.values()) {
				if(eval.getValue() < treshold) {
					//eval.flag = true;
				}
			}
		}
		
		return allHeuristics;
	}
}
