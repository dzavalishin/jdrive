package com.dzavalishin.aystar;

import com.dzavalishin.struct.OpenListNode;

@FunctionalInterface
public interface AyStar_CheckTile
{
	int apply(AyStar aystar, AyStarNode current, OpenListNode parent);
}