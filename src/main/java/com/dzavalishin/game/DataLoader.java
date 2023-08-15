package com.dzavalishin.game;

import com.dzavalishin.util.ByteArrayPtr;

// TODO replace with ByteArrayPtr where used, move needed methods there

class DataLoader extends ByteArrayPtr
{
	private final int sprite_offset;

	public DataLoader(byte[] start, int sprite_offset) {
		super(start);
		this.sprite_offset = sprite_offset;
	}




	public DataLoader(DataLoader bufp, int shift) {
		super( bufp, shift );
		sprite_offset = bufp.sprite_offset;
	}




	byte grf_load_byte()
	{
		return rpp();//*(*buf)++;
	}


	int grf_load_ubyte()
	{
		return urpp();//*(*buf)++;
	}


	/**
	 * Bytes left (past current position) > len
	 * @param len number of bytes to check for
	 * @return true if >= len bytes is left
	 */
	public boolean has(int len) {
		return hasBytesLeft() > len;
	}

	public int grf_load_dword_le() 
	{
		int v;
		v =  urpp() << 24; //*(buf++) << 24;
		v |= urpp() << 16; //*(buf++) << 16;
		v |= urpp() << 8; //*(buf++) << 8;
		v |= urpp() << 0; //*(buf++);
		return v;
	}

	int grf_load_word()
	{
		int val;

		val  = urpp();
		val |= urpp() << 8;

		return val;
	}

	int grf_load_extended()
	{
		int val;
		val = grf_load_ubyte();
		if (val == 0xFF) val = grf_load_word();
		return val;
	}

	int grf_load_dword()
	{
		int val;

		val  = urpp();
		val |= urpp() << 8;
		val |= urpp() << 16;
		val |= urpp() << 24;

		return val;
	}

	// zero terminated
	public String grf_load_string() 
	{
		StringBuilder sb = new StringBuilder();
		int c;
		while(true) {
			c = 0xFF & grf_load_byte();
			if( c == 0 ) break;
			sb.append((char) c);
		}
		return sb.toString();
	}

	public String grf_load_string(int start, int [] skip) 
	{
		StringBuilder sb = new StringBuilder();
		int c;
		int count = 0;

		for( int i = start; ; i++)
		{
			c = 0xFF & r(i);
			if( c == 0 ) break;
			sb.append((char) c);
			count++;
		}

		if( skip != null ) skip[0] = count;

		return sb.toString();
	}




	public void check_length( int wanted, String where ) 
	{
		int real = hasBytesLeft();

		if (real < wanted) { 
			GRFFile.grfmsg(GRFFile.severity.GMS_ERROR, "%s/%d: Invalid special sprite length %d (expected %d)!", 
					where, GRFFile._cur_spriteid - sprite_offset, real, wanted); 
			//throw new GrfLoadException();
			Global.fail("NewGrf is dead");
		} 
	} 

}