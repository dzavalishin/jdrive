package com.dzavalishin.xui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.AirCraft;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StationID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.tables.StationTables;
import com.dzavalishin.util.BinaryString;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Strings;

public class StationGui extends StationTables  // to get constants
{
	static final int _rating_colors[] = {152,32,15,174,208,194,191,55,184,10,191,48};


	static void StationsWndShowStationRating(int x, int y, int type, int acceptance, int rating)
	{
		int color = _rating_colors[type];
		int w;

		if (acceptance > 575) acceptance = 575;

		acceptance = (acceptance + 7) / 8;

		/* draw cargo */
		w = acceptance / 8;
		if (w != 0) {
			Gfx.GfxFillRect(x, y, x + w - 1, y + 6, color);
			x += w;
		}

		w = acceptance % 8;
		if (w != 0) {
			if (w == 7) w--;
			Gfx.GfxFillRect(x, y + (w - 1), x, y + 6, color);
		}

		x -= acceptance / 8;

		Gfx.DrawString(x + 1, y, Global._cargoc.names_short[type], 0x10);

		/* draw green/red ratings bar */
		Gfx.GfxFillRect(x + 1, y + 8, x + 7, y + 8, 0xB8);

		rating >>= 5;

		if (rating != 0) Gfx.GfxFillRect(x + 1, y + 8, x + rating, y + 8, 0xD0);
	}

	//static char _bufcache[64];
	//static int _last_station_idx;

	static class StationNameSorter implements Comparator<Station>
	{
		public int compare(Station a, Station b)
		{
			String buf1 = Strings.GetStringWithArgs(Str.STR_STATION, a.getIndex());
			String buf2 = Strings.GetStringWithArgs(Str.STR_STATION, b.getIndex());
			return buf1.compareTo(buf2);
		}
	}

	private static class PlayerStations {
		boolean dirtySort = true;
		List<Station> stations = new ArrayList<>();
	}

	private static Map<Integer, PlayerStations> playersStations;

	public static void requestSortStations() {
		playersStations = null;
	}

	public static void requestSortStations(int player) {
		if (!isSortedListDirty()) {
			PlayerStations info = playersStations.get(player);
			if (info != null) {
				info.dirtySort = true;
			}
		}
	}

	private static boolean isSortedListDirty(int player) {
		return isSortedListDirty() || playersStations.get(player) == null || playersStations.get(player).dirtySort;
	}

	private static boolean isSortedListDirty() {
		return playersStations == null;
	}

	static void GlobalSortStationList()
	{
		playersStations = new HashMap<>();

		Station.forEach( (station) -> {
			if (station.isValid() && station.getOwner().isNotNone()) {
				playersStations.computeIfAbsent(station.getOwner().id, (key) -> new PlayerStations() ).stations.add(station);
			}
		});

		Global.DEBUG_misc( 1, "Resorting global station list...");
	}

	static void MakeSortedStationList(int owner)
	{
		PlayerStations ps = playersStations.computeIfAbsent(owner, (key) -> new PlayerStations());
		ps.stations.sort(new StationNameSorter());
		ps.dirtySort = false;

		Global.DEBUG_misc( 1, "Resorting Stations list player %d...", owner+1);
	}

	static void PlayerStationsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			//final PlayerID 
			int owner = w.window_number;

			// resort station window if stations have been added/removed
			if (isSortedListDirty()) GlobalSortStationList();
			if (isSortedListDirty(owner)) MakeSortedStationList(owner);

			List<Station> stations = playersStations.get(owner).stations;
			w.SetVScrollCount(stations.size());

			/* draw widgets, with player's name in the caption */
			{
				final Player player = Player.GetPlayer(owner);
				Global.SetDParam(0, player.getName_1());
				Global.SetDParam(1, player.getName_2());
				Global.SetDParam(2, w.vscroll.getCount());
				w.DrawWindowWidgets();
			}

