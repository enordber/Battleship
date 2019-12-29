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
		int row = getRandom().nextInt(getTargetGridRowCount());
		int column = getRandom().nextInt(getTargetGridColumnCount());
		while(getTargetGrid()[row][column] != Ship.UNKNOWN_SHIP) {
			row = getRandom().nextInt(getTargetGridRowCount());
			column = getRandom().nextInt(getTargetGridColumnCount());			
		}
		r[columnIndex] = column;
		r[rowIndex] = row;
		return r;
	}

}
