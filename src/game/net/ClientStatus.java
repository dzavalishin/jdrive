package game.net;

public enum ClientStatus {
	STATUS_INACTIVE,
	STATUS_AUTH, // This means that the client is authorized
	STATUS_MAP_WAIT, // This means that the client is put on hold because someone else is getting the map
	STATUS_MAP,
	STATUS_DONE_MAP,
	STATUS_PRE_ACTIVE,
	STATUS_ACTIVE,
}