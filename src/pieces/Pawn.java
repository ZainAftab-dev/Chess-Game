package pieces;

import board.Board;

public class Pawn extends Piece {
    public Pawn(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int newX, int newY, Board board) {
        if (!isValidTarget(newX, newY, board)) return false;

        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        // Forward move (one square)
        if (newX == x + direction && newY == y && board.getPiece(newX, newY) == null) {
            return true;
        }

        // Forward move (two squares from starting position)
        if (!hasMoved && newX == x + 2 * direction && newY == y && board.getPiece(newX, newY) == null && board.getPiece(x + direction, y) == null) {
            return true;
        }

        // Capture diagonally
        if (newX == x + direction && Math.abs(newY - y) == 1) {
            Piece target = board.getPiece(newX, newY);
            return target != null && target.isWhite != this.isWhite;
        }

        // En passant capture
        if (newX == x + direction && Math.abs(newY - y) == 1 && board.getPiece(newX, newY) == null) {
            Piece enPassantTarget = board.getPiece(x, newY);
            if (enPassantTarget instanceof Pawn && enPassantTarget.isWhite != this.isWhite) {
                // Check if it's the last move and it moved 2 squares
                return board.isValidEnPassant(x, newY);
            }
        }

        return false;
    }

    @Override
    public String getSymbol() {
        return isWhite ? "♙" : "♟";
    }
}
