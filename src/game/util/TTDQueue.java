package game.util;

public abstract class TTDQueue<ItemType> 
{

	public abstract void push(ItemType new_node, int f);

	public abstract ItemType pop();

	public abstract void clear();

	public abstract void del(ItemType check, int i);

}
