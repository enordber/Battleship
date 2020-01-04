package battleship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Provides a command line interface to the Battleship game. 
 * This was used for development purposes, and development of
 * the command line interface was stopped when the GUI Player
 * was added.
 * 
 * @author enordber
 *
 */
class CommandLinePlayer extends UIPlayer {
	private static HashMap<String,String> menuItemDesciptions = new HashMap<String, String>();
	static {
		menuItemDesciptions.put("M","Print Menu.");
		menuItemDesciptions.put("T","Print Target Grid.");
		menuItemDesciptions.put("O","Print Ocean Grid.");
		menuItemDesciptions.put("P","Place Ships on Ocean Grid.");
		menuItemDesciptions.put("S","Print Game Summary.");
		menuItemDesciptions.put("Q","Quit Game.");
		menuItemDesciptions.put("CO","Cheat. Look at Opponent's Ocean Grid.");
		menuItemDesciptions.put("CT","Cheat. Look at Opponent's Target Grid.");
		menuItemDesciptions.put("<row><column>","Shoot at indicated position. 'D5', 'A7', etc");
	}

	private static HashMap<ShipType,String> shipTypeLabels = new HashMap<ShipType,String>();
	static {
		shipTypeLabels.put(ShipType.CARRIER, "A");
		shipTypeLabels.put(ShipType.BATTLESHIP, "B");
		shipTypeLabels.put(ShipType.CRUISER, "C");
		shipTypeLabels.put(ShipType.SUBMARINE, "S");
		shipTypeLabels.put(ShipType.DESTROYER, "D");
		shipTypeLabels.put(ShipType.CARRIER_HIT, "a");
		shipTypeLabels.put(ShipType.BATTLESHIP_HIT, "b");
		shipTypeLabels.put(ShipType.CRUISER_HIT, "c");
		shipTypeLabels.put(ShipType.SUBMARINE_HIT, "s");
		shipTypeLabels.put(ShipType.DESTROYER_HIT, "d");
		shipTypeLabels.put(ShipType.UNKNOWN, "o");
		shipTypeLabels.put(ShipType.NONE, ".");
	}

	private Scanner scanner;

	CommandLinePlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}

	/**
	 * Main game event loop.
	 */
	void play(BattleshipGame game) {
		setGame(game);
		if(scanner == null) {
			scanner = new Scanner(System.in);
		}
		printMenu();
		while(true) {
			System.out.print("Enter Command ('M' for Menu of options.): ");
			String command = scanner.next().trim().toUpperCase();
			boolean isMenuOption = menuItemDesciptions.containsKey(command);
			if(isMenuOption) {
				executeMenuOption(command);
			} else { 
				int[] shotPosition = parseForShotPosition(command);
				if(shotPosition == null) {
					System.out.println("'"+ command + "' is not a valid menu option or shot position. Please try again.");
				} else {
					fireShot(shotPosition);
				}

				//accept next shot from opponent
				int[] incomingShotPosition = game.getNextShotPosition();
				System.out.print("Incoming shot at " + 
						BattleshipGame.rowLabels.get(incomingShotPosition[rowIndex]) +
						BattleshipGame.columnLabels.get(incomingShotPosition[columnIndex]) + "... ");
				Ship ownShipAtPosition = shotAt(incomingShotPosition[columnIndex], 
						incomingShotPosition[rowIndex]);
				if(ownShipAtPosition == Ship.NO_SHIP) {
					System.out.println("Miss");
				} else {
					System.out.println("Hit on " + ownShipAtPosition);
				}
				
				//send back result of the shot from opponent
				game.registerShotOnTargetResults(incomingShotPosition, ownShipAtPosition);
				VictoryStatus victoryStatus = game.evaluateVictory();
				if(victoryStatus != VictoryStatus.UNDECIDED) {
					System.out.println("Game Over.");
				}
			}
		}
	}

	private void fireShot(int[] shotPosition) {
		//fire shot
		Ship opponentShipAtPosition = getGame().shootAt(shotPosition[columnIndex], shotPosition[rowIndex]);
		if(opponentShipAtPosition == Ship.NO_SHIP) {
			System.out.println("Miss");
		} else {
			System.out.println("Hit on " + opponentShipAtPosition);
		}
		registerShotOnTargetResults(shotPosition, opponentShipAtPosition);
	}

	private void executeMenuOption(String command) {
		switch(command) {
		case "Q": 
			attemptQuit();
			break;
		case "N":
			break;
		case "M":
			printMenu();
			break;
		case "S":
			printStatus();
			break;
		case "T":
			System.out.println("Target Grid");
			printShipGrid(getTargetGrid());
			break;
		case "P":
			placeShips();
			break;
		case "O":
			System.out.println("Ocean Grid");
			printShipGrid(getOceanGrid());
			break;
		case "CO":
			System.out.println("Opponent Ocean Grid. (Cheat, cheat, never beat.)");
			printShipGrid(getGame().getOpponent().getOceanGrid());
			break;
		case "CT":
			System.out.println("Opponent Target Grid.");
			printShipGrid(getGame().getOpponent().getTargetGrid());
		}

	}

	private void printStatus() {
		//print target grid
		//print ocean grid
		//print own ship status
		printShipStatus(getShips());
		//print opponent ship status
		//print number of shots
	}

	private void attemptQuit() {
		System.out.println("Quit.");
		//TODO - request verification
		System.exit(0);
	}

	private void printMenu() {
		for(String command: menuItemDesciptions.keySet()) {
			System.out.println(command + "\t" + menuItemDesciptions.get(command));
		}
	}

	private static void printShipGrid(Ship[][] shipGrid) {
		//TODO - print key
		System.out.println();
		String spacer = "  ";
		StringBuffer rowHeader = new StringBuffer();
		rowHeader.append(" ");
		int columnCount = shipGrid[0].length;
		for(int i = 0; i < columnCount; i++) {
			rowHeader.append(spacer);
			rowHeader.append(BattleshipGame.columnLabels.get(i));
		}
		System.out.println(rowHeader);

		for(int i = 0; i < shipGrid.length; i++) {
			StringBuffer rowBuffer = new StringBuffer();
			rowBuffer.append(BattleshipGame.rowLabels.get(i));
			for(int j = 0; j < shipGrid[i].length; j++) {
				rowBuffer.append(spacer);
				rowBuffer.append(shipTypeLabels.get(shipGrid[i][j].getType()));
			}
			System.out.println(rowBuffer);
		}
	}

	private static void printShipStatus(ArrayList<Ship> ships) {
		for(Ship ship: ships) {
			System.out.print(ship.toString() + " \tHits: " + ship.getHitCount() + ". ");
			if(ship.isSunk()) {
				System.out.print("Sunk.");
			}
			System.out.println();
		}
	}
}
