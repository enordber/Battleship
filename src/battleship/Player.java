package battleship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

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

	private int oceanGridRowCount;
	private int oceanGridColumnCount;
	private int targetGridRowCount;
	private int targetGridColumnCount;
	
	//The targetGrid represents the opponents field, and only contains information
	//gained from shots fired.
	private ShipType[][] targetGrid;
	
	//The oceanGrid represents the players own field, and includes information
	//about all of the player's ships.
	private ShipType[][] oceanGrid;
	
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
		targetGrid = new ShipType[targetGridRowCount][targetGridColumnCount];
		for(ShipType[] row: targetGrid) {
			Arrays.fill(row, ShipType.UNKNOWN);
		}
		
		oceanGrid = new ShipType[oceanGridRowCount][oceanGridColumnCount];
		for(ShipType[] row: oceanGrid) {
			Arrays.fill(row, ShipType.NONE);
		}
	}

	ShipType[][] getTargetGrid() {
		return targetGrid;
	}
	
	ShipType[][] getOceanGrid() {
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
			if(getShipAtPosition(position[columnIndex], position[rowIndex]) != null) {
				r = false;
			}
		}
		
		if(r) {
			//all positions are available, so place ship here
			ship.setPositions(positions);
			ships.add(ship);
			for(int[] position: positions) {
				oceanGrid[position[rowIndex]][position[columnIndex]] = ship.getType();
			}
		}

		return r;
	}
	
	Ship getShipAtPosition(int column, int row) {
		Ship r = null;

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
	 * @return The ShipType at the position
	 */
	ShipType shotAt(int column, int row) {
		ShipType r = ShipType.NONE;
		Ship hitShip = getShipAtPosition(column, row);
		if(hitShip != null) {
			hitShip.setHitAtPosition(column, row);
			oceanGrid[row][column] = shipTypeToHitShipType.get(oceanGrid[row][column]);
			r = hitShip.getType();
		}
		return r;
	}
	
	void registerShotOnTargetResults(int[] shotPosition, ShipType opponentShipAtPosition) {
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
}

abstract class HumanPlayer extends Player {

	HumanPlayer(int oceanGridRowCount, int oceanGridColumnCount,
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
