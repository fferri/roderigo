package roderigo2.tests;

import static org.junit.Assert.*;

import java.util.List;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;

import org.junit.Test;

import othello.OthelloGame;
import tictactoe.TicTacToeGame;

public class TestBoardSerializer {
	@Test
	public void testTicTacToe() {
		if(!test(new TicTacToeGame(), 1000))
			fail();
	}
	
	@Test
	public void testOthello() {
		if(!test(new OthelloGame(), 1000))
			fail();
	}
	
	public static <S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition>
	S randomState(Game<S, A, C, P> game) {
		S s0 = game.getInitialState();
		int n = (int)(Math.random() * s0.getNumRows() * s0.getNumCols());
		while(n-- > 0) {
			List<A> a = game.getAvailableActions(s0);
			game.executeInPlace(s0, a.get((int)(Math.random() * a.size())));
		}
		return s0;
	}
	
	public static <S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition>
	boolean test(Game<S, A, C, P> game, int numTests) {
		while(numTests-- > 0) {
			S s0 = randomState(game);
			String s0str = game.stateToString(s0);
			S s1 = game.stateFromString(s0str);
			if(!s0.equals(s1)) {
				System.out.println("Failed: " + s0str);
				System.out.println("s0:");
				System.out.println(s0);
				System.out.println("s1:");
				System.out.println(s1);
				return false;
			}
		}
		return true;
	}
}
