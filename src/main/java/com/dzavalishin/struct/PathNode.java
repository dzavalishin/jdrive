package com.dzavalishin.struct;

import com.dzavalishin.aystar.AyStarNode;

public class PathNode {
	public AyStarNode node;
	// The parent of this item
	public PathNode parent;

	public PathNode() {
		node = new AyStarNode();
		parent = null;
		
	}

	public PathNode(PathNode src) {
		node = src.node;
		parent = src.parent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PathNode) {
			PathNode pnode = (PathNode) obj;
			
			if( parent == null && pnode.parent != null ) return false;
			if( parent != null && pnode.parent == null ) return false;
			
			if( node == null && pnode.node != null ) return false;
			if( node != null && pnode.node == null ) return false;
			
			if( parent != null && !parent.equals(pnode.parent) ) return false; 
			if( node != null && !node.equals(pnode.node) ) return false;
			
			return true;
		}
		
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		int hc = 0;
		
		if(parent != null) hc += parent.hashCode() << 2;
		if(node != null) hc += node.hashCode();
		
		return hc;
	}
	
}
