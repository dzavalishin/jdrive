package com.dzavalishin.util;

public interface IArrayPtr {


	/**
	 * Move current element pointer.
	 * @param add Amount to move. Can be negative.
	 */
	public void shift( int add );
	
	public void inc();
	public void dec();
	
	/**
	 * Current position is in buffer, can read/write next element.
	 * @return true if not out of bounds.
	 */
	public boolean hasCurrent();			
	
	/**
	 * Get current position.
	 * @return Position of pointer in base array.
	 */
	public int getPos();
	public int getDisplacement(); // same as above	

	/**
	 * Set current position.
	 * @param pos Position of pointer in base array.
	 */
	void setPos(int pos);
	
}
