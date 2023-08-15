package com.dzavalishin.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

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
	//BinaryString data = new BinaryString();
	private int type;
	private ByteBuffer data;
	private ByteArrayOutputStream collector = null;


	public int getType() { return type; }







	

	
	// -------------------------------------------------------------------
	// Incoming packets
	// -------------------------------------------------------------------
	

	/**
	 * Parse incoming packet.
	 * @param rdata Raw data from network.
	 */
	public Packet(byte [] rdata) {
		parse(rdata); // TODO success? throw?
	}

	/**
	 * Parse incoming packet.
	 * @param Already extracted from data stream packet type
	 * @param rdata Data from network, excluding packet size and type fields.
	 */
	public Packet(int packetType, byte[] rdata) {
		type = packetType;
		//data = new BinaryString( rdata, 0, rdata.length );
		data = ByteBuffer.wrap(rdata, 0, rdata.length);
	}


	private void parse(byte [] rdata )
	{
		int len = parseLen(rdata);
		type = Byte.toUnsignedInt(rdata[2]);

		//data = new BinaryString( rdata, HEADER_SIZE, len-1 );
		data = ByteBuffer.wrap(rdata, HEADER_SIZE, len-1);
	}

	public static int parseLen(byte[] rdata) {
		int len;
		len = Byte.toUnsignedInt(rdata[0]);
		len |= Byte.toUnsignedInt(rdata[1]) << 8;
		return len;
	}

	public byte[] asByteArray() {
		return data.array();
	}

	
	public byte nextByte() {
		return data.get();
	}

	public int nextInt() {
		return data.getInt();
	}


	public long nextLong() {
		return data.getLong();
	}


	public String nextString() {
		int len = data.getInt();
		byte[] sdata = new byte[len];
		data.get(sdata, 0, len);
		return new String(sdata);
	}


	/**
	 * Extract object from packet.
	 * <br>
	 * NB! Can't be used with other nextXXX methods. Only one object per packet.
	 * TODO change it - use ByteArrayInputStream?
	 * @return Object decoded.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object decodeObject() throws IOException, ClassNotFoundException {
		
		/*char[] ca = data.toCharArray();
		byte [] ba = new byte[ca.length];
		
		for(int i = 0; i < ca.length; i++)
			ba[i] = (byte) ca[i];
		
		ByteArrayInputStream bais = new ByteArrayInputStream(ba); */
		ByteArrayInputStream bais = new ByteArrayInputStream(data.array());
		ObjectInputStream ois = new ObjectInputStream(bais);
		
		return ois.readObject();
	}

	
	
	// -------------------------------------------------------------------
	// Outgoing packets - TODO split in two classes?
	// -------------------------------------------------------------------

	/**
	 * Create paket to be sent.
	 * @param type Type of packet.
	 */
	public Packet(PacketType type) {
		//data.append((char)type.ordinal());
		this.type = type.ordinal();
		data = null;
		collector = new ByteArrayOutputStream();
	}

	public void append(byte b) {
		collector.write(b);
	}

	public void appendInt(int i) 
	{
		collector.write( (byte) (i >> 24) );
		collector.write( (byte) (i >> 16) );
		collector.write( (byte) (i >> 8) );
		collector.write( (byte) i );		
	}


	public void appendLong(long l) {
		appendInt((int) l);
		appendInt((int) (l >>> 32));
	}

	final static Charset forName = Charset.forName("ISO-8859-1");
	
	public void append(String s) throws IOException {
		//data.append(s);		
		appendInt(s.length());
		collector.write(s.getBytes(forName));
	}


	


	public void append(byte[] buffer) throws IOException {
		//data.assign(buffer);
		collector.write(buffer);
	}



	private byte [] encode()
	{
		byte[] sdata = collector.toByteArray();
		
		int size = sdata.length;
		byte [] buffer = new byte[size+HEADER_SIZE];

		int nsize = size+1;  // 1 is for type
		
		buffer[0] = (byte) (nsize & 0xFF);
		buffer[1] = (byte) (nsize >> 8);
		buffer[2] = (byte) type;

		// TODO Arraycopy
		for(int i = 0; i < size; i++)
		{
			byte c = sdata[i];
			buffer[i+HEADER_SIZE] = (byte) c;
		}

		return buffer;
	}

	public void encodeObject(Object o) throws IOException {
		//ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		ObjectOutputStream oos = new ObjectOutputStream(collector);
		oos.writeObject(o);
		//assert data.length() == 0; // Must be empty, we're overwriting
		//data = new BinaryString(bos.toByteArray());
	}

	
	
	public void sendTo(SocketChannel socket) throws IOException 
	{	
		byte [] buffer = encode();
		//socket.getOutputStream().write(buffer, 0, buffer.length);
		ByteBuffer bb = ByteBuffer.wrap(buffer);  
		int len = socket.write(bb); // TODO XXX might write part of packet!
		assert len == buffer.length;
	}

	public void sendTo(DatagramChannel udp, SocketAddress a) throws IOException 
	{
		byte [] buffer = encode();

		ByteBuffer bb = ByteBuffer.wrap(buffer);
		//int len = udp.write(bb);
		int len = udp.send(bb, a);
		assert len == buffer.length;
	}










	
	
	
}
