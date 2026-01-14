package ai;

import board.Board;
import pieces.*;
import game.ChessGame;
import game.ChessGame.Move;
import java.util.List;

/**
 * AI opponent using Minimax algorithm with Alpha-Beta Pruning.
 * Supports multiple difficulty levels.
 */
public class AIPlayer {
    public enum Difficulty {
        EASY(2),
        MEDIUM(3),
        HARD(4),
        EXPERT(5);
        
        public final int maxDepth;
        Difficulty(int maxDepth) {
            this.maxDepth = maxDepth;
        }
    }
    
    private static final int TIMEOUT_MS = 3000;
    
    // Piece values for evaluation
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;

    private ChessGame game;
    private long startTime;
    private Difficulty difficulty;

    public AIPlayer(ChessGame game) {
        this(game, Difficulty.HARD);
    }

    public AIPlayer(ChessGame game, Difficulty difficulty) {
        this.game = game;
        this.difficulty = difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Find the best move for Black using Minimax with Alpha-Beta Pruning.
     */
    public Move findBestMove() {
        startTime = System.currentTimeMillis();
        
        List<Move> possibleMoves = game.getAllValidMoves(false); // Black's moves
        if (possibleMoves.isEmpty()) return null;

        Move bestMove = possibleMoves.get(0);
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : possibleMoves) {
            Board testBoard = game.getBoard().copy();
            testBoard.movePiece(move.fromX, move.fromY, move.toX, move.toY);
            
            if (isKingInCheck(true, testBoard) && getAllValidMovesForBoard(testBoard, true).isEmpty()) {
                return move; // Instant checkmate!
            }
            
            // Create temporary game state
            int value = minimax(testBoard, difficulty.maxDepth - 1, alpha, beta, true);
            
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
            alpha = Math.max(alpha, value);
        }

        return bestMove;
    }

    /**
     * Minimax algorithm with Alpha-Beta Pruning.
     */
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        // Timeout check
        if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
            return evaluateBoard(board);
        }

        if (depth == 0) {
            return evaluateBoard(board);
        }

        List<Move> moves = getAllValidMovesForBoard(board, isMaximizing);
        moves = orderMoves(moves, board, isMaximizing);
        
        if (moves.isEmpty()) {
            return evaluateBoard(board);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            
            for (Move move : moves) {
                Board testBoard = board.copy();
                testBoard.movePiece(move.fromX, move.fromY, move.toX, move.toY);
                
                int eval = minimax(testBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            
            for (Move move : moves) {
                Board testBoard = board.copy();
                testBoard.movePiece(move.fromX, move.fromY, move.toX, move.toY);
                
                int eval = minimax(testBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    /**
     * Evaluate the board position.
     */
    private int evaluateBoard(Board board) {
        int score = 0;
        
        boolean whiteInCheck = isKingInCheck(true, board);
        boolean blackInCheck = isKingInCheck(false, board);
        List<Move> whiteMoves = getAllValidMovesForBoard(board, true);
        List<Move> blackMoves = getAllValidMovesForBoard(board, false);
        
        if (whiteMoves.isEmpty() && whiteInCheck) {
            return Integer.MIN_VALUE + 1000;
        }
        if (blackMoves.isEmpty() && blackInCheck) {
            return Integer.MAX_VALUE - 1000;
        }
        
        if (whiteInCheck) score -= 50;
        if (blackInCheck) score += 50;
        
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece p = board.getPiece(x, y);
                if (p != null) {
                    int value = getPieceValue(p);
                    int centerDistance = (int)(Math.abs(x - 3.5) + Math.abs(y - 3.5));
                    int positionBonus = (8 - centerDistance) * 5;
                    
                    if (p.isWhite()) {
                        score += value + positionBonus;
                    } else {
                        score -= value + positionBonus;
                    }
                }
            }
        }
        return score;
    }

    private int getPieceValue(Piece p) {
        if (p instanceof Pawn) return PAWN_VALUE;
        if (p instanceof Knight) return KNIGHT_VALUE;
        if (p instanceof Bishop) return BISHOP_VALUE;
        if (p instanceof Rook) return ROOK_VALUE;
        if (p instanceof Queen) return QUEEN_VALUE;
        if (p instanceof King) return KING_VALUE;
        return 0;
    }

    private List<Move> getAllValidMovesForBoard(Board board, boolean isWhite) {
        List<Move> validMoves = new java.util.ArrayList<>();
        
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece p = board.getPiece(x, y);
                if (p != null && p.isWhite() == isWhite) {
                    for (int newX = 0; newX < 8; newX++) {
                        for (int newY = 0; newY < 8; newY++) {
                            if (p.isValidMove(newX, newY, board)) {
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

    private boolean isKingInCheck(boolean isWhite, Board checkBoard) {
        King king = findKing(isWhite, checkBoard);
        if (king == null) return false;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece p = checkBoard.getPiece(x, y);
                if (p != null && p.isWhite() != isWhite) {
                    if (p.isValidMove(king.getX(), king.getY(), checkBoard)) {
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
                Piece p = searchBoard.getPiece(x, y);
                if (p instanceof King && p.isWhite() == isWhite) {
                    return (King) p;
                }
            }
        }
        return null;
    }

    private List<Move> orderMoves(List<Move> moves, Board board, boolean isMaximizing) {
        List<Move> capturedMoves = new java.util.ArrayList<>();
        List<Move> checkMoves = new java.util.ArrayList<>();
        List<Move> otherMoves = new java.util.ArrayList<>();
        
        for (Move move : moves) {
            Board testBoard = board.copy();
            testBoard.movePiece(move.fromX, move.fromY, move.toX, move.toY);
            
            if (board.getPiece(move.toX, move.toY) != null) {
                capturedMoves.add(move);
            }
            else if (isKingInCheck(!isMaximizing, testBoard)) {
                checkMoves.add(move);
            } else {
                otherMoves.add(move);
            }
        }
        
        capturedMoves.sort((m1, m2) -> {
            int v1 = getPieceValue(board.getPiece(m1.toX, m1.toY));
            int v2 = getPieceValue(board.getPiece(m2.toX, m2.toY));
            return Integer.compare(v2, v1);
        });
        
        capturedMoves.addAll(checkMoves);
        capturedMoves.addAll(otherMoves);
        return capturedMoves;
    }
}
