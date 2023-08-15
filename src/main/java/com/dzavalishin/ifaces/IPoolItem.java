package com.dzavalishin.ifaces;

import java.io.Serializable;

public interface IPoolItem extends Serializable 
{

	void setIndex(int index);
	boolean isValid();
	
	//public static Iterator<Player> getIterator();
	//public static void forEach( Consumer<Player> p )

}
