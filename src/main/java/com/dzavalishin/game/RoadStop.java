package com.dzavalishin.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.dzavalishin.enums.RoadStopType;
import com.dzavalishin.ifaces.IPoolItem;
import com.dzavalishin.ifaces.IPoolItemFactory;


public class RoadStop implements IPoolItem, Serializable
{

	private static final long serialVersionUID = 1L;
	
	TileIndex	xy;
	boolean		used;
	byte		status;
	int			index;
	int			slot[];
	int			station;
	int			type;

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
	}

	public RoadStop() {
		clear();
	}

	public RoadStop( RoadStop src ) {
		clear();
		xy = src.xy;
		used = false;
		status = src.status;
		index = 0;
		slot = new int[NUM_SLOTS];
		station = src.station;
		type = src.type;
	}
	
	@Override
	public void setIndex(int index) {
		this.index = index;		
	}
	
	@Override
	public boolean isValid() { return used; }
	
	static final IPoolItemFactory<RoadStop> factory = new IPoolItemFactory<RoadStop>() 
	{
		private static final long serialVersionUID = 1L;

		@Override
		public RoadStop createObject() {
			return new RoadStop();
		}
	};
	
	public static Iterator<RoadStop> getIterator()
	{
		return Global.gs._roadstops.getIterator(); //pool.values().iterator();
	}

	public static void forEach( Consumer<RoadStop> c )
	{
		Global.gs._roadstops.forEach(c);
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
			if(rs.xy.equals(tile))
				return rs;
		}

		return null;
	}

	static int GetNumRoadStops(final Station st, RoadStopType type)
	{
		assert(st != null);
		return GetPrimaryRoadStop(st, type).size();
	}

	static RoadStop AllocateRoadStop()
	{
		RoadStop[] ret = { null };
	
		//Global.gs._roadstops.forEach( (Integer i, RoadStop rs) ->
		Global.gs._roadstops.forEach( rs ->
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
		if (Global.gs._roadstops.AddBlockToPool()) 
			return AllocateRoadStop();

		return null;
	}

	public static void loadGame(ObjectInputStream oin) throws ClassNotFoundException, IOException
	{
		//_roadstop_pool = (MemoryPool<RoadStop>) oin.readObject();
	}

	public static void saveGame(ObjectOutputStream oos) throws IOException 
	{
		//oos.writeObject(_roadstop_pool);		
	}
	
}


