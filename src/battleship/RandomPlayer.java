package battleship;

public class RandomPlayer extends AIPlayer {

	RandomPlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}

	@Override
	int[] getNextShotPosition() {
		int[] r = new int[2];
		//choose row and column
		int row = getRandom().nextInt(getTargetGridRowCount());
		int column = getRandom().nextInt(getTargetGridColumnCount());
		//check if the position is unknown
		while(getTargetGrid()[row][column] != ShipType.UNKNOWN) {
			row = getRandom().nextInt(getTargetGridRowCount());
			column = getRandom().nextInt(getTargetGridColumnCount());			
		}
		r[columnIndex] = column;
		r[rowIndex] = row;
		return r;
	}

}
