package ui;

import ai.AIPlayer;
import game.ChessGame;
import game.ChessGame.Move;
import javax.swing.*;
import java.awt.*;

/**
 * Main window: the board on the left, the side panel on the right, and the loop that lets the AI reply.
 */
public class ChessFrame extends JFrame {
    private final ChessGame game;
    private final AIPlayer aiPlayer;
    private final GamePanel gamePanel;
    private final SidePanel sidePanel;
    private volatile boolean aiThinking = false;
    private volatile int generation = 0;

    public ChessFrame() {
        setTitle("Chess · Play against the computer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        game = new ChessGame();
        aiPlayer = new AIPlayer(game, AIPlayer.Difficulty.HARD);
        gamePanel = new GamePanel(game);
        sidePanel = new SidePanel(game, AIPlayer.Difficulty.HARD, aiPlayer::setDifficulty, this::resetGame);
        gamePanel.setOnMove(this::refresh);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(gamePanel, BorderLayout.CENTER);
        root.add(sidePanel, BorderLayout.EAST);
        setContentPane(root);

        getRootPane().registerKeyboardAction(e -> resetGame(),
                KeyStroke.getKeyStroke("control N"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null);
        refresh();
    }

    /** Show the window and start the loop that triggers the AI. */
    public void startGame() {
        setVisible(true);
        gamePanel.requestFocusInWindow();
        Thread loop = new Thread(this::gameLoop, "game-loop");
        loop.setDaemon(true);
        loop.start();
    }

    private boolean gameOver() {
        ChessGame.GameState st = game.getGameState();
        return st == ChessGame.GameState.WHITE_CHECKMATE || st == ChessGame.GameState.BLACK_CHECKMATE || st == ChessGame.GameState.STALEMATE;
    }

    private void gameLoop() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (!game.isWhiteTurn() && !aiThinking && !gameOver()) {
                aiThinking = true;
                final int startedIn = generation;
                SwingUtilities.invokeLater(this::refresh);
                Thread ai = new Thread(() -> {
                    try {
                        Thread.sleep(250); // let the player see their own move land first
                        Move move = aiPlayer.findBestMove();
                        if (move != null && startedIn == generation) {
                            game.movePiece(move.fromX, move.fromY, move.toX, move.toY);
                        }
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    } finally {
                        aiThinking = false;
                        SwingUtilities.invokeLater(this::refresh);
                    }
                }, "ai");
                ai.setDaemon(true);
                ai.start();
            }
        }
    }

    private void refresh() {
        sidePanel.refresh(aiThinking);
        gamePanel.repaint();
    }

    private void resetGame() {
        generation++;
        game.reset();
        gamePanel.clearSelection();
        refresh();
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        SwingUtilities.invokeLater(() -> new ChessFrame().startGame());
    }
}
