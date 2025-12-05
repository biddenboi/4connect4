import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class Connect4Server {

    private Board board = new Board();
    private PlayerHandler player1;
    private PlayerHandler player2;
    private int currentPlayer = 1;
    private boolean gameOver = false;

    public static void main(String[] args) {
        new Connect4Server().startServer();
    }

    public void startServer() {
        try {
            System.out.println("server start");
            try (ServerSocket serverSocket = new ServerSocket(8000)) {
                Socket p1Socket = serverSocket.accept();
                System.out.println("p1c");
                player1 = new PlayerHandler(this, p1Socket, 1);
                new Thread(player1).start();

                Socket p2Socket = serverSocket.accept();
                System.out.println("p2c");
                player2 = new PlayerHandler(this, p2Socket, 2);
            }

            new Thread(player2).start();

            player1.sendWelcome();
            player2.sendWelcome();

            broadcastState("Player 1's turn.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void handleMove(PlayerHandler player, int column) {
        if (gameOver) {
            player.sendInfo("Game is over. Press restart.");
            return;
        }

        if (player.getPlayerNumber() != currentPlayer) {
            player.sendInfo("It is not your turn!");
            return;
        }

        if (column < 0 || column >= Board.COLS) {
            player.sendInvalidMove("Invalid column.");
            return;
        }

        if (board.isColumnFull(column)) {
            player.sendInvalidMove("Column is full.");
            return;
        }

        board.dropPiece(currentPlayer, column);

        if (board.checkWin(currentPlayer)) {
            gameOver = true;
            Message m = new Message("GAME_OVER");
            m.board = board;
            m.currentPlayer = currentPlayer; 
            m.text = "Player " + currentPlayer + " wins!";
            broadcast(m);
            return;
        }

        if (board.isFull()) {
            gameOver = true;
            Message m = new Message("TIE");
            m.board = board;
            m.text = "The game is a tie.";
            broadcast(m);
            return;
        }

        //MARK SWITCH PLAYER MOVE
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        broadcastState("Player " + currentPlayer + "'s turn.");
    }

    public synchronized void handleRestartRequest(PlayerHandler player) {
        if (!gameOver) {
            player.sendInfo("You can only restart after the game is over.");
            return;
        }
        board.clear();
        gameOver = false;
        currentPlayer = 1;

        Message m = new Message("RESTART");
        m.board = board;
        m.currentPlayer = currentPlayer;
        m.text = "Game restarted. Player 1 goes first.";
        broadcast(m);
    }

    public synchronized void handleDisconnect(PlayerHandler whoDisconnected) {
        System.out.println("Player " + whoDisconnected.getPlayerNumber() + " disconnected.");

        PlayerHandler other = (whoDisconnected == player1) ? player2 : player1;
        if (other == null) return;

        try {
            Message info = new Message("OPPONENT_DISCONNECTED");
            info.text = "Your opponent disconnected. Closing in 5 seconds.";
            other.send(info);

            for (int i = 5; i >= 1; i--) {
                Message countdown = new Message("COUNTDOWN");
                countdown.countdown = i;
                countdown.text = "Closing in " + i + " seconds...";
                other.send(countdown);
                Thread.sleep(1000);
            }

            Message finalMsg = new Message("INFO");
            finalMsg.text = "Server closing connection.";
            other.send(finalMsg);

            other.close();

        } catch (Exception e) {
        }
    }

    public synchronized void broadcastState(String statusText) {
        Message m = new Message("STATE");
        m.board = board;
        m.currentPlayer = currentPlayer;
        m.text = statusText;
        broadcast(m);
    }

    public synchronized void broadcast(Message m) {
        if (player1 != null) player1.send(m);
        if (player2 != null) player2.send(m);
    }
}