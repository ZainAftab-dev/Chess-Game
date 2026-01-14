package pieces;

import board.Board;

public class King extends Piece {
    public King(int x, int y, boolean isWhite) {
        super(x, y, isWhite);
    }

    @Override
    public boolean isValidMove(int newX, int newY, Board board) {
        if (!isValidTarget(newX, newY, board)) return false;
        
        int dx = Math.abs(newX - x);
        int dy = Math.abs(newY - y);
        
        // Standard king move: one square in any direction
        if (dx <= 1 && dy <= 1 && !(dx == 0 && dy == 0)) {
            return true;
        }
        
        // Castling: king and rook both haven't moved
        if (!hasMoved && dy == 2 && newX == x) {
            int rookY = newY > y ? 7 : 0; // Kingside (7) or queenside (0)
            Piece rook = board.getPiece(x, rookY);
            
            if (rook instanceof Rook && !rook.getHasMoved() && rook.isWhite() == isWhite) {
                // Check if path is clear
                int minY = Math.min(y, rookY);
                int maxY = Math.max(y, rookY);
                for (int checkY = minY + 1; checkY < maxY; checkY++) {
                    if (board.getPiece(x, checkY) != null) {
                        return false;
                    }
                }
                return true;
            }
        }
        
        return false;
    }

    @Override
    public String getSymbol() {
        return isWhite ? "♔" : "♚";
    }
}
