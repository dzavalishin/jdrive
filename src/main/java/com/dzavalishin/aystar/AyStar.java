package com.dzavalishin.aystar;

import com.dzavalishin.game.NPFFoundTargetData;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.struct.OpenListNode;
import com.dzavalishin.struct.PathNode;
import com.dzavalishin.struct.TileMarker;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.util.Hash;
import com.dzavalishin.util.TTDQueue;
import com.dzavalishin.util.TTDQueueImpl;


public abstract class AyStar extends AyStarDefs
{

	/* These are completely untouched by AyStar, they can be accesed by
	 * the application specific routines to input and output data.
	 * user_path should typically contain data about the resulting path
	 * afterwards, user_target should typically contain information about
	 * what where looking for, and user_data can contain just about
	 * everything */

	public Object user_target;
	public final int [] user_data = new int[10];
	public TransportType userTransportType = TransportType.Invalid;

	// [dz] can be some superclass or interface of NPFFoundTargetData? 
	public NPFFoundTargetData user_path;

	/* How many loops are there called before AyStarMain_Main gives
	 * control back to the caller. 0 = until done */
	private int loops_per_tick;
	/* If the g-value goes over this number, it stops searching
	 *  0 = infinite */
	private int max_path_cost;
	/* The maximum amount of nodes that will be expanded, 0 = infinite */
	private int max_search_nodes;

	/* These should be filled with the neighbours of a tile by
	 * GetNeighbours */
	public final AyStarNode[] neighbours;
	public int num_neighbours;



	/* These will contain the open and closed lists */

	/* The actual closed list */
	protected final Hash ClosedListHash = new Hash();
	/* The open queue */
	final TTDQueue<OpenListNode> OpenListQueue = new TTDQueueImpl<>();
	/* An extra hash to speed up the process of looking up an element in
	 * the open list */
	final Hash OpenListHash = new Hash();


	protected static boolean debug = false;


	protected AyStar() {
		neighbours = new AyStarNode[12];
	}



	/** 
	 * Adds a node to the OpenList
	 * 
	 * It makes a copy of node, and puts the pointer of parent in the struct
	 *  
	**/
	protected void openList_Add(PathNode parent, AyStarNode node, int f, int g)
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

	/**
	 * 
	 * This looks in the Hash if a node exists in ClosedList
	 * 
	 * If so, it returns the PathNode, else NULL
	 * 
	 * @param node
	 * @return
	 */
	protected PathNode closedList_IsInList(AyStarNode node)
	{
		return (PathNode)ClosedListHash.Hash_Get( node.tile, node.direction );
	}

	// This adds a node to the ClosedList
	//  It makes a copy of the data
	protected void closedList_Add(PathNode node)
	{
		// Add a node to the ClosedList
		PathNode new_node = new PathNode( node );
		ClosedListHash.Hash_Set( node.node.tile, node.node.direction, new_node);
	}

	// Checks if a node is in the OpenList
	//   If so, it returns the OpenListNode, else NULL
	protected OpenListNode openList_IsInList(AyStarNode node)
	{
		return (OpenListNode)OpenListHash.Hash_Get(node.tile, node.direction);
	}

	// Gets the best node from OpenList
	//  returns the best node, or NULL of none is found
	// Also it deletes the node from the OpenList
	protected OpenListNode openList_Pop()
	{
		// Return the item the Queue returns.. the best next OpenList item.
		OpenListNode res = OpenListQueue.pop();
		if (res != null)
			OpenListHash.Hash_Delete(res.path.node.tile, res.path.node.direction);

		return res;
	}



