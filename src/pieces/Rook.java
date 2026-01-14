package pieces;

import board.Board;

public class Rook extends Piece {
    public Rook(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int newX, int newY, Board board) {
        if (!isValidTarget(newX, newY, board)) return false;
        if (x != newX && y != newY) return false; // Must move in straight line
        if (!isPathClear(newX, newY, board)) return false;
        return true;
    }

    @Override
    public String getSymbol() {
        return isWhite ? "♖" : "♜";
    }
}
