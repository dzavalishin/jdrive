package com.dzavalishin.game;

import java.io.Serializable;

import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;

public class Tile implements Serializable 
{

	private static final long serialVersionUID = -6829939704826964776L;
	
	public int type;
	public int height;

	public int m1; // owner?
	public int m2;

	//byte m3;
	//byte m4;
	//byte m5;
	//byte extra;

	// being bytes they're treated as negative too often
	public int m3;
	public int m4;
	public int m5;
	public int extra;
	
	// TODO use DiaginalDirections instead
	public static final int DIAGDIR_NE  = 0;      /* Northeast, upper right on your monitor */
	public static final int DIAGDIR_SE  = 1;
	public static final int DIAGDIR_SW  = 2;
	public static final int DIAGDIR_NW  = 3;
	public static final int DIAGDIR_END = 4;
	public static final int INVALID_DIAGDIR = 0xFF;
	
	public byte get_type_height() {
		return (byte) (((type << 4) & 0xF0) | (height & 0x0F));
	}
	
	public void set_type_height(byte b) {
		type = b >> 4;
		height = b & 0xF;		
	}

	public Tile() {
		type        = TileTypes.MP_CLEAR.ordinal();
		height      = 0;
		m1          = Owner.OWNER_NONE;
		m2          = 0;
		m3          = 0;
		m4          = 0;
		m5          = 3;
		extra       = 0;
	}


} 