	/**
	 * Checks one tile and calculate his f-value
	 *  
	 * @return AYSTAR_DONE : indicates we are done
	 *
	 * Can be overridden
	 */
	int checkTile(AyStarNode current, OpenListNode parent) 
	{
		int new_f, new_g, new_h;
		PathNode closedlist_parent;
		OpenListNode check;

		// Check the new node against the ClosedList
		if (closedList_IsInList(current) != null) return AYSTAR_DONE;

		// Calculate the G-value for this node
		new_g = calculateG(current, parent);
		// If the value was INVALID_NODE, we don't do anything with this node
		if (new_g == AYSTAR_INVALID_NODE) return AYSTAR_DONE;

		// There should not be given any other error-code..
		assert(new_g >= 0);
		// Add the parent g-value to the new g-value
		new_g += parent.g;
		if (getMax_path_cost() != 0 && new_g > getMax_path_cost()) return AYSTAR_DONE;

		// Calculate the h-value
		new_h = calculateH(current, parent);
		// There should not be given any error-code..
		assert(new_h >= 0);

		// The f-value if g + h
		new_f = new_g + new_h;

		// Get the pointer to the parent in the ClosedList (the currentone is to a copy of the one in the OpenList)
		closedlist_parent = closedList_IsInList(parent.path.node);

		// Check if this item is already in the OpenList
		if ((check = openList_IsInList(current)) != null) 
		{
			int i;
			
			// Yes, check if this g value is lower..
			if (new_g > check.g) return AYSTAR_DONE;
			OpenListQueue.del(check);
			
			// It is lower, so change it to this item
			check.g = new_g;
			check.path.parent = closedlist_parent;
			
			// Copy user data, will probably have changed 
			for (i=0;i< current.user_data.length;i++)
				check.path.node.user_data[i] = current.user_data[i];
			
			//check.path.node.user_data = Arrays.copyOf(current.user_data, current.user_data.length);
			
			// Readd him in the OpenListQueue
			OpenListQueue.push(check, new_f);
		} 
		else 
		{
			// A new node, add him to the OpenList
			openList_Add(closedlist_parent, current, new_f, new_g);
			markRed(current.tile); // Debug
		}

		return AYSTAR_DONE;
	}


	/**
	 * 
	 * This function is the core of AyStar. It handles one item and checks
	 *  his neighbour items. If they are valid, they are added to be checked too.
	 *  return values:
	 *	AYSTAR_EMPTY_OPENLIST : indicates all items are tested, and no path
	 *	has been found.
	 *	AYSTAR_LIMIT_REACHED : Indicates that the max_nodes limit has been
	 *	reached.
	 *	AYSTAR_FOUND_END_NODE : indicates we found the end. Path_found now is true, and in path is the path found.
	 *	AYSTAR_STILL_BUSY : indicates we have done this tile, did not found the path yet, and have items left to try.
	 *  <p>
	 *  Was func ptr in C version. So can be overridden.
	 */
	int loop() 
	{
		int i;

		// Get the best node from OpenList
		OpenListNode current = openList_Pop();
		// If empty, drop an error
		if (current == null) return AYSTAR_EMPTY_OPENLIST;

		markBlue(current); // Debug
		
		// Check for end node and if found, return that code
		if (endNodeCheck(current) == AYSTAR_FOUND_END_NODE) {
			foundEndNode(current);
			//free(current);
			return AYSTAR_FOUND_END_NODE;
		}

		// Add the node to the ClosedList
		closedList_Add(current.path);

		// Load the neighbours
		getNeighbours(current);

		// Go through all neighbours
		for (i=0;i<num_neighbours;i++) {
			// Check and add them to the OpenList if needed
			//int r = 
			checkTile(neighbours[i], current);
		}

		if (max_search_nodes != 0 && ClosedListHash.Hash_Size() >= max_search_nodes)
			// We've expanded enough nodes 
			return AYSTAR_LIMIT_REACHED;
		else
			// Return that we are still busy
			return AYSTAR_STILL_BUSY;
	}


	private static void markBlue(OpenListNode current) 
	{
		if( 
				current != null &&
				current.path != null &&
				current.path.node != null &&
				current.path.node.tile != null
		)
			TileMarker.markFlashBlue(current.path.node.tile);
	}

	private static void markRed(TileIndex tile) {
		
		
		if( tile != null )
			TileMarker.markFlashRed(tile);
		
	}
	

