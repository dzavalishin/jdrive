import game.util.BitOps;

public class Station
{

	TileIndex xy;
	RoadStop bus_stops;
	RoadStop truck_stops;
	TileIndex train_tile;
	TileIndex airport_tile;
	TileIndex dock_tile;
	Town town;
	int string_id;

	ViewportSign sign;

	int had_vehicle_of_type;

	byte time_since_load;
	byte time_since_unload;
	byte delete_ctr;
	PlayerID owner;
	byte facilities;
	byte airport_type;

	// trainstation width/height
	byte trainst_w, trainst_h;

	byte class_id; // custom graphics station class
	byte stat_id; // custom graphics station id in the @class_id class
	int16 build_date;

	//int16 airport_flags;
	int airport_flags;
	StationID index;
	VehicleQueue airport_queue;			// airport queue
	VehicleQueue helicopter_queue;			// airport queue

	VehicleID last_vehicle;
	GoodsEntry goods[] = new GoodsEntry[NUM_CARGO];

	// Stuff that is no longer used, but needed for conversion 
	//TileIndex bus_tile_obsolete;
	//TileIndex lorry_tile_obsolete;

	//byte truck_stop_status_obsolete;
	//byte bus_stop_status_obsolete;
	//byte blocked_months_obsolete;




    private void StationInitialize(TileIndex tile)
    {
        GoodsEntry ge;
    
        xy = tile;
        airport_tile = dock_tile = train_tile = 0;
        bus_stops = truck_stops = NULL;
        had_vehicle_of_type = 0;
        time_since_load = 255;
        time_since_unload = 255;
        delete_ctr = 0;
        facilities = 0;
    
        last_vehicle = INVALID_VEHICLE;
    
        for (ge = goods; ge != endof(goods); ge++) {
            ge.waiting_acceptance = 0;
            ge.days_since_pickup = 0;
            ge.enroute_from = INVALID_STATION;
            ge.rating = 175;
            ge.last_speed = 0;
            ge.last_age = 0xFF;
            ge.feeder_profit = 0;
        }
    
        airport_queue = new_VQueue();
        helicopter_queue = new_VQueue();
    
        _global_station_sort_dirty = true; // build a new station
    }
    
    // Update the virtual coords needed to draw the station sign.
    // st = Station to update for.
    private void UpdateStationVirtCoord()
    {
        Point pt = RemapCoords2(TileX(xy) * 16, TileY(xy) * 16);
    
        pt.y -= 32;
        if (facilities & FACIL_AIRPORT && airport_type == AT_OILRIG) pt.y -= 16;
    
        SetDParam(0, index);
        SetDParam(1, facilities);
        UpdateViewportSignPos(sign, pt.x, pt.y, STR_305C_0);
    }

    
    // Update the virtual coords needed to draw the station sign for all stations.
void UpdateAllStationVirtCoord()
{
	Station st;

	FOR_ALL_STATIONS(st) {
		if (st.xy != 0) st.UpdateStationVirtCoord();
	}
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

	for (i = 0; i != NUM_CARGO; i++) {
		if (goods[i].waiting_acceptance & 0x8000) mask |= 1 << i;
	}
	return mask;
}

// Items contains the two cargo names that are to be accepted or rejected.
// msg is the string id of the message to display.
private void ShowRejectOrAcceptNews(int items, StringID msg)
{
	if (items) {
		SetDParam(2, BitOps.GB(items, 16, 16));
		SetDParam(1, BitOps.GB(items,  0, 16));
		SetDParam(0, index);
		AddNewsItem(msg + ((items >> 16)?1:0), NEWS_FLAGS(NM_SMALL, NF_VIEWPORT|NF_TILE, NT_ACCEPTANCE, 0), xy, 0);
	}
}




// Get a list of the cargo types being produced around the tile.
static void GetProductionAroundTiles(AcceptedCargo produced, TileIndex tile0,
	int w, int h, int rad)
{
	int x,y;
	int x1,y1,x2,y2;
	int xc,yc;

	memset(produced, 0, sizeof(AcceptedCargo));

	x = TileX(tile0);
	y = TileY(tile0);

	// expand the region by rad tiles on each side
	// while making sure that we remain inside the board.
	x2 = min(x + w + rad, MapSizeX());
	x1 = max(x - rad, 0);

	y2 = min(y + h + rad, MapSizeY());
	y1 = max(y - rad, 0);

	assert(x1 < x2);
	assert(y1 < y2);
	assert(w > 0);
	assert(h > 0);

	for (yc = y1; yc != y2; yc++) {
		for (xc = x1; xc != x2; xc++) {
			if (!(IS_INSIDE_1D(xc, x, w) && IS_INSIDE_1D(yc, y, h))) {
				GetProducedCargoProc *gpc;
				TileIndex tile1 = new TileIndex(xc, yc);

				gpc = _tile_type_procs[GetTileType(tile1)].get_produced_cargo_proc;
				if (gpc != NULL) {
					byte cargos[2] = { CT_INVALID, CT_INVALID };

					gpc(tile1, cargos);
					if (cargos[0] != CT_INVALID) {
						produced[cargos[0]]++;
						if (cargos[1] != CT_INVALID) {
							produced[cargos[1]]++;
						}
					}
				}
			}
		}
	}
}

// Get a list of the cargo types that are accepted around the tile.
static void GetAcceptanceAroundTiles(AcceptedCargo accepts, TileIndex tile0,
	int w, int h, int rad)
{
	int x,y;
	int x1,y1,x2,y2;
	int xc,yc;

	memset(accepts, 0, sizeof(AcceptedCargo));

	x = TileX(tile0);
	y = TileY(tile0);

	// expand the region by rad tiles on each side
	// while making sure that we remain inside the board.
	x2 = min(x + w + rad, MapSizeX());
	y2 = min(y + h + rad, MapSizeY());
	x1 = max(x - rad, 0);
	y1 = max(y - rad, 0);

	assert(x1 < x2);
	assert(y1 < y2);
	assert(w > 0);
	assert(h > 0);

	for (yc = y1; yc != y2; yc++) {
		for (xc = x1; xc != x2; xc++) {
			TileIndex tile1 = new TileIndex(xc, yc);

			if (!IsTileType(tile1, MP_STATION)) {
				AcceptedCargo ac;
				int i;

				GetAcceptedCargo(tile1, ac);
				for (i = 0; i < lengthof(ac); ++i) accepts[i] += ac[i];
			}
		}
	}
}





// Update the acceptance for a station.
// show_msg controls whether to display a message that acceptance was changed.
private void UpdateStationAcceptance(boolean show_msg)
{
	uint old_acc, new_acc;
	RoadStop cur_rs;
	int i;
	ottd_Rectangle rect;
	int rad;
	AcceptedCargo accepts;

	rect.min_x = MapSizeX();
	rect.min_y = MapSizeY();
	rect.max_x = rect.max_y = 0;
	// Don't update acceptance for a buoy
	if (IsBuoy()) return;

	/* old accepted goods types */
	old_acc = GetAcceptanceMask();

	// Put all the tiles that span an area in the table.
	if (train_tile != 0) {
		rect.MergePoint(train_tile);
		rect.MergePoint(
			train_tile + TileDiffXY(trainst_w - 1, trainst_h - 1)
		);
	}

	if (airport_tile != 0) {
		rect.MergePoint( airport_tile);
		rect.MergePoint(
			airport_tile + TileDiffXY(
				_airport_size_x[airport_type] - 1,
				_airport_size_y[airport_type] - 1
			)
		);
	}

	if (dock_tile != 0) rect.MergePoint( dock_tile);

	for (cur_rs = bus_stops; cur_rs != NULL; cur_rs = cur_rs.next) {
		rect.MergePoint( cur_rs.xy);
	}

	for (cur_rs = truck_stops; cur_rs != NULL; cur_rs = cur_rs.next) {
		rect.MergePoint( cur_rs.xy);
	}

	rad = (_patches.modified_catchment) ? FindCatchmentRadius(st) : 4;

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
		memset(accepts, 0, sizeof(accepts));
	}

