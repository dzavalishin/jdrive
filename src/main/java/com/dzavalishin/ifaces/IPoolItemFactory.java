package com.dzavalishin.ifaces;

import java.io.Serializable;

public interface IPoolItemFactory<T> extends Serializable 
{

	T createObject();
	
}
