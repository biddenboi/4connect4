import java.io.Serializable;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int ROWS = 6;
    public static final int COLS = 7;

    private int[][] grid = new int[ROWS][COLS];

    public Board() {
        clear();
    }

    public void clear() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = 0;
            }
        }
    }

    public int getCell(int r, int c) {
        return grid[r][c];
    }

    public boolean isColumnFull(int col) {
        return grid[0][col] != 0;
    }

    public boolean isFull() {
        for (int c = 0; c < COLS; c++) {
            if (!isColumnFull(c)) {
                return false;
            }
        }
        return true;
    }


    public int dropPiece(int player, int col) {
        if (col < 0 || col >= COLS) return -1;

        for (int r = ROWS - 1; r >= 0; r--) {
            if (grid[r][col] == 0) {
                grid[r][col] = player;
                return r;
            }
        }
        return -1;
    }

    public boolean checkWin(int player) {

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (grid[r][c] == player && grid[r][c + 1] == player &&
                    grid[r][c + 2] == player && grid[r][c + 3] == player) {
                    return true;
                }
            }
        }

        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r <= ROWS - 4; r++) {
                if (grid[r][c] == player && grid[r + 1][c] == player &&
                    grid[r + 2][c] == player && grid[r + 3][c] == player) {
                    return true;
                }
            }
        }

        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (grid[r][c] == player && grid[r + 1][c + 1] == player &&
                    grid[r + 2][c + 2] == player && grid[r + 3][c + 3] == player) {
                    return true;
                }
            }
        }

        for (int r = 3; r < ROWS; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (grid[r][c] == player && grid[r - 1][c + 1] == player &&
                    grid[r - 2][c + 2] == player && grid[r - 3][c + 3] == player) {
                    return true;
                }
            }
        }

        return false;
    }
}