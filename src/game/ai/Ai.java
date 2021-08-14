package game.ai;

import game.Global;
import game.ids.PlayerID;

public class Ai {



	public static AIStruct _ai = new AIStruct();
	//static AIPlayer _ai_player[MAX_PLAYERS];

	// ai.c

	/** Is it allowed to start a new AI.
	 * This function checks some boundries to see if we should launch a new AI.
	 * @return True if we can start a new AI.
	 */
	public static boolean AI_AllowNewAI()
	{
		/*
		// If disabled, no AI 
		if (!_ai.enabled)
			return false;

		// If in network, but no server, no AI 
		if (_networking && !_network_server)
			return false;

		// If in network, and server, possible AI 
		if (_networking && _network_server) {
			// Do we want AIs in multiplayer? 
			if (!_patches.ai_in_multiplayer)
				return false;

			// * Only the NewAI is allowed... sadly enough the old AI just doesn't support this
			// *  system, because all commands are delayed by at least 1 tick, which causes
			// *  a big problem, because it uses variables that are only set AFTER the command
			// *  is really executed... 
			if (!_patches.ainew_active)
				return false;
		}

		return true;
		*/
		return false;
	}

	public static void AI_Initialize() {
		// TODO Auto-generated method stub
		
	}

	public static void AI_Uninitialize() {
		// TODO Auto-generated method stub
		
	}

	//public static void AI_StartNewAI(PlayerID index) {
	public static void AI_StartNewAI(int index) {
		// TODO Auto-generated method stub
		Global.error("Ai?");
	}

	public static void AI_PlayerDied(PlayerID owner) {
		// TODO Auto-generated method stub
		Global.error("Ai?");
		
	}

	//#define AI_CHANCE16(a,b)    ((uint16)     AI_Random()  <= (uint16)((65536 * a) / b))
	//#define AI_CHANCE16R(a,b,r) ((uint16)(r = AI_Random()) <= (uint16)((65536 * a) / b))

	/**
	 * The random-function that should be used by ALL AIs.
	 * /
	static  int AI_RandomRange(int max)
	{
		/* We pick RandomRange if we are in SP (so when saved, we do the same over and over)
		 *   but we pick InteractiveRandomRange if we are a network_server or network-client.
		 * /
		if (_networking)
			return InteractiveRandomRange(max);
		else
			return RandomRange(max);
	}

	/**
	 * The random-function that should be used by ALL AIs.
	 * /
	static  int AI_Random()
	{
	/* We pick RandomRange if we are in SP (so when saved, we do the same over and over)
		 *   but we pick InteractiveRandomRange if we are a network_server or network-client.
		 * /
		if (_networking)
			return InteractiveRandom();
		else
			return Random();
	}
	*/

	
}




/* How DoCommands look like for an AI */
class AICommand {
	int tile;
	int p1;
	int p2;
	int procc;

	String text;
	int uid;

	AICommand next;
} 

/* The struct for an AIScript Player */
class AIPlayer {
	boolean active;            //! Is this AI active?
	AICommand queue;       //! The commands that he has in his queue
	AICommand queue_tail;  //! The tail of this queue
} 
