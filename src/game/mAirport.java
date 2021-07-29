package game;

import java.util.Iterator;

public class mAirport 
{

















	final static float PI = 3.141f; // duuuuuuuuuuhhhhhh
	final static int INT_AIRPORT_YEAR = 1990;
	final static int MA_MIN_POPULATION = 15000;
	final static int MA_MAX_DISTANCE_FROM_TOWN = 15;
	final static int MA_MIN_DISTANCE_FROM_TOWN = 10;
	final static int MA_MAX_AIRPORTS_PER_TOWN = 2;
	final static int MA_MAX_PLANES_QUOTA = 5;

	//Checks if a vehicle serves an MS returns the amount that it serves 
	static int MA_VehicleServesMS(Vehicle v)
	{
		Order order = v.orders;
		int stationcount = 0;
		Station st;

		while(order != null)
		{
			st = Station.GetStation(order.station);
			if(st.owner.id == Owner.OWNER_TOWN)
				stationcount++;

			order = order.next;
		}
		return stationcount;
	}

	// finds a municipal station in a vehicles order list the count
	// arg is for which municipal station in the list ie. 1st, 2nd 
	static StationID MA_Find_MS_InVehicleOrders(Vehicle v, int count)
	{
		Order order = v.orders;
		Station st;
		int stationcount = 0;

		while(order != null)
		{
			st = Station.GetStation(order.station);
			if(st.owner.id == Owner.OWNER_TOWN)
				stationcount++;
			if(stationcount == count) 
				return StationID.get(st.index);
			order = order.next;
		}
		return  StationID.getInvalid(); // Station.INVALID_STATION;
	}

	//checks if a plane is allowed to stop
	static boolean MA_VehicleIsAtMunicipalAirport(Vehicle v)
	{
		if(!Global._patches.allow_municipal_airports)
			return false;
		if(v.tile.GetTileOwner().owner == Owner.OWNER_TOWN)
			return true;

		return false;
	}	

	//does exactly what it says on the tin
	static void MA_DestroyAirport(Town tn)
	{
		Station.forEach( (st) ->
		{
			if(st.IsValidStation() 
				&& st.owner.id == Owner.OWNER_TOWN
				&& st.town == tn) { 
					Cmd.DoCommandByTile(st.xy, 0, 0, Cmd.DC_EXEC,Cmd.CMD_LANDSCAPE_CLEAR);
					tn.ExpandTown(); //this just builds over the gap thats left
			}
		});
	}

	//converts degrees to radians
	static float D2R(int degrees)
	{
		return (float) (degrees * (PI / 180.0));
	}
	/*
	//calculates tax
	void MA_Tax(int income, Vehicle v)
	{
		int old_expenses_type = Player._yearly_expenses_type;

		if(Global._patches.allow_municipal_airports) {
			float tax = 0;
			tax = (income / 100.0) * 20; //Global._patches.municipal_airports_tax;

			ShowCostOrIncomeAnimation(v.x_pos ,v.y_pos ,v.z_pos - 13, tax);

			switch(v.type) {

			case Vehicle.VEH_Aircraft:	
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_AIRCRAFT_RUN);
				break;
			case Vehicle.VEH_Train:		
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_TRAIN_RUN);
				break;
			case Vehicle.VEH_Ship:		
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_SHIP_RUN);				
				break;
			case Vehicle.VEH_Road:		
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_ROADVehicle.VEH_RUN);				
				break;

			}
			
			SubtractMoneyFromPlayer(tax);
			Player._yearly_expenses_type = old_expenses_type;
		}
		return;
	}
	*/
	//checks to see if we have used our quota at any municipal station;
	static boolean MA_WithinVehicleQuota(Station st)
	{
		//Vehicle v;
		int [] vehiclecount = { 0 };

		if(!Global._patches.allow_municipal_airports) 
			return true;

		if(st.owner.id != Owner.OWNER_TOWN)
			return true;

		//FOR_ALL_VEHICLES(v)
		Vehicle.forEach( (v) ->
		{
			Order order = v.orders;
			while(v.IsValidVehicle() && order != null)
			{
				if(v.IsValidVehicle()
					&& v.owner == Global._current_player
					&& Station.GetStation(order.station) == st
					&& st.owner.id == Owner.OWNER_TOWN) {
						vehiclecount[0]++;
						break;
				}
				order = order.next;
			}// while v.orders
		});//for all vehicles
		if(vehiclecount[0] >= MA_MAX_PLANES_QUOTA) return false;
		else return true;
	}

