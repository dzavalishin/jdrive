package com.dzavalishin.enums;

public enum TileTypes {

	MP_CLEAR,
	MP_RAILWAY,
	MP_STREET,
	MP_HOUSE,
	MP_TREES,
	MP_STATION,
	MP_WATER,
	MP_VOID, // invisible tiles at the SW and SE border
	MP_INDUSTRY,
	MP_TUNNELBRIDGE,
	MP_UNMOVABLE,

	MP_NOCHANGE // used in modify tile func if we don't want to change type
	;

	public static final TileTypes[] values = values();


	//public static int MP_SETTYPE( TileTypes x ) { return  ((x.ordinal()+1) << 8); }

	public static final int MP_MAP2 = 1<<0;
	public static final int MP_MAP3LO = 1<<1;
	public static final int MP_MAP3HI = 1<<2;
	public static final int MP_MAP5 = 1<<3;
	public static final int MP_MAPOWNER_CURRENT = 1<<4;
	public static final int MP_MAPOWNER = 1<<5;

	public static final int MP_TYPE_MASK = 0xF << 8;

	public static final int MP_MAP2_CLEAR = 1 << 12;
	public static final int MP_MAP3LO_CLEAR = 1 << 13;
	public static final int MP_MAP3HI_CLEAR = 1 << 14;

	public static final int MP_NODIRTY = 1<<15;



}
