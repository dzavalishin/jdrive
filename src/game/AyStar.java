package game;

public abstract class AyStar 
{


	static final int	AYSTAR_FOUND_END_NODE = 1;
	static final int	AYSTAR_EMPTY_OPENLIST = 2;
	static final int	AYSTAR_STILL_BUSY = 3;
	static final int	AYSTAR_NO_PATH = 4;
	static final int	AYSTAR_LIMIT_REACHED = 5;
	static final int	AYSTAR_DONE = 6;

	static final int	AYSTAR_INVALID_NODE = -1;



	/* These fields should be filled before initting the AyStar, but not changed
	 * afterwards (except for user_data and user_path)! (free and init again to change them) */

	/* These should point to the application specific routines that do the
	 * actual work 
		AyStar_CalculateG* CalculateG;
		AyStar_CalculateH* CalculateH;
		AyStar_GetNeighbours* GetNeighbours;
		AyStar_EndNodeCheck* EndNodeCheck;
		AyStar_FoundEndNode* FoundEndNode;
		AyStar_BeforeExit* BeforeExit;
	 */

	/*
	 * This function is called to calculate the G-value for AyStar Algorithm.
	 *  return values can be:
	 *	AYSTAR_INVALID_NODE : indicates an item is not valid (e.g.: unwalkable)
	 *	Any value >= 0 : the g-value for this tile
	 */
	abstract int CalculateG(AyStarNode current, OpenListNode parent);

	/*
	 * This function is called to calculate the H-value for AyStar Algorithm.
	 *  Mostly, this must result the distance (Manhattan way) between the
	 *   current point and the end point
	 *  return values can be:
	 *	Any value >= 0 : the h-value for this tile
	 */
	abstract int CalculateH(AyStarNode current, OpenListNode parent);

	/*
	 * This function request the tiles around the current tile and put them in tiles_around
	 *  tiles_around is never resetted, so if you are not using directions, just leave it alone.
	 * Warning: never add more tiles_around than memory allocated for it.
	 */
	abstract  void GetNeighbours(OpenListNode current);

	/*
	 * This function is called to check if the end-tile is found
	 *  return values can be:
	 *	AYSTAR_FOUND_END_NODE : indicates this is the end tile
	 *	AYSTAR_DONE : indicates this is not the end tile (or direction was wrong)
	 */
	/*
	 * The 2nd parameter should be OpenListNode, and NOT AyStarNode. AyStarNode is
	 * part of OpenListNode and so it could be accessed without any problems.
	 * The good part about OpenListNode is, and how AIs use it, that you can
	 * access the parent of the current node, and so check if you, for example
	 * don't try to enter the file tile with a 90-degree curve. So please, leave
	 * this an OpenListNode, it works just fine -- TrueLight
	 */
	abstract int EndNodeCheck(OpenListNode current);

	/*
	 * If the End Node is found, this function is called.
	 *  It can do, for example, calculate the route and put that in an array
	 */
	abstract void FoundEndNode(OpenListNode current);

	/*
	 * Is called when aystar ends it pathfinding, but before cleanup.
	 */
	abstract void BeforeExit();



	/* These are completely untouched by AyStar, they can be accesed by
	 * the application specific routines to input and output data.
	 * user_path should typically contain data about the resulting path
	 * afterwards, user_target should typically contain information about
	 * what where looking for, and user_data can contain just about
	 * everything */
	// TODO resurrect
	//void *user_path;
	//void *user_target;
	//int user_data[10];

	// [dz] can be some superclass or interface of NPFFoundTargetData? 
	NPFFoundTargetData user_path;
	
	/* How many loops are there called before AyStarMain_Main gives
	 * control back to the caller. 0 = until done */
	byte loops_per_tick;
	/* If the g-value goes over this number, it stops searching
	 *  0 = infinite */
	int max_path_cost;
	/* The maximum amount of nodes that will be expanded, 0 = infinite */
	int max_search_nodes;

	/* These should be filled with the neighbours of a tile by
	 * GetNeighbours */
	AyStarNode neighbours[];
	byte num_neighbours;

	/* These will contain the methods for manipulating the AyStar. Only
	 * main() should be called externally * /
		AyStar_AddStartNode* addstart;
		AyStar_Main* main;
		AyStar_Loop* loop;
		AyStar_Free* free;
		AyStar_Clear* clear;
		AyStar_CheckTile* checktile;
	 */

	abstract void addstart(AyStarNode start_node, int g);
	abstract int main();
	abstract int loop();
	abstract int checktile(AyStarNode current, OpenListNode parent);
	abstract void free();
	abstract void clear();


	/* These will contain the open and closed lists */

	/* The actual closed list */
	Hash ClosedListHash;
	/* The open queue */
	TTDQueue OpenListQueue;
	/* An extra hash to speed up the process of looking up an element in
	 * the open list */
	Hash OpenListHash;




	public AyStar() {
		neighbours = new AyStarNode[12];
	}



	// Adds a node to the OpenList
	//  It makes a copy of node, and puts the pointer of parent in the struct
	void AyStarMain_OpenList_Add(PathNode parent, AyStarNode node, int f, int g)
	{
		// Add a new Node to the OpenList
		OpenListNode new_node = new OpenListNode();
		new_node.g = g;
		new_node.path.parent = parent;
		new_node.path.node = new AyStarNode( node );
		OpenListHash.Hash_Set(node.tile, node.direction, new_node);

		// Add it to the queue
		OpenListQueue.push(new_node, f);
	}

}
