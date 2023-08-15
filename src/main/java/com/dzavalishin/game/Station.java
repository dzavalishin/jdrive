package com.dzavalishin.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StationID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.RoadStopType;
import com.dzavalishin.enums.StationClassID;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.IPoolItem;
import com.dzavalishin.ifaces.IPoolItemFactory;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.DrawTileSeqStruct;
import com.dzavalishin.struct.DrawTileSprites;
import com.dzavalishin.struct.GoodsEntry;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.ProducedCargo;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.struct.TileIndexDiff;
import com.dzavalishin.struct.TileIndexDiffC;
import com.dzavalishin.tables.StationTables;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.ByteArrayPtr;
import com.dzavalishin.util.IntContainer;
import com.dzavalishin.util.VehicleQueue;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.StationGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class Station extends StationTables implements IPoolItem
{
	private static final long serialVersionUID = 1L;

	TileIndex xy;

	List<RoadStop> bus_stops; 
	List<RoadStop> truck_stops;

	TileIndex train_tile;
	TileIndex airport_tile;
	TileIndex dock_tile;
	public Town town;
	public int string_id;

	ViewportSign sign; // = new ViewportSign();

	int had_vehicle_of_type;

	int time_since_load;
	int time_since_unload;
	int delete_ctr;
	PlayerID owner;
	protected int facilities;
	int airport_type;

	// trainstation width/height
	int trainst_w, trainst_h;

	int class_id; // custom graphics station class
	int stat_id; // custom graphics station id in the @class_id class
	int build_date;

	private int airport_flags; // Airport tile blocks - forbid entering tile
	//StationID index;
	int index;
	VehicleQueue airport_queue;			// airport queue
	VehicleQueue helicopter_queue;			// airport queue

	VehicleID last_vehicle;
	public GoodsEntry goods[] = new GoodsEntry[AcceptedCargo.NUM_CARGO];




	private void clear()
	{
		xy= null;
		bus_stops   = new ArrayList<>();
		truck_stops = new ArrayList<>();
		train_tile=airport_tile=dock_tile = null;
		town = null;
		string_id = 0;

		sign = new ViewportSign();

		had_vehicle_of_type = 0;

		time_since_load = 0;
		time_since_unload = 0;
		delete_ctr = 0;
		owner= null;
		facilities = 0;
		airport_type = 0;

		// trainstation width/height
		trainst_w = trainst_h = 0;

		class_id = 0; // custom graphics station class
		stat_id = 0; // custom graphics station id in the @class_id class
		build_date = 0;

		// airport_flags;
		airport_flags = 0;
		//StationID index;
		index = 0;
		airport_queue= null;			// airport queue
		helicopter_queue= null;			// airport queue

		last_vehicle= null;
		goods = new GoodsEntry[AcceptedCargo.NUM_CARGO];
	}


	private Station() {
		clear();
	}
	

	public TileIndex getXy() {
		return xy;
	}



	// Determines what station to operate on in the
	//  tick handler.
	public static int _station_tick_ctr = 0;



	private void StationInitialize(TileIndex tile)
	{
		// GoodsEntry ge;

		xy = tile;
		airport_tile = dock_tile = train_tile = null;
		bus_stops = new ArrayList<>(); 
		truck_stops = new ArrayList<>();
		had_vehicle_of_type = 0;
		time_since_load = 255;
		time_since_unload = 255;
		delete_ctr = 0;
		facilities = 0;

		last_vehicle = VehicleID.getInvalid();

		//for (ge = goods; ge != endof(goods); ge++)
		for( int i = 0; i < goods.length; i++)
			goods[i] = new GoodsEntry();
		
		for(GoodsEntry ge : goods)
		{
			ge.waiting_acceptance = 0;
			ge.days_since_pickup = 0;
			ge.enroute_from = INVALID_STATION;
			ge.rating = 175;
			ge.last_speed = 0;
			ge.last_age = 0xFF;
			ge.feeder_profit = 0;
		}

		airport_queue = VehicleQueue.new_VQueue();
		helicopter_queue = VehicleQueue.new_VQueue();

		StationGui.requestSortStations(); // build a new station
	}

	// Update the virtual coords needed to draw the station sign.
	// st = Station to update for.
	private void UpdateStationVirtCoord()
	{
		Point pt = Point.RemapCoords2(xy.TileX() * 16, xy.TileY() * 16);

		pt.y -= 32;
		if ( (0 != (facilities & FACIL_AIRPORT)) && airport_type == Airport.AT_OILRIG) pt.y -= 16;

		Global.SetDParam(0, index);
		Global.SetDParam(1, facilities);
		ViewPort.UpdateViewportSignPos(sign, pt.x, pt.y, Str.STR_305C_0);
	}


	// Update the virtual coords needed to draw the station sign for all stations.
	static void UpdateAllStationVirtCoord()
	{
		Global.gs._stations.forEachValid( st -> st.UpdateStationVirtCoord() );
	}

	// Update the station virt coords while making the modified parts dirty.
	private void UpdateStationVirtCoordDirty()
	{
		MarkStationDirty();
		UpdateStationVirtCoord();
		MarkStationDirty();
	}



	// Get a mask of the cargo types that the station accepts.
	private int GetAcceptanceMask()
	{
		int mask = 0;
		int i;

		for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
			if( goods[i] != null && 0 != (goods[i].waiting_acceptance & 0x8000) ) 
				mask |= 1 << i;
		}
		return mask;
	}

	// Items contains the two cargo names that are to be accepted or rejected.
	// msg is the string id of the message to display.
	private void ShowRejectOrAcceptNews(int items, StringID msg)
	{
		if (items > 0) {
			Global.SetDParam(2, BitOps.GB(items, 16, 16));
			Global.SetDParam(1, BitOps.GB(items,  0, 16));
			Global.SetDParam(0, index);
			NewsItem.AddNewsItem(msg.id + (((items >> 16) != 0 )?1:0), NewsItem.NEWS_FLAGS(NewsItem.NM_SMALL, NewsItem.NF_VIEWPORT|NewsItem.NF_TILE, NewsItem.NT_ACCEPTANCE, 0), xy.tile, 0);
		}
	}



	// Get a list of the cargo types being produced around the tile.
	static void GetProductionAroundTiles(AcceptedCargo produced, TileIndex tile0,
			int w, int h, int rad)
	{
		int x,y;
		int x1,y1,x2,y2;
		int xc,yc;

		//memset(produced, 0, sizeof(AcceptedCargo));
		produced.clear();

		x = tile0.TileX();
		y = tile0.TileY();

		// expand the region by rad tiles on each side
		// while making sure that we remain inside the board.
		x2 = Integer.min(x + w + rad, Global.MapSizeX());
		x1 = Integer.max(x - rad, 0);

		y2 = Integer.min(y + h + rad, Global.MapSizeY());
		y1 = Integer.max(y - rad, 0);

		assert(x1 < x2);
		assert(y1 < y2);
		assert(w > 0);
		assert(h > 0);

		for (yc = y1; yc != y2; yc++) {
			for (xc = x1; xc != x2; xc++) {
				if (!(BitOps.IS_INSIDE_1D(xc, x, w) && BitOps.IS_INSIDE_1D(yc, y, h))) {
					//GetProducedCargoProc gpc;
					TileIndex tile1 = new TileIndex(xc, yc);

					/*
				gpc = _tile_type_procs[GetTileType(tile1)].get_produced_cargo_proc;
				if (gpc != null) {
					byte cargos[2] = { AcceptedCargo.CT_INVALID, AcceptedCargo.CT_INVALID };

					gpc(tile1, cargos);
					 */
					{
						//int cargos[] = { AcceptedCargo.CT_INVALID, AcceptedCargo.CT_INVALID };
						ProducedCargo cargos = Landscape._tile_type_procs[tile1.GetTileType().ordinal()].get_produced_cargo_proc.apply(tile1 ); 
						if (cargos.cargo[0] != AcceptedCargo.CT_INVALID) {
							produced.ct[cargos.cargo[0]]++;
							if (cargos.cargo[1] != AcceptedCargo.CT_INVALID) {
								produced.ct[cargos.cargo[1]]++;
							}
						}
					}
				}
			}
		}
	}

	// Get a list of the cargo types that are accepted around the tile.
	public static void GetAcceptanceAroundTiles(AcceptedCargo accepts, TileIndex tile0,
			int w, int h, int rad)
	{
		int x,y;
		int x1,y1,x2,y2;
		int xc,yc;

		//memset(accepts, 0, sizeof(AcceptedCargo));
		accepts.clear();

		x = tile0.TileX();
		y = tile0.TileY();

		// expand the region by rad tiles on each side
		// while making sure that we remain inside the board.
		x2 = Integer.min(x + w + rad, Global.MapSizeX());
		y2 = Integer.min(y + h + rad, Global.MapSizeY());
		x1 = Integer.max(x - rad, 0);
		y1 = Integer.max(y - rad, 0);

		assert(x1 < x2);
		assert(y1 < y2);
		assert(w > 0);
		assert(h > 0);

		for (yc = y1; yc != y2; yc++) {
			for (xc = x1; xc != x2; xc++) {
				TileIndex tile1 = new TileIndex(xc, yc);

				if (!tile1.IsTileType(TileTypes.MP_STATION)) {
					AcceptedCargo ac;
					int i;

					ac = Landscape.GetAcceptedCargo(tile1);
					if( ac != null)
					{
						for (i = 0; i < ac.ct.length; ++i) 
							accepts.ct[i] += ac.ct[i];
					}
				}
			}
		}
	}





	// Update the acceptance for a station.
	// show_msg controls whether to display a message that acceptance was changed.
	private void UpdateStationAcceptance(boolean show_msg)
	{
		int old_acc, new_acc;
		//RoadStop cur_rs;
		int i;
		ottd_Rectangle rect = new ottd_Rectangle();
		int rad;
		AcceptedCargo accepts = new AcceptedCargo();

		rect.min_x = Global.MapSizeX();
		rect.min_y = Global.MapSizeY();
		rect.max_x = rect.max_y = 0;
		// Don't update acceptance for a buoy
		if (IsBuoy()) return;

		/* old accepted goods types */
		old_acc = GetAcceptanceMask();

		// Put all the tiles that span an area in the table.
		if (train_tile != null) {
			rect.MergePoint(train_tile);
			rect.MergePoint(
					train_tile.iadd(trainst_w - 1, trainst_h - 1)
					);
		}

		if (airport_tile != null) {
			rect.MergePoint( airport_tile);
			rect.MergePoint(
					airport_tile.iadd(
							_airport_size_x[airport_type] - 1,
							_airport_size_y[airport_type] - 1
							)
					);
		}

		if (dock_tile != null) rect.MergePoint( dock_tile);

		//for (cur_rs = bus_stops; cur_rs != null; cur_rs = cur_rs.next) {
		for (RoadStop cur_rs : bus_stops) {
			rect.MergePoint( cur_rs.xy);
		}

		//for (cur_rs = truck_stops; cur_rs != null; cur_rs = cur_rs.next) {
		for (RoadStop cur_rs : truck_stops) {
			rect.MergePoint( cur_rs.xy);
		}

		rad = (Global._patches.modified_catchment) ? FindCatchmentRadius() : 4;

		// And retrieve the acceptance.
		if (rect.max_x >= rect.min_x) {
			GetAcceptanceAroundTiles(
					accepts,
					new TileIndex(rect.min_x, rect.min_y),
					rect.max_x - rect.min_x + 1,
					rect.max_y - rect.min_y + 1,
					rad
					);
		} else {
			//memset(accepts, 0, sizeof(accepts));
			accepts.clear();
		}

		// Adjust in case our station only accepts fewer kinds of goods
		for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
			int amt = Integer.min(accepts.ct[i], 15);

			// Make sure the station can accept the goods type.
			if ((i != AcceptedCargo.CT_PASSENGERS && 0 ==(facilities & (byte)~FACIL_BUS_STOP)) ||
					(i == AcceptedCargo.CT_PASSENGERS && 0 == (facilities & (byte)~FACIL_TRUCK_STOP)))
				amt = 0;

			if(goods[i] == null) goods[i] = new GoodsEntry();
			goods[i].waiting_acceptance = BitOps.RETSB(goods[i].waiting_acceptance, 12, 4, amt);
		}

		// Only show a message in case the acceptance was actually changed.
		new_acc = GetAcceptanceMask();
		if (old_acc == new_acc)
			return;

		// show a message to report that the acceptance was changed?
		if (show_msg && owner.isLocalPlayer() && 0 != facilities) {
			int accept=0, reject=0; /* these contain two string ids each */
			final int[] str = Global._cargoc.names_s;

			int si = 0;
			do {
				if(0 != (new_acc & 1)) {
					if(0 ==(old_acc & 1)) accept = (accept << 16) | str[si];
				} else {
					if(0 !=(old_acc & 1)) reject = (reject << 16) | str[si];
				}
				si++;
			} while ((new_acc>>=1) != (old_acc>>=1));

			ShowRejectOrAcceptNews(accept, new StringID(Str.STR_3040_NOW_ACCEPTS));
			ShowRejectOrAcceptNews(reject, new StringID(Str.STR_303E_NO_LONGER_ACCEPTS));
		}

		// redraw the station view since acceptance changed
		Window.InvalidateWindowWidget(Window.WC_STATION_VIEW, index, 4);
	}

	// This is called right after a station was deleted.
	// It checks if the whole station is free of substations, and if so, the station will be
	// deleted after a little while.
	private void DeleteStationIfEmpty()
	{
		if (facilities == 0) {
			delete_ctr = 0;
			Window.InvalidateWindow(Window.WC_STATION_LIST, owner.id);
		}
	}

	static final IPoolItemFactory<Station> factory = new IPoolItemFactory<Station>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Station createObject() {
			return new Station();
		}
	};
	
	@Override
	public void setIndex(int index) {
		this.index = index;
	}


	/**
	 * Get the pointer to the station with index 'index'
	 */
	public static Station GetStation(int index)
	{
		return Global.gs._stations.GetItemFromPool(index);
	}

	/**
	 * Get the current size of the StationPool
	 */
	protected static int GetStationPoolSize()
	{
		return Global.gs._stations.total_items();
	}

	static boolean IsStationIndex(int index)
	{
		return index < GetStationPoolSize();
	}



	private void MarkStationDirty()
	{
		if (sign.getWidth_1() != 0) {
			Window.InvalidateWindowWidget(Window.WC_STATION_VIEW, index, 1);

			ViewPort.MarkAllViewportsDirty(
					sign.getLeft() - 6,
					sign.getTop(),
					sign.getLeft() + (sign.getWidth_1() << 2) + 12,
					sign.getTop() + 48);
		}
	}

	/**
	 *  Calculate the radius of the station. Basically it is the biggest
	 *  radius that is available within the Station 
	 *  
	 */
	private int FindCatchmentRadius()
	{
		int ret = 0;

		if (!bus_stops.isEmpty())   ret = Math.max(ret, CA_BUS);
		if (!truck_stops.isEmpty()) ret = Math.max(ret, CA_TRUCK);
		if (train_tile != null) ret = Math.max(ret, CA_TRAIN);
		if (dock_tile != null)  ret = Math.max(ret, CA_DOCK);

		if (airport_tile != null) {
			switch (airport_type) {
			case Airport.AT_OILRIG:        ret = Math.max(ret, CA_AIR_OILPAD);   break;
			case Airport.AT_SMALL:         ret = Math.max(ret, CA_AIR_SMALL);    break;
			case Airport.AT_HELIPORT:      ret = Math.max(ret, CA_AIR_HELIPORT); break;
			case Airport.AT_LARGE:         ret = Math.max(ret, CA_AIR_LARGE);    break;
			case Airport.AT_METROPOLITAN:  ret = Math.max(ret, CA_AIR_METRO);    break;
			case Airport.AT_INTERNATIONAL: ret = Math.max(ret, CA_AIR_INTER);    break;
			}
		}

		return ret;
	}


	private static Station  GetStationAround(TileIndex tile, int w, int h, /*StationID*/ int closest_station_i, boolean [] canBuild)
	{
		int [] closest_station = {closest_station_i};
		if(canBuild!= null) canBuild[0] = true;
		// check around to see if there's any stations there
		//BEGIN_TILE_LOOP(tile_cur, w + 2, h + 2, tile - TileDiffXY(1, 1))
		TileIndex.forAll(w + 2, h + 2, tile.isub( TileIndex.TileDiffXY(1, 1) ).getTile(), (tile_cur) -> {
			if (tile_cur.IsTileType(TileTypes.MP_STATION)) {
				/*StationID*/ int t = tile_cur.getMap().m2;
				{
					Station st = GetStation(t);
					// you cannot take control of an oilrig!!
					if (st.airport_type == Airport.AT_OILRIG && st.facilities == (FACIL_AIRPORT|FACIL_DOCK))
						//continue;
						return false;
				}

				if (closest_station[0] == INVALID_STATION) {
					closest_station[0] = t;
				} else if (closest_station[0] != t) {
					Global._error_message = Str.STR_3006_ADJOINS_MORE_THAN_ONE_EXISTING;
					if(canBuild!= null) canBuild[0] = false;
					closest_station[0] = INVALID_STATION;
					return true; // break loop
				}
			}
			return false;
		});
		//END_TILE_LOOP(tile_cur, w + 2, h + 2, tile - TileDiffXY(1, 1))
		return (closest_station[0] == INVALID_STATION) ? null : GetStation(closest_station[0]);
	}

	public static TileIndex GetStationTileForVehicle(final  Vehicle v, final  Station st)
	{
		switch (v.type) {
		case Vehicle.VEH_Train: 		return st.train_tile;
		case Vehicle.VEH_Aircraft:	return st.airport_tile;
		case Vehicle.VEH_Ship:			return st.dock_tile;
		case Vehicle.VEH_Road:
			if (v.getCargo_type() == AcceptedCargo.CT_PASSENGERS) {
				return (st.bus_stops != null) ? st.bus_stops.get(0).xy : null;
			} else {
				return (st.truck_stops != null) ? st.truck_stops.get(0).xy : null;
			}
		default:
			assert(false);
			return TileIndex.INVALID_TILE;
		}
	}

	private boolean CheckStationSpreadOut(TileIndex tile, int w, int h)
	{
		StationID station_index = StationID.get(index);
		int i;
		int x1 = tile.TileX();
		int y1 = tile.TileY();
		int x2 = x1 + w - 1;
		int y2 = y1 + h - 1;
		int t;

		for (i = 0; i != Global.MapSize(); i++) 
		{
			TileIndex ii = new TileIndex(i);
			if (ii.IsTileType(TileTypes.MP_STATION) && ii.getMap().m2 == station_index.id) {
				t = ii.TileX();
				if (t < x1) x1 = t;
				if (t > x2) x2 = t;

				t = ii.TileY();
				if (t < y1) y1 = t;
				if (t > y2) y2 = t;
			}
		}

		if (y2 - y1 >= Global._patches.station_spread || x2 - x1 >= Global._patches.station_spread) {
			Global._error_message = Str.STR_306C_STATION_TOO_SPREAD_OUT;
			return false;
		}

		return true;
	}

	public static Station AllocateStation() {
		Iterator<Station> iterator = Global.gs._stations.getIterator();
		while(iterator.hasNext() ) {
			Station station = iterator.next();
			if (!station.isValid()) {
				StationID index = StationID.get(station.index);

				station.clear();
				station.index = index.id;

				return station;
			}
		}

		/* Check if we can add a block to the pool */
		if (Global.gs._stations.AddBlockToPool()) {
			return AllocateStation();
		} else {
			Global._error_message = Str.STR_3008_TOO_MANY_STATIONS_LOADING;
			return null;
		}
	}



	private static int CountMapSquareAround(TileIndex tile, TileTypes type, int min, int max)
	{
		int num = 0;

		for(TileIndexDiffC p : _count_square_table) 
		{
			tile = tile.iadd( TileIndex.ToTileIndexDiff(p) );
			tile.TILE_MASK();

			if (tile.IsTileType(type) && tile.getMap().m5 >= min && tile.getMap().m5 <= max)
				num++;
		}

		return num;
	}

	private static boolean GenerateStationName(Station st, TileIndex tile, int flag)
	{

		Town t = st.town;
		int [] free_names = {-1};
		int found;
		int z,z2;
		long tmp;

		Global.gs._stations.forEach( s ->			
		{
			if (s != st && s.isValid() && s.town==t) {
				int str = M(s.string_id);
				if (str <= 0x20) {
					if (str == M(Str.STR_SV_STNAME_FOREST))
						str = M(Str.STR_SV_STNAME_WOODS);
					free_names[0] = BitOps.RETCLRBIT(free_names[0], str);
				}
			}
		});


		/* check default names */
		tmp = free_names[0] & _gen_station_name_bits[flag];
		if (tmp != 0) {
			found = BitOps.FindFirstBit(tmp);
			//goto done;
			st.string_id = found + Str.STR_SV_STNAME;
			return true;
		}

		/* check mine? */
		if (BitOps.HASBIT(free_names[0], M(Str.STR_SV_STNAME_MINES))) {
			if (CountMapSquareAround(tile, TileTypes.MP_INDUSTRY, 0, 6) >= 2 ||
					CountMapSquareAround(tile, TileTypes.MP_INDUSTRY, 0x64, 0x73) >= 2 ||
					CountMapSquareAround(tile, TileTypes.MP_INDUSTRY, 0x2F, 0x33) >= 2 ||
					CountMapSquareAround(tile, TileTypes.MP_INDUSTRY, 0x48, 0x58) >= 2 ||
					CountMapSquareAround(tile, TileTypes.MP_INDUSTRY, 0x5B, 0x63) >= 2) {
				found = M(Str.STR_SV_STNAME_MINES);
				//goto done;
				st.string_id = found + Str.STR_SV_STNAME;
				return true;
			}
		}

		/* check close enough to town to get central as name? */
		if (Map.DistanceMax(tile,t.getXy()) < 8) {
			found = M(Str.STR_SV_STNAME);
			if (BitOps.HASBIT(free_names[0], M(Str.STR_SV_STNAME)))
			{
				//goto done;
				st.string_id = found + Str.STR_SV_STNAME;
				return true;
			}

			found = M(Str.STR_SV_STNAME_CENTRAL);
			if (BitOps.HASBIT(free_names[0], M(Str.STR_SV_STNAME_CENTRAL)))
			{
				//goto done;
				st.string_id = found + Str.STR_SV_STNAME;
				return true;
			}
		}

		/* Check lakeside */
		if (BitOps.HASBIT(free_names[0], M(Str.STR_SV_STNAME_LAKESIDE)) &&
				Map.DistanceFromEdge(tile) < 20 &&
				CountMapSquareAround(tile, TileTypes.MP_WATER, 0, 0) >= 5) {
			found = M(Str.STR_SV_STNAME_LAKESIDE);
			//goto done;
			st.string_id = found + Str.STR_SV_STNAME;
			return true;
		}

		/* Check woods */
		if (BitOps.HASBIT(free_names[0], M(Str.STR_SV_STNAME_WOODS)) && (
				CountMapSquareAround(tile, TileTypes.MP_TREES, 0, 255) >= 8 ||
				CountMapSquareAround(tile, TileTypes.MP_INDUSTRY, 0x10, 0x11) >= 2)
				) {
			found = GameOptions._opt.landscape == Landscape.LT_DESERT ?
					M(Str.STR_SV_STNAME_FOREST) : M(Str.STR_SV_STNAME_WOODS);
			//goto done;
			st.string_id = found + Str.STR_SV_STNAME;
			return true;
		}

		/* check elevation compared to Town */
		z = tile.GetTileZ();
		z2 = t.getXy().GetTileZ();
		if (z < z2) {
			found = M(Str.STR_SV_STNAME_VALLEY);
			if (BitOps.HASBIT(free_names[0], M(Str.STR_SV_STNAME_VALLEY))) 
			{
				//goto done;
				st.string_id = found + Str.STR_SV_STNAME;
				return true;
			}
		} else if (z > z2) {
			found = M(Str.STR_SV_STNAME_HEIGHTS);
			if (BitOps.HASBIT(free_names[0], M(Str.STR_SV_STNAME_HEIGHTS))) 
			{
				//goto done;
				st.string_id = found + Str.STR_SV_STNAME;
				return true;
			}
		}

		/* check direction compared to Town */
		{

			free_names[0] &= _direction_and_table[
			                                      ((tile.TileX() < t.getXy().TileX()) ? 1 : 0) +
			                                      (((tile.TileY() < t.getXy().TileY()) ? 1 : 0) * 2)];
		}

		tmp = free_names[0] & ((1<<1)|(1<<2)|(1<<3)|(1<<4)|(1<<6)|(1<<7)|(1<<12)|(1<<26)|(1<<27)|(1<<28)|(1<<29)|(1<<30));
		if (tmp == 0) {
			Global._error_message = Str.STR_3007_TOO_MANY_STATIONS_LOADING;
			return false;
		}
		found = BitOps.FindFirstBit(tmp);

		//done:
		st.string_id = found + Str.STR_SV_STNAME;
		return true;
	}
	//#undef M

	private static Station GetClosestStationFromTile(TileIndex tile, int threshold_i, PlayerID owner)
	{
		Station [] best_station = {null};
		int [] threshold = {threshold_i};

		Global.gs._stations.forEach( st ->			
		{
			if (st.isValid() && (owner.isSpectator() || st.owner.equals(owner))) {
				int cur_dist = Map.DistanceManhattan(tile, st.xy);

				if (cur_dist < threshold[0]) {
					threshold[0] = cur_dist;
					best_station[0] = st;
				}
			}
		});

		return best_station[0];
	}


	/**
	 * 
	 * Tries to clear the given area. Returns the cost in case of success.
	 * Or an error code if it failed.
	 * 
	 * @param tile
	 * @param w
	 * @param h
	 * @param flags
	 * @param invalid_dirs
	 * @param station - return something here? :)
	 * @return
	 */
	static public int CheckFlatLandBelow(TileIndex tile, int w, int h, int flags, int invalid_dirs, StationID[] station)
	{
		int [] cost = {0};
		int [] ret = {0};
		int [] allowed_z = {-1};
		int [] error = {0};

		//BEGIN_TILE_LOOP(tile_cur, w, h, tile)
		TileIndex.forEach(w, h, tile.getTile(), (tile_cur, h_cur, w_cur ) -> 
		{

			if (!Vehicle.EnsureNoVehicle(tile_cur))
			{
				error[0] = Cmd.CMD_ERROR;
				return true;
			}
			IntContainer zp = new IntContainer(); 
			int tileh = tile_cur.GetTileSlope(zp);
			int z = zp.v;

			/* Prohibit building if
				1) The tile is "steep" (i.e. stretches two height levels)
				-OR-
				2) The tile is non-flat if
					a) the player building is an "old-school" AI
					-OR-
					b) the build_on_slopes switch is disabled
			 */
			if (TileIndex.IsSteepTileh(tileh) ||
					((Global.gs._is_old_ai_player || !Global._patches.build_on_slopes) && tileh != 0)) {
				Global._error_message = Str.STR_0007_FLAT_LAND_REQUIRED;
				error[0] = Cmd.CMD_ERROR;
				return true;
			}

			int flat_z = z;
			if (tileh>0) {
				// need to check so the entrance to the station is not pointing at a slope.
				if (((invalid_dirs&1)!=0 && 0==(tileh & 0xC) && w_cur == w) ||
						((invalid_dirs&2)!=0 && 0==(tileh & 6) &&	h_cur == 1) ||
						((invalid_dirs&4)!=0 && 0==(tileh & 3) && w_cur == 1) ||
						((invalid_dirs&8)!=0 && 0==(tileh & 9) && h_cur == h)) {
					Global._error_message = Str.STR_0007_FLAT_LAND_REQUIRED;
					error[0] = Cmd.CMD_ERROR;
					return true;
				}
				cost[0] += Global._price.terraform;
				flat_z += 8;
			}

			// get corresponding flat level and make sure that all parts of the station have the same level.
			if (allowed_z[0] == -1) {
				// first tile
				allowed_z[0] = flat_z;
			} else if (allowed_z[0] != flat_z) {
				Global._error_message = Str.STR_0007_FLAT_LAND_REQUIRED;
				error[0] = Cmd.CMD_ERROR;
				return true;
			}

			// if station is set, then we have special handling to allow building on top of already existing stations.
			// so station points to INVALID_STATION if we can build on any station. or it points to a station if we're only allowed to build
			// on exactly that station.
			if (station != null && tile_cur.IsTileType(TileTypes.MP_STATION)) {
				if (tile_cur.getMap().m5 >= 8) {
					Global._error_message = ClearTile_Station(tile_cur, Cmd.DC_AUTO); // get error message
					error[0] = Cmd.CMD_ERROR;
					return true;
				} else {
					StationID st = StationID.get( tile_cur.getMap().m2 );
					if (station[0].id == INVALID_STATION) {
						station[0] = st;
					} else if(!station[0].equals(st)) {
						Global._error_message = Str.STR_3006_ADJOINS_MORE_THAN_ONE_EXISTING;
						error[0] = Cmd.CMD_ERROR;
						return true;
					}
				}
			} else {
				ret[0] = Cmd.DoCommandByTile(tile_cur, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
				if (Cmd.CmdFailed(ret[0]))
				{
					error[0] = Cmd.CMD_ERROR;
					return true;
				}
				cost[0] += ret[0];
			}

			return false;
		});
		//END_TILE_LOOP(tile_cur, w, h, tile)

		if(error[0] != 0) return error[0]; 

		return cost[0];

	}

	private static boolean CanExpandRailroadStation(Station st, int [] fin, int direction)
	{
		int curw = st.trainst_w, curh = st.trainst_h;
		MutableTileIndex tile = new MutableTileIndex( new TileIndex(fin[0]) );
		int w = fin[1];
		int h = fin[2];

		if (Global._patches.nonuniform_stations) {
			// determine new size of train station region..
			int x = Math.min(st.train_tile.TileX(), tile.TileX());
			int y = Math.min(st.train_tile.TileY(), tile.TileY());
			curw = Math.max(st.train_tile.TileX() + curw, tile.TileX() + w) - x;
			curh = Math.max(st.train_tile.TileY() + curh, tile.TileY() + h) - y;
			tile = new MutableTileIndex( TileIndex.TileXY(x, y) );
		} else {
			// check so the direction is the same
			if ((st.train_tile.getMap().m5 & 1) != direction) {
				Global._error_message = Str.STR_306D_NONUNIFORM_STATIONS_DISALLOWED;
				return false;
			}

			// check if the new station adjoins the old station in either direction
			if (curw == w && st.train_tile.equals( tile.iadd(0, h) ) ) {
				// above
				curh += h;
			} else if (curw == w && st.train_tile.equals( tile.iadd(0, -curh) ) ) {
				// below
				tile.madd(0, -curh);
				curh += h;
			} else if (curh == h && st.train_tile.equals( tile.iadd(w, 0) ) ) {
				// to the left
				curw += w;
			} else if (curh == h && st.train_tile.equals( tile.iadd(-curw, 0) ) ) {
				// to the right
				tile.madd(-curw, 0);
				curw += w;
			} else {
				Global._error_message = Str.STR_306D_NONUNIFORM_STATIONS_DISALLOWED;
				return false;
			}
		}
		// make sure the final size is not too big.
		if (curw > Global._patches.station_spread || curh > Global._patches.station_spread) {
			Global._error_message = Str.STR_306C_STATION_TOO_SPREAD_OUT;
			return false;
		}

		// now tile contains the new value for st.train_tile
		// curw, curh contain the new value for width and height
		fin[0] = tile.tile;
		fin[1] = curw;
		fin[2] = curh;
		return true;
	}


	private static void CreateSingle(ByteArrayPtr layout, int n)
	{
		int i = n;
		do { layout.wpp( (byte) 0 ); } while (--i > 0);
				
		layout.w( ((n-1) >> 1)-n, (byte) 2 );
	}

	private static void CreateMulti(ByteArrayPtr layout, int n, int b)
	{
		int i = n;
		do { layout.wpp( (byte) b ); } while (--i > 0);

		if (n > 4) {
			layout.w(0-n, (byte) 0);
			layout.w(n-1-n, (byte) 0 );
		}
	}

	private static void GetStationLayout(ByteArrayPtr layout, int numtracks, int plat_len, final  StationSpec spec)
	{
		if (spec != null && spec.lengths >= plat_len &&
				spec.platforms[plat_len - 1] >= numtracks &&
				null != spec.layouts[plat_len - 1][numtracks - 1]) {

			// Custom layout defined, follow it. 
			//memcpy(layout, spec.layouts[plat_len - 1][numtracks - 1], plat_len * numtracks);

			System.arraycopy(spec.layouts[plat_len - 1][numtracks - 1], 0, layout, 0, plat_len * numtracks);
			return;
		}

		if (plat_len == 1) {
			CreateSingle(layout, numtracks);
		} else {
			if(0 != (numtracks & 1)) CreateSingle(layout, plat_len);
			numtracks >>= 1;

		while (--numtracks >= 0) {
			CreateMulti(layout, plat_len, 4);
			CreateMulti(layout, plat_len, 6);
		}
		}
	}


	/** Build railroad station
	 * @param x,y starting position of station dragging/placement
	 * @param p1 various bitstuffed elements
	 * - p1 = (bit  0)    - orientation (p1 & 1)
	 * - p1 = (bit  8-15) - number of tracks
	 * - p1 = (bit 16-23) - platform length
	 * @param p2 various bitstuffed elements
	 * - p2 = (bit  0- 3) - railtype (p2 & 0xF)
	 * - p2 = (bit  4)    - set for custom station (p2 & 0x10)
	 * - p2 = (bit  8-..) - custom station id (p2 >> 8)
	 */
	public static int CmdBuildRailroadStation(int x, int y, int flags, int p1, int p2)
	{
		Station station;
		TileIndex tile_org;
		int w_org, h_org;
		int cost, ret;
		StationID[] est = { StationID.getInvalid() };
		int plat_len, numtracks;
		int direction;
		int [] finalvalues = new int[3];

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile_org = TileIndex.TileVirtXY(x, y);

		/* Does the authority allow this? */
		if (0 == (flags & Cmd.DC_NO_TOWN_RATING) && !Town.CheckIfAuthorityAllows(tile_org)) return Cmd.CMD_ERROR;
		if (!Player.ValParamRailtype(p2 & 0xF)) return Cmd.CMD_ERROR;

		/* unpack parameters */
		direction = p1 & 1;
		numtracks = BitOps.GB(p1,  8, 8);
		plat_len  = BitOps.GB(p1, 16, 8);
		/* w = length, h = num_tracks */
		if (direction != 0) {
			h_org = plat_len;
			w_org = numtracks;
		} else {
			w_org = plat_len;
			h_org = numtracks;
		}

		if (h_org > Global._patches.station_spread || w_org > Global._patches.station_spread) return Cmd.CMD_ERROR;

		// these values are those that will be stored in train_tile and station_platforms
		finalvalues[0] = tile_org.getTile();
		finalvalues[1] = w_org;
		finalvalues[2] = h_org;

		// Make sure the area below consists of clear tiles. (OR tiles belonging to a certain rail station)
		est[0] = StationID.getInvalid();
		// If DC_EXEC is in flag, do not want to pass it to CheckFlatLandBelow, because of a nice bug
		//  for detail info, see: https://sourceforge.net/tracker/index.php?func=detail&aid=1029064&group_id=103924&atid=636365
		if (Cmd.CmdFailed(ret = CheckFlatLandBelow(tile_org, w_org, h_org, flags&~Cmd.DC_EXEC, 5 << direction, Global._patches.nonuniform_stations ? est : null))) return Cmd.CMD_ERROR;
		cost = (int) (ret + (numtracks * Global._price.train_station_track + Global._price.train_station_length) * plat_len);

		// Make sure there are no similar stations around us.
		boolean [] canBuild = {true};
		station = GetStationAround(tile_org, w_org, h_org, est[0].id, canBuild);
		if (!canBuild[0]) return Cmd.CMD_ERROR;

		// See if there is a deleted station close to us.
		if (station == null) {
			station = GetClosestStationFromTile(tile_org, 8, PlayerID.getCurrent());
			if (station != null && 0 != station.facilities) station = null;
		}

		if (station != null) {
			// Reuse an existing station.
			if (station.owner.isNotNone() && !station.owner.isCurrentPlayer())
				return Cmd.return_cmd_error(Str.STR_3009_TOO_CLOSE_TO_ANOTHER_STATION);

			if (station.train_tile != null) {
				// check if we want to expanding an already existing station?
				if (Global.gs._is_old_ai_player || !Global._patches.join_stations)
					return Cmd.return_cmd_error(Str.STR_3005_TOO_CLOSE_TO_ANOTHER_RAILROAD);
				if (!CanExpandRailroadStation(station, finalvalues, direction))
					return Cmd.CMD_ERROR;
			}

			//XXX can't we pack this in the "else" part of the if above?
			if (!station.CheckStationSpreadOut(tile_org, w_org, h_org)) return Cmd.CMD_ERROR;
		}	else {
			// Create a new station
			station = AllocateStation();
			if (station == null) return Cmd.CMD_ERROR;

			station.town = Town.ClosestTownFromTile(tile_org, -1);
			if (!PlayerID.getCurrent().isSpecial() && 0 != (flags & Cmd.DC_EXEC))
				station.town.have_ratings = BitOps.RETSETBIT(station.town.have_ratings, PlayerID.getCurrent().id);

			if (!GenerateStationName(station, tile_org, 0)) return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC))
				station.StationInitialize(tile_org);
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			TileIndexDiff tile_delta;
			StationID station_index = StationID.get( station.index );
			StationSpec statspec;

			// Now really clear the land below the station
			// It should never return Cmd.CMD_ERROR.. but you never know ;)
			//  (a bit strange function name for it, but it really does clear the land, when DC_EXEC is in flags)
			if (Cmd.CmdFailed(CheckFlatLandBelow(tile_org, w_org, h_org, flags, 5 << direction, Global._patches.nonuniform_stations ? est : null))) return Cmd.CMD_ERROR;

			station.train_tile = new TileIndex( finalvalues[0] );
			if (0 == station.facilities) station.xy = new TileIndex( finalvalues[0] );
			station.facilities |= FACIL_TRAIN;
			station.owner = PlayerID.getCurrent();

			station.trainst_w = finalvalues[1];
			station.trainst_h = finalvalues[2];

			station.build_date = Global.get_date();

			tile_delta = direction != 0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0);

			statspec = (p2 & 0x10) != 0 ? StationClass.GetCustomStation(StationClassID.STAT_CLASS_DFLT, p2 >> 8) : null;
			//statspec = null;
			byte [] layout_ptr = new byte[numtracks * plat_len];
			GetStationLayout( new ByteArrayPtr(layout_ptr), numtracks, plat_len, statspec);

			int lpi = 0; // layout_ptr index

			do {
				MutableTileIndex tile = new MutableTileIndex( tile_org );
				int w = plat_len;
				do {

					Landscape.ModifyTile(tile, TileTypes.MP_STATION,
							//TileTypes.MP_SETTYPE(TileTypes.MP_STATION) | 
							TileTypes.MP_MAPOWNER_CURRENT |
							TileTypes.MP_MAP2 | TileTypes.MP_MAP5 | TileTypes.MP_MAP3LO | TileTypes.MP_MAP3HI,
							station_index.id, /* map2 parameter */
							p2 & 0xFF,     /* map3lo parameter */
							p2 >> 8,       /* map3hi parameter */
					layout_ptr[lpi++] + direction   /* map5 parameter */
							);

					tile.madd( tile_delta );
				} while (--w > 0);
				tile_org = tile_org.iadd( tile_delta.diff ^ TileIndex.TileDiffXY(1, 1).diff ); // perpendicular to tile_delta
			} while (--numtracks > 0);

			station.UpdateStationVirtCoordDirty();
			station.UpdateStationAcceptance(false);
			Window.InvalidateWindow(Window.WC_STATION_LIST, station.owner.id);
		}

		return cost;
	}

	private static void MakeRailwayStationAreaSmaller(Station st)
	{
		int w = st.trainst_w;
		int h = st.trainst_h;
		MutableTileIndex tile = new MutableTileIndex( st.train_tile );
		int i;

		//restart:
		boolean restart = false;
		while(true)
		{
			restart = false;

			// too small?
			if (w != 0 && h != 0) {
				// check the left side, x = constant, y changes
				for (i = 0; !st.TileBelongsToRailStation(tile.iadd(0, i));) {
					// the left side is unused?
					if (++i == h) {
						tile.madd(1, 0);
						w--;
						//goto restart;
						restart = true; break;
					}
				}
				if( restart ) continue;

				// check the right side, x = constant, y changes
				for (i = 0; !st.TileBelongsToRailStation(tile.iadd(w - 1, i));) {
					// the right side is unused?
					if (++i == h) {
						w--;
						//goto restart;
						restart = true; break;
					}
				}
				if( restart ) continue;

				// check the upper side, y = constant, x changes
				for (i = 0; !st.TileBelongsToRailStation(tile.iadd(i, 0));) {
					// the left side is unused?
					if (++i == w) {
						tile.madd(0, 1);
						h--;
						//goto restart;
						restart = true; break;
					}
				}
				if( restart ) continue;

				// check the lower side, y = constant, x changes
				for (i = 0; !st.TileBelongsToRailStation(tile.iadd(i, h - 1));) {
					// the left side is unused?
					if (++i == w) {
						h--;
						//goto restart;
						restart = true; break;
					}
				}
				if( restart ) continue;

			} else {
				tile = null;
			}
			break;
		}
		st.trainst_w = w;
		st.trainst_h = h;
		st.train_tile = tile;
	}

	public boolean TileBelongsToRailStation(TileIndex tile) {
		return tile.IsTileType(TileTypes.MP_STATION) && tile.getMap().m2 == this.index && tile.getMap().m5 < 8;
	}

	/** Remove a single tile from a railroad station.
	 * This allows for custom-built station with holes and weird layouts
	 * @param x,y tile coordinates to remove
	 * @param p1 unused
	 * @param p2 unused
	 */
	public static int CmdRemoveFromRailroadStation(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		Station st;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		// make sure the specified tile belongs to the current player, and that it is a railroad station.
		if (!tile.IsTileType(TileTypes.MP_STATION) || tile.getMap().m5 >= 8 || !Global._patches.nonuniform_stations) return Cmd.CMD_ERROR;
		st = GetStation(tile.getMap().m2);
		if (!PlayerID.getCurrent().isWater() && (!Player.CheckOwnership(st.owner) || !tile.EnsureNoVehicle())) return Cmd.CMD_ERROR;

		// if we reached here, it means we can actually delete it. do that.
		if(0 != (flags & Cmd.DC_EXEC)) {
			Landscape.DoClearSquare(tile);
			// now we need to make the "spanned" area of the railway station smaller if we deleted something at the edges.
			// we also need to adjust train_tile.
			MakeRailwayStationAreaSmaller(st);

			// if we deleted the whole station, delete the train facility.
			if (st.train_tile == null) {
				st.facilities &= ~FACIL_TRAIN;
				st.UpdateStationVirtCoordDirty();
				st.DeleteStationIfEmpty();
			}
		}
		return (int) Global._price.remove_rail_station;
	}


	// determine the number of platforms for the station
	public int GetStationPlatforms( TileIndex tile)
	{
		return GetStationPlatforms( this, tile);
	}
	// determine the number of platforms for the station
	public static int GetStationPlatforms( Station st, TileIndex tile)
	{
		MutableTileIndex t;
		TileIndexDiff delta;
		int dir;
		int len;

		assert(st.TileBelongsToRailStation(tile));

		len = 0;
		dir = tile.getMap().m5 & 1;
		delta = dir!=0 ? TileIndex.TileDiffXY(0, 1) : TileIndex.TileDiffXY(1, 0);

		// find starting tile..
		t = new MutableTileIndex(tile);
		do {
			t.msub( delta );
			len++;
		} while (st.TileBelongsToRailStation(t) && (t.getMap().m5 & 1) == dir);

		// find ending tile
		//t = tile;
		t = new MutableTileIndex(tile);
		do {
			t.madd( delta );
			len++;
		} while (st.TileBelongsToRailStation(t) && (t.getMap().m5 & 1) == dir);

		return len - 1;
	}

	/*
	static boolean TileBelongsToRailStation(final Station st, TileIndex tile)
	{
		return tile.IsTileType(TileTypes.MP_STATION) 
				&& tile.M().m2 == st.index && tile.M().m5 < 8;
	}*/


	/* Get's the direction the station exit points towards. Ie, returns 0 for a
	 * station with the exit NE. */
	public static int GetRoadStationDir(TileIndex tile)
	{
		assert(tile.IsRoadStationTile());
		return (tile.M().m5 - 0x43) & 3;
	}	


	private static RealSpriteGroup ResolveStationSpriteGroup(final SpriteGroup spg, final  Station st)
	{
		switch (spg.type) {
		case SGT_REAL:
			return (RealSpriteGroup)spg;

		case SGT_DETERMINISTIC: {
			final  DeterministicSpriteGroup dsg = (DeterministicSpriteGroup) spg;
			SpriteGroup target;
			int value = -1;

			if ((dsg.variable >> 6) == 0) {
				/* General property */
				value = Sprite.GetDeterministicSpriteValue(dsg.variable);

			} else {
				if (st == null) {
					/* We are in a build dialog of something,
					 * and we are checking for something undefined.
					 * That means we should get the first target
					 * (NOT the default one). */
					if (dsg.num_ranges > 0) {
						target = dsg.ranges[0].group;
					} else {
						target = dsg.default_group;
					}
					return ResolveStationSpriteGroup(target, null);
				}

				/* Station-specific property. */
				if (dsg.var_scope == VarSpriteGroupScope.VSG_SCOPE_PARENT) {
					/* TODO: Town structure. */

				} else /* VSG_SELF */ {
					if (dsg.variable == 0x40 || dsg.variable == 0x41) {
						/* FIXME: This is ad hoc only
						 * for waypoints. */
						value = 0x01010000;
					} else {
						/* TODO: Only small fraction done. */
						// TTDPatch runs on little-endian arch;
						// Variable is 0x70 + offset in the TTD's station structure
						switch (dsg.variable - 0x70) {
						case 0x80: value = st.facilities;             break;
						case 0x81: value = st.airport_type;           break;
						case 0x82: value = st.truck_stops.get(0).status;    break; // TODO why just use stop 0?
						case 0x83: value = st.bus_stops.get(0).status;      break;
						case 0x86: value = st.airport_flags & 0xFFFF; break;
						case 0x87: value = st.airport_flags & 0xFF;   break;
						case 0x8A: value = st.build_date;             break;
						}
					}
				}
			}

			target = value != -1 ? Sprite.EvalDeterministicSpriteGroup(dsg, value) : dsg.default_group;
			return ResolveStationSpriteGroup(target, st);
		}

		default:
		case SGT_RANDOMIZED:
			Global.error("I don't know how to handle random spritegroups yet!");
			return null;
		}
	}

	public static int GetCustomStationRelocation(final StationSpec spec, final Station st, int ctype)
	{
		final  RealSpriteGroup rsg = ResolveStationSpriteGroup(spec.spritegroup[ctype], st);

		if (rsg.sprites_per_set != 0) {
			if (rsg.loading.length != 0) return ((ResultSpriteGroup)rsg.loading[0]).result;

			if (rsg.loaded.length!= 0) return ((ResultSpriteGroup)rsg.loaded[0]).result;
		}

		Global.error("Custom station 0x%08x::0x%02x has no sprites associated.",
				spec.grfid, spec.localidx);
		/* This is what gets subscribed of dtss.image in newgrf.c,
		 * so it's probably kinda "default offset". Try to use it as
		 * emergency measure. */
		return Sprite.SPR_RAIL_PLATFORM_Y_FRONT;
	}

	private static int RemoveRailroadStation(Station st, TileIndex itile, int flags)
	{
		int w,h;
		int cost;

		/* if there is flooding and non-uniform stations are enabled, remove platforms tile by tile */
		if (PlayerID.getCurrent().isWater() && Global._patches.nonuniform_stations)
			return Cmd.DoCommandByTile(itile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_REMOVE_FROM_RAILROAD_STATION);

		/* Current player owns the station? */
		if (!PlayerID.getCurrent().isWater() && !Player.CheckOwnership(st.owner))
			return Cmd.CMD_ERROR;

		/* determine width and height of platforms */
		MutableTileIndex mtile = new MutableTileIndex( st.train_tile );
		w = st.trainst_w;
		h = st.trainst_h;

		assert(w != 0 && h != 0);

		/* cost is area * constant */
		cost = (int) (w*h*Global._price.remove_rail_station);

		/* clear all areas of the Station */
		do {
			int w_bak = w;
			do {
				// for nonuniform stations, only remove tiles that are actually train station tiles
				if (st.TileBelongsToRailStation(mtile)) {
					if (!mtile.EnsureNoVehicle())
						return Cmd.CMD_ERROR;
					if(0 != (flags & Cmd.DC_EXEC))
						Landscape.DoClearSquare(mtile);
				}
				mtile.madd( TileIndex.TileDiffXY(1, 0) );
			} while (--w > 0);
			w = w_bak;
			mtile.madd(-w, 1);
		} while (--h > 0);

		if(0 != (flags & Cmd.DC_EXEC)) {
			st.train_tile = null;
			st.facilities &= ~FACIL_TRAIN;

			st.UpdateStationVirtCoordDirty();
			st.DeleteStationIfEmpty();
		}

		return cost;
	}

	public static int DoConvertStationRail(TileIndex tile, int totype, boolean exec)
	{
		final  Station st = GetStation(tile.getMap().m2);
		if (!Player.CheckOwnership(st.owner) || !tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		// tile is not a railroad station?
		if (tile.getMap().m5 >= 8) return Cmd.CMD_ERROR;

		// tile is already of requested type?
		if (BitOps.GB(tile.getMap().m3, 0, 4) == totype) return Cmd.CMD_ERROR;

		if (exec) {
			// change type.
			tile.getMap().m3 =  BitOps.RETSB(tile.getMap().m3, 0, 4, totype);
			tile.MarkTileDirtyByTile();
		}

		return (int) (Global._price.build_rail/2);
	}


	/** Build a bus station
	 * @param x,y coordinates to build bus station at
	 * @param p1 busstop entrance direction (0 through 3), where 0 is NW, 1 is NE, etc.
	 * @param p2 0 for Bus stops, 1 for truck stops
	 */
	public static int CmdBuildRoadStop(int x, int y, int flags, int p1, int p2)
	{
		Station st;
		RoadStop road_stop;
		//RoadStop currstop = new RoadStop();
		//RoadStop prev = null; //new RoadStop();
		TileIndex tile;
		int cost;
		boolean type = p2 != 0 ;

		/* Saveguard the parameters */
		if (p1 > 3) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		tile = TileIndex.TileVirtXY(x, y);

		if (0==(flags & Cmd.DC_NO_TOWN_RATING) && !Town.CheckIfAuthorityAllows(tile))
			return Cmd.CMD_ERROR;

		cost = CheckFlatLandBelow(tile, 1, 1, flags, 1 << p1, null);
		if (Cmd.CmdFailed(cost)) return Cmd.CMD_ERROR;

		boolean [] canBuild = {true};
		st = GetStationAround(tile, 1, 1, StationID.getInvalid().id, canBuild);
		if (!canBuild[0]) return Cmd.CMD_ERROR;

		/* Find a station close to us */
		if (st == null) {
			st = GetClosestStationFromTile(tile, 8, PlayerID.getCurrent());
			if (st != null && 0 != st.facilities) st = null;
		}

		//give us a road stop in the list, and check if something went wrong
		road_stop = RoadStop.AllocateRoadStop();
		if (road_stop == null)
			return Cmd.return_cmd_error( (type) ? Str.STR_3008B_TOO_MANY_TRUCK_STOPS : Str.STR_3008A_TOO_MANY_BUS_STOPS);

		if ( st != null && (RoadStop.GetNumRoadStops(st, RoadStopType.RS_BUS) + RoadStop.GetNumRoadStops(st, RoadStopType.RS_TRUCK) >= RoadStop.ROAD_STOP_LIMIT))
			return Cmd.return_cmd_error( (type) ? Str.STR_3008B_TOO_MANY_TRUCK_STOPS : Str.STR_3008A_TOO_MANY_BUS_STOPS);

		if (st != null) {
			if (st.owner.isNotNone() && !st.owner.isCurrentPlayer())
				return Cmd.return_cmd_error(Str.STR_3009_TOO_CLOSE_TO_ANOTHER_STATION);

			if (!st.CheckStationSpreadOut(tile, 1, 1))
				return Cmd.CMD_ERROR;
		} else {
			Town t;

			st = AllocateStation();
			if (st == null) return Cmd.CMD_ERROR;

			st.town = t = Town.ClosestTownFromTile(tile, -1);

			if (!PlayerID.getCurrent().isSpecial() && 0 != (flags&Cmd.DC_EXEC))
				t.have_ratings = BitOps.RETSETBIT(t.have_ratings, PlayerID.getCurrent().id);

			st.sign.setWidth_1(0);

			if (!GenerateStationName(st, tile, 0)) return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC)) st.StationInitialize(tile);
		}

		cost += (type) ? Global._price.build_truck_station : Global._price.build_bus_station;

		if(0 != (flags & Cmd.DC_EXEC)) {
			//initialize an empty station
			RoadStop.InitializeRoadStop(road_stop, /*prev,*/ tile, st.index);
			road_stop.type = type ? 1 : 0;
			if (0==st.facilities) st.xy = tile;
			st.facilities |= (type) ? FACIL_TRUCK_STOP : FACIL_BUS_STOP;
			st.owner = PlayerID.getCurrent();

			st.build_date = Global.get_date();

			Landscape.ModifyTile(tile, TileTypes.MP_STATION,
					//TileTypes.MP_SETTYPE(TileTypes.MP_STATION) | 
					TileTypes.MP_MAPOWNER_CURRENT |
					TileTypes.MP_MAP2 | TileTypes.MP_MAP5 | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR,
					st.index,                       /* map2 parameter */
					/* XXX - Truck stops have 0x43 _m[].m5 value + direction
					 * XXX - Bus stops have a _map5 value of 0x47 + direction */
					((type) ? 0x43 : 0x47) + p1 /* map5 parameter */
					);

			if( type )
				st.truck_stops.add(road_stop);
			else
				st.bus_stops.add(road_stop);
			
			st.UpdateStationVirtCoordDirty();
			st.UpdateStationAcceptance(false);
			Window.InvalidateWindow(Window.WC_STATION_LIST, st.owner.id);
		}
		return cost;
	}

	/**
	 * Remove a truck/bus station 
	 * @param flags
	 * @param tile
	 * @return
	 */
	private int RemoveRoadStop(int flags, TileIndex tile)
	{
		List<RoadStop> primary_stop;
		RoadStop cur_stop;
		boolean is_truck = tile.getMap().m5 < 0x47;

		if (!PlayerID.getCurrent().isWater() && !Player.CheckOwnership(owner))
			return Cmd.CMD_ERROR;

		if (is_truck) { // truck stop
			primary_stop = truck_stops;
			cur_stop = RoadStop.GetRoadStopByTile(tile, RoadStopType.RS_TRUCK);
		} else {
			primary_stop = bus_stops;
			cur_stop = RoadStop.GetRoadStopByTile(tile, RoadStopType.RS_BUS);
		}

		assert(cur_stop != null);

		if (!tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			int i;
			Landscape.DoClearSquare(tile);

			/* Clear all vehicles destined for this Station */
			for (i = 0; i != Station.NUM_SLOTS; i++) {
				if (cur_stop.slot[i] != INVALID_SLOT) {
					Vehicle v = Vehicle.GetVehicle(cur_stop.slot[i]);
					RoadVehCmd.ClearSlot(v, v.road.slot);
				}
			}

			cur_stop.used = false;
			//noinspection AssertWithSideEffects
			final boolean removeRet = primary_stop.remove(cur_stop);
			assert removeRet;

			//we only had one stop left
			if(primary_stop.isEmpty())
				facilities &= (is_truck) ? ~FACIL_TRUCK_STOP : ~FACIL_BUS_STOP;
			
			UpdateStationVirtCoordDirty();
			DeleteStationIfEmpty();
		}

		return (int) ((is_truck) ? Global._price.remove_truck_station : Global._price.remove_bus_station);
	}




	/** Place an Airport.
	 * @param x,y tile coordinates where airport will be built
	 * @param p1 airport type, @see airport.h
	 * @param p2 unused
	 */
	public static int CmdBuildAirport(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile;
		Town t;
		//Station st;
		int cost;
		int w, h;
		boolean airport_upgrade = true;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* Check if a valid, buildable airport was chosen for construction */
		if (p1 > _airport_map5_tiles.length || !BitOps.HASBIT(Airport.GetValidAirports(), p1)) return Cmd.CMD_ERROR;

		tile = TileIndex.TileVirtXY(x, y);

		if (0 == (flags & Cmd.DC_NO_TOWN_RATING) && !Town.CheckIfAuthorityAllows(tile))
			return Cmd.CMD_ERROR;

		t = Town.ClosestTownFromTile(tile, -1);

		/* Check if local auth refuses a new airport */
		{
			int [] num = {0};
 
			Global.gs._stations.forEach( st ->
			{
				if ( (st.owner == null || st.owner.id != Owner.OWNER_TOWN) 
						&& st.isValid() 
						&& st.town == t 
						&& 0 != (st.facilities&FACIL_AIRPORT) 
						&& st.airport_type != Airport.AT_OILRIG)
					num[0]++;
			});

			//if (num[0] >= 2 && Global.gs._current_player.id != Owner.OWNER_TOWN) 
			if (num[0] >= 2 && !PlayerID.getCurrent().isTown()) 
			{
				Global.SetDParam(0, t.index);
				return Cmd.return_cmd_error(Str.STR_2035_LOCAL_AUTHORITY_REFUSES);
			}
		}

		w = _airport_size_x[p1];
		h = _airport_size_y[p1];

		cost = CheckFlatLandBelow(tile, w, h, flags, 0, null);
		if (Cmd.CmdFailed(cost)) return Cmd.CMD_ERROR;

		boolean [] canBuild = {true};
		Station st = GetStationAround(tile, w, h, StationID.getInvalid().id, canBuild);
		if (!canBuild[0]) return Cmd.CMD_ERROR;

		/* Find a station close to us */
		if (st == null) {
			st = GetClosestStationFromTile(tile, 8, PlayerID.getCurrent());
			if (st != null && 0 != st.facilities) st = null;
		}

		if (st != null) {
			if (st.owner.isNotNone() && !st.owner.isCurrentPlayer() )
				return Cmd.return_cmd_error(Str.STR_3009_TOO_CLOSE_TO_ANOTHER_STATION);

			if (!st.CheckStationSpreadOut(tile, 1, 1))
				return Cmd.CMD_ERROR;

			if (st.airport_tile != null)
				return Cmd.return_cmd_error(Str.STR_300D_TOO_CLOSE_TO_ANOTHER_AIRPORT);
		} else {
			airport_upgrade = false;

			st = AllocateStation();
			if (st == null) return Cmd.CMD_ERROR;

			st.town = t;

			if (!PlayerID.getCurrent().isSpecial() && (flags & Cmd.DC_EXEC) != 0)
				t.have_ratings = BitOps.RETSETBIT(t.have_ratings, PlayerID.getCurrent().id);

			st.sign.setWidth_1(0);

			// if airport type equals Heliport then generate
			// type 5 name, which is heliport, otherwise airport names (1)
			if (!GenerateStationName(st, tile, (p1 == Airport.AT_HELIPORT) ? 5 : 1))
				return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC))
				st.StationInitialize(tile);
		}

		cost += Global._price.build_airport * w * h;

		if( 0 != (flags & Cmd.DC_EXEC)) {
			final Airport afc = Airport.GetAirport(p1);

			st.owner = PlayerID.getCurrent();
			if (Player.IsLocalPlayer() && afc.airport_depots.length /*nof_depots*/ != 0)
				Depot._last_built_aircraft_depot_tile = tile.iadd( TileIndex.ToTileIndexDiff(afc.airport_depots[0]));

			st.airport_tile = tile;
			if (0 == st.facilities) st.xy = tile;
			st.facilities |= FACIL_AIRPORT;
			st.airport_type = p1;
			st.airport_flags = 0;

			st.build_date = Global.get_date();

			/* if airport was demolished while planes were en-route to it, the
			 * positions can no longer be the same (v.air.pos), since different
			 * airports have different indexes. So update all planes en-route to this
			 * airport. Only update if
			 * 1. airport is upgraded
			 * 2. airport is added to existing station (unfortunately unavoideable)
			 */
			if (airport_upgrade) 
				AirCraft.UpdateAirplanesOnNewStation(st);

			{
				final byte []b = _airport_map5_tiles[p1];
				int [] bi = {0};
				Station fst = st;
				//BEGIN_TILE_LOOP(tile_cur,w,h,tile)
				TileIndex.forAll(w, h, tile, (tile_cur) ->
				{
					Landscape.ModifyTile(tile_cur, TileTypes.MP_STATION,
							//TileTypes.MP_SETTYPE(TileTypes.MP_STATION) | 
							TileTypes.MP_MAPOWNER_CURRENT |
							TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR | TileTypes.MP_MAP5,
							fst.index, b[bi[0]++]);
					return false;
				}); 
				//END_TILE_LOOP(tile_cur,w,h,tile)
			}

			st.UpdateStationVirtCoordDirty();
			st.UpdateStationAcceptance(false);
			Window.InvalidateWindow(Window.WC_STATION_LIST, st.owner.id);
		}

		st.airport_queue = VehicleQueue.new_VQueue();
		st.helicopter_queue = VehicleQueue.new_VQueue();

		return cost;
	}

	private static int RemoveAirport(Station st, int flags)
	{
		TileIndex tile;
		int w,h;
		int cost;

		if (!PlayerID.getCurrent().isWater() && !Player.CheckOwnership(st.owner))
			return Cmd.CMD_ERROR;

		tile = st.airport_tile;

		w = _airport_size_x[st.airport_type];
		h = _airport_size_y[st.airport_type];

		cost = (int) (w * h * Global._price.remove_airport);

		int [] err = {0};
		{
			//BEGIN_TILE_LOOP(tile_cur,w,h,tile)
			TileIndex.forAll(w, h, tile, (tile_cur) ->
			{
				if (!tile_cur.EnsureNoVehicle())
				{
					err[0] = Cmd.CMD_ERROR;
					return true;
				}

				if(0 != (flags & Cmd.DC_EXEC)) {
					TextEffect.DeleteAnimatedTile(tile_cur);
					Landscape.DoClearSquare(tile_cur);
				}
				return false;
			});
			//END_TILE_LOOP(tile_cur, w,h,tile)
		}
		if( err[0] != 0 ) return err[0];

		if(0 != (flags & Cmd.DC_EXEC)) {
			final  Airport afc = Airport.GetAirport(st.airport_type);
			int i;

			for (i = 0; i < afc.nof_depots(); ++i)
				Window.DeleteWindowById(Window.WC_VEHICLE_DEPOT, tile.iadd(TileIndex.ToTileIndexDiff(afc.airport_depots[i])).tile );

			st.airport_tile = null;
			st.facilities &= ~FACIL_AIRPORT;

			st.UpdateStationVirtCoordDirty();
			st.DeleteStationIfEmpty();
		}

		return cost;
	}

	/** Build a buoy.
	 * @param x,y tile coordinates of bouy construction
	 * @param p1 unused
	 * @param p2 unused
	 */
	public static int CmdBuildBuoy(int x, int y, int flags, int p1, int p2)
	{
		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		TileInfo ti = Landscape.FindLandscapeHeight(x, y);

		if (ti.type != TileTypes.MP_WATER.ordinal() || ti.tileh != 0 || ti.map5 != 0 || ti.tile == null)
			return Cmd.return_cmd_error(Str.STR_304B_SITE_UNSUITABLE);

		Station st = AllocateStation();
		if (st == null) return Cmd.CMD_ERROR;

		st.town = Town.ClosestTownFromTile(ti.tile, -1);
		st.sign.setWidth_1(0);

		if (!GenerateStationName(st, ti.tile, 4)) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			st.StationInitialize(ti.tile);
			st.dock_tile = ti.tile;
			st.facilities |= FACIL_DOCK;
			/* Buoys are marked in the Station struct by this flag. Yes, it is this
			 * braindead.. */
			st.had_vehicle_of_type |= HVOT_BUOY;
			st.owner = PlayerID.getNone();

			st.build_date = Global.get_date();

			Landscape.ModifyTile(ti.tile, TileTypes.MP_STATION,
					//TileTypes.MP_SETTYPE(TileTypes.MP_STATION) |
					TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5,
					st.index,		/* map2 */
					Owner.OWNER_NONE,		/* map_owner */
					0x52					/* map5 */
					);

			st.UpdateStationVirtCoordDirty();
			st.UpdateStationAcceptance(false);
			Window.InvalidateWindow(Window.WC_STATION_LIST, st.owner.id);
		}

		return (int) Global._price.build_dock;
	}

	/* Checks if any ship is servicing the buoy specified. Returns yes or no */
	private boolean CheckShipsOnBuoy()
	{
		boolean [] ret = {false};
		Vehicle.forEach( (v) ->
		{
			if (v.type == Vehicle.VEH_Ship) {
				v.forEachOrder( (order) ->
				{
					if (order.type == Order.OT_GOTO_STATION && order.station == index) {
						ret[0] = true;
					}
				});
			}
		});
		return ret[0];
	}

	private int RemoveBuoy(int flags)
	{
		//if (Global.gs._current_player.id >= Global.MAX_PLAYERS) 
		if(PlayerID.getCurrent().isSpecial()) 
		{
			/* XXX: strange stuff */
			return Cmd.return_cmd_error(Str.INVALID_STRING);
		}

		TileIndex tile = dock_tile;

		if (CheckShipsOnBuoy())   return Cmd.return_cmd_error(Str.STR_BUOY_IS_IN_USE);
		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			dock_tile = null;
			/* Buoys are marked in the Station struct by this flag. 
			 * Yes, it is this braindead.. */
			facilities &= ~FACIL_DOCK;
			had_vehicle_of_type &= ~HVOT_BUOY;

			Landscape.ModifyTile(tile, TileTypes.MP_WATER,
					//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) |
					TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR | TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5,
					Owner.OWNER_WATER, /* map_owner */
					0			/* map5 */
					);

			UpdateStationVirtCoordDirty();
			DeleteStationIfEmpty();
		}

		return (int) Global._price.remove_truck_station;
	}

	static final  TileIndexDiffC _dock_tileoffs_chkaround[] = {
			new TileIndexDiffC( -1,  0),
			new TileIndexDiffC( 0,  0),
			new TileIndexDiffC( 0,  0),
			new TileIndexDiffC( 0, -1)
	};
	static final  byte _dock_w_chk[] = { 2,1,2,1 };
	static final  byte _dock_h_chk[] = { 1,2,1,2 };

	/** Build a dock/haven.
	 * @param x,y tile coordinates where dock will be built
	 * @param p1 unused
	 * @param p2 unused
	 */
	public static int CmdBuildDock(int x, int y, int flags, int p1, int p2)
	{
		int direction;
		int cost;
		TileIndex tile, tile_cur;
		Station st;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		TileInfo ti = Landscape.FindLandscapeHeight(x, y);

		/*
		if ((direction=0,ti.tileh) != 3 &&
				(direction++,ti.tileh) != 9 &&
				(direction++,ti.tileh) != 12 &&
				(direction++,ti.tileh) != 6)
			return Cmd.return_cmd_error(Str.STR_304B_SITE_UNSUITABLE);
		 */

		direction=0;
		if(ti.tileh != 3)
		{
		direction++;
		if(ti.tileh != 9 )
		{
		direction++;
		if(ti.tileh != 12 )
		{
		direction++;
		if(ti.tileh != 6)
			return Cmd.return_cmd_error(Str.STR_304B_SITE_UNSUITABLE);
		}
		}
		}
		if (!ti.tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		cost = Cmd.DoCommandByTile(ti.tile, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(cost)) return Cmd.CMD_ERROR;

		tile_cur = (tile=ti.tile).iadd( TileIndex.TileOffsByDir(direction) );

		if (!tile_cur.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		Landscape.FindLandscapeHeightByTile(ti, tile_cur);
		if (ti.tileh != 0 || ti.type != TileTypes.MP_WATER.ordinal()) return Cmd.return_cmd_error(Str.STR_304B_SITE_UNSUITABLE);

		cost = Cmd.DoCommandByTile(tile_cur, 0, 0, flags, Cmd.CMD_LANDSCAPE_CLEAR);
		if (Cmd.CmdFailed(cost)) return Cmd.CMD_ERROR;

		tile_cur = tile_cur.iadd( TileIndex.TileOffsByDir(direction) );
		Landscape.FindLandscapeHeightByTile(ti, tile_cur);
		if (ti.tileh != 0 || ti.type != TileTypes.MP_WATER.ordinal())
			return Cmd.return_cmd_error(Str.STR_304B_SITE_UNSUITABLE);

		/* middle */
		boolean [] canBuild = {true};
		st = GetStationAround(
				tile.iadd( TileIndex.ToTileIndexDiff(_dock_tileoffs_chkaround[direction]) ),
				_dock_w_chk[direction], _dock_h_chk[direction], -1, canBuild);
		if (!canBuild[0]) return Cmd.CMD_ERROR;

		/* Find a station close to us */
		if (st == null) {
			st = GetClosestStationFromTile(tile, 8, PlayerID.getCurrent());
			if (st!=null && 0 != st.facilities) st = null;
		}

		if (st != null) {
			if(st.owner.isNotNone() && !st.owner.isCurrentPlayer())
				return Cmd.return_cmd_error(Str.STR_3009_TOO_CLOSE_TO_ANOTHER_STATION);

			if (!st.CheckStationSpreadOut(tile, 1, 1)) return Cmd.CMD_ERROR;

			if (st.dock_tile != null) return Cmd.return_cmd_error(Str.STR_304C_TOO_CLOSE_TO_ANOTHER_DOCK);
		} else {
			Town t;

			st = AllocateStation();
			if (st == null) return Cmd.CMD_ERROR;

			st.town = t = Town.ClosestTownFromTile(tile, -1);

			if (!PlayerID.getCurrent().isSpecial() && 0!= (flags&Cmd.DC_EXEC) )
				t.have_ratings = BitOps.RETSETBIT(t.have_ratings, PlayerID.getCurrent().id);

			st.sign.setWidth_1(0);

			if (!GenerateStationName(st, tile, 3)) return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC)) st.StationInitialize(tile);
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			st.dock_tile = tile;
			if (0==st.facilities) st.xy = tile;
			st.facilities |= FACIL_DOCK;
			st.owner = PlayerID.getCurrent();

			st.build_date = Global.get_date();

			Landscape.ModifyTile(tile, TileTypes.MP_STATION,
					//TileTypes.MP_SETTYPE(TileTypes.MP_STATION) | 
					TileTypes.MP_MAPOWNER_CURRENT |
					TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR |
					TileTypes.MP_MAP5,
					st.index,
					direction + 0x4C);

			Landscape.ModifyTile(tile.iadd(TileIndex.TileOffsByDir(direction)),
					TileTypes.MP_STATION,
					//TileTypes.MP_SETTYPE(TileTypes.MP_STATION) | 
					TileTypes.MP_MAPOWNER_CURRENT |
					TileTypes.MP_MAP2 | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR |
					TileTypes.MP_MAP5,
					st.index,
					(direction&1) + 0x50);

			st.UpdateStationVirtCoordDirty();
			st.UpdateStationAcceptance(false);
			Window.InvalidateWindow(Window.WC_STATION_LIST, st.owner);
		}
		return (int) Global._price.build_dock;
	}

	private int RemoveDock(int flags)
	{
		TileIndex tile1;
		TileIndex tile2;

		if (!Player.CheckOwnership(owner)) return Cmd.CMD_ERROR;

		tile1 = dock_tile;
		tile2 = tile1.iadd( TileIndex.TileOffsByDir(tile1.M().m5 - 0x4C) );

		if (!tile1.EnsureNoVehicle()) return Cmd.CMD_ERROR;
		if (!tile2.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			Landscape.DoClearSquare(tile1);

			// convert the water tile to water.
			Landscape.ModifyTile(tile2, TileTypes.MP_WATER,
					//TileTypes.MP_SETTYPE(TileTypes.MP_WATER) | 
					TileTypes.MP_MAPOWNER | TileTypes.MP_MAP5 | TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP3LO_CLEAR | TileTypes.MP_MAP3HI_CLEAR, Owner.OWNER_WATER, 0);

			dock_tile = null;
			facilities &= ~FACIL_DOCK;

			UpdateStationVirtCoordDirty();
			DeleteStationIfEmpty();
		}

		return (int) Global._price.remove_dock;
	}


	private static void DrawTile_Station(TileInfo ti)
	{
		int image_or_modificator;
		int image;

		DrawTileSprites t = null;
		/* RailType */ int railtype = BitOps.GB(ti.tile.M().m3, 0, 4);
		final  RailtypeInfo rti = Rail.GetRailTypeInfo(railtype);
		//SpriteID 
		int offset;
		int relocation = 0;

		{
			PlayerID owner = ti.tile.GetTileOwner();
			image_or_modificator = Sprite.PALETTE_TO_GREY; /* NOTE: possible bug in ttd here? */
			if (owner.id < Global.MAX_PLAYERS) image_or_modificator = Sprite.PLAYER_SPRITE_COLOR(owner);
		}

		// don't show foundation for docks (docks are between 76 (0x4C) and 81 (0x51))
		if (ti.tileh != 0 && (ti.map5 < 0x4C || ti.map5 > 0x51))
			Landscape.DrawFoundation(ti, ti.tileh);

		if(0 != (ti.tile.M().m3 & 0x10)) {
			// look for customization
			final  StationSpec statspec = StationClass.GetCustomStation(StationClassID.STAT_CLASS_DFLT, ti.tile.M().m4);

			//debug("Cust-o-mized %p", statspec);

			if (statspec != null) {
				final  Station  st = GetStation(ti.tile.M().m2);

				relocation = GetCustomStationRelocation(statspec, st, 0);
				//debug("Relocation %d", relocation);
				t = statspec.renderdata[ti.map5];
			}
		} /* */

		if (t == null) 
			t = _station_display_datas[ti.map5];

		image = t.ground_sprite;
		if(0 != (image & Sprite.PALETTE_MODIFIER_COLOR)) image |= image_or_modificator;

		// For custom sprites, there's no railtype-based pitching.
		offset = (image & Sprite.SPRITE_MASK) < GRFFile._custom_sprites_base ? rti.total_offset.id : railtype;
		image += offset;

		// station_land array has been increased from 82 elements to 114
		// but this is something else. If AI builds station with 114 it looks all weird
		ViewPort.DrawGroundSprite(image);

		if (Global._debug_pbs_level >= 1) {
			int pbs =  Pbs.PBSTileReserved(ti.tile);
			if(0 != (pbs & Rail.TRACK_BIT_DIAG1)) ViewPort.DrawGroundSprite(rti.base_sprites.single_y.id | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_DIAG2)) ViewPort.DrawGroundSprite(rti.base_sprites.single_x.id | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_UPPER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_n.id | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_LOWER)) ViewPort.DrawGroundSprite(rti.base_sprites.single_s.id | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_LEFT))  ViewPort.DrawGroundSprite(rti.base_sprites.single_w.id | Sprite.PALETTE_CRASH);
			if(0 != (pbs & Rail.TRACK_BIT_RIGHT)) ViewPort.DrawGroundSprite(rti.base_sprites.single_e.id | Sprite.PALETTE_CRASH);
		}

		//foreach_draw_tile_seq(dtss, t.seq)
		//for (dtss = t.seq; ((byte) dtss.delta_x) != 0x80; dtss++)
		for (int ssi = 0; ssi < t.seq.length; ssi++)
		{
			final DrawTileSeqStruct dtss = t.seq[ssi];

			if((dtss.delta_x) == 0x80)
				break;

			image = dtss.image + relocation;
			image += offset;
			if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS)) {
				image = Sprite.RET_MAKE_TRANSPARENT(image);
			} else {
				if(0 != (image & Sprite.PALETTE_MODIFIER_COLOR)) image |= image_or_modificator;
			}

			if (dtss.delta_z != 0x80) {
				ViewPort.AddSortableSpriteToDraw(image, ti.x + dtss.delta_x, ti.y + dtss.delta_y, dtss.width, dtss.height, dtss.unk, ti.z + dtss.delta_z);
			} else {
				ViewPort.AddChildSpriteScreen(image, dtss.delta_x, dtss.delta_y);
			}
		}
	}

	public static void StationPickerDrawSprite(int x, int y, /* RailType */ int railtype, int image)
	{
		int ormod, img;
		//DrawTileSeqStruct dtss;
		final  DrawTileSprites t;
		final  RailtypeInfo rti = Rail.GetRailTypeInfo(railtype);

		ormod = Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player);

		t = _station_display_datas[image];

		img = t.ground_sprite;
		if(0 != (img & Sprite.PALETTE_MODIFIER_COLOR)) img |= ormod;
		Gfx.DrawSprite(img + rti.total_offset.id, x, y);

		/*(/foreach_draw_tile_seq(dtss, t.seq) 
		for (dtss = t.seq; ((byte) dtss.delta_x) != 0x80; dtss++)
		{
			Point pt = Point.RemapCoords(dtss.delta_x, dtss.delta_y, dtss.delta_z);
			Gfx.DrawSprite((dtss.image | ormod) + rti.total_offset, x + pt.x, y + pt.y);
		}*/

		for (int ssp = 0; ssp < t.seq.length ; ssp++)
		{
			final DrawTileSeqStruct dtss = t.seq[ssp];

			if( (dtss.delta_x) == 0x80)
				break;

			Point pt = Point.RemapCoords(dtss.delta_x, dtss.delta_y, dtss.delta_z);
			Gfx.DrawSprite((dtss.image | ormod) + rti.total_offset.id, x + pt.x, y + pt.y);
		}

	}

	private static int GetSlopeZ_Station(final  TileInfo  ti)
	{
		return (ti.tileh != 0) ? ti.z + 8 : ti.z;
	}

	private static int GetSlopeTileh_Station(final  TileInfo ti)
	{
		return 0;
	}

	private static AcceptedCargo GetAcceptedCargo_Station(TileIndex tile)
	{
		return new AcceptedCargo();
		/* not used */
	}

	private static TileDesc GetTileDesc_Station(TileIndex tile)
	{
		int str;
		TileDesc td = new TileDesc();

		td.owner = tile.GetTileOwner().id;
		td.build_date = GetStation(tile.getMap().m2).build_date;

		int m5 = tile.getMap().m5;

		if(m5 < 8) 					str=Str.STR_305E_RAILROAD_STATION;
		else if(m5==32 || m5==45) 	str=Str.STR_305F_AIRCRAFT_HANGAR; // hangars
		else if(m5 < 0x43 || (m5 >= 83 && m5 <= 114)) str=Str.STR_3060_AIRPORT;
		else if(m5 < 0x47) 			str=Str.STR_3061_TRUCK_LOADING_AREA;
		else if(m5 < 0x4B) 			str=Str.STR_3062_BUS_STATION;
		else if(m5 == 0x4B) 		str=Str.STR_4807_OIL_RIG;
		else if(m5 != 0x52) 		str=Str.STR_3063_SHIP_DOCK;
		else 						str=Str.STR_3069_BUOY;

		td.str = str;
		return td;
	}


	static final  byte tile_track_status_rail[] = { 1, 2, 1, 2, 1, 2, 1, 2 };

	//private static int GetTileTrackStatus_Station(TileIndex tile, TransportType mode)
	private static int GetTileTrackStatus_Station(TileIndex tile, TransportType mode)
	{
		int i = tile.getMap().m5;
		int j = 0;

		switch (mode) {
		case Rail:
			if (i < 8) {
				j = tile_track_status_rail[i];
			}
			j += (j << 8);
			break;

		case Water:
			// buoy is coded as a station, it is always on open water
			// (0x3F, all tracks available)
			if (i == 0x52) j = 0x3F;
			j += (j << 8);
			break;

		default:
			break;
		}

		return j;
	}


	private static void TileLoop_Station(TileIndex tile)
	{
		// FIXME -- GetTileTrackStatus_Station . animated stationtiles
		// hardcoded.....not good
		switch (tile.getMap().m5) {
		case 0x27: // large big airport
		case 0x3A: // flag small airport
		case 0x5A: // radar international airport
		case 0x66: // radar metropolitan airport
			TextEffect.AddAnimatedTile(tile);
			break;

		case 0x4B: // oilrig (station part)
		case 0x52: // bouy
			WaterCmd.TileLoop_Water(tile);
			break;

		default: break;
		}
	}


	private static void AnimateTile_Station(TileIndex tile)
	{
		int m5 = tile.getMap().m5;
		//FIXME -- AnimateTile_Station . not nice code, lots of things double
		// again hardcoded...was a quick hack

		// turning radar / windsack on airport
		if (m5 >= 39 && m5 <= 50) { // turning radar (39 - 50)
			if( 0 != (Global._tick_counter & 3) )
				return;

			if (++m5 == 50+1)
				m5 = 39;

			tile.getMap().m5 = m5;
			tile.MarkTileDirtyByTile();
			//added - begin
		} else if (m5 >= 90 && m5 <= 113) { // turning radar with ground under it (different fences) (90 - 101 | 102 - 113)
			if( 0 != (Global._tick_counter & 3) )
				return;

			m5++;

			if (m5 == 101+1) {m5 = 90;}  // radar with fences in south
			else if (m5 == 113+1) {m5 = 102;} // radar with fences in north

			tile.getMap().m5 = m5;
			tile.MarkTileDirtyByTile();
			//added - end
		} else if (m5 >= 0x3A && m5 <= 0x3D) {  // windsack (58 - 61)
			if( 0 != (Global._tick_counter & 1) )
				return;

			if (++m5 == 0x3D+1)
				m5 = 0x3A;

			tile.getMap().m5 = m5;
			tile.MarkTileDirtyByTile();
		}
	}

	private static void ClickTile_Station(TileIndex tile)
	{
		// 0x20 - hangar large airport (32)
		// 0x41 - hangar small airport (65)
		if (tile.getMap().m5 == 32 || tile.getMap().m5 == 65) {
			AirCraft.ShowAircraftDepotWindow(tile);
		} else {
			StationGui.ShowStationViewWindow(tile.getMap().m2);
		}
	}

	private static final int[] _enter_station_speedtable = {
		215, 195, 175, 155, 135, 115, 95, 75, 55, 35, 15, 0
	};

	private static int VehicleEnter_Station(Vehicle v, TileIndex tile, int x, int y)
	{
		//StationID 
		int station_id;
		int dir;

		if (v.type == Vehicle.VEH_Train) {
			if (BitOps.IS_INT_INSIDE(tile.getMap().m5, 0, 8) && v.IsFrontEngine() &&
					!tile.iadd(TileIndex.TileOffsByDir(v.direction >> 1)).IsCompatibleTrainStationTile(tile)) 
			{

				station_id = tile.getMap().m2;
				if ((0==(v.getCurrent_order().flags & Order.OF_NON_STOP) && !Global._patches.new_nonstop) ||
						(v.getCurrent_order().type == Order.OT_GOTO_STATION && v.getCurrent_order().station == station_id)) {
					if (!(Global._patches.new_nonstop && 0!=(v.getCurrent_order().flags & Order.OF_NON_STOP) ) &&
							v.getCurrent_order().type != Order.OT_LEAVESTATION &&
							v.last_station_visited != station_id) {
						x &= 0xF;
						y &= 0xF;

						dir = v.direction & 6;
						if(0 != (dir & 2)) { int t = x; x = y; y = t; } // intswap(x,y);
						if (y == 8) {
							if (dir != 2 && dir != 4) x = ~x & 0xF;
							if (x == 12) return 2 | (station_id << 8); // enter Station 
							if (x < 12) {
								v.setTrainSlowing(true);
								int newMaxSpeed = _enter_station_speedtable[x];
								if (newMaxSpeed < v.cur_speed) {
									v.cur_speed = newMaxSpeed;
								}
							}
						}
					}
				}
			}
		} else if (v.type == Vehicle.VEH_Road) {
			if (v.road.state < 16 && !BitOps.HASBIT(v.road.state, 2) && v.road.frame == 0) {
				if (BitOps.IS_INT_INSIDE(tile.getMap().m5, 0x43, 0x4B)) {
					/* Attempt to allocate a parking bay in a road stop */
					RoadStop rs = RoadStop.GetRoadStopByTile(tile, RoadStop.GetRoadStopType(tile));

					/* rs.status bits 0 and 1 describe current the two parking spots.
					 * 0 means occupied, 1 means free. */

					// Check if station is busy or if there are no free bays.
					if (BitOps.HASBIT(rs.status, 7) || BitOps.GB(rs.status, 0, 2) == 0)
						return 8;

					v.road.state += 32;

					// if the first bay is free, allocate that, else the second bay must be free.
					if (BitOps.HASBIT(rs.status, 0)) {
						rs.status = BitOps.RETCLRBIT(rs.status, 0);
					} else {
						rs.status = BitOps.RETCLRBIT(rs.status, 1);
						v.road.state += 2;
					}

					// mark the station as busy
					rs.status = BitOps.RETSETBIT(rs.status, 7);
				}
			}
		}

		return 0;
	}

	/** Removes a station from the list.
	 * This is done by setting the .xy property to 0,
	 * and doing some maintenance, especially clearing vehicle orders.
	 * Aircraft-Hangar orders need special treatment here, as the hangars are
	 * actually part of a station (tiletype is STATION), but the order type
	 * is OT_GOTO_DEPOT.
	 * @param st Station to be deleted
	 */
	private static void DeleteStation(Station st)
	{
		//StationID 
		int index;
		//Vehicle v;
		st.xy = null;

		Global.DeleteName(st.string_id);
		st.MarkStationDirty();
		StationGui.requestSortStations(); // delete station, remove sign
		Window.InvalidateWindowClasses(Window.WC_STATION_LIST);

		index = st.index;
		Window.DeleteWindowById(Window.WC_STATION_VIEW, index);

		{
			//Now delete all orders that go to the station
			Order order = new Order();
			order.type = Order.OT_GOTO_STATION;
			order.station = index;
			Order.DeleteDestinationFromVehicleOrder(order);
		}

		//And do the same with aircraft that have the station as a hangar-stop
		Vehicle.forEach( (v) ->
		{
			boolean [] invalidate = {false};
			if (v.type == Vehicle.VEH_Aircraft) {
				v.forEachOrder( (order) ->
				{
					if (order.type == Order.OT_GOTO_DEPOT && order.station == index) {
						order.type = Order.OT_DUMMY;
						order.flags = 0;
						invalidate[0] = true;
					}
				});
			}
			//Orders for the vehicle have been changed, invalidate the window
			if (invalidate[0]) Window.InvalidateWindow(Window.WC_VEHICLE_ORDERS, v.index);
		});

		//Subsidies need removal as well
		Subsidy.DeleteSubsidyWithStation(index);

		st.airport_queue.clear();
		st.helicopter_queue.clear();
	}

	public static void DeleteAllPlayerStations()
	{
		Global.gs._stations.forEach( st ->
		{
			if (st.isValid() && st.owner.isValid()) DeleteStation(st);
		});
	}

	private void CheckOrphanedSlots(RoadStopType rst)
	{
		for(RoadStop rs : RoadStop.GetPrimaryRoadStop(this, rst))
			checkStop(rst, rs);
	}


	private void checkStop(RoadStopType rst, RoadStop rs) 
	{
		for (int k = 0; k < RoadStop.NUM_SLOTS; k++) 
		{
			if (rs.slot[k] != INVALID_SLOT) {
				final  Vehicle v = Vehicle.GetVehicle(rs.slot[k]);

				if (v.type != Vehicle.VEH_Road || v.road.slot != rs) {
					Global.DEBUG_ms( 0,
							"Multistop: Orphaned %s slot at 0x%X of station %d (don't panic)",
							(rst == RoadStopType.RS_BUS) ? "bus" : "truck", rs.xy, index);
					rs.slot[k] = INVALID_SLOT;
				}
			}
		}		
	}

	/* this function is called for one station each tick */
	private void StationHandleBigTick()
	{
		UpdateStationAcceptance(true);

		if (facilities == 0 && ++delete_ctr >= 8) DeleteStation(this);

		// Here we saveguard against orphaned slots
		CheckOrphanedSlots(RoadStopType.RS_BUS);
		CheckOrphanedSlots(RoadStopType.RS_TRUCK);
	}

	private void UpdateStationRating()
	{
		//GoodsEntry ge;
		int rating;
		//StationID index;
		int waiting;
		boolean waiting_changed = false;

		time_since_load = BitOps.byte_inc_sat_RET((byte) time_since_load);
		time_since_unload = BitOps.byte_inc_sat_RET((byte) time_since_unload);

		for( GoodsEntry ge : goods )
		{
			if (ge.enroute_from != INVALID_STATION) {
				ge.enroute_time = BitOps.byte_inc_sat_RET((byte) ge.enroute_time);
				ge.days_since_pickup = BitOps.inc_sat_RET(ge.days_since_pickup);

				rating = 0;

				{
					int b = ge.last_speed;
					if ((b-=85) >= 0)
						rating += b >> 2;
				}

				{
					int age = ge.last_age;
					if(age < 3)
					{
						rating += 10;
						if(age < 2)
						{
							rating += 10;
							if( age < 1)
								rating += 13;
						}
					}
				}

				if (owner.id < Global.MAX_PLAYERS && BitOps.HASBIT(town.statues, owner.id))
					rating += 26;

				{
					int days = ge.days_since_pickup;
					if (last_vehicle.id != INVALID_VEHICLE &&
							Vehicle.GetVehicle(last_vehicle).type == Vehicle.VEH_Ship)
						days >>= 2;
				if(days <= 21) 
				{
					rating += 25;
					if(days <= 12) 
					{
						rating += 25;
						if(days <= 6) {
							rating += 45;
							if(days <= 3) 
								rating += 35;
						}
					}
				}
				}

				{
					waiting = BitOps.GB(ge.waiting_acceptance, 0, 12);
					rating -= 90;
					if( waiting <= 1500) 
					{
						rating += 55;
						if(waiting <= 1000)
						{
							rating += 35;
							if(waiting <= 600) {
								rating += 10;
								if( waiting <= 300) {
									rating += 20;
									if(waiting <= 100) 
										rating += 10;
								}
							}
						}
					}
				}

				{
					int or = ge.rating; // old rating

					// only modify rating in steps of -2, -1, 0, 1 or 2
					ge.rating = (rating = or + BitOps.clamp(BitOps.clamp(rating, 0, 255) - or, -2, 2));

					// if rating is <= 64 and more than 200 items waiting, remove some random amount of goods from the station
					if (rating <= 64 && waiting >= 200) {
						int dec = Hal.Random() & 0x1F;
						if (waiting < 400) dec &= 7;
						waiting -= dec + 1;
						waiting_changed = true;
					}

					// if rating is <= 127 and there are any items waiting, maybe remove some goods.
					if (rating <= 127 && waiting != 0) {
						int r = Hal.Random();
						if ( rating <= (r & 0x7F) ) {
							waiting = Math.max(waiting - ((r >> 8)&3) - 1, 0);
							waiting_changed = true;
						}
					}

					if (waiting_changed) 
						ge.waiting_acceptance = BitOps.RETSB(ge.waiting_acceptance, 0, 12, waiting);
				}
			}
		} //while (++ge != endof(st.goods));

		StationID id = StationID.get( index );

		if (waiting_changed)
			Window.InvalidateWindow(Window.WC_STATION_VIEW, id);
		else
			Window.InvalidateWindowWidget(Window.WC_STATION_VIEW, id.id, 5);
	}

	/* called for every station each tick */
	private void StationHandleSmallTick()
	{
		if (facilities == 0) return;

		int b = delete_ctr + 1;
		if (b >= 185) b = 0;
		delete_ctr = b;

		if (b == 0) UpdateStationRating();
	}

	public static void OnTick_Station()
	{
		int i;
		Station st;

		if (Global._game_mode == GameModes.GM_EDITOR) return;

		i = _station_tick_ctr;
		if (++_station_tick_ctr == GetStationPoolSize()) _station_tick_ctr = 0;

		st = GetStation(i);
		if (st != null && st.isValid()) st.StationHandleBigTick();

		Global.gs._stations.forEachValid( sst -> sst.StationHandleSmallTick() );
	}

	public static void StationMonthlyLoop()
	{
		/* is empty */
	}


	public static void ModifyStationRatingAround(TileIndex tile, PlayerID owner, int amount, int radius)
	{
		Global.gs._stations.forEach( st ->
		{
			if (st.isValid() && st.owner.equals(owner) &&
					Map.DistanceManhattan(tile, st.xy) <= radius) {

				for (int i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
					GoodsEntry ge = st.goods[i];

					if (ge.enroute_from != INVALID_STATION) {
						ge.rating = BitOps.clamp(ge.rating + amount, 0, 255);
					}
				}
			}
		});
	}

	private static void UpdateStationWaiting(Station st, int type, int amount)
	{
		st.goods[type].waiting_acceptance = BitOps.RETSB(st.goods[type].waiting_acceptance, 0, 12,
				Math.min(0xFFF, BitOps.GB(st.goods[type].waiting_acceptance, 0, 12) + amount)
				);

		st.goods[type].enroute_time = 0;
		st.goods[type].enroute_from = st.index;
		Window.InvalidateWindow(Window.WC_STATION_VIEW, st.index);
	}

	/** Rename a station
	 * @param x,y unused
	 * @param p1 station ID that is to be renamed
	 * @param p2 unused
	 */
	public static int CmdRenameStation(int x, int y, int flags, int p1, int p2)
	{
		StringID str;
		Station st;

		if (!IsStationIndex(p1) || Global._cmd_text == null) return Cmd.CMD_ERROR;
		st = GetStation(p1);

		if (!st.IsValidStation() || !Player.CheckOwnership(st.owner)) return Cmd.CMD_ERROR;

		str = Global.AllocateNameUnique(Global._cmd_text, 6);
		//if (str == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			//StringID 
			int old_str = st.string_id;

			st.string_id = str.id;
			st.UpdateStationVirtCoord();
			Global.DeleteName(old_str);
			StationGui.requestSortStations(st.owner.id); // rename a station
			Hal.MarkWholeScreenDirty();
		} else {
			Global.DeleteName(str);
		}

		return 0;
	}


	private static class SearchState
	{
		int rad = 0;
		int x_dist, y_dist;
		int x_min_prod, x_max_prod;     //min and max coordinates of the producer
		int y_min_prod, y_max_prod;     //relative
		int w_prod=0, h_prod=0; //width and height of the "producer" of the cargo		
	}

	public static int MoveGoodsToStation(TileIndex tile, int w, int h, int type, int amount)
	{
		Station around_ptr[] = new Station[8];
		//StationID [] around = new StationID[8];
		int [] around = new int[8];
		//StationID st_index;
		//int i;
		int moved;
		//int best_rating, best_rating2;
		//Station st1, st2;
		int t;
		//int rad=0;
		int max_rad;

		final SearchState ss = new SearchState();

		//memset(around, 0xff, sizeof(around));
		Arrays.fill(around, INVALID_STATION);

		if (Global._patches.modified_catchment) {
			ss.w_prod = w;
			ss.h_prod = h;
			w += 16;
			h += 16;
			max_rad = 8;
		} else {
			w += 8;
			h += 8;
			max_rad = 4;
		}

		//BEGIN_TILE_LOOP(cur_tile, w, h, tile - TileDiffXY(max_rad, max_rad))
		TileIndex.forEach(w, h, tile.isub(max_rad, max_rad).tile, (cur_tile,h_cur,w_cur) -> 
		{
			//Station st;

			cur_tile.TILE_MASK();

			if(!cur_tile.IsTileType( TileTypes.MP_STATION))
				return false;


			int st_index = cur_tile.getMap().m2;
			for (int i = 0; i != 8; i++) 
			{
				if (around[i] == INVALID_STATION) 
				{
					Station st = GetStation(st_index);
					if (!st.IsBuoy() &&
							( 0==st.town.exclusive_counter || (st.town.exclusivity.equals(st.owner) ) ) && // check exclusive transport rights
							st.goods[type].rating != 0 &&
							(!Global._patches.selectgoods || 0!=st.goods[type].last_speed) && // if last_speed is 0, no vehicle has been there.
							((st.facilities & (byte)~FACIL_BUS_STOP)!=0 || type==AcceptedCargo.CT_PASSENGERS) && // if we have other fac. than a bus stop, or the cargo is passengers
							((st.facilities & (byte)~FACIL_TRUCK_STOP)!=0 || type!=AcceptedCargo.CT_PASSENGERS)) 
					{ // if we have other fac. than a cargo bay or the cargo is not passengers
						if (Global._patches.modified_catchment) 
						{
							ss.rad = st.FindCatchmentRadius();
							ss.x_min_prod = ss.y_min_prod = 9;
							ss.x_max_prod = 8 + ss.w_prod;
							ss.y_max_prod = 8 + ss.h_prod;

							ss.x_dist = Math.min(w_cur - ss.x_min_prod, ss.x_max_prod - w_cur);

							if (w_cur < ss.x_min_prod) 
							{
								ss.x_dist = ss.x_min_prod - w_cur;
							} else 
							{        //save cycles
								if (w_cur > ss.x_max_prod) ss.x_dist = w_cur - ss.x_max_prod;
							}

							ss.y_dist = Math.min(h_cur - ss.y_min_prod, ss.y_max_prod - h_cur);
							if (h_cur < ss.y_min_prod) 
							{
								ss.y_dist = ss.y_min_prod - h_cur;
							} else 
							{
								if (h_cur > ss.y_max_prod) ss.y_dist = h_cur - ss.y_max_prod;
							}

						} else 
						{
							ss.x_dist = ss.y_dist = 0;
						}

						if ( !(ss.x_dist > ss.rad) && !(ss.y_dist > ss.rad) ) {

							around[i] = st_index;
							around_ptr[i] = st;
						}
					}
					break;
				} else if (around[i] == st_index)
					break;
			}

			return false;
		});
		//END_TILE_LOOP(cur_tile, w, h, tile - TileDiffXY(max_rad, max_rad))

		/* no stations around at all? */
		if (around[0] == INVALID_STATION)
			return 0;

		if (around[1] == INVALID_STATION) {
			/* only one station around */
			moved = (amount * around_ptr[0].goods[type].rating >> 8) + 1;
			UpdateStationWaiting(around_ptr[0], type, moved);
			return moved;
		}

		/* several stations around, find the two with the highest rating */
		Station st1 = null;
		Station st2 = null;

		int best_rating, best_rating2;
		best_rating = best_rating2 = 0;

		for( int i = 0; i != 8 && around[i] != INVALID_STATION; i++) {
			if (around_ptr[i].goods[type].rating >= best_rating) {
				best_rating2 = best_rating;
				st2 = st1;

				best_rating = around_ptr[i].goods[type].rating;
				st1 = around_ptr[i];
			} else if (around_ptr[i].goods[type].rating >= best_rating2) {
				best_rating2 = around_ptr[i].goods[type].rating;
				st2 = around_ptr[i];
			}
		}

		assert(st1 != null);
		assert(st2 != null);
		assert(best_rating != 0 || best_rating2 != 0);

		/* the 2nd highest one gets a penalty */
		best_rating2 >>= 1;

				/* amount given to station 1 */
				t = (best_rating * (amount + 1)) / (best_rating + best_rating2);

				moved = 0;
				if (t != 0) {
					moved = (t * best_rating >> 8) + 1;
					amount -= t;
					UpdateStationWaiting(st1, type, moved);
				}

				if (amount != 0) {
					moved += (amount = (amount * best_rating2 >> 8) + 1);
					UpdateStationWaiting(st2, type, amount);
				}

				return moved;
	}

	public static void BuildOilRig(TileIndex tile)
	{
		int j;
		Station st = AllocateStation();

		if (st == null) {
			Global.DEBUG_misc( 0, "Couldn't allocate station for oilrig at %#X, reverting to oilrig only...", tile.getTile());
			return;
		}
		if (!GenerateStationName(st, tile, 2)) {
			Global.DEBUG_misc( 0, "Couldn't allocate station-name for oilrig at %#X, reverting to oilrig only...", tile.getTile());
			return;
		}

		st.town = Town.ClosestTownFromTile(tile, -1);
		st.sign.setWidth_1(0);

		tile.SetTileType(TileTypes.MP_STATION);
		tile.SetTileOwner(Owner.OWNER_NONE);
		tile.getMap().m2 = st.index;
		tile.getMap().m3 = 0;
		tile.getMap().m4 = 0;
		tile.getMap().m5 = 0x4B;

		st.owner = PlayerID.getNone();
		st.airport_flags = 0;
		st.airport_type = Airport.AT_OILRIG;
		st.xy = tile;
		//st.bus_stops = new Array;
		//st.truck_stops = null;
		st.airport_tile = tile;
		st.dock_tile = tile;
		st.train_tile = null;
		st.had_vehicle_of_type = 0;
		st.time_since_load = 255;
		st.time_since_unload = 255;
		st.delete_ctr = 0;
		st.last_vehicle = VehicleID.getInvalid();
		st.facilities = FACIL_AIRPORT | FACIL_DOCK;
		st.build_date = Global.get_date();

		for (j = 0; j != AcceptedCargo.NUM_CARGO; j++) 
		{
			st.goods[j] = new GoodsEntry();
			st.goods[j].waiting_acceptance = 0;
			st.goods[j].days_since_pickup = 0;
			st.goods[j].enroute_from = INVALID_STATION;
			st.goods[j].rating = 175;
			st.goods[j].last_speed = 0;
			st.goods[j].last_age = 255;
		}

		st.UpdateStationVirtCoordDirty();
		st.UpdateStationAcceptance(false);
	}

	public static void DeleteOilRig(TileIndex tile)
	{
		Station st = GetStation(tile.getMap().m2);

		Landscape.DoClearSquare(tile);

		st.dock_tile = null;
		st.airport_tile = null;
		st.facilities &= ~(FACIL_AIRPORT | FACIL_DOCK);
		st.airport_flags = 0;
		st.UpdateStationVirtCoordDirty();
		DeleteStation(st);
	}

	private static void ChangeTileOwner_Station(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		if (!tile.IsTileOwner(old_player)) return;

		if (new_player.id != Owner.OWNER_SPECTATOR) {
			Station st = GetStation(tile.getMap().m2);
			tile.SetTileOwner(new_player);
			st.owner = new_player;
			StationGui.requestSortStations(); // transfer ownership of station to another player
			Window.InvalidateWindowClasses(Window.WC_STATION_LIST);
		} else {
			Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
		}
	}

	private static int ClearTile_Station(TileIndex tile, int flags)
	{
		int m5 = tile.getMap().m5;
		Station st;

		if( 0 != (flags & Cmd.DC_AUTO)) {
			if (m5 < 8) return Cmd.return_cmd_error(Str.STR_300B_MUST_DEMOLISH_RAILROAD);
			if (m5 < 0x43 || (m5 >= 83 && m5 <= 114)) return Cmd.return_cmd_error(Str.STR_300E_MUST_DEMOLISH_AIRPORT_FIRST);
			if (m5 < 0x47) return Cmd.return_cmd_error(Str.STR_3047_MUST_DEMOLISH_TRUCK_STATION);
			if (m5 < 0x4B) return Cmd.return_cmd_error(Str.STR_3046_MUST_DEMOLISH_BUS_STATION);
			if (m5 == 0x52) return Cmd.return_cmd_error(Str.STR_306A_BUOY_IN_THE_WAY);
			if (m5 != 0x4B && m5 < 0x53) return Cmd.return_cmd_error(Str.STR_304D_MUST_DEMOLISH_DOCK_FIRST);
			Global.SetDParam(0, Str.STR_4807_OIL_RIG);
			return Cmd.return_cmd_error(Str.STR_4800_IN_THE_WAY);
		}

		st = GetStation(tile.getMap().m2);

		if (m5 < 8) return RemoveRailroadStation(st, tile, flags);
		// original airports < 67, new airports between 83 - 114
		if (m5 < 0x43 || (m5 >= 83 && m5 <= 114)) return RemoveAirport(st, flags);
		if (m5 < 0x4B) return st.RemoveRoadStop(flags, tile);
		if (m5 == 0x52) return st.RemoveBuoy(flags);
		if (m5 != 0x4B && m5 < 0x53) return st.RemoveDock(flags);

		return Cmd.CMD_ERROR;
	}

	public static void InitializeStations()
	{
		/* Clean the station pool and create 1 block in it */
		Global.gs._stations.CleanPool();
		Global.gs._stations.AddBlockToPool();

		/* Clean the roadstop pool and create 1 block in it */
		//CleanPool(&_roadstop_pool);
		//AddBlockToPool(&_roadstop_pool);

		_station_tick_ctr = 0;

		// set stations to be sorted on load of savegame
		//memset(StationGui._station_sort_dirty, true, sizeof(_station_sort_dirty));
		StationGui.requestSortStations(); // load of savegame
	}


	final static  TileTypeProcs _tile_type_station_procs = new TileTypeProcs(
			Station::DrawTile_Station,						/* draw_tile_proc */
			Station::GetSlopeZ_Station,					/* get_slope_z_proc */
			Station::ClearTile_Station,					/* clear_tile_proc */
			Station::GetAcceptedCargo_Station,		/* get_accepted_cargo_proc */
			Station::GetTileDesc_Station,				/* get_tile_desc_proc */
			Station::GetTileTrackStatus_Station,	/* get_tile_track_status_proc */
			Station::ClickTile_Station,					/* click_tile_proc */
			Station::AnimateTile_Station,				/* animate_tile_proc */
			Station::TileLoop_Station,						/* tile_loop_clear */
			Station::ChangeTileOwner_Station,		/* change_tile_owner_clear */
			null,												/* get_produced_cargo_proc */
			Station::VehicleEnter_Station,				/* vehicle_enter_tile_proc */
			null,												/* vehicle_leave_tile_proc */
			Station::GetSlopeTileh_Station			/* get_slope_tileh_proc */
			);



	/**
	 * Check if a station really exists.
	 */
	public boolean IsValidStation()
	{
		return xy != null;
	}

	/**
	 * Check if a station really exists.
	 */
	public boolean isValid()
	{
		return xy != null;
	}
	
	public boolean IsBuoy()
	{
		return 0 != (had_vehicle_of_type & HVOT_BUOY); /* XXX: We should really ditch this ugly coding and switch to something sane... */
	}

	/*public static void forEach(BiConsumer<Integer, Station> c) 
	{
		Global.gs._stations.forEach(c);

	}*/

	public static Iterator<Station> getIterator()
	{
		return Global.gs._stations.getIterator(); //pool.values().iterator();
	}

	public static void forEach( Consumer<Station> c )
	{
		Global.gs._stations.forEach(c);
	}

	public static void forEachValid( Consumer<Station> c )
	{
		Global.gs._stations.forEachValid(c);
	}




	/*
	{
		Station::DrawTile_Station,						// draw_tile_proc 
		Station::GetSlopeZ_Station,					// get_slope_z_proc 
		Station::ClearTile_Station,					// clear_tile_proc 
		Station::GetAcceptedCargo_Station,		// get_accepted_cargo_proc 
		Station::GetTileDesc_Station,				// get_tile_desc_proc 
		Station::GetTileTrackStatus_Station,	// get_tile_track_status_proc 
		Station::ClickTile_Station,					// click_tile_proc 
		Station::AnimateTile_Station,				// animate_tile_proc 
		Station::TileLoop_Station,						// tile_loop_clear 
		Station::ChangeTileOwner_Station,		// change_tile_owner_clear 
		null,												// get_produced_cargo_proc 
		Station::VehicleEnter_Station,				// vehicle_enter_tile_proc 
		null,												// vehicle_leave_tile_proc 
		Station::GetSlopeTileh_Station,			// get_slope_tileh_proc 
	};
	/*
	static final  SaveLoad _roadstop_desc[] = {
		SLE_VAR(RoadStop,xy,           SLE_UINT32),
		SLE_VAR(RoadStop,used,         SLE_UINT8),
		SLE_VAR(RoadStop,status,       SLE_UINT8),
		// Index was saved in some versions, but this is not needed 
		SLE_CONDARR(NullStruct,null,SLE_FILE_U32 | SLE_VAR_NULL, 1, 0, 8),
		SLE_VAR(RoadStop,station,      SLE_UINT16),
		SLE_VAR(RoadStop,type,         SLE_UINT8),

		SLE_REF(RoadStop,next,         REF_ROADSTOPS),
		SLE_REF(RoadStop,prev,         REF_ROADSTOPS),

		SLE_ARR(RoadStop,slot,         SLE_UINT16, NUM_SLOTS),

		SLE_END()
	};

	static final  SaveLoad _station_desc[] = {
		SLE_CONDVAR(Station, xy,           SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Station, xy,           SLE_UINT32, 6, 255),
		SLE_CONDVAR(Station, bus_tile_obsolete,    SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Station, lorry_tile_obsolete,  SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Station, train_tile,   SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Station, train_tile,   SLE_UINT32, 6, 255),
		SLE_CONDVAR(Station, airport_tile, SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Station, airport_tile, SLE_UINT32, 6, 255),
		SLE_CONDVAR(Station, dock_tile,    SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Station, dock_tile,    SLE_UINT32, 6, 255),
		SLE_REF(Station,town,						REF_TOWN),
		SLE_VAR(Station,trainst_w,			SLE_UINT8),
		SLE_CONDVAR(Station,trainst_h,	SLE_UINT8, 2, 255),

		// alpha_order was stored here in savegame format 0 - 3
		SLE_CONDARR(NullStruct,null,SLE_FILE_U8 | SLE_VAR_NULL, 1, 0, 3),

		SLE_VAR(Station,string_id,			SLE_STRINGID),
		SLE_VAR(Station,had_vehicle_of_type,SLE_UINT16),

		SLE_VAR(Station,time_since_load,		SLE_UINT8),
		SLE_VAR(Station,time_since_unload,	SLE_UINT8),
		SLE_VAR(Station,delete_ctr,					SLE_UINT8),
		SLE_VAR(Station,owner,							SLE_UINT8),
		SLE_VAR(Station,facilities,					SLE_UINT8),
		SLE_VAR(Station,airport_type,				SLE_UINT8),

		// truck/bus_stop_status was stored here in savegame format 0 - 6
		SLE_CONDVAR(Station,truck_stop_status_obsolete,	SLE_UINT8, 0, 5),
		SLE_CONDVAR(Station,bus_stop_status_obsolete,		SLE_UINT8, 0, 5),

		// blocked_months was stored here in savegame format 0 - 4.0
		SLE_CONDVAR(Station,blocked_months_obsolete,	SLE_UINT8, 0, 4),

		SLE_CONDVAR(Station,airport_flags,			SLE_VAR_U32 | SLE_FILE_U16, 0, 2),
		SLE_CONDVAR(Station,airport_flags,			SLE_UINT32, 3, 255),

		SLE_VAR(Station,last_vehicle,				SLE_UINT16),

		SLE_CONDVAR(Station,class_id,				SLE_UINT8, 3, 255),
		SLE_CONDVAR(Station,stat_id,				SLE_UINT8, 3, 255),
		SLE_CONDVAR(Station,build_date,			SLE_UINT16, 3, 255),

		SLE_CONDREF(Station,bus_stops,					REF_ROADSTOPS, 6, 255),
		SLE_CONDREF(Station,truck_stops,				REF_ROADSTOPS, 6, 255),

		// reserve extra space in savegame here. (currently 28 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U8 | SLE_VAR_NULL, 32, 2, 255),

		SLE_END()
	};

	static final  SaveLoad _goods_desc[] = {
		SLE_VAR(GoodsEntry,waiting_acceptance,SLE_UINT16),
		SLE_VAR(GoodsEntry,days_since_pickup,	SLE_UINT8),
		SLE_VAR(GoodsEntry,rating,						SLE_UINT8),
		SLE_CONDVAR(GoodsEntry,enroute_from,			SLE_FILE_U8 | SLE_VAR_U16, 0, 6),
		SLE_CONDVAR(GoodsEntry,enroute_from,			SLE_UINT16, 7, 255),
		SLE_VAR(GoodsEntry,enroute_time,			SLE_UINT8),
		SLE_VAR(GoodsEntry,last_speed,				SLE_UINT8),
		SLE_VAR(GoodsEntry,last_age,					SLE_UINT8),
		SLE_CONDVAR(GoodsEntry,feeder_profit,			SLE_INT32, 14, 255),

		SLE_END()
	};


	static void SaveLoad_STNS(Station st)
	{
		int i;

		SlObject(st, _station_desc);
		for (i = 0; i != NUM_CARGO; i++) {
			SlObject(&st.goods[i], _goods_desc);

			// In older versions, enroute_from had 0xFF as INVALID_STATION, is now 0xFFFF 
			if (CheckSavegameVersion(7) && st.goods[i].enroute_from == 0xFF) {
				st.goods[i].enroute_from = INVALID_STATION;
			}
		}
	}

	static void Save_STNS()
	{
		Station st;
		// Write the stations
		FOR_ALL_STATIONS(st) {
			if (st.xy != 0) {
				SlSetArrayIndex(st.index);
				SlAutolength((AutolengthProc*)SaveLoad_STNS, st);
			}
		}
	}

	static void Load_STNS()
	{
		int index;
		while ((index = SlIterateArray()) != -1) {
			Station st;

			if (!AddBlockIfNeeded(&_station_pool, index))
				error("Stations: failed loading savegame: too many stations");

			st = GetStation(index);
			SaveLoad_STNS(st);

			// this means it's an oldstyle savegame without support for nonuniform stations
			if (st.train_tile != 0 && st.trainst_h == 0) {
				int w = BitOps.GB(st.trainst_w, 4, 4);
				int h = BitOps.GB(st.trainst_w, 0, 4);

				if (_m[st.train_tile].m5 & 1) intswap(w, h);
				st.trainst_w = w;
				st.trainst_h = h;
			}

			//* In older versions, we had just 1 tile for a bus/lorry, now we have more..
			// *  convert, if needed 
			if (CheckSavegameVersion(6)) {
				if (st.bus_tile_obsolete != 0) {
					st.bus_stops = AllocateRoadStop();
					if (st.bus_stops == null)
						error("Station: too many busstations in savegame");

					InitializeRoadStop(st.bus_stops, null, st.bus_tile_obsolete, st.index);
				}
				if (st.lorry_tile_obsolete != 0) {
					st.truck_stops = AllocateRoadStop();
					if (st.truck_stops == null)
						error("Station: too many truckstations in savegame");

					InitializeRoadStop(st.truck_stops, null, st.lorry_tile_obsolete, st.index);
				}
			}

		st.airport_queue = new_VQueue();
		st.helicopter_queue = new_VQueue();
		}

		//* This is to ensure all pointers are within the limits of _stations_size 
		if (_station_tick_ctr > GetStationPoolSize()) _station_tick_ctr = 0;
	}

	static void Save_ROADSTOP()
	{
		RoadStop rs;

		FOR_ALL_ROADSTOPS(rs) {
			if (rs.used) {
				SlSetArrayIndex(rs.index);
				SlObject(rs, _roadstop_desc);
			}
		}
	}

	static void Load_ROADSTOP()
	{
		int index;

		while ((index = SlIterateArray()) != -1) {
			RoadStop rs;

			if (!AddBlockIfNeeded(&_roadstop_pool, index))
				error("RoadStops: failed loading savegame: too many RoadStops");

			rs = GetRoadStop(index);
			SlObject(rs, _roadstop_desc);
		}
	}

	final  ChunkHandler _station_chunk_handlers[] = {
		{ 'STNS', Save_STNS,      Load_STNS,      CH_ARRAY },
		{ 'ROAD', Save_ROADSTOP,  Load_ROADSTOP,  CH_ARRAY | CH_LAST},
	};
	 */

	public static void loadGame(ObjectInputStream oin) throws ClassNotFoundException, IOException
	{
		//_station_pool = (MemoryPool<Station>) oin.readObject();

	}

	public static void saveGame(ObjectOutputStream oos) throws IOException 
	{
		//oos.writeObject(_station_pool);		
	}


	public boolean hasFacility(int f) {
		return 0 != (facilities & f);
	}


	public PlayerID getOwner() { return owner; }
	public int getIndex() { return index; }
	public int getFacilities() { return facilities; }
	public ViewportSign getSign() {		return sign;	}
	public int getBuild_date() {		return build_date;	}


	public boolean hasNoFacilities() { return 0 == facilities; }

	// NB! Bit mask!

	public boolean hasAirportBlocks(int block) {
		return BitOps.HASBITS(airport_flags, block);
	}

	public void resetAirportBlocks(int blocks) {
		airport_flags = BitOps.RETCLRBITS(airport_flags, blocks);
	}

	public void setAirportBlocks(int blocks) {
		airport_flags = BitOps.RETSETBITS(airport_flags, blocks);		
	}

	// NB! Bit number!

	public boolean hasAirportBlock(int i) {
		return BitOps.HASBIT(airport_flags, i);
	}

	public void setAirportBlock(int i) {
		airport_flags = BitOps.RETSETBIT(airport_flags, i); // occupy 
	}







}




class ottd_Rectangle {
	int min_x;
	int min_y;
	int max_x;
	int max_y;


	void MergePoint(TileIndex tile)
	{
		int x = tile.TileX();
		int y = tile.TileY();

		if (min_x > x) min_x = x;
		if (min_y > y) min_y = y;
		if (max_x < x) max_x = x;
		if (max_y < y) max_y = y;
	}

}

