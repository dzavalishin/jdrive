package game;

import game.exceptions.InvalidSpriteFormat;
import game.util.BitOps;

public class SingleSprite {

	private boolean hasRGB;
	private boolean hasAlpha;
	private boolean hasPalette;
	private boolean hasTransparency;
	private boolean exactSize;

	public SingleSprite(int type, byte[] spriteData) throws InvalidSpriteFormat 
	{
		if( type == 0xFF )
		{
			//Global.error("Non-sprite in SingleSprite c'tor");
			//return;
			throw new InvalidSpriteFormat("Non-sprite in SingleSprite c'tor");
		}

		hasRGB			= BitOps.HASBIT(type, 0);
		hasAlpha		= BitOps.HASBIT(type, 1);
		hasPalette		= BitOps.HASBIT(type, 2);
		hasTransparency = BitOps.HASBIT(type, 3);
		exactSize		= BitOps.HASBIT(type, 6);
	}

}
