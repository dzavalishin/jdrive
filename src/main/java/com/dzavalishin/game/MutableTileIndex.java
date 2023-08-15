package com.dzavalishin.game;

import com.dzavalishin.struct.TileIndexDiff;

/**
 * Can be modified
 * @author dz
 *
 */
public class MutableTileIndex extends TileIndex
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MutableTileIndex(TileIndex src)
	{
		super( src.tile );
	}

	/**
	 * Mutable sub - modifies self
	 * @param diff
	 * @return returns this
	 */
	public TileIndex msub(TileIndexDiff diff)
	{
		tile -= diff.diff;
		return this;
	}

	/**
	 * Mutable add - modifies self
	 * @param diff
	 * @return returns this
	 */
	public TileIndex madd(TileIndexDiff diff)
	{
		tile += diff.diff;
		return this;
	}

	/**
	 * Mutable sub - modifies self
	 * @param diff
	 * @return returns this
	 */
	public TileIndex msub(int diff)
	{
		tile -= diff;
		return this;
	}

	/**
	 * Mutable add - modifies self
	 * @param diff
	 * @return returns this
	 */
	public TileIndex madd(int diff)
	{
		tile += diff;
		return this;
	}
	
	/**
	 * Mutable add - modifies self with given x and y 
	 * @param x
	 * @param y
	 * @return returns modified TileIndex, original one is not changed
	 */
	public TileIndex madd(int x, int y)
	{
		tile += (y * Global.MapSizeX()) + x;
		return this;
	}
	
}
