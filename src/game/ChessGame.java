package game;

import board.Board;
import pieces.Piece;
import pieces.King;
import pieces.Pawn;
import pieces.Queen;
import pieces.Rook;
import pieces.Bishop;
import pieces.Knight;
import java.util.*;

/**
 * Manages the core chess game logic, move validation, and game state.
 */
public class ChessGame {
    private Board board;
    private boolean isWhiteTurn;
    private Move lastMove;
    private GameState gameState;
    private List<Move> moveHistory;
    private int capturedWhitePieces;
    private int capturedBlackPieces;
    private List<String> capturedWhiteSymbols;
    private List<String> capturedBlackSymbols;

    public enum GameState {
        PLAYING, WHITE_CHECK, BLACK_CHECK, WHITE_CHECKMATE, BLACK_CHECKMATE, STALEMATE
    }

    public ChessGame() {
        board = new Board();
        isWhiteTurn = true;
        lastMove = null;
        gameState = GameState.PLAYING;
        moveHistory = new java.util.concurrent.CopyOnWriteArrayList<>();
        capturedWhitePieces = 0;
        capturedBlackPieces = 0;
        capturedWhiteSymbols = new java.util.concurrent.CopyOnWriteArrayList<>();
        capturedBlackSymbols = new java.util.concurrent.CopyOnWriteArrayList<>();
    }

    /**
     * Attempt to move a piece from (fromX, fromY) to (toX, toY).
     * Returns true if move is valid and executed.
     */
    public boolean movePiece(int fromX, int fromY, int toX, int toY) {
        if (fromX < 0 || fromX >= 8 || fromY < 0 || fromY >= 8 ||
            toX < 0 || toX >= 8 || toY < 0 || toY >= 8) {
            return false;
        }
        
        Piece piece = board.getPiece(fromX, fromY);
        
        if (piece == null) return false;
        if (piece.isWhite() != isWhiteTurn) return false;
        if (!piece.isValidMove(toX, toY, board)) return false;

        // Make the move on a copy to check if it leaves king in check
        Board testBoard = board.copy();
        testBoard.movePiece(fromX, fromY, toX, toY);

        if (isKingInCheck(isWhiteTurn, testBoard)) return false;

        Piece capturedPiece = board.getPiece(toX, toY);
        if (capturedPiece == null && piece instanceof Pawn && fromY != toY) {
            // En passant capture
            capturedPiece = board.getPiece(fromX, toY);
        }
        if (capturedPiece != null) {
            if (capturedPiece.isWhite()) {
                capturedWhitePieces++;
                capturedWhiteSymbols.add(capturedPiece.getSymbol());
            } else {
                capturedBlackPieces++;
                capturedBlackSymbols.add(capturedPiece.getSymbol());
            }
        }
        
        // Move is valid, execute it
        String movedSymbol = piece.getSymbol();
        board.movePiece(fromX, fromY, toX, toY);
        lastMove = new Move(fromX, fromY, toX, toY);
        lastMove.symbol = movedSymbol;
        lastMove.capture = capturedPiece != null;
        moveHistory.add(lastMove);
        
        if (piece instanceof Pawn) {
            int promotionRow = piece.isWhite() ? 0 : 7;
            if (toX == promotionRow) {
                board.setPiece(toX, toY, new Queen(toX, toY, piece.isWhite()));
            }
        }
        
        // Hand the turn over first, so the state is evaluated for the side that must move next
        isWhiteTurn = !isWhiteTurn;
        updateGameState();

        return true;
    }

    /**
     * Get all valid moves for a player.
     */
    public List<Move> getAllValidMoves(boolean isWhite) {
        List<Move> validMoves = new ArrayList<>();
        
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = board.getPiece(x, y);
                if (piece != null && piece.isWhite() == isWhite) {
                    for (int newX = 0; newX < 8; newX++) {
                        for (int newY = 0; newY < 8; newY++) {
                            if (piece.isValidMove(newX, newY, board)) {
                                Board testBoard = board.copy();
                                testBoard.movePiece(x, y, newX, newY);
                                
                                if (!isKingInCheck(isWhite, testBoard)) {
                                    validMoves.add(new Move(x, y, newX, newY));
                                }
                            }
                        }
                    }
                }
            }
        }
        return validMoves;
    }

    /**
     * Check if the king of the specified color is in check.
     */
    public boolean isKingInCheck(boolean isWhite, Board checkBoard) {
        King king = findKing(isWhite, checkBoard);
        if (king == null) return false;

        int kingX = king.getX();
        int kingY = king.getY();

        // Check if any opponent piece can attack the king
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = checkBoard.getPiece(x, y);
                if (piece != null && piece.isWhite() != isWhite) {
                    if (piece.isValidMove(kingX, kingY, checkBoard)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private King findKing(boolean isWhite, Board searchBoard) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = searchBoard.getPiece(x, y);
                if (piece instanceof King && piece.isWhite() == isWhite) {
                    return (King) piece;
                }
            }
        }
        return null;
    }

    private void updateGameState() {
        boolean whiteInCheck = isKingInCheck(true, board);
        boolean blackInCheck = isKingInCheck(false, board);
        List<Move> whiteMoves = getAllValidMoves(true);
        List<Move> blackMoves = getAllValidMoves(false);

        if (isWhiteTurn && whiteMoves.isEmpty()) {
            gameState = whiteInCheck ? GameState.WHITE_CHECKMATE : GameState.STALEMATE;
        } else if (!isWhiteTurn && blackMoves.isEmpty()) {
            gameState = blackInCheck ? GameState.BLACK_CHECKMATE : GameState.STALEMATE;
        } else if (whiteInCheck) {
            gameState = GameState.WHITE_CHECK;
        } else if (blackInCheck) {
            gameState = GameState.BLACK_CHECK;
        } else {
            gameState = GameState.PLAYING;
        }
    }

    public void reset() {
        board.reset();
        isWhiteTurn = true;
        lastMove = null;
        gameState = GameState.PLAYING;
        moveHistory = new java.util.concurrent.CopyOnWriteArrayList<>();
        capturedWhitePieces = 0;
        capturedBlackPieces = 0;
        capturedWhiteSymbols = new java.util.concurrent.CopyOnWriteArrayList<>();
        capturedBlackSymbols = new java.util.concurrent.CopyOnWriteArrayList<>();
    }

    // Getters
    public Board getBoard() { return board; }
    public boolean isWhiteTurn() { return isWhiteTurn; }
    public Move getLastMove() { return lastMove; }
    public GameState getGameState() { return gameState; }
    public List<Move> getMoveHistory() { return new ArrayList<>(moveHistory); }
    public int getCapturedWhitePieces() { return capturedWhitePieces; }
    public int getCapturedBlackPieces() { return capturedBlackPieces; }
    public int getMoveCount() { return moveHistory.size(); }
    /** Symbols of the white pieces Black has captured, in order. */
    public List<String> getCapturedWhiteSymbols() { return new ArrayList<>(capturedWhiteSymbols); }
    /** Symbols of the black pieces White has captured, in order. */
    public List<String> getCapturedBlackSymbols() { return new ArrayList<>(capturedBlackSymbols); }

    /**
     * Inner class to represent a chess move.
     */
    public static class Move {
        public int fromX, fromY, toX, toY;
        /** Symbol of the piece that moved and whether it captured (set for played moves only). */
        public String symbol;
        public boolean capture;

        public Move(int fromX, int fromY, int toX, int toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }
}
