import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * We don't need mem mgmt in java.
 * Just keep a map of int to object.
 */

public class MemoryPool<CType>
{
    Map<Integer,CType> pool = new HashMap<>();

    public CleanPool()
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

    public forEach( BiConsumer c )
    {
        pool.forEach( c );
    }

}