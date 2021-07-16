import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * We don't need mem mgmt in java.
 * Just keep a map of int to object.
 */

public class MemoryPool<CType>
{
    Map<Integer,CType> pool = new HashMap<>();

    public void CleanPool()
    {
        pool.clear();
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

}