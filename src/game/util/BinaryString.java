package game.util;

import java.util.Arrays;

/** 
 * 
 * Emulate C byte array strings
 * 
 * @author dz
 *
 * Mutable, mimics StringBuilder to some extent too.
 *
 */

public class BinaryString 
{
	private static final int ALLOC_STEP = 15;
	private static final int ALLOC_START = 15;
	
	private byte[] mem; // = new byte[ALLOC_START];
	int len; // = 15;
	int pos = 0;

	public BinaryString() 
	{
		mem = new byte[ALLOC_START];
		len = mem.length;
		pos = 0;
	}

	public BinaryString( BinaryString bs ) 
	{
		mem = Arrays.copyOf(bs.mem, bs.mem.length);
		len = mem.length;
		pos = len; // append
	}

	public BinaryString( String s ) 
	{
		mem = new byte[s.length()];
		len = mem.length;
		pos = 0;
		append(s);
	}
	
	
	char charAt(int i)
	{
		return (char) (0xFF & (int)mem[i]);
	}

	private void tryExtend(int size) {
		if( len - pos > size)
			return;
		
		int ext = Math.max(size - (len-pos), ALLOC_STEP );
		
	}
	
	
	BinaryString append( char c )
	{
		tryExtend(1);
		mem[pos++] = (byte)c;
		return this;
	}

	BinaryString append( byte c )
	{
		tryExtend(1);
		mem[pos++] = (byte)c;
		return this;
	}


	BinaryString append( String s )
	{
		tryExtend(s.length());
		
		for( char c : s.toCharArray() )
			append(c);
		
		return this;
	}
	
	
}
