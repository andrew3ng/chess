package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/*
 * TODO: 
 * - Implement Check System
 * - Implement Critical System
 * - Implement King Movement
 * - Implement Turn System
 * - Create a system to parse the user input
 */

/**
 * Chess Board to hold the game on
 * 8x8 board that takes the inputs the user gives
 * 
 * @author andreweng
 *
 */
public class Board {
	private Scanner in;
	private final char[] xAxis = "ABCDEFGH".toCharArray();
	
	private Piece[][] board = new Piece[8][8];
	private Piece[][] team = new Piece[2][16]; // 0 is white, 1 is black
	
	// If the piece is returned to within the same turn, then no need to recalculate the moves it can do
	// Resets with each turn
	private HashMap<Piece, ArrayList<int[]>> possibleMoves = new HashMap<Piece, ArrayList<int[]>>();
	
	int turn = 0; // To know whose turn it is
	
	// Constructor and Helper Methods \\
	/**
	 * Standard board constructor for a normal game
	 */
	public Board() {
		createTeam(Color.WHITE);
		createTeam(Color.BLACK);
		in = new Scanner(System.in);
	}
	
	/**
	 * Debugging for board; creates a game for testing movement
	 * 
	 * @param i
	 */
	public Board(int i) {
		board[3][3] = new Piece(Color.WHITE, Role.QUEEN, 0);
		board[1][2] = new Piece(Color.WHITE, Role.BISHOP, 0);
		board[5][4] = new Piece(Color.WHITE, Role.KNIGHT, 0);
		board[2][5] = new Piece(Color.WHITE, Role.PAWN, 0);
		board[3][4] = new Piece(Color.BLACK, Role.BISHOP, 0);
		board[3][0] = new Piece(Color.BLACK, Role.ROOK, 0);
		System.out.println(getMoves(calcMoves(board[3][3], 3, 3)));
		System.out.println(getMoves(calcMoves(board[1][2], 1, 2)));
		System.out.println(getMoves(calcMoves(board[5][4], 5, 4)));
		System.out.println(getMoves(calcMoves(board[2][5], 2, 5)));
		
	}
	
	/**
	 * Creates the teams and places the respective pieces from left to right
	 * Designated only for standard games and used only in the constructor
	 * 
	 * @param color
	 *            Indicates which team is being created
	 */
	private void createTeam(Color color) {
		int ord = color.ordinal();
		// Unique Pieces
		team[ord][0] = new Piece(color, Role.KING, 0);
		team[ord][1] = new Piece(color, Role.QUEEN, 0);
		board[7 * ord][4] = team[ord][0]; // King's opposite of each other
		board[7 * ord][3] = team[ord][1]; // Queen's on her color
		
		for (int i = 0; i < 2; i++) { // For 2 of a kind (Bishops, Knights, and Rooks)
			// If White and first, then off = 0
			// If White and second, then off = 1
			// If Black and first, then off = 1
			// If Black and second, then off = 0
			int off = ((ord + i) % 2); // So everything goes from player's left to right
			
			team[ord][2 + i] = new Piece(color, Role.BISHOP, i + 1);
			team[ord][4 + i] = new Piece(color, Role.KNIGHT, i + 1);
			team[ord][6 + i] = new Piece(color, Role.ROOK, i + 1);
			// Place on board
			board[7 * ord][off * (board.length - 1) + (int) (Math.pow(-1, off) * 2)] = team[ord][2 + i];
			board[7 * ord][off * (board.length - 1) + (int) (Math.pow(-1, off))] = team[ord][4 + i];
			board[7 * ord][off * (board.length - 1)] = team[ord][6 + i];
		}
		for (int i = 0; i < 8; i++) { // For Pawns
			team[ord][8 + i] = new Piece(color, Role.PAWN, i + 1);
			board[1 + (5 * ord)][((board.length - 1) * ord) + (int) (Math.pow(-1, ord) * i)] = team[ord][8 + i];
		}
	}
	
