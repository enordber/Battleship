package battleship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

abstract class Player {
	static final int rowIndex = 0;
	static final int columnIndex = 1;
	static HashMap<ShipType,ShipType> shipTypeToHitShipType = new HashMap<ShipType,ShipType>();
	static{
		shipTypeToHitShipType.put(ShipType.CARRIER, ShipType.CARRIER_HIT);
		shipTypeToHitShipType.put(ShipType.BATTLESHIP, ShipType.BATTLESHIP_HIT);
		shipTypeToHitShipType.put(ShipType.CRUISER, ShipType.CRUISER_HIT);
		shipTypeToHitShipType.put(ShipType.DESTROYER, ShipType.DESTROYER_HIT);
		shipTypeToHitShipType.put(ShipType.SUBMARINE, ShipType.SUBMARINE_HIT);
	}
	
	//A called shot will have a non-numeric row label followed by a numeric column label
	private static Pattern shotPattern = Pattern.compile("[^\\d.][\\d]+");

	private BattleshipGame game;
	private int oceanGridRowCount;
	private int oceanGridColumnCount;
	private int targetGridRowCount;
	private int targetGridColumnCount;
	
	//The targetGrid represents the opponents field, and only contains information
	//gained from shots fired.
	private Ship[][] targetGrid;
	
	//The oceanGrid represents the players own field, and includes information
	//about all of the player's ships.
	private Ship[][] oceanGrid;
	
	private ArrayList<Ship> ships = new ArrayList<Ship>(5);
	private Random random = new Random(65287);

	Player(int oceanGridRowCount, int oceanGridColumnCount, int targetGridRowCount, int targetGridColumnCount) {
		this.oceanGridRowCount = oceanGridRowCount;
		this.oceanGridColumnCount = oceanGridColumnCount;
		this.targetGridRowCount = targetGridRowCount;
		this.targetGridColumnCount = targetGridColumnCount;

		init();
	}
	
	void init(){
		targetGrid = new Ship[targetGridRowCount][targetGridColumnCount];
		for(Ship[] row: targetGrid) {
			Arrays.fill(row, Ship.UNKNOWN_SHIP);
		}
		
		oceanGrid = new Ship[oceanGridRowCount][oceanGridColumnCount];
		for(Ship[] row: oceanGrid) {
			Arrays.fill(row, Ship.NO_SHIP);
		}
		ships = new ArrayList<Ship>(5);
	}

	Ship[][] getTargetGrid() {
		return targetGrid;
	}
	
	Ship[][] getOceanGrid() {
		return oceanGrid;
	}
	
	/**
	 * Default placement is random.
	 * 
	 * @param shipsToPlace
	 */
	void placeShips(ArrayList<Ship> shipsToPlace) {
		for(Ship ship: shipsToPlace) {
			boolean placed = false;
			while(!placed) {
				Orientation orientation = Orientation.HORIZONTAL;
				int maxColumn = oceanGridColumnCount - ship.getSize();
				int maxRow = oceanGridRowCount;
				if(random.nextDouble() > 0.5) {
					orientation = Orientation.VERTICAL;
					maxColumn = oceanGridColumnCount - 1;
					maxRow = oceanGridColumnCount - ship.getSize() - 1;
				}

				int column = random.nextInt(maxColumn);
				int row = random.nextInt(maxRow);
				placed = attemptPlacingShip(ship, column, row, orientation);
			}
		}
	}

	private boolean attemptPlacingShip(Ship ship, int column, int row, 
			Orientation orientation) {
		boolean r = true;

		int[][] positions = new int[ship.getSize()][2];
		for(int i = 0; i < positions.length; i++) {
			positions[i][columnIndex] = column;
			positions[i][rowIndex] = row;
			if(orientation == Orientation.HORIZONTAL) {
				column++;
			} else {
				row++;
			}
		}
		
		//make sure positions are not already occupied
		for(int[] position: positions) {
			if(getShipAtPosition(position[columnIndex], position[rowIndex]) != Ship.NO_SHIP) {
				r = false;
			}
		}
		
		if(r) {
			//all positions are available, so place ship here
			ship.setPositions(positions);
			ships.add(ship);
			for(int[] position: positions) {
				oceanGrid[position[rowIndex]][position[columnIndex]] = ship;
			}
		}

		return r;
	}
	
	Ship getShipAtPosition(int column, int row) {
		Ship r = Ship.NO_SHIP;

		for(Ship ship: ships) {
			if(ship.hitsAtPosition(column, row)) {
				r = ship;
			}
		}

		return r;
	}
	
	void setRandomSeed(long seed) {
		random = new Random(seed);
	}

	ArrayList<Ship> getShips() {
		return ships;
	}

	/**
	 * Registers a shot at the specified position.
	 * 
	 * @param column
	 * @param row
	 * @return The Ship at the position
	 */
	Ship shotAt(int column, int row) {
		Ship hitShip = getShipAtPosition(column, row);
		if(hitShip != Ship.NO_SHIP) {
			hitShip.setHitAtPosition(column, row);
		}
		return hitShip;
	}
	
	void registerShotOnTargetResults(int[] shotPosition, Ship opponentShipAtPosition) {
		getTargetGrid()[shotPosition[rowIndex]][shotPosition[columnIndex]] = opponentShipAtPosition;
	}

	Random getRandom() {
		return random;
	}

	int getTargetGridRowCount() {
		return targetGridRowCount;
	}

	int getTargetGridColumnCount() {
		return targetGridColumnCount;
	}
	
	BattleshipGame getGame() {
		return game;
	}

	void setGame(BattleshipGame game) {
		this.game = game;
	}
	
	/**
	 * Create the standard set of ships and places them randomly.
	 */
	void placeShips() {
		ArrayList<Ship> ships = new ArrayList<Ship>();
		ships.add(new Ship(ShipType.CARRIER));
		ships.add(new Ship(ShipType.BATTLESHIP));
		ships.add(new Ship(ShipType.CRUISER));
		ships.add(new Ship(ShipType.SUBMARINE));
		ships.add(new Ship(ShipType.DESTROYER));
		placeShips(ships);
	}

	static int[] parseForShotPosition(String command) {
		int[] r = null;

		if(shotPattern.matcher(command).matches()) {
			String columnLabel = command.replaceAll("[^\\d.]", "");
			String rowLabel = command.replaceAll("[\\d.]", "");
			int shotRowIndex = BattleshipGame.rowLabels.indexOf(rowLabel);
			int shotColumnIndex = BattleshipGame.columnLabels.indexOf(columnLabel);
			r =  new int[2];
			r[rowIndex] = shotRowIndex;
			r[columnIndex] = shotColumnIndex;
		}

		return r;
	}
}

abstract class UIPlayer extends Player {

	UIPlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}

	abstract void play(BattleshipGame game);	
}

abstract class AIPlayer extends Player {

	AIPlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}
	
	abstract int[] getNextShotPosition();
}
