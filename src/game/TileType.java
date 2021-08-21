package game;

import game.enums.TileTypes;

//@Deprecated
public class TileType {
	int type;
	
	public TileType(int t) {
		type = t;
	}
	
	public TileType(TileTypes t) {
		type = t.ordinal();
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof TileType) {
			TileType tt = (TileType) obj;
			return tt.type == type;
		}
		
		if (obj instanceof TileTypes) {
			TileTypes tts = (TileTypes) obj;
			return tts.ordinal() == type;			
		}
		
		return super.equals(obj);
	}
	// TODO comarison / equals
}
