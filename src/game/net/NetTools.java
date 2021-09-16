package game.net;

public interface NetTools {

	public static void NetworkSend_byte(Packet p, byte b) {
		p.append(b);	
	}

	
}
