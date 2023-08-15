package com.dzavalishin.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.dzavalishin.exceptions.InvalidSpriteFormat;

/**
 * 
 * Stores multiple sprites with identical ID.
 * Supposedly with different resolution/bpp.
 * 
 * @author dz
 *
 */
public class MultiSprite {

	private int id;
	private List<SingleSprite> list = new ArrayList<>();
	private Map<Integer,SingleSprite> zoom = new HashMap<>();
	
	public MultiSprite(int id) {
		this.id = id;
	}

	/**
	 * Load from file
	 * 
	 * @param type
	 * @param spriteData
	 * @throws InvalidSpriteFormat 
	 */
	public void load(int type, byte[] spriteData) throws InvalidSpriteFormat {
		if( type == 0xFF )
		{
			//Global.error("Non-sprite in sprite section, id %d", id);
			//return;
			throw new InvalidSpriteFormat(String.format("Non-sprite in sprite section, id %d", id));
 
		}
		
		SingleSprite ss = new SingleSprite(type, spriteData);
		list.add(ss);		
		zoom.put(ss.getZoomLevel(), ss);
	}

	SingleSprite get(int zoomLevel)
	{
		SingleSprite ss = zoom.get(zoomLevel);
		
		if(ss == null)
		{
			if(zoom.isEmpty())
				return null;
			ss = generateZoomLevel(zoomLevel);
			if(ss != null)
			{
				assert ss.getZoomLevel() == zoomLevel;
				zoom.put(ss.getZoomLevel(), ss);
			}
		}
		
		return ss;
	}

	private SingleSprite generateZoomLevel(int zoomLevel) {
		SingleSprite src = getLargest();
		
		assert src.getZoomLevel() != zoomLevel;
		
		return new SingleSprite( src, zoomLevel );
	}

	private SingleSprite getLargest() {
		int maxX = 0;
		SingleSprite best = null;
		
		for(SingleSprite ss : list)
		{
			if(ss.getxSize() > maxX)
			{
				maxX = ss.getxSize();
				best = ss;
			}
		}
		
		return best;
	}
	
}
