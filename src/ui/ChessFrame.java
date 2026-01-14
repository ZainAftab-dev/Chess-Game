package ui;

import game.ChessGame;
import ai.AIPlayer;
import game.ChessGame.Move;
import javax.swing.*;
import java.awt.*;

/**
 * Main game window with GUI and game loop integration.
 */
public class ChessFrame extends JFrame {
    private GamePanel gamePanel;
    private ChessGame game;
    private AIPlayer aiPlayer;
    private JLabel statusLabel;
    private JButton resetButton;
    private Thread aiThread;
    private boolean aiThinking = false;
    private JLabel moveCountLabel;
    private JLabel capturesLabel;
    private JComboBox<AIPlayer.Difficulty> difficultyCombo;

    public ChessFrame() {
        setTitle("Chess Game - AI Opponent");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        game = new ChessGame();
        aiPlayer = new AIPlayer(game, AIPlayer.Difficulty.HARD);
        gamePanel = new GamePanel(game);

        // Create top panel with difficulty selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(new Color(50, 50, 50));
        
        JLabel diffLabel = new JLabel("Difficulty:");
        diffLabel.setForeground(new Color(200, 200, 200));
        topPanel.add(diffLabel);
        
        difficultyCombo = new JComboBox<>(AIPlayer.Difficulty.values());
        difficultyCombo.setSelectedItem(AIPlayer.Difficulty.HARD);
        difficultyCombo.addActionListener(e -> {
            AIPlayer.Difficulty selected = (AIPlayer.Difficulty) difficultyCombo.getSelectedItem();
            aiPlayer.setDifficulty(selected);
        });
        topPanel.add(difficultyCombo);

        // Create bottom panel with status and reset button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(new Color(50, 50, 50));

        statusLabel = new JLabel("White's Turn");
        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(statusLabel);
        
        moveCountLabel = new JLabel("Moves: 0");
        moveCountLabel.setForeground(new Color(150, 150, 150));
        moveCountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bottomPanel.add(moveCountLabel);
        
        capturesLabel = new JLabel("Captured: W(0) B(0)");
        capturesLabel.setForeground(new Color(150, 150, 150));
        capturesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bottomPanel.add(capturesLabel);

        resetButton = new JButton("Reset Game");
        resetButton.setBackground(new Color(76, 110, 66));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetGame());
        bottomPanel.add(resetButton);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(50, 50, 50));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setSize(640, 720);
        setLocationRelativeTo(null);
    }

    /**
     * Start the game and game loop.
     */
    public void startGame() {
        setVisible(true);
        Thread gameThread = new Thread(this::gameLoop);
        gameThread.setDaemon(true);
        gameThread.start();
    }

    /**
     * Main game loop: handles AI moves and game state updates.
     */
    private void gameLoop() {
        while (true) {
            try {
                Thread.sleep(100);

                // If it's Black's turn and AI is not already thinking, make AI move
                if (!game.isWhiteTurn() && !aiThinking) {
                    aiThinking = true;
                    aiThread = new Thread(() -> {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("AI is Thinking... (" + aiPlayer.getDifficulty() + ")");
                            gamePanel.repaint();
                        });
                        
                        Move aiMove = aiPlayer.findBestMove();
                        if (aiMove != null) {
                            game.movePiece(aiMove.fromX, aiMove.fromY, aiMove.toX, aiMove.toY);
                        }
                        
                        aiThinking = false;
                        SwingUtilities.invokeLater(() -> {
                            updateStatus();
                            gamePanel.repaint();
                        });
                    });
                    aiThread.setDaemon(true);
                    aiThread.start();
                } else if (game.isWhiteTurn() && !aiThinking) {
                    SwingUtilities.invokeLater(this::updateStatus);
                }

                SwingUtilities.invokeLater(() -> gamePanel.repaint());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateStatus() {
        ChessGame.GameState state = game.getGameState();
        
        switch (state) {
            case PLAYING:
                statusLabel.setText(game.isWhiteTurn() ? "White's Turn" : "Black's Turn (AI)");
                break;
            case WHITE_CHECK:
                statusLabel.setText("White in Check! Your Turn");
                break;
            case BLACK_CHECK:
                statusLabel.setText("Black in Check!");
                break;
            case WHITE_CHECKMATE:
                statusLabel.setText("Black Wins! (White Checkmate)");
                break;
            case BLACK_CHECKMATE:
                statusLabel.setText("White Wins! (Black Checkmate)");
                break;
            case STALEMATE:
                statusLabel.setText("Stalemate - Draw!");
                break;
        }
        
        moveCountLabel.setText("Moves: " + game.getMoveCount());
        capturesLabel.setText("Captured: W(" + game.getCapturedWhitePieces() + ") B(" + game.getCapturedBlackPieces() + ")");
    }

    private void resetGame() {
        game.reset();
        gamePanel.clearSelection();
        aiThinking = false;
        updateStatus();
        gamePanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChessFrame frame = new ChessFrame();
            frame.startGame();
        });
    }
}
