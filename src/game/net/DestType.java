package game.net;

public enum DestType {
	BROADCAST,
	PLAYER,
	CLIENT;

	static DestType [] values = values();
	
	static DestType value(int v) {
		return values[v];
	}
}
