package game.struct;

public class ColorList {
	public int unk0, unk1, unk2;
	public int window_color_1a;
	public int window_color_1b;
	public int window_color_bga, window_color_bgb;
	public int window_color_2;

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
