package game;

import java.util.function.Consumer;

public class Tile {

	//byte type_height;
	int type;
	int height;

	int m1; // owner?
	int m2;
	byte m3;
	byte m4;
	byte m5;
	byte extra;

	// TODO use DiaginalDirections instead
	public static final int DIAGDIR_NE  = 0;      /* Northeast, upper right on your monitor */
	public static final int DIAGDIR_SE  = 1;
	public static final int DIAGDIR_SW  = 2;
	public static final int DIAGDIR_NW  = 3;
	public static final int DIAGDIR_END = 4;
	public static final int INVALID_DIAGDIR = 0xFF;



} 

