package game;

import java.util.Iterator;
import java.util.function.Consumer;

public class RoadStop implements IPoolItem
{

	TileIndex xy;
	boolean used;
	byte status;
	int index;
	int slot[];// = new int[NUM_SLOTS];
	//StationID station;
	int station;
	int type;
	RoadStop next;
	RoadStop prev;

	public static final int INVALID_STATION = -1; //0xFFFF;
	public static final int INVALID_SLOT = -1; //0xFFFF;
	public static final int NUM_SLOTS = 2;
	public static final int ROAD_STOP_LIMIT = 8;

	
	private void clear() {
		xy = null;
		used = false;
		status = 0;
		index = 0;
		slot = new int[NUM_SLOTS];
		station = 0;
		type = 0;
		prev = next = null;
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
		prev = next = null;
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
		return _roadstop_pool.pool.values().iterator();
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
	static void InitializeRoadStop(RoadStop road_stop, RoadStop previous, TileIndex tile, int sindex)
	{
		road_stop.xy = tile;
		road_stop.used = true;
		road_stop.status = 3; //stop is free
		road_stop.slot[0] = road_stop.slot[1] = INVALID_SLOT;
		road_stop.next = null;
		road_stop.prev = previous;
		road_stop.station = sindex;
	}

	static RoadStop GetPrimaryRoadStop(final Station st, RoadStopType type)
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
		RoadStop rs;

		for (rs = GetPrimaryRoadStop(st, type); rs.xy != tile; rs = rs.next) {
			assert(rs.next != null);
		}

		return rs;
	}

	static int GetNumRoadStops(final Station st, RoadStopType type)
	{
		int num = 0;
		RoadStop rs;

		assert(st != null);
		for (rs = GetPrimaryRoadStop(st, type); rs != null; rs = rs.next) num++;

		return num;
	}

	static RoadStop AllocateRoadStop()
	{
		RoadStop[] ret = { null };
	
		//FOR_ALL_ROADSTOPS(rs) 
		_roadstop_pool.forEach( (Integer i, RoadStop rs) ->
		{
			if (!rs.used) {
				int index = rs.index;

				//memset(rs, 0, sizeof(*rs));
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

	
}

enum RoadStopType {
	RS_BUS,
	RS_TRUCK
} 

