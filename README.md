<div align="center">

# ♟️ Chess Game with AI Opponent

A desktop chess game in pure Java Swing where you play White against a computer opponent that searches with **Minimax and Alpha-Beta Pruning**.

![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Swing-5382A1)
![Dependencies](https://img.shields.io/badge/dependencies-none-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

<img src="docs/screenshot.png" alt="Chess board in the walnut theme with a knight selected and its legal moves marked, next to the side panel with status, strength, captured pieces and move list" width="760"/>

</div>

---

## Features

**Chess rules**
- Legal move generation for all six piece types
- Castling, en passant and pawn promotion (pawns promote to a queen)
- Check, checkmate and stalemate detection

**AI opponent**
- Minimax search with Alpha-Beta Pruning
- Move ordering (captures searched first) for better pruning
- Four difficulty levels, changeable at any time from the dropdown
- Plays a checkmate immediately when one is available
- 3-second search time limit per move, so the game never freezes

**Interface**
- Walnut-and-ivory board with ivory and ebony pieces, each outlined so they stand out on light and dark squares
- File and rank labels (a–h, 1–8) on the board edge
- Click a piece to see its moves: dots for empty squares, rings for captures; the last move is tinted and a king in check glows red
- Side panel with a status line (your move, computer thinking, check), a four-step **Computer strength** control, captured pieces with the material lead, and a numbered **move list** (e2 – e4, captures shown with ×)
- Checkmate and stalemate appear as a card over the board
- **New game** button (or <kbd>Ctrl</kbd>+<kbd>N</kbd>); the window can be resized and the board scales with it
- The AI thinks on a background thread, so the window stays responsive

## Difficulty levels

| Level | Search depth | Plays like |
|---|---|---|
| Easy | 2 plies | Sees immediate threats |
| Medium | 3 plies | Spots simple tactics |
| Hard (default) | 4 plies | Solid, careful play |
| Expert | 5 plies | Deepest search, slowest |

## How the AI evaluates a position

| Factor | Value |
|---|---|
| Pawn / Knight / Bishop / Rook / Queen | 100 / 320 / 330 / 500 / 900 |
| Centre control | Bonus that grows as a piece gets closer to the centre |
| Giving check | +50 |
| Checkmate | Winning score |

Moves are searched in order (captures first, ranked by the value of the captured piece) so Alpha-Beta can cut off weak branches early.

## Project structure

```
src/
├── pieces/   Piece (abstract) + Pawn, Knight, Bishop, Rook, Queen, King
├── board/    Board: 8x8 grid, move execution, en passant checks
├── game/     ChessGame: turns, rule enforcement, game state
├── ai/       AIPlayer: Minimax + Alpha-Beta, evaluation, move ordering
└── ui/       ChessFrame (window, game loop), GamePanel (board drawing, mouse input), SidePanel (status, strength, captures, moves), Theme (colours, fonts, buttons)
```

Each piece class computes its own legal moves (polymorphism over the abstract `Piece`), so the board and game logic never need to know piece-specific rules.

## Getting started

**Requirements:** JDK 8 or newer. No external libraries.

```bash
git clone https://github.com/ZainAftab-dev/Chess-Game.git
cd Chess-Game

# compile
javac -d bin $(find src -name "*.java")

# run
java -cp bin ui.ChessFrame
```

On Windows (PowerShell):

```powershell
javac -d bin (Get-ChildItem -Recurse src\*.java).FullName
java -cp bin ui.ChessFrame
```

Or open the folder in IntelliJ IDEA / Eclipse / VS Code and run `ui.ChessFrame`.

## How to play

1. You are White. Click one of your pieces to see its legal moves.
2. Click a highlighted square to move.
3. Black (the AI) replies automatically.
4. Change the computer's strength at any time, or press **New game** to start over.

## Fixes in the latest version

- `Board.copy()` started from the opening position and only overwrote occupied squares, so test boards kept "ghost" pieces on squares that had since emptied. Check detection, legal-move filtering and the AI's search all used these copies. Copies now match the real board.
- The game state was worked out before the turn passed to the other player, so checkmate and stalemate were never reported. It is now evaluated for the side that has to move.
- You can no longer move Black's pieces while the computer is thinking, and pressing **New game** mid-search no longer lets the old search play a move on the new board.

## Known limitations

- No threefold-repetition or 50-move draw rules
- Promotion always gives a queen
- No move history export (PGN) or save/load

## Author

**Zain Aftab** · [GitHub](https://github.com/ZainAftab-dev) · [LinkedIn](https://www.linkedin.com/in/zain-aftab-563727295/)

## License

[MIT](LICENSE)
