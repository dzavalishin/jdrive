package com.dzavalishin.aystar;

@FunctionalInterface
public interface AyStar_AddStartNode {
	void apply(AyStar aystar, AyStarNode start_node, int g);
}