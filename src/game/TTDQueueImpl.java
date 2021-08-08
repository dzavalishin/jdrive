package game;

import java.util.ArrayList;

public class TTDQueueImpl<ItemType>  extends TTDQueue<ItemType>  
{
	ArrayList<ItemType> list = new ArrayList<ItemType>();
	
	@Override
	public void push(ItemType new_node, int f) {
		list.add(f, new_node);

	}

	@Override
	public ItemType pop() {
		return list.remove(0);
	}

	@Override
	public void clear(boolean b) {
		list.clear();
	}

	@Override
	public void del(ItemType check, int i) {
		assert check == list.remove(i);
	}

}
