package qirkat;
import java.util.ArrayList;
import static qirkat.PieceColor.*;

/** A Player that computes its own moves.
 *  @author Casper Yang
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 5;
    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A position magnitude indicating a win. */
    private static final int LOOSING_VALUE = Integer.MIN_VALUE + 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that  really will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        if (move != null) {
            System.out.println(myColor().toString()
                    + " moves " + move.toString() + ".");
        }
        return move;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == WHITE) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0) {
            return staticScore(board);
        }
        if (board.getMoves().isEmpty()) {
            if (board.whoseMove() == WHITE) {
                return LOOSING_VALUE;
            } else {
                return WINNING_VALUE;
            }
        }
        int best;
        Move bestMove;
        bestMove = null;
        if (sense == 1) {
            best = Integer.MIN_VALUE;
            ArrayList<Move> moves = board.getMoves();
            for (Move move: moves) {
                int val;
                Board newBoard = new Board(board);
                newBoard.makeMove(move);
                val = findMove(newBoard, depth - 1, false, sense, alpha, beta);
                if (val >= best) {
                    bestMove = move;
                    best = Math.max(val, best);
                    alpha = Math.max(best, alpha);
                }
                if (beta <= alpha) {
                    break;
                }
            }
            if (saveMove) {
                _lastFoundMove = bestMove;
            }
            return best;
        } else {
            best = INFTY;
            ArrayList<Move> moves = board.getMoves();
            for (Move move : moves) {
                int val;
                Board newBoard = new Board(board);
                newBoard.makeMove(move);
                val = findMove(newBoard, depth - 1, false, sense, alpha, beta);
                if (val < best) {
                    bestMove = move;
                    best = Math.min(best, val);
                    beta = Math.min(best, beta);
                }
                if (beta <= alpha) {
                    break;
                }
            }
            if (saveMove) {
                _lastFoundMove = bestMove;
            }
        }
        return best;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
<<<<<<< HEAD
        int whiteCount = board.getWhitePieces();
        int blackCount = board.getBlackPieces();
        return whiteCount - blackCount;
=======
        return  board.getColorPieces(myColor().opposite()) - board.getColorPieces(myColor());
>>>>>>> newbranch
    }
}
