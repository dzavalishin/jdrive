package game;

public class ColorList {
	int unk0, unk1, unk2;
	int window_color_1a, window_color_1b;
	int window_color_bga, window_color_bgb;
	int window_color_2;

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
