package com.dzavalishin.util;

import com.dzavalishin.game.Hal;
import com.dzavalishin.tables.TrackPathFinderTables;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

// macros.h stuff
public class BitOps {
	private BitOps() { } // Prevent construction

	/// Fetch n bits starting at bit s from x
	static public int GB(int x, int s, int n) { return (x >> s) & ((1 << n) - 1); }

	/// Set n bits starting at bit s in x to d
	static public void SB(IntContainer x, int s, int n, int d)
	{
		x.v = (x.v & ~(((1 << n) - 1) << s)) | (d << s);
	}

	/// Set n bits starting at bit s in x to d
	static public int RETSB(int x, int s, int n, int d)
	{
		x = (x & ~(((1 << n) - 1) << s)) | (d << s);
		return x;
	}

	/// Add i to the n bits starting at bit s in x
	static public int RETAB(int x, int s, int n, int i)
	{
		return (x & ~(((1 << n) - 1) << s)) | ((x + (i << s)) & (((1 << n) - 1) << s));	
	}


	/*
	static public int min(int a, int b) { if (a <= b) return a; return b; }
	static public int max(int a, int b) { if (a >= b) return a; return b; }
	static public long max64(long a, long b) { if (a >= b) return a; return b; }
	*/
	//static  uint minu(uint a, uint b) { if (a <= b) return a; return b; }
	//static  uint maxu(uint a, uint b) { if (a >= b) return a; return b; }


	public static long minu(int a, int b) 
	{ 
		long la = Integer.toUnsignedLong(a); long lb = Integer.toUnsignedLong(b); 
		if (la <= lb) return la; 
		return lb; 
	}

	public static long maxu(int a, int b) 
	{ 
		long la = Integer.toUnsignedLong(a); long lb = Integer.toUnsignedLong(b); 
		if (la >= lb) return la; 
		return lb; 
	}

	static public int clamp(int a, int min, int max)
	{
		if (a <= min) return min;
		if (a >= max) return max;
		return a;
	}

	static public long clamp(long a, long min, long max)
	{
		if (a <= min) return min;
		if (a >= max) return max;
		return a;
	}

	static public int BIGMULSS(int a, int b, int shift) {
		return (int)(((long)(a) * (long)(b)) >> (shift));
	}

	static public long BIGMULSS64(long a, long b, int shift) {
		return ((a) * (b)) >> (shift);
	}

	/*
	static  uint BIGMULUS(uint a, uint b, int shift) {
		return (uint)(((ulong)(a) * (ulong)(b)) >> (shift));
	}

	static  long BIGMULS(int a, int b) {
		return (int)(((long)(a) * (long)(b)));
	}
	 */

	/* OPT: optimized into an unsigned comparison */
	//static void IS_INSIDE_1D(x, base, size) ((x) >= (base) && (x) < (base) + (size))
	public static boolean IS_INSIDE_1D(int x, int base, int size)
	{
		return (x >= base) && (x < (base + size));
		//assert (x) - (base) > 0;
		//assert size > 0;
		//return (x - base) < size;
	}

	static public boolean HASBIT(int x, int y) { return 0 != (x &   (1 << y)); }
	//static void SETBIT(x,y)    ((x) |=  (1 << (y)))
	//static void CLRBIT(x,y)    ((x) &= ~(1 << (y)))
	//static void TOGGLEBIT(x,y) ((x) ^=  (1 << (y)))

	public static byte RETCLRBIT(byte x, int y) {
		x &= ~(1 << y);
		return x;
	}

	public static byte RETSETBIT(byte x, int y) {
		x |=  (1 << y);
		return x;
	}

	public static byte RETTOGGLEBIT(byte x, int y) {
		x ^=  (1 << y);
		return x;
	}

	public static int RETCLRBIT(int x, int y) {
		x &= ~(1 << y);
		return x;
	}

	public static int RETSETBIT(int x, int y) {
		x |=  (1 << y);
		return x;
	}

	public static int RETTOGGLEBIT(int x, int y) {
		x ^=  (1 << y);
		return x;
	}


	// checking more bits. Maybe unneccessary, but easy to use
	static public boolean HASBITS(int x, int y) { return 0 != (x & y); }
	//static void SETBITS(x,y) ((x) |= (y))
	//static void CLRBITS(x,y) ((x) &= ~(y))

