package game;


public abstract class AbstractID {
	public int id;
	
	protected AbstractID(int id) {
		this.id = id;
	}
	
	//@Override	public abstract boolean equals(Object obj);
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractID) {
			AbstractID him = (AbstractID) obj;
			return him.id == id;
		}
		return false;
	}

	
}
