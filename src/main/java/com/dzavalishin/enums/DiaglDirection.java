package com.dzavalishin.enums;

public enum DiaglDirection 
{
	DIAGDIR_NE( 0 ),      // Northeast, upper right on your monitor 
	DIAGDIR_SE(  1 ),
	DIAGDIR_SW(  2 ),
	DIAGDIR_NW(  3 ),
	DIAGDIR_END( 4 ),
	INVALID_DIAGDIR ( 0xFF );
	
	private final int value;
	DiaglDirection(int value)
	{ 
		this.value = value; 
	}
	
	public int getValue() {
		return value;
	}
	
	public static DiaglDirection [] values = values();
}