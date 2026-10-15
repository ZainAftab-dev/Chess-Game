package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Colours, fonts and small painted widgets shared by the UI ("walnut tournament" theme).
 */
public final class Theme {
    private Theme() {}

    // surroundings
    public static final Color BG = new Color(0x1c1915);
    public static final Color CARD = new Color(0x27221c);
    public static final Color CARD_EDGE = new Color(0x3a3229);
    public static final Color TEXT = new Color(0xf2eadc);
    public static final Color MUTED = new Color(0xa89a85);
    public static final Color BRASS = new Color(0xd9a35b);
    public static final Color BRASS_DARK = new Color(0x2b2114);
    public static final Color DANGER = new Color(0xe0695a);
    public static final Color GOOD = new Color(0x8fbf6a);

    // board
    public static final Color LIGHT_SQUARE = new Color(0xeedcbc);
    public static final Color DARK_SQUARE = new Color(0xb07d52);
    public static final Color LAST_MOVE = new Color(232, 190, 80, 120);
    public static final Color SELECTED = new Color(217, 163, 91, 175);
    public static final Color HOVER = new Color(255, 255, 255, 40);
    public static final Color MOVE_DOT = new Color(40, 25, 10, 70);

    public static final Font UI = new Font(pick(new String[] {"Segoe UI", "SF Pro Text", "Helvetica Neue", "Inter", "Ubuntu", "DejaVu Sans"}, "Dialog", 'a'), Font.PLAIN, 14);
    public static final Font TITLE = new Font(pick(new String[] {"Georgia", "Cambria", "Lora", "DejaVu Serif"}, "Serif", 'a'), Font.BOLD, 30);
    public static final String PIECE_FAMILY = pick(new String[] {"Segoe UI Symbol", "Apple Symbols", "DejaVu Sans", "Noto Sans Symbols2", "FreeSerif", "Arial Unicode MS"}, "Dialog", '♚');

    private static String pick(String[] families, String fallback, char mustShow) {
        java.util.Set<String> available = new java.util.HashSet<>(java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : families) {
            if (available.contains(f) && new Font(f, Font.PLAIN, 12).canDisplay(mustShow)) return f;
        }
        return fallback;
    }

    public static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Small uppercase section heading. */
    public static JLabel heading(String text) {
        return label(text.toUpperCase(), UI.deriveFont(Font.BOLD, 11f), MUTED);
    }

    /** A rounded, flat button that looks the same on every look-and-feel. */
    public static class FlatButton extends JButton {
        private final boolean primary;
        private boolean selected;
        private boolean hover;

        public FlatButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(UI.deriveFont(Font.BOLD, primary ? 15f : 13f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(primary ? 12 : 8, 12, primary ? 12 : 8, 12));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        public void setChosen(boolean chosen) {
            this.selected = chosen;
            getAccessibleContext().setAccessibleDescription(chosen ? "selected" : null);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            Color fill;
            Color fg;
            if (primary) {
                fill = hover ? BRASS.brighter() : BRASS;
                fg = BRASS_DARK;
            } else if (selected) {
                fill = BRASS;
                fg = BRASS_DARK;
            } else {
                fill = hover ? new Color(0x3a332a) : new Color(0x312b24);
                fg = TEXT;
            }
            if (getModel().isPressed()) fill = fill.darker();
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            if (isFocusOwner()) {
                g2.setColor(TEXT);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 12, 12));
            }
            g2.setFont(getFont());
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            String t = getText();
            g2.drawString(t, (w - fm.stringWidth(t)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** A panel with a rounded card background. */
    public static class Card extends JPanel {
        public Card() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(CARD);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
            g2.setColor(CARD_EDGE);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
            g2.dispose();
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }
}
