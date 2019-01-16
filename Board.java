package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/*
 * TODO: 
 * - Apply Check System to movement options
 * - Apply Critical System to movement options
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
	
	// Locations of each piece
	private HashMap<Piece, int[]> locations = new HashMap<Piece, int[]>();
	
	// If the piece is returned to within the same turn,
	// then no need to recalculate the moves it can do
	// Resets with each turn
	private HashMap<Piece, ArrayList<int[]>> possibleMoves = new HashMap<Piece, ArrayList<int[]>>();
	
	// If we're in check, we need to check every possible space
	// that can take us out from check asides from moving the King
	private ArrayList<int[]> checkedMoves = new ArrayList<int[]>();
	
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
		// Update Locations
		locations.put(team[ord][0], new int[] { 7 * ord, 4 });
		locations.put(team[ord][1], new int[] { 7 * ord, 3 });
		
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
			// Update Locations
			locations.put(team[ord][2 + i], new int[] { 7 * ord, off * (board.length - 1) + (int) (Math.pow(-1, off) * 2) });
			locations.put(team[ord][4 + i], new int[] { 7 * ord, off * (board.length - 1) + (int) (Math.pow(-1, off)) });
			locations.put(team[ord][6 + i], new int[] { 7 * ord, off * (board.length - 1) });
			
		}
		for (int i = 0; i < 8; i++) { // For Pawns
			team[ord][8 + i] = new Piece(color, Role.PAWN, i + 1);
			// Put on the board
			board[1 + (5 * ord)][((board.length - 1) * ord) + (int) (Math.pow(-1, ord) * i)] = team[ord][8 + i];
			// Update Locations
			locations.put(team[ord][8 + i], new int[] { 1 + (5 * ord), ((board.length - 1) * ord) + (int) (Math.pow(-1, ord) * i) });
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
		Piece selected = null; // Selected Piece
		int[] origin = null;
		int[] loc = null; // Selected Location
		
		boolean finish = false; // Has the game finished?
		boolean chosenP = false; // Has the Piece been chosen?
		boolean chosenM = false; // Has the Move been chosen?
		boolean valid = false; // Is the move valid?
		
		while (!finish) {
			// Updates & Checks
			for (Piece p : team[(turn + 1) % 2]) {
				if (p != null && isCheck(p, locations.get(team[turn % 2][0]))) {
					Piece.check[turn % 2] = true;
					team[turn % 2][0].critical = p;
					break;
				}
			}
			criticalCheck();
			
			// Updates pawns so they can't be caught in en passant anymore
			for (int i = 8; i < 16; i++) {
				if (team[turn % 2][i] != null && team[turn % 2][i].special == 1)
					team[turn % 2][i].special = -1;
			}
			
			// Choose a piece
			chosenP = false;
			chosenM = false;
			valid = false;
			while (!chosenP) {
				System.out.println(this);
				
				if (Piece.check[turn % 2]) // If the king's in check
					System.out.println("You're in Check!");
				
				System.out.print("Which piece would you like to move? ");
				selected = Parser.getPiece(in.next().toUpperCase(), board);
				System.out.println();
				if (selected == null || !selected.moveable || selected.team.ordinal() != turn % 2) {
					System.out.println("You can't move this\n");
					continue;
				}
				chosenP = true;
				origin = locations.get(selected);
			}
			
			// Choose a move for the piece
			while (!chosenM) {
				if (calcMoves(selected, origin[0], origin[1]).isEmpty()) {
					System.out.println("This piece cannot move. Try another\n");
					break;
				}
				System.out.println(getMoves(possibleMoves.get(selected)));
				System.out.print("Where would you like to move? ('C' to cancel) ");
				String cmd = in.next().toUpperCase();
				System.out.println();
				if (cmd.equals("C"))
					break;
				else
					loc = Parser.getTile(cmd, board);
				
				if (loc == null) {
					System.out.println("That's not a location on the board. Try again");
					continue;
				}
				
				for (int[] tile : possibleMoves.get(selected)) {
					if (tile[0] == loc[0] && tile[1] == loc[1]) {
						valid = true;
						break;
					}
				}
				if (!valid) {
					System.out.println("Invalid move. Try again");
					continue;
				}
				chosenM = true;
			}
			if (!chosenM) // Broke due to cancel, so redo everything
				continue;
			
			// Perform Movement
			move(selected, origin, loc);
			
			possibleMoves.clear();
			turn++;
		}
		
	}
	
	// Check and Critical System \\
	
	/**
	 * Checks to see if some location is in check
	 * Typically done for the king's location itself
	 * But will also be used to prospect other potential
	 * spaces the king could move to
	 * 
	 * Does this piece put the king in check?
	 * 
	 * @param p
	 *            The piece that may be checking the space
	 * @param loc
	 *            The location that is potentially threatened
	 * @return
	 * 		Whether or not it's safe
	 */
	private boolean isCheck(Piece p, int[] loc) {
		int[] oppLoc = locations.get(p);
		if (p.role == Role.KING)
			return Math.abs(oppLoc[1] - loc[1]) + Math.abs(oppLoc[0] - loc[0]) <= 2
					&& Math.abs(oppLoc[1] - loc[1]) < 2 && Math.abs(oppLoc[0] - loc[0]) < 2;
		else if (p.role == Role.PAWN) {
			int inc = (int) Math.pow(-1, turn % 2);
			if (loc[0] + inc >= 0 && loc[0] + inc < board.length) {
				if (loc[1] + 1 >= 0 && loc[1] + 1 < board.length)
					if (board[loc[0] + inc][loc[1] + 1] == p)
						return true;
				if (loc[1] - 1 >= 0 && loc[1] - 1 < board.length)
					if (board[loc[0] + inc][loc[1] - 1] == p)
						return true;
			}
		}
		else if (p.role == Role.KNIGHT) {
			Color t = (turn % 2 == 0) ? Color.WHITE : Color.BLACK;
			ArrayList<int[]> potentSpots = calcKnight(new Piece(t, Role.KNIGHT, 0), loc[0], loc[1]);
			for (int[] spot : potentSpots) {
				if (spot[0] == oppLoc[0] && spot[1] == oppLoc[1])
					return true;
			}
		}
		else {
			if (p.role != Role.ROOK) {
				Color t = (turn % 2 == 0) ? Color.WHITE : Color.BLACK;
				ArrayList<int[]> potentSpots = calcDiag(new Piece(t, Role.BISHOP, 0), loc[0], loc[1]);
				for (int[] spot : potentSpots) {
					if (spot[0] == oppLoc[0] && spot[1] == oppLoc[1])
						return true;
				}
			}
			if (p.role != Role.BISHOP) {
				Color t = (turn % 2 == 0) ? Color.WHITE : Color.BLACK;
				ArrayList<int[]> potentSpots = calcPlus(new Piece(t, Role.ROOK, 0), loc[0], loc[1]);
				for (int[] spot : potentSpots) {
					if (spot[0] == oppLoc[0] && spot[1] == oppLoc[1])
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks to see which pieces are "critical" to the king's safety
	 * Based on their current location
	 * If some allied piece is the only piece in between the king and
	 * an enemy bishop, queen, or rook, then that piece is critical.
	 * 
	 * A critical piece's movement then will be affected so that it
	 * may only move on that line. Should the king be in check, then
	 * the piece is considered immovable
	 */
	private void criticalCheck() {
		int[] loc = locations.get(team[turn % 2][0]);
		// Plus
		for (int i = -1; i < 2; i += 2) {
			critCheckCalc(loc[0], loc[1], i, 0);
			critCheckCalc(loc[0], loc[1], 0, i);
		}
		// Diagonals
		for (int i = 0; i < 4; i++) {
			int inc1 = (int) Math.pow(-1, i % 2);
			int inc2 = (int) Math.pow(-1, i + (i / 2) % 2);
			critCheckCalc(loc[0], loc[1], inc1, inc2);
		}
	}
	
	/**
	 * Helper calculator to do the brunt of the work
	 * 
	 * @param x
	 *            x Val of the king
	 * @param y
	 *            y Val of the king
	 * @param inc1
	 *            How we increment the x coord
	 * @param inc2
	 *            How we increment the y coord
	 */
	private void critCheckCalc(int x, int y, int inc1, int inc2) {
		
		// Setting the parameters
		Piece first = null;
		int xLim = (inc1 == -1) ? x : (inc1 == 0) ? 100 : board.length - 1 - x;
		int yLim = (inc2 == -1) ? y : (inc2 == 0) ? 100 : board.length - 1 - y;
		int lim = Math.min(xLim, yLim); // Number of rounds we can have
		
		for (int i = 1; i <= lim; i++) {
			// If we encounter an occupied space
			if (board[x + (i * inc1)][y + (i * inc2)] != null) {
				// If that space isn't friendly
				if (board[x + (i * inc1)][y + (i * inc2)].team.ordinal() != turn % 2) {
					// If we haven't encountered a friendly piece
					// It's either already checking us or can't reach the king
					// In this case, it doesn't matter
					if (first == null)
						break;
					// If there was a first piece
					else {
						// If we found a critical piece
						if (board[x + (i * inc1)][y + (i * inc2)].role == Role.QUEEN // Any queen
								|| (board[x + (i * inc1)][y + (i * inc2)].role == Role.ROOK && (inc1 == 0 || inc2 == 0)) // A rook on the plus
								|| board[x + (i * inc1)][y + (i * inc2)].role == Role.BISHOP && (inc1 != 0 && inc2 != 0)) { // Or a bishop on a diag
							first.critical = board[x + (i * inc1)][y + (i * inc2)];
							// System.out.println(first.tag.trim() + " is blocking " + board[x + (i * inc1)][y + (i * inc2)].tag.trim() + " for the King");
							
							// If we're in check
							if (Piece.check[turn % 2])
								first.moveable = false;
							// if the first piece was a knight
							// We can't move this piece
							else if (first.role == Role.KNIGHT)
								first.moveable = false;
							// if it's on the plus and the first was a pawn or bishop
							else if (first.role == Role.BISHOP || first.role == Role.PAWN) {
								if (inc1 == 0 || inc2 == 0)
									first.moveable = false;
							}
							// if it's on the diagonal and the first was a rook
							else if (first.role == Role.KNIGHT) {
								if (inc1 != 0 && inc2 != 0)
									first.moveable = false;
							}
							break;
						}
					}
				}
				// If that space is friendly
				else {
					if (first == null) // If it's the first occupied space
						first = board[x + (i * inc1)][y + (i * inc2)]; // We'll take it into consideration
					else // Otherwise it doesn't matter
						break;
				}
			}
		}
	}
	
	// Movement \\
	
	/**
	 * Moves a piece from one location to the new one, removing any opposing piece it lands on
	 * 
	 * @param piece
	 * @param origin
	 * @param newLoc
	 */
	private void move(Piece piece, int[] origin, int[] newLoc) {
		Piece overridden = board[newLoc[0]][newLoc[1]];
		
		board[newLoc[0]][newLoc[1]] = piece;
		board[origin[0]][origin[1]] = null;
		
		locations.put(piece, newLoc);
		
		// Special cases
		if (piece.role == Role.PAWN && Math.abs(origin[1] - newLoc[1]) == 1 && overridden == null) {
			System.out.println("En Passant!");
			overridden = board[newLoc[0]][origin[1]]; // En Passant
			board[origin[0]][newLoc[1]] = null;
		}
		else if (piece.role == Role.PAWN && Math.abs(origin[0] - newLoc[0]) == 2) // Extra space
			piece.special = 1;
		// Promotion!
		else if (piece.role == Role.PAWN && ((newLoc[0] == 0 && piece.team == Color.BLACK)
				|| (newLoc[0] == board.length - 1 && piece.team == Color.WHITE)))
			promote(piece);
		else if (piece.role == Role.PAWN) // Doesn't use the boost
			piece.special = -1;
		else if (overridden != null && overridden.team == piece.team) { // castling
			board[origin[0]][origin[1]] = overridden;
			locations.put(overridden, origin);
			piece.special = -1;
			overridden.special = -1;
			return;
		}
		else if ((piece.role == Role.ROOK || piece.role == Role.KING) && piece.special == 0) // Piece's First move
			piece.special = -1;
		
		// Remove overridden if needed
		if (overridden != null) {
			for (int i = 0; i < team[0].length; i++) {
				if (team[overridden.team.ordinal()][i] == overridden)
					team[overridden.team.ordinal()][i] = null;
				locations.remove(overridden);
			}
		}
		
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
		if (piece.critical == null) {
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
		}
		else if (piece.critical.role != Role.ROOK) {
			int[] criticalLoc = locations.get(piece.critical);
			if (criticalLoc[0] - x == inc && Math.abs(criticalLoc[1] - y) == 1)
				moves.add(criticalLoc);
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
		if (piece.critical == null) {
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
		
		// Normal Movement
		for (int i = -1; i < 2; i++) {
			if (x + i < 0 || x + i >= board.length)
				continue;
			for (int j = -1; j < 2; j++) {
				if (i == 0 && j == 0)
					continue;
				if (y + j < 0 || y + j >= board.length)
					continue;
				if (board[x + i][y + j] == null || board[x + i][y + j].team != piece.team) {
					int[] potential = new int[] { x + i, y + j };
					// System.out.println("Checking if moving to " + xAxis[potential[1]] + (potential[0] + 1) + " is ok");
					boolean ok = true;
					for (Piece p : team[(turn + 1) % 2]) {
						if (p != null && isCheck(p, potential))
							ok = false;
					}
					if (ok)
						moves.add(potential);
				}
			}
		}
		
		// Castling
		if (piece.special == 0) {
			// Looks at the rooks to see if we can castle
			for (int i = 0; i < 2; i++) {
				if (team[piece.team.ordinal()][6 + i] != null && team[piece.team.ordinal()][6 + i].special == 0) {
					boolean valid = true;
					int[] loc = locations.get(team[piece.team.ordinal()][6 + i]);
					int inc = (loc[1] - y) / Math.abs(loc[1] - y);
					// Make sure each space in between is empty
					for (int j = y + inc; j != loc[1]; j += inc) {
						if (board[x][j] != null)
							valid = false;
						else {
							// If the spaces are empty, make sure that they wouldn't be put in check
							int[] tempCheck = new int[] { x, j };
							for (Piece p : team[(piece.team.ordinal() + 1) % 2]) {
								if (p != null && isCheck(p, tempCheck))
									valid = false;
							}
						}
						
					}
					if (valid)
						moves.add(loc);
				}
			}
		}
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
	
	/**
	 * Promotes a pawn into either a rook, a bishop, a knight, or a queen
	 */
	private void promote(Piece pawn) {
		boolean valid = false;
		while (!valid) {
			System.out.println("Your pawn can promote. What would you like to promote it to?\n((Q)ueen/K(N)ight/(B)ishop/(R)ook)\n");
			String wish = in.next().toLowerCase();
			if (wish.startsWith("q"))
				pawn.role = Role.QUEEN;
			else if (wish.startsWith("n"))
				pawn.role = Role.KNIGHT;
			else if (wish.startsWith("b"))
				pawn.role = Role.BISHOP;
			else if (wish.startsWith("r"))
				pawn.role = Role.ROOK;
			else {
				System.out.println("I don't understand. What do you want?\n");
				continue;
			}
			pawn.tag = "" + pawn.tag.charAt(0) + pawn.role.toString().charAt(0) + "P";
			if (pawn.role == Role.KNIGHT)
				pawn.tag = pawn.tag.replace("K", "N");
			valid = true;
		}
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