package com.dzavalishin.tables;

import com.dzavalishin.game.Sprite;
import com.dzavalishin.struct.Point;

public class TreeTables 
{

	static final int PALETTE_TO_RED = Sprite.PALETTE_TO_RED;
	static final int PALETTE_TO_GREEN = Sprite.PALETTE_TO_GREEN;
	static final int PALETTE_TO_BLUE = Sprite.PALETTE_TO_BLUE;
	static final int PALETTE_TO_PINK = Sprite.PALETTE_TO_PINK;
	static final int PALETTE_TO_PALE_GREEN = Sprite.PALETTE_TO_PALE_GREEN;
	static final int PALETTE_TO_ORANGE = Sprite.PALETTE_TO_ORANGE;
	static final int PALETTE_TO_MAUVE= Sprite.PALETTE_TO_MAUVE;
	static final int PALETTE_TO_PURPLE= Sprite.PALETTE_TO_PURPLE;
	static final int PALETTE_TO_GREY= Sprite.PALETTE_TO_GREY;
	static final int PALETTE_TO_WHITE= Sprite.PALETTE_TO_WHITE;	
	static final int PALETTE_TO_YELLOW= Sprite.PALETTE_TO_YELLOW;	
	static final int PALETTE_TO_CREAM= Sprite.PALETTE_TO_CREAM;
	static final int PALETTE_TO_BROWN= Sprite.PALETTE_TO_BROWN;

	public static final /*SpriteID*/ int _tree_sprites_1[] = {
			0x118D,
			0x11A0,
			0x11B3,
			0x11C6,
	};

	public static final byte _tree_base_by_landscape[] = {0, 12, 20, 32};
	public static final byte _tree_count_by_landscape[] = {12, 8, 12, 9};

	/*
		typedef struct TreePos {
			uint8 x;
			uint8 y;
		} TreePos;
	 */

	//static final TreePos _tree_layout_xy[][4] = 
	public static final Point _tree_layout_xy[][] = 
		{
				{ new Point( 9, 3 ), new Point( 1, 8 ), new Point( 0, 0 ), new Point( 8, 9 ) },
				{ new Point( 4, 4 ), new Point( 9, 1 ), new Point( 6, 9 ), new Point( 0, 9 ) },
				{ new Point( 9, 1 ), new Point( 0, 9 ), new Point( 6, 6 ), new Point( 3, 0 ) },
				{ new Point( 3, 9 ), new Point( 8, 2 ), new Point( 9, 9 ), new Point( 1, 5 ) }
		};

