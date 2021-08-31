package game.util;

import game.TileIndex;
import game.Vehicle;
import game.tables.Snd;

public class Sound {

	public static void TrainPlayLeaveStationSound(final Vehicle  v)
	{
	/*
		EngineID engtype = v.getEngine_type();
	
		switch (Engine.GetEngine(engtype).getRailtype()) {
		case RAILTYPE_RAIL:
			//SndPlayVehicleFx(sfx[RailVehInfo(engtype).engclass], v);
			break;
	
		case RAILTYPE_MONO:
			SndPlayVehicleFx(SND_47_MAGLEV_2, v);
			break;
	
		case RAILTYPE_MAGLEV:
			//SndPlayVehicleFx(SND_41_MAGLEV, v);
			break;
		}
		*/
	}

	public static void SndPlayTileFx(Snd snd, TileIndex tile) {
		// TODO Auto-generated method stub
		
	}

}
