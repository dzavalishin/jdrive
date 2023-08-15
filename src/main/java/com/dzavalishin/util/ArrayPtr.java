package com.dzavalishin.util;

import java.util.Arrays;

/**
 * Smart pointer to any array
 * 
 * Maintains displacement from memory start.
 * 
 * @author dz
 *
 */

public class ArrayPtr<ItemType> implements IArrayPtr 
{

	private final ItemType [] mem; // real mem
	private int displ;		// current displacement

	// --------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------

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
	
	// --------------------------------------------------------------
	// Read/write
	// --------------------------------------------------------------
	

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



	// --------------------------------------------------------------
	// Special write
	// --------------------------------------------------------------

	public void copyFrom(ArrayPtr<ItemType> src, int num) 
	{
		System.arraycopy(src.mem, src.displ, mem, displ, num);		
	}

	public void setRange(ItemType value, int right) {
		for( int i = displ; i < displ+right; i++ )
			mem[i] = value;		
	}


	// --------------------------------------------------------------
	// Static converters
	// --------------------------------------------------------------

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

	// --------------------------------------------------------------
	// Move position
	// --------------------------------------------------------------
	
	public void madd( int add ) { displ += add; }
	public void shift( int add ) { displ += add; }

	@Override
	public void inc() { displ++; }

	@Override
	public void dec() { displ--; }

	
	// --------------------------------------------------------------
	// Getters
	// --------------------------------------------------------------

	@Override
	public int getDisplacement() 	{		return displ;	}

	@Override
	public int getPos() 			{		return displ;	}

	public ItemType[] getMem() 		{		return mem;		}

	// --------------------------------------------------------------
	// Setters
	// --------------------------------------------------------------
	
	@Override
	public void setPos(int pos) { displ = pos; }

	// --------------------------------------------------------------
	// State
	// --------------------------------------------------------------

	@Override
	public boolean hasCurrent() {			
		return displ >= 0 && displ < mem.length;
	}

	public boolean inside(ArrayPtr<ItemType> from, ArrayPtr<ItemType> to) {
		assert( mem == from.mem );
		assert( mem == to.mem );		

		return displ >= from.displ && displ <= to.displ;
	}



}
