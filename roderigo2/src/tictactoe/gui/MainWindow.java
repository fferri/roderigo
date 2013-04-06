package tictactoe.gui;

import game.GameController;
import game.ai.rl.AbstractQTable;
import game.ai.rl.QLearning;
import game.ai.rl.QTableNeuralNet;
import game.player.AIMinMaxPlayer;
import game.player.AIQPolicyPlayer;
import game.player.AbstractPlayer;
import game.player.HumanPlayer;
import game.player.RandomPlayer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import tictactoe.TicTacToeBoard;
import tictactoe.TicTacToeGame;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	static TicTacToeBoard board;
	static TicTacToeGame game = new TicTacToeGame();
	static GameController<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> controller;
	static SynchronousQueue<TicTacToeBoard.Action> queue = new SynchronousQueue<>();
	static AbstractPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> humanPlayer;
	static AbstractPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> aiPlayer;
	
	BoardWidget boardWidget;
	
	public MainWindow() {
		super("Roderigo2");
		
		boardWidget = new BoardWidget();
		boardWidget.setBoard(board);
		boardWidget.addCellClickListener(new BoardWidget.CellClickListener() {
			public void cellClicked(TicTacToeBoard.Position p, boolean leftButton) {
				if(humanPlayer != null && humanPlayer.getColor().equals(board.getTurn()))
					if(queue.isEmpty())
						queue.add(leftButton ? board.action(p) : board.actionPass());
			}
		});
		
		add(boardWidget);
		
		pack();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	@SuppressWarnings("unused")
	public static final void main(String[] args) throws IOException {
		board = new TicTacToeBoard();
		//if(Math.random() > 0.5)
		//	board.flipTurn();
		
		AbstractQTable<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> qtable = new QTableNeuralNet<>(9, 6);
		QLearning<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> qlearning = new QLearning<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>(game, TicTacToeBoard.CIRCLE, qtable);
		qlearning.load(new File("tictactoe.dat"));
		//qlearning.getQTable().dump(true, Integer.MAX_VALUE);
		
		humanPlayer = new HumanPlayer<>(TicTacToeBoard.CROSS, "human", queue);
		AIMinMaxPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> minMax = new AIMinMaxPlayer<>(TicTacToeBoard.CIRCLE, "ai", 3);
		RandomPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> rand = new RandomPlayer<>(TicTacToeBoard.CIRCLE, "ai");
		AIQPolicyPlayer<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position> qpolicy = new AIQPolicyPlayer<>("ai", qlearning);
		
		controller = new GameController<TicTacToeBoard, TicTacToeBoard.Action, TicTacToeBoard.Color, TicTacToeBoard.Position>(game);
		controller.addPlayer(aiPlayer = qpolicy);
		controller.addPlayer(humanPlayer);

		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow();
            }
        });
		
		controller.play(board);
	}
}
