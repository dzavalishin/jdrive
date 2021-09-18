package game;

import game.exceptions.InvalidSpriteFormat;
import game.util.BitOps;

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
		
		// TODO store in map?
		
	}
	
}