	//saves me changing too much code elsewhere
	boolean MA_OwnerHandler(PlayerID owner)
	{

		if(!Global._patches.allow_municipal_airports) 
			return false;
		
		return (owner.id == Owner.OWNER_TOWN);
		
	}

	//returns a position of a tile on the circuference of a circle;
	TileIndex CircularPos(int radius, int angle, TileIndex centre)
	{
		return TileIndex.TileXY(
				(int)(centre.TileX() + (Math.cos(D2R(angle)) * radius)), 
				(int)(centre.TileY() + (Math.sin(D2R(angle)) * radius)));
	}

	//checks if site is level and buildable
	boolean MA_CheckCandidate(TileIndex candidatetile_p, Town tn)
	{
		TileIndex candidatetile = candidatetile_p.iadd(-4, -4);
		boolean [] retcode = { true };
		int tileHeight = candidatetile.TileHeight();
		//BEGIN_TILE_LOOP(tl, 9, 9, candidatetile)
		//TileIndex.forEach( 9, 9, candidatetile.tile, (tl) ->
		TileIndex.forAll(9, 9, candidatetile.tile, (tl) ->
		{

			if(!tl.IsValidTile()) {
				retcode[0] = false;
				return true;
			}

			if(
					tl.GetTileType() == TileTypes.MP_UNMOVABLE || 
							tl.GetTileType() == TileTypes.MP_INDUSTRY || 
							tl.GetTileType() == TileTypes.MP_WATER) {
				retcode[0] = false;
				return true;
			}

			if(tl.TileHeight() != tileHeight) { 
				retcode[0] = false;
				return true;
			}
			
			if(!tl.EnsureNoVehicle()) {
				retcode[0] = false;
				return true;
			}
			
			if(Cmd.CmdFailed(Cmd.DoCommandByTile(tl, 0, 0, ~Cmd.DC_EXEC,Cmd.CMD_LANDSCAPE_CLEAR))) { 
				retcode[0] = false;
				return true;
			}

			return false;
		});
		//END_TILE_LOOP(tl, 9, 9, candidatetile)
		
		return retcode[0];
	}

