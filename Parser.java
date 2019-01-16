package game;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Commands taken by the Scanner
 * Uses RegEx to quickly match patterns
 * 
 * @author andreweng
 *
 */
public class Parser {
	private static final String xAx = "ABCDEFGH";
	private static final String piecePattern = "[BW]([KQ]P?|([BNR]([1-2]|P))|(P[1-8]))"; // RegEx for pieces
	private static final String tilePattern = "[A-H][1-8]"; // RegEx for Tiles
	
	private static final Pattern piecePat = Pattern.compile(piecePattern);
	private static final Pattern tilePat = Pattern.compile(tilePattern);
	
	private static Matcher m;
	
	public static Piece getPiece(String input, Piece[][] board) {
		m = piecePat.matcher(input);
		if (m.matches()) {
			// System.out.println("Command recognized as a piece");
			for (Piece[] row : board) {
				for (Piece piece : row) {
					if (piece != null && piece.tag.trim().equals(input))
						return piece;
				}
			}
		}
		// System.out.println("Command is not recognized as a piece\nAttempting a tile check");
		int[] loc = getTile(input, board);
		if (loc == null)
			return null;
		return board[loc[0]][loc[1]];
	}
	
	public static int[] getTile(String input, Piece[][] board) {
		m = tilePat.matcher(input);
		if (m.matches()) {
			// System.out.println("Command recognized as a tile");
			return new int[] {
					Integer.parseInt(input.substring(1, 2)) - 1, xAx.indexOf(input.substring(0, 1)) };
		}
		// System.out.println("Input unrecognizable");
		return null;
	}
	
}
