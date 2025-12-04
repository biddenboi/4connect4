import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class Connect4Client extends JFrame {

    private static final int PORT = 8000;
    private static final String HOST = "127.0.0.1";

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private Board board = new Board();
    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private JButton restartButton;

    private int myPlayerNumber = 0;
    private int currentPlayer = 1;
    private boolean myTurn = false;
    private boolean gameOver = false;

    public Connect4Client() {
        super("Connect 4 Client");
        setupGUI();
        connectToServer();
        Thread t = new Thread(new Listener());
        t.start();
    }

    private void setupGUI() {
        boardPanel = new BoardPanel();
        statusLabel = new JLabel("Connecting...", JLabel.CENTER);
        restartButton = new JButton("Restart");

        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (gameOver) {
                    sendRestartRequest();
                } else {
                    JOptionPane.showMessageDialog(null, "You can only restart after the game is over.");
                }
            }
        });

        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.SOUTH);
        add(boardPanel, BorderLayout.CENTER);
        add(restartButton, BorderLayout.NORTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server.");
            System.exit(0);
        }
    }

    private void sendMove(int column) {
        if (!myTurn || gameOver) return;
        try {
            Message m = new Message("MOVE");
            m.column = column;
            out.writeObject(m);
            out.reset();
        } catch (IOException e) {}
    }

    private void sendRestartRequest() {
        try {
            Message m = new Message("RESTART");
            out.writeObject(m);
            out.reset();
        } catch (IOException e) {}
    }

    private class Listener implements Runnable {
        public void run() {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    handleMessage(msg);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Disconnected from server.");
                System.exit(0);
            }
        }
    }

    private void handleMessage(Message msg) {
        if (msg.type.equals("WELCOME")) {
            myPlayerNumber = msg.currentPlayer;
            statusLabel.setText(msg.text);
        } else if (msg.type.equals("STATE")) {
            board = msg.board;
            currentPlayer = msg.currentPlayer;
            gameOver = false;
            myTurn = (currentPlayer == myPlayerNumber);
            statusLabel.setText(msg.text + " You are Player " + myPlayerNumber + ".");
            boardPanel.repaint();
        } else if (msg.type.equals("INVALID_MOVE")) {
            JOptionPane.showMessageDialog(this, msg.text);
        } else if (msg.type.equals("INFO")) {
            statusLabel.setText(msg.text);
        } else if (msg.type.equals("GAME_OVER")) {
            board = msg.board;
            boardPanel.repaint();
            gameOver = true;
            myTurn = false;
            if (msg.currentPlayer == myPlayerNumber) {
                JOptionPane.showMessageDialog(this, "You win!");
            } else {
                JOptionPane.showMessageDialog(this, "You lose. " + msg.text);
            }
            statusLabel.setText(msg.text + " Press restart to play again.");
        } else if (msg.type.equals("TIE")) {
            board = msg.board;
            boardPanel.repaint();
            gameOver = true;
            myTurn = false;
            JOptionPane.showMessageDialog(this, "It's a tie.");
            statusLabel.setText("Tie game. Press restart to play again.");
        } else if (msg.type.equals("RESTART")) {
            board = msg.board;
            boardPanel.repaint();
            gameOver = false;
            currentPlayer = msg.currentPlayer;
            myTurn = (currentPlayer == myPlayerNumber);
            statusLabel.setText(msg.text + " You are Player " + myPlayerNumber + ".");
        } else if (msg.type.equals("OPPONENT_DISCONNECTED")) {
            gameOver = true;
            myTurn = false;
            statusLabel.setText(msg.text);
        } else if (msg.type.equals("COUNTDOWN")) {
            statusLabel.setText(msg.text);
        }
    }

    class BoardPanel extends JPanel implements MouseListener {
        private static final int DIAMETER = 80;
        private static final int PADDING = 20;

        public BoardPanel() {
            addMouseListener(this);
        }

        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(new Color(50, 90, 130));
            g.fillRect(0, 0, getWidth(), getHeight());

            int rows = Board.ROWS;
            int cols = Board.COLS;
            int totalWidth = cols * DIAMETER + (cols - 1) * PADDING;
            int totalHeight = rows * DIAMETER + (rows - 1) * PADDING;
            int startX = (getWidth() - totalWidth) / 2;
            int startY = (getHeight() - totalHeight) / 2;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = startX + c * (DIAMETER + PADDING);
                    int y = startY + r * (DIAMETER + PADDING);
                    int cell = board.getCell(r, c);

                    if (cell == 0) g.setColor(Color.BLACK);
                    else if (cell == 1) g.setColor(Color.RED);
                    else g.setColor(Color.YELLOW);

                    g.fillOval(x, y, DIAMETER, DIAMETER);
                }
            }
        }

        public void mouseClicked(MouseEvent e) {
            if (!myTurn || gameOver) return;
            int cols = Board.COLS;
            int rows = Board.ROWS;
            int totalWidth = cols * DIAMETER + (cols - 1) * PADDING;
            int totalHeight = rows * DIAMETER + (rows - 1) * PADDING;
            int startX = (getWidth() - totalWidth) / 2;
            int startY = (getHeight() - totalHeight) / 2;
            int x = e.getX();
            int y = e.getY();

            if (y < startY || y > startY + totalHeight) return;
            if (x < startX || x > startX + totalWidth) return;

            int col = (x - startX) / (DIAMETER + PADDING);
            if (col < 0) col = 0;
            if (col >= Board.COLS) col = Board.COLS - 1;

            sendMove(col);
        }

        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    }

    public static void main(String[] args) {
        Connect4Client client = new Connect4Client();
    }
}
