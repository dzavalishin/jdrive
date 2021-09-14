package game.net;

public class Packet {
	
	public static final int SEND_MTU = 1460;

	
	Packet next;
	int size;
	int pos;
	byte [] buffer = new byte[SEND_MTU];
}
