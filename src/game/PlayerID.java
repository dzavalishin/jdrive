package game;



public class PlayerID extends AbstractID {

	public PlayerID(int i) {
		id = i;
	}

	public PlayerID(Owner o) {
		id = o.owner;
	}

	public Player GetPlayer() {
		// TODO Auto-generated method stub
		//return null;
	}

}
