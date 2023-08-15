package com.dzavalishin.util;

import java.util.ArrayList;
import java.util.ListIterator;

public class TTDQueueImpl<ItemType>  implements TTDQueue<ItemType>  
{
	
	//private ArrayList<ItemType> list = new ArrayList<ItemType>();
	private final ArrayList<WeightedPair> list = new ArrayList<>();
	
	static class WeightedPair
	{
		final int  weight;
		final Object item;

		public WeightedPair(int w, Object new_node) {
			weight = w;
			item = new_node;
		}
	}
	
	@SuppressWarnings("ConstantConditions")
	@Override
	public void push(ItemType new_node, int f) {
		//list.add(f, new_node);
		
		WeightedPair pair = new WeightedPair(f, new_node);
		
		if(list.isEmpty())
		{
			list.add(pair);
			return;
		}
		
		// sorted list, do bin search
		int low = 0; // will insert before this one, so finally both must be worse than f
		int high = list.size()-1;
		

		while(low < high - 1 )
		{
			int mid = (low+high)/2;
			assert mid > low;
			assert mid < high;

			int midWeight = list.get(mid).weight;
			
			if(f > midWeight) // move to start
				high = mid;
			else if( f < midWeight)
				low = mid;
			else
			{
				assert f == midWeight;
				list.add(mid, pair);
				return;
			}			
		}
		
		int lowWeight = list.get(low).weight;
		int highWeight = list.get(high).weight;
		
		if( f > lowWeight )
			list.add(low, pair);
		else if( f > highWeight )
			list.add(high, pair);
		else
		{
			list.add(high+1,pair);
		}
		
	}

	@Override
	public ItemType pop() {
		if( list.isEmpty()) return null;
		//WeightedPair ret = list.remove(0);
		WeightedPair ret = list.remove(list.size()-1);
		return ret == null ? null : (ItemType) ret.item;
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public void del(ItemType check) 
	{
		for( ListIterator<WeightedPair> i = list.listIterator();
				i.hasNext(); )
		{
			final Object item = i.next().item;
			if(check == item || check.equals(item))
			{
				i.remove();
				return;
			}
		}
	}

}
