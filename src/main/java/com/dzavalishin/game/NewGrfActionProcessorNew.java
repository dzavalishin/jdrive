package com.dzavalishin.game;

public class NewGrfActionProcessorNew extends NewGrfActionProcessor 
{

	public NewGrfActionProcessorNew(int sprite_offset) {
		super(sprite_offset);
	}

	@Override
	protected void loadSprite(int spriteid, DataLoader bufp) {
		// TODO [dz] In a new GRF file there's no data here, so we can do nothing?		
	}

}
