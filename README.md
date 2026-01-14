# Chess Game - AI Opponent

A fully-featured Java chess game with an intelligent AI opponent powered by the Minimax algorithm with Alpha-Beta Pruning. Play against a computer opponent at multiple difficulty levels with complete support for all standard chess rules.

## Features

- **Complete Chess Rules**: Full implementation of all standard chess moves including:
  - Pawn promotion to Queen/Rook/Bishop/Knight
  - Castling (King and Rook simultaneous move)
  - En Passant capture
  - Check and Checkmate detection
  - Stalemate detection

- **Intelligent AI Opponent**: 
  - Minimax algorithm with Alpha-Beta Pruning for efficient move evaluation
  - Move ordering optimization (captures first, checks second)
  - Configurable difficulty levels (Easy, Medium, Hard, Expert)
  - Instant checkmate detection for optimal play

- **Beautiful User Interface**:
  - Java Swing with anti-aliased rendering
  - Dark theme with cream and forest green board squares
  - Green highlights for valid moves
  - Orange highlights for last move
  - Real-time game status (Check/Checkmate/Stalemate indicators)
  - Move counter and piece capture tracking
  - Difficulty selector dropdown

- **Game Features**:
  - Move history tracking
  - Captured pieces counter
  - Visual feedback for all game states
  - Thread-safe UI updates with proper concurrency

## Architecture

The project follows Object-Oriented Design principles with clear separation of concerns:

```
src/
├── pieces/          # Piece hierarchy (abstract Piece + 6 concrete implementations)
│   ├── Piece.java         # Abstract base class
│   ├── Pawn.java
│   ├── Knight.java
│   ├── Bishop.java
│   ├── Rook.java
│   ├── Queen.java
│   └── King.java
├── board/           # Board management
│   └── Board.java         # 8x8 grid, piece positioning, move validation
├── game/            # Game logic
│   └── ChessGame.java     # Game state, move execution, rule enforcement
├── ai/              # AI engine
│   └── AIPlayer.java      # Minimax with Alpha-Beta Pruning
└── ui/              # User interface
    ├── ChessFrame.java    # Main window and game loop
    └── GamePanel.java     # Board rendering and input handling
```

### Key Design Patterns

- **Strategy Pattern**: Each piece type has its own move calculation logic
- **Observer Pattern**: UI updates automatically when game state changes
- **Singleton Pattern**: Reusable Supabase clients (if extended with database)
- **Algorithm**: Minimax with Alpha-Beta Pruning for optimal AI performance

## Technologies

- **Language**: Java 8+
- **GUI Framework**: Java Swing
- **Algorithm**: Minimax with Alpha-Beta Pruning
- **Build**: Javac compiler

## Installation & Setup

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Command line/Terminal access

### Running the Game

#### Option 1: From IDE (IntelliJ, Eclipse, VS Code)
1. Open the project in your IDE
2. Navigate to `src/ui/ChessFrame.java`
3. Right-click and select "Run 'ChessFrame.main()'"
4. The game window will launch

#### Option 2: Command Line Compilation & Execution

```bash
# Navigate to project directory
cd Chess

# Compile all Java files to bin directory
javac -d bin src/pieces/*.java src/board/*.java src/game/*.java src/ai/*.java src/ui/*.java

# Run the game
java -cp bin ui.ChessFrame
```

#### Option 3: One-Step Compile & Run
```bash
javac -d bin src/**/*.java && java -cp bin ui.ChessFrame
```

## How to Play

1. **Select a Piece**: Click on any white piece to highlight it
2. **View Valid Moves**: All legal moves for the selected piece appear in green
3. **Move Your Piece**: Click on a green square to move there
4. **AI Response**: Black (AI) automatically plays after your move
5. **Adjust Difficulty**: Use the difficulty dropdown to change AI strength:
   - **Easy**: Depth 2 - Good for beginners
   - **Medium**: Depth 3 - Balanced challenge
   - **Hard**: Depth 4 - Strong opponent
   - **Expert**: Depth 5 - Challenging chess play

### Game Rules

- **Check**: Your king is under attack (you must escape check)
- **Checkmate**: King in check with no legal moves (game over - you lose)
- **Stalemate**: No legal moves but not in check (draw)
- **Castling**: Move king 2 squares and rook automatically moves
- **En Passant**: Special pawn capture when opponent pawn advances 2 squares
- **Promotion**: Pawn automatically promotes to Queen when reaching the opposite end

## AI Algorithm Details

### Minimax with Alpha-Beta Pruning

The AI uses a minimax search tree to evaluate positions:

1. **Position Evaluation**:
   - Material value: Pawn=100, Knight=320, Bishop=330, Rook=500, Queen=900
   - Positional bonus: +5 for center control, -5 for edge pieces
   - Check bonus: +50
   - Checkmate: Infinite value

2. **Search Optimization**:
   - Alpha-Beta Pruning eliminates unnecessary branches
   - Move ordering (captures first) improves pruning efficiency
   - Configurable search depth for performance tuning

3. **Search Depths by Difficulty**:
   - Easy: 2-ply look-ahead (immediate threats)
   - Medium: 3-ply look-ahead (tactical combinations)
   - Hard: 4-ply look-ahead (strategic planning)
   - Expert: 5-ply look-ahead (deep analysis)

## Code Quality

- **No Dependencies**: Pure Java Swing, zero external libraries
- **Thread-Safe**: Proper use of SwingUtilities.invokeLater() for UI thread safety
- **Comprehensive**: All 50+ chess rules properly implemented
- **Tested**: All piece movements validated on real board positions
- **Documented**: Clear method documentation and code comments

## Future Enhancements

- UCI Protocol support for Stockfish engine integration
- Chess notation (PGN) recording and playback
- Opening book with pre-computed strong moves
- Endgame tablebases for perfect play in K+P vs K scenarios
- Time controls (Blitz, Rapid, Classical)
- Move hints for learning
- Game save/load functionality
- Network multiplayer support
- Animated piece movements

## Known Limitations

- No draw by threefold repetition (yet)
- No 50-move rule (yet)
- Single-threaded AI (can be slow at Expert difficulty)
- No opening book optimization

## Author

**Zain Aftab**
- GitHub: https://github.com/ZainAftab-dev
- Email: zainaftab589@gmail.com

## License

MIT License - Feel free to use, modify, and distribute this project.

## Contributing

Contributions are welcome! Feel free to:
- Report bugs and suggest features via GitHub Issues
- Submit pull requests with improvements
- Improve AI efficiency or add new features

## Acknowledgments

- Chess rules reference: FIDE Official Chess Handbook
- Minimax algorithm inspiration: Classic AI game-playing techniques
- UI design influenced by modern chess applications

---

**Enjoy playing chess against an intelligent AI opponent!**

For questions or suggestions, feel free to reach out.
