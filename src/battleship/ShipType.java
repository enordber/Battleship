package battleship;

enum ShipType {
    BATTLESHIP("Battleship"),
    CARRIER("Aircraft Carrier"),
    CRUISER("Cruiser"),
    DESTROYER("Destroyer"),
    SUBMARINE("Submarine"),
    NONE("None"),
    UNKNOWN("Unknown"),
    BATTLESHIP_HIT("battleship"),
    CARRIER_HIT("aircraft carrier"),
    CRUISER_HIT("cruiser"),
    DESTROYER_HIT("destroyer"),
    SUBMARINE_HIT("submarine"),
    SALVO_TARGET("Salvo Target");
    
    private String type;

    private ShipType(String type) {
    	this.type = type;
    }
    
    @Override
    public String toString() {
    	return type;
    }
}
