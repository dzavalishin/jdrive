package com.dzavalishin.ai;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.net.Net;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.enums.Owner;

public class Ai {



	public static AIStruct _ai = new AIStruct();
	static AIPlayer [] _ai_player = new AIPlayer[Global.MAX_PLAYERS];

	// ai.c

	/** Is it allowed to start a new AI.
	 * This function checks some boundries to see if we should launch a new AI.
	 * @return True if we can start a new AI.
	 */
	public static boolean AI_AllowNewAI()
	{
	
		// If disabled, no AI 
		if (!_ai.enabled)
			return false;

		// If in network, but no server, no AI 
		if (Global._networking && !Global._network_server)
			return false;

		// If in network, and server, possible AI 
		if (Global._networking && Global._network_server) {
			// Do we want AIs in multiplayer? 
			if (!Global._patches.ai_in_multiplayer)
				return false;

			// * Only the NewAI is allowed... sadly enough the old AI just doesn't support this
			// *  system, because all commands are delayed by at least 1 tick, which causes
			// *  a big problem, because it uses variables that are only set AFTER the command
			// *  is really executed... 
			if (!Global._patches.ainew_active)
				return false;
		}

		return true;
		
		//
	}

	//#define AI_CHANCE16(a,b)    ((uint16)     AI_Random()  <= (uint16)((65536 * a) / b))
	//#define AI_CHANCE16R(a,b,r) ((uint16)(r = AI_Random()) <= (uint16)((65536 * a) / b))

	public static boolean AI_CHANCE16(int a, int b)
	{		
		return AI_Random() <= (0xFFFF & ((65536 * a) / b));
	}
	
	//#define AI_CHANCE16R(a,b,r) ((uint16)(r = AI_Random()) <= (uint16)((65536 * a) / b))
	
	public static boolean AI_CHANCE16R(int a, int b, int [] r)
	{
		r[0] = AI_Random();
		return r[0] <= (0xFFFF & ((65536 * a) / b));
	}
	
	/**
	 * The random-function that should be used by ALL AIs.
	 */
	static  int AI_RandomRange(int max)
	{
		/* We pick RandomRange if we are in SP (so when saved, we do the same over and over)
		 *   but we pick InteractiveRandomRange if we are a network_server or network-client.
		 */
		if (Global._networking)
			return Hal.InteractiveRandomRange(max);
		else
			return Hal.RandomRange(max);
	}

	/**
	 * The random-function that should be used by ALL AIs.
	 */
	static  int AI_Random()
	{
	/* We pick RandomRange if we are in SP (so when saved, we do the same over and over)
		 *   but we pick InteractiveRandomRange if we are a network_server or network-client.
		 */
		if (Global._networking)
			return Hal.InteractiveRandom();
		else
			return Hal.Random();
	}
	


	/**
	 * Dequeues commands put in the queue via AI_PutCommandInQueue.
	 */
	static void AI_DequeueCommands(int player)
	{
		AICommand com, entry_com;

		entry_com = _ai_player[player].queue;

		/* It happens that DoCommandP issues a new DoCommandAI which adds a new command
		 *  to this very same queue (don't argue about this, if it currently doesn't
		 *  happen I can tell you it will happen with AIScript -- TrueLight). If we
		 *  do not make the queue null, that commands will be dequeued immediatly.
		 *  Therefor we safe the entry-point to entry_com, and make the queue null, so
		 *  the new queue can be safely built up. */
		_ai_player[player].queue = null;
		_ai_player[player].queue_tail = null;

		/* Dequeue all commands */
		while ((com = entry_com) != null) {
			//_current_player = player;
			Global.gs.setCurrentPlayer(PlayerID.get(player));

			/* Copy the DP back in place */
			Global._cmd_text = com.text;
			Cmd.DoCommandP( TileIndex.get(com.tile), com.p1, com.p2, null, com.procc);

			/* Free item */
			entry_com = com.next;
			//if (com.text != null)				free(com.text);
			//free(com);
		}
	}

