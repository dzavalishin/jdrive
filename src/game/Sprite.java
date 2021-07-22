package game;

import game.util.Sprites;

public class Sprite extends Sprites {

	byte info;
	byte height;
	int width;
	int x_offs;
	int y_offs;
	byte data[];

	public Sprite(int dataSize) 
	{
		info = height = 0;
		width = x_offs = y_offs = 0;
		data = new byte[dataSize];	
	}

	public Sprite() 
	{
		info = height = 0;
		width = x_offs = y_offs = 0;
		data = null;	
	}
	
	// It is natural to have it here
	public static Sprite GetSprite(SpriteID sprite)
	{
		return SpriteCache.GetSprite(sprite);
	}
}

// User should decide by object type
class DataCarrier extends Sprite
{
	public DataCarrier(byte [] data) {
		super();
		this.data = data;
		this.info = (byte) 0xFF;
	}
}