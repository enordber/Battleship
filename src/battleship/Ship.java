package battleship;

import java.util.HashMap;
import java.util.Map;

public class Ship {
	private static final Map<ShipType,Integer> shipTypeToHoleCount =
			new HashMap<ShipType, Integer>();
	static {
		shipTypeToHoleCount.put(ShipType.BATTLESHIP,4);
		shipTypeToHoleCount.put(ShipType.CARRIER,5);
		shipTypeToHoleCount.put(ShipType.CRUISER,3);
		shipTypeToHoleCount.put(ShipType.DESTROYER,2);
		shipTypeToHoleCount.put(ShipType.SUBMARINE,3);
		shipTypeToHoleCount.put(ShipType.NONE,0);
		shipTypeToHoleCount.put(ShipType.UNKNOWN,0);
	};
	
	static final Ship NO_SHIP = new Ship(ShipType.NONE);
	static final Ship UNKNOWN_SHIP = new Ship(ShipType.UNKNOWN);
	
	private ShipType type;
	
	/**
	 * Cell coordinates for Ship's holes.
	 */
	private int[][] positions;
	
	private boolean[] positionHit;

	/**
	 * Size is the number of holes.
	 */
	private int size;

	Ship(ShipType shipType) {
		type = shipType;
		size = shipTypeToHoleCount.get(shipType);
		positions = new int[size][2];
		positionHit = new boolean[size];
	}

	/**
	 * 
	 * @return Number of target holes in this Ship.
	 */
	int getSize() {
		return size;
	}

	/**
	 * 
	 * @return Cell coordinates occupied by this Ship.
	 */
	int[][] getPositions() {
		return positions;
	}

	/**
	 * 
	 * @param positions Cell coordinates occupied by this Ship.
	 */
	void setPositions(int[][] positions) {
		this.positions = positions;
	}
	
	/**
	 * Registers a hit on this Ship at the given cell coordinates.
	 * 
	 * @param column
	 * @param row
	 */
	void setHitAtPosition(int column, int row) {
		int positionIndex = getIndexOfPosition(column, row);
		positionHit[positionIndex] = true;
	}

	/**
	 * Returns the index for the hole at the specified position. This
	 * is the index in positions[] and positionHit[].
	 * 
	 * @param column
	 * @param row
	 * @return Index of target hole at specified position, or -1 if
	 * this Ship does not occupy the specified position.
	 */
	private int getIndexOfPosition(int column, int row) {
		int r = -1;
		if(positions != null) {
			for(int i = 0; i < positions.length; i++) {
				if(positions[i][Player.columnIndex] == column 
						&& positions[i][Player.rowIndex] == row) {
					r = i;
				}
			}
		}
		return r;
	}

	/**
	 * Indicates if a shot at the specified position would hit this Ship.
	 * 
	 * @param column
	 * @param row
	 * @return true if this Ship occupies the specified position.
	 */
	boolean hitsAtPosition(int column, int row) {
		boolean r = getIndexOfPosition(column, row) > -1;
		return r;
	}
	
	/**
	 * Indicates if the specified position has been hit.
	 * 
	 * @param column
	 * @param row
	 * @return true if the specified position has been hit.
	 */
	boolean isPositionHit(int column, int row) {
		boolean r = false;
		int index = getIndexOfPosition(column, row);
		r = index > -1 && positionHit[index];
		return r;
	}
	
	boolean isSunk() {
		boolean r = true;
		for(boolean hit: positionHit) {
			if(!hit) {
				r = false;
			}
		}
		return r;
	}
	
	ShipType getType() {
		return type;
	}
	
	/**
	 * 
	 * @return number of hits this Ship has sustained.
	 */
	int getHitCount() {
		int r = 0;
		for(boolean hit: positionHit) {
			if(hit) {
				r++;
			}
		}
		return r;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(type);
		sb.append(", positions: ");
		for(int[] position: positions) {
			sb.append("{");
			sb.append(BattleshipGame.rowLabels.get(position[Player.rowIndex]));
			sb.append(",");
			sb.append(BattleshipGame.columnLabels.get(position[Player.columnIndex]));
			sb.append("}");
			sb.append(",");
		}
		
		return sb.toString();
	}
}

