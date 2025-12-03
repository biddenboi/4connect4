import java.io.*;
import java.net.Socket;

public class PlayerHandler implements Runnable {

    private Connect4Server server;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int playerNumber;  // 1 or 2
    private boolean running = true;

    public PlayerHandler(Connect4Server server, Socket socket, int playerNumber) {
        this.server = server;
        this.socket = socket;
        this.playerNumber = playerNumber;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Message msg = (Message) in.readObject();

                if (msg.type.equals("MOVE")) {
                    server.handleMove(this, msg.column);
                } else if (msg.type.equals("RESTART")) {
                    server.handleRestartRequest(this);
                }
            }
        } catch (Exception e) {
            // client disconnected
        } finally {
            running = false;
            server.handleDisconnect(this);
            close();
        }
    }

    public void sendWelcome() {
        Message m = new Message("WELCOME");
        m.currentPlayer = playerNumber; 
        m.text = "Welcome! You are Player " + playerNumber + ".";
        send(m);
    }

    public void sendInvalidMove(String text) {
        Message m = new Message("INVALID_MOVE");
        m.text = text;
        send(m);
    }

    public void sendInfo(String text) {
        Message m = new Message("INFO");
        m.text = text;
        send(m);
    }

    public void send(Message m) {
        try {
            out.writeObject(m);
            out.reset();
        } catch (IOException e) {
            // ignore; probably disconnected
        }
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}