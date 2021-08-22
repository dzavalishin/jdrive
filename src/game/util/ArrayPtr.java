package game.util;

import java.util.Arrays;

/**
 * Smart pointer to any array
 * 
 * Maintains displacement from memory start.
 * 
 * @author dz
 *
 */

public class ArrayPtr<ItemType> {


		private final ItemType [] mem; // real mem
		private int displ;		// current displacement
		
		public void madd( int add ) { displ += add; }
		public void shift( int add ) { displ += add; }

		public void inc() { displ++; }
		public void dec() { displ--; }
		
		public void write( int shift, ItemType b )
		{
			mem[ displ + shift ] = b;
		}
		
		/**
		 * Write a pixel to this video ptr
		 * @param shift from current ptr pos
		 * @param b data to write
		 */
		public void w( int shift, ItemType b )
		{
			write(shift,b);
		}

		/**
		 * Write ++ - write item at current position and increment pointer.
		 * @param v
		 */
		public void wpp( ItemType v ) 
		{
			write(0,v);
			displ++;			
		}
		
		public ItemType read( int shift )
		{
			return mem[ displ + shift ];
		}
		
		public ItemType r( int shift )
		{
			return read( shift );
		}

		public ItemType r() {
			return read(0);
		}

		/**
		 * read ++ - read item at current position and increment pointer.
		 */
		public ItemType rpp() {
			ItemType r = read(0);
			displ++;			
			return r;
		}
		
		
		public ArrayPtr( ItemType [] start ) {
			mem = start;
			displ = 0;
		}

		public ArrayPtr( ItemType [] start, int pos ) {
			mem = start;
			displ = pos;
		}

		public ArrayPtr( ArrayPtr<ItemType> p ) {
			mem = p.mem;
			displ = p.displ;
		}
		
		public ArrayPtr(ArrayPtr<ItemType> p, int shift) {
			mem = p.mem;
			displ = p.displ + shift;
		}

		/*
		public void wor(int shift, int b) 
		{
			mem[ displ + shift ] |= b;	
		}
		
		public void wand(int shift, int b) 
		{
			mem[ displ + shift ] &= b;	
		}
		*/
		

		
		/*
		public void WRITE_PIXELS(int val)
		{
			w(0, (byte) BitOps.GB(val,  0, 8) );
			w(1, (byte) BitOps.GB(val,  8, 8) );
			w(2, (byte) BitOps.GB(val, 16, 8) );
			w(3, (byte) BitOps.GB(val, 24, 8) );
		}

		/* need to use OR, otherwise we will overwrite the wrong pixels at the edges :( * /
		public void WRITE_PIXELS_OR(int val)
		{
			wor(0, BitOps.GB(val,  0, 8) );
			wor(1, BitOps.GB(val,  8, 8) );
			wor(2, BitOps.GB(val, 16, 8) );
			wor(3, BitOps.GB(val, 24, 8) );
		}
		*/
		
		public boolean inside(ArrayPtr<ItemType> from, ArrayPtr<ItemType> to) {
			assert( mem == from.mem );
			assert( mem == to.mem );		
			
			return displ >= from.displ && displ <= to.displ;
		}
		
		public void setRange(ItemType value, int right) {
			for( int i = displ; i < displ+right; i++ )
				mem[i] = value;		
		}
		
		
		public ItemType[] getMem() {
			return mem;
		}

		public void copyFrom(ArrayPtr<ItemType> src, int num) 
		{
			System.arraycopy(src.mem, src.displ, mem, displ, num);		
		}
		
		// TODO toLongArray 		Long[] la = (Long[]) Arrays.stream(gw.cost[0]).mapToObj( (lv) -> Long.valueOf(lv) ).toArray();

		
		public static Integer[] toIntegerArray(int [] iia) 
		{
			//Integer[] ia = (Integer[]) Arrays.stream(iia).mapToObj( (iv) -> Integer.valueOf(iv) ).toArray();
			Integer[] ia = new Integer[iia.length];
			for( int i = 0; i < iia.length; i++)
				ia[i] = iia[i];
			return ia;
		}
		public static Long[] toLongArray(long[] li) {
			return Arrays.stream(li).mapToObj( (lv) -> Long.valueOf(lv) ).toArray(Long[]::new);
		}
		
		public boolean hasCurrent() {			
			return displ >= 0 && displ < mem.length;
		}
		
		

}
