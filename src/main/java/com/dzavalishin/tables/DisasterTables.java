package com.dzavalishin.tables;

import com.dzavalishin.game.Global;

public class DisasterTables 
{

	
	static final /*SpriteID*/ int  _disaster_images_1[] = {0xF41,0xF41,0xF41,0xF41,0xF41,0xF41,0xF41,0xF41};
	static final /*SpriteID*/ int  _disaster_images_2[] = {0xF44,0xF44,0xF44,0xF44,0xF44,0xF44,0xF44,0xF44};
	static final /*SpriteID*/ int  _disaster_images_3[] = {0xF4E,0xF4E,0xF4E,0xF4E,0xF4E,0xF4E,0xF4E,0xF4E};
	static final /*SpriteID*/ int  _disaster_images_4[] = {0xF46,0xF46,0xF47,0xF47,0xF48,0xF48,0xF49,0xF49};
	static final /*SpriteID*/ int  _disaster_images_5[] = {0xF4A,0xF4A,0xF4B,0xF4B,0xF4C,0xF4C,0xF4D,0xF4D};
	static final /*SpriteID*/ int  _disaster_images_6[] = {0xF50,0xF50,0xF50,0xF50,0xF50,0xF50,0xF50,0xF50};
	static final /*SpriteID*/ int  _disaster_images_7[] = {0xF51,0xF51,0xF51,0xF51,0xF51,0xF51,0xF51,0xF51};
	static final /*SpriteID*/ int  _disaster_images_8[] = {0xF52,0xF52,0xF52,0xF52,0xF52,0xF52,0xF52,0xF52};
	static final /*SpriteID*/ int  _disaster_images_9[] = {0xF3E,0xF3E,0xF3E,0xF3E,0xF3E,0xF3E,0xF3E,0xF3E};
	
	protected static final /*SpriteID*/ int   _disaster_images[][] = {
			_disaster_images_1,_disaster_images_1,
			_disaster_images_2,_disaster_images_2,
			_disaster_images_3,_disaster_images_3,
			_disaster_images_8,_disaster_images_8,_disaster_images_9,
			_disaster_images_6,_disaster_images_6,
			_disaster_images_7,_disaster_images_7,
			_disaster_images_4,_disaster_images_5,
		};
	
	static MinMax MK(int a, int b) { return new MinMax( (a) - Global.MAX_YEAR_BEGIN_REAL, (b) - Global.MAX_YEAR_BEGIN_REAL); }

	public static class MinMax {
		public MinMax(int i, int j) {
			min = i;
			max = j;
		}
		public final int min;
		public final int max;
	} 
	
	public static final MinMax [] _dis_years = {
		MK(1930, 1955),
		MK(1940, 1970),
		MK(1960, 1990),
		MK(1970, 2000),
		MK(2000, 2100),
		MK(1940, 1965),
		MK(1975, 2010),
		MK(1950, 1985)
	};
	
	
}
