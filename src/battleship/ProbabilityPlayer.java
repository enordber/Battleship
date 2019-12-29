package battleship;

/**
 * This AI Player cheats by looking at the UI players board. Each 
 * shot has a configurable probability of hitting a ship.
 *  
 * @author enordber
 *
 */
public class ProbabilityPlayer extends AIPlayer {
	private double probability = 0.5;

	ProbabilityPlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}

	@Override
	int[] getNextShotPosition() {
		int[] r = new int[2];
		boolean hit = getRandom().nextDouble() < probability;
		hit |= allUnoccupiedCellsRevealed();
		Ship[][] playerOceanGrid = getGame().getUIPlayer().getOceanGrid();
		int row = getRandom().nextInt(getTargetGridRowCount());
		int column = getRandom().nextInt(getTargetGridColumnCount());
		while((getTargetGrid()[row][column] != Ship.UNKNOWN_SHIP) 
				|| (playerOceanGrid[row][column] != Ship.NO_SHIP ^ hit)) {
			row = getRandom().nextInt(getTargetGridRowCount());
			column = getRandom().nextInt(getTargetGridColumnCount());			
		}
		r[columnIndex] = column;
		r[rowIndex] = row;
		return r;
	}
	
	private boolean allUnoccupiedCellsRevealed() {
		boolean r = true;
		Ship[][] uiPlayerOceanGrid = getGame().getUIPlayer().getOceanGrid();
		Ship[][] targetGrid = getTargetGrid();
		for(int i = 0; i < targetGrid.length; i++) {
			for(int j = 0; j < targetGrid[i].length; j++) {
				if(targetGrid[i][j] == Ship.UNKNOWN_SHIP &&
						uiPlayerOceanGrid[i][j] == Ship.NO_SHIP) {
					r = false;
				}
			}
		}
		return r;
	}

	void setHitProbability(double probability) {
		this.probability = probability;
	}
}
