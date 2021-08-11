package game.struct;

import game.AcceptedCargo;
import game.TileDesc;
import game.TileIndex;
import game.Town;

public class LandInfoData {
	public Town town;
	public int costclear;
	public AcceptedCargo ac = new AcceptedCargo();
	public TileIndex tile;
	public TileDesc td = new TileDesc();
}
