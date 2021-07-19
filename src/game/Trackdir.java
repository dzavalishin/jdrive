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
	
	
	private int value; 
	private Trackdir(int value) 
	{ 
		this.value = value; 
	}

	public int getValue() {
		return value;
	}

	public static final Trackdir values[] = values();
	
}
