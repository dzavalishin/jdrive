package com.dzavalishin.aystar;

public abstract class AyStarImpl extends AyStar {

	/*
	@Override
	int AyStar_CalculateG(AyStarNode current, OpenListNode parent) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	int AyStar_CalculateH(AyStarNode current, OpenListNode parent) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void AyStar_GetNeighbours(OpenListNode current) {
		// TODO Auto-generated method stub

	}

	@Override
	int AyStar_EndNodeCheck(OpenListNode current) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void AyStar_FoundEndNode(OpenListNode current) {
		// TODO Auto-generated method stub

	}

	@Override
	void BeforeExit() {
		// TODO Auto-generated method stub

	}
	 

	//@Override
	void addstart(AyStarNode start_node, int g) {
		if( Global.AYSTAR_DEBUG )
			Global.printf( String.format("[AyStar] Starting A* Algorithm from node (%d, %d, %d)\n", start_node.tile.TileX(), start_node.tile.TileY(), start_node.direction) );

		AyStarMain_OpenList_Add( null, start_node, 0, g);

	}


	/**
	 * This is the function you call to run AyStar.
	 *  return values:
	 *	AYSTAR_FOUND_END_NODE : indicates we found an end node.
	 *	AYSTAR_NO_PATH : indicates that there was no path found.
	 *	AYSTAR_STILL_BUSY : indicates we have done some checked, that we did not found the path yet, and that we still have items left to try.
	 * When the algorithm is done (when the return value is not AYSTAR_STILL_BUSY)
	 * aystar->clear() is called. Note that when you stop the algorithm halfway,
	 * you should still call clear() yourself!
	 * /
	//@Override
	int main() {
		int r, i = 0;
		// Loop through the OpenList
		//  Quit if result is no AYSTAR_STILL_BUSY or is more than loops_per_tick
		while ((r = loop()) == AYSTAR_STILL_BUSY && (loops_per_tick == 0 || ++i < loops_per_tick)) { }
		if( Global.AYSTAR_DEBUG )
		{
			if (r == AYSTAR_FOUND_END_NODE)
				Global.printf("[AyStar] Found path!\n");
			else if (r == AYSTAR_EMPTY_OPENLIST)
				Global.printf("[AyStar] OpenList run dry, no path found\n");
			else if (r == AYSTAR_LIMIT_REACHED)
				Global.printf("[AyStar] Exceeded search_nodes, no path found\n");
		}


		BeforeExit();

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
	 * This function is the core of AyStar. It handles one item and checks
	 *  his neighbour items. If they are valid, they are added to be checked too.
	 *  return values:
	 *	AYSTAR_EMPTY_OPENLIST : indicates all items are tested, and no path
	 *	has been found.
	 *	AYSTAR_LIMIT_REACHED : Indicates that the max_nodes limit has been
	 *	reached.
	 *	AYSTAR_FOUND_END_NODE : indicates we found the end. Path_found now is true, and in path is the path found.
	 *	AYSTAR_STILL_BUSY : indicates we have done this tile, did not found the path yet, and have items left to try.
	 * /

	//@Override
	int loop() {
		int i, r;

		// Get the best node from OpenList
		OpenListNode current = OpenList_Pop();

		// If empty, drop an error
		if (current == null) return AYSTAR_EMPTY_OPENLIST;

		// Check for end node and if found, return that code
		if (EndNodeCheck(current) == AYSTAR_FOUND_END_NODE) {
			//if (aystar->FoundEndNode != null)
			FoundEndNode(current);
			//free(current);
			return AYSTAR_FOUND_END_NODE;
		}

		// Add the node to the ClosedList
		ClosedList_Add(current.path);

		// Load the neighbours
		GetNeighbours(current);

		// Go through all neighbours
		for (i=0;i<num_neighbours;i++) {
			// Check and add them to the OpenList if needed
			r = checktile(neighbours[i], current);
		}

		// Free the node
		//current.free();

		if (max_search_nodes != 0 && ClosedListHash.Hash_Size() >= max_search_nodes)
			// We've expanded enough nodes 
			return AYSTAR_LIMIT_REACHED;
		else
			// Return that we are still busy
			return AYSTAR_STILL_BUSY;
	}


	/**
	 * Checks one tile and calculate his f-value
	 *  return values:
	 *	AYSTAR_DONE : indicates we are done
	 * /

	//@Override
	int checktile(AyStarNode current, OpenListNode parent) 
	{
		int new_f, new_g, new_h;
		PathNode closedlist_parent;
		OpenListNode check;

		// Check the new node against the ClosedList
		if(ClosedList_IsInList( current) != null) return AYSTAR_DONE;

		// Calculate the G-value for this node
		new_g = CalculateG(current, parent);
		// If the value was INVALID_NODE, we don't do anything with this node
		if (new_g == AYSTAR_INVALID_NODE) return AYSTAR_DONE;

		// There should not be given any other error-code..
		assert(new_g >= 0);
		// Add the parent g-value to the new g-value
		new_g += parent.g;
		if (max_path_cost != 0 && new_g > max_path_cost) return AYSTAR_DONE;

		// Calculate the h-value
		new_h = CalculateH(current, parent);
		// There should not be given any error-code..
		assert(new_h >= 0);

		// The f-value if g + h
		new_f = new_g + new_h;

		// Get the pointer to the parent in the ClosedList (the currentone is to a copy of the one in the OpenList)
		closedlist_parent = ClosedList_IsInList( parent.path.node );

		// Check if this item is already in the OpenList
		if ((check = OpenList_IsInList(current)) != null) {
			int i;
			// Yes, check if this g value is lower..
			if (new_g > check.g) return AYSTAR_DONE;
			OpenListQueue.del(check, 0);
			// It is lower, so change it to this item
			check.g = new_g;
			check.path.parent = closedlist_parent;
			// Copy user data, will probably have changed 
			for( i=0; i < current.user_data.length; i++ )
				check.path.node.user_data[i] = current.user_data[i];
			// Readd him in the OpenListQueue
			OpenListQueue.push(check, new_f);
		} else {
			// A new node, add him to the OpenList
			OpenList_Add( closedlist_parent, current, new_f, new_g);
		}

		return AYSTAR_DONE;
	}



	//@Override	void free() {		// Unused in java	}

	//@Override
	void clear() {
		// Clean the Queue, but not the elements within. That will be done by
		// the hash.
		OpenListQueue.clear(false);
		// Clean the hashes
		OpenListHash.clear_Hash( true);
		ClosedListHash.clear_Hash( true);

		if( Global.AYSTAR_DEBUG )

			Global.printf("[AyStar] Cleared AyStar\n");

	}




	// This looks in the Hash if a node exists in ClosedList
	//  If so, it returns the PathNode, else NULL
	PathNode ClosedList_IsInList(AyStarNode node)
	{
		return (PathNode)ClosedListHash.Hash_Get( node.tile, node.direction);
	}

	// This adds a node to the ClosedList
	//  It makes a copy of the data
	void ClosedList_Add(PathNode node)
	{
		// Add a node to the ClosedList
		PathNode new_node = new PathNode(node);

		ClosedListHash.Hash_Set( node.node.tile, node.node.direction, new_node);
	}

	// Checks if a node is in the OpenList
	//   If so, it returns the OpenListNode, else NULL
	OpenListNode OpenList_IsInList(AyStarNode node)
	{
		return (OpenListNode)OpenListHash.Hash_Get( node.tile, node.direction );
	}

	// Gets the best node from OpenList
	//  returns the best node, or NULL of none is found
	// Also it deletes the node from the OpenList
	OpenListNode OpenList_Pop()
	{
		// Return the item the Queue returns.. the best next OpenList item.
		OpenListNode res = (OpenListNode)OpenListQueue.pop();
		if (res != null)
			OpenListHash.Hash_Delete(res.path.node.tile, res.path.node.direction);

		return res;
	}

	// Adds a node to the OpenList
	//  It makes a copy of node, and puts the pointer of parent in the struct
	void OpenList_Add(PathNode parent, AyStarNode node, int f, int g)
	{
		// Add a new Node to the OpenList
		OpenListNode new_node = new OpenListNode();
		new_node.g = g;
		new_node.path.parent = parent;

		//new_node.path.node = *node; 
		new_node.path.node = node; // TODO need a copy?

		OpenListHash.Hash_Set(node.tile, node.direction, new_node);

		// Add it to the queue
		OpenListQueue.push( new_node, f);
	}
	*/
}
