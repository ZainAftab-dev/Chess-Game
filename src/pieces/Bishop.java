package pieces;

import board.Board;

public class Bishop extends Piece {
    public Bishop(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int newX, int newY, Board board) {
        if (!isValidTarget(newX, newY, board)) return false;
        
        int dx = Math.abs(newX - x);
        int dy = Math.abs(newY - y);
        
        if (dx != dy) return false; // Must move diagonally
        if (!isPathClear(newX, newY, board)) return false;
        
        return true;
    }

    @Override
    public String getSymbol() {
        return isWhite ? "♗" : "♝";
    }
}
