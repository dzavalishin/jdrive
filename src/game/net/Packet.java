package game.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

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
	public static final int HEADER_SIZE = 3;

	@Deprecated
	Packet next;
	//int size;
	//int pos;
	//byte [] buffer = new byte[SEND_MTU];
	BinaryString data = new BinaryString();
	int type;

	public Packet(PacketType type) {
		//data.append((char)type.ordinal());
		this.type = type.ordinal();
	}


	private byte [] encode()
	{
		int size = data.length();
		byte [] buffer = new byte[size+HEADER_SIZE];

		buffer[0] = (byte) (size & 0xFF);
		buffer[1] = (byte) (size >> 8);
		buffer[2] = (byte) type;

		for(int i = 0; i < size; i++)
		{
			char c = data.charAt(i);
			assert(c <= 0xFF);
			buffer[i+HEADER_SIZE] = (byte) c;
		}

		return buffer;
	}

	public void sendTo(Socket s) throws IOException 
	{	
		byte [] buffer = encode();
		s.getOutputStream().write(buffer, 0, buffer.length);		
	}

	public void sendTo(DatagramSocket udp, SocketAddress a) throws IOException 
	{
		byte [] buffer = encode();
		
		DatagramPacket sendPacket = 
				new DatagramPacket(buffer, buffer.length, a );
						//a, Net.NETWORK_DEFAULT_PORT); // TODO just default port?
		
		udp.send(sendPacket);

	}


	public Packet(byte [] rdata) {
		parse(rdata); // TODO success? throw?
	}

	void parse(byte [] rdata )
	{
		int len;
		len = Byte.toUnsignedInt(rdata[0]);
		len |= Byte.toUnsignedInt(rdata[1]) << 8;
		type = Byte.toUnsignedInt(rdata[2]);

		data = new BinaryString( rdata, HEADER_SIZE, len-1 );
	}

	public void encodeObject(Object o) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(o);
		assert data.length() == 0; // Must be empty, we're overwriting
		data = new BinaryString(bos.toByteArray());
	}

	public int getType() { return type; }


	public void append(byte b) {
		data.append(b);		
	}

}
