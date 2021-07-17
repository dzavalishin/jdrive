package game;


public class GenerateWorld {


	static void GenerateWorld(int mode, uint size_x, uint size_y)
	{
		int i;

		// Make sure everything is done via OWNER_NONE
		Global._current_player = OWNER_NONE;

		_generating_world = true;
		InitializeGame(size_x, size_y);
		SetObjectToPlace(SPR_CURSOR_ZZZ, 0, 0, 0);

		// Must start economy early because of the costs.
		StartupEconomy();

		// Don't generate landscape items when in the scenario editor.
		if (mode == 1) {
			// empty world in scenario editor
			ConvertGroundTilesIntoWaterTiles();
		} else {
			GenerateLandscape();
			GenerateClearTile();

			// only generate towns, tree and industries in newgame mode.
			if (mode == 0) {
				GenerateTowns();
				GenerateTrees();
				GenerateIndustries();
				GenerateUnmovables();
			}
		}

		// These are probably pointless when inside the scenario editor.
		StartupPlayers();
		StartupEngines();
		StartupDisasters();
		_generating_world = false;

		// No need to run the tile loop in the scenario editor.
		if (mode != 1) {
			for(i=0x500; i!=0; i--)
				RunTileLoop();
		}

		ResetObjectToPlace();

	}
	
	
	
	static void InitializeGame(uint size_x, uint size_y)
	{
		AllocateMap(size_x, size_y);

		AddTypeToEngines(); // make sure all engines have a type

		SetObjectToPlace(SPR_CURSOR_ZZZ, 0, 0, 0);

		_pause = 0;
		_fast_forward = 0;
		_tick_counter = 0;
		_date_fract = 0;
		_cur_tileloop_tile = 0;

		{
			uint starting = ConvertIntDate(_patches.starting_date);
			if ( starting == (uint)-1) starting = 10958;
			SetDate(starting);
		}

		InitializeVehicles();
		InitializeWaypoints();
		InitializeDepot();
		InitializeOrders();

		InitNewsItemStructs();
		InitializeLandscape();
		InitializeClearLand();
		InitializeRail();
		InitializeRailGui();
		InitializeRoad();
		InitializeRoadGui();
		InitializeAirportGui();
		InitializeDock();
		InitializeDockGui();
		InitializeTowns();
		InitializeTrees();
		InitializeSigns();
		InitializeStations();
		InitializeIndustries();

		InitializeNameMgr();
		InitializeVehiclesGuiList();
		InitializeTrains();
		InitializeNPF();

		AI_Initialize();
		InitializePlayers();
		InitializeCheats();

		InitTextEffects();
		InitTextMessage();
		InitializeAnimatedTiles();

		InitializeLandscapeVariables(false);

		ResetObjectToPlace();
	}
	
	static void InitializeCheats()
	{
		memset(_cheats, 0, sizeof(Cheats));
	}


	static void InitializeNameMgr()
	{
		memset(_name_array, 0, sizeof(_name_array));
	}
	
	
}
