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
	
	/**
	 * Constructs a basic piece based on the parameters
	 * Tags are done based on the piece's role
	 * If the piece is a King, Pawn, or Rook, then (more so for the king and pawn)
	 * they get special moves that they can perform
	 * 
	 * @param team
	 * @param role
	 * @param num
	 */
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
		String s = "Name: " + tag + "\nTeam: " + team + "\nRole: " + role + "\n";
		s += (special == 0) ? "Can potentially perform a special move" : (special == 1) ? "Just took a bonus move" : "Can't perform a special move";
		s += (critical != null && role != Role.KING) ? "\nProtects King from " + critical.tag
				: (critical != null && role == Role.KING) ? "\nIn check to " + critical.tag : "\nCurrently not critical for the king's protection";
		s += "\n";
		return s;
	}
}
