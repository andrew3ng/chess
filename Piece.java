package game;

/**
 * Abstract class for all chess pieces
 * Each piece has a symbol, alignment, current location, and list of possible moves
 * 
 * @author andreweng
 *
 */
public class Piece {
	static boolean[] check = { false, false }; // 0 for White, 1 for Black
	Color team;
	Role role;
	
	String tag; // Each piece has a unique ID ((b/w) + (role) + (num if needed))
	
	// indicates when a special move can be made
	// 0 means that a special move is possible
	// 1 means that a special move was just made
	// -1 means that a special move cannot be made
	int special;
	boolean moveable; // When Check is in play. Resets each turn
	
	// Indicates if it is keeping the King from being in check by some piece
	// If the piece is a Bishop, the piece can only move diagonal
	// If the piece is a Rook, the piece can only move in its row or col
	// If the piece is a Queen, then it depends on its relation to the Queen
	// If the King is in Check, then you can't move this piece
	Piece critical = null;
	int[] criticalLoc = null; // Gives the location of the critical piece
	
	public Piece(Color team, Role role, int num) {
		this.team = team;
		this.role = role;
		
		tag = "" + team.toString().charAt(0) + role.toString().charAt(0);
		if (role == Role.KNIGHT)
			tag = tag.replace("K", "N");
		tag += (num > 0) ? num : " ";
		
		if (role == Role.KING || role == Role.PAWN || role == Role.ROOK)
			special = 0;
		else
			special = -1;
		
		moveable = true;
	}
	
	@Override
	public String toString() {
		return tag;
	}
}
