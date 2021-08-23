package game;

import game.ai.Ai;
import game.enums.Owner;
import game.ids.PlayerID;
import game.util.GameDate;
import game.xui.AirportGui;
import game.xui.DockGui;
import game.xui.RailGui;
import game.xui.RoadGui;
import game.xui.VehicleGui;
import game.xui.ViewPort;

public class GenerateWorld {


	public static void doGenerateWorld(int mode, int size_x, int size_y)
	{
		int i;

		// Make sure everything is done via OWNER_NONE
		Global.gs._current_player = PlayerID.get( Owner.OWNER_NONE );

		Global._generating_world = true;
		InitializeGame(size_x, size_y);
		ViewPort.SetObjectToPlace(Sprite.SPR_CURSOR_ZZZ, 0, 0, 0);

		// Must start economy early because of the costs.
		Economy.StartupEconomy();

		// Don't generate landscape items when in the scenario editor.
		if (mode == 1) {
			// empty world in scenario editor
			Landscape.ConvertGroundTilesIntoWaterTiles();
		} else {
			Landscape.GenerateLandscape();
			Clear.GenerateClearTile();

			// only generate towns, tree and industries in newgame mode.
			if (mode == 0) {
				Town.GenerateTowns();
				Tree.GenerateTrees();
				Industry.GenerateIndustries();
				UnmovableCmd.GenerateUnmovables();
			}
		}

		// These are probably pointless when inside the scenario editor.
		Player.StartupPlayers();
		Engine.StartupEngines();
		DisasterCmd.StartupDisasters();
		Global._generating_world = false;

		// No need to run the tile loop in the scenario editor.
		if (mode != 1) {
			for(i=0x500; i!=0; i--)
				Landscape.RunTileLoop();
		}

		ViewPort.ResetObjectToPlace();

	}
	
	
	
	static void InitializeGame(int size_x, int size_y)
	{
		Map.AllocateMap(size_x, size_y);

		Engine.AddTypeToEngines(); // make sure all engines have a type

		ViewPort.SetObjectToPlace(Sprite.SPR_CURSOR_ZZZ, 0, 0, 0);

		Global._pause = 0;
		Global._fast_forward = false;
		Global._tick_counter = 0;
		Global._date_fract = 0;
		Global._cur_tileloop_tile = 0;

		{
			int starting = GameDate.ConvertIntDate(Global._patches.starting_date);
			if ( starting == -1) starting = 10958;
			Global.SetDate(starting);
		}

		Vehicle.InitializeVehicles();
		WayPoint.InitializeWaypoints();
		Depot.InitializeDepot();
		Order.InitializeOrders();

		NewsItem.InitNewsItemStructs();
		Landscape.InitializeLandscape();
		Clear.InitializeClearLand();
		Rail.InitializeRail();
		RailGui.InitializeRailGui();
		Road.InitializeRoad();
		RoadGui.InitializeRoadGui();
		AirportGui.InitializeAirportGui();
		WaterCmd.InitializeDock();
		DockGui.InitializeDockGui();
		Town.InitializeTowns();
		Tree.InitializeTrees();
		SignStruct.InitializeSigns();
		Station.InitializeStations();
		Industry.InitializeIndustries();

		Global.InitializeNameMgr();
		VehicleGui.InitializeVehiclesGuiList();
		TrainCmd.InitializeTrains();
		Npf.InitializeNPF();

		Ai.AI_Initialize();
		Player.InitializePlayers();
		InitializeCheats();

		TextEffect.InitTextEffects();
		TextEffect.InitTextMessage();
		TextEffect.InitializeAnimatedTiles();

		Misc.InitializeLandscapeVariables(false);

		ViewPort.ResetObjectToPlace();
	}
	
	static void InitializeCheats()
	{
		// TODO memset(_cheats, 0, sizeof(Cheats));
	}


	/* Global
	static void InitializeNameMgr()
	{
		// TODO memset(_name_array, 0, sizeof(_name_array));
	}*/
	
	
}
