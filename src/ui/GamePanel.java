package ui;

import game.ChessGame;
import game.ChessGame.Move;
import pieces.Piece;
import board.Board;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * JPanel that renders the chess board and handles mouse interactions.
 */
public class GamePanel extends JPanel {
    private ChessGame game;
    private int selectedX = -1;
    private int selectedY = -1;
    private List<Move> validMoves;
    private static final int SQUARE_SIZE = 70;
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181); // Cream
    private static final Color DARK_SQUARE = new Color(76, 110, 66);   // Forest Green
    private static final Color HIGHLIGHT_COLOR = new Color(186, 202, 43); // Yellow-green highlight
    private static final Color LAST_MOVE_COLOR = new Color(255, 200, 124); // Orange highlight
    private static final Color BOARD_BG = new Color(40, 40, 40); // Dark gray border
    private static final Color PANEL_BG = new Color(50, 50, 50); // Darker background

    public GamePanel(ChessGame game) {
        this.game = game;
        this.validMoves = null;
        setBackground(PANEL_BG);
        setPreferredSize(new Dimension(600, 600));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw board border
        g2d.setColor(BOARD_BG);
        g2d.fillRect(20, 20, 560, 560);

        // Draw squares
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int screenX = 20 + y * SQUARE_SIZE;
                int screenY = 20 + x * SQUARE_SIZE;

                // Determine square color
                Color squareColor;
                if (selectedX == x && selectedY == y) {
                    squareColor = HIGHLIGHT_COLOR;
                } else if (game.getLastMove() != null && 
                           ((game.getLastMove().fromX == x && game.getLastMove().fromY == y) ||
                            (game.getLastMove().toX == x && game.getLastMove().toY == y))) {
                    squareColor = LAST_MOVE_COLOR;
                } else {
                    squareColor = (x + y) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                }

                g2d.setColor(squareColor);
                g2d.fillRect(screenX, screenY, SQUARE_SIZE, SQUARE_SIZE);

                // Highlight valid moves
                if (validMoves != null) {
                    for (Move move : validMoves) {
                        if (move.toX == x && move.toY == y) {
                            g2d.setColor(new Color(255, 255, 100, 100));
                            g2d.fillOval(screenX + SQUARE_SIZE / 2 - 8, screenY + SQUARE_SIZE / 2 - 8, 16, 16);
                        }
                    }
                }

                // Draw piece
                Piece piece = game.getBoard().getPiece(x, y);
                if (piece != null) {
                    drawPiece(g2d, piece, screenX, screenY);
                }

                // Draw grid
                g2d.setColor(new Color(100, 100, 100));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(screenX, screenY, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    private void drawPiece(Graphics2D g, Piece piece, int x, int y) {
        g.setFont(new Font("Arial Unicode MS", Font.BOLD, 48));
        g.setColor(piece.isWhite() ? Color.WHITE : new Color(200, 200, 200));
        
        String symbol = piece.getSymbol();
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (SQUARE_SIZE - fm.stringWidth(symbol)) / 2;
        int textY = y + ((SQUARE_SIZE - fm.getAscent()) / 2) + fm.getAscent();
        
        g.drawString(symbol, textX, textY);
    }

    private void handleMouseClick(MouseEvent e) {
        int clickX = e.getX();
        int clickY = e.getY();
        
        if (clickX < 20 || clickX > 580 || clickY < 20 || clickY > 580) return;

        int x = (clickY - 20) / SQUARE_SIZE;
        int y = (clickX - 20) / SQUARE_SIZE;

        if (selectedX == -1) {
            // First click: select piece
            Piece piece = game.getBoard().getPiece(x, y);
            if (piece != null && piece.isWhite() == game.isWhiteTurn()) {
                selectedX = x;
                selectedY = y;
                List<Move> allMoves = game.getAllValidMoves(piece.isWhite());
                validMoves = new java.util.ArrayList<>();
                for (Move move : allMoves) {
                    if (move.fromX == x && move.fromY == y) {
                        validMoves.add(move);
                    }
                }
            }
        } else {
            // Second click: attempt move
            if (game.movePiece(selectedX, selectedY, x, y)) {
                selectedX = -1;
                selectedY = -1;
                validMoves = null;
            } else {
                // Invalid move or deselect
                if (selectedX == x && selectedY == y) {
                    selectedX = -1;
                    selectedY = -1;
                    validMoves = null;
                } else {
                    Piece piece = game.getBoard().getPiece(x, y);
                    if (piece != null && piece.isWhite() == game.isWhiteTurn()) {
                        selectedX = x;
                        selectedY = y;
                        List<Move> allMoves = game.getAllValidMoves(piece.isWhite());
                        validMoves = new java.util.ArrayList<>();
                        for (Move move : allMoves) {
                            if (move.fromX == x && move.fromY == y) {
                                validMoves.add(move);
                            }
                        }
                    }
                }
            }
        }
        repaint();
    }

    public void clearSelection() {
        selectedX = -1;
        selectedY = -1;
        validMoves = null;
        repaint();
    }
}
