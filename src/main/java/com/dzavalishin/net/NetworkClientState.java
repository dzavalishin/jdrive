package com.dzavalishin.net;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

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

	//Packet packet_queue; // Packets that are awaiting delivery
	List<Packet> packet_queue = new ArrayList<>();
	//Packet packet_recv; // Partially received packet

	//CommandPacket command_queue; // The command-queue awaiting delivery
	List<CommandPacket> command_queue = new ArrayList<>();

	public boolean hasValidSocket() { return socket != null; }

	NetworkClientInfo ci;

	public NetworkClientInfo getCi() { return ci; }
	public ClientStatus getStatus() { return status; }
	public int getIndex() {
		return index;
	}
}
