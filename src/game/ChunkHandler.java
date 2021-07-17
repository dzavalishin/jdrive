package game;

// saveload.h
public abstract class ChunkHandler {

	String id;
	int flags;

	abstract void save_proc();
	abstract void load_proc();

	public ChunkHandler(String id, int flags ) {
		this.id = id;
		this.flags = flags;
	}

	
	public static final int CH_RIFF = 0;
	public static final int CH_ARRAY = 1;
	public static final int CH_SPARSE_ARRAY = 2;
	public static final int CH_TYPE_MASK = 3;
	public static final int CH_LAST = 8;
	public static final int CH_AUTO_LENGTH = 16;
	public static final int CH_PRI_0 = 0 << 4;
	public static final int CH_PRI_1 = 1 << 4;
	public static final int CH_PRI_2 = 2 << 4;
	public static final int CH_PRI_3 = 3 << 4;
	public static final int CH_PRI_SHL = 4;
	public static final int CH_NUM_PRI_LEVELS = 4;

}
