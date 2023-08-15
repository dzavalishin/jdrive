package com.dzavalishin.aystar;

/**
 * Is called when aystar ends it pathfinding, but before cleanup.
 */
@FunctionalInterface
public interface AyStar_BeforeExit {
	void apply(AyStar as);
}