	// Adjust in case our station only accepts fewer kinds of goods
	for (i = 0; i != NUM_CARGO; i++) {
		uint amt = min(accepts[i], 15);

		// Make sure the station can accept the goods type.
		if ((i != CT_PASSENGERS && !(facilities & (byte)~FACIL_BUS_STOP)) ||
				(i == CT_PASSENGERS && !(facilities & (byte)~FACIL_TRUCK_STOP)))
			amt = 0;

		SB(goods[i].waiting_acceptance, 12, 4, amt);
	}

	// Only show a message in case the acceptance was actually changed.
	new_acc = GetAcceptanceMask(st);
	if (old_acc == new_acc)
		return;

	// show a message to report that the acceptance was changed?
	if (show_msg && owner == _local_player && facilities) {
		uint32 accept=0, reject=0; /* these contain two string ids each */
		final StringID str = _cargoc.names_s;

		do {
			if (new_acc & 1) {
				if (!(old_acc & 1)) accept = (accept << 16) | *str;
			} else {
				if (old_acc & 1) reject = (reject << 16) | *str;
			}
		} while (str++,(new_acc>>=1) != (old_acc>>=1));

		ShowRejectOrAcceptNews(st, accept, STR_3040_NOW_ACCEPTS);
		ShowRejectOrAcceptNews(st, reject, STR_303E_NO_LONGER_ACCEPTS);
	}

	// redraw the station view since acceptance changed
	InvalidateWindowWidget(WC_STATION_VIEW, index, 4);
}

// This is called right after a station was deleted.
// It checks if the whole station is free of substations, and if so, the station will be
// deleted after a little while.
private void DeleteStationIfEmpty()
{
	if (facilities == 0) {
		delete_ctr = 0;
		InvalidateWindow(WC_STATION_LIST, owner);
	}
}



}




class ottd_Rectangle {
	uint min_x;
	uint min_y;
	uint max_x;
	uint max_y;


    void MergePoint(TileIndex tile)
    {
	uint x = TileX(tile);
	uint y = TileY(tile);

	if (min_x > x) min_x = x;
	if (min_y > y) min_y = y;
	if (max_x < x) max_x = x;
	if (max_y < y) max_y = y;
    }

}

