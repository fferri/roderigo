package othello;

import java.util.Iterator;


/**
 * This class computes the discs that are stable FOR SURE.
 * It is incomplete, i.e. it may not neturn ALL stable discs.
 * 
 * @author Federico Ferri
 *
 */
public class StableDiscs {
	private boolean mask[][][];

	private final OthelloBoard othelloBoard;
	
	public StableDiscs(OthelloBoard othelloBoard) {
		this.mask = new boolean[2][othelloBoard.getNumRows()][othelloBoard.getNumCols()];
		this.othelloBoard = othelloBoard;
	}
	
	public boolean get(OthelloBoard.Color player, OthelloBoard.Position position) {
		return mask[othelloBoard.colorIndex(player)][position.getRow()][position.getCol()];
	}
	
	public void set(OthelloBoard.Color player, OthelloBoard.Position position, boolean value) {
		mask[othelloBoard.colorIndex(player)][position.getRow()][position.getCol()] = value;
	}
	
	public void update() {
		update(OthelloBoard.BLACK);
		update(OthelloBoard.WHITE);
	}
	
	protected void update(OthelloBoard.Color player) {
		boolean changed;
		OthelloBoard.Color opp = othelloBoard.colorFlip(player);		
		do {
			changed = false;
			int N[] = {-1,0}, S[] = {1,0}, E[] = {0,1}, W[] = {0,-1};
			
			for(Iterator<OthelloBoard.Position> i = othelloBoard.positionIterator(); i.hasNext(); ) {
				OthelloBoard.Position position = i.next();
				boolean old = get(player, position);
				if(old == true) continue;
				if(!player.equals(othelloBoard.get(position))) continue;
				if(othelloBoard.positionIsCorner(position)) {
					set(player, position, true);
				} else if(othelloBoard.positionIsVSide(position)) {
					OthelloBoard.Position n = othelloBoard.positionIncr(position, N),
							s = othelloBoard.positionIncr(position, S);
					if(get(player, n) || get(player, s)) set(player, position, true);
					if(get(opp, n) && get(opp, s)) set(player, position, true);
				} else if(othelloBoard.positionIsHSide(position)) {
					OthelloBoard.Position e = othelloBoard.positionIncr(position, E),
							w = othelloBoard.positionIncr(position, W);
					if(get(player, e) || get(player, w)) set(player, position, true);
					if(get(opp, e) && get(opp, w)) set(player, position, true);
				} else {
					boolean s[] = new boolean[8];
					
					int j = 0;
					for(int direction[] : othelloBoard.getAllDirections())
						s[j++] = get(player, othelloBoard.positionIncr(position, direction));
					// stable if there are 4 consecutive adjacent stables
					for(j = 0; j < 8; j++) {
						if(s[j] && s[(j + 1) % 8] && s[(j + 2) % 8] && s[(j + 3) % 8]) {
							set(player, position, true);
							break;
						}
					}
					// or every opponent's adjacent disc is stable:
					boolean stab2 = true;
					for(int direction[] : othelloBoard.getAllDirections())
						stab2 = stab2 && get(opp, othelloBoard.positionIncr(position, direction));
					if(stab2) set(player, position, true);
				}
				if(old != get(player, position)) changed = true;
			}
		} while(changed);
	}
}
