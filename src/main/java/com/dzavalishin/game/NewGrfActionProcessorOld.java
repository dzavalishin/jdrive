package com.dzavalishin.game;

public class NewGrfActionProcessorOld extends NewGrfActionProcessor 
{

	public NewGrfActionProcessorOld(int sprite_offset) {
		super(sprite_offset);
	}

	@Override
	protected void loadSprite(int spriteid, DataLoader bufp) 
	{
		// bufp is unused
		SpriteCache.LoadNextSprite(spriteid, GRFFile._file_index);
	}

}
