package game.net;

public enum ClientStatus {
	INACTIVE,
	AUTH, // This means that the client is authorized
	MAP_WAIT, // This means that the client is put on hold because someone else is getting the map
	MAP,
	DONE_MAP,
	PRE_ACTIVE,
	ACTIVE,
}