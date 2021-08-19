package game.ids;

import java.io.Serializable;

public abstract class AbstractID implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public final int id;
	
	protected AbstractID(int id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractID) {
			AbstractID him = (AbstractID) obj;
			return him.id == id;
		}
		return false;
	}

	
}