	/**
	 * This function make the memory go back to zero
	 * 
	 * This function should be called when you are using the same instance again.
	 */
	void clear() 
	{
		// Clean the Queue
		OpenListQueue.clear();
		// Clean the hashes
		OpenListHash.clear_Hash(true);
		ClosedListHash.clear_Hash(true);

		if(debug) System.out.printf("[AyStar] Cleared AyStar%n");
	}

	/**
	 * This is the function you call to run AyStar.
	 * 
	 * @return
	 *	AYSTAR_FOUND_END_NODE : indicates we found an end node.
	 *	AYSTAR_NO_PATH : indicates that there was no path found.
	 *	AYSTAR_STILL_BUSY : indicates we have done some checked, that we did not found the path yet, and that we still have items left to try.
	 * <p>
	 * When the algorithm is done (when the return value is not AYSTAR_STILL_BUSY)
	 * aystar.clear() is called. Note that when you stop the algorithm halfway,
	 * you should still call clear() yourself!
	 */
	public int main() {
		int r, i = 0;
		// Loop through the OpenList
		//  Quit if result is no AYSTAR_STILL_BUSY or is more than loops_per_tick
		//noinspection StatementWithEmptyBody
		while ((r = loop()) == AYSTAR_STILL_BUSY && (getLoops_per_tick() == 0 || ++i < getLoops_per_tick()))
			;
		
		if(debug) 
		{
		if (r == AYSTAR_FOUND_END_NODE)
			System.out.printf("[AyStar] Found path!%n");
		else if (r == AYSTAR_EMPTY_OPENLIST)
			System.out.printf("[AyStar] OpenList run dry, no path found%n");
		else if (r == AYSTAR_LIMIT_REACHED)
			System.out.printf("[AyStar] Exceeded search_nodes, no path found%n");
		}

		
		beforeExit();

		if (r != AYSTAR_STILL_BUSY)
			// We're done, clean up 
			clear();

		// Check result-value
		if (r == AYSTAR_FOUND_END_NODE) return AYSTAR_FOUND_END_NODE;
		// Check if we have some left in the OpenList
		if (r == AYSTAR_EMPTY_OPENLIST || r == AYSTAR_LIMIT_REACHED) return AYSTAR_NO_PATH;

		// Return we are still busy
		return AYSTAR_STILL_BUSY;
	}


	/**
	 * 
	 * Adds a node from where to start an algorithm. Multiple nodes can be added
	 * if wanted. You should make sure that clear() is called before adding nodes
	 * if the AyStar has been used before (though the normal main loop calls
	 * clear() automatically when the algorithm finishes
	 * g is the cost for starting with this node.
	 * <p>
	 * Can be overridden.
	 */
	public void addStartNode(AyStarNode start_node, int g) 
	{
		if(debug) 
			System.out.printf("[AyStar] Starting A* Algorithm from node (%d, %d, %d)%n",
					start_node.tile.getX(), start_node.tile.getY(), start_node.direction);

		openList_Add(null, start_node, 0, g);
		TileMarker.mark(start_node.tile, 209);
	}

	public int getMax_search_nodes() {
		return max_search_nodes;
	}

	public void setMax_search_nodes(int max_search_nodes) {
		this.max_search_nodes = max_search_nodes;
	}

	public int getMax_path_cost() {
		return max_path_cost;
	}

	public void setMax_path_cost(int max_path_cost) {
		this.max_path_cost = max_path_cost;
	}

	public int getLoops_per_tick() {
		return loops_per_tick;
	}

	public void setLoops_per_tick(int loops_per_tick) {
		this.loops_per_tick = loops_per_tick;
	}	


	// To be defined by caller
	
	public abstract int endNodeCheck(OpenListNode current);
	protected abstract int calculateG (AyStarNode current, OpenListNode parent);
	protected abstract int calculateH(AyStarNode current, OpenListNode parent);
	protected abstract void getNeighbours(OpenListNode current);
		
	protected void beforeExit() { } 
	protected void foundEndNode(OpenListNode current) { }	
}


