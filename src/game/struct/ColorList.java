package game.struct;

public class ColorList {
	public final int unk0;
    public final int unk1;
    public final int unk2;
	public final int window_color_1a;
	public final int window_color_1b;
	public final int window_color_bga;
    public final int window_color_bgb;
	public final int window_color_2;

	public ColorList(byte[] b) 
	{
		int i = 0;
		
		unk0 = b[i++];		 
		unk1 = b[i++];
		unk2 = b[i++];
		window_color_1a = b[i++];
		window_color_1b = b[i++];
		window_color_bga = b[i++];
		window_color_bgb = b[i++];
		window_color_2 = b[i++];		
	}

}
