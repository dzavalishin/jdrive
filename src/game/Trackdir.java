package game;

public enum Trackdir {
	
	TRACKDIR_DIAG1_NE ( 0),
	TRACKDIR_DIAG2_SE ( 1),
	TRACKDIR_UPPER_E  ( 2),
	TRACKDIR_LOWER_E  ( 3),
	TRACKDIR_LEFT_S   ( 4),
	TRACKDIR_RIGHT_S  ( 5),
	/* Note the two missing values here. This enables trackdir -> track
	 * conversion by doing (trackdir & 7) */
	TRACKDIR_DIAG1_SW ( 8),
	TRACKDIR_DIAG2_NW ( 9),
	TRACKDIR_UPPER_W  ( 10),
	TRACKDIR_LOWER_W  ( 11),
	TRACKDIR_LEFT_N   ( 12),
	TRACKDIR_RIGHT_N  ( 13),
	TRACKDIR_END(14),
	INVALID_TRACKDIR  ( 0xFF),
	;

	
	/** These are a combination of tracks and directions. Values are 0-5 in one
	direction (corresponding to the Track enum) and 8-13 in the other direction. */
	//typedef enum TrackdirBits {
		public static final int TRACKDIR_BIT_DIAG1_NE = 0x1;
		public static final int TRACKDIR_BIT_DIAG2_SE = 0x2;
		public static final int TRACKDIR_BIT_UPPER_E  = 0x4;
		public static final int TRACKDIR_BIT_LOWER_E  = 0x8;
		public static final int TRACKDIR_BIT_LEFT_S   = 0x10;
		public static final int TRACKDIR_BIT_RIGHT_S  = 0x20;
		/* Again; note the two missing values here. This enables trackdir -> track conversion by doing (trackdir & 0xFF) */
		public static final int TRACKDIR_BIT_DIAG1_SW = 0x0100;
		public static final int TRACKDIR_BIT_DIAG2_NW = 0x0200;
		public static final int TRACKDIR_BIT_UPPER_W  = 0x0400;
		public static final int TRACKDIR_BIT_LOWER_W  = 0x0800;
		public static final int TRACKDIR_BIT_LEFT_N   = 0x1000;
		public static final int TRACKDIR_BIT_RIGHT_N  = 0x2000;
		
		public static final int TRACKDIR_BIT_MASK	= 0x3F3F;
		public static final int INVALID_TRACKDIR_BIT  = 0xFFFF;
//	} TrackdirBits;
	
	
	private int value; 
	private Trackdir(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

	public static final Trackdir values[] = values();

	
	/* Maps a trackdir to the (4-way) direction the tile is exited when following
	 * that trackdir */
	private static final DiagDirection _trackdir_to_exitdir[] = {
		DIAGDIR_NE,DIAGDIR_SE,DIAGDIR_NE,DIAGDIR_SE,DIAGDIR_SW,DIAGDIR_SE, DIAGDIR_NE,DIAGDIR_NE,
		DIAGDIR_SW,DIAGDIR_NW,DIAGDIR_NW,DIAGDIR_SW,DIAGDIR_NW,DIAGDIR_NE,
	};

	/**
	 * Maps a trackdir to the (4-way) direction the tile is exited when following
	 * that trackdir.
	 */
	public static DiagDirection TrackdirToExitdir() 
	{
		//extern const DiagDirection _trackdir_to_exitdir[TRACKDIR_END];
		return _trackdir_to_exitdir[ordinal()];
	}

	static Trackdir fromInt(int direction) {		
		return values[direction];
	}
	
}
