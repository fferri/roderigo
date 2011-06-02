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
 * <li>"Wall"
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
	public static enum Measure {
		ownMobility,
		opponentMobility,
		ownBorderPieceCount,
		opponentBorderPieceCount,
		ownPieceCount,
		opponentPieceCount,
		ownStablePieceCount,
		opponentStablePieceCount,
		ownCorners,
		opponentCorners,
		ownXcells,
		opponentXcells,
		ownCcells,
		opponentCcells,
		ownABcells,
		opponentABcells
	};
	
	public static final int defaultWeights[] = {
		10, -86, -30, 25, 0, 0, 0, 0, 30000, -30000, -200, 200, -190, 10, 50, -50
	};
	
	private int value[];
	
	private boolean gameEnd;

	public BoardEvaluation(Board board, BoardCellColor color) {
		int n = Measure.values().length;
		value = new int[n];
		
		value[Measure.ownMobility.ordinal()] = board.getValidMoves(color).size();
		value[Measure.opponentMobility.ordinal()] = board.getValidMoves(color.opposite()).size();
		
		BoardCellSet border = board.getBorder();
		value[Measure.ownBorderPieceCount.ordinal()] = border.piecesOfColor(color).size();
		value[Measure.opponentBorderPieceCount.ordinal()] = border.piecesOfColor(color.opposite()).size();
		
		BoardCellSet allPieces = board.getAllPieces();
		value[Measure.ownPieceCount.ordinal()] = allPieces.piecesOfColor(color).size();
		value[Measure.opponentPieceCount.ordinal()] = allPieces.piecesOfColor(color.opposite()).size();
		
		BoardCellSet corners = board.getCellsOfType(BoardCell.Type.CORNER);
		value[Measure.ownCorners.ordinal()] = corners.piecesOfColor(color).size();
		value[Measure.opponentCorners.ordinal()] = corners.piecesOfColor(color.opposite()).size();
		
		BoardCellSet xcells = board.getCellsOfType(BoardCell.Type.X);
		value[Measure.ownXcells.ordinal()] = xcells.piecesOfColor(color).size();
		value[Measure.opponentXcells.ordinal()] = xcells.piecesOfColor(color.opposite()).size();

		BoardCellSet ccells = board.getCellsOfType(BoardCell.Type.C);
		value[Measure.ownCcells.ordinal()] = ccells.piecesOfColor(color).size();
		value[Measure.opponentCcells.ordinal()] = ccells.piecesOfColor(color.opposite()).size();
		
		BoardCellSet abcells = board.getCellsOfType(BoardCell.Type.B); abcells.addAll(board.getCellsOfType(BoardCell.Type.A));
		value[Measure.ownABcells.ordinal()] = abcells.piecesOfColor(color).size();
		value[Measure.opponentABcells.ordinal()] = abcells.piecesOfColor(color.opposite()).size();
		
		gameEnd = value[Measure.ownMobility.ordinal()] == 0 && value[Measure.opponentMobility.ordinal()] == 0;
	}
	
	public int getValue(int weight[]) {
		if(gameEnd) return 10000000 * (value[Measure.ownPieceCount.ordinal()] - value[Measure.opponentPieceCount.ordinal()]);
		
		assert weight != null && weight.length == Measure.values().length;
		
		int v = 0;
		for(int i = 0; i < value.length; i++) v += value[i] * weight[i];
		return v;
		
		/*
		return 10 * ownMobility - 86 * opponentMobility
			- 30 * ownBorderPieceCount + 25 * opponentBorderPieceCount +
			(ownPieceCount - opponentPieceCount) * 2 * (38 - (ownPieceCount + opponentPieceCount)) +
			30000 * ownCorners - 30000 * opponentCorners +
			200 * opponentXcells - 200 * ownXcells +
			10 * opponentCcells - 190 * ownCcells + 
			50 * ownABcells - 50 * opponentABcells;
		*/
	}
	
	public String getHTMLString() {
		return "<html>" +
			String.format("MOBILITY: own=%d, opp=%d<br>", value[Measure.ownMobility.ordinal()], value[Measure.opponentMobility.ordinal()]) +
			String.format("BORDER PIECES: own=%d, opp=%d<br>", value[Measure.ownBorderPieceCount.ordinal()], value[Measure.opponentBorderPieceCount.ordinal()]) +
			String.format("PIECE COUNT: own=%d, opp=%d<br>", value[Measure.ownPieceCount.ordinal()], value[Measure.opponentPieceCount.ordinal()]) +
			String.format("CORNERS: own=%d, opp=%d<br>", value[Measure.ownCorners.ordinal()], value[Measure.opponentCorners.ordinal()]) +
			String.format("STABLE PIECES: own=%d, opp=%d<br>", value[Measure.ownStablePieceCount.ordinal()], value[Measure.opponentStablePieceCount.ordinal()]) +
			"<br>" +
			"<b>Heuristic value: " + getValue(defaultWeights) + "</b>" + 
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
				
				int value = eval.getValue(defaultWeights);
				
				if(hMin == null) hMin = value;
				else if(value < hMin) hMin = value;
				if(hMax == null) hMax = value;
				else if(value > hMax) hMax = value;
			}
		}
		
		/*if(!validMoves.isEmpty()) {
			int treshold = hMin + (hMax - hMin) * 20 / 100;
			for(BoardEvaluation eval : allHeuristics.values()) {
				if(eval.getValue() < treshold) {
					eval.flag = true;
				}
			}
		}*/
		
		return allHeuristics;
	}
}
