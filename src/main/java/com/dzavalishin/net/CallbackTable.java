package com.dzavalishin.net;

import com.dzavalishin.game.AirCraft;
import com.dzavalishin.game.Bridge;
import com.dzavalishin.game.SignStruct;
import com.dzavalishin.game.Terraform;
import com.dzavalishin.ifaces.CommandCallback;
import com.dzavalishin.xui.AirportGui;
import com.dzavalishin.xui.DockGui;
import com.dzavalishin.xui.Gui;
import com.dzavalishin.xui.RailGui;
import com.dzavalishin.xui.RoadGui;
import com.dzavalishin.xui.RoadVehGui;
import com.dzavalishin.xui.ShipGui;
import com.dzavalishin.xui.TrainGui;

// If you add a callback for DoCommandP, also add the callback in here
//   see below for the full list!
// If you don't do it, it won't work across the network!!
public class CallbackTable {



	public static CommandCallback _callback_table[] = {
		/* 0x00 */ null,
		/* 0x01 */ AirCraft::CcBuildAircraft,
		/* 0x02 */ AirportGui::CcBuildAirport,
		/* 0x03 */ Bridge::CcBuildBridge,
		/* 0x04 */ DockGui::CcBuildCanal,
		/* 0x05 */ DockGui::CcBuildDocks,
		/* 0x06 */ TrainGui::CcBuildLoco,
		/* 0x07 */ RoadVehGui::CcBuildRoadVeh,
		/* 0x08 */ ShipGui::CcBuildShip,
		/* 0x09 */ Gui::CcBuildTown,
		/* 0x0A */ RoadGui::CcBuildRoadTunnel,
		/* 0x0B */ RailGui::CcBuildRailTunnel,
		/* 0x0C */ TrainGui::CcBuildWagon,
		/* 0x0D */ RoadGui::CcRoadDepot,
		/* 0x0E */ RailGui::CcRailDepot,
		/* 0x0F */ SignStruct::CcPlaceSign,
		/* 0x10 */ Gui::CcPlaySound10,
		/* 0x11 */ RoadGui::CcPlaySound1D,
		/* 0x12 */ RailGui::CcPlaySound1E,
		/* 0x13 */ RailGui::CcStation,
		/* 0x14 */ Terraform::CcTerraform,
		/* 0x15 */ AirCraft::CcCloneAircraft,
		/* 0x16 */ RoadVehGui::CcCloneRoadVeh,
		/* 0x17 */ ShipGui::CcCloneShip,
		/* 0x18 */ TrainGui::CcCloneTrain,
	};

	static final int _callback_table_count = _callback_table.length;

}
