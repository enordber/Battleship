package battleship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BattleshipGame {
	static final List<String> rowLabels = 
	Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
			"M", "N", "O", "P", "Q", "R", "S", "T");
	static final List<String> columnLabels = 
	Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", 
			"12", "13", "14", "15", "16", "17", "18", "19", "20");

	private Random random = new Random(42245);	
	private UIPlayer uiPlayer;
	private AIPlayer aiPlayer;
	private int rowCount = 10;
	private int columnCount = 10;

	public static void main(String[] args) {
		BattleshipGame game = new BattleshipGame();
		game.playGame();
	}

	public void playGame() {
//		uiPlayer = new CommandLinePlayer(10, 10, 10, 10);
		uiPlayer = new GUIPlayer(rowCount, columnCount, rowCount, columnCount);
		uiPlayer.setRandomSeed(random.nextLong());
		newGame();
	}
	
	void newGame() {
//		aiPlayer = new RandomPlayer(10, 10, 10, 10);
		aiPlayer = new ProbabilityPlayer(rowCount, columnCount, rowCount, columnCount);
		aiPlayer.setGame(this);
		aiPlayer.setRandomSeed(random.nextLong());
		ArrayList<Ship> ships = new ArrayList<Ship>();
		ships.add(new Ship(ShipType.CARRIER));
		ships.add(new Ship(ShipType.BATTLESHIP));
		ships.add(new Ship(ShipType.CRUISER));
		ships.add(new Ship(ShipType.SUBMARINE));
		ships.add(new Ship(ShipType.DESTROYER));
		aiPlayer.placeShips(ships);
		uiPlayer.init();
		uiPlayer.play(this);
	}

	Ship shootAt(int column, int row) {
		return aiPlayer.shotAt(column, row);
	}
	
	Player getOpponent() {
		return aiPlayer;
	}
	
	Player getUIPlayer() {
		return uiPlayer;
	}
 	
	int[] getNextShotPosition() {
		int[] r = aiPlayer.getNextShotPosition();
		return r;
	}
	
	void registerShotOnTargetResults(int[] shotPosition, Ship opponentShipAtPosition) {
		aiPlayer.registerShotOnTargetResults(shotPosition, opponentShipAtPosition);
	}
	
	VictoryStatus evaluateVictory() {
		VictoryStatus r = VictoryStatus.UNDECIDED;
		boolean humanPlayerVictory = true;
		boolean aiPlayerVictory = false;
		for(Ship ship: aiPlayer.getShips()) {
			if(!ship.isSunk()) {
				humanPlayerVictory = false;
			}
		}
		if(humanPlayerVictory) {
			System.out.println("You Win.");
			r = VictoryStatus.PLAYER_VICTORY;
		} else {		
		    aiPlayerVictory = true;
		    for(Ship ship: uiPlayer.getShips()) {
		    	if(!ship.isSunk()) {
		    		aiPlayerVictory = false;
		    	}
		    }
		    if(aiPlayerVictory) {
		    	System.out.println("You Lose.");
		    	r = VictoryStatus.OPPONENT_VICTORY;
		    }
		}
		
		return r;
	}
}
