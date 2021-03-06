package battleship;

/**
 * AI Player with a configurable probability of hitting a ship.
 * This player cheats by looking at the UI player's ship locations.
 *  
 * @author enordber
 *
 */
public class ProbabilityPlayer extends AIPlayer {
	private double probability = 0.3;

	ProbabilityPlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}

	@Override
	int[] getNextShotPosition() {
		int[] r = new int[2];
		
		boolean hit = getRandom().nextDouble() < probability;
		//even if hit is randomly assigned false, set it to true if the
		//only cells left unrevealed are occupied
		hit |= allUnoccupiedCellsRevealed();
		//set to true if all occupied cells have been revealed (this can happen in Salvo mode)
		hit &= unrevealedOccupiedCell();
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
		for(int i = 0; r && i < targetGrid.length; i++) {
			for(int j = 0; r && j < targetGrid[i].length; j++) {
				if(targetGrid[i][j] == Ship.UNKNOWN_SHIP &&
						uiPlayerOceanGrid[i][j] == Ship.NO_SHIP) {
					r = false;
				}
			}
		}
		return r;
	}
	
	private boolean unrevealedOccupiedCell() {
		boolean r = false;
		Ship[][] uiPlayerOceanGrid = getGame().getUIPlayer().getOceanGrid();
		Ship[][] targetGrid = getTargetGrid();
		for(int i = 0; !r && i < targetGrid.length; i++) {
			for(int j = 0; !r && j < targetGrid[i].length; j++) {
				if(targetGrid[i][j] == Ship.UNKNOWN_SHIP &&
						uiPlayerOceanGrid[i][j] != Ship.NO_SHIP) {
					r = true;
				}
			}
		}

		return r;
	}

	void setHitProbability(double probability) {
		this.probability = probability;
	}
	
	double getHitProbability() {
		return probability;
	}
	
	@Override
	int getNumberOfDifficultyLevels() {
	    return 100;	
	}
	
	@Override
	void setDifficultyLevel(int difficultyLevel) {
		setHitProbability(difficultyLevel/100.0);
	}
}
