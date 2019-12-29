package battleship;

public enum VictoryStatus {
	UNDECIDED("Undecided"),
	PLAYER_VICTORY("Player Victory"),
	OPPONENT_VICTORY("Opponent Victory");
	
	private String status;
	
	VictoryStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return status;
	}
}
