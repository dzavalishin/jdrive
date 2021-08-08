package game;

public abstract class TTDQueue<ItemType> 
{

	public abstract void push(ItemType new_node, int f);

	public abstract ItemType pop();

	public abstract void clear(boolean b);

	public abstract void del(ItemType check, int i);

}
