package game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import game.enums.RoadStopType;
import game.ifaces.IPoolItem;
import game.ifaces.IPoolItemFactory;
import game.util.MemoryPool;


public class RoadStop implements IPoolItem
{

	private static final long serialVersionUID = 1L;
	
	TileIndex	xy;
	boolean		used;
	byte		status;
	int			index;
	int			slot[];
	int			station;
	int			type;

	public static final int INVALID_STATION = -1; 
	public static final int INVALID_SLOT = -1; 
	public static final int NUM_SLOTS = Station.NUM_SLOTS;
	public static final int ROAD_STOP_LIMIT = 8;

	
	private void clear() {
		xy = null;
		used = false;
		status = 0;
		index = 0;
		slot = new int[NUM_SLOTS];
		station = 0;
		type = 0;
		//prev = next = null;
	}

	public RoadStop() {
		clear();
	}

	public RoadStop( RoadStop src ) {
		clear();
		xy = src.xy;
		used = false; // TODO check usage!
		status = src.status;
		index = 0;
		slot = new int[NUM_SLOTS];
		station = src.station;
		type = src.type;
		//prev = next = null;
	}
	
	@Override
	public void setIndex(int index) {
		this.index = index;		
	}
	
	
	private static IPoolItemFactory<RoadStop> factory = new IPoolItemFactory<RoadStop>() {		
		@Override
		public RoadStop createObject() {
			return new RoadStop();
		}
	};
	
	private static MemoryPool<RoadStop> _roadstop_pool = new MemoryPool<RoadStop>(factory);

	
	public static Iterator<RoadStop> getIterator()
	{
		return _roadstop_pool.getIterator(); //pool.values().iterator();
	}

	public static void forEach( Consumer<RoadStop> c )
	{
		_roadstop_pool.forEach(c);
	}

	
	
	
	
	static public RoadStopType GetRoadStopType(TileIndex tile)
	{
		return (tile.getMap().m5 < 0x47) ? RoadStopType.RS_TRUCK : RoadStopType.RS_BUS;
	}

	//static void InitializeRoadStop(RoadStop road_stop, RoadStop previous, TileIndex tile, StationID index)
	static void InitializeRoadStop(RoadStop road_stop, /*RoadStop previous,*/ TileIndex tile, int sindex)
	{
		road_stop.xy = tile;
		road_stop.used = true;
		road_stop.status = 3; //stop is free
		road_stop.slot[0] = road_stop.slot[1] = INVALID_SLOT;
		//road_stop.next = null;
		//road_stop.prev = previous;
		road_stop.station = sindex;
	}

	static List<RoadStop> GetPrimaryRoadStop(final Station st, RoadStopType type)
	{
		switch (type) {
			case RS_BUS:   return st.bus_stops;
			case RS_TRUCK: return st.truck_stops;
			default: assert false;//NOT_REACHED();
		}

		return null;
	}

	static RoadStop GetRoadStopByTile(TileIndex tile, RoadStopType type)
	{
		final Station st = Station.GetStation(tile.getMap().m2);
		
		for(RoadStop rs : GetPrimaryRoadStop(st, type))
		{
			if(rs.xy.getTile() == tile.getTile())
				return rs;
		}

		return null;
	}

	static int GetNumRoadStops(final Station st, RoadStopType type)
	{
		//int num = 0;
		//RoadStop rs;

		assert(st != null);
		//for (rs = GetPrimaryRoadStop(st, type); rs != null; rs = rs.next) num++;

		return GetPrimaryRoadStop(st, type).size();
	}

	static RoadStop AllocateRoadStop()
	{
		RoadStop[] ret = { null };
	
		_roadstop_pool.forEach( (Integer i, RoadStop rs) ->
		{
			if (!rs.used) {
				int index = rs.index;
				rs.clear();
				rs.index = index;

				ret[0] = rs;
			}
		});

		if( ret[0] != null ) return ret[0];
		
		// Check if we can add a block to the pool 
		if (_roadstop_pool.AddBlockToPool()) 
			return AllocateRoadStop();

		return null;
	}

	public static void loadGame(ObjectInputStream oin) throws ClassNotFoundException, IOException
	{
		_roadstop_pool = (MemoryPool<RoadStop>) oin.readObject();
	}

	public static void saveGame(ObjectOutputStream oos) throws IOException 
	{
		oos.writeObject(_roadstop_pool);		
	}
	
}