	/**
	 * Needed for SP; we need to delay DoCommand with 1 tick, because else events
	 *  will make infinite loops (AIScript).
	 */
	static void AI_PutCommandInQueue(int player, int tile, int p1, int p2, int procc)
	{
		AICommand com;

		if (_ai_player[player].queue_tail == null) {
			/* There is no item in the queue yet, create the queue */
			_ai_player[player].queue = new AICommand(); // malloc(sizeof(AICommand));
			_ai_player[player].queue_tail = _ai_player[player].queue;
		} else {
			/* Add an item at the end */
			_ai_player[player].queue_tail.next = new AICommand(); // malloc(sizeof(AICommand));
			_ai_player[player].queue_tail = _ai_player[player].queue_tail.next;
		}

		/* This is our new item */
		com = _ai_player[player].queue_tail;

		/* Assign the info */
		com.tile  = tile;
		com.p1    = p1;
		com.p2    = p2;
		com.procc = procc;
		com.next  = null;
		com.text  = null;

		/* Copy the cmd_text, if needed */
		if (Global._cmd_text != null) {
			com.text = Global._cmd_text;
			Global._cmd_text = null;
		}
	}

	
	/**
	 * Executes a raw DoCommand for the AI.
	 */
	static int AI_DoCommand(TileIndex tile, int p1, int p2, int flags, int procc)
	{
		final int t = tile == null ? 0 : tile.getTile();
		return AI_DoCommand(t, p1, p2, flags, procc);
	}	
	/**
	 * Executes a raw DoCommand for the AI.
	 */
	static int AI_DoCommand(int tile, int p1, int p2, int flags, int procc)
	{
		PlayerID old_lp;
		int res = 0;
		String tmp_cmdtext = null;

		/* If you enable DC_EXEC with DC_QUERY_COST you are a really strange
		 *   person.. should we check for those funny jokes?
		 */

		/* The test already free _cmd_text in most cases, so let's backup the string, else we have a problem ;) */
		if (Global._cmd_text != null)
			tmp_cmdtext = Global._cmd_text;

		/* First, do a test-run to see if we can do this */
		res = Cmd.DoCommandByTile(TileIndex.get(tile), p1, p2, flags & ~Cmd.DC_EXEC, procc);
		/* The command failed, or you didn't want to execute, or you are quering, return */
		if ((Cmd.CmdFailed(res)) || 0 == (flags & Cmd.DC_EXEC) || 0 != (flags & Cmd.DC_QUERY_COST)) {
			//if (tmp_cmdtext != null)				free(tmp_cmdtext);
			return res;
		}

		/* Recover _cmd_text */
		if (tmp_cmdtext != null)
			Global._cmd_text = tmp_cmdtext;

		/* If we did a DC_EXEC, and the command did not return an error, execute it
		    over the network */
		if(0 != (flags & Cmd.DC_AUTO))                  procc |= Cmd.CMD_AUTO;
		if(0 != (flags & Cmd.DC_NO_WATER))              procc |= Cmd.CMD_NO_WATER;

		/* NetworkSend_Command needs _local_player to be set correctly, so
		    adjust it, and put it back right after the function */
		old_lp = Global.gs._local_player;
		Global.gs._local_player = Global.gs.getCurrentPlayer();

	//*#ifdef ENABLE_NETWORK
		// Send the command 
		if (Global._networking)
			// Network is easy, send it to his handler 
			Net.NetworkSend_Command(TileIndex.get(tile), p1, p2, procc, null);
		else
	//#endif*/
			/* If we execute BuildCommands directly in SP, we have a big problem with events
			 *  so we need to delay is for 1 tick */
			AI_PutCommandInQueue(Global.gs.getCurrentPlayer().id, tile, p1, p2, procc);

		/* Set _local_player back */
			Global.gs._local_player = old_lp;

		/* Free the temp _cmd_text var */
		//if (tmp_cmdtext != null)			free(tmp_cmdtext);

		return res;
	}