	public static int RETSETBITS( int x, int bits)
	{
		x |= bits;
		return x;
	}

	public static byte RETSETBITS( byte x, byte bits)
	{
		x |= bits;
		return x;
	}

	public static int RETCLRBITS( int x, int y)
	{
		x &= ~y;
		return x;
	}

	public static byte RETCLRBITS( byte x, byte y)
	{
		x &= ~y;
		return x;
	}


	//static void PLAYER_SPRITE_COLOR(owner) ( (_player_colors[owner] + 0x307) << PALETTE_SPRITE_START)
	//static void SPRITE_PALETTE(x) ((x) | PALETTE_MODIFIER_COLOR)

	//extern const byte _ffb_64[128];

	/**
	 * Returns the position of the first bit that is not zero, counted from the
	 * RIGHT. Ie, 10110100 returns 2, 00000001 returns 0, etc. 
	 * 
	 * LIMITED TO LOWER 6 BITS!
	 * 
	 * @return When x == 0 returns 0
	 */
	public static int FIND_FIRST_BIT(int x) { return TrackPathFinderTables._ffb_64[(x & 0xFF)]; }
	
	/**
	 * Returns x with the first bit that is not zero, counted from the RIGHT, set
	 * to zero. So, 10110100 returns 10110000, 00000001 returns 00000000, etc.

	 * LIMITED TO LOWER 6 BITS!
	 * 
	 */
	public static int KILL_FIRST_BIT(int x) { return TrackPathFinderTables._ffb_64[(x & 0xFF)+64]; }
	
	public static  int FindFirstBit2x64(int value)
	{

	//	int i = 0;
	//	if ( (byte) value == 0) {
	//		i += 8;
	//		value >>= 8;
	//	}
	//	return i + FIND_FIRST_BIT(value & 0x3F);

	//Faster ( or at least cleaner ) implementation below?

		if (GB(value, 0, 8) == 0) {
			return FIND_FIRST_BIT(GB(value, 8, 6)) + 8;
		} else {
			return FIND_FIRST_BIT(GB(value, 0, 6));
		}

	}


	public static  int KillFirstBit2x64(int value)
	{
		if (GB(value, 0, 8) == 0) {
			return KILL_FIRST_BIT(GB(value, 8, 6)) << 8;
		} else {
			return value & (KILL_FIRST_BIT(GB(value, 0, 6)) | 0x3F00);
		}
	}

	/* [min,max), strictly less than */
	//static void IS_BYTE_INSIDE(a,min,max) ((byte)((a)-(min)) < (byte)((max)-(min)))
	//IS_BYTE_INSIDE(a,min,max)

	public static boolean IS_BYTE_INSIDE(int ia, int imin, int imax)
	{
		int a = 0xFF & ia;
		int min = 0xFF & imin;
		int max = 0xFF & imax;
		
		return Integer.compareUnsigned(a-min, max-min) < 0;
		//return ((uint)((a)-(min)) < (uint)((max)-(min)))
	}

	public static boolean IS_INT_INSIDE(int a, int min, int max)
	{
		return Integer.compareUnsigned(a-min, max-min) < 0;
		//return ((uint)((a)-(min)) < (uint)((max)-(min)))
	}

	public static boolean CHANCE16(int a, int b) { 
		return Hal.Random() <= (0xFFFF * a) / b;
	}
	
	public static boolean CHANCE16R(int a, int b, int [] rv) 
	{ 
		//return ((r[0]=Hal.Random()) <= ((Integer.MAX_VALUE * a) / b));
		final int r = Hal.Random();
		rv[0] = r;
		//final double v = (Integer.MAX_VALUE * (double)a) / b;
		final int v = (int)( (Integer.MAX_VALUE*2L * (double)a) / b );
		return Integer.compareUnsigned( r, v ) <= 0; 
	}

	public static boolean CHANCE16I(int a, int b, int r) 
	{ 
		//return Integer.compareUnsigned( v, (65536 * a) / b ) <= 0; 
		//final double v = (Integer.MAX_VALUE * (double)a) / b;
		final int v = (int)( (Integer.MAX_VALUE*2L * (double)a) / b );
		return Integer.compareUnsigned( r, v ) <= 0; 
	}


