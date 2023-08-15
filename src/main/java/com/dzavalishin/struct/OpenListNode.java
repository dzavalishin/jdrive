package com.dzavalishin.struct;

import com.dzavalishin.game.Global;

public class OpenListNode {
	public int g;
	public final PathNode path = new PathNode();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OpenListNode) {
			OpenListNode node = (OpenListNode) obj;
			
			boolean eq =  node.path.equals(path);
			
			if(eq)
			{
				if( node.g != g )
					Global.error("Same path, differeng g");
			}
			
			return eq;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return path.hashCode()+g;
	}
}
