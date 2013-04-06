package othello.gui;

import game.GameController;
import game.player.AIMinMaxPlayer;
import game.player.AbstractPlayer;
import game.player.HumanPlayer;

import java.util.concurrent.SynchronousQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import othello.OthelloBoard;
import othello.OthelloGame;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	static OthelloBoard board;
	static SynchronousQueue<OthelloBoard.Action> queue = new SynchronousQueue<>();
	static OthelloGame game = new OthelloGame();
	static AbstractPlayer<OthelloBoard, OthelloBoard.Action, OthelloBoard.Color, OthelloBoard.Position> aiPlayer, humanPlayer;
	static GameController<OthelloBoard, OthelloBoard.Action, OthelloBoard.Color, OthelloBoard.Position> controller;
	
	BoardWidget boardWidget;
	
	public MainWindow() {
		super("Roderigo2");
		
		boardWidget = new BoardWidget();
		boardWidget.setBoard(board);
		boardWidget.addCellClickListener(new BoardWidget.CellClickListener() {
			public void cellClicked(OthelloBoard.Position position, boolean leftButton) {
				if(humanPlayer != null && humanPlayer.getColor().equals(board.getTurn()))
					if(queue.isEmpty())
						queue.add(leftButton ? board.action(position) : board.actionPass());
			}
		});
		
		add(boardWidget);
		
		pack();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static final void main(String[] args) {
		board = new OthelloBoard();
		humanPlayer = new HumanPlayer<OthelloBoard, OthelloBoard.Action, OthelloBoard.Color, OthelloBoard.Position>(OthelloBoard.BLACK, "human", queue);
		aiPlayer = new AIMinMaxPlayer<OthelloBoard, OthelloBoard.Action, OthelloBoard.Color, OthelloBoard.Position>(OthelloBoard.WHITE, "ai", 4);
		//aiPlayer = new RandomPlayer<OthelloBoard, OthelloBoard.Action, OthelloBoard.Color, OthelloBoard.Position>(OthelloBoard.WHITE, "ai");
		//aiPlayer = new AIQPolicyPlayer<OthelloBoard, OthelloBoard.Action, OthelloBoard.Color, OthelloBoard.Position>("ai", QLearning.loadQTable(new File("q.dat"), new Persistence()));
		controller = new GameController<OthelloBoard, OthelloBoard.Action, OthelloBoard.Color, OthelloBoard.Position>(game);
		controller.addPlayer(aiPlayer);
		controller.addPlayer(humanPlayer);

		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow();
            }
        });
		
		controller.play(board);
	}
}
