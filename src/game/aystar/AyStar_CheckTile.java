package game.aystar;

import game.struct.OpenListNode;

@FunctionalInterface
public interface AyStar_CheckTile
{
	int apply(AyStar aystar, AyStarNode current, OpenListNode parent);
}