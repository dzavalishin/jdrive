package game.net;

import java.io.IOException;
import java.net.Socket;

import game.util.BinaryString;

/**
 * 2 bytes size
 * 1 byte PacketType
 * 
 * Rest is JSON?
 * 
 * @author dz
 *
 */

public class Packet {
	
	public static final int SEND_MTU = 1460;
	public static final int SIZE_SIZE = 2; // packet size is 2 bytes

	
	Packet next;
	//int size;
	//int pos;
	//byte [] buffer = new byte[SEND_MTU];
	BinaryString data = new BinaryString();
	
	public Packet(PacketType type) {
		data.append((char)type.ordinal());
	}

	public void sendTo(Socket s) throws IOException {
		int size = data.length();
		byte [] buffer = new byte[size+2];

		buffer[0] = (byte) (size & 0xFF);
		buffer[1] = (byte) (size >> 8);

		for(int i = 0; i < size; i++)
		{
			char c = data.charAt(i);
			assert(c <= 0xFF);
			buffer[i+2] = (byte) c;
		}
		
		s.getOutputStream().write(buffer, 0, size+2);		
	}
}
