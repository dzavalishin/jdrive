package game;

public class PathNode {
	AyStarNode node;
	// The parent of this item
	PathNode parent;

	public PathNode() {
		node = null;
		parent = null;
		
	}

	public PathNode(PathNode src) {
		node = src.node;
		parent = src.parent;
	}

}
