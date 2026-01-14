package board;

import pieces.*;

/**
 * Manages the 8x8 chess board and piece positions.
 */
public class Board {
    private Piece[][] grid;
    private static final int SIZE = 8;
    private int lastMoveFromY = -1;
    private int lastMoveToY = -1;
    private int lastMoveFromX = -1;
    private int lastMoveToX = -1;

    public Board() {
        grid = new Piece[SIZE][SIZE];
        initializeBoard();
    }

    /**
     * Initialize the board with standard chess starting position.
     */
    private void initializeBoard() {
        // Clear board
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = null;
            }
        }

        // White pieces (bottom)
        grid[7][0] = new Rook(7, 0, true);
        grid[7][1] = new Knight(7, 1, true);
        grid[7][2] = new Bishop(7, 2, true);
        grid[7][3] = new Queen(7, 3, true);
        grid[7][4] = new King(7, 4, true);
        grid[7][5] = new Bishop(7, 5, true);
        grid[7][6] = new Knight(7, 6, true);
        grid[7][7] = new Rook(7, 7, true);

        for (int j = 0; j < SIZE; j++) {
            grid[6][j] = new Pawn(6, j, true);
        }

        // Black pieces (top)
        grid[0][0] = new Rook(0, 0, false);
        grid[0][1] = new Knight(0, 1, false);
        grid[0][2] = new Bishop(0, 2, false);
        grid[0][3] = new Queen(0, 3, false);
        grid[0][4] = new King(0, 4, false);
        grid[0][5] = new Bishop(0, 5, false);
        grid[0][6] = new Knight(0, 6, false);
        grid[0][7] = new Rook(0, 7, false);

        for (int j = 0; j < SIZE; j++) {
            grid[1][j] = new Pawn(1, j, false);
        }
    }

    public Piece getPiece(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return null;
        return grid[x][y];
    }

    public void setPiece(int x, int y, Piece piece) {
        grid[x][y] = piece;
    }

    public void movePiece(int fromX, int fromY, int toX, int toY) {
        Piece piece = grid[fromX][fromY];
        if (piece != null) {
            if (piece instanceof King && Math.abs(toY - fromY) == 2) {
                int rookFromY = toY > fromY ? 7 : 0;
                int rookToY = toY > fromY ? 5 : 3;
                Piece rook = grid[fromX][rookFromY];
                if (rook != null) {
                    rook.setX(fromX);
                    rook.setY(rookToY);
                    rook.setHasMoved(true);
                    grid[fromX][rookFromY] = null;
                    grid[fromX][rookToY] = rook;
                }
            }
            
            if (piece instanceof Pawn && grid[toX][toY] == null && fromY != toY) {
                grid[fromX][toY] = null; // Remove en passant victim
            }
            
            piece.setX(toX);
            piece.setY(toY);
            piece.setHasMoved(true);
            grid[toX][toY] = piece;
            grid[fromX][fromY] = null;
            
            lastMoveFromX = fromX;
            lastMoveFromY = fromY;
            lastMoveToX = toX;
            lastMoveToY = toY;
        }
    }

    public void reset() {
        initializeBoard();
        lastMoveFromY = -1;
        lastMoveToY = -1;
        lastMoveFromX = -1;
        lastMoveToX = -1;
    }

    public int getSize() {
        return SIZE;
    }

    public Piece[][] getGrid() {
        return grid;
    }

    /**
     * Check if en passant is valid for a pawn at (fromX, captureY).
     */
    public boolean isValidEnPassant(int fromX, int captureY) {
        if (lastMoveToX >= 0 && lastMoveToX < 8 && lastMoveToY >= 0 && lastMoveToY < 8) {
            if (lastMoveFromX == fromX + (fromX < 4 ? 1 : -1) && lastMoveToY == captureY) {
                Piece lastMovedPiece = grid[lastMoveToX][lastMoveToY];
                return lastMovedPiece instanceof Pawn && Math.abs(lastMoveFromX - lastMoveToX) == 2;
            }
        }
        return false;
    }

    /**
     * Create a deep copy of the board for AI evaluation.
     */
    public Board copy() {
        Board newBoard = new Board();
        newBoard.lastMoveFromX = this.lastMoveFromX;
        newBoard.lastMoveFromY = this.lastMoveFromY;
        newBoard.lastMoveToX = this.lastMoveToX;
        newBoard.lastMoveToY = this.lastMoveToY;
        
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] != null) {
                    Piece original = grid[i][j];
                    Piece copy = copyPiece(original);
                    newBoard.grid[i][j] = copy;
                }
            }
        }
        return newBoard;
    }

    private Piece copyPiece(Piece p) {
        Piece copy = null;
        if (p instanceof Pawn) copy = new Pawn(p.getX(), p.getY(), p.isWhite());
        else if (p instanceof Rook) copy = new Rook(p.getX(), p.getY(), p.isWhite());
        else if (p instanceof Knight) copy = new Knight(p.getX(), p.getY(), p.isWhite());
        else if (p instanceof Bishop) copy = new Bishop(p.getX(), p.getY(), p.isWhite());
        else if (p instanceof Queen) copy = new Queen(p.getX(), p.getY(), p.isWhite());
        else if (p instanceof King) copy = new King(p.getX(), p.getY(), p.isWhite());
        
        if (copy != null) copy.setHasMoved(p.getHasMoved());
        return copy;
    }
}
