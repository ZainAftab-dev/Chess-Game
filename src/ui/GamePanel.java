package ui;

import game.ChessGame;
import game.ChessGame.Move;
import pieces.King;
import pieces.Piece;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the board and pieces and handles the human player's clicks (the human plays White).
 */
public class GamePanel extends JPanel {
    private static final int PAD = 28;

    private final ChessGame game;
    private int selectedX = -1;
    private int selectedY = -1;
    private int hoverX = -1;
    private int hoverY = -1;
    private List<Move> validMoves;
    private Runnable onMove = () -> {};

    public GamePanel(ChessGame game) {
        this.game = game;
        setBackground(Theme.BG);
        setPreferredSize(new Dimension(680, 680));
        setFocusable(true);
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handleClick(e.getX(), e.getY()); }
            @Override public void mouseMoved(MouseEvent e) { updateHover(e.getX(), e.getY()); }
            @Override public void mouseExited(MouseEvent e) { hoverX = hoverY = -1; repaint(); }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { repaint(); }
        });
    }

    public void setOnMove(Runnable onMove) {
        this.onMove = onMove;
    }

    /* ---------- geometry ---------- */

    private int square() {
        return Math.max(24, (Math.min(getWidth(), getHeight()) - 2 * PAD) / 8);
    }

    private Point origin() {
        int size = square() * 8;
        return new Point((getWidth() - size) / 2, (getHeight() - size) / 2);
    }

    /** Returns {row, col} for a pixel, or null when outside the board. */
    private int[] cellAt(int px, int py) {
        Point o = origin();
        int s = square();
        int col = Math.floorDiv(px - o.x, s);
        int row = Math.floorDiv(py - o.y, s);
        if (row < 0 || row > 7 || col < 0 || col > 7) return null;
        return new int[] {row, col};
    }

    private boolean humanCanMove() {
        ChessGame.GameState st = game.getGameState();
        boolean over = st == ChessGame.GameState.WHITE_CHECKMATE || st == ChessGame.GameState.BLACK_CHECKMATE || st == ChessGame.GameState.STALEMATE;
        return game.isWhiteTurn() && !over;
    }

    /* ---------- painting ---------- */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.smooth(g2);
        int s = square();
        Point o = origin();
        int size = s * 8;

        // frame and shadow
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fill(new RoundRectangle2D.Float(o.x - 10, o.y - 6, size + 20, size + 22, 22, 22));
        g2.setColor(new Color(0x3b2a1c));
        g2.fill(new RoundRectangle2D.Float(o.x - 10, o.y - 10, size + 20, size + 20, 20, 20));

        Shape clip = new RoundRectangle2D.Float(o.x, o.y, size, size, 8, 8);
        Shape oldClip = g2.getClip();
        g2.clip(clip);

        Move last = game.getLastMove();
        int[] checkedKing = checkedKing();
        Font coordFont = Theme.UI.deriveFont(Font.BOLD, Math.max(10f, s * 0.16f));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int x = o.x + col * s;
                int y = o.y + row * s;
                boolean light = (row + col) % 2 == 0;
                g2.setColor(light ? Theme.LIGHT_SQUARE : Theme.DARK_SQUARE);
                g2.fillRect(x, y, s, s);

                if (last != null && ((last.fromX == row && last.fromY == col) || (last.toX == row && last.toY == col))) {
                    g2.setColor(Theme.LAST_MOVE);
                    g2.fillRect(x, y, s, s);
                }
                if (selectedX == row && selectedY == col) {
                    g2.setColor(Theme.SELECTED);
                    g2.fillRect(x, y, s, s);
                } else if (hoverX == row && hoverY == col && humanCanMove()) {
                    g2.setColor(Theme.HOVER);
                    g2.fillRect(x, y, s, s);
                }
                if (checkedKing != null && checkedKing[0] == row && checkedKing[1] == col) {
                    float cx = x + s / 2f, cy = y + s / 2f;
                    g2.setPaint(new RadialGradientPaint(cx, cy, s * 0.62f, new float[] {0f, 0.55f, 1f},
                            new Color[] {new Color(255, 40, 30, 230), new Color(230, 40, 30, 140), new Color(230, 40, 30, 0)}));
                    g2.fillRect(x, y, s, s);
                }

                // coordinates on the edge squares, in the opposite square colour
                g2.setFont(coordFont);
                g2.setColor(light ? Theme.DARK_SQUARE : Theme.LIGHT_SQUARE);
                FontMetrics fm = g2.getFontMetrics();
                if (col == 0) g2.drawString(String.valueOf(8 - row), x + 4, y + fm.getAscent() + 2);
                if (row == 7) {
                    String file = String.valueOf((char) ('a' + col));
                    g2.drawString(file, x + s - fm.stringWidth(file) - 4, y + s - 4);
                }
            }
        }

        // legal-move markers
        if (validMoves != null) {
            for (Move m : validMoves) {
                int x = o.x + m.toY * s;
                int y = o.y + m.toX * s;
                g2.setColor(Theme.MOVE_DOT);
                if (game.getBoard().getPiece(m.toX, m.toY) != null) {
                    float w = s * 0.08f;
                    g2.setStroke(new BasicStroke(w));
                    g2.draw(new Ellipse2D.Float(x + w / 2 + 2, y + w / 2 + 2, s - w - 4, s - w - 4));
                } else {
                    float d = s * 0.3f;
                    g2.fill(new Ellipse2D.Float(x + (s - d) / 2, y + (s - d) / 2, d, d));
                }
            }
        }

        // pieces
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = game.getBoard().getPiece(row, col);
                if (p != null) drawPiece(g2, p.getSymbol(), p.isWhite(), o.x + col * s, o.y + row * s, s);
            }
        }

        g2.setClip(oldClip);
        drawGameOver(g2, o, size);
        g2.dispose();
    }

    private int[] checkedKing() {
        ChessGame.GameState st = game.getGameState();
        Boolean white = null;
        if (st == ChessGame.GameState.WHITE_CHECK || st == ChessGame.GameState.WHITE_CHECKMATE) white = true;
        if (st == ChessGame.GameState.BLACK_CHECK || st == ChessGame.GameState.BLACK_CHECKMATE) white = false;
        if (white == null) return null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.getBoard().getPiece(r, c);
                if (p instanceof King && p.isWhite() == white) return new int[] {r, c};
            }
        }
        return null;
    }

    /** Solid glyph for both colours: white pieces are filled ivory, black pieces ebony, each with an outline. */
    static String solidGlyph(String symbol) {
        switch (symbol) {
            case "♔": return "♚";
            case "♕": return "♛";
            case "♖": return "♜";
            case "♗": return "♝";
            case "♘": return "♞";
            case "♙": return "♟";
            default: return symbol;
        }
    }

    static void drawPiece(Graphics2D g2, String symbol, boolean white, float x, float y, float s) {
        Font f = new Font(Theme.PIECE_FAMILY, Font.PLAIN, Math.round(s * 0.8f));
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = f.createGlyphVector(frc, solidGlyph(symbol));
        Shape outline = gv.getOutline();
        Rectangle2D b = outline.getBounds2D();
        double tx = x + (s - b.getWidth()) / 2 - b.getX();
        double ty = y + (s - b.getHeight()) / 2 - b.getY() + s * 0.01;
        Shape shape = AffineTransform.getTranslateInstance(tx, ty).createTransformedShape(outline);

        Shape shadow = AffineTransform.getTranslateInstance(0, s * 0.03).createTransformedShape(shape);
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fill(shadow);

        g2.setStroke(new BasicStroke(s < 32 ? 1.1f : Math.max(1.4f, s * 0.035f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (white) {
            g2.setColor(new Color(0x2a1f15));
            g2.draw(shape);
            g2.setColor(new Color(0xfbf6ec));
            g2.fill(shape);
        } else {
            g2.setColor(new Color(0xd8c7ad));
            g2.draw(shape);
            g2.setColor(new Color(0x1e1a16));
            g2.fill(shape);
        }
    }

    /** Fills a piece glyph in one colour (used for small icons on dark backgrounds). */
    static void drawGlyph(Graphics2D g2, String symbol, Color color, float x, float y, float s) {
        Font f = new Font(Theme.PIECE_FAMILY, Font.PLAIN, Math.round(s * 0.8f));
        GlyphVector gv = f.createGlyphVector(g2.getFontRenderContext(), solidGlyph(symbol));
        Shape outline = gv.getOutline();
        Rectangle2D b = outline.getBounds2D();
        double tx = x + (s - b.getWidth()) / 2 - b.getX();
        double ty = y + (s - b.getHeight()) / 2 - b.getY();
        g2.setColor(color);
        g2.fill(AffineTransform.getTranslateInstance(tx, ty).createTransformedShape(outline));
    }

    private void drawGameOver(Graphics2D g2, Point o, int size) {
        ChessGame.GameState st = game.getGameState();
        String title;
        String sub;
        if (st == ChessGame.GameState.BLACK_CHECKMATE) {
            title = "You win";
            sub = "Checkmate. The computer's king is trapped.";
        } else if (st == ChessGame.GameState.WHITE_CHECKMATE) {
            title = "Checkmate";
            sub = "The computer wins this one.";
        } else if (st == ChessGame.GameState.STALEMATE) {
            title = "Stalemate";
            sub = "No legal moves left. It's a draw.";
        } else {
            return;
        }
        g2.setColor(new Color(20, 16, 12, 170));
        g2.fill(new RoundRectangle2D.Float(o.x, o.y, size, size, 8, 8));
        int w = Math.min(size - 40, 360);
        int h = 132;
        int x = o.x + (size - w) / 2;
        int y = o.y + (size - h) / 2;
        g2.setColor(Theme.CARD);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 20, 20));
        g2.setColor(Theme.BRASS);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 20, 20));
        g2.setFont(Theme.TITLE);
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Theme.TEXT);
        g2.drawString(title, x + (w - fm.stringWidth(title)) / 2, y + 52);
        g2.setFont(Theme.UI.deriveFont(14f));
        fm = g2.getFontMetrics();
        g2.setColor(Theme.MUTED);
        g2.drawString(sub, x + (w - fm.stringWidth(sub)) / 2, y + 82);
        String hint = "Press New game to play again";
        g2.setColor(Theme.BRASS);
        g2.setFont(Theme.UI.deriveFont(Font.BOLD, 13f));
        fm = g2.getFontMetrics();
        g2.drawString(hint, x + (w - fm.stringWidth(hint)) / 2, y + 108);
    }

    /* ---------- interaction ---------- */

    private void updateHover(int px, int py) {
        int[] cell = cellAt(px, py);
        int hx = cell == null ? -1 : cell[0];
        int hy = cell == null ? -1 : cell[1];
        boolean pointer = false;
        if (cell != null && humanCanMove()) {
            Piece p = game.getBoard().getPiece(hx, hy);
            pointer = (p != null && p.isWhite()) || isTarget(hx, hy);
        }
        setCursor(Cursor.getPredefinedCursor(pointer ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        if (hx != hoverX || hy != hoverY) {
            hoverX = hx;
            hoverY = hy;
            repaint();
        }
    }

    private boolean isTarget(int row, int col) {
        if (validMoves == null) return false;
        for (Move m : validMoves) if (m.toX == row && m.toY == col) return true;
        return false;
    }

    private void select(int row, int col) {
        selectedX = row;
        selectedY = col;
        validMoves = new ArrayList<>();
        for (Move m : game.getAllValidMoves(true)) {
            if (m.fromX == row && m.fromY == col) validMoves.add(m);
        }
    }

    private void handleClick(int px, int py) {
        int[] cell = cellAt(px, py);
        if (cell == null || !humanCanMove()) return;
        int row = cell[0], col = cell[1];
        Piece p = game.getBoard().getPiece(row, col);

        if (selectedX == -1) {
            if (p != null && p.isWhite()) select(row, col);
        } else if (selectedX == row && selectedY == col) {
            clearSelection();
        } else if (p != null && p.isWhite()) {
            select(row, col);
        } else if (game.movePiece(selectedX, selectedY, row, col)) {
            clearSelection();
            onMove.run();
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