			{
				byte p = 0;
				int xb = 2;
				int y = 16; // offset from top of widget

				if (w.vscroll.getCount() == 0) { // player has no stations
					Gfx.DrawString(xb, y, Str.STR_304A_NONE, 0);
					return;
				}

				for(Station station: stations) {
					int j;
					int x;

					assert(station.isValid() && station.getOwner().id == owner);

					Global.SetDParam(0, station.getIndex());
					Global.SetDParam(1, station.getFacilities());
					x = Gfx.DrawString(xb, y, Str.STR_3049_0, 0) + 5;

					// show cargo waiting and station ratings
					for (j = 0; j != AcceptedCargo.NUM_CARGO; j++) {
						int acc = BitOps.GB(station.goods[j].waiting_acceptance, 0, 12);

						if (acc != 0) {
							StationsWndShowStationRating(x, y, j, acc, station.goods[j].rating);
							x += 10;
						}
					}
					y += 10;
					if (++p == w.vscroll.getCap()) break; // max number of stations in 1 window
				}
			}
		} break;
		case WE_CLICK: {
			switch (e.widget) {
			case 3: {
				int id_v = (e.pt.y - 15) / 10;

				if (id_v >= w.vscroll.getCap()) return; // click out of bounds

				id_v += w.vscroll.pos;

				{
					final PlayerID owner = PlayerID.get( w.window_number );
					final Station  st;

					if (id_v >= playersStations.get(owner.id).stations.size()) return; // click out of station bound

					st = playersStations.get(owner.id).stations.get(id_v);

					assert(st.getXy() != null && st.getOwner() == owner);

					ViewPort.ScrollMainWindowToTile(st.getXy());
				}
			} break;
			}
		} break;

		case WE_4:
			w.as_plstations_d().refresh_counter++;
			if (w.as_plstations_d().refresh_counter == 5) {
				w.as_plstations_d().refresh_counter = 0;
				w.SetWindowDirty();
			}
			break;

		case WE_RESIZE:
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 10);
			break;
		default:
			break;
		}
	}

	static final Widget _player_stations_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   345,     0,    13, Str.STR_3048_STATIONS,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   346,   357,     0,    13, 0x0,											Str.STR_STICKY_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_RB,    14,     0,   345,    14,   137, 0x0,											Str.STR_3057_STATION_NAMES_CLICK_ON),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   346,   357,    14,   125, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   346,   357,   126,   137, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _player_stations_desc = new WindowDesc(
			-1, -1, 358, 138,
			Window.WC_STATION_LIST,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_player_stations_widgets,
			StationGui::PlayerStationsWndProc
			);


	static void ShowPlayerStations(/*PlayerID*/ int player)
	{
		Window w;

		w = Window.AllocateWindowDescFront(_player_stations_desc, player);
		if (w != null) {
			w.caption_color = (byte)w.window_number;
			w.vscroll.setCap(12);
			w.resize.step_height = 10;
			w.resize.height = w.height - 10 * 7; // minimum if 5 in the list
		}
	}

	static final Widget _station_view_expanded_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   236,     0,    13, Str.STR_300A_0,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    14,   237,   248,     0,    13, 0x0,         Str.STR_STICKY_BUTTON),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   236,    14,    65, 0x0,					Str.STR_NULL),
			new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,    14,   237,   248,    14,    65, 0x0,					Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   248,    66,   197, 0x0,					Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    63,   198,   209, Str.STR_00E4_LOCATION,	Str.STR_3053_CENTER_MAIN_VIEW_ON_STATION),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,    64,   128,   198,   209, Str.STR_3033_ACCEPTS,	Str.STR_3056_SHOW_LIST_OF_ACCEPTED_CARGO),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   129,   192,   198,   209, Str.STR_0130_RENAME,		Str.STR_3055_CHANGE_NAME_OF_STATION),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   193,   206,   198,   209, Str.STR_TRAIN, Str.STR_SCHEDULED_TRAINS_TIP ),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   207,   220,   198,   209, Str.STR_LORRY, Str.STR_SCHEDULED_ROAD_VEHICLES_TIP ),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   221,   234,   198,   209, Str.STR_PLANE, Str.STR_SCHEDULED_AIRCRAFT_TIP ),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   235,   248,   198,   209, Str.STR_SHIP, Str.STR_SCHEDULED_SHIPS_TIP ),
	};

	static final Widget _station_view_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   236,     0,    13, Str.STR_300A_0,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    14,   237,   248,     0,    13, 0x0,         Str.STR_STICKY_BUTTON),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   236,    14,    65, 0x0,					Str.STR_NULL),
			new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,    14,   237,   248,    14,    65, 0x0,					Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   248,    66,    97, 0x0,					Str.STR_NULL),
			new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,    63,    98,   109, Str.STR_00E4_LOCATION,	Str.STR_3053_CENTER_MAIN_VIEW_ON_STATION),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,    64,   128,    98,   109, Str.STR_3032_RATINGS,	Str.STR_3054_SHOW_STATION_RATINGS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   129,   192,    98,   109, Str.STR_0130_RENAME,		Str.STR_3055_CHANGE_NAME_OF_STATION),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   193,   206,    98,   109, Str.STR_TRAIN, Str.STR_SCHEDULED_TRAINS_TIP ),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   207,   220,    98,   109, Str.STR_LORRY, Str.STR_SCHEDULED_ROAD_VEHICLES_TIP ),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   221,   234,    98,   109, Str.STR_PLANE, Str.STR_SCHEDULED_AIRCRAFT_TIP ),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   235,   248,    98,   109, Str.STR_SHIP, Str.STR_SCHEDULED_SHIPS_TIP ),
	};

	static void DrawStationViewWindow(Window w)
	{
		//StationID 
		int station_id = w.window_number;
		final Station  st = Station.GetStation(station_id);
		int i;
		int num;
		int x,y;
		int pos;
		//StringID 
		int str;

		num = 1;
		for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
			if (BitOps.GB(st.goods[i].waiting_acceptance, 0, 12) != 0) {
				num++;
				if (st.goods[i].enroute_from != station_id) num++;
			}
		}
		w.SetVScrollCount( num);

		w.disabled_state = st.getOwner().isLocalPlayer() ? 0 : (1 << 9);

		/*
		if (0==(st.facilities & FACIL_TRAIN)) 		w.disabled_state = BitOps.RETSETBIT(w.disabled_state,  10);
		if (0==(st.facilities & FACIL_TRUCK_STOP) &&
				0==(st.facilities & FACIL_BUS_STOP))  w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 11);
		if (0==(st.facilities & FACIL_AIRPORT))       w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 12);
		if (0==(st.facilities & FACIL_DOCK))          w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 13);
		*/
		
		if (!st.hasFacility(FACIL_TRAIN)) 		w.disabled_state = BitOps.RETSETBIT(w.disabled_state,  10);
		if (!st.hasFacility(FACIL_TRUCK_STOP) &&
				!st.hasFacility(FACIL_BUS_STOP))  w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 11);
		if (!st.hasFacility(FACIL_AIRPORT))       w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 12);
		if (!st.hasFacility(FACIL_DOCK))          w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 13);
		
		Global.SetDParam(0, st.getIndex());
		Global.SetDParam(1, st.getFacilities());
		w.DrawWindowWidgets();

		x = 2;
		y = 15;
		pos = w.vscroll.pos;

		if (--pos < 0) {
			str = Str.STR_00D0_NOTHING;
			for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
				if (BitOps.GB(st.goods[i].waiting_acceptance, 0, 12) != 0) str = Str.STR_EMPTY;
			}
			Global.SetDParam(0, str);
			Gfx.DrawString(x, y, Str.STR_0008_WAITING, 0);
			y += 10;
		}

		i = 0;
		do {
			int waiting = BitOps.GB(st.goods[i].waiting_acceptance, 0, 12);
			if (waiting == 0) continue;

			num = (waiting + 5) / 10;
			if (num != 0) {
				int cur_x = x;
				num = Math.min(num, 23);
				do {
					Gfx.DrawSprite(Global._cargoc.sprites[i], cur_x, y);
					cur_x += 10;
				} while (--num > 0);
			}

			if ( st.goods[i].enroute_from == station_id) {
				if (--pos < 0) {
					Global.SetDParam(1, waiting);
					Global.SetDParam(0, Global._cargoc.names_long[i]);
					Gfx.DrawStringRightAligned(x + 234, y, Str.STR_0009, 0);
					y += 10;
				}
			} else {
				/* enroute */
				if (--pos < 0) {
					Global.SetDParam(1, waiting);
					Global.SetDParam(0, Global._cargoc.names_long[i]);
					Gfx.DrawStringRightAligned(x + 234, y, Str.STR_000A_EN_ROUTE_FROM, 0);
					y += 10;
				}

				if (pos > -5 && --pos < 0) {
					Global.SetDParam(0, st.goods[i].enroute_from);
					Gfx.DrawStringRightAligned(x + 234, y, Str.STR_000B, 0);
					y += 10;
				}
			}
		} while (pos > -5 && ++i != AcceptedCargo.NUM_CARGO);

		if (Window.IsWindowOfPrototype( w, _station_view_widgets)) {
			//char *b = _userstring;
			BinaryString sb = new BinaryString();

			sb.appendInlineString(Str.STR_000C_ACCEPTS);

			boolean nonempty = false;
			for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
				//if (b >= endof(_userstring) - 5 - 1) break;
				if( 0 != (st.goods[i].waiting_acceptance & 0x8000) ) 
				{
					sb.appendInlineString( Global._cargoc.names_s[i]);
					//*b++ = ',';
					//*b++ = ' ';
					sb.append( ", " );
					nonempty = true;
				}
			}

			if (nonempty) {				
				Strings._userstring = sb;
				//Strings._userstring = Strings._userstring.substring(0, Strings._userstring.length()-2 );
				sb.setLength(sb.length()-2);
			} else {
				sb = new BinaryString();
				sb.appendInlineString(Str.STR_000C_ACCEPTS);
				sb.appendInlineString(Str.STR_00D0_NOTHING);
				Strings._userstring = sb;
			}

			Gfx.DrawStringMultiLine(2, 67, new StringID( Strings.STR_SPEC_USERSTRING ), 245);
		} else {
			Gfx.DrawString(2, 67, Str.STR_3034_LOCAL_RATING_OF_TRANSPORT, 0);

			y = 77;
			for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
				if (st.goods[i].enroute_from != INVALID_STATION) {
					Global.SetDParam(0, Global._cargoc.names_s[i]);
					Global.SetDParam(2, st.goods[i].rating * 101 >> 8);
					Global.SetDParam(1, Str.STR_3035_APPALLING + (st.goods[i].rating >> 5));
					Gfx.DrawString(8, y, Str.STR_303D, 0);
					y += 10;
				}
			}
		}
	}


	static void StationViewWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			DrawStationViewWindow(w);
			break;

		case WE_CLICK:
			switch (e.widget) {
			case 7:
				ViewPort.ScrollMainWindowToTile(Station.GetStation(w.window_number).getXy());
				break;

			case 8:
				w.SetWindowDirty();

				/* toggle height/widget set */
				if (Window.IsWindowOfPrototype(w, _station_view_expanded_widgets)) {
					w.AssignWidgetToWindow(_station_view_widgets);
					w.height = 110;
				} else {
					w.AssignWidgetToWindow( _station_view_expanded_widgets);
					w.height = 210;
				}

				w.SetWindowDirty();
				break;

			case 9: {
				Global.SetDParam(0, w.window_number);
				MiscGui.ShowQueryString( new StringID( Str.STR_STATION ), new StringID( Str.STR_3030_RENAME_STATION_LOADING ), 31, 180, w.getWindow_class(), w.window_number);
			} break;

			case 10: { /* Show a list of scheduled trains to this station */
				final Station st = Station.GetStation(w.window_number);
				TrainGui.ShowPlayerTrains(st.getOwner().id, w.window_number);
				break;
			}

			case 11: { /* Show a list of scheduled road-vehicles to this station */
				final Station st = Station.GetStation(w.window_number);
				RoadVehGui.ShowPlayerRoadVehicles(st.getOwner().id, w.window_number);
				break;
			}

			case 12: { /* Show a list of scheduled aircraft to this station */
				final Station st = Station.GetStation(w.window_number);
				/* Since oilrigs have no owners, show the scheduled aircraft of current player */
				PlayerID owner = (st.getOwner().isNone()) ? PlayerID.getCurrent() : st.getOwner();
				AirCraft.ShowPlayerAircraft(owner.id, w.window_number);
				break;
			}

			case 13: { /* Show a list of scheduled ships to this station */
				final Station st = Station.GetStation(w.window_number);
				/* Since oilrigs/bouys have no owners, show the scheduled ships of current player */
				PlayerID owner = (st.getOwner().isNone()) ? PlayerID.getCurrent() : st.getOwner();
				ShipGui.ShowPlayerShips(owner, StationID.get( w.window_number ) );
				break;
			}
			}
			break;

		case WE_ON_EDIT_TEXT:
			if (e.str != null) {
				Global._cmd_text = e.str;
				Cmd.DoCommandP(null, w.window_number, 0, null,
						Cmd.CMD_RENAME_STATION | Cmd.CMD_MSG(Str.STR_3031_CAN_T_RENAME_STATION));
			}
			break;

		case WE_DESTROY: {
			//WindowNumber 
			int wno =
					(w.window_number << 16) | Station.GetStation(w.window_number).getOwner().id;

			Window.DeleteWindowById(Window.WC_TRAINS_LIST, wno);
			Window.DeleteWindowById(Window.WC_ROADVEH_LIST, wno);
			Window.DeleteWindowById(Window.WC_SHIPS_LIST, wno);
			Window.DeleteWindowById(Window.WC_AIRCRAFT_LIST, wno);
			break;
		}
		default:
			break;
		}
	}


	static final WindowDesc _station_view_desc = new WindowDesc(
			-1, -1, 249, 110,
			Window.WC_STATION_VIEW,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON,
			_station_view_widgets,
			StationGui::StationViewWndProc
			);

	public static void ShowStationViewWindow(/*StationID*/ int station)
	{
		Window w;

		w = Window.AllocateWindowDescFront(_station_view_desc, station);
		if (w != null) {
			PlayerID owner = Station.GetStation(w.window_number).getOwner();
			if (owner.isNotNone()) w.caption_color =  owner.id;
			w.vscroll.setCap(5);
		}
	}


}
