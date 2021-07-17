package game.util;

import game.IntContainer;

// macros.h stuff
public class BitOps {

	/// Fetch n bits starting at bit s from x
	static public int GB(int x, int s, int n) { return (x >> s) & ((1 << n) - 1); }
	
	/// Set n bits starting at bit s in x to d
	static public void SB(IntContainer x, int s, int n, int d)
	{
		x.v = (x.v & ~(((1 << n) - 1) << s)) | (d << s);
	}
	
	/// Add i to the n bits starting at bit s in x
	static public void AB(IntContainer x, int s, int n, int i)
	{
		x.v = (x.v & ~(((1 << n) - 1) << s)) | ((x.v + (i << s)) & (((1 << n) - 1) << s));	
	}

	
	static public int min(int a, int b) { if (a <= b) return a; return b; }
	static public int max(int a, int b) { if (a >= b) return a; return b; }
	static public long max64(long a, long b) { if (a >= b) return a; return b; }
	
	//static  uint minu(uint a, uint b) { if (a <= b) return a; return b; }
	//static  uint maxu(uint a, uint b) { if (a >= b) return a; return b; }

	static public int clamp(int a, int min, int max)
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
	//static boolean IS_INSIDE_1D(x, base, size) ( (uint)((x) - (base)) < ((uint)(size)) )


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

	//static void PLAYER_SPRITE_COLOR(owner) ( (_player_colors[owner] + 0x307) << PALETTE_SPRITE_START)
	//static void SPRITE_PALETTE(x) ((x) | PALETTE_MODIFIER_COLOR)

	//extern const byte _ffb_64[128];
	
	/* Returns the position of the first bit that is not zero, counted from the
	 * left. Ie, 10110100 returns 2, 00000001 returns 0, etc. When x == 0 returns
	 * 0.
	 */
	//static void FIND_FIRST_BIT(x) _ffb_64[(x)]
	/* Returns x with the first bit that is not zero, counted from the left, set
	 * to zero. So, 10110100 returns 10110000, 00000001 returns 00000000, etc.
	 */
	//static void KILL_FIRST_BIT(x) _ffb_64[(x)+64]
	/*
	static  int FindFirstBit2x64(int value)
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

	
	static  int KillFirstBit2x64(int value)
	{
		if (GB(value, 0, 8) == 0) {
			return KILL_FIRST_BIT(GB(value, 8, 6)) << 8;
		} else {
			return value & (KILL_FIRST_BIT(GB(value, 0, 6)) | 0x3F00);
		}
	}
*/
	/* [min,max), strictly less than */
	//static void IS_BYTE_INSIDE(a,min,max) ((byte)((a)-(min)) < (byte)((max)-(min)))
	//static void IS_INT_INSIDE(a,min,max) ((uint)((a)-(min)) < (uint)((max)-(min)))


	//static void CHANCE16(a,b) ((uint16)Random() <= (uint16)((65536 * a) / b))
	//static void CHANCE16R(a,b,r) ((uint16)(r=Random()) <= (uint16)((65536 * a) / b))
	//static void CHANCE16I(a,b,v) ((uint16)(v) <= (uint16)((65536 * a) / b))


	/*
	#define for_each_bit(_i,_b)										\
		for(_i=0; _b!=0; _i++,_b>>=1)								\
			if (_b&1)
	*/
	

	/*
	static  int intxchg_(IntContainer a, int b) { int t = a.v; a.v = b; return t; }
	static void intswap(a,b) ((b) = intxchg_(&(a), (b)))
	static  int uintxchg_(uint *a, uint b) { uint t = *a; *a = b; return t; }
	static void uintswap(a,b) ((b) = uintxchg_(&(a), (b)))

	static  int myabs(int a) { if (a<0) a = -a; return a; }
	static  long myabs64(long a) { if (a<0) a = -a; return a; }

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
	 * ROtate x Left/Right by n (must be >= 0)
	 * @note Assumes a byte has 8 bits
	 */
	//static void ROL(x, n) ((x) << (n) | (x) >> (sizeof(x) * 8 - (n)))
	//static void ROR(x, n) ((x) >> (n) | (x) << (sizeof(x) * 8 - (n)))

	/**
	 * Return the smallest multiple of n equal or greater than x
	 * @note n must be a power of 2
	 */
	static public int ALIGN(int x, int n) 
	{ 
		return (x + n - 1) & ~(n - 1); 
	}

	
	
}
