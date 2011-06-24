package roderigo.ai;

import java.util.HashMap;
import java.util.Map;

import roderigo.ai.genetic.Genome;
import roderigo.ai.genetic.Genome.Bit;
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
	private int value[];
	
	private boolean gameEnd;

	public BoardEvaluation(Board board, BoardCellColor color) {
		int n = Genome.Bit.values().length;
		value = new int[n];
		
		value[Genome.Bit.ownMobility.ordinal()] = board.getValidMoves(color).size();
		value[Genome.Bit.opponentMobility.ordinal()] = board.getValidMoves(color.opposite()).size();
		
		BoardCellSet border = board.getBorder();
		value[Genome.Bit.ownBorderPieceCount.ordinal()] = border.piecesOfColor(color).size();
		value[Genome.Bit.opponentBorderPieceCount.ordinal()] = border.piecesOfColor(color.opposite()).size();
		
		BoardCellSet allPieces = board.getAllPieces();
		value[Genome.Bit.ownPieceCount.ordinal()] = allPieces.piecesOfColor(color).size();
		value[Genome.Bit.opponentPieceCount.ordinal()] = allPieces.piecesOfColor(color.opposite()).size();
		
		BoardCellSet corners = board.getCellsOfType(BoardCell.Type.CORNER);
		value[Genome.Bit.ownCorners.ordinal()] = corners.piecesOfColor(color).size();
		value[Genome.Bit.opponentCorners.ordinal()] = corners.piecesOfColor(color.opposite()).size();
		
		BoardCellSet xcells = board.getCellsOfType(BoardCell.Type.X);
		value[Genome.Bit.ownXcells.ordinal()] = xcells.piecesOfColor(color).size();
		value[Genome.Bit.opponentXcells.ordinal()] = xcells.piecesOfColor(color.opposite()).size();

		BoardCellSet ccells = board.getCellsOfType(BoardCell.Type.C);
		value[Genome.Bit.ownCcells.ordinal()] = ccells.piecesOfColor(color).size();
		value[Genome.Bit.opponentCcells.ordinal()] = ccells.piecesOfColor(color.opposite()).size();
		
		BoardCellSet abcells = board.getCellsOfType(BoardCell.Type.B); abcells.addAll(board.getCellsOfType(BoardCell.Type.A));
		value[Genome.Bit.ownABcells.ordinal()] = abcells.piecesOfColor(color).size();
		value[Genome.Bit.opponentABcells.ordinal()] = abcells.piecesOfColor(color.opposite()).size();
		
		gameEnd = value[Genome.Bit.ownMobility.ordinal()] == 0 && value[Genome.Bit.opponentMobility.ordinal()] == 0;
	}
	
	public int getValue() {
		return getValue(Genome.DEFAULT);
	}
	
	public int getValue(Genome g) {
		if(gameEnd) {
			return 10000000 * (value[Genome.Bit.ownPieceCount.ordinal()] - value[Genome.Bit.opponentPieceCount.ordinal()]);
		} else {
			int v = 0;
			for(Bit bit : Genome.Bit.values())
				v += value[bit.ordinal()] * g.get(bit);
			return v;
		}
	}
	
	public String getHTMLString() {
		return "<html>" +
			String.format("MOBILITY: own=%d, opp=%d<br>", value[Genome.Bit.ownMobility.ordinal()], value[Genome.Bit.opponentMobility.ordinal()]) +
			String.format("BORDER PIECES: own=%d, opp=%d<br>", value[Genome.Bit.ownBorderPieceCount.ordinal()], value[Genome.Bit.opponentBorderPieceCount.ordinal()]) +
			String.format("PIECE COUNT: own=%d, opp=%d<br>", value[Genome.Bit.ownPieceCount.ordinal()], value[Genome.Bit.opponentPieceCount.ordinal()]) +
			String.format("CORNERS: own=%d, opp=%d<br>", value[Genome.Bit.ownCorners.ordinal()], value[Genome.Bit.opponentCorners.ordinal()]) +
			String.format("STABLE PIECES: own=%d, opp=%d<br>", value[Genome.Bit.ownStablePieceCount.ordinal()], value[Genome.Bit.opponentStablePieceCount.ordinal()]) +
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
				
				int value = eval.getValue();
				
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
