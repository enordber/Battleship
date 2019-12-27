package battleship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BattleshipGame {
	static final List<String> rowLabels = 
	Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
	static final List<String> columnLabels = 
	Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

	private Random random = new Random(42245);	
	private HumanPlayer humanPlayer;
	private AIPlayer aiPlayer;

	public static void main(String[] args) {
		System.out.println("Battleship Game");
		BattleshipGame game = new BattleshipGame();
		game.playGame();
	}

	public BattleshipGame() {
	}

	public void playGame() {
		humanPlayer = new CommandLinePlayer(10, 10, 10, 10);
		humanPlayer.setRandomSeed(random.nextLong());
		newGame();
		humanPlayer.play(this);
	}
	
	void newGame() {
		aiPlayer = new RandomPlayer(10, 10, 10, 10);
		aiPlayer.setRandomSeed(random.nextLong());
		ArrayList<Ship> ships = new ArrayList<Ship>();
		ships.add(new Ship(ShipType.CARRIER));
		ships.add(new Ship(ShipType.BATTLESHIP));
		ships.add(new Ship(ShipType.CRUISER));
		ships.add(new Ship(ShipType.SUBMARINE));
		ships.add(new Ship(ShipType.DESTROYER));
		aiPlayer.placeShips(ships);
		humanPlayer.init();
	}

	ShipType shootAt(int column, int row) {
		return aiPlayer.shotAt(column, row);
	}
	
	Player getOpponent() {
		return aiPlayer;
	}
 	
	int[] getNextShotPosition() {
		int[] r = aiPlayer.getNextShotPosition();
		return r;
	}
	
	void registerShotOnTargetResults(int[] shotPosition, ShipType opponentShipAtPosition) {
		aiPlayer.registerShotOnTargetResults(shotPosition, opponentShipAtPosition);
	}
	
	boolean evaluateVictory() {
		boolean humanPlayerVictory = true;
		boolean aiPlayerVictory = false;
		for(Ship ship: aiPlayer.getShips()) {
			if(!ship.isSunk()) {
				humanPlayerVictory = false;
			}
		}
		if(humanPlayerVictory) {
			System.out.println("You Win.");
		} else {		
		    aiPlayerVictory = true;
		    for(Ship ship: humanPlayer.getShips()) {
		    	if(!ship.isSunk()) {
		    		aiPlayerVictory = false;
		    	}
		    }
		    if(aiPlayerVictory) {
		    	System.out.println("You Lose.");
		    }
		}
		
		return humanPlayerVictory || aiPlayerVictory;
	}
}