	//static final PalSpriteID _tree_layout_sprite[164+(79-48+1)][4] = 
	public static final /*PalSpriteID*/ int _tree_layout_sprite[][] = 
		{
				{     0x652,      0x659,      0x660,      0x667}, /* 0 */
				{     0x652,      0x667,      0x66e,      0x675}, /* 1 */
				{     0x652,      0x66e,      0x659,      0x675}, /* 2 */
				{     0x652,      0x652,      0x660,      0x66e}, /* 3 */
				{     0x660,      0x667,      0x659,      0x652}, /* 4 */
				{     0x660,      0x675,      0x660,      0x660}, /* 5 */
				{     0x660,      0x652,      0x652,      0x66e}, /* 6 */
				{     0x660,      0x675,      0x667,      0x659}, /* 7 */
				{     0x675,      0x660,      0x675,      0x675}, /* 8 */
				{     0x675,      0x659,      0x652,      0x652}, /* 9 */
				{     0x675,      0x66e,      0x652,      0x652}, /* 10 */
				{     0x675,      0x667,      0x659,      0x667}, /* 11 */
				{     0x628,      0x652,      0x660,      0x62f}, /* 12 */
				{     0x628,      0x636,      0x675,      0x644}, /* 13 */
				{     0x628,      0x652,      0x63d,      0x66e}, /* 14 */
				{     0x628,      0x667,      0x644,      0x652}, /* 15 */
				{     0x644,      0x659,      0x660,      0x628}, /* 16 */
				{     0x644,      0x64b,      0x659,      0x636}, /* 17 */
				{     0x644,      0x675,      0x652,      0x63d}, /* 18 */
				{     0x644,      0x63d,      0x66e,      0x652}, /* 19 */
				{     0x636,      0x636,      0x628,      0x636}, /* 20 */
				{     0x636,      0x63d,      0x636,      0x636}, /* 21 */
				{     0x636,      0x64b,      0x636,      0x636}, /* 22 */
				{     0x636,      0x636,      0x636,      0x636}, /* 23 */
				{     0x64b,      0x628,      0x62f,      0x636}, /* 24 */
				{     0x64b,      0x63d,      0x644,      0x636}, /* 25 */
				{     0x64b,      0x636,      0x63d,      0x628}, /* 26 */
				{     0x64b,      0x64b,      0x636,      0x63d}, /* 27 */
				{     0x62f,      0x644,      0x644,      0x636}, /* 28 */
				{     0x62f,      0x62f,      0x636,      0x628}, /* 29 */
				{     0x62f,      0x64b,      0x636,      0x636}, /* 30 */
				{     0x62f,      0x636,      0x62f,      0x636}, /* 31 */
				{     0x67c,      0x675,      0x683,      0x67c}, /* 32 */
				{     0x67c,      0x69f,      0x67c,      0x659}, /* 33 */
				{     0x67c,      0x67c,      0x67c,      0x6a6}, /* 34 */
				{     0x67c,      0x691,      0x66e,      0x68a}, /* 35 */
				{     0x68a,      0x68a,      0x698,      0x68a}, /* 36 */
				{     0x68a,      0x698,      0x683,      0x68a}, /* 37 */
				{     0x68a,      0x67c,      0x691,      0x68a}, /* 38 */
				{     0x68a,      0x683,      0x6a6,      0x69f}, /* 39 */
				{     0x698,      0x68a,      0x698,      0x652}, /* 40 */
				{     0x698,      0x698,      0x660,      0x667}, /* 41 */
				{     0x698,      0x67c,      0x6a6,      0x698}, /* 42 */
				{     0x698,      0x698,      0x698,      0x691}, /* 43 */
				{     0x6a6,      0x6a6,      0x67c,      0x660}, /* 44 */
				{     0x6a6,      0x69f,      0x6a6,      0x652}, /* 45 */
				{     0x6a6,      0x67c,      0x6a6,      0x691}, /* 46 */
				{     0x6a6,      0x691,      0x69f,      0x6a6}, /* 47 */
				{     0x6ad,      0x6ad,      0x6ad,      0x6ad}, /* 48 */
				{     0x6ad,      0x6ad,      0x6c2,      0x6d0}, /* 49 */
				{     0x6ad,      0x6d7,      0x6ad,      0x6ad}, /* 50 */
				{     0x6ad,      0x6d0,      0x6c9,      0x6ad}, /* 51 */
				{     0x6d0,      0x6d0,      0x6d0,      0x6ad}, /* 52 */
				{     0x6d0,      0x6ad,      0x6d7,      0x6c9}, /* 53 */
				{     0x6d0,      0x6d7,      0x6d0,      0x6c2}, /* 54 */
				{     0x6d0,      0x6d0,      0x6d0,      0x6ad}, /* 55 */
				{     0x6d7,      0x6d7,      0x6d7,      0x6d7}, /* 56 */
				{     0x6d7,      0x6d7,      0x6ad,      0x6ad}, /* 57 */
				{     0x6d7,      0x6d0,      0x6d7,      0x6ad}, /* 58 */
				{     0x6d7,      0x6d7,      0x6d0,      0x6ad}, /* 59 */
				{     0x6c2,      0x6d0,      0x6c9,      0x6c2}, /* 60 */
				{     0x6c2,      0x6c9,      0x6c2,      0x6ad}, /* 61 */
				{     0x6c2,      0x6c2,      0x6c2,      0x6ad}, /* 62 */
				{     0x6c2,      0x6c2,      0x6c2,      0x6c9}, /* 63 */
				{     0x6c9,      0x6d0,      0x6b4,      0x6c2}, /* 64 */
				{     0x6c9,      0x6bb,      0x6de,      0x6d7}, /* 65 */
				{     0x6c9,      0x6c2,      0x6bb,      0x6b4}, /* 66 */
				{     0x6c9,      0x6bb,      0x6c2,      0x6de}, /* 67 */
				{     0x6b4,      0x6b4,      0x6de,      0x6c9}, /* 68 */
				{     0x6b4,      0x6bb,      0x6bb,      0x6ad}, /* 69 */
				{     0x6b4,      0x6de,      0x6bb,      0x6b4}, /* 70 */
				{     0x6b4,      0x6ad,      0x6c2,      0x6de}, /* 71 */
				{     0x6bb,      0x6d0,      0x6de,      0x6c2}, /* 72 */
				{     0x6bb,      0x6b4,      0x6bb,      0x6d7}, /* 73 */
				{     0x6bb,      0x6de,      0x6bb,      0x6b4}, /* 74 */
				{     0x6bb,      0x6c9,      0x6c2,      0x6de}, /* 75 */
				{     0x6de,      0x6d7,      0x6de,      0x6c2}, /* 76 */
				{     0x6de,      0x6bb,      0x6de,      0x6d0}, /* 77 */
				{     0x6de,      0x6de,      0x6bb,      0x6b4}, /* 78 */
				{     0x6de,      0x6c9,      0x6c2,      0x6de}, /* 79 */
				{     0x72b,      0x732,      0x72b,      0x739}, /* 80 */
				{     0x72b,      0x747,      0x755,      0x72b}, /* 81 */
				{     0x72b,      0x72b,      0x76a,      0x786}, /* 82 */
				{     0x72b,      0x74e,      0x72b,      0x72b}, /* 83 */
				{     0x732,      0x732,      0x72b,      0x739}, /* 84 */
				{     0x732,      0x747,      0x732,      0x732}, /* 85 */
				{     0x732,      0x732,      0x755,      0x794}, /* 86 */
				{     0x732,      0x74e,      0x732,      0x78d}, /* 87 */
				{     0x747,      0x732,      0x747,      0x740}, /* 88 */
				{     0x747,      0x747,      0x732,      0x76a}, /* 89 */
				{     0x747,      0x72b,      0x755,      0x747}, /* 90 */
				{     0x747,      0x786,      0x732,      0x747}, /* 91 */
				{     0x74e,      0x74e,      0x72b,      0x794}, /* 92 */
				{     0x74e,      0x755,      0x732,      0x74e}, /* 93 */
				{     0x74e,      0x72b,      0x786,      0x747}, /* 94 */
				{     0x74e,      0x74e,      0x732,      0x794}, /* 95 */
				{     0x76a,      0x76a,      0x74e,      0x74e}, /* 96 */
				{     0x76a,      0x794,      0x732,      0x76a}, /* 97 */
				{     0x76a,      0x732,      0x786,      0x76a}, /* 98 */
				{     0x76a,      0x786,      0x732,      0x78d}, /* 99 */
				{     0x78d,      0x78d,      0x74e,      0x794}, /* 100 */
				{     0x78d,      0x732,      0x739,      0x747}, /* 101 */
				{     0x78d,      0x732,      0x786,      0x76a}, /* 102 */
				{     0x78d,      0x786,      0x78d,      0x794}, /* 103 */
				{     0x786,      0x786,      0x740,      0x732}, /* 104 */
				{     0x786,      0x786,      0x72b,      0x732}, /* 105 */
				{     0x786,      0x732,      0x786,      0x786}, /* 106 */
				{     0x786,      0x786,      0x78d,      0x794}, /* 107 */
				{     0x778,      0x778,      0x77f,      0x778}, /* 108 */
				{     0x778,      0x77f,      0x778,      0x77f}, /* 109 */
				{     0x778,      0x77f,      0x77f,      0x778}, /* 110 */
				{     0x778,      0x778,      0x778,      0x77f}, /* 111 */
				{     0x75c,      0x71d,      0x75c,      0x724}, /* 112 */
				{     0x75c,      0x72b,      0x75c,      0x763}, /* 113 */
				{     0x75c,      0x75c,      0x771,      0x71d}, /* 114 */
				{     0x75c,      0x771,      0x75c,      0x75c}, /* 115 */
				{     0x771,      0x771,      0x75c,      0x71d}, /* 116 */
				{     0x771,      0x747,      0x75c,      0x771}, /* 117 */
				{     0x771,      0x75c,      0x771,      0x724}, /* 118 */
				{     0x771,      0x771,      0x75c,      0x763}, /* 119 */
				{     0x71d,      0x71d,      0x771,      0x724}, /* 120 */
				{     0x71d,      0x74e,      0x763,      0x71d}, /* 121 */
				{     0x71d,      0x724,      0x794,      0x71d}, /* 122 */
				{     0x71d,      0x71d,      0x75c,      0x78d}, /* 123 */
				{     0x794,      0x724,      0x75c,      0x794}, /* 124 */
				{     0x794,      0x794,      0x75c,      0x71d}, /* 125 */
				{     0x794,      0x724,      0x794,      0x71d}, /* 126 */
				{     0x794,      0x794,      0x771,      0x78d}, /* 127 */
				{ 0x79b | PALETTE_TO_RED,  0x79b | PALETTE_TO_PALE_GREEN,  0x79b | PALETTE_TO_MAUVE,  0x79b | PALETTE_TO_PURPLE}, /* 128 */
				{     0x79b,  0x79b | PALETTE_TO_GREY,  0x79b | PALETTE_TO_GREEN,  0x79b | PALETTE_TO_WHITE}, /* 129 */
				{ 0x79b | PALETTE_TO_GREEN,  0x79b | PALETTE_TO_ORANGE,  0x79b | PALETTE_TO_PINK,      0x79b}, /* 130 */
				{ 0x79b | PALETTE_TO_YELLOW,  0x79b | PALETTE_TO_RED,  0x79b | PALETTE_TO_CREAM,  0x79b | PALETTE_TO_RED}, /* 131 */
				{     0x7a2,  0x7a2 | PALETTE_TO_RED,  0x7a2 | PALETTE_TO_PINK,  0x7a2 | PALETTE_TO_PURPLE}, /* 132 */
				{ 0x7a2 | PALETTE_TO_MAUVE,  0x7a2 | PALETTE_TO_GREEN,  0x7a2 | PALETTE_TO_PINK,  0x7a2 | PALETTE_TO_GREY}, /* 133 */
				{ 0x7a2 | PALETTE_TO_RED,  0x7a2 | PALETTE_TO_PALE_GREEN,  0x7a2 | PALETTE_TO_YELLOW,  0x7a2 | PALETTE_TO_WHITE}, /* 134 */
				{ 0x7a2 | PALETTE_TO_ORANGE,  0x7a2 | PALETTE_TO_MAUVE,  0x7a2 | PALETTE_TO_CREAM,  0x7a2 | PALETTE_TO_BROWN}, /* 135 */
				{ 0x7a9 | PALETTE_TO_RED,      0x7a9,  0x7a9 | PALETTE_TO_ORANGE,  0x7a9 | PALETTE_TO_GREY}, /* 136 */
				{ 0x7a9 | PALETTE_TO_ORANGE,  0x7a9 | PALETTE_TO_GREEN,  0x7a9 | PALETTE_TO_PALE_GREEN,  0x7a9 | PALETTE_TO_MAUVE}, /* 137 */
				{ 0x7a9 | PALETTE_TO_PINK,  0x7a9 | PALETTE_TO_RED,  0x7a9 | PALETTE_TO_GREEN,  0x7a9 | PALETTE_TO_BROWN}, /* 138 */
				{ 0x7a9 | PALETTE_TO_GREEN,      0x7a9,  0x7a9 | PALETTE_TO_RED,  0x7a9 | PALETTE_TO_CREAM}, /* 139 */
				{     0x7b0,      0x7b0,      0x7b0,      0x7b0}, /* 140 */
				{     0x7b0,      0x7b0,      0x7b0,      0x7b0}, /* 141 */
				{     0x7b0,      0x7b0,      0x7b0,      0x7b0}, /* 142 */
				{     0x7b0,      0x7b0,      0x7b0,      0x7b0}, /* 143 */
				{ 0x7b7 | PALETTE_TO_PINK,  0x7b7 | PALETTE_TO_RED,  0x7b7 | PALETTE_TO_ORANGE,  0x7b7 | PALETTE_TO_MAUVE}, /* 144 */
				{ 0x7b7 | PALETTE_TO_RED,      0x7b7,  0x7b7 | PALETTE_TO_GREY,  0x7b7 | PALETTE_TO_CREAM}, /* 145 */
				{ 0x7b7 | PALETTE_TO_GREEN,  0x7b7 | PALETTE_TO_BROWN,  0x7b7 | PALETTE_TO_PINK,  0x7b7 | PALETTE_TO_RED}, /* 146 */
				{     0x7b7,  0x7b7 | PALETTE_TO_PALE_GREEN,  0x7b7 | PALETTE_TO_ORANGE,  0x7b7 | PALETTE_TO_RED}, /* 147 */
				{ 0x7be | PALETTE_TO_RED,  0x7be | PALETTE_TO_PINK,  0x7be | PALETTE_TO_GREEN,      0x7be}, /* 148 */
				{ 0x7be | PALETTE_TO_GREEN,  0x7be | PALETTE_TO_BROWN,  0x7be | PALETTE_TO_PURPLE,  0x7be | PALETTE_TO_GREY}, /* 149 */
				{ 0x7be | PALETTE_TO_MAUVE,  0x7be | PALETTE_TO_CREAM,  0x7be | PALETTE_TO_ORANGE,  0x7be | PALETTE_TO_RED}, /* 150 */
				{     0x7be,  0x7be | PALETTE_TO_RED,  0x7be | PALETTE_TO_PALE_GREEN,  0x7be | PALETTE_TO_PINK}, /* 151 */
				{ 0x7c5 | PALETTE_TO_YELLOW,  0x7c5 | PALETTE_TO_RED,  0x7c5 | PALETTE_TO_WHITE,  0x7c5 | PALETTE_TO_CREAM}, /* 152 */
				{ 0x7c5 | PALETTE_TO_RED,  0x7c5 | PALETTE_TO_PALE_GREEN,  0x7c5 | PALETTE_TO_BROWN,  0x7c5 | PALETTE_TO_YELLOW}, /* 153 */
				{     0x7c5,  0x7c5 | PALETTE_TO_PURPLE,  0x7c5 | PALETTE_TO_GREEN,  0x7c5 | PALETTE_TO_YELLOW}, /* 154 */
				{ 0x7c5 | PALETTE_TO_PINK,  0x7c5 | PALETTE_TO_CREAM,      0x7c5,  0x7c5 | PALETTE_TO_GREY}, /* 155 */
				{ 0x7cc | PALETTE_TO_YELLOW,  0x7cc | PALETTE_TO_GREY,  0x7cc | PALETTE_TO_PURPLE,  0x7cc | PALETTE_TO_BROWN}, /* 156 */
				{ 0x7cc | PALETTE_TO_GREEN,      0x7cc,  0x7cc | PALETTE_TO_CREAM,  0x7cc | PALETTE_TO_WHITE}, /* 157 */
				{ 0x7cc | PALETTE_TO_RED,  0x7cc | PALETTE_TO_PALE_GREEN,  0x7cc | PALETTE_TO_MAUVE,  0x7cc | PALETTE_TO_RED}, /* 158 */
				{ 0x7cc | PALETTE_TO_PINK,  0x7cc | PALETTE_TO_ORANGE,  0x7cc | PALETTE_TO_GREEN,  0x7cc | PALETTE_TO_YELLOW}, /* 159 */
				{ 0x7d3 | PALETTE_TO_RED,  0x7d3 | PALETTE_TO_PINK,  0x7d3 | PALETTE_TO_BROWN,  0x7d3 | PALETTE_TO_WHITE}, /* 160 */
				{ 0x7d3 | PALETTE_TO_GREEN,  0x7d3 | PALETTE_TO_ORANGE,  0x7d3 | PALETTE_TO_GREY,  0x7d3 | PALETTE_TO_MAUVE}, /* 161 */
				{ 0x7d3 | PALETTE_TO_YELLOW,  0x7d3 | PALETTE_TO_PALE_GREEN,      0x7d3,  0x7d3 | PALETTE_TO_CREAM}, /* 162 */
				{ 0x7d3 | PALETTE_TO_GREY,  0x7d3 | PALETTE_TO_RED,  0x7d3 | PALETTE_TO_WHITE,      0x7d3}, /* 163 */
				/* the extra things follow */
				{     0x6e5,      0x6e5,      0x6e5,      0x6e5}, /* 0 */
				{     0x6e5,      0x6e5,      0x6fa,      0x708}, /* 1 */
				{     0x6e5,      0x70f,      0x6e5,      0x6e5}, /* 2 */
				{     0x6e5,      0x708,      0x701,      0x6e5}, /* 3 */
				{     0x708,      0x708,      0x708,      0x6e5}, /* 4 */
				{     0x708,      0x6e5,      0x70f,      0x701}, /* 5 */
				{     0x708,      0x70f,      0x708,      0x6fa}, /* 6 */
				{     0x708,      0x708,      0x708,      0x6e5}, /* 7 */
				{     0x70f,      0x70f,      0x70f,      0x70f}, /* 8 */
				{     0x70f,      0x70f,      0x6e5,      0x6e5}, /* 9 */
				{     0x70f,      0x708,      0x70f,      0x6e5}, /* 10 */
				{     0x70f,      0x70f,      0x708,      0x6e5}, /* 11 */
				{     0x6fa,      0x708,      0x701,      0x6fa}, /* 12 */
				{     0x6fa,      0x701,      0x6fa,      0x6e5}, /* 13 */
				{     0x6fa,      0x6fa,      0x6fa,      0x6e5}, /* 14 */
				{     0x6fa,      0x6fa,      0x6fa,      0x701}, /* 15 */
				{     0x701,      0x708,      0x6ec,      0x6fa}, /* 16 */
				{     0x701,      0x6f3,      0x716,      0x70f}, /* 17 */
				{     0x701,      0x6fa,      0x6f3,      0x6ec}, /* 18 */
				{     0x701,      0x6f3,      0x6fa,      0x716}, /* 19 */
				{     0x6ec,      0x6ec,      0x716,      0x701}, /* 20 */
				{     0x6ec,      0x6f3,      0x6f3,      0x6e5}, /* 21 */
				{     0x6ec,      0x716,      0x6f3,      0x6ec}, /* 22 */
				{     0x6ec,      0x6e5,      0x6fa,      0x716}, /* 23 */
				{     0x6f3,      0x708,      0x716,      0x6fa}, /* 24 */
				{     0x6f3,      0x6ec,      0x6f3,      0x70f}, /* 25 */
				{     0x6f3,      0x716,      0x6f3,      0x6ec}, /* 26 */
				{     0x6f3,      0x701,      0x6fa,      0x716}, /* 27 */
				{     0x716,      0x70f,      0x716,      0x6fa}, /* 28 */
				{     0x716,      0x6f3,      0x716,      0x708}, /* 29 */
				{     0x716,      0x716,      0x6f3,      0x6ec}, /* 30 */
				{     0x716,      0x701,      0x6fa,      0x716}, /* 31 */
		};


}
