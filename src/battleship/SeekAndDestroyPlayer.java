package battleship;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * AI Player that mimics a human player, using only information that
 * would be available to a human player.
 * 
 * @author enordber
 *
 */
public class SeekAndDestroyPlayer extends AIPlayer {
	private ArrayDeque<TargetShip> destroyQueue = new ArrayDeque<TargetShip>();

	//Assuming there is a maximum of one ship per type
	private HashMap<ShipType,TargetShip> shipTypeToTargetShip = 
			new HashMap<ShipType,TargetShip>();

	SeekAndDestroyPlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}

	@Override
	int[] getNextShotPosition() {
		int[] r = new int[2];
		if(destroyQueue.size() == 0) {
			r = getNextSeekPosition();
		} else {
			r = getNextDestroyPosition();
		}
		return r;
	}

	private int[] getNextDestroyPosition() {
		int[] r = null;
		TargetShip activeTarget = destroyQueue.getFirst();
		switch(activeTarget.orientation) {
		case UNKNOWN:
			//evaluate north
			int[] candidatePosition = activeTarget.getPositionToTheNorth();
			if(isGoodCandidatePosition(candidatePosition)) {
				r = candidatePosition;
			}
			//evaluate east
			candidatePosition = activeTarget.getPositionToTheEast();
			if(r == null && isGoodCandidatePosition(candidatePosition)) {
				r = candidatePosition;
			}
			//evaluate south
			candidatePosition = activeTarget.getPositionToTheSouth();
			if(r == null && isGoodCandidatePosition(candidatePosition)) {
				r = candidatePosition;
			}
			//evaluate west
			candidatePosition = activeTarget.getPositionToTheWest();
			if(r == null && isGoodCandidatePosition(candidatePosition)) {
				r = candidatePosition;
			}
			break;
		case HORIZONTAL:
			//evaluate east
			candidatePosition = activeTarget.getPositionToTheEast();
			if(r == null && isGoodCandidatePosition(candidatePosition)) {
				r = candidatePosition;
			}
			//evaluate west
			candidatePosition = activeTarget.getPositionToTheWest();
			if(r == null && isGoodCandidatePosition(candidatePosition)) {
				r = candidatePosition;
			}
			break;
		case VERTICAL:
			//evaluate north
			candidatePosition = activeTarget.getPositionToTheNorth();
			if(isGoodCandidatePosition(candidatePosition)) {
				r = candidatePosition;
			}
			//evaluate south
			candidatePosition = activeTarget.getPositionToTheSouth();
			if(r == null && isGoodCandidatePosition(candidatePosition)) {
				r = candidatePosition;
			}
			break;
		}

		return r;
	}

	private boolean isGoodCandidatePosition(int[] position) {
		boolean r = true;
		//check that position is within bounds of the target grid
		r = position[rowIndex] >= 0 
				&& position[rowIndex] < getTargetGridRowCount()
				&& position[columnIndex] >= 0 
				&& position[columnIndex] < getTargetGridColumnCount();
		
		if(r) {
			//check that position is not already revealed
			r = getTargetGrid()[position[rowIndex]][position[columnIndex]] == Ship.UNKNOWN_SHIP;
		}
		
		//TODO - add check that there is enough room for ship in this direction
		return r;
	}

	private int[] getNextSeekPosition() {
		int[] r = new int[2];
		int row = getRandom().nextInt(getTargetGridRowCount());
		int column = getRandom().nextInt(getTargetGridColumnCount());
		boolean blackCell = (row+column) % 2 == 0; 
		while(blackCell || getTargetGrid()[row][column] != Ship.UNKNOWN_SHIP) {
			row = getRandom().nextInt(getTargetGridRowCount());
			column = getRandom().nextInt(getTargetGridColumnCount());
			blackCell = (row+column) % 2 == 0; 
		}
		r[columnIndex] = column;
		r[rowIndex] = row;

		return r;
	}

	@Override
	void registerShotOnTargetResults(int[] shotPosition, Ship opponentShipAtPosition) {
		super.registerShotOnTargetResults(shotPosition, opponentShipAtPosition);
		ShipType typeOfShipHit = opponentShipAtPosition.getType();
		if(typeOfShipHit != ShipType.NONE) {
			TargetShip targetShip = shipTypeToTargetShip.get(typeOfShipHit);
			if(targetShip == null) {
				targetShip = new TargetShip(typeOfShipHit);
				shipTypeToTargetShip.put(typeOfShipHit, targetShip);
				destroyQueue.add(targetShip);
			}
			targetShip.addHitPosition(shotPosition);
			if(targetShip.isSunk()) {
				destroyQueue.remove(targetShip);
			}
		}
	}

	/**
	 * Represents an enemy Ship that has been hit at least once.
	 *  
	 * @author enordber
	 *
	 */
	class TargetShip {
		private ShipType type = ShipType.UNKNOWN;
		private Orientation orientation = Orientation.UNKNOWN;
		private int holeCount;
		private boolean sunk = false;

		/*
		 * Known Positions are sorted down and to the right.
		 */
		private ArrayList<int[]> knownPositions;
		private Comparator<int[]> knownPositionComparator = new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				int r = o1[0] - o2[0];
				if(r == 0) {
					r = o1[1] - o2[1];
				}
				return r;
			}
		};

		TargetShip(ShipType type) {
			this.type = type;
			holeCount = Ship.shipTypeToHoleCount.get(type);
			knownPositions = new ArrayList<int[]>(holeCount);
		}

		int[] getPositionToTheNorth() {
			int[] r = new int[2];
			int[] northernmostPosition = knownPositions.get(0);
			r[rowIndex] = northernmostPosition[rowIndex] - 1;
			r[columnIndex] = northernmostPosition[columnIndex];
			return r;
		}

		int[] getPositionToTheSouth() {
			int[] r = new int[2];
			int[] southernmostPosition = knownPositions.get(knownPositions.size()-1);
			r[rowIndex] = southernmostPosition[rowIndex] + 1;
			r[columnIndex] = southernmostPosition[columnIndex];
			return r;
		}

		int[] getPositionToTheEast() {
			int[] r = new int[2];
			int[] easternmostPosition = knownPositions.get(knownPositions.size()-1);
			r[rowIndex] = easternmostPosition[rowIndex];
			r[columnIndex] = easternmostPosition[columnIndex] + 1;
			return r;
		}

		int[] getPositionToTheWest() {
			int[] r = new int[2];
			int[] westernmostPosition = knownPositions.get(0);
			r[rowIndex] = westernmostPosition[rowIndex];
			r[columnIndex] = westernmostPosition[columnIndex] - 1;
			return r;
		}

		void addHitPosition(int[] position) {
			knownPositions.add(position);
			if(knownPositions.size() == holeCount) {
				sunk = true;
			} else if(knownPositions.size() == 2) {
				determineOrientation();
			}
			Collections.sort(knownPositions, knownPositionComparator);
		}

		void determineOrientation() {
			if(knownPositions.size() < 2) {
				orientation = Orientation.UNKNOWN;
			} else if(knownPositions.get(0)[rowIndex] == knownPositions.get(1)[rowIndex]) {
				orientation = Orientation.HORIZONTAL;
			} else {
				orientation = Orientation.VERTICAL;
			}
		}

		boolean isSunk() {
			return sunk;
		}

		@Override 
		public String toString() {
			StringBuffer sb  = new StringBuffer();
			sb.append("TargetShip[");
			sb.append("type:" + type);
			sb.append(", knownPositions:");
			for(int[] position: knownPositions) {
				sb.append(Arrays.toString(position));
			}
			sb.append(", orientation:" + orientation);
			sb.append("");
			sb.append("");
			sb.append("");
			sb.append("");
			sb.append("]");
			return sb.toString();
		}
	}
}
