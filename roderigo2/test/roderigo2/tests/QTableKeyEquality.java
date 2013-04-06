package roderigo2.tests;

import static org.junit.Assert.*;

import game.ai.rl.QTable;
import game.ai.rl.QTableKey;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import tictactoe.TicTacToeBoard;
import tictactoe.TicTacToeGame;

public class QTableKeyEquality {

	@Test
	public void test() throws IOException {
		TicTacToeGame game = new TicTacToeGame();
		TicTacToeBoard s = TestBoardSerializer.randomState(game);
		List<TicTacToeBoard.Action> as = s.getValidActions();
		TicTacToeBoard.Action a = as.get((int)(Math.random() * as.size()));
		QTableKey<TicTacToeBoard, TicTacToeBoard.Action> k = new QTableKey<TicTacToeBoard, TicTacToeBoard.Action>(s, a);
		TicTacToeBoard s1 = (TicTacToeBoard)s.clone();
		TicTacToeBoard.Action a1 = (TicTacToeBoard.Action)a.clone();
		QTableKey<TicTacToeBoard, TicTacToeBoard.Action> k1 = new QTableKey<TicTacToeBoard, TicTacToeBoard.Action>(s1, a1);
		assertEquals("key equals()", true, k.equals(k1));
		assertEquals("key hashCode() equality", true, k.hashCode() == k1.hashCode());
		QTable<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> t = new QTable<>();
		t.set(s, a, 8);
		assertEquals("key hashCode() equality", 8, t.get(s1, a1), 0.001);
		File tmpFile = new File("tmp.dat");
		t.save(tmpFile, game);
		QTable<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> t1 = new QTable<>();
		t1.load(tmpFile, game);
		assertEquals("key hashCode() equality [after deserialize]", 8, t1.get(s1, a1), 0.001);
	}

}
