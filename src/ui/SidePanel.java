package ui;

import ai.AIPlayer;
import game.ChessGame;
import game.ChessGame.Move;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * Right-hand panel: status, difficulty, captured pieces, move list and New game.
 */
public class SidePanel extends JPanel {
    private final ChessGame game;
    private final StatusLine status = new StatusLine();
    private final Theme.FlatButton[] levelButtons;
    private final CapturedRow byYou = new CapturedRow("By you");
    private final CapturedRow byComputer = new CapturedRow("By the computer");
    private final MoveList moveList = new MoveList();
    private final JScrollPane moveScroll;
    private int lastMoveCount = -1;

    public SidePanel(ChessGame game, AIPlayer.Difficulty initial, Consumer<AIPlayer.Difficulty> onDifficulty, Runnable onNewGame) {
        this.game = game;
        setBackground(Theme.BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(28, 4, 28, 28));
        setPreferredSize(new Dimension(320, 680));

        add(Theme.label("Chess", Theme.TITLE, Theme.TEXT));
        add(Box.createVerticalStrut(2));
        add(Theme.label("You play White against the computer", Theme.UI, Theme.MUTED));
        add(Box.createVerticalStrut(18));

        Theme.Card statusCard = new Theme.Card();
        statusCard.add(status);
        add(statusCard);
        add(Box.createVerticalStrut(18));

        add(Theme.heading("Computer strength"));
        add(Box.createVerticalStrut(8));
        JPanel levels = new JPanel(new GridLayout(1, 4, 6, 0));
        levels.setOpaque(false);
        levels.setAlignmentX(LEFT_ALIGNMENT);
        levels.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        AIPlayer.Difficulty[] all = AIPlayer.Difficulty.values();
        levelButtons = new Theme.FlatButton[all.length];
        for (int i = 0; i < all.length; i++) {
            AIPlayer.Difficulty d = all[i];
            String name = d.name().charAt(0) + d.name().substring(1).toLowerCase();
            Theme.FlatButton b = new Theme.FlatButton(name, false);
            b.setToolTipText(name + ": searches " + d.maxDepth + " moves ahead");
            b.setChosen(d == initial);
            b.addActionListener(e -> {
                for (Theme.FlatButton other : levelButtons) other.setChosen(other == b);
                onDifficulty.accept(d);
            });
            levelButtons[i] = b;
            levels.add(b);
        }
        add(levels);
        add(Box.createVerticalStrut(18));

        add(Theme.heading("Captured"));
        add(Box.createVerticalStrut(8));
        Theme.Card capturedCard = new Theme.Card();
        capturedCard.add(byYou);
        capturedCard.add(Box.createVerticalStrut(8));
        capturedCard.add(byComputer);
        add(capturedCard);
        add(Box.createVerticalStrut(18));

        add(Theme.heading("Moves"));
        add(Box.createVerticalStrut(8));
        moveScroll = new JScrollPane(moveList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        moveScroll.setBorder(BorderFactory.createLineBorder(Theme.CARD_EDGE));
        moveScroll.getViewport().setBackground(Theme.CARD);
        moveScroll.getVerticalScrollBar().setUnitIncrement(16);
        moveScroll.setAlignmentX(LEFT_ALIGNMENT);
        moveScroll.setPreferredSize(new Dimension(280, 160));
        add(moveScroll);
        add(Box.createVerticalStrut(18));

        Theme.FlatButton newGame = new Theme.FlatButton("New game", true);
        newGame.setAlignmentX(LEFT_ALIGNMENT);
        newGame.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        newGame.addActionListener(e -> onNewGame.run());
        add(newGame);
    }

    public void refresh(boolean aiThinking) {
        status.update(game.getGameState(), game.isWhiteTurn(), aiThinking);
        List<String> lostWhite = game.getCapturedWhiteSymbols();
        List<String> lostBlack = game.getCapturedBlackSymbols();
        int diff = material(lostBlack) - material(lostWhite);
        byYou.set(lostBlack, false, diff > 0 ? "+" + diff : "");
        byComputer.set(lostWhite, true, diff < 0 ? "+" + (-diff) : "");

        List<Move> moves = game.getMoveHistory();
        if (moves.size() != lastMoveCount) {
            lastMoveCount = moves.size();
            moveList.setMoves(moves);
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = moveScroll.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        }
        repaint();
    }

    private static int material(List<String> symbols) {
        int total = 0;
        for (String s : symbols) {
            switch (GamePanel.solidGlyph(s)) {
                case "♟": total += 1; break;
                case "♞": case "♝": total += 3; break;
                case "♜": total += 5; break;
                case "♛": total += 9; break;
                default: break;
            }
        }
        return total;
    }

    /* ---------- status line ---------- */

    private static class StatusLine extends JComponent {
        private String text = "Your move";
        private String detail = "Pick a white piece to see its moves.";
        private Color dot = Theme.GOOD;

        StatusLine() {
            setAlignmentX(LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(260, 48));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        }

        void update(ChessGame.GameState st, boolean whiteTurn, boolean thinking) {
            switch (st) {
                case WHITE_CHECKMATE:
                    text = "Checkmate";
                    detail = "The computer wins. Try a lower level?";
                    dot = Theme.DANGER;
                    break;
                case BLACK_CHECKMATE:
                    text = "You win!";
                    detail = "Checkmate against the computer.";
                    dot = Theme.BRASS;
                    break;
                case STALEMATE:
                    text = "Stalemate";
                    detail = "No legal moves. The game is a draw.";
                    dot = Theme.MUTED;
                    break;
                case WHITE_CHECK:
                    text = "Check!";
                    detail = "Your king is under attack. Get it out of check.";
                    dot = Theme.DANGER;
                    break;
                default:
                    if (whiteTurn && !thinking) {
                        text = "Your move";
                        detail = st == ChessGame.GameState.BLACK_CHECK ? "The computer's king is in check." : "Pick a white piece to see its moves.";
                        dot = Theme.GOOD;
                    } else {
                        text = "Computer is thinking…";
                        detail = "It searches for the best reply.";
                        dot = Theme.BRASS;
                    }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.smooth(g2);
            g2.setColor(new Color(dot.getRed(), dot.getGreen(), dot.getBlue(), 60));
            g2.fill(new Ellipse2D.Float(0, 4, 18, 18));
            g2.setColor(dot);
            g2.fill(new Ellipse2D.Float(4, 8, 10, 10));
            g2.setFont(Theme.UI.deriveFont(Font.BOLD, 18f));
            g2.setColor(Theme.TEXT);
            g2.drawString(text, 28, 20);
            g2.setFont(Theme.UI.deriveFont(13f));
            g2.setColor(Theme.MUTED);
            g2.drawString(detail, 28, 41);
            g2.dispose();
        }
    }

    /* ---------- captured pieces ---------- */

    private static class CapturedRow extends JComponent {
        private final String label;
        private List<String> symbols = java.util.Collections.emptyList();
        private boolean whitePieces;
        private String advantage = "";

        CapturedRow(String label) {
            this.label = label;
            setAlignmentX(LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(260, 30));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        }

        void set(List<String> symbols, boolean whitePieces, String advantage) {
            this.symbols = symbols;
            this.whitePieces = whitePieces;
            this.advantage = advantage;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.smooth(g2);
            g2.setFont(Theme.UI.deriveFont(12f));
            g2.setColor(Theme.MUTED);
            g2.drawString(label, 0, 19);
            int x = 112;
            if (symbols.isEmpty()) {
                g2.drawString("–", x, 19);
            }
            int step = symbols.size() > 9 ? 12 : 16;
            for (String s : symbols) {
                GamePanel.drawGlyph(g2, s, whitePieces ? Theme.TEXT : new Color(0x9c8a72), x - 3, 4, 22);
                x += step;
            }
            if (!advantage.isEmpty()) {
                g2.setFont(Theme.UI.deriveFont(Font.BOLD, 12f));
                g2.setColor(Theme.BRASS);
                g2.drawString(advantage, x + 14, 19);
            }
            g2.dispose();
        }
    }

    /* ---------- move list ---------- */

    private static class MoveList extends JComponent implements Scrollable {
        private List<Move> moves = java.util.Collections.emptyList();
        private static final int ROW = 26;

        void setMoves(List<Move> moves) {
            this.moves = moves;
            revalidate();
            repaint();
        }

        private static String square(int row, int col) {
            return "" + (char) ('a' + col) + (8 - row);
        }

        private static String text(Move m) {
            String sep = m.capture ? " × " : " – ";
            return square(m.fromX, m.fromY) + sep + square(m.toX, m.toY);
        }

        @Override
        public Dimension getPreferredSize() {
            int rows = Math.max(1, (moves.size() + 1) / 2);
            return new Dimension(260, rows * ROW + 12);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.smooth(g2);
            g2.setColor(Theme.CARD);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (moves.isEmpty()) {
                g2.setFont(Theme.UI.deriveFont(13f));
                g2.setColor(Theme.MUTED);
                g2.drawString("No moves yet. White starts.", 14, 26);
                g2.dispose();
                return;
            }
            int colW = (getWidth() - 44) / 2;
            for (int i = 0; i < moves.size(); i += 2) {
                int y = 6 + (i / 2) * ROW;
                if ((i / 2) % 2 == 1) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fillRect(0, y, getWidth(), ROW);
                }
                g2.setFont(Theme.UI.deriveFont(12f));
                g2.setColor(Theme.MUTED);
                g2.drawString((i / 2 + 1) + ".", 12, y + 18);
                drawMove(g2, moves.get(i), true, 40, y, i == moves.size() - 1);
                if (i + 1 < moves.size()) drawMove(g2, moves.get(i + 1), false, 40 + colW, y, i + 1 == moves.size() - 1);
            }
            g2.dispose();
        }

        private void drawMove(Graphics2D g2, Move m, boolean white, int x, int y, boolean latest) {
            if (m.symbol != null) GamePanel.drawGlyph(g2, m.symbol, white ? Theme.TEXT : new Color(0x9c8a72), x - 2, y + 3, 20);
            g2.setFont(Theme.UI.deriveFont(latest ? Font.BOLD : Font.PLAIN, 13f));
            g2.setColor(latest ? Theme.BRASS : Theme.TEXT);
            g2.drawString(text(m), x + 22, y + 18);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return new Dimension(260, 160); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return ROW; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return ROW * 4; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
