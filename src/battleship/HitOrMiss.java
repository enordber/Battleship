package battleship;

public enum HitOrMiss {
	HIT("*"),
	MISS("."),
	UNKNOWN("O");
	
	private String label;
	
	private HitOrMiss(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return label;
	}

}
