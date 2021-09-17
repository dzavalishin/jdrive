package game.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

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

	//@Deprecated	Packet next = null;
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

	public void sendTo(SocketChannel socket) throws IOException 
	{	
		byte [] buffer = encode();
		//socket.getOutputStream().write(buffer, 0, buffer.length);
		ByteBuffer bb = ByteBuffer.wrap(buffer);  
		socket.write(bb); // TODO XXX might write part of packet!
	}

	public void sendTo(DatagramChannel _udp_client_socket, SocketAddress a) throws IOException 
	{
		byte [] buffer = encode();
		/*
		DatagramPacket sendPacket = 
				new DatagramPacket(buffer, buffer.length, a );
						//a, Net.NETWORK_DEFAULT_PORT); // TODO just default port?
		
		_udp_client_socket.send(sendPacket);
		*/
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		_udp_client_socket.write(bb);
	}


	public Packet(byte [] rdata) {
		parse(rdata); // TODO success? throw?
	}

	public Packet(int packetType, byte[] rdata) {
		type = packetType;
		data = new BinaryString( rdata, 0, rdata.length );
	}


	void parse(byte [] rdata )
	{
		int len = parseLen(rdata);
		type = Byte.toUnsignedInt(rdata[2]);

		data = new BinaryString( rdata, HEADER_SIZE, len-1 );
	}

	public static int parseLen(byte[] rdata) {
		int len;
		len = Byte.toUnsignedInt(rdata[0]);
		len |= Byte.toUnsignedInt(rdata[1]) << 8;
		return len;
	}


	public int getType() { return type; }



	
	public void encodeObject(Object o) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(o);
		assert data.length() == 0; // Must be empty, we're overwriting
		data = new BinaryString(bos.toByteArray());
	}

	
	public Object decodeObject() throws IOException, ClassNotFoundException {
		
		char[] ca = data.toCharArray();
		byte [] ba = new byte[ca.length];
		
		for(int i = 0; i < ca.length; i++)
			ba[i] = (byte) ca[i];
		
		ByteArrayInputStream bais = new ByteArrayInputStream(ba); 
		ObjectInputStream ois = new ObjectInputStream(bais);
		
		return ois.readObject();
	}


	public void append(byte b) {
		data.append(b);		
	}

	public void appendInt(int i) 
	{
		data.append( (byte) (i >> 24) );
		data.append( (byte) (i >> 16) );
		data.append( (byte) (i >> 8) );
		data.append( (byte) i );		
	}


	public void appendLong(long l) {
		appendInt((int) l);
		appendInt((int) (l >>> 32));
	}

	public void append(String s) {
		data.append(s);		
	}


	
	private ByteBuffer getBB() {
		// TODO Auto-generated method stub
		
	}
	
	public byte nextByte() {
		ByteBuffer bb = getBB();
		return bb.get();
	}

	public int nextInt() {
		ByteBuffer bb = getBB();
		return bb.getInt();
	}


	public long nextLong() {
		ByteBuffer bb = getBB();
		return bb.getLong();
	}


	public String nextString() {
		ByteBuffer bb = getBB();
		int len = bb.getInt();
		byte[] sdata = new byte[len];
		bb.get(sdata, 0, len);
		return new String(sdata);
	}


	public void setBuffer(byte[] buffer) {
		data.assign(buffer);
		
	}



}
