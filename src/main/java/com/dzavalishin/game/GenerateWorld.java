package com.dzavalishin.game;

import com.dzavalishin.ai.Ai;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.util.GameDate;
import com.dzavalishin.xui.AirportGui;
import com.dzavalishin.xui.DockGui;
import com.dzavalishin.xui.RailGui;
import com.dzavalishin.xui.RoadGui;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.ViewPort;

public class GenerateWorld {


	public static void doGenerateWorld(int mode, int size_x, int size_y)
	{
		int i;

		// Make sure everything is done via OWNER_NONE
		PlayerID.setCurrentToNone();

		Global._generating_world = true;
		InitializeGame(size_x, size_y);
		ViewPort.SetObjectToPlace(Sprite.SPR_CURSOR_ZZZ, 0, 0, 0);

		// Must start economy early because of the costs.
		Global.gs._economy.StartupEconomy();

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
		Global.gs.date.reset_date_fract();
		Global._cur_tileloop_tile = 0;

		{
			int starting = GameDate.ConvertIntDate(Global._patches.starting_date);
			if ( starting == -1) starting = 10958;
			Global.gs.date.SetDate(starting);
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


	
}
