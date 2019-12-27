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
	};
	
	private ShipType type;
	private int[][] positions;
	private boolean[] positionHit;
	private int size;

	private Ship() {}

	Ship(ShipType shipType) {
		type = shipType;
		size = shipTypeToHoleCount.get(shipType);
		positions = new int[size][2];
		positionHit = new boolean[size];
	}

	int getSize() {
		return size;
	}

	int[][] getPositions() {
		return positions;
	}

	void setPositions(int[][] positions) {
		this.positions = positions;
	}
	
	void setHitAtPosition(int column, int row) {
		int positionIndex = getIndexOfPosition(column, row);
		positionHit[positionIndex] = true;
	}

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

	boolean hitsAtPosition(int column, int row) {
		boolean r = getIndexOfPosition(column, row) > -1;
		return r;
	}
	
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