	//adds a news item for display
	void MA_AnnounceAirport(Town tn, TileIndex tl)
	{
		Global.SetDParam(0 ,tn.index);
		NewsItem.AddNewsItem(Str.STR_MA_BUILT_MUNICIPAL_AIRPORT, NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_TILE, NewsItem.NT_ECONOMY, 0), tl.tile, 0);
	}

	//looks for a goodsite (works in outward spiral)
	TileIndex MA_FindSite(Town tn)
	{
		int angle,radius;

		for(radius = MA_MIN_DISTANCE_FROM_TOWN; (radius <= MA_MAX_DISTANCE_FROM_TOWN); radius++) {
			for(angle = 0; (angle <= 360); angle++) {		
				if(MA_CheckCandidate(CircularPos(radius, angle, tn.xy), tn)) return CircularPos(radius, angle, tn.xy);
			}//angle
		}//radius
		return TileIndex.getInvalid();
	}

	//the fun bit 
	void MA_BuildAirport(TileIndex buildtile_p)
	{
		TileIndex buildtile = buildtile_p.iadd(-3,-3);

		//BEGIN_TILE_LOOP(tl, 7, 7, buildtile)
		TileIndex.forAll( 7, 7, buildtile, (tl) ->
		{
			Cmd.DoCommandByTile(tl, 0, 0, Cmd.DC_EXEC,Cmd.CMD_LANDSCAPE_CLEAR);
			return false;
		});
		//END_TILE_LOOP(tl, 7, 7, buildtile)
		
		Cmd.DoCommandByTile(buildtile, AirportFTAClass.AT_INTERNATIONAL, 0, Cmd.DC_EXEC, Cmd.CMD_BUILD_AIRPORT);
	}

	//the main procedure, does the checks and runs the process.
	public static void MunicipalAirport(Town tn)
	{
		//Station st;
		TileIndex tl; 

		PlayerID old_player = Global._current_player;
		Global._current_player.id = Owner.OWNER_TOWN;

		if(!Global._patches.allow_municipal_airports) 
			MA_DestroyAirport(tn);
		
		if(!Global._patches.allow_municipal_airports 
			|| (Global._cur_year + 1920) < 1990 
			|| tn.population < MA_MIN_POPULATION) {
			Global._current_player = old_player;
			return;
		}

		//FOR_ALL_STATIONS(st)
		Iterator<Station> it = Station.getIterator();
		while(it.hasNext())
		{
			Station st = it.next();
			
			if(st.IsValidStation()
				&& st.facilities == Station.FACIL_AIRPORT
				&& st.owner.id == Owner.OWNER_TOWN 
				&& st.town == tn) 
			{
				Global._current_player = old_player;
				return;
			}

		}
		

		
		tl = MA_FindSite(tn);
		
		if(tl == TileIndex.INVALID_TILE) {
			Global._current_player = old_player;
			return;
		}
		
		MA_BuildAirport(tl);
		MA_AnnounceAirport(tn, tl);
		
		Global._current_player = old_player;
	}

	// same as above but isnt as stringent
	void MA_EditorAddAirport(Town tn)
	{
		//Station st;
		TileIndex tl;
		PlayerID old_player = Global._current_player;
		
		Global._current_player = PlayerID.get( Owner.OWNER_TOWN );
		
		//FOR_ALL_STATIONS(st) 
		Iterator<Station> it = Station.getIterator();
		while(it.hasNext())
		{
			Station st = it.next();
			if(st.IsValidStation()
				&& st.facilities == Station.FACIL_AIRPORT
				&& st.owner.id == Owner.OWNER_TOWN 
				&& st.town == tn
				&& st.airport_type != AirportFTAClass.AT_OILRIG) { //not really needed but you never know
					MA_DestroyAirport(tn);
					Global._current_player = old_player;
					return;
			}
		}

		if(!Global._patches.allow_municipal_airports) return;

		if(Global._cur_year + 1920 < INT_AIRPORT_YEAR) {
			Global.SetDParam(0, tn.index);
			Gui.ShowErrorMessage(Str.STR_MA_CANT_BUILD_TOO_EARLY, Str.INVALID_STRING_ID, 300, 300);
			Global._current_player = old_player;
			return;
		}

		if(tn.population < MA_MIN_POPULATION) {
			Global.SetDParam(0, tn.index);
			Gui.ShowErrorMessage(Str.STR_MA_CANT_BUILD_LOW_POPULATION, Str.INVALID_STRING_ID, 300, 300);
			Global._current_player = old_player;
			return;
		}

		tl = MA_FindSite(tn);

		if(tl == TileIndex.INVALID_TILE) {
			Global.SetDParam(0, tn.index);
			Gui.ShowErrorMessage(Str.STR_MA_CANT_BUILD_NO_SITE, Str.INVALID_STRING_ID, 300, 300);
			Global._current_player = old_player;
			return;
		}
		
		MA_BuildAirport(tl);
		
		
		Global._current_player = old_player;
		return;
	}
	
	
}
