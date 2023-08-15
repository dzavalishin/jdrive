package com.dzavalishin.ai;

/* The struct to keep some data about the AI in general */
public class AIStruct {
	/* General */
	public boolean enabled = true;           //! Is AI enabled?
	public int tick;              //! The current tick (something like _frame_counter, only for AIs)

	/* For network-clients (a OpenTTD client who acts as an AI connected to a server) */
	public boolean network_client;    //! Are we a network_client?
	public int network_playas;   //! The current network player we are connected as
}