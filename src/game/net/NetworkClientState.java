package game.net;

import java.nio.channels.SocketChannel;

public class NetworkClientState {
	//Socket socket;
	SocketChannel socket;
	int index;
	int last_frame;
	int last_frame_server;
	int lag_test; // This byte is used for lag-testing the client

	ClientStatus status;
	boolean writable; // is client ready to write to?
	boolean quited;

	Packet packet_queue; // Packets that are awaiting delivery
	//Packet packet_recv; // Partially received packet

	CommandPacket command_queue; // The command-queue awaiting delivery

	public boolean hasValidSocket() { return socket != null; }

	NetworkClientInfo ci;
}
