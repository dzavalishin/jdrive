package game;

// TODO KILL ALL ID classes
@Deprecated
public abstract class AbstractID {
	public int id;
	
	protected AbstractID(int id) {
		this.id = id;
	}
	
	@Override
	public abstract boolean equals(Object obj);
}