	/*
	#define for_each_bit(_i,_b)										\
		for(_i=0; _b!=0; _i++,_b>>=1)								\
			if (_b&1)
	 */
	//public static  int myabs(int a) { if (a<0) a = -a; return a; }
	//public static  long myabs64(long a) { if (a<0) a = -a; return a; }


	/*
	static  int intxchg_(IntContainer a, int b) { int t = a.v; a.v = b; return t; }
	static void intswap(a,b) ((b) = intxchg_(&(a), (b)))
	static  int uintxchg_(uint *a, uint b) { uint t = *a; *a = b; return t; }
	static void uintswap(a,b) ((b) = uintxchg_(&(a), (b)))


	/*
	static  void swap_byte(byte *a, byte *b) { byte t = *a; *a = *b; *b = t; }
	static  void swap_uint16(uint16 *a, uint16 *b) { uint16 t = *a; *a = *b; *b = t; }
	static  void swap_int16(int16 *a, int16 *b) { int16 t = *a; *a = *b; *b = t; }
	static  void swap_uint(uint *a, uint *b) { uint t = *a; *a = *b; *b = t; }
	static  void swap_int(int *a, int *b) { int t = *a; *a = *b; *b = t; }
	static  void swap_tile(TileIndex *a, TileIndex *b) { TileIndex t = *a; *a = *b; *b = t; }
	 */

	/*
	#if defined(TTD_LITTLE_ENDIAN)
	#	define READ_LE_UINT16(b) (*(const uint16*)(b))
	#elif defined(TTD_BIG_ENDIAN)
		static  uint16 READ_LE_UINT16(const void *b) {
			return ((const byte*)b)[0] + (((const byte*)b)[1] << 8);
		}
	#endif
	 */

	/**
	 * Rotate x Left/Right by n (must be >= 0)
	 * Assumes a byte has 8 bits
	 */
	//static void ROL(x, n) ((x) << (n) | (x) >> (sizeof(x) * 8 - (n)))

	//static void ROR(x, n) ((x) >> (n) | (x) << (sizeof(x) * 8 - (n)))
	public static int ROR8(int x, int n)
	{
		return 0xFF & ((x >>> n) | (x << (1 * 8 - n)));		
	}

	public static int ROR16(int x, int n)
	{
		return 0xFFFF & ((x >>> n) | (x << (2 * 8 - n)));		
	}

	public static int ROR32(int x, int n)
	{
		return (x >>> n) | (x << (4 * 8 - n));		
	}

	/**
	 * Return the smallest multiple of n equal or greater than x
	 * @apiNote  n must be a power of 2
	 */
	static public int ALIGN(int data, int n)
	{ 
		return (data + n - 1) & ~(n - 1);
	}

	/* orig impl below
	public static int FindFirstBit(int a) {
		//return Integer.numberOfTrailingZeros(a)+1;
		return 32-Integer.numberOfLeadingZeros(a);
	} */

	public static int FindFirstBit(int value)
	{
		// This is much faster than the one that was before here.
		//  Created by Darkvater.. blame him if it is wrong ;)
		// Btw, the macro FINDFIRSTBIT is better to use when your value is
		//  not more than 128.
		byte i = 0;
		if(0!=(value & 0xffff0000)) { value >>= 16; i += 16; }
		if(0!=(value & 0x0000ff00)) { value >>= 8;  i += 8; }
		if(0!=(value & 0x000000f0)) { value >>= 4;  i += 4; }
		if(0!=(value & 0x0000000c)) { value >>= 2;  i += 2; }
		if(0!=(value & 0x00000002)) { i += 1; }
		return i;
	}
	
	
	public static int FindFirstBit(long a) {
		//return Integer.numberOfTrailingZeros(a)+1;
		//return 31-Long.numberOfLeadingZeros(a);
		return 63-Long.numberOfLeadingZeros(a);
	}

	public static int BIGMULUS(int a, int b, int shift) 
	{
		long prod = Math.abs( ((long)a) * ((long)b) );
		return (int) (prod >>> shift);
	}

