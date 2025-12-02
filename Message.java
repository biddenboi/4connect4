import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public String type;
    public int column;
    public Board board;
    public int currentPlayer; 
    public String text;
    public int countdown;

    public Message(String type) {
        this.type = type;
    }
}