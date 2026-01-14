package pieces;

import board.Board;

/**
 * Abstract base class for all chess pieces.
 * Defines the common structure and methods for piece behavior.
 */
public abstract class Piece {
    protected int x;
    protected int y;
    protected boolean isWhite;
    protected boolean hasMoved; // Track if piece has moved (for castling/pawn first move)

    public Piece(int x, int y, boolean isWhite) {
        this.x = x;
        this.y = y;
        this.isWhite = isWhite;
        this.hasMoved = false;
    }

    /**
     * Determines if a move from current position to (newX, newY) is valid.
     */
    public abstract boolean isValidMove(int newX, int newY, Board board);

    /**
     * Checks if the path between two squares is clear (for pieces that need line of sight).
     */
    protected boolean isPathClear(int newX, int newY, Board board) {
        int dx = Integer.compare(newX, x);
        int dy = Integer.compare(newY, y);
        int currentX = x + dx;
        int currentY = y + dy;

        while (currentX != newX || currentY != newY) {
            if (board.getPiece(currentX, currentY) != null) {
                return false;
            }
            currentX += dx;
            currentY += dy;
        }
        return true;
    }

    /**
     * Checks if a target square is empty or occupied by an opponent's piece.
     */
    protected boolean isValidTarget(int newX, int newY, Board board) {
        if (newX < 0 || newX > 7 || newY < 0 || newY > 7) {
            return false;
        }
        Piece targetPiece = board.getPiece(newX, newY);
        return targetPiece == null || targetPiece.isWhite != this.isWhite;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isWhite() { return isWhite; }
    public boolean getHasMoved() { return hasMoved; }

    // Setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setHasMoved(boolean moved) { this.hasMoved = moved; }

    public abstract String getSymbol();
}