	/**
	 * Only allow valid ascii-function codes. Filter special codes like BELL and
	 * so on [we need a special filter here later]
	 *
	 * @param bkey character to be checked
	 * @return true or false depending if the character is printable/valid or not */
	public static boolean IsValidAsciiChar(int bkey) 
	{
		int key = bkey & 0xFF; // unsigned
		return (key >= ' ' && key < 127) || (key >= 160 &&
				key != 0xAA && key != 0xAC && key != 0xAD && key != 0xAF &&
				key != 0xB5 && key != 0xB6 && key != 0xB7 && key != 0xB9);
	}

	public static boolean i2b(int i) {
		return i != 0;
	}

	public static int b2i(boolean b) {
		return b ? 1 : 0;
	}

	public static byte[] subArray(byte[] in, int start) {		
		return Arrays.copyOfRange(in, start, in.length);
	}

	public static int READ_LE_UINT16(byte[] b, int shift) 
	{
		int hi = b[1+shift];
		int lo = b[0+shift];
		return (0xFF & lo) + ((0xFF & hi) << 8);
	}

	public static int READ_LE_UINT32(byte[] b, int shift) 
	{
		int b3 = b[3+shift];
		int b2 = b[2+shift];
		int b1 = b[1+shift];
		int b0 = b[0+shift];
		return (0xFF & b0) + ((0xFF & b1) << 8) + ((0xFF & b2) << 16) + ((0xFF & b3) << 24);
	}

	public static String stringFromBytes(byte[] b, int start, int len) {
		//return new String( b, start, len, StandardCharsets.ISO_8859_1 );
		char [] ca = new char[len];
		
		// Verbatim!
		for( int i = 0; i < len; i++)
		{
			int c = b[i+start];
			c &= 0xFF;
			ca[i] = (char) c;
		}
		
		return new String( ca );
	}

	public static int uint16Wrap(int i) {
		return i & 0xFFFF;
	}

	public static void writeFixedString(DataOutputStream f, String s, int len) throws IOException 
	{
		
		int sl = s.length();
		for( int i = 0; i < len; i++ )
		{
			char c = i >= sl ? 0 : s.charAt(i);
			f.writeByte(c);
		}
		
	}

	
	
	
	public static void hexDump(byte[] array) {
		System.out.println(formatHexDump(array));
	}

	public static void HexDump(byte[] array, int offset, int length) {
		System.out.println(formatHexDump(array, offset, length));
	}

	public static String formatHexDump(byte[] array) {
		return formatHexDump(array, 0, array.length);
		}	
	public static String formatHexDump(byte[] array, int offset, int length) {
        final int width = 16;

        StringBuilder builder = new StringBuilder();

        for (int rowOffset = offset; rowOffset < offset + length; rowOffset += width) {
            builder.append(String.format("%06d:  ", rowOffset));

            for (int index = 0; index < width; index++) {
                if (rowOffset + index < array.length) {
                    builder.append(String.format("%02x ", array[rowOffset + index]));
                } else {
                    builder.append("   ");
                }
            }

            if (rowOffset < array.length) {
                int asciiWidth = Math.min(width, array.length - rowOffset);
                builder.append("  |  ");
                try {
                    builder.append(new String(array, rowOffset, asciiWidth, "UTF-8").replaceAll("\r\n", " ").replaceAll("\n", " "));
                } catch (UnsupportedEncodingException ignored) {
                    //If UTF-8 isn't available as an encoding then what can we do?!
                }
            }

            builder.append(String.format("%n"));
        }

        return builder.toString();
    }

	public static int inc_sat_RET(int p) { // TODO  test
		int b = (p + 1); 
		if (b != 0) p = b; 
		return p; 
	}

	public static byte byte_inc_sat_RET(byte p) { // TODO test
		byte b = (byte) (p + 1); 
		if (b != 0) p = b; 
		return p; 
	}	
	
	/*
	static  int BIGMULSS(int a, int b, int shift) {
		return (int)(((long)(a) * (long)(b)) >> (shift));
	}

	static  long BIGMULSS64(long a, long b, int shift) {
		return ((a) * (b)) >> (shift);
	}


	static  long BIGMULS(int a, int b) {
		return (int)(((long)(a) * (long)(b)));
	}	
	 */
}
