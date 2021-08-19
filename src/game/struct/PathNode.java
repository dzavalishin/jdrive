package game.struct;

import game.AyStarNode;

public class PathNode {
	public AyStarNode node;
	// The parent of this item
	public PathNode parent;

	public PathNode() {
		node = null;
		parent = null;
		
	}

	public PathNode(PathNode src) {
		node = src.node;
		parent = src.parent;
	}

}
