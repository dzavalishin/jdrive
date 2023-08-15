package com.dzavalishin.aystar;

import com.dzavalishin.struct.OpenListNode;

/**
 * If the End Node is found, this function is called.
 *  It can do, for example, calculate the route and put that in an array
 */
@FunctionalInterface
public interface AyStar_FoundEndNode {
	void apply(AyStar as, OpenListNode current);
}