package com.dzavalishin.util;

import com.dzavalishin.game.Sprite;

/**
 * 
 *  Rail selection types (directions):
 / \    / \    / \    / \   / \   / \
/  /\  /\  \  /===\  /   \ /|  \ /  |\
\/  /  \  \/  \   /  \===/ \|  / \  |/
 \ /    \ /    \ /    \ /   \ /   \ /
  0      1      2      3     4     5
*/

public class AutoRail 
{


	// mark invalid tiles red
	static int RED(int c) { return c | Sprite.PALETTE_SEL_TILE_RED; }

	// table maps each of the six rail directions and tileh combinations to a sprite
	// invalid entries are required to make sure that this array can be quickly accessed
	public static final int _AutorailTilehSprite[][] = {
	// type   0        1        2        3        4        5
		{       0,       8,      16,      25,      34,      42 }, // tileh = 0
		{       5,      13, RED(22), RED(31),      35,      42 }, // tileh = 1
		{       5,      10,      16,      26, RED(38), RED(46) }, // tileh = 2
		{       5,       9, RED(23),      26,      35, RED(46) }, // tileh = 3
		{       2,      10, RED(19), RED(28),      34,      43 }, // tileh = 4
		{       1,       9,      17,      26,      35,      43 }, // tileh = 5
		{       1,      10, RED(20),      26, RED(38),      43 }, // tileh = 6
		{       1,       9,      17,      26,      35,      43 }, // tileh = 7
		{       2,      13,      17,      25, RED(40), RED(48) }, // tileh = 8
		{       1,      13,      17, RED(32),      35, RED(48) }, // tileh = 9
		{       1,       9,      17,      26,      35,      43 }, // tileh = 10
		{       1,       9,      17,      26,      35,      43 }, // tileh = 11
		{       2,       9,      17, RED(29), RED(40),      43 }, // tileh = 12
		{       1,       9,      17,      26,      35,      43 }, // tileh = 13
		{       1,       9,      17,      26,      35,      43 }, // tileh = 14
		{       0,       1,       2,       3,       4,       5 }, // invalid (15)
		{       0,       1,       2,       3,       4,       5 }, // invalid (16)
		{       0,       1,       2,       3,       4,       5 }, // invalid (17)
		{       0,       1,       2,       3,       4,       5 }, // invalid (18)
		{       0,       1,       2,       3,       4,       5 }, // invalid (19)
		{       0,       1,       2,       3,       4,       5 }, // invalid (20)
		{       0,       1,       2,       3,       4,       5 }, // invalid (21)
		{       0,       1,       2,       3,       4,       5 }, // invalid (22)
		{  RED(6), RED(11), RED(17), RED(27), RED(39), RED(47) }, // tileh = 23
		{       0,       1,       2,       3,       4,       5 }, // invalid (24)
		{       0,       1,       2,       3,       4,       5 }, // invalid (25)
		{       0,       1,       2,       3,       4,       5 }, // invalid (26)
		{  RED(7), RED(15), RED(24), RED(33), RED(36), RED(44) }, // tileh = 27
		{       0,       1,       2,       3,       4,       5 }, // invalid (28)
		{  RED(3), RED(14), RED(18), RED(26), RED(41), RED(49) }, // tileh = 29
		{  RED(4), RED(12), RED(21), RED(30), RED(37), RED(45) }, // tileh = 30
	};


	// maps each pixel of a tile (16x16) to a selection type
	// (0,0) is the top corner, (16,16) the bottom corner
	public static final byte _AutorailPiece[][] = {
		{ 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 5, 5, 5, 5, 5, 5 },
		{ 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 5, 5, 5, 5, 5, 5 },
		{ 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 5, 5, 5, 5, 5, 5 },
		{ 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 5, 5, 5, 5, 5, 5 },
		{ 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 5, 5, 5, 5, 5, 5 },
		{ 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 5, 5, 5, 5, 5, 5 },
		{ 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1 },
		{ 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3 },
		{ 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3 },
		{ 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3 },
		{ 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3 },
		{ 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3 },
		{ 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3 }
	};

}
