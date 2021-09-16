package game.net;

public enum NetworkPasswordType {
	NETWORK_GAME_PASSWORD,
	NETWORK_COMPANY_PASSWORD,;

	static final NetworkPasswordType values[] = values();
	
	static NetworkPasswordType value(int ord) {
		return values[ord];
	}
}