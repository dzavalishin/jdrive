package com.dzavalishin.ids;

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
			if( getClass() != him.getClass() ) return false;
			return him.id == id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id;
	}
	
}
