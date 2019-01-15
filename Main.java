package game;

/**
 * Engine of the game. Everything gets started here
 * 
 * @author andreweng
 *
 */
public class Main {
	
	public static Board board;
	
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		board = new Board();
		System.out.println(board);
		// board.turn++;
		// System.out.println(board);
		
	}
	
}