	/**
	 * Run 1 tick of the AI. Don't overdo it, keep it realistic.
	 */
	static void AI_RunTick(PlayerID player)
	{
		//extern void AiNewDoGameLoop(Player p);

		Player p = Player.GetPlayer(player);
		//_current_player = player;
		Global.gs.setCurrentPlayer(player);

		//if (Global._patches.ainew_active) {
			Trolly.AiNewDoGameLoop(p);
		/*} else {
			// Enable all kind of cheats the old AI needs in order to operate correctly... 
			Global._is_old_ai_player = true;
			Trolly.AiDoGameLoop(p);
			_is_old_ai_player = false;
		}*/
	}


	/**
	 * The gameloop for AIs.
	 *  Handles one tick for all the AIs.
	 */
	public static void AI_RunGameLoop()
	{
		/* Don't do anything if ai is disabled */
		if (!_ai.enabled) return;

		/* Don't do anything if we are a network-client
		 *  (too bad when a client joins, he thinks the AIs are real, so it wants to control
		 *   them.. this avoids that, while loading a network game in singleplayer, does make
		 *   the AIs to continue ;))
		 */
		if (Global._networking && !Global._network_server && !_ai.network_client)
			return;

		/* New tick */
		_ai.tick++;

		/* Make sure the AI follows the difficulty rule.. */
		assert(GameOptions._opt.diff.competitor_speed <= 4);
		if ((_ai.tick & ((1 << (4 - GameOptions._opt.diff.competitor_speed)) - 1)) != 0)
			return;

		/* Check for AI-client (so joining a network with an AI) */
		if (_ai.network_client && _ai_player[_ai.network_playas].active) {
			/* Run the script */
			AI_DequeueCommands(_ai.network_playas);
			AI_RunTick( PlayerID.get(_ai.network_playas) );
		} else if (!Global._networking || Global._network_server) {
			/* Check if we want to run AIs (server or SP only) */
			//Player p;

			//FOR_ALL_PLAYERS(p) 
			Player.forEach( p -> {
				if (p.isActive() && p.isAi()) {
					/* This should always be true, else something went wrong... */
					assert(_ai_player[p.getIndex().id].active);

					/* Run the script */
					AI_DequeueCommands(p.getIndex().id);
					AI_RunTick(p.getIndex());
				}
			});
		}
		//_current_player = OWNER_NONE;
		Global.gs.setCurrentPlayer(PlayerID.getNone());
	}

	/**
	 * A new AI sees the day of light. You can do here what ever you think is needed.
	 */
	public static void AI_StartNewAI(PlayerID player)
	{
		//assert(player.id < Global.MAX_PLAYERS);
		assert(!player.isSpecial());
		if(_ai_player[player.id] == null ) _ai_player[player.id] = new AIPlayer();
		/* Called if a new AI is booted */
		_ai_player[player.id].active = true;
	}

	/**
	 * This AI player died. Give it some chance to make a final puf.
	 */
	public static void AI_PlayerDied(PlayerID player)
	{
		if (_ai.network_client && _ai.network_playas == player.id)
			_ai.network_playas = Owner.OWNER_SPECTATOR;

		/* Called if this AI died */
		_ai_player[player.id].active = false;
	}

	/**
	 * Initialize some AI-related stuff.
	 */
	public static void AI_Initialize()
	{
		boolean ai_network_client = _ai.network_client;

		/* First, make sure all AIs are DEAD! */
		AI_Uninitialize();

		//memset(&_ai, 0, sizeof(_ai));
		_ai = new AIStruct();
		//memset(&_ai_player, 0, sizeof(_ai_player));
		_ai_player = new AIPlayer[Global.MAX_PLAYERS];

		_ai.network_client = ai_network_client;
		_ai.network_playas = Owner.OWNER_SPECTATOR;
		_ai.enabled = true;
	}

	/**
	 * Deinitializer for AI-related stuff.
	 */
	public static void AI_Uninitialize()
	{
		Player.forEach( p -> { if (p != null && p.isActive() && p.isAi()) AI_PlayerDied(p.getIndex()); });
	}
	
	
	
} 
