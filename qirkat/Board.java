package qirkat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import static qirkat.PieceColor.*;
import static qirkat.Move.*;

/** A Qirkat board.   The squares are labeled by column (a char value between
 *  'a' and 'e') and row (a char value between '1' and '5'.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (with row 0 being the bottom row)
 *  counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Casper Yang
 */
class Board extends Observable {

    /** A new, cleared board at the start of the game. */
    Board() {
        _board = new PieceColor[Move.SIDE * Move.SIDE];
        _blackPiece = 0;
        _whitePiece = 0;
        moveStack = new Stack<>();
        noLookingBack = new int[Move.SIDE * Move.SIDE];
        clear();
    }

    /** A copy of B. */
    Board(Board b) {
        internalCopy(b);
    }

    /** Return a constant view of me (allows any access method, but no
     *  method that modifies it). */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions. */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;
        _blackPiece = 12;
        _whitePiece = 12;

        for (char i = 'a'; i <= 'e'; i++) {
            for (char j = '1'; j <= '2'; j++) {
                set(i, j, WHITE);
            }
        }
        for (char i = 'a'; i <= 'e'; i++) {
            for (char j = '4'; j <= '5'; j++) {
                set(i, j, BLACK);
            }
        }
        set('d', '3', WHITE);
        set('e', '3', WHITE);
        set('a', '3', BLACK);
        set('b', '3', BLACK);
        set('c', '3', EMPTY);
        setChanged();
        notifyObservers();
    }

    /** Copy B into me. */
    void copy(Board b) {
        internalCopy(b);
    }

    /** Copy B into me. */
    private void internalCopy(Board b) {
        _board = b._board.clone();
        Stack<Move> copy = new Stack<>();
        copy.addAll(b.moveStack);
        this.moveStack = copy;
        this._whitePiece = b._whitePiece;
        this._blackPiece = b._blackPiece;
        this._whoseMove = b.whoseMove();
        this.noLookingBack = b.noLookingBack;
        this._gameOver = b._gameOver;
    }


    /** Set my contents as defined by STR.  STR consists of 25 characters,
     *  each of which is b, w, or -, optionally interspersed with whitespace.
     *  These give the contents of the Board in row-major order, starting
     *  with the bottom row (row 1) and left column (column a). All squares
     *  are initialized to allow horizontal movement in either direction.
     *  NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }
        _whoseMove = nextMove;
        for (int k = 0; k < str.length(); k += 1) {
            switch (str.charAt(k)) {
            case '-':
                set(k, EMPTY);
                break;
            case 'b': case 'B':
                set(k, BLACK);
                break;
            case 'w': case 'W':
                set(k, WHITE);
                break;
            default:
                break;
            }
        }
        noLookingBack = new int[Move.SIDE * Move.SIDE];
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Board) {
            Board b = (Board) object;
            boolean player = _whoseMove == b.whoseMove();
            boolean gameOver = _gameOver == b.gameOver();
            boolean stack = moveStack.equals(b.moveStack);
            boolean board = toString().equals(b.toString());
            return player && gameOver && stack && board;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** Return true iff the game is over: i.e., if the current player has
     *  no moves. */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current contents of square C R, where 'a' <= C <= 'e',
     *  and '1' <= R <= '5'.  */
    PieceColor get(char c, char r) {
        assert validSquare(c, r);
        return _board[index(c, r)];
    }

    /** Return the current contents of the square at linearized index K. */
    PieceColor get(int k) {
        assert validSquare(k);
        return _board[k];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'e', and
     *  '1' <= R <= '5'. */
    private void set(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /** Set get(K) to V, where K is the linearized index of a square. */
    private void set(int k, PieceColor v) {
        assert validSquare(k);
        _board[k] = v;
    }

    /** A Array to act as flag, to apply to the additional rule. */
    private int[] noLookingBack;

    /** The boxes that whites cannot move horizontally. */
    private int enimyZone = Move.SIDE * Move.SIDE - 6;

    /** Return true iff MOV is legal on the current board. */
    boolean legalMove(Move mov) {
        int toIndex = mov.toIndex();
        int fromIndex = mov.fromIndex();
        if (mov != null && get(mov.fromIndex()) != _whoseMove) {
            return false;
        }
        if (get(fromIndex) == EMPTY) {
            return false;
        }
        if (get(toIndex) != EMPTY) {
            return false;
        }
        if (_whoseMove == WHITE && !mov.isJump() && fromIndex > enimyZone) {
            return false;
        }
        if (fromIndex < 5 && !mov.isJump() && _whoseMove == BLACK) {
            return false;
        }
        if (mov != null && mov.isJump()) {
            return checkJump(mov, false);
        } else {
            if (_whoseMove == WHITE) {
                if (noLookingBack[fromIndex] == 1
                        && fromIndex - toIndex == 1) {
                    return false;
                } else if (noLookingBack[fromIndex] == -1
                        && fromIndex - toIndex == -1) {
                    return false;
                } else if (!legalMoveHelper(mov, WHITE)) {
                    return false;
                }
                return true;
            } else {
                if (noLookingBack[fromIndex] == 1
                        && fromIndex - toIndex == 1) {
                    return false;
                } else if (noLookingBack[fromIndex] == -1
                        && fromIndex - toIndex == -1) {
                    return false;
                } else if (!legalMoveHelper(mov, BLACK)) {
                    return false;
                }
                return true;
            }
        }
    }

    /** Return true iff MOV is legal on the current board.
     * @param color a color
     * @param mov  a move. */
    boolean legalMoveHelper(Move mov, PieceColor color) {
        int toIndex = mov.toIndex();
        int fromIndex = mov.fromIndex();
        int diff = toIndex - fromIndex;
        if (color == WHITE) {
            if (fromIndex % 2 == 0) {
                if (diff == 5 || diff == 4
                        || diff == 6 || diff == -1 || diff == 1) {
                    return true;
                }
            }
            if (fromIndex % 2 == 1) {
                if (diff == 5 || diff == -1 || diff == 1) {
                    return true;
                }
            }
            return false;
        } else {
            if (fromIndex % 2 == 0) {
                if (diff == -5 || diff == -4
                        || diff == -6 || diff == -1 || diff == 1) {
                    return true;
                }
            }
            if (fromIndex % 2 == 1) {
                if (diff == -5 || diff == -1 || diff == 1) {
                    return true;
                }
            }
            return false;
        }
    }
    /** Return a list of all legal moves from the current position. */
    ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        return result;
    }

    /** Add all legal moves from the current position to MOVES. */
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        if (jumpPossible()) {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getJumps(moves, k);
            }
        } else {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getMoves(moves, k);
            }
        }
    }
    /** A list of all possible moves made by white.
     * @param k a index
     * @return int[].*/
    private int[] whiteMoves(int k) {
        if (k % 2 == 0) {
            if (k % 5 == 0) {
                return new int[] { k + 1, k + 6, k + 5};
            }
            if (k % 5 == 4) {
<<<<<<< HEAD
                return new int[] {k - 6, k - 1, k + 4, k + 5};
=======
                return new int[] { k + 4, k + 5, k - 1};
            }
            if (k % 5 == 2 || k % 5 == 1 || k % 5 == 3) {
                return new int[] { k + 6,
                     k + 4, k + 1, k - 1, k + 5};
>>>>>>> newbranch
            }
        }
        return new int[] {k + 5, k + 1};
    }
    /** A list of all possible moves made by black.
     * @param k a index
     * @return int[]. */
    private int[] blackMoves(int k) {
        if (k % 2 == 0) {
            if (k % 5 == 0) {
                return new int[] {k - 4, k + 1, k - 5};
            }
            if (k % 5 == 4) {
<<<<<<< HEAD
                return new int[] {k - 6, k - 1, k + 4, k - 5};
=======
                return new int[] {k - 6, k - 1, k - 5};
            }
            if (k % 5 == 2 || k % 5 == 1 || k % 5 == 3) {
                return new int[] {k - 6,
                    k - 4,  k - 1, k + 1, k - 5};
>>>>>>> newbranch
            }
        }
        return new int[] {k - 5, k - 1, k + 1};
    }
    /** Add all legal non-capturing moves from the position
     *  with linearized index K to MOVES. */
    private void getMoves(ArrayList<Move> moves, int k) {
<<<<<<< HEAD
        int[] wlocation = whiteavailableMoves(k);
        int[] blocation = blackavailableMoves(k);
=======

        int[] w = whiteMoves(k);
        int[] b = blackMoves(k);

>>>>>>> newbranch
        if (get(k) == _whoseMove) {
            if (_whoseMove == WHITE) {
                for (int i : w) {
                    if (validSquare(i)) {
                        Move mov = Move.move(col(k), row(k), col(i), row(i), null);
                        if (legalMove(mov)) {
                            moves.add(mov);
                        }
                    }
                }
            }
            if (_whoseMove == BLACK) {
                for (int i : b) {
                    if (validSquare(i)) {
                        Move mov = Move.move(col(k), row(k), col(i), row(i), null);
                        if (legalMove(mov)) {
                            moves.add(mov);
                        }
                    }
                }
            }

        }
    }

    /** Return true the index of where k can jump to.
     * @param k */
    private int [] indexJump(int k) {
        if (k % 2 == 0) {
            if (k % 5 == 2) {
                return new int[]{k + 10,
                    k - 10, k + 2, k - 2, k + 12, k + 8, k - 8, k - 12};
            }
            if (k % 5 == 0 || k % 5 == 1) {
                return new int[]{k + 10, k - 10, k + 2, k + 12,  k - 8};
            }
            if (k % 5 == 3 || k % 5 == 4) {
                return new int[]{k + 10, k - 10, k - 2, k + 8, k - 12};
            }
        } else {
            if (k % 5 == 2) {
                return new int[]{k + 10, k - 10, k + 2, k - 2};
            }
            if (k % 5 == 0 || k % 5 == 1) {
                return new int[]{k + 10, k - 10, k + 2};
            }
            if (k % 5 == 3 || k % 5 == 4) {
                return new int[]{k + 10, k - 10, k - 2};
            }
        }
        return new int[]{};
    }

    /** Add all legal captures from the position with linearized index K
     *  to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        if (!jumpPossible(k)) {
            return;
        }
        for (int i : indexJump(k)) {
            if (validSquare(i)) {
                Board b0 = new Board(this);
                b0.copy(this);
                Move current = Move.move(col(k), row(k), col(i), row(i));
                int jumpedIndex = index(current.jumpedCol(),
                        current.jumpedRow());
                if (checkJump(current, true)) {
                    b0.set(i, get(k));
                    b0.set(jumpedIndex, EMPTY);
                    ArrayList<Move> history = new ArrayList<>();
                    b0.getJumps(history, i);
                    if (history.size() == 0) {
                        history.add(null);
                    }
                    for (Move mov : history) {
                        Move thisLevel = new Move(current);
                        Move jump = Move.move(thisLevel, mov);
                        moves.add(jump);
                    }
                }
            }
        }
    }

    /** Return true iff MOV is a valid jump sequence on the current board.
     *  MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     *  could be continued and are valid as far as they go.  */
    boolean checkJump(Move mov, boolean allowPartial) {
        if (mov == null) {
            return true;
        } else {
            int between = (mov.fromIndex() + mov.toIndex()) / 2;
            if (_board[between] != _board[mov.fromIndex()]
                    && _board[between] != EMPTY
                    && _board[mov.toIndex()] == EMPTY) {
                if (allowPartial) {
                    return true;
                } else {
                    return checkJump(mov.jumpTail(), allowPartial);
                }
            } else {
                return false;
            }
        }
    }

    /** Return true iff a jump is possible for a piece at position C R. */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /** Return true iff a jump is possible for a piece at position with
     *  linearized index K. */
    boolean jumpPossible(int k) {
        for (int i : indexJump(k)) {
            if (validSquare(i)) {
                int between = (k + i) / 2;
                if ((!(_board[between] == EMPTY))
                        && (_board[between] == (_board[k].opposite()))
                        && (_board[i] == EMPTY)
                        && (get(k).equals(_whoseMove))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return true iff a jump is possible from the current board. */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }
    /** Return the number of black pieces on the board. */
    int getBlackPieces() {
        int count = 0;
        for (PieceColor piece : _board) {
            if (piece == BLACK) {
                count += 1;
            }
        }
        return count;
    }
    /** Return the number of white pieces on the board. */
    int getWhitePieces() {
        int count = 0;
        for (PieceColor piece : _board) {
            if (piece == WHITE) {
                count += 1;
            }
        }
        return count;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        makeMove(Move.move(c0, r0, c1, r1, null));
    }

    /** Make the multi-jump C0 R0-C1 R1..., where NEXT is C1R1....
     *  Assumes the result is legal. */
    void makeMove(char c0, char r0, char c1, char r1, Move next) {
        makeMove(Move.move(c0, r0, c1, r1, next));
    }
    /** A stack that stores all moves. */
    private Stack<Move> moveStack;

    /** Make the Move MOV on this Board, assuming it is legal. */
    void makeMove(Move mov) {
        assert legalMove(mov);
            int fromindex = index(mov.col0(), mov.row0());
            int toindex = index(mov.col1(), mov.row1());
            if (mov.isJump()) {
                Move temp = mov;
                while (temp != null) {
                    if (get(mov.jumpedIndex()) == BLACK) {
                        _blackPiece -= 1;
                    } else {
                        _whitePiece -= 1;
                    }
                    set(temp.jumpedCol(), temp.jumpedRow(), EMPTY);
                    set(temp.toIndex(), _whoseMove);
                    set(temp.fromIndex(), EMPTY);
                    noLookingBack[temp.jumpedIndex()] = 0;
                    temp = temp.jumpTail();
                }
                noLookingBack[fromindex] = 0;
            } else {
                set(toindex, _whoseMove);
                set(fromindex, EMPTY);
                if (mov.isLeftMove()) {
                    noLookingBack[toindex] = -1;
                    noLookingBack[fromindex] = 0;
                }
                if (mov.isRightMove()) {
                    noLookingBack[toindex] = 1;
                    noLookingBack[fromindex] = 0;

                }
            }
            _whoseMove = _whoseMove.opposite();
            if (isNotMove()) {
                _gameOver = true;
            }
            moveStack.push(mov);
            setChanged();
            notifyObservers();
        }
<<<<<<< HEAD


=======
    }
    /** Return true iff there is a move for the current player. */
>>>>>>> newbranch
    private boolean isNotMove() {
        return getMoves().size() == 0;
    }
    /** Undo the last move, if any. */
    void undo() {
        if (moveStack != null) {
            Move move = moveStack.pop();
            if (!move.isJump()) {
                PieceColor temp = get(move.toIndex());
                set(move.toIndex(), EMPTY);
                set(move.fromIndex(), temp);
            } else if (move.isJump() && move.jumpTail() == null) {
                PieceColor temp = get(move.toIndex());
                set(move.toIndex(), EMPTY);
                set(index(move.jumpedCol(), move.jumpedRow()), temp.opposite());
                set(move.fromIndex(), temp);
            } else {
                Move temp = move;
                Stack<Move> tempStack = new Stack<>();
                while (temp != null) {
                    Move jump = move(temp.col0(), temp.row0(),
                            temp.jumpedCol(), temp.jumpedRow());
                    tempStack.push(jump);
                    temp = temp.jumpTail();
                }
                while (!tempStack.empty()) {
                    Move jump = tempStack.pop();
                    set(jump.toIndex(), EMPTY);
                    set(index(jump.jumpedCol(), jump.jumpedRow()), _whoseMove);
                    set(jump.fromIndex(), _whoseMove.opposite());
                }
            }
        }
        _whoseMove = _whoseMove.opposite();
        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /** Return a text depiction of the board.  If LEGEND, supply row and
     *  column numbers around the edges. */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        for (char j = '5'; j >= '1'; j--) {
            out.format(" ");
            for (char i = 'a'; i <= 'e'; i++) {
                PieceColor pc = get(i, j);
                String p = "";
                out.format(" ");
                if (pc.equals(EMPTY)) {
                    p = "-";
                } else if (pc.equals(BLACK)) {
                    p = "b";
                } else if (pc.equals(WHITE)) {
                    p = "w";
                }
                out.format("%s", p);
            }
            if (j == '1') {
                return out.toString();
            } else {
                out.format("\n");
            }
        }
        return out.toString();
    }


    /** @param color
     * Return the number of pieces of a color on the board. */
    int getColorPieces(PieceColor color) {
        if (color == WHITE) {
            return getBlackPieces();
        } else {
            return getWhitePieces();
        }
    }

    /** An arraylist to store all moves. */
    private ArrayList<Move> _allMoves = new ArrayList<Move>();

    /** Initialize a game board.*/
    private PieceColor[] _board;

    /** A black piece. */
    private int _blackPiece;

    /** A white piece. */
    private int _whitePiece;

    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Set true when game ends. */
    private boolean _gameOver;

    int getColor(PieceColor color) {
        if (color == WHITE) {
            return getWhitePieces();
        } else {
            return getBlackPieces();
        }
    }

    /** Convenience value giving values of pieces at each ordinal position. */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();

    /** One cannot create arrays of ArrayList<Move>, so we introduce
     *  a specialized private list type for this purpose. */
    private static class MoveList extends ArrayList<Move> {
    }

    /** A read-only view of a Board. */
    private class ConstantBoard extends Board implements Observer {
        /** A constant view of this Board. */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /** Undo the last move. */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }
}
