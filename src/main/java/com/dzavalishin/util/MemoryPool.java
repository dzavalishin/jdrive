package com.dzavalishin.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.dzavalishin.game.Industry;
import com.dzavalishin.ifaces.IPoolItem;
import com.dzavalishin.ifaces.IPoolItemFactory;

/**
 * 
 * Pool if game items.
 * 
 */

public class MemoryPool<CType extends IPoolItem> implements Serializable
{
	private static final long serialVersionUID = -6590423407301731923L;
	
	private final Map<Integer,CType> pool = new HashMap<>();
    private final IPoolItemFactory<CType> ctor;
    //private static int lastIndex = 1;
    //private static int lastIndex = 0;
    private int lastIndex = 0;


    public MemoryPool(IPoolItemFactory<CType> ctor) {
		this.ctor = ctor;
		CleanPool();
	}
    
    public void CleanPool()
    {
        pool.clear();
        lastIndex = 0;
    }

    public CType GetItemFromPool(int index)
    {
        return pool.get(index);
    }

    public void PutItemToPool(CType item, int index)
    {
        pool.put(index, item);
    }


    public int total_items()
    {
        return pool.size();
    }

   /*public void forEach( BiConsumer<Integer,CType> c )
    {
        pool.forEach( c );
    }*/

    public void forEach( Consumer<CType> c )
    {
        //pool.forEach( (i,o) -> c.accept(o) );
        // Fifgt concurrent mod
        List<CType> s = new ArrayList<CType>(pool.values());
        //s.forEach( (o) -> c.accept(o) );
        s.forEach( c );
    }

    public void forEachValid( Consumer<CType> c )
    {
        //pool.forEach( (i,o) -> { if( o.isValid() ) c.accept(o); } );
        List<CType> s = new ArrayList<CType>(pool.values());
        s.forEach( o -> { if( o.isValid() ) c.accept(o); } );
    }

	public boolean AddBlockToPool() 
	{
		int key = lastIndex ++;
		CType o = ctor.createObject();
		o.setIndex(key);
		pool.put(key, o);
		
		return true;
	}

	/**
	 * Adds blocks to the pool if needed (and possible) till index fits inside the pool
	 *
	 * @return Returns false if adding failed
	 */
	public boolean AddBlockIfNeeded(int index)
	{
		while (index >= total_items()) {
			if (!AddBlockToPool())
				return false;
		}

		return true;
	}

	public Iterator<CType> getIterator()
	{
		return pool.values().iterator();
	}

	public Industry[] getValuesArray() {
		return pool.values().toArray(Industry[]::new);
	}

	public int size() {
		return pool.size();
	}

	public Stream<CType> stream() {
		return pool.values().stream();		
	}

	
}