	// Play/Game Method \\
	
	/**
	 * The main function of the Board. Will cycle through each player's turn
	 * 1. Asks for the player to select a piece to move either by name or coord
	 * 2. Calculates the potential moves the piece can do
	 * 3. If the piece cannot move, it will ask the player to select another piece
	 * 4. Once calc'd, the player is asked to select a location to move to
	 * 5. Perform chosen action
	 * 6. Check if the opposing player is in check
	 * 7. If in check, notify the opposing player
	 * 8. Check for the opposing player's critical pieces
	 * 9. If all pieces cannot move and the king is in check, call checkmate
	 * 10. If all pieces are critical but the king is not in check, call stalemate
	 * 10. Update turn counter
	 * 11. Next player's turn begins
	 */
	public void play() {
		
	}
	
	// Move Calc Methods \\
	
	/**
	 * This will calculate moves for a specific piece
	 * Only calculates moves for any piece that might be able to move
	 * Pawns, Knights, and Kings get their own unique calculation
	 * Rooks, Bishops, and Queens share calculated movement patterns
	 * 
	 * @param piece
	 *            The piece we are checking
	 * @param x
	 *            Its x location on the map
	 * @param y
	 *            Its y location on the map
	 */
	private ArrayList<int[]> calcMoves(Piece piece, int x, int y) {
		if (!possibleMoves.containsKey(piece) && piece.moveable) {
			ArrayList<int[]> moves = new ArrayList<int[]>();
			// Special pieces
			if (piece.role == Role.PAWN)
				moves = calcPawn(piece, x, y);
			else if (piece.role == Role.KNIGHT)
				moves = calcKnight(piece, x, y);
			else if (piece.role == Role.KING)
				moves = calcKing(piece, x, y);
			else { // For Bishops, Rooks, and Queens
				if (piece.role != Role.BISHOP)
					moves.addAll(calcPlus(piece, x, y));
				if (piece.role != Role.ROOK)
					moves.addAll(calcDiag(piece, x, y));
			}
			possibleMoves.put(piece, moves); // Adds the set of moves to the list
			if (moves.isEmpty()) // We don't even need to do this again if this is the case
				piece.moveable = false;
			return moves;
		}
		return possibleMoves.get(piece);
		
	}
	
