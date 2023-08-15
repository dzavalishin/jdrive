package com.dzavalishin.struct;

import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Town;

public class LandInfoData {
	public Town town;
	public int costclear;
	public AcceptedCargo ac = new AcceptedCargo();
	public TileIndex tile;
	public TileDesc td = new TileDesc();
}
