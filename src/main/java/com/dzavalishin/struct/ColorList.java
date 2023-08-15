package com.dzavalishin.struct;

import java.io.Serializable;

public class ColorList implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
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
		
		unk0 = Byte.toUnsignedInt(b[i++]);		 
		unk1 = Byte.toUnsignedInt(b[i++]);
		unk2 = Byte.toUnsignedInt(b[i++]);
		window_color_1a = Byte.toUnsignedInt(b[i++]);
		window_color_1b = Byte.toUnsignedInt(b[i++]);
		window_color_bga = Byte.toUnsignedInt(b[i++]);
		window_color_bgb = Byte.toUnsignedInt(b[i++]);
		window_color_2 = Byte.toUnsignedInt(b[i++]);		
	}
}
