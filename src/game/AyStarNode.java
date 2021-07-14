package game;

public class AyStarNode {
	TileIndex tile;
	int direction;
	int user_data[];

	
	public AyStarNode() {
		user_data = new int[2];
	}


	public AyStarNode(AyStarNode node) {
		tile = node.tile;
		direction = node.direction;
		user_data = new int[2];
		System.arraycopy(node.user_data, 0, user_data, 0, user_data.length);						
	}
	
}
