package com.dzavalishin.xui;

import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.game.Bridge;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Rail;
import com.dzavalishin.game.RailtypeInfo;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Terraform;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.WayPoint;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ifaces.OnButtonClick;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.Sprites;

public class RailGui {

	//static RailType _cur_railtype;
	static int _cur_railtype;

	static boolean 	_remove_button_clicked;
	static int		_build_depot_direction;
	static int		_waypoint_count = 1;
	static int		_cur_waypoint_type;
	static int		_cur_signal_type;
	static int		_cur_presig_type;
	static boolean _cur_autosig_compl;

	//static final StringID _presig_types_dropdown[] = 
	static final int _presig_types_dropdown[] = 
		{
				Str.STR_SIGNAL_NORMAL,
				Str.STR_SIGNAL_ENTRANCE,
				Str.STR_SIGNAL_EXIT,
				Str.STR_SIGNAL_COMBO,
				Str.STR_SIGNAL_PBS,
				Str.INVALID_STRING
		};

	static class _Railstation {
		int orientation;
		int numtracks;
		int platlength;
		boolean dragdrop;
	}

	private static final _Railstation _railstation = new _Railstation();


	public static void CcPlaySound1E(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) Sound.SndPlayTileFx(Snd.SND_20_SPLAT_2, tile);
	}

	static void GenericPlaceRail(TileIndex tile, int cmd)
	{
		Cmd.DoCommandP(tile, _cur_railtype, cmd, RailGui::CcPlaySound1E,
				_remove_button_clicked ?
						Cmd.CMD_REMOVE_SINGLE_RAIL | Cmd.CMD_MSG(Str.STR_1012_CAN_T_REMOVE_RAILROAD_TRACK) | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER :
							Cmd.CMD_BUILD_SINGLE_RAIL | Cmd.CMD_MSG(Str.STR_1011_CAN_T_BUILD_RAILROAD_TRACK) | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER
				);
	}

	static void PlaceRail_N(TileIndex tile)
	{
		int cmd = ViewPort._tile_fract_coords.x > ViewPort._tile_fract_coords.y ? 4 : 5;
		GenericPlaceRail(tile, cmd);
	}

	static void PlaceRail_NE(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_FIX_Y);
	}

	static void PlaceRail_E(TileIndex tile)
	{
		int cmd = ViewPort._tile_fract_coords.x + ViewPort._tile_fract_coords.y <= 15 ? 2 : 3;
		GenericPlaceRail(tile, cmd);
	}

	static void PlaceRail_NW(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_FIX_X);
	}

	static void PlaceRail_AutoRail(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_RAILDIRS);
	}

	static void PlaceExtraDepotRail(TileIndex tile, int extra)
	{
		int b = tile.getMap().m5;

		if (BitOps.GB(b, 6, 2) != Rail.RAIL_TYPE_NORMAL >> 6) return;
		if (0 == (b & (extra >> 8))) return;

		Cmd.DoCommandP(tile, _cur_railtype, extra & 0xFF, null, Cmd.CMD_BUILD_SINGLE_RAIL | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER);
	}

	static final int _place_depot_extra[] = {
			0x604,		0x2102,		0x1202,		0x505,
			0x2400,		0x2801,		0x1800,		0x1401,
			0x2203,		0x904,		0x0A05,		0x1103,
	};


	public static void CcRailDepot(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			int dir = p2;

			Sound.SndPlayTileFx(Snd.SND_20_SPLAT_2, tile);
			ViewPort.ResetObjectToPlace();

			tile = tile.iadd(TileIndex.TileOffsByDir(dir));

			if (tile.IsTileType( TileTypes.MP_RAILWAY)) {
				PlaceExtraDepotRail(tile, _place_depot_extra[dir]);
				PlaceExtraDepotRail(tile, _place_depot_extra[dir + 4]);
				PlaceExtraDepotRail(tile, _place_depot_extra[dir + 8]);
			}
		}
	}

	static void PlaceRail_Depot(TileIndex tile)
	{
		Cmd.DoCommandP(tile, _cur_railtype, _build_depot_direction, RailGui::CcRailDepot,
				Cmd.CMD_BUILD_TRAIN_DEPOT | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_100E_CAN_T_BUILD_TRAIN_DEPOT));
	}

	static void PlaceRail_Waypoint(TileIndex tile)
	{
		if (!_remove_button_clicked) {
			Cmd.DoCommandP(tile, _cur_waypoint_type, 0, null/*CcPlaySound1E*/, Cmd.CMD_BUILD_TRAIN_WAYPOINT | Cmd.CMD_MSG(Str.STR_CANT_BUILD_TRAIN_WAYPOINT));
		} else {
			Cmd.DoCommandP(tile, 0, 0, null/*CcPlaySound1E*/, Cmd.CMD_REMOVE_TRAIN_WAYPOINT | Cmd.CMD_MSG(Str.STR_CANT_REMOVE_TRAIN_WAYPOINT));
		}
	}

	public static void CcStation(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Sound.SndPlayTileFx(Snd.SND_20_SPLAT_2, tile);
			ViewPort.ResetObjectToPlace();
		}
	}

	static void PlaceRail_Station(TileIndex tile)
	{
		if(_remove_button_clicked)
			Cmd.DoCommandP(tile, 0, 0, null/*CcPlaySound1E*/, Cmd.CMD_REMOVE_FROM_RAILROAD_STATION | Cmd.CMD_MSG(Str.STR_CANT_REMOVE_PART_OF_STATION));
		else if (_railstation.dragdrop) {
			ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y_LIMITED);
			ViewPort.VpSetPlaceSizingLimit(Global._patches.station_spread);
		} else {
			// TODO: Custom station selector GUI. Now we just try using first custom station
			// (and fall back to normal stations if it isn't available).
			Cmd.DoCommandP(tile, _railstation.orientation | (_railstation.numtracks<<8) | (_railstation.platlength<<16),_cur_railtype|1<<4, RailGui::CcStation,
					Cmd.CMD_BUILD_RAILROAD_STATION | Cmd.CMD_NO_WATER | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_100F_CAN_T_BUILD_RAILROAD_STATION));
		}
	}

	static void GenericPlaceSignals(TileIndex tile)
	{
		int trackstat;
		int i;

		trackstat = tile.GetTileTrackStatus(TransportType.Rail);

		if(0 != (trackstat & 0x30)) // N-S direction
			trackstat = (ViewPort._tile_fract_coords.x <= ViewPort._tile_fract_coords.y) ? 0x20 : 0x10;

		if(0 !=  (trackstat & 0x0C)) // E-W direction
			trackstat = (ViewPort._tile_fract_coords.x + ViewPort._tile_fract_coords.y <= 15) ? 4 : 8;

		// Lookup the bit index
		i = 0;
		if (trackstat != 0) {
			for (; 0 == (trackstat & 1); trackstat >>= 1) i++;
		}

		if (!_remove_button_clicked) {
			Cmd.DoCommandP(tile, i + (Global._ctrl_pressed ? 8 : 0) +
					(!BitOps.HASBIT(_cur_signal_type, 0) != !Global._ctrl_pressed ? 16 : 0) +
					(_cur_presig_type << 5) ,
					0, RailGui::CcPlaySound1E, Cmd.CMD_BUILD_SIGNALS | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE));
		} else {
			Cmd.DoCommandP(tile, i, 0, RailGui::CcPlaySound1E,
					Cmd.CMD_REMOVE_SIGNALS | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM));
		}
	}

	static void PlaceRail_Bridge(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_OR_Y);
	}

	public static void CcBuildRailTunnel(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Sound.SndPlayTileFx(Snd.SND_20_SPLAT_2, tile);
			ViewPort.ResetObjectToPlace();
		} else {
			ViewPort.SetRedErrorSquare(Global._build_tunnel_endtile);
		}
	}

	static void PlaceRail_Tunnel(TileIndex tile)
	{
		Cmd.DoCommandP(tile, _cur_railtype, 0, RailGui::CcBuildRailTunnel,
				Cmd.CMD_BUILD_TUNNEL | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_5016_CAN_T_BUILD_TUNNEL_HERE));
	}

	public static void PlaceProc_BuyLand(TileIndex tile)
	{
		Cmd.DoCommandP(tile, 0, 0, null /*CcPlaySound1E*/, Cmd.CMD_PURCHASE_LAND_AREA | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_5806_CAN_T_PURCHASE_THIS_LAND));
	}

	static void PlaceRail_ConvertRail(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y | Gui.GUI_PlaceProc_ConvertRailArea);
	}

	static void PlaceRail_AutoSignals(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_SIGNALDIRS);
	}


	
	
	
	
	
	
	static void BuildRailClick_N(Window w)
	{
		Gui.HandlePlacePushButton(w, 4, Rail.GetRailTypeInfo(_cur_railtype).cursor.rail_ns, 1, RailGui::PlaceRail_N);
	}

	static void BuildRailClick_NE(Window w)
	{
		Gui.HandlePlacePushButton(w, 5, Rail.GetRailTypeInfo(_cur_railtype).cursor.rail_swne, 1, RailGui::PlaceRail_NE);
	}

	static void BuildRailClick_E(Window w)
	{
		Gui.HandlePlacePushButton(w, 6, Rail.GetRailTypeInfo(_cur_railtype).cursor.rail_ew, 1, RailGui::PlaceRail_E);
	}

	static void BuildRailClick_NW(Window w)
	{
		Gui.HandlePlacePushButton(w, 7, Rail.GetRailTypeInfo(_cur_railtype).cursor.rail_nwse, 1, RailGui::PlaceRail_NW);
	}

	static void BuildRailClick_AutoRail(Window w)
	{
		Gui.HandlePlacePushButton(w, 8, Rail.GetRailTypeInfo(_cur_railtype).cursor.autorail, ViewPort.VHM_RAIL, RailGui::PlaceRail_AutoRail);
	}

	static void BuildRailClick_Demolish(Window w)
	{
		Gui.HandlePlacePushButton(w, 9, Sprites.ANIMCURSOR_DEMOLISH, 1, Terraform::PlaceProc_DemolishArea);
	}

	static void BuildRailClick_Depot(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 10, Rail.GetRailTypeInfo(_cur_railtype).cursor.depot, 1, RailGui::PlaceRail_Depot)) {
			ShowBuildTrainDepotPicker();
		}
	}

	static void BuildRailClick_Waypoint(Window w)
	{
		_waypoint_count = 0; // TODO GetNumCustomStations(STAT_CLASS_WAYP);
		if (Gui.HandlePlacePushButton(w, 11, Sprite.SPR_CURSOR_WAYPOINT, 1, RailGui::PlaceRail_Waypoint) &&
				_waypoint_count > 1) {
			ShowBuildWaypointPicker();
		}
	}

	static void BuildRailClick_Station(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 12, Sprite.SPR_CURSOR_RAIL_STATION, 1, RailGui::PlaceRail_Station)) ShowStationBuilder();
	}

	static void BuildRailClick_AutoSignals(Window w)
	{
		if (Gui.HandlePlacePushButton(w, 13, Sprites.ANIMCURSOR_BUILDSIGNALS, ViewPort.VHM_RECT, RailGui::PlaceRail_AutoSignals))
			ShowSignalBuilder();
	}

	static void BuildRailClick_Bridge(Window w)
	{
		Gui.HandlePlacePushButton(w, 14, Sprite.SPR_CURSOR_BRIDGE, 1, RailGui::PlaceRail_Bridge);
	}

	static void BuildRailClick_Tunnel(Window w)
	{
		Gui.HandlePlacePushButton(w, 15, Rail.GetRailTypeInfo(_cur_railtype).cursor.tunnel, 3, RailGui::PlaceRail_Tunnel);
	}

	static void BuildRailClick_Remove(Window w)
	{
		if (BitOps.HASBIT(w.disabled_state, 16)) return;
		w.SetWindowDirty();
		Sound.SndPlayFx(Snd.SND_15_BEEP);

		w.click_state = BitOps.RETTOGGLEBIT(w.click_state, 16);
		_remove_button_clicked = BitOps.HASBIT(w.click_state, 16);
		ViewPort.SetSelectionRed(BitOps.HASBIT(w.click_state, 16));

		// handle station builder
		if (BitOps.HASBIT(w.click_state, 16)) {
			if (_remove_button_clicked) {
				ViewPort.SetTileSelectSize(1, 1);
			} else {
				Window.BringWindowToFrontById(Window.WC_BUILD_STATION, 0);
			}
		}
	}

	static void BuildRailClick_Convert(Window w)
	{
		Gui.HandlePlacePushButton(w, 17, Rail.GetRailTypeInfo(_cur_railtype).cursor.convert, 1, RailGui::PlaceRail_ConvertRail);
	}

	static void BuildRailClick_Landscaping(Window w)
	{
		Terraform.ShowTerraformToolbar();
	}

	static void DoRailroadTrack(int mode)
	{
		//Cmd.DoCommandP(TileIndex.TileVirtXY(ViewPort._thd.selstart.x, ViewPort._thd.selstart.y), TileIndex.TileVirtXY(ViewPort._thd.selend.x, ViewPort._thd.selend.y).getTile(), _cur_railtype | (mode << 4), null,
		Cmd.DoCommandP(ViewPort._thd.getStartTile(), ViewPort._thd.getEndTile().getTile(), _cur_railtype | (mode << 4), null,
				_remove_button_clicked ?
						Cmd.CMD_REMOVE_RAILROAD_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1012_CAN_T_REMOVE_RAILROAD_TRACK) :
							Cmd.CMD_BUILD_RAILROAD_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1011_CAN_T_BUILD_RAILROAD_TRACK)
				);
	}

	static void HandleAutodirPlacement()
	{
		TileHighlightData thd = ViewPort._thd;
		int trackstat = thd.getTrackState();

		if(thd.hasRail()) { // one tile case
			GenericPlaceRail(thd.getEndTile(), trackstat);
			return;
		}

		DoRailroadTrack(trackstat);
	}

	static void HandleAutoSignalPlacement()
	{
		TileHighlightData thd = ViewPort._thd;
		int trackstat = thd.getTrackState(); // (thd.drawstyle & 0xF); // 0..5

		if (thd.isRect()) { // one tile case
			GenericPlaceSignals(thd.getEndTile()); // TileIndex.TileVirtXY(thd.selend.x, thd.selend.y));
			return;
		}

		// Global._patches.drag_signals_density is given as a parameter such that each user in a network
		// game can specify his/her own signal density
		Cmd.DoCommandP(
				thd.getStartTile(), //TileIndex.TileVirtXY(thd.selstart.x, thd.selstart.y),
				thd.getEndTile().getTile(),// TileIndex.TileVirtXY(thd.selend.x, thd.selend.y).tile,
				(Global._ctrl_pressed ? 1 << 3 : 0) | (trackstat << 4) | (Global._patches.drag_signals_density << 24),
				RailGui::CcPlaySound1E,
				_remove_button_clicked ?
						(Cmd.CMD_REMOVE_SIGNAL_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM)) 
						:
							(Cmd.CMD_BUILD_SIGNAL_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE))
				);


		/* TODO What the hell is this?

		(!BitOps.HASBIT(_cur_signal_type, 0) != !_ctrl_pressed ? 1 << 3 : 0) | 
		(trackstat << 4) |
		(Global._patches.drag_signals_density << 24) | 
		(_cur_autosig_compl ? 2 : 0),
		RailGui::CcPlaySound1E,
		(_remove_button_clicked ? Cmd.CMD_REMOVE_SIGNAL_TRACK | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | 
		Cmd.CMD_MSG(Str.STR_1013_CAN_T_REMOVE_SIGNALS_FROM) :
	        Cmd.CMD_BUILD_SIGNAL_TRACK  | Cmd.CMD_AUTO | Cmd.CMD_NO_WATER | Cmd.CMD_MSG(Str.STR_1010_CAN_T_BUILD_SIGNALS_HERE) );
		 */
	}

	
	
	

	static final OnButtonClick _build_railroad_button_proc[] = {
			RailGui::BuildRailClick_N,
			RailGui::BuildRailClick_NE,
			RailGui::BuildRailClick_E,
			RailGui::BuildRailClick_NW,
			RailGui::BuildRailClick_AutoRail,
			RailGui::BuildRailClick_Demolish,
			RailGui::BuildRailClick_Depot,
			RailGui::BuildRailClick_Waypoint,
			RailGui::BuildRailClick_Station,
			RailGui::BuildRailClick_AutoSignals,
			RailGui::BuildRailClick_Bridge,
			RailGui::BuildRailClick_Tunnel,
			RailGui::BuildRailClick_Remove,
			RailGui::BuildRailClick_Convert,
			RailGui::BuildRailClick_Landscaping,
	};

	static final int _rail_keycodes[] = {
			'1',
			'2',
			'3',
			'4',
			'5',
			'6',
			'7', // depot
			'8', // waypoint
			'9', // station
			'S', // signals
			'B', // bridge
			'T', // tunnel
			'R', // remove
			'C', // convert rail
			'L', // landscaping
	};


	static void BuildRailToolbWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			w.disabled_state &= ~(1 << 16);
			if (0 == (w.click_state & ((1<<4)|(1<<5)|(1<<6)|(1<<7)|(1<<8)|(1<<11)|(1<<12)|(1<<13)))) {
				w.disabled_state |= (1 << 16);
				w.click_state &= ~(1<<16);
			}
			w.DrawWindowWidgets();
			break;

		case WE_CLICK:
			if (e.widget >= 4) {
				_remove_button_clicked = false;
				_build_railroad_button_proc[e.widget - 4].accept(w);
			}
			break;

		case WE_KEYPRESS: {
			int i;

			for (i = 0; i < _rail_keycodes.length; i++) {
				if (e.keycode == _rail_keycodes[i]) {
					e.cont = false;
					_remove_button_clicked = false;
					_build_railroad_button_proc[i].accept(w);
					break;
				}
			}
			// redraw tile selection
			ViewPort._thd.markPosDirty();
			break;
		}

		case WE_PLACE_OBJ:
			Global._place_proc.accept(e.tile);
			return;

		case WE_PLACE_DRAG: {
			ViewPort.VpSelectTilesWithMethod(e.pt.x, e.pt.y, e.userdata & 0xF);
			return;
		}

		case WE_PLACE_MOUSEUP:
			if (e.pt.x != -1) {
				TileIndex start_tile = e.starttile;
				TileIndex end_tile = e.tile;

				if (e.userdata == ViewPort.VPM_X_OR_Y) {
					ViewPort.ResetObjectToPlace();
					Bridge.ShowBuildBridgeWindow(start_tile, end_tile, _cur_railtype);
				} else if (e.userdata == ViewPort.VPM_RAILDIRS) {
					boolean old = _remove_button_clicked;
					if (Global._ctrl_pressed) _remove_button_clicked = true;
					HandleAutodirPlacement();
					_remove_button_clicked = old;
				} else if (e.userdata == ViewPort.VPM_SIGNALDIRS) {
					HandleAutoSignalPlacement();
				} else if ((e.userdata & 0xF) == ViewPort.VPM_X_AND_Y) {
					if (Terraform.GUIPlaceProcDragXY(e)) break;

					if ((e.userdata >> 4) == Gui.GUI_PlaceProc_ConvertRailArea >> 4)
						Cmd.DoCommandP(end_tile, start_tile.getTile(), _cur_railtype, null/*RailGui::CcPlaySound10*/, Cmd.CMD_CONVERT_RAIL | Cmd.CMD_MSG(Str.STR_CANT_CONVERT_RAIL));
				} else if (e.userdata == ViewPort.VPM_X_AND_Y_LIMITED) {
					HandleStationPlacement(start_tile, end_tile);
				} else
					DoRailroadTrack(e.userdata & 1);
			}
			break;

		case WE_ABORT_PLACE_OBJ:
			w.UnclickWindowButtons();
			w.SetWindowDirty();

			w = Window.FindWindowById(Window.WC_BUILD_STATION, 0);
			if (w != null) w.as_def_d().close = true;
			w = Window.FindWindowById(Window.WC_BUILD_DEPOT, 0);
			if (w != null) w.as_def_d().close = true;
			w = Window.FindWindowById(Window.WC_BUILD_SIGNALS, 0);
			if (w != null) w.as_def_d().close=true;

			break;

		case WE_PLACE_PRESIZE: {
			TileIndex tile = e.tile;

			Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_AUTO, Cmd.CMD_BUILD_TUNNEL);
			ViewPort.VpSetPresizeRange(tile, Global._build_tunnel_endtile == null ? tile : Global._build_tunnel_endtile);
		} break;

		case WE_DESTROY:
			if (Global._patches.link_terraform_toolbar) Window.DeleteWindowById(Window.WC_SCEN_LAND_GEN, 0);
			break;
		default:
			break;
		}
	}


	static final Widget _build_rail_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   359,     0,    13, Str.STR_100A_RAILROAD_CONSTRUCTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   360,   371,     0,    13, 0x0,     Str.STR_STICKY_BUTTON),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   110,   113,    14,    35, 0x0,			Str.STR_NULL),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    0,     21,    14,    35, 0x4E3,		Str.STR_1018_BUILD_RAILROAD_TRACK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    22,    43,    14,    35, 0x4E4,		Str.STR_1018_BUILD_RAILROAD_TRACK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    44,    65,    14,    35, 0x4E5,		Str.STR_1018_BUILD_RAILROAD_TRACK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    66,    87,    14,    35, 0x4E6,		Str.STR_1018_BUILD_RAILROAD_TRACK),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    88,   109,    14,    35, Sprite.SPR_IMG_AUTORAIL, Str.STR_BUILD_AUTORAIL_TIP),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   114,   135,    14,    35, 0x2BF,		Str.STR_018D_DEMOLISH_BUILDINGS_ETC),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   136,   157,    14,    35, 0x50E,		Str.STR_1019_BUILD_TRAIN_DEPOT_FOR_BUILDING),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   158,   179,    14,    35, Sprite.SPR_IMG_WAYPOINT, Str.STR_CONVERT_RAIL_TO_WAYPOINT_TIP),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   180,   221,    14,    35, 0x512,		Str.STR_101A_BUILD_RAILROAD_STATION),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   222,   243,    14,    35, 0x50B,		Str.STR_101B_BUILD_RAILROAD_SIGNALS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   244,   285,    14,    35, 0xA22,		Str.STR_101C_BUILD_RAILROAD_BRIDGE),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   286,   305,    14,    35, Sprite.SPR_IMG_TUNNEL_RAIL, Str.STR_101D_BUILD_RAILROAD_TUNNEL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   306,   327,    14,    35, 0x2CA,		Str.STR_101E_TOGGLE_BUILD_REMOVE_FOR),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   328,   349,    14,    35, Sprite.SPR_IMG_CONVERT_RAIL, Str.STR_CONVERT_RAIL_TIP),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   350,   371,    14,    35, Sprite.SPR_IMG_LANDSCAPING,	Str.STR_LANDSCAPING_TOOLBAR_TIP),

			//	{   WIDGETS_END},
	};

	static final WindowDesc _build_rail_desc = new WindowDesc(
			640-372, 22, 372, 36,
			Window.WC_BUILD_TOOLBAR,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
			_build_rail_widgets,
			RailGui::BuildRailToolbWndProc
			);


	/** Configures the rail toolbar for railtype given
	 * @param railtype the railtype to display
	 * @param w the window to modify
	 */
	static void SetupRailToolbar(/*RailType*/ int railtype, Window  w)
	{
		final RailtypeInfo rti = Rail.GetRailTypeInfo(railtype);

		assert(railtype < Rail.RAILTYPE_END);
		//w.getWidget(RTW_CAPTION).unkA = rti.strings.toolbar_caption;
		w.getWidget(Rail.RTW_CAPTION).unkA = rti.toolbar_caption.id;
		w.getWidget(Rail.RTW_BUILD_NS).unkA = rti.gui_sprites.build_ns_rail.id;
		w.getWidget(Rail.RTW_BUILD_X).unkA = rti.gui_sprites.build_x_rail.id;
		w.getWidget(Rail.RTW_BUILD_EW).unkA = rti.gui_sprites.build_ew_rail.id;
		w.getWidget(Rail.RTW_BUILD_Y).unkA = rti.gui_sprites.build_y_rail.id;
		w.getWidget(Rail.RTW_AUTORAIL).unkA = rti.gui_sprites.auto_rail.id;
		w.getWidget(Rail.RTW_BUILD_DEPOT).unkA = rti.gui_sprites.build_depot.id;
		w.getWidget(Rail.RTW_CONVERT_RAIL).unkA = rti.gui_sprites.convert_rail.id;
		w.getWidget(Rail.RTW_BUILD_TUNNEL).unkA = rti.gui_sprites.build_tunnel.id;
	}

	public static void ShowBuildRailToolbar(/*RailType*/ int railtype, int button)
	{
		Window w;

		if (PlayerID.getCurrent().isSpectator()) return;

		//BiConsumer<Window,WindowEvent>  
		WindowProc cmp = RailGui::BuildRailToolbWndProc;
		
		// don't recreate the window if we're clicking on a button and the window exists.
		if (button < 0 || null == (w = Window.FindWindowById(Window.WC_BUILD_TOOLBAR, 0)) || (w.getWndproc() != cmp) ) {
			Window.DeleteWindowById(Window.WC_BUILD_TOOLBAR, 0);
			_cur_railtype = railtype;
			w = Window.AllocateWindowDesc(_build_rail_desc);
			SetupRailToolbar(railtype, w);
		}

		_remove_button_clicked = false;
		if (w != null && button >= 0) _build_railroad_button_proc[button].accept(w);
		if (Global._patches.link_terraform_toolbar) Terraform.ShowTerraformToolbar();
	}

	/* TODO: For custom stations, respect their allowed platforms/lengths bitmasks!
	 * --pasky */

	static void HandleStationPlacement(TileIndex start, TileIndex end)
	{
		int sx = start.TileX();
		int sy = start.TileY();
		int ex = end.TileX();
		int ey = end.TileY();
		int w,h;

		if (sx > ex) { int t = sx; sx = ex; ex = t; } // intswap(sx,ex); 
		if (sy > ey) { int t = sy; sy = ey; ey = t; } // intswap(sy,ey);
		w = ex - sx + 1;
		h = ey - sy + 1;
		if (0==_railstation.orientation) { int t = w; w = h; h = t; } // intswap(w,h);

		// TODO: Custom station selector GUI. Now we just try using first custom station
		// (and fall back to normal stations if it isn't available).
		Cmd.DoCommandP(TileIndex.TileXY(sx, sy), _railstation.orientation | (w << 8) | (h << 16), _cur_railtype | 1 << 4, RailGui::CcStation,
				Cmd.CMD_BUILD_RAILROAD_STATION | Cmd.CMD_NO_WATER | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_100F_CAN_T_BUILD_RAILROAD_STATION));
	}

	static void StationBuildWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int rad;
			int bits;

			if (w.as_def_d().close) return;

			bits = (1<<3) << ( _railstation.orientation);
			if (_railstation.dragdrop) {
				bits |= (1<<19);
			} else {
				bits |= (1<<(5-1)) << (_railstation.numtracks);
				bits |= (1<<(12-1)) << (_railstation.platlength);
			}
			bits |= (1<<20) << (Gui._station_show_coverage);
			w.click_state = bits;

			if (_railstation.dragdrop) {
				ViewPort.SetTileSelectSize(1, 1);
			} else {
				int x = _railstation.numtracks;
				int y = _railstation.platlength;
				if (_railstation.orientation == 0) { int t = x; x = y; y = t; } // intswap(x,y);
				if(!_remove_button_clicked)
					ViewPort.SetTileSelectSize(x, y);
			}

			rad = (Global._patches.modified_catchment) ? Station.CA_TRAIN : 4;

			if (Gui._station_show_coverage != 0)
				ViewPort.SetTileSelectBigSize(-rad, -rad, 2 * rad, 2 * rad);

			/* Update buttons for correct spread value */
			w.disabled_state = 0;
			for (bits = Global._patches.station_spread; bits < 7; bits++) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, bits + 5);
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, bits + 12);
			}

			w.DrawWindowWidgets();

			Station.StationPickerDrawSprite(39, 42, _cur_railtype, 2);
			Station.StationPickerDrawSprite(107, 42, _cur_railtype, 3);

			Gfx.DrawStringCentered(74, 15, Str.STR_3002_ORIENTATION, 0);
			Gfx.DrawStringCentered(74, 76, Str.STR_3003_NUMBER_OF_TRACKS, 0);
			Gfx.DrawStringCentered(74, 101, Str.STR_3004_PLATFORM_LENGTH, 0);
			Gfx.DrawStringCentered(74, 141, Str.STR_3066_COVERAGE_AREA_HIGHLIGHT, 0);

			MiscGui.DrawStationCoverageAreaText(2, 166, -1, rad);
		} break;

		case WE_CLICK: {
			switch (e.widget) {
			case 3:
			case 4:
				_railstation.orientation =  (e.widget - 3);
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;

			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				_railstation.numtracks =  ((e.widget - 5) + 1);
				_railstation.dragdrop = false;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;

			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
				_railstation.platlength =  ((e.widget - 12) + 1);
				_railstation.dragdrop = false;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;

			case 19:
				_railstation.dragdrop ^= true;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;

			case 20:
			case 21:
				Gui._station_show_coverage = e.widget - 20;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
		} break;

		case WE_MOUSELOOP:
			if (w.as_def_d().close) {
				w.DeleteWindow();
				return;
			}
			MiscGui.CheckRedrawStationCoverage(w);
			break;

		case WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		default:
			break;
		}
	}

	static final Widget _station_builder_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   147,     0,    13, Str.STR_3000_RAIL_STATION_SELECTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   147,    14,   199, 0x0,					Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     7,    72,    26,    73, 0x0,					Str.STR_304E_SELECT_RAILROAD_STATION),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    75,   140,    26,    73, 0x0,					Str.STR_304E_SELECT_RAILROAD_STATION),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    22,    36,    87,    98, Str.STR_00CB_1,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,    51,    87,    98, Str.STR_00CC_2,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    52,    66,    87,    98, Str.STR_00CD_3,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    67,    81,    87,    98, Str.STR_00CE_4,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    82,    96,    87,    98, Str.STR_00CF_5,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    97,   111,    87,    98, Str.STR_0335_6,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   112,   126,    87,    98, Str.STR_0336_7,	Str.STR_304F_SELECT_NUMBER_OF_PLATFORMS),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    22,    36,   112,   123, Str.STR_00CB_1,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,    51,   112,   123, Str.STR_00CC_2,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    52,    66,   112,   123, Str.STR_00CD_3,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    67,    81,   112,   123, Str.STR_00CE_4,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    82,    96,   112,   123, Str.STR_00CF_5,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    97,   111,   112,   123, Str.STR_0335_6,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   112,   126,   112,   123, Str.STR_0336_7,	Str.STR_3050_SELECT_LENGTH_OF_RAILROAD),

			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    37,   111,   126,   137, Str.STR_DRAG_DROP, Str.STR_STATION_DRAG_DROP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    14,    73,   152,   163, Str.STR_02DB_OFF, Str.STR_3065_DON_T_HIGHLIGHT_COVERAGE),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    74,   133,   152,   163, Str.STR_02DA_ON, Str.STR_3064_HIGHLIGHT_COVERAGE_AREA),
			//{   WIDGETS_END},
	};

	static final WindowDesc _station_builder_desc = new WindowDesc(
			-1, -1, 148, 200,
			Window.WC_BUILD_STATION,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_station_builder_widgets,
			RailGui::StationBuildWndProc
			);

	static void ShowStationBuilder()
	{
		Window.AllocateWindowDesc(_station_builder_desc);
	}

	static void BuildTrainDepotWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			/*RailType*/ int r;

			w.click_state = (1 << 3) << _build_depot_direction;
			w.DrawWindowWidgets();

			r = _cur_railtype;
			Rail.DrawTrainDepotSprite(70, 17, 0, r);
			Rail.DrawTrainDepotSprite(70, 69, 1, r);
			Rail.DrawTrainDepotSprite( 2, 69, 2, r);
			Rail.DrawTrainDepotSprite( 2, 17, 3, r);
			break;
		}

		case WE_CLICK:
			switch (e.widget) {
			case 3:
			case 4:
			case 5:
			case 6:
				_build_depot_direction =  (e.widget - 3);
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
			break;

		case WE_MOUSELOOP:
			if (w.as_def_d().close) w.DeleteWindow();
			return;

		case WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		default:
			break;
		}
	}

	static final Widget _build_depot_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   139,     0,    13, Str.STR_1014_TRAIN_DEPOT_ORIENTATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   139,    14,   121, 0x0,			Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    17,    66, 0x0,			Str.STR_1020_SELECT_RAILROAD_DEPOT_ORIENTATIO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    71,   136,    69,   118, 0x0,			Str.STR_1020_SELECT_RAILROAD_DEPOT_ORIENTATIO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    69,   118, 0x0,			Str.STR_1020_SELECT_RAILROAD_DEPOT_ORIENTATIO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     3,    68,    17,    66, 0x0,			Str.STR_1020_SELECT_RAILROAD_DEPOT_ORIENTATIO),
			//{   WIDGETS_END},
	};

	static final WindowDesc _build_depot_desc = new WindowDesc(
			-1,-1, 140, 122,
			Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_depot_widgets,
			RailGui::BuildTrainDepotWndProc
			);

	static void ShowBuildTrainDepotPicker()
	{
		Window.AllocateWindowDesc(_build_depot_desc);
	}


	static void BuildWaypointWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int i;

			w.click_state = (1 << 3) << (_cur_waypoint_type - w.hscroll.getPos());
			w.DrawWindowWidgets();

			for (i = 0; i < 5; i++) {
				if (w.hscroll.getPos() + i < _waypoint_count) {
					WayPoint.DrawWaypointSprite(2 + i * 68, 25, w.hscroll.getPos() + i, _cur_railtype);
				}
			}
			break;
		}
		case WE_CLICK: {
			switch (e.widget) {
			case 3: case 4: case 5: case 6: case 7:
				_cur_waypoint_type =  (e.widget - 3 + w.hscroll.getPos());
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			}
			break;
		}

		case WE_MOUSELOOP:
			if (w.as_def_d().close) w.DeleteWindow();
			break;

		case WE_DESTROY:
			if (!w.as_def_d().close) ViewPort.ResetObjectToPlace();
			break;
		default:
			break;
		}
	}

	static final Widget _build_waypoint_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   343,     0,    13, Str.STR_WAYPOINT,Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   343,    14,    91, 0x0, 0),

			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     3,    68,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,    71,   136,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   139,   204,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   207,   272,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,   275,   340,    17,    76, 0x0, Str.STR_WAYPOINT_GRAPHICS_TIP),

			new Widget( Window.WWT_HSCROLLBAR,   Window.RESIZE_NONE,    7,     1,   343,     80,    91, 0x0, Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			//{    WIDGETS_END},
	};

	static final WindowDesc _build_waypoint_desc = new WindowDesc(
			-1,-1, 344, 92,
			Window.WC_BUILD_DEPOT,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_waypoint_widgets,
			RailGui::BuildWaypointWndProc
			);

	static void ShowBuildWaypointPicker()
	{
		Window w = Window.AllocateWindowDesc(_build_waypoint_desc);
		w.hscroll.setCap(5);
		w.hscroll.setCount(_waypoint_count);
	}

	static void BuildSignalWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			/* XXX TODO: dont always hide the buttons when more than 2 signal types are available */
			w.hidden_state = (1 << 3) | (1 << 6);

			/* XXX TODO: take into account the scroll position for setting the click state */
			w.click_state = ((1 << 4) << _cur_signal_type) | (_cur_autosig_compl ? 1 << 9 : 0);

			Global.SetDParam(10, _presig_types_dropdown[_cur_presig_type]);
			w.DrawWindowWidgets();

			// Draw the string for current signal type
			Gfx.DrawStringCentered(69, 49, Str.STR_SIGNAL_TYPE_STANDARD + _cur_signal_type, 0);

			// Draw the strings for drag density
			Gfx.DrawStringCentered(69, 60, Str.STR_SIGNAL_DENSITY_DESC, 0);
			Global.SetDParam(0, Global._patches.drag_signals_density);
			Gfx.DrawString( 50, 71, Str.STR_SIGNAL_DENSITY_TILES , 0);

			// Draw the '<' and '>' characters for the decrease/increase buttons
			Gfx.DrawStringCentered(30, 72, Str.STR_6819, 0);
			Gfx.DrawStringCentered(40, 72, Str.STR_681A, 0);

			break;
		}
		case WE_CLICK: {
			switch(e.widget) {
			case 3: case 6: // scroll signal types
				/* XXX TODO: implement scrolling */
				break;
			case 4: case 5: // select signal type
				/* XXX TODO: take into account the scroll position for changing selected type */
				_cur_signal_type =  (e.widget - 4);
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			case 7: // decrease drag density
				if (Global._patches.drag_signals_density > 1) {
					Global._patches.drag_signals_density--;
					Sound.SndPlayFx(Snd.SND_15_BEEP);
					w.SetWindowDirty();
				}
				break;
			case 8: // increase drag density
				if (Global._patches.drag_signals_density < 20) {
					Global._patches.drag_signals_density++;
					Sound.SndPlayFx(Snd.SND_15_BEEP);
					w.SetWindowDirty();
				}
				break;
			case 9: // autosignal mode toggle button
				_cur_autosig_compl = !_cur_autosig_compl;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				w.SetWindowDirty();
				break;
			case 10: case 11: // presignal-type dropdown list
				Window.ShowDropDownMenu(w, _presig_types_dropdown, _cur_presig_type, 11, 0, 0);
				break;
			}
		}
		break;
		case WE_DROPDOWN_SELECT: // change presignal type
			_cur_presig_type =  e.index;
			w.SetWindowDirty();
			break;


		case WE_MOUSELOOP:
			if (w.as_def_d().close)
				w.DeleteWindow();
			return;

		case WE_DESTROY:
			if (!w.as_def_d().close)
				ViewPort.ResetObjectToPlace();
			break;
		default:
			break;
		}
	}

	static final Widget _build_signal_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX, Window.RESIZE_NONE,    7,    0,   10,    0,   13, Str.STR_00C5                 , Str.STR_018B_CLOSE_WINDOW),
			new Widget(   Window.WWT_CAPTION,  Window.RESIZE_NONE,    7,   11,  139,    0,   13, Str.STR_SIGNAL_SELECTION     , Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,    0,  139,   14,  114, 0x0                      , Str.STR_NULL),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   22,   30,   29,   39, Sprite.SPR_ARROW_LEFT           , Str.STR_SIGNAL_TYPE_TIP),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   43,   64,   24,   45, 0x50B                    , Str.STR_SIGNAL_TYPE_TIP),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,   75,   96,   24,   45, Sprite.SPR_SEMA                 , Str.STR_SIGNAL_TYPE_TIP),
			new Widget(   Window.WWT_PANEL,    Window.RESIZE_NONE,    7,  109,  117,   29,   39, Sprite.SPR_ARROW_RIGHT          , Str.STR_SIGNAL_TYPE_TIP),
			new Widget(   Window.WWT_IMGBTN,   Window.RESIZE_NONE,    3,   25,   34,   72,   80, 0x0                      , Str.STR_SIGNAL_DENSITY_TIP),
			new Widget(   Window.WWT_IMGBTN,   Window.RESIZE_NONE,    3,   35,   44,   72,   80, 0x0                      , Str.STR_SIGNAL_DENSITY_TIP),
			new Widget(   Window.WWT_TEXTBTN,  Window.RESIZE_NONE,    7,   20,  119,   84,   95, Str.STR_SIGNAL_COMPLETION    , Str.STR_SIGNAL_COMPLETION_TIP),
			new Widget(   Window.WWT_6,        Window.RESIZE_NONE,    7,   10,  129,   99,  110, Str.STR_SIGNAL_PRESIG_COMBO  , Str.STR_SIGNAL_PRESIG_TIP),
			new Widget(   Window.WWT_CLOSEBOX, Window.RESIZE_NONE,    7,  118,  128,  100,  109, Str.STR_0225                 , Str.STR_SIGNAL_PRESIG_TIP),
			//{   WIDGETS_END},
	};

	static final WindowDesc _build_signal_desc = new WindowDesc(
			-1,-1, 140, 115,
			Window.WC_BUILD_SIGNALS,Window.WC_BUILD_TOOLBAR,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_signal_widgets,
			RailGui::BuildSignalWndProc
			);

	static void ShowSignalBuilder()
	{
		_cur_presig_type = 0;
		Window.AllocateWindowDesc(_build_signal_desc);
	}



	public static void InitializeRailGui()
	{
		_build_depot_direction = 3;
		_railstation.numtracks = 1;
		_railstation.platlength = 1;
		_railstation.dragdrop = true;
		_cur_signal_type = 0;
		_cur_presig_type = 0;
		_cur_autosig_compl = false;
	}

	
	
}
