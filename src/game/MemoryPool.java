package game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * We don't need mem mgmt in java.
 * Just keep a map of int to object.
 * 
 * TODO use dynamic array?
 */

public class MemoryPool<CType extends IPoolItem>
{
    Map<Integer,CType> pool = new HashMap<>();
    IPoolItemFactory<CType> ctor;
    //private static int lastIndex = 1;
    private static int lastIndex = 0;


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

    public void forEach( BiConsumer<Integer,CType> c )
    {
        pool.forEach( c );
    }

    public void forEach( Consumer<CType> c )
    {
        pool.forEach( (i,o) -> c.accept(o) );
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
	boolean AddBlockIfNeeded(int index)
	{
		while (index >= total_items()) {
			if (!AddBlockToPool())
				return false;
		}

		return true;
	}

	Iterator<CType> getIterator()
	{
		return pool.values().iterator();
	}
	
}




