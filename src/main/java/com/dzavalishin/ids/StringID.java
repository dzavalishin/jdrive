package com.dzavalishin.ids;

import java.io.Serializable;

import com.dzavalishin.game.Str;

public class StringID implements Serializable //extends AbstractID 
{
	private static final long serialVersionUID = 1L;
	
	public final int id;
	
	public StringID(int i) {
		id = i;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof StringID) {
			StringID si = (StringID) obj;
			return si.id == id;
		}
		
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return id;
	}

	public boolean isValid() {
		return  id != Str.INVALID_STRING;
	}

	private static StringID invalid = null;
	
	public static StringID getInvalid() {
		if( invalid == null )
			invalid = new StringID(Str.INVALID_STRING);
		return invalid;
	}
	
}
