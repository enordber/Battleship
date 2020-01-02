package battleship;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.text.MaskFormatter;

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
			if(r == null) {
				//In SALVO mode, all available 'destroy' positions may be 
				//targeted. Remaining shots will be 'seek' shots.
				r = getNextSeekPosition();
			}
		}
		System.out.println("SeekAndDestroyPlayer.getNextShotPosition() " + Arrays.toString(r));
		return r;
	}

	private int[] getNextDestroyPosition() {
		int[] r = null;
		TargetShip activeTarget = destroyQueue.getFirst();
		Iterator<TargetShip> targetShipIterator = destroyQueue.iterator();
		activeTarget = targetShipIterator.next();
		if(activeTarget.getOrientation() == Orientation.UNKNOWN) {
			boolean roomForVertical = isRoomForShip(activeTarget, Orientation.VERTICAL);
			if(roomForVertical) {
				boolean roomForHorizontal = isRoomForShip(activeTarget, Orientation.HORIZONTAL);
				if(!roomForHorizontal) {
					activeTarget.setOrientation(Orientation.VERTICAL);
				}
			} else {
				activeTarget.setOrientation(Orientation.HORIZONTAL);
			}
		}
		ArrayList<int[]> candidatePositions = new ArrayList<int[]>(4);

		while(r == null && activeTarget != null) {
			switch(activeTarget.getOrientation()) {
			case UNKNOWN:
				//evaluate north
				if(isEligibleCandidatePosition(activeTarget.getPositionToTheNorth())) {
					r = activeTarget.getPositionToTheNorth();
					candidatePositions.add(r);
				}
				//evaluate east
				if(isEligibleCandidatePosition(activeTarget.getPositionToTheEast())) {
					r = activeTarget.getPositionToTheEast();
					candidatePositions.add(r);
				}
				//evaluate south
				if(isEligibleCandidatePosition(activeTarget.getPositionToTheSouth())) {
					r = activeTarget.getPositionToTheSouth();
					candidatePositions.add(r);
				}
				//evaluate west
				if(isEligibleCandidatePosition(activeTarget.getPositionToTheWest())) {
					r = activeTarget.getPositionToTheWest();
					candidatePositions.add(r);
				}
				break;
			case HORIZONTAL:
				//evaluate east
				if(isEligibleCandidatePosition(activeTarget.getPositionToTheEast())) {
					r = activeTarget.getPositionToTheEast();
					candidatePositions.add(r);
				}
				//evaluate west
				if(isEligibleCandidatePosition(activeTarget.getPositionToTheWest())) {
					r = activeTarget.getPositionToTheWest();
					candidatePositions.add(r);
				}
				break;
			case VERTICAL:
				//evaluate north
				if(isEligibleCandidatePosition(activeTarget.getPositionToTheNorth())) {
					r = activeTarget.getPositionToTheNorth();
					candidatePositions.add(r);
				}
				//evaluate south
				if(isEligibleCandidatePosition(activeTarget.getPositionToTheSouth())) {
					r = activeTarget.getPositionToTheSouth();
					candidatePositions.add(r);
				}
				break;
			}

			if(candidatePositions.size() == 0) {
				if(targetShipIterator.hasNext()) {
					activeTarget = targetShipIterator.next();
				} else {
					activeTarget = null;
				}
			}
		}
		if(candidatePositions.size() > 0) {
			//randomly select from available candidates
			r = candidatePositions.get(getRandom().nextInt(candidatePositions.size()));
		}
		return r;
	}

	private boolean isRoomForShip(TargetShip targetShip, Orientation orientation) {
		boolean r = true;
		int[] position = targetShip.getKnownPositions().get(0);
		int column = position[columnIndex];
		int row = position[rowIndex];
		Ship[][] targetGrid = getTargetGrid();
		int available = 0;
		switch(orientation) {
		case VERTICAL:
			//available positions to the north
			int i = row - 1;
			while(i >= 0 && targetGrid[i][column].getType() == ShipType.UNKNOWN) {
				available++;
				i--;
			}

			//available positions to the south
			i = row + 1;
			while(i < targetGrid.length && targetGrid[i][column].getType() == ShipType.UNKNOWN) {
				available++;
				i++;
			}

			break;
		case HORIZONTAL:
			//available available positions to the west
			i = column - 1;
			while(i >= 0 && targetGrid[row][i].getType() == ShipType.UNKNOWN) {
				available++;
				i--;
			}

			//available positions to the east
			i = column + 1;
			while(i < targetGrid[row].length && targetGrid[row][i].getType() == ShipType.UNKNOWN) {
				available++;
				i++;
			}

			break;
		}
		available++; //add one for the hit position, which was not counted above
		r = available >= targetShip.getHoleCount();

		return r;
	}

	private boolean isEligibleCandidatePosition(int[] position) {
		boolean r = true;
		//check that position is within bounds of the target grid
		r = position[rowIndex] >= 0 &&
				position[rowIndex] < getTargetGridRowCount() &&
				position[columnIndex] >= 0 &&
				position[columnIndex] < getTargetGridColumnCount();

				if(r) {
					//check that position is not already revealed
					r = getTargetGrid()[position[rowIndex]][position[columnIndex]] == Ship.UNKNOWN_SHIP;
				}

				return r;
	}

	private int[] getNextSeekPosition() {
		int[] r = new int[2];
		r = getNextMaximizeDisruptionPosition();
//		r = getNextCheckerboardPosition();
		return r;
	}

	private int[] getNextCheckerboardPosition() {
		int[]r = new int[2];
		int row = getRandom().nextInt(getTargetGridRowCount());
		int column = getRandom().nextInt(getTargetGridColumnCount());
		double variance = 0.2;
		double mean = 0.5;
		boolean useGaussian = true;
		//use gaussian to focus on cells in the center area of the board,
		//which are more likely to be occupied
		if(useGaussian) {
			row = (int) Math.min(getTargetGridRowCount()-1,Math.max(0,((getRandom().nextGaussian()*variance)+mean) * getTargetGridRowCount()));
			column = (int) Math.min(getTargetGridColumnCount()-1,Math.max(0,((getRandom().nextGaussian()*variance)+mean) * getTargetGridColumnCount()));
		}
		boolean alternateCell = (row+column) % 2 == 0; 
		while(alternateCell || getTargetGrid()[row][column] != Ship.UNKNOWN_SHIP) {
			if(useGaussian) {
				row = (int) Math.min(getTargetGridRowCount()-1,Math.max(0,((getRandom().nextGaussian()*variance)+mean) * getTargetGridRowCount()));
				column = (int) Math.min(getTargetGridColumnCount()-1,Math.max(0,((getRandom().nextGaussian()*variance)+mean) * getTargetGridColumnCount()));
			} else {
				row = getRandom().nextInt(getTargetGridRowCount());
				column = getRandom().nextInt(getTargetGridColumnCount());
			}
			alternateCell = (row+column) % 2 == 0; 
		}
		r[columnIndex] = column;
		r[rowIndex] = row;

		return r;
	}

	/**
	 * Selects a shot position based on the number of ship placements the shot
	 * can rule out.
	 * 
	 * @return shot position
	 */
	private int[] getNextMaximizeDisruptionPosition() {
		System.out.println("SeekAndDestroyPlayer.getNextMaximizeDisruptionPosition()");
		int[]r = new int[2];
		int maxPlacementsDisrupted = 0;
		ArrayList<int[]> maxDisruptivePositions = new ArrayList<int[]>();
		Ship[][] targetGrid = getTargetGrid();
		int shipSize = getSizeOfLargestUnlocatedShip();
		System.out.println("\tshipSize: " + shipSize);

		for(int row = 0; row < targetGrid.length; row++) {
			for(int column = 0; column < targetGrid[row].length; column++) {
				if(targetGrid[row][column] == Ship.UNKNOWN_SHIP) {
					int placementsDisrupted = getNumberOfPlacementsDisrupted(row, column, shipSize);
					if(placementsDisrupted > maxPlacementsDisrupted) {
						maxDisruptivePositions.clear();
						maxPlacementsDisrupted = placementsDisrupted;
					}
					if(placementsDisrupted == maxPlacementsDisrupted) {
						int[] position = new int[2];
						position[rowIndex] = row;
						position[columnIndex] = column;
						maxDisruptivePositions.add(position);
					}
				}
			}
		}
		System.out.println("\tmaxPlacementsDisrupted: " + maxPlacementsDisrupted);
		System.out.println("\tmaxDisruptivePositions: " + maxDisruptivePositions.size());
		int index = getRandom().nextInt(maxDisruptivePositions.size());
		r = maxDisruptivePositions.get(index);

		return r;
	}

	private int getSizeOfLargestUnlocatedShip() {
		int r = 0;
		ArrayList<Ship> opponentShips = getGame().getUIPlayer().getShips();
		for(Ship ship: opponentShips) {
			if(ship.getHitCount() == 0 && ship.getSize() > r) {
				r = ship.getSize();
			}
		}

		return r;
	}

	/**
	 * Determines the number of distinct ship placements that would be 
	 * hit/ruled out by a shot at the specified position.
	 * 
	 * @param row
	 * @param column
	 * @param shipSize
	 * @return
	 */
	private int getNumberOfPlacementsDisrupted(int row, int column, int shipSize) {
		int r = 0;
		Ship[][] targetGrid = getTargetGrid();
		//count number of contiguous unrevealed vertical cells, including the target
		//cell, from row-(shipSize-1) to row+(shipSize-1)
		int contiguousVertical = 1;
		int minRow = Math.max(0, row - (shipSize - 1));
		for(int i = row-1; i >= minRow; i--) {
			if(targetGrid[i][column] == Ship.UNKNOWN_SHIP) {
				contiguousVertical++;
			} else {
				break;
			}
		}

		int maxRow = Math.min(getTargetGridRowCount() - 1, row + (shipSize - 1));
		for(int i = row+1; i <= maxRow; i++) {
			if(targetGrid[i][column] == Ship.UNKNOWN_SHIP) {
				contiguousVertical++;
			} else {
				break;
			}
		}

		//The number of disrupted vertical placements is contiguousVertical-(shipSize-1)
		int disruptedVertical = Math.max(0, contiguousVertical - (shipSize - 1));

		//count number of contiguous unrevealed horizontal cells, including the target
		//cell, from column-(shipShip-1) to column+(shipSize-1)
		int contiguousHorizontal = 1;
		int minColumn = Math.max(0, column - (shipSize - 1));
		for(int i = column-1; i >= minColumn; i--) {
			if(targetGrid[row][i] == Ship.UNKNOWN_SHIP) {
				contiguousHorizontal++;
			} else {
				break;
			}
		}

		int maxColumn = Math.min(getTargetGridColumnCount() - 1, column + (shipSize -1));
		for(int i = column+1; i <= maxColumn; i++) {
			if(targetGrid[row][i] == Ship.UNKNOWN_SHIP) {
				contiguousHorizontal++;
			} else {
				break;
			}
		}
		//The number of disrupted horizontal placements is contiguousHorizontal-(shipSize-1)
		int disruptedHorizontal = Math.max(0, contiguousHorizontal - (shipSize - 1));
		r = disruptedHorizontal + disruptedVertical;

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

		private int getHoleCount() {
			return holeCount;
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

		private Orientation getOrientation() {
			return orientation;
		}

		private void setOrientation(Orientation orientation) {
			this.orientation = orientation;
		}

		private ArrayList<int[]> getKnownPositions() {
			return knownPositions;
		}
	}
}