	/**
	 * Calculates the possible moves for a pawn
	 * If unblocked, the pawn can move forward
	 * 
	 * If a special move is possible, the pawn can move 2 spaces forward
	 * 
	 * If an opposing piece is diagonal to it, the pawn can take it
	 * 
	 * En Passant: If an opposing pawn is next to it and that pawn's
	 * special = 1, then the pawn can take the diagonal above it
	 * 
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private ArrayList<int[]> calcPawn(Piece piece, int x, int y) {
		ArrayList<int[]> moves = new ArrayList<int[]>();
		int inc = (int) (Math.pow(-1, piece.team.ordinal()));
		if (board[x + inc][y] == null) { // Normal Step
			moves.add(new int[] { x + inc, y });
			// Bonus Step
			if (piece.special == 0 && board[x + (2 * inc)][y] == null)
				moves.add(new int[] { x + (2 * inc), y });
		}
		for (int i = 0; i < 2; i++) { // Attacking and En Passant
			int step = (int) Math.pow(-1, i);
			if (y + step >= 0 && y + step < board.length) {
				if (board[x + inc][y + step] != null && board[x + inc][y + step].team != piece.team)
					moves.add(new int[] { x + inc, y + step });
				if (board[x][y + step] != null && board[x][y + step].team != piece.team // En Passant
						&& board[x][y + step].role == Role.PAWN && board[x][y + step].special == 1)
					moves.add(new int[] { x + inc, y + step });
			}
		}
		return moves;
	}
	
	/**
	 * Calculates the possible moves for a knight
	 * Can move two spaces, then one space over
	 * Can jump over other pieces, but cannot land on allies
	 * 
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private ArrayList<int[]> calcKnight(Piece piece, int x, int y) {
		ArrayList<int[]> moves = new ArrayList<int[]>();
		for (int i = 0; i < 4; i++) {
			int next1 = (int) (Math.pow(-1, (i + (i / 2)) % 2)); // One Step
			int next2 = 2 * (int) (Math.pow(-1, i)); // Two Step
			if (x + next1 >= 0 && x + next1 < board.length && y + next2 >= 0 && y + next2 < board.length) {
				if (board[x + next1][y + next2] == null || board[x + next1][y + next2].team != piece.team)
					moves.add(new int[] { x + next1, y + next2 });
			}
			if (x + next2 >= 0 && x + next2 < board.length && y + next1 >= 0 && y + next1 < board.length) {
				if (board[x + next2][y + next1] == null || board[x + next2][y + next1].team != piece.team)
					moves.add(new int[] { x + next2, y + next1 });
			}
		}
		return moves;
	}
	
	/**
	 * Calculates the possible moves for a king
	 * If an adjacent square is not occupied by an ally and
	 * does not put the king in check, the king can move there
	 * 
	 * Castle: If the king hasn't moved, one of the two rooks
	 * haven't moved, and there are no pieces in between them,
	 * then the king and the rook can swap places and castle
	 * 
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private ArrayList<int[]> calcKing(Piece piece, int x, int y) {
		ArrayList<int[]> moves = new ArrayList<int[]>();
		
		return moves;
	}
	
	/**
	 * Calculates the moves for a bishop and some moves for a queen
	 * Sees how far on a diagonal the piece can move until it hits
	 * some piece or the edge of the board
	 * 
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private ArrayList<int[]> calcDiag(Piece piece, int x, int y) {
		ArrayList<int[]> moves = new ArrayList<int[]>();
		int toLower = board.length - x - 1;
		int toRight = board.length - y - 1;
		for (int i = 1; i <= Math.min(x, y); i++) {
			if (board[x - i][y - i] == null)
				moves.add(new int[] { x - i, y - i });
			else if (board[x - i][y - i].team != piece.team) {
				moves.add(new int[] { x - i, y - i });
				break;
			}
			else
				break;
		}
		for (int i = 1; i <= Math.min(x, toRight); i++) {
			if (board[x - i][y + i] == null)
				moves.add(new int[] { x - i, y + i });
			else if (board[x - i][y + i].team != piece.team) {
				moves.add(new int[] { x - i, y + i });
				break;
			}
			else
				break;
		}
		for (int i = 1; i <= Math.min(toLower, y); i++) {
			if (board[x + i][y - i] == null)
				moves.add(new int[] { x + i, y - i });
			else if (board[x + i][y - i].team != piece.team) {
				moves.add(new int[] { x + i, y - i });
				break;
			}
			else
				break;
		}
		for (int i = 1; i <= Math.min(toLower, toRight); i++) {
			if (board[x + i][y + i] == null)
				moves.add(new int[] { x + i, y + i });
			else if (board[x + i][y + i].team != piece.team) {
				moves.add(new int[] { x + i, y + i });
				break;
			}
			else
				break;
		}
		return moves;
	}
	
	/**
	 * Calculates the moves for a rook and some moves for a queen
	 * Sees how far on a row or column the piece can move until
	 * it hits some piece or the edge of the board
	 * 
	 * @param piece
	 * @param x
	 * @param y
	 * @return
	 */
	private ArrayList<int[]> calcPlus(Piece piece, int x, int y) {
		ArrayList<int[]> moves = new ArrayList<int[]>();
		for (int i = x + 1; i < board.length; i++) {
			if (board[i][y] == null)
				moves.add(new int[] { i, y });
			else if (board[i][y].team != piece.team) {
				moves.add(new int[] { i, y });
				break;
			}
			else
				break;
			
		}
		for (int i = x - 1; i >= 0; i--) {
			if (board[i][y] == null)
				moves.add(new int[] { i, y });
			else if (board[i][y].team != piece.team) {
				moves.add(new int[] { i, y });
				break;
			}
			else
				break;
		}
		for (int j = y + 1; j < board.length; j++) {
			if (board[x][j] == null)
				moves.add(new int[] { x, j });
			else if (board[x][j].team != piece.team) {
				moves.add(new int[] { x, j });
				break;
			}
			else
				break;
		}
		for (int j = y - 1; j >= 0; j--) {
			if (board[x][j] == null)
				moves.add(new int[] { x, j });
			else if (board[x][j].team != piece.team) {
				moves.add(new int[] { x, j });
				break;
			}
			else
				break;
		}
		return moves;
	}
	
