package pieces;

import board.Board;

public class Queen extends Piece {
    public Queen(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int newX, int newY, Board board) {
        if (!isValidTarget(newX, newY, board)) return false;
        
        int dx = Math.abs(newX - x);
        int dy = Math.abs(newY - y);
        
        // Queen moves like rook or bishop
        if ((x == newX || y == newY) || (dx == dy)) {
            if (!isPathClear(newX, newY, board)) return false;
            return true;
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return isWhite ? "♕" : "♛";
    }
}