	// Debugging Methods \\
	/**
	 * Creates a string representation of the standard board from the perspective of the current player's turn
	 * Where (0,0) will be where WR1 is initially and (7,7) will be where BR1 is initially
	 */
	@Override
	public String toString() {
		String s = "  ";
		
		// Sets up the parameters
		int t = turn % 2;
		int inc = (int) Math.pow(-1, t); // 1 when t == 0, -1 when t == 1
		int start = (board.length - 1) * t; // 0 when t == 0, 7 when t == 1
		int end = ((board.length - 1) * (1 - t)) + inc; // 8 when t == 0, -1 when t == 1
		
		// X Axis
		for (int i = start; i != end; i += inc)
			s += "  " + xAxis[i] + "  "; // 5 units
		s += "  \n";
		
		// Y Axis and Board
		for (int i = start; i != end; i += inc) {
			s += (8 - i) + " ";
			for (int j = start; j != end; j += inc) // for testing: " + i + "," + j + "
				s += (board[board.length - 1 - i][j] == null) ? "[   ]" : "[" + board[board.length - 1 - i][j].toString() + "]";
			s += " " + (8 - i) + "\n";
		}
		
		// X Axis
		s += "  ";
		for (int i = start; i != end; i += inc)
			s += "  " + xAxis[i] + "  "; // 5 units
		s += "\n";
		
		return s;
	}
	
	/**
	 * Creates a String Representation of the Board from the perspective of the
	 * current player's turn with all of the potential moves a piece can do
	 * 
	 * @param moves
	 *            The set of moves a player can do
	 * @return
	 */
	public String getMoves(ArrayList<int[]> moves) {
		String s = "  ";
		boolean can;
		
		// Sets up the parameters
		int t = turn % 2;
		int inc = (int) Math.pow(-1, t); // 1 when t == 0, -1 when t == 1
		int start = (board.length - 1) * t; // 0 when t == 0, 7 when t == 1
		int end = ((board.length - 1) * (1 - t)) + inc; // 8 when t == 0, -1 when t == 1
		
		// X Axis
		for (int i = start; i != end; i += inc)
			s += "  " + xAxis[i] + "  "; // 5 units
		s += "  \n";
		
		// Y Axis and Board
		for (int i = start; i != end; i += inc) {
			s += (8 - i) + " ";
			for (int j = start; j != end; j += inc) {// for testing: " + i + "," + j + "
				can = false;
				for (int[] loc : moves) {
					if (loc[0] == board.length - i - 1 && loc[1] == j) {
						can = true;
						s += "[ X ]";
						break;
					}
				}
				if (!can)
					s += (board[board.length - 1 - i][j] == null) ? "[   ]" : "[" + board[board.length - 1 - i][j].toString() + "]";
			}
			s += " " + (8 - i) + "\n";
		}
		
		// X Axis
		s += "  ";
		for (int i = start; i != end; i += inc)
			s += "  " + xAxis[i] + "  "; // 5 units
		s += "\n";
		return s;
	}
}