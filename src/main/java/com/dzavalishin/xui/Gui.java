package com.dzavalishin.xui;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

import com.dzavalishin.console.Console;
import com.dzavalishin.console.ConsoleFactory;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.SwitchModes;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.game.AirCraft;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.GenerateWorld;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Industry;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.NewsItem;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.Rail;
import com.dzavalishin.game.SignStruct;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.SpriteCache;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Subsidies;
import com.dzavalishin.game.Terraform;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Town;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.game.WayPoint;
import com.dzavalishin.ids.CursorID;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ifaces.MenuClickedProc;
import com.dzavalishin.ifaces.OnButtonClick;
import com.dzavalishin.ifaces.ToolbarButtonProc;
import com.dzavalishin.net.DestType;
import com.dzavalishin.net.Net;
import com.dzavalishin.net.NetServer;
import com.dzavalishin.struct.ColorList;
import com.dzavalishin.struct.Point;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.GameDate;
import com.dzavalishin.util.ScreenShot;
import com.dzavalishin.util.ShortSounds;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.Strings;
import com.dzavalishin.net.NetClient;
import com.dzavalishin.net.NetGui;
import com.dzavalishin.net.NetworkAction;
import com.dzavalishin.net.NetworkPasswordType;

import static com.dzavalishin.enums.GameModes.*;

public class Gui
{


	//enum { // max 32 - 4 = 28 types
	public static final int GUI_PlaceProc_DemolishArea    = 0 << 4;
	public static final int GUI_PlaceProc_LevelArea       = 1 << 4;
	public static final int GUI_PlaceProc_DesertArea      = 2 << 4;
	public static final int GUI_PlaceProc_WaterArea       = 3 << 4;
	public static final int GUI_PlaceProc_ConvertRailArea = 4 << 4;
	public static final int GUI_PlaceProc_RockyArea       = 5 << 4;
	//};


	/*	FIOS_TYPE_FILE, FIOS_TYPE_OLDFILE etc. different colours */
	//static byte _fios_colors[];


	//enum {
	public static final int ZOOM_IN = 0;
	public static final int ZOOM_OUT = 1;
	public static final int ZOOM_NONE = 2; // hack, used to update the button status
	//};


	public static int _station_show_coverage;






	public static void SetupColorsAndInitialWindow()
	{
		Window w;
		int width,height;

		setupColors();

		width = Hal._screen.width;
		height = Hal._screen.height;

		switch (Global._game_mode) {
		case GM_MENU:
			Window.deleteMain(); // [dz] hacked in
			w = Window.AllocateWindow(0, 0, width, height, Gui::MainWindowWndProc, Window.WC_MAIN_WINDOW, null);
			ViewPort.AssignWindowViewport( w, 0, 0, width, height, new TileIndex(32, 32).getTile(), 0);
			IntroGui.ShowSelectGameWindow();
			break;
			
		case GM_NORMAL:
			Window.deleteMain(); // [dz] hacked in
			w = Window.AllocateWindow(0, 0, width, height, Gui::MainWindowWndProc, Window.WC_MAIN_WINDOW, null);
			ViewPort.AssignWindowViewport(w, 0, 0, width, height, new TileIndex(32, 32).getTile(), 0);

			ShowVitalWindows();

			
			// Bring joining GUI to front till the client is really joined 
			if (Global._networking && !Global._network_server)
				NetGui.ShowJoinStatusWindowAfterJoin();
			
			
			break;
			
		case GM_EDITOR:
			Window.deleteMain();
			w = Window.AllocateWindow(0, 0, width, height, Gui::MainWindowWndProc, Window.WC_MAIN_WINDOW, null);
			ViewPort.AssignWindowViewport(w, 0, 0, width, height, 0, 0);

			w = Window.AllocateWindowDesc(_toolb_scen_desc,0);
			w.disabled_state = 1 << 9;
			w.flags4 = BitOps.RETCLRBITS(w.flags4, Window.WF_WHITE_BORDER_MASK);

			Window.PositionMainToolbar(w); // already WC_MAIN_TOOLBAR passed (&_toolb_scen_desc)
			break;

		default:
			assert false;
		}
	}
	
	private static void setupColors() 
	{
		for (int i = 0; i != 16; i++) {
			final byte[] b = SpriteCache.GetNonSprite(0x307 + i);

			assert(b != null);
			Global._color_list[i] = new ColorList( BitOps.subArray(b, 0xC6) );
		}
	}



	/* Min/Max date for scenario editor */
	static final int MinDate = 0;     // 1920-01-01 (MAX_YEAR_BEGIN_REAL)
	static final int MaxDate = 29220; // 2000-01-01

	static int _rename_id;
	static int _rename_what;

	static byte _terraform_size = 1;
	static /* RailType */ int _last_built_railtype;

	static void HandleOnEditTextCancel()
	{
		switch (_rename_what) {
		case 4:
			Net.NetworkDisconnect();
			NetGui.ShowNetworkGameWindow();
			break;
		}
	}

	static void HandleOnEditText(WindowEvent e)
	{
		//final char *b = e.edittext.str;
		int id;

		Global._cmd_text = e.str;

		id = _rename_id;

		switch (_rename_what) {
		case 0: /* Rename a s sign, if string is empty, delete sign */
			Cmd.DoCommandP(null, id, 0, null, Cmd.CMD_RENAME_SIGN | Cmd.CMD_MSG(Str.STR_280C_CAN_T_CHANGE_SIGN_NAME));
			break;
		case 1: /* Rename a waypoint */
			//if (*b == '\0') return;
			if( e.str == null || e.str.length() == 0)
				return;
			Cmd.DoCommandP(null, id, 0, null, Cmd.CMD_RENAME_WAYPOINT | Cmd.CMD_MSG(Str.STR_CANT_CHANGE_WAYPOINT_NAME));
			break;
	
		case 2: // Speak to.. 
			if (!Global._network_server)
				try {
					NetClient.NetworkPacketSend_PACKET_CLIENT_CHAT_command(NetworkAction.uiAction(id), DestType.value( id & 0xFF ), (id >> 8) & 0xFF, e.str);
				} catch (IOException e1) {
					Global.error(e1);
				}
			else
				try {
					NetServer.NetworkServer_HandleChat( NetworkAction.uiAction(id), DestType.value(id & 0xFF), (id >> 8) & 0xFF, e.str, Net.NETWORK_SERVER_INDEX);
				} catch (IOException e2) {
					// e2.printStackTrace();
					Global.error(e2);
				}
			break;
			/*
		case 3: { // Give money, you can only give money in excess of loan 
			final Player p = Player.GetCurrentPlayer();
			// TODO _currency
			//long money = min(p.getMoney() - p.getCurrent_loan(), Integer.parseInt(e.str) / Currency._currency.rate);
			long money = Long.min(p.getMoney() - p.getCurrent_loan(), Integer.parseInt(e.str) / 1);
			String msg;

			money = BitOps.clamp(money, 0, 20000000); // Clamp between 20 million and 0

			// Give 'id' the money, and substract it from ourself TODO (int)money
			if (!Cmd.DoCommandP(null, (int)money, id, null, Cmd.CMD_GIVE_MONEY | Cmd.CMD_MSG(Str.STR_INSUFFICIENT_FUNDS))) break;

			// Inform the player of this action
			//snprintf(msg, sizeof(msg), "%d", money);
			msg = Long.toString(money);

			// TODO server must be in _clients somehow - looked up by NETWORK_SERVER_INDEX
			if (!Global._network_server)
				NetClient.NetworkPacketSend_PACKET_CLIENT_CHAT_command(NetworkAction.GIVE_MONEY, DestType.PLAYER, id + 1, msg);
			else
				Net.NetworkServer_HandleChat(NetworkAction.GIVE_MONEY, DestType.PLAYER, id + 1, msg, NETWORK_SERVER_INDEX);
			break; 
		}*/
		case 4: // Game-Password and Company-Password 
			try {
				NetClient.NetworkPacketSend_PACKET_CLIENT_PASSWORD_command(NetworkPasswordType.value(id), e.str);
			} catch (IOException e1) {
				Global.error(e1);
			}
			break;
	
		}
	}

	/**
	 * This code is shared for the majority of the pushbuttons.
	 * Handles e.g. the pressing of a button (to build things), playing of click sound and sets certain parameters
	 *
	 * @param w Window which called the function
	 * @param widget ID of the widget (=button) that called this function
	 * @param cursor How should the cursor image change? E.g. cursor with depot image in it
	 * @param mode Tile highlighting mode, e.g. drawing a rectangle or a dot on the ground
	 * @param placeproc Procedure which will be called when someone clicks on the map

	 * @return true if the button is clicked, false if it's unclicked
	 */
	//boolean HandlePlacePushButton(Window w, int widget, CursorID cursor, int mode, Consumer<TileIndex> placeproc)
	public static boolean HandlePlacePushButton(Window w, int widget, int cursor, int mode, Consumer<TileIndex> placeproc)
	{
		int mask = 1 << widget;

		if( 0 != (w.disabled_state & mask)) return false;

		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();
		w.SetWindowDirty();

		if(0 != (w.click_state & mask)) {
			ViewPort.ResetObjectToPlace();
			return false;
		}

		ViewPort.SetObjectToPlace(cursor, mode, w.getWindow_class(), w.window_number);
		w.click_state |= mask;
		Global._place_proc = placeproc;
		return true;
	}

	public static boolean HandlePlacePushButton(Window w, int widget, CursorID cursor, int mode, Consumer<TileIndex> placeproc)
	{
		return HandlePlacePushButton(w, widget, cursor.id, mode, placeproc);
	}

	public static void CcPlaySound10(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) Sound.SndPlayTileFx(Snd.SND_12_EXPLOSION, tile);
	}


	static void ToolbarPauseClick(Window w)
	{
		if (Global._networking && !Global._network_server) return; // only server can pause the game

		if (Cmd.DoCommandP(null, Global._pause != 0 ? 0 : 1, 0, null, Cmd.CMD_PAUSE)) {
			//Sound.SndPlayFx(Snd.SND_15_BEEP);
			ShortSounds.playBlipSound();
		}
	}

	static void ToolbarFastForwardClick(Window w)
	{
		Global._fast_forward ^= true;
		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();
	}


	static void MenuClickSettings(int index)
	{
		switch (index) {
			case 0: SettingsGui.ShowGameOptions();      return;
			case 1: SettingsGui.ShowGameDifficulty();   return;
			case 2: SettingsGui.ShowPatchesSelection(); return;
			case 3: SettingsGui.ShowNewgrf();           return;

			case  5: Global._display_opt ^= Global.DO_SHOW_TOWN_NAMES;    break;
			case  6: Global._display_opt ^= Global.DO_SHOW_STATION_NAMES; break;
			case  7: Global._display_opt ^= Global.DO_SHOW_SIGNS;         break;
			case  8: Global._display_opt ^= Global.DO_WAYPOINTS;          break;
			case  9: Global._display_opt ^= Global.DO_FULL_ANIMATION;     break;
			case 10: Global._display_opt ^= Global.DO_FULL_DETAIL;        break;
			case 11: Global._display_opt ^= Global.DO_TRANS_BUILDINGS;    break;
			case 12: Global._display_opt ^= Global.DO_TRANS_SIGNS;        break;
		}
		Hal.MarkWholeScreenDirty();
	}

	static void MenuClickSaveLoad(int index)
	{
		if (Global._game_mode == GM_EDITOR) {
			switch (index) {
				case 0: MiscGui.ShowSaveLoadDialog(Global.SLD_SAVE_SCENARIO); break;
				case 1: MiscGui.ShowSaveLoadDialog(Global.SLD_LOAD_SCENARIO); break;
				case 2: IntroGui.AskExitToGameMenu();                   break;
				case 4: IntroGui.AskExitGame();                         break;
			}
		} else {
			switch (index) {
				case 0: MiscGui.ShowSaveLoadDialog(Global.SLD_SAVE_GAME); break;
				case 1: MiscGui.ShowSaveLoadDialog(Global.SLD_LOAD_GAME); break;
				case 2: IntroGui.AskExitToGameMenu();               break;
				case 3: IntroGui.AskExitGame();                     break;
			}
		}
	}

	static void MenuClickMap(int index)
	{
		switch (index) {
			case 0: SmallMapGui.ShowSmallMap();            break;
			case 1: SmallMapGui.ShowExtraViewPortWindow(); break;
			case 2: GraphGui.ShowSignList();            break;
		}
	}

	static void MenuClickTown(int index)
	{
		TownGui.ShowTownDirectory();
	}

	static void MenuClickScenMap(int index)
	{
		switch (index) {
			case 0: SmallMapGui.ShowSmallMap();            break;
			case 1: SmallMapGui.ShowExtraViewPortWindow(); break;
			case 2: GraphGui.ShowSignList();            break;
			case 3: TownGui.ShowTownDirectory();       break;
		}
	}

	static void MenuClickSubsidies(int index)
	{
		Subsidies.ShowSubsidiesList();
	}

	static void MenuClickStations(int index)
	{
		StationGui.ShowPlayerStations(index);
	}

	static void MenuClickFinances(int index)
	{
		PlayerGui.ShowPlayerFinances(index);
	}


	static void MenuClickCompany(int index)
	{
		if (Global._networking && index == 0) {	
			NetGui.ShowClientList();
		} else {
			if (Global._networking) index--;
			PlayerGui.ShowPlayerCompany(index);
		}
	}


	static void MenuClickGraphs(int index)
	{
		switch (index) {
			case 0: GraphGui.ShowOperatingProfitGraph();    break;
			case 1: GraphGui.ShowIncomeGraph();             break;
			case 2: GraphGui.ShowDeliveredCargoGraph();     break;
			case 3: GraphGui.ShowPerformanceHistoryGraph(); break;
			case 4: GraphGui.ShowCompanyValueGraph();       break;
			case 5: GraphGui.ShowCargoPaymentRates();       break;
		}
	}

	static void MenuClickLeague(int index)
	{
		switch (index) {
			case 0: GraphGui.ShowCompanyLeagueTable();      break;
			case 1: GraphGui.ShowPerformanceRatingDetail(); break;
		}
	}

	static void MenuClickIndustry(int index)
	{
		switch (index) {
			case 0: Industry.ShowIndustryDirectory();   break;
			case 1: Industry.ShowBuildIndustryWindow(); break;
		}
	}

	static void MenuClickShowTrains(int index)
	{
		TrainGui.ShowPlayerTrains(index, Station.INVALID_STATION);
	}

	static void MenuClickShowRoad(int index)
	{
		RoadVehGui.ShowPlayerRoadVehicles(index, Station.INVALID_STATION);
	}

	static void MenuClickShowShips(int index)
	{
		ShipGui.ShowPlayerShips(index, Station.INVALID_STATION);
	}

	static void MenuClickShowAir(int index)
	{
		AirCraft.ShowPlayerAircraft(index, Station.INVALID_STATION);
	}

	static void MenuClickBuildRail(int index)
	{
		_last_built_railtype = index; //RailType.values[index];
		RailGui.ShowBuildRailToolbar(_last_built_railtype, -1);
	}

	static void MenuClickBuildRoad(int index)
	{
		RoadGui.ShowBuildRoadToolbar();
	}

	static void MenuClickBuildWater(int index)
	{
		DockGui.ShowBuildDocksToolbar();
	}

	static void MenuClickBuildAir(int index)
	{
		AirportGui.ShowBuildAirToolbar();
	}

	

	public static void ShowNetworkChatQueryWindow(int desttype, int dest)
	{
		_rename_id = desttype + (dest << 8);
		_rename_what = 2;
		NetGui.ShowChatWindow(new StringID(Str.STR_EMPTY), new StringID(Str.STR_NETWORK_CHAT_QUERY_CAPTION), 150, 338, 1, 0);
	}

	public static void ShowNetworkGiveMoneyWindow(int player)
	{
		_rename_id = player;
		_rename_what = 3;
		MiscGui.ShowQueryString(Str.STR_EMPTY, Str.STR_NETWORK_GIVE_MONEY_CAPTION, 30, 180, 1, 0);
	}

	public static void ShowNetworkNeedGamePassword()
	{
		_rename_id = NetworkPasswordType.NETWORK_GAME_PASSWORD.ordinal();
		_rename_what = 4;
		MiscGui.ShowQueryString(Str.STR_EMPTY, Str.STR_NETWORK_NEED_GAME_PASSWORD_CAPTION, 20, 180, Window.WC_SELECT_GAME, 0);
	}

	public static void ShowNetworkNeedCompanyPassword()
	{
		_rename_id = NetworkPasswordType.NETWORK_COMPANY_PASSWORD.ordinal();
		_rename_what = 4;
		MiscGui.ShowQueryString(Str.STR_EMPTY, Str.STR_NETWORK_NEED_COMPANY_PASSWORD_CAPTION, 20, 180, Window.WC_SELECT_GAME, 0);
	}

	

	public static void ShowRenameSignWindow(final SignStruct ss)
	{
		_rename_id = ss.getIndex();
		_rename_what = 0;
		MiscGui.ShowQueryString(ss.getString(), new StringID( Str.STR_280B_EDIT_SIGN_TEXT ), 30, 180, 1 , 0 );
	}

	public static void ShowRenameWaypointWindow(final WayPoint wp)
	{
		int id = wp.index;

		/* Are we allowed to change the name of the waypoint? */
		if (!wp.xy.CheckTileOwnership()) {
			Global.ShowErrorMessage(Global._error_message, Str.STR_CANT_CHANGE_WAYPOINT_NAME,
					wp.xy.TileX() * 16, wp.xy.TileY() * 16);
			return;
		}

		_rename_id = id;
		_rename_what = 1;
		Global.SetDParam(0, id);
		MiscGui.ShowQueryString(new StringID( Str.STR_WAYPOINT_RAW), new StringID( Str.STR_EDIT_WAYPOINT_NAME), 30, 180, 1, 0);
	}

	static void SelectSignTool()
	{
		if (Hal._cursor.sprite.id == Sprite.SPR_CURSOR_SIGN) {
			ViewPort.ResetObjectToPlace();
		} else {
			ViewPort.SetObjectToPlace(Sprite.SPR_CURSOR_SIGN, 1, 1, 0);
			Global._place_proc = SignStruct::PlaceProc_Sign;
		}
	}

	static void MenuClickForest(int index)
	{
		switch (index) {
			case 0: Terraform.ShowTerraformToolbar();  break;
			case 1: MiscGui.ShowBuildTreesToolbar(); break;
			case 2: SelectSignTool();        break;
		}
	}

	static void MenuClickMusicWindow(int index)
	{
		MusicGui.ShowMusicWindow();
	}

	static void MenuClickNewspaper(int index)
	{
		switch (index) {
			case 0: NewsItem.ShowLastNewsMessage(); break;
			case 1: NewsItem.ShowMessageOptions();  break;
			case 2: NewsItem.ShowMessageHistory();  break;
		}
	}

	static void MenuClickHelp(int index)
	{
		switch (index) {
			case 0: MiscGui.PlaceLandBlockInfo(); break;
			case 2:
				ConsoleFactory.INSTANCE.getCurrentConsole().ifPresent(Console::switchState);
				break;
			case 3: ScreenShot._make_screenshot = 1; break;
			case 4: ScreenShot._make_screenshot = 2; break;
			case 5: MiscGui.ShowAboutWindow();    break;
		}
	}



	static final MenuClickedProc _menu_clicked_procs[] = {
		null, /* 0 */
		null, /* 1 */
		Gui::MenuClickSettings, /* 2 */
		Gui::MenuClickSaveLoad, /* 3 */
		Gui::MenuClickMap, /* 4 */
		Gui::MenuClickTown, /* 5 */
		Gui::MenuClickSubsidies, /* 6 */
		Gui::MenuClickStations, /* 7 */
		Gui::MenuClickFinances, /* 8 */
		Gui::MenuClickCompany, /* 9 */
		Gui::MenuClickGraphs, /* 10 */
		Gui::MenuClickLeague, /* 11 */
		Gui::MenuClickIndustry, /* 12 */
		Gui::MenuClickShowTrains, /* 13 */
		Gui::MenuClickShowRoad, /* 14 */
		Gui::MenuClickShowShips, /* 15 */
		Gui::MenuClickShowAir, /* 16 */
		Gui::MenuClickScenMap,  /* 17 */
		null, /* 18 */
		Gui::MenuClickBuildRail, /* 19 */
		Gui::MenuClickBuildRoad, /* 20 */
		Gui::MenuClickBuildWater, /* 21 */
		Gui::MenuClickBuildAir, /* 22 */
		Gui::MenuClickForest, /* 23 */
		Gui::MenuClickMusicWindow, /* 24 */
		Gui::MenuClickNewspaper, /* 25 */
		Gui::MenuClickHelp, /* 26 */
	};

	static void MenuWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int count,sel;
			int x,y;
			int chk;
			//StringID 
			int string;
			int eo;
			int inc;
			byte color;

			w.DrawWindowWidgets();

			count = w.as_menu_d().item_count;
			sel = w.as_menu_d().sel_index;
			chk = w.as_menu_d().checked_items;
			string = w.as_menu_d().string_id.id;

			x = 1;
			y = 1;

			eo = 157;

			inc = (chk != 0) ? 2 : 1;

			do {
				if (sel== 0) Gfx.GfxFillRect(x, y, x + eo, y+9, 0);
				color = (byte) ((sel == 0) ? 0xC : 0x10);
				if (BitOps.HASBIT(w.as_menu_d().disabled_items, (string - w.as_menu_d().string_id.id))) color = 0xE;
				Gfx.DrawString(x + 2, y, string + (chk & 1), color);
				y += 10;
				string += inc;
				chk >>= 1;
				--sel;
			} while (--count>0);
		} break;

		case WE_DESTROY: {
				Window v = Window.FindWindowById(Window.WC_MAIN_TOOLBAR, 0);
				v.click_state &= ~(1 << w.as_menu_d().main_button);
				v.SetWindowDirty();
				return;
			}

		case WE_POPUPMENU_SELECT: {
			int index = Window.GetMenuItemIndex(w, e.pt.x, e.pt.y);
			int action_id;


			if (index < 0) {
				Window w2 = Window.FindWindowById(Window.WC_MAIN_TOOLBAR,0);
				if (w2.GetWidgetFromPos( e.pt.x - w2.left, e.pt.y - w2.top) == w.as_menu_d().main_button)
					index = w.as_menu_d().sel_index;
			}

			action_id = w.as_menu_d().action_id;
			w.DeleteWindow();

			if (index >= 0) _menu_clicked_procs[action_id].accept(index);

			break;
			}

		case WE_POPUPMENU_OVER: {
			int index = Window.GetMenuItemIndex(w, e.pt.x, e.pt.y);

			if (index == -1 || index == w.as_menu_d().sel_index) return;

			w.as_menu_d().sel_index = index;
			w.SetWindowDirty();
			return;
			}
		default:
			break;
		}
	}

	static final Widget _menu_widgets[] = {
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   159,     0, 65535,     0,	Str.STR_NULL),
	};


	static final Widget _player_menu_widgets[] = {
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   240,     0,    81,     0,	Str.STR_NULL),
	};


	static int GetPlayerIndexFromMenu(int index)
	{
		if (index >= 0) {

			Iterator<Player> i = Player.getIterator();
			while(i.hasNext())
			{
				final Player p = i.next();
				if (p.isActive() && --index < 0) return p.getIndex().id;
			}
		}
		return -1;
	}

	static void UpdatePlayerMenuHeight(Window w)
	{
		int num = 0;

		Iterator<Player> i = Player.getIterator();
		while(i.hasNext())
		{
			final Player p = i.next();
			if (p.isActive()) num++;
		}

		// Increase one to fit in PlayerList in the menu when in network
		if (Global._networking && w.as_menu_d().main_button == 9) num++;

		if (w.as_menu_d().item_count != num) {
			w.as_menu_d().item_count = (byte) num;
			w.SetWindowDirty();
			num = num * 10 + 2;
			w.height = num;
			w.widget.get(0).bottom = w.widget.get(0).top + num - 1;
			w.SetWindowDirty();
		}
	}

	static void PlayerMenuWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int x,y;
			int sel, color;
			//Player p;
			int chk;

			UpdatePlayerMenuHeight(w);
			w.DrawWindowWidgets();

			x = 1;
			y = 1;
			sel = w.as_menu_d().sel_index;
			chk = w.as_menu_d().checked_items; // let this mean gray items.

			// 9 = playerlist
			if (Global._networking && w.as_menu_d().main_button == 9) {
				if (sel == 0) {
					Gfx.GfxFillRect(x, y, x + 238, y + 9, 0);
				}
				Gfx.DrawString(x + 19, y, Str.STR_NETWORK_CLIENT_LIST, 0x0);
				y += 10;
				sel--;
			}

			Iterator<Player> i = Player.getIterator();
			while(i.hasNext())
			{
				final Player p = i.next();
				if (p.isActive()) {
					if (p.getIndex().id == sel) {
						Gfx.GfxFillRect(x, y, x + 238, y + 9, 0);
					}

					GraphGui.DrawPlayerIcon(p.getIndex().id, x + 2, y + 1);

					Global.SetDParam(0, p.getName_1());
					Global.SetDParam(1, p.getName_2());
					Global.SetDParam(2, Player.GetPlayerNameString(p.getIndex(), 3));

					color = (p.getIndex().id == sel) ? 0xC : 0x10;
					if(0 != (chk&1)) color = 14;
					Gfx.DrawString(x + 19, y, Str.STR_7021, color);

					y += 10;
				}
				chk >>= 1;
			}

			break;
			}

		case WE_DESTROY: {
			Window v = Window.FindWindowById(Window.WC_MAIN_TOOLBAR, 0);
			v.click_state &= ~(1 << w.as_menu_d().main_button);
			v.SetWindowDirty();
			return;
			}

		case WE_POPUPMENU_SELECT: {
			int index = Window.GetMenuItemIndex(w, e.pt.x, e.pt.y);
			int action_id = w.as_menu_d().action_id;

			// We have a new entry at the top of the list of menu 9 when networking
			//  so keep that in count
			if (Global._networking && w.as_menu_d().main_button == 9) {
				if (index > 0) index = GetPlayerIndexFromMenu(index - 1) + 1;
			} else {
				index = GetPlayerIndexFromMenu(index);
			}

			if (index < 0) {
				Window w2 = Window.FindWindowById(Window.WC_MAIN_TOOLBAR,0);
				if (w2.GetWidgetFromPos(e.pt.x - w2.left, e.pt.y - w2.top) == w.as_menu_d().main_button)
					index = w.as_menu_d().sel_index;
			}

			w.DeleteWindow();

			if (index >= 0) {
				assert(index >= 0 && index < 30);
				_menu_clicked_procs[action_id].accept(index);
			}
			break;
			}
		case WE_POPUPMENU_OVER: {
			int index;
			UpdatePlayerMenuHeight(w);
			index = Window.GetMenuItemIndex(w, e.pt.x, e.pt.y);

			// We have a new entry at the top of the list of menu 9 when networking
			//  so keep that in count
			if (Global._networking && w.as_menu_d().main_button == 9) {
				if (index > 0) index = GetPlayerIndexFromMenu(index - 1) + 1;
			} else {
				index = GetPlayerIndexFromMenu(index);
			}

			if (index == -1 || index == w.as_menu_d().sel_index) return;

			w.as_menu_d().sel_index = index;
			w.SetWindowDirty();
			return;
			}
		default:
			break;
		}
	}

	//static Window PopupMainToolbMenu(Window w, int x, int main_button, StringID base_string, int item_count, byte disabled_mask)
	static Window PopupMainToolbMenu(Window w, int x, int main_button, int base_string, int item_count, int disabled_mask)
	{
		x += w.left;

		w.click_state = BitOps.RETSETBIT(w.click_state, (byte)main_button);
		w.InvalidateWidget( (byte)main_button);

		Window.DeleteWindowById(Window.WC_TOOLBAR_MENU, 0);

		w = Window.AllocateWindow(x, 0x16, 0xA0, item_count * 10 + 2, Gui::MenuWndProc, Window.WC_TOOLBAR_MENU, _menu_widgets);
		w.widget.get(0).bottom = item_count * 10 + 1;
		w.flags4 &= ~Window.WF_WHITE_BORDER_MASK;

		w.as_menu_d().item_count = item_count;
		w.as_menu_d().sel_index = 0;
		w.as_menu_d().main_button = main_button;
		w.as_menu_d().action_id = (0 != (main_button >> 8)) ? (main_button >> 8) : main_button;
		w.as_menu_d().string_id = new StringID( base_string );
		w.as_menu_d().checked_items = 0;
		w.as_menu_d().disabled_items = disabled_mask;

		Window._popup_menu_active = true;

		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();

		return w;
	}

	static Window PopupMainPlayerToolbMenu(Window w, int x, int main_button, int gray)
	{
		x += w.left;

		w.click_state = BitOps.RETSETBIT(w.click_state, main_button);
		w.InvalidateWidget(main_button);

		Window.DeleteWindowById(Window.WC_TOOLBAR_MENU, 0);
		w = Window.AllocateWindow(x, 0x16, 0xF1, 0x52, Gui::PlayerMenuWndProc, Window.WC_TOOLBAR_MENU, _player_menu_widgets);
		w.flags4 &= ~Window.WF_WHITE_BORDER_MASK;
		w.as_menu_d().item_count = 0;
		w.as_menu_d().sel_index = (Global.gs._local_player.id != Owner.OWNER_SPECTATOR) ? Global.gs._local_player.id : GetPlayerIndexFromMenu(0);
		if (Global._networking && main_button == 9) {
			if (Global.gs._local_player.id != Owner.OWNER_SPECTATOR) {
				w.as_menu_d().sel_index++;
			} else {
				/* Select client list by default for spectators */
				w.as_menu_d().sel_index = 0;
			}
		}
		w.as_menu_d().action_id = main_button;
		w.as_menu_d().main_button = main_button;
		w.as_menu_d().checked_items = gray;
		w.as_menu_d().disabled_items = 0;
		Window._popup_menu_active = true;
		
		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();
		
		return w;
	}

	static void ToolbarSaveClick(Window w)
	{
		PopupMainToolbMenu(w, 66, 3, Str.STR_015C_SAVE_GAME, 4, 0);
	}

	static void ToolbarMapClick(Window w)
	{
		PopupMainToolbMenu(w, 96, 4, Str.STR_02DE_MAP_OF_WORLD, 3, 0);
	}

	static void ToolbarTownClick(Window w)
	{
		PopupMainToolbMenu(w, 118, 5, Str.STR_02BB_TOWN_DIRECTORY, 1, 0);
	}

	static void ToolbarSubsidiesClick(Window w)
	{
		PopupMainToolbMenu(w, 140, 6, Str.STR_02DD_SUBSIDIES, 1, 0);
	}

	static void ToolbarStationsClick(Window w)
	{
		PopupMainPlayerToolbMenu(w, 162, 7, 0);
	}

	static void ToolbarMoneyClick(Window w)
	{
		PopupMainPlayerToolbMenu(w, 191, 8, 0);
	}

	static void ToolbarPlayersClick(Window w)
	{
		PopupMainPlayerToolbMenu(w, 213, 9, 0);
	}

	static void ToolbarGraphsClick(Window w)
	{
		PopupMainToolbMenu(w, 236, 10, Str.STR_0154_OPERATING_PROFIT_GRAPH, 6, 0);
	}

	static void ToolbarLeagueClick(Window w)
	{
		PopupMainToolbMenu(w, 258, 11, Str.STR_015A_COMPANY_LEAGUE_TABLE, 2, 0);
	}

	static void ToolbarIndustryClick(Window w)
	{
		PopupMainToolbMenu(w, 280, 12, Str.STR_INDUSTRY_DIR, 2, 0);
	}

	static void ToolbarTrainClick(Window w)
	{
		int [] dis = { -1 };

		Vehicle.forEach( (v) ->
		{
			if (v.getType() == Vehicle.VEH_Train && v.IsFrontEngine()) 
				dis[0] = BitOps.RETCLRBIT(dis[0], v.getOwner().id);
		});
		PopupMainPlayerToolbMenu(w, 310, 13, dis[0]);
	}

	static void ToolbarRoadClick(Window w)
	{
		int [] dis = {-1};

		Vehicle.forEach( (v) ->
		{
			if (v.getType() == Vehicle.VEH_Road) dis[0] = BitOps.RETCLRBIT(dis[0], v.getOwner().id);
		});
		PopupMainPlayerToolbMenu(w, 332, 14, dis[0]);
	}

	static void ToolbarShipClick(Window w)
	{
		int []dis = {-1};

		Vehicle.forEach( (v) ->
		{
			if (v.getType() == Vehicle.VEH_Ship) dis[0] = BitOps.RETCLRBIT(dis[0], v.getOwner().id);
		});
		PopupMainPlayerToolbMenu(w, 354, 15, dis[0]);
	}

	static void ToolbarAirClick(Window w)
	{
		int [] dis = {-1};

		Vehicle.forEach( (v) ->
		{
			if (v.getType() == Vehicle.VEH_Aircraft) dis[0] = BitOps.RETCLRBIT(dis[0], v.getOwner().id);
		});
		PopupMainPlayerToolbMenu(w, 376, 16, dis[0]);
	}

	/* Zooms a viewport in a window in or out */
	/* No button handling or what so ever */
	public static boolean DoZoomInOutWindow(int how, Window w)
	{
		ViewPort vp;
		int button;

		switch (Global._game_mode) {
			case GM_EDITOR: button = 9;  break;
			case GM_NORMAL: button = 17; break;
			default: return false;
		}

		assert(w != null);
		vp = w.getViewport();

		if (how == ZOOM_IN) {
			if (vp.zoom == 0) return false;
			vp.zoom--;
			vp.virtual_width >>= 1;
			vp.virtual_height >>= 1;

			w.as_vp_d().scrollpos_x += vp.virtual_width >> 1;
			w.as_vp_d().scrollpos_y += vp.virtual_height >> 1;

			w.SetWindowDirty();
		} else if (how == ZOOM_OUT) {
			if (vp.zoom == 2) return false;
			vp.zoom++;

			w.as_vp_d().scrollpos_x -= vp.virtual_width >> 1;
			w.as_vp_d().scrollpos_y -= vp.virtual_height >> 1;

			vp.virtual_width <<= 1;
			vp.virtual_height <<= 1;

			w.SetWindowDirty();
		}

		// routine to disable/enable the zoom buttons. Didn't know where to place these otherwise
		{
			Window wt = null;

			switch (w.getWindow_class()) {
				case Window.WC_MAIN_WINDOW:
					wt = Window.FindWindowById(Window.WC_MAIN_TOOLBAR, 0);
					break;

				case Window.WC_EXTRA_VIEW_PORT:
					wt = Window.FindWindowById(Window.WC_EXTRA_VIEW_PORT, w.window_number);
					button = 5;
					break;
			}

			assert(wt != null);

			// update the toolbar button too
			wt.disabled_state = BitOps.RETCLRBIT(wt.disabled_state, button);
			wt.disabled_state = BitOps.RETCLRBIT(wt.disabled_state, button + 1);
			switch (vp.zoom) {
				case 0: wt.disabled_state = BitOps.RETSETBIT(wt.disabled_state, button); break;
				case 2: wt.disabled_state = BitOps.RETSETBIT(wt.disabled_state, button + 1); break;
			}
			wt.SetWindowDirty();
		}

		return true;
	}

	@SuppressWarnings("StatementWithEmptyBody")
	static void MaxZoomIn()
	{
		while (DoZoomInOutWindow(ZOOM_IN, Window.getMain() ) )
			;
	}

	static void ToolbarZoomInClick(Window w)
	{
		if (DoZoomInOutWindow(ZOOM_IN, Window.getMain() )) {
			w.HandleButtonClick(17);
			//Sound.SndPlayFx(Snd.SND_15_BEEP);
			ShortSounds.playBlipSound();
		}
	}

	static void ToolbarZoomOutClick(Window w)
	{
		if (DoZoomInOutWindow( ZOOM_OUT, Window.getMain() )) {
			w.HandleButtonClick(18);
			//Sound.SndPlayFx(Snd.SND_15_BEEP);
			ShortSounds.playBlipSound();
		}
	}

	static void ToolbarBuildRailClick(Window w)
	{
		final Player p = Player.GetPlayer(Global.gs._local_player);
		Window w2;
		w2 = PopupMainToolbMenu(w, 457, 19, Str.STR_1015_RAILROAD_CONSTRUCTION, Rail.RAILTYPE_END, ~p.avail_railtypes);
		w2.as_menu_d().sel_index = _last_built_railtype;
	}

	static void ToolbarBuildRoadClick(Window w)
	{
		PopupMainToolbMenu(w, 479, 20, Str.STR_180A_ROAD_CONSTRUCTION, 1, 0);
	}

	static void ToolbarBuildWaterClick(Window w)
	{
		PopupMainToolbMenu(w, 501, 21, Str.STR_9800_DOCK_CONSTRUCTION, 1, 0);
	}

	static void ToolbarBuildAirClick(Window w)
	{
		PopupMainToolbMenu(w, 0x1E0, 22, Str.STR_A01D_AIRPORT_CONSTRUCTION, 1, 0);
	}

	static void ToolbarForestClick(Window w)
	{
		PopupMainToolbMenu(w, 0x1E0, 23, Str.STR_LANDSCAPING, 3, 0);
	}

	static void ToolbarMusicClick(Window w)
	{
		PopupMainToolbMenu(w, 0x1E0, 24, Str.STR_01D3_SOUND_MUSIC, 1, 0);
	}

	static void ToolbarNewspaperClick(Window w)
	{
		PopupMainToolbMenu(w, 0x1E0, 25, Str.STR_0200_LAST_MESSAGE_NEWS_REPORT, 3, 0);
	}

	static void ToolbarHelpClick(Window w)
	{
		PopupMainToolbMenu(w, 0x1E0, 26, Str.STR_02D5_LAND_BLOCK_INFO, 6, 0);
	}

	static void ToolbarOptionsClick(Window w)
	{
		int x;

		w = PopupMainToolbMenu(w,  43, 2, Str.STR_02C3_GAME_OPTIONS, 13, 0);

		x = -1;
		if(0 != (Global._display_opt & Global.DO_SHOW_TOWN_NAMES))    x = BitOps.RETCLRBIT(x,  5);
		if(0 != (Global._display_opt & Global.DO_SHOW_STATION_NAMES)) x = BitOps.RETCLRBIT(x,  6);
		if(0 != (Global._display_opt & Global.DO_SHOW_SIGNS))         x = BitOps.RETCLRBIT(x,  7);
		if(0 != (Global._display_opt & Global.DO_WAYPOINTS))          x = BitOps.RETCLRBIT(x,  8);
		if(0 != (Global._display_opt & Global.DO_FULL_ANIMATION))     x = BitOps.RETCLRBIT(x,  9);
		if(0 != (Global._display_opt & Global.DO_FULL_DETAIL))        x = BitOps.RETCLRBIT(x, 10);
		if(0 != (Global._display_opt & Global.DO_TRANS_BUILDINGS))    x = BitOps.RETCLRBIT(x, 11);
		if(0 != (Global._display_opt & Global.DO_TRANS_SIGNS))        x = BitOps.RETCLRBIT(x, 12);
		w.as_menu_d().checked_items = x;
	}


	static void ToolbarScenSaveOrLoad(Window w)
	{
		PopupMainToolbMenu(w, 0x2C, 3, Str.STR_0292_SAVE_SCENARIO, 5, 0);
	}

	static void ToolbarScenDateBackward(Window w)
	{
		// don't allow too fast scrolling
		if ((w.flags4 & Window.WF_TIMEOUT_MASK) <= 2 << Window.WF_TIMEOUT_SHL) {
			w.HandleButtonClick(6);
			w.InvalidateWidget(5);

			if (Global.get_date() > MinDate) Global.gs.date.SetDate(GameDate.ConvertYMDToDay(Global.get_cur_year() - 1, 0, 1));
		}
		Window._left_button_clicked = false;
	}

	static void ToolbarScenDateForward(Window w)
	{
		// don't allow too fast scrolling
		if ((w.flags4 & Window.WF_TIMEOUT_MASK) <= 2 << Window.WF_TIMEOUT_SHL) {
			w.HandleButtonClick(7);
			w.InvalidateWidget(5);

			if (Global.get_date() < MaxDate) Global.gs.date.SetDate(GameDate.ConvertYMDToDay(Global.get_cur_year() + 1, 0, 1));
		}
		Window._left_button_clicked = false;
	}

	static void ToolbarScenMapTownDir(Window w)
	{
		PopupMainToolbMenu(w, 0x16A, 8 | (17<<8), Str.STR_02DE_MAP_OF_WORLD, 4, 0);
	}

	static void ToolbarScenZoomIn(Window w)
	{
		if (DoZoomInOutWindow(ZOOM_IN, Window.getMain())) {
			w.HandleButtonClick(9);
			//Sound.SndPlayFx(Snd.SND_15_BEEP);
			ShortSounds.playBlipSound();
		}
	}

	static void ToolbarScenZoomOut(Window w)
	{
		if (DoZoomInOutWindow(ZOOM_OUT, Window.getMain())) {
			w.HandleButtonClick(10);
			//Sound.SndPlayFx(Snd.SND_15_BEEP);
			ShortSounds.playBlipSound();
		}
	}

	static void ZoomInOrOutToCursorWindow(boolean in, Window w)
	{
		ViewPort vp;
		Point pt;

		assert(w != null);

		vp = w.getViewport();

		if (Global._game_mode != GM_MENU) {
			if ((in && vp.zoom == 0) || (!in && vp.zoom == 2))
				return;

			pt = ViewPort.GetTileZoomCenterWindow(in,w);
			if (pt.x != -1) {
				ViewPort.ScrollWindowTo(pt.x, pt.y, w);

				DoZoomInOutWindow(in ? ZOOM_IN : ZOOM_OUT, w);
			}
		}
	}

	static void ResetLandscape()
	{
		Global._random_seeds[0][0] = Hal.InteractiveRandom();
		Global._random_seeds[0][1] = Hal.InteractiveRandom();

		GenerateWorld.doGenerateWorld(1, 1 << Global._patches.map_x, 1 << Global._patches.map_y);
		Hal.MarkWholeScreenDirty();
	}

	static final Widget _ask_reset_landscape_widgets[] = {
		new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     4,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
		new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     4,    11,   179,     0,    13, Str.STR_022C_RESET_LANDSCAPE,	Str.STR_NULL),
		new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     4,     0,   179,    14,    91, 0x0,												Str.STR_NULL),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    12,    25,    84,    72,    83, Str.STR_00C9_NO,								Str.STR_NULL),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    12,    95,   154,    72,    83, Str.STR_00C8_YES,							Str.STR_NULL),
	};

	// Ask first to reset landscape or to make a random landscape
	static void AskResetLandscapeWndProc(Window w, WindowEvent e)
	{
		int mode = w.window_number;

		switch (e.event) {
		case WE_PAINT:
			w.DrawWindowWidgets();
			Gfx.DrawStringMultiCenter(
				90, 38,
				mode != 0 ? Str.STR_022D_ARE_YOU_SURE_YOU_WANT_TO : Str.STR_GENERATE_RANDOM_LANDSCAPE,
				168
			);
			break;
		case WE_CLICK:
			switch (e.widget) {
			case 3:
				w.DeleteWindow();
				break;
			case 4:
				w.DeleteWindow();
				Window.DeleteWindowByClass(Window.WC_INDUSTRY_VIEW);
				Window.DeleteWindowByClass(Window.WC_TOWN_VIEW);
				Window.DeleteWindowByClass(Window.WC_LAND_INFO);

				if (mode!=0) { // reset landscape
					ResetLandscape();
				} else { // make random landscape
					//Sound.SndPlayFx(Snd.SND_15_BEEP);
					ShortSounds.playBlipSound();
					Global._switch_mode = SwitchModes.SM_GENRANDLAND;
				}
				break;
			}
			break;
		default:
			break;
		}
	}

	static final WindowDesc _ask_reset_landscape_desc = new WindowDesc(
		230,205, 180, 92,
		Window.WC_ASK_RESET_LANDSCAPE,0,
		WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_ask_reset_landscape_widgets,
		Gui::AskResetLandscapeWndProc
	);

	static void AskResetLandscape(int mode)
	{
		Window.AllocateWindowDescFront(_ask_reset_landscape_desc, mode);
	}

	// TODO - Incorporate into game itself to allow for ingame raising/lowering of
	// larger chunks at the same time OR remove altogether, as we have 'level land' ?
	/**
	 * Raise/Lower a bigger chunk of land at the same time in the editor. When
	 * raising get the lowest point, when lowering the highest point, and set all
	 * tiles in the selection to that height.
	 * @param tile The top-left tile where the terraforming will start
	 * @param mode 1 for raising, 0 for lowering land
	 */
	static void CommonRaiseLowerBigLand(TileIndex tile, int mode)
	{
		int sizex, sizey;
		int [] h = { Integer.MAX_VALUE };

		Global._error_message_2 = mode != 0 ? Str.STR_0808_CAN_T_RAISE_LAND_HERE : Str.STR_0809_CAN_T_LOWER_LAND_HERE;

		Global._generating_world = true; // used to create green terraformed land

		if (_terraform_size == 1) {
			Cmd.DoCommandP(tile, 8, mode, Terraform::CcTerraform, Cmd.CMD_TERRAFORM_LAND | Cmd.CMD_AUTO | Cmd.CMD_MSG(Global._error_message_2));
		} else {
			Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, tile);

			assert(_terraform_size != 0);
			// check out for map overflows
			sizex = Math.min(Global.MapSizeX() - tile.TileX() - 1, _terraform_size);
			sizey = Math.min(Global.MapSizeY() - tile.TileY() - 1, _terraform_size);

			if (sizex == 0 || sizey == 0) return;

			if (mode != 0) {
				/* Raise land */
				h[0] = 15; // XXX - max height
				//BEGIN_TILE_LOOP(tile2, sizex, sizey, tile) 
				TileIndex.forAll( sizex, sizey, tile, (tile2) ->
				{
					h[0] = Math.min(h[0], tile2.TileHeight());
					return false;
				});// END_TILE_LOOP(tile2, sizex, sizey, tile)
			} else {
				/* Lower land */
				h[0] = 0;
				//BEGIN_TILE_LOOP(tile2, sizex, sizey, tile) 
				TileIndex.forAll( sizex, sizey, tile, (tile2) ->
				{
					h[0] = Math.max(h[0], tile2.TileHeight());
					return false;
				}); //END_TILE_LOOP(tile2, sizex, sizey, tile)
			}

			//BEGIN_TILE_LOOP(tile2, sizex, sizey, tile) 
			TileIndex.forAll( sizex, sizey, tile, (tile2) ->
			{
				if (tile2.TileHeight() == h[0]) {
					Cmd.DoCommandP(tile2, 8, mode, null, Cmd.CMD_TERRAFORM_LAND | Cmd.CMD_AUTO);
				}
				return false;
			}); //END_TILE_LOOP(tile2, sizex, sizey, tile)
		}

		Global._generating_world = false;
	}

	static void PlaceProc_RaiseBigLand(TileIndex tile)
	{
		CommonRaiseLowerBigLand(tile, 1);
	}

	static void PlaceProc_LowerBigLand(TileIndex tile)
	{
		CommonRaiseLowerBigLand(tile, 0);
	}

	static void PlaceProc_RockyArea(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y | GUI_PlaceProc_RockyArea);
	}

	static void PlaceProc_LightHouse(TileIndex tile)
	{
		if (!tile.IsTileType(TileTypes.MP_CLEAR) || TileIndex.IsSteepTileh(tile.GetTileSlope(null))) {
			return;
		}

		Landscape.ModifyTile(tile, TileTypes.MP_UNMOVABLE,
				//TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | 
				TileTypes.MP_MAP5, 1);
		Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, tile);
	}

	static void PlaceProc_Transmitter(TileIndex tile)
	{
		if (!tile.IsTileType(TileTypes.MP_CLEAR) || TileIndex.IsSteepTileh(tile.GetTileSlope(null))) {
			return;
		}

		Landscape.ModifyTile(tile, TileTypes.MP_UNMOVABLE,
				//TileTypes.MP_SETTYPE(TileTypes.MP_UNMOVABLE) | 
				TileTypes.MP_MAP5, 0);
		Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, tile);
	}

	static void PlaceProc_DesertArea(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y | GUI_PlaceProc_DesertArea);
	}

	static void PlaceProc_WaterArea(TileIndex tile)
	{
		ViewPort.VpStartPlaceSizing(tile, ViewPort.VPM_X_AND_Y | GUI_PlaceProc_WaterArea);
	}

	static final Widget _scen_edit_land_gen_widgets[] = {
		new Widget(  Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,                  Str.STR_018B_CLOSE_WINDOW),
		new Widget(   Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_0223_LAND_GENERATION,  Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
		new Widget( Window.WWT_STICKYBOX,   Window.RESIZE_NONE,     7,   170,   181,     0,    13, Str.STR_NULL,                  Str.STR_STICKY_BUTTON),
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,     0,   181,    14,   101, Str.STR_NULL,                  Str.STR_NULL),
	
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,    23,    14,    35, Sprite.SPR_IMG_DYNAMITE,          Str.STR_018D_DEMOLISH_BUILDINGS_ETC),
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    24,    45,    14,    35, Sprite.SPR_IMG_TERRAFORM_DOWN,    Str.STR_018F_RAISE_A_CORNER_OF_LAND),
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    46,    67,    14,    35, Sprite.SPR_IMG_TERRAFORM_UP,      Str.STR_018E_LOWER_A_CORNER_OF_LAND),
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    68,    89,    14,    35, Sprite.SPR_IMG_LEVEL_LAND,        Str.STR_LEVEL_LAND_TOOLTIP),
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    90,   111,    14,    35, Sprite.SPR_IMG_BUILD_CANAL,       Str.STR_CREATE_LAKE),
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   112,   134,    14,    35, Sprite.SPR_IMG_ROCKS,             Str.STR_028C_PLACE_ROCKY_AREAS_ON_LANDSCAPE),
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   135,   157,    14,    35, Sprite.SPR_IMG_LIGHTHOUSE_DESERT, Str.STR_NULL), // dynamic
		new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   158,   179,    14,    35, Sprite.SPR_IMG_TRANSMITTER,       Str.STR_028E_PLACE_TRANSMITTER),
		new Widget(   Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   139,   149,    43,    54, Str.STR_0224,                  Str.STR_0228_INCREASE_SIZE_OF_LAND_AREA),
		new Widget(   Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   139,   149,    56,    67, Str.STR_0225,                  Str.STR_0229_DECREASE_SIZE_OF_LAND_AREA),
		new Widget(   Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    34,   149,    75,    86, Str.STR_0226_RANDOM_LAND,      Str.STR_022A_GENERATE_RANDOM_LAND),
		new Widget(   Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    34,   149,    88,    99, Str.STR_0227_RESET_LAND,       Str.STR_022B_RESET_LANDSCAPE),
	};

	static final int _multi_terraform_coords[][] = {
		{  0, -2},
		{  4,  0},{ -4,  0},{  0,  2},
		{ -8,  2},{ -4,  4},{  0,  6},{  4,  4},{  8,  2},
		{-12,  0},{ -8, -2},{ -4, -4},{  0, -6},{  4, -4},{  8, -2},{ 12,  0},
		{-16,  2},{-12,  4},{ -8,  6},{ -4,  8},{  0, 10},{  4,  8},{  8,  6},{ 12,  4},{ 16,  2},
		{-20,  0},{-16, -2},{-12, -4},{ -8, -6},{ -4, -8},{  0,-10},{  4, -8},{  8, -6},{ 12, -4},{ 16, -2},{ 20,  0},
		{-24,  2},{-20,  4},{-16,  6},{-12,  8},{ -8, 10},{ -4, 12},{  0, 14},{  4, 12},{  8, 10},{ 12,  8},{ 16,  6},{ 20,  4},{ 24,  2},
		{-28,  0},{-24, -2},{-20, -4},{-16, -6},{-12, -8},{ -8,-10},{ -4,-12},{  0,-14},{  4,-12},{  8,-10},{ 12, -8},{ 16, -6},{ 20, -4},{ 24, -2},{ 28,  0},
	};

	// TODO - Merge with terraform_gui.c (move there) after I have cooled down at its braindeadness
	// and changed OnButtonClick to include the widget as well in the function decleration. Post 0.4.0 - Darkvater
	static void EditorTerraformClick_Dynamite(Window w)
	{
		HandlePlacePushButton(w, 4, Sprite.ANIMCURSOR_DEMOLISH, 1, Terraform::PlaceProc_DemolishArea);
	}

	static void EditorTerraformClick_LowerBigLand(Window w)
	{
		HandlePlacePushButton(w, 5, Sprite.ANIMCURSOR_LOWERLAND, 2, Gui::PlaceProc_LowerBigLand);
	}

	static void EditorTerraformClick_RaiseBigLand(Window w)
	{
		HandlePlacePushButton(w, 6, Sprite.ANIMCURSOR_RAISELAND, 2, Gui::PlaceProc_RaiseBigLand);
	}

	static void EditorTerraformClick_LevelLand(Window w)
	{
		HandlePlacePushButton(w, 7, Sprite.SPR_CURSOR_LEVEL_LAND, 2, Terraform::PlaceProc_LevelLand);
	}

	static void EditorTerraformClick_WaterArea(Window w)
	{
		HandlePlacePushButton(w, 8, Sprite.SPR_CURSOR_CANAL, 1, Gui::PlaceProc_WaterArea);
	}

	static void EditorTerraformClick_RockyArea(Window w)
	{
		HandlePlacePushButton(w, 9, Sprite.SPR_CURSOR_ROCKY_AREA, 1, Gui::PlaceProc_RockyArea);
	}

	static void EditorTerraformClick_DesertLightHouse(Window w)
	{
		HandlePlacePushButton(w, 10, Sprite.SPR_CURSOR_LIGHTHOUSE, 1, (GameOptions._opt.landscape == Landscape.LT_DESERT) ? Gui::PlaceProc_DesertArea : Gui::PlaceProc_LightHouse);
	}

	static void EditorTerraformClick_Transmitter(Window w)
	{
		HandlePlacePushButton(w, 11, Sprite.SPR_CURSOR_TRANSMITTER, 1, Gui::PlaceProc_Transmitter);
	}

	static final int _editor_terraform_keycodes[] = {
		'D',
		'Q',
		'W',
		'E',
		'R',
		'T',
		'Y',
		'U'
	};

	static final OnButtonClick _editor_terraform_button_proc[] = {
		Gui::EditorTerraformClick_Dynamite,
		Gui::EditorTerraformClick_LowerBigLand,
		Gui::EditorTerraformClick_RaiseBigLand,
		Gui::EditorTerraformClick_LevelLand,
		Gui::EditorTerraformClick_WaterArea,
		Gui::EditorTerraformClick_RockyArea,
		Gui::EditorTerraformClick_DesertLightHouse,
		Gui::EditorTerraformClick_Transmitter
	};

	static void ScenEditLandGenWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_CREATE:
			// XXX - lighthouse button is widget 10!! Don't forget when changing
			w.widget.get(10).tooltips = (GameOptions._opt.landscape == Landscape.LT_DESERT) ? Str.STR_028F_DEFINE_DESERT_AREA : Str.STR_028D_PLACE_LIGHTHOUSE;
			break;

		case WE_PAINT:
			w.DrawWindowWidgets();

			{
				int n = _terraform_size * _terraform_size;
				int ci = 0;
				assert(n != 0);
				do {
					int x = _multi_terraform_coords[ci][0];
					int y = _multi_terraform_coords[ci][1];

					Gfx.DrawSprite(Sprite.SPR_WHITE_POINT, 77 + x, 55 + y);
					ci++;
				} while (--n > 0);
			}

			if(0 != (w.click_state & ( 1 << 5 | 1 << 6))) // change area-size if raise/lower corner is selected
				ViewPort.SetTileSelectSize(_terraform_size, _terraform_size);

			break;

		case WE_KEYPRESS: {
			int i;

			for (i = 0; i != _editor_terraform_keycodes.length; i++) {
				if (e.keycode == _editor_terraform_keycodes[i]) {
					e.cont = false;
					_editor_terraform_button_proc[i].accept(w);
					break;
				}
			}
		} break;

		case WE_CLICK:
			switch (e.widget) {
			case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11:
				_editor_terraform_button_proc[e.widget - 4].accept(w);
				break;
			case 12: case 13: { /* Increase/Decrease terraform size */
				int size = (e.widget == 12) ? 1 : -1;
				w.HandleButtonClick(e.widget);
				size += _terraform_size;

				if (!BitOps.IS_INT_INSIDE(size, 1, 8 + 1))	return;
				_terraform_size = (byte) size;

				//Sound.SndPlayFx(Snd.SND_15_BEEP);
				ShortSounds.playBlipSound();
				
				w.SetWindowDirty();
			} break;
			case 14: /* gen random land */
				w.HandleButtonClick(14);
				AskResetLandscape(0);
				break;
			case 15: /* reset landscape */
				w.HandleButtonClick(15);
				AskResetLandscape(1);
				break;
			}
			break;

		case WE_TIMEOUT:
			w.UnclickSomeWindowButtons( ~(1<<4 | 1<<5 | 1<<6 | 1<<7 | 1<<8 | 1<<9 | 1<<10 | 1<<11));
			break;
			
		case WE_PLACE_OBJ:
			Global._place_proc.accept(e.tile);
			break;
			
		case WE_PLACE_DRAG:
			ViewPort.VpSelectTilesWithMethod(e.pt.x, e.pt.y, e.userdata & 0xF);
			break;

		case WE_PLACE_MOUSEUP:
			if (e.pt.x != -1) {
				if ((e.userdata & 0xF) == ViewPort.VPM_X_AND_Y) // dragged actions
					Terraform.GUIPlaceProcDragXY(e);
			}
			break;

		case WE_ABORT_PLACE_OBJ:
			w.click_state = 0;
			w.SetWindowDirty();
			break;
		default:
			break;
		}
	}

	static final WindowDesc _scen_edit_land_gen_desc = new WindowDesc(
		-1,-1, 182, 102,
		Window.WC_SCEN_LAND_GEN,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
		_scen_edit_land_gen_widgets,
		Gui::ScenEditLandGenWndProc
	);

	static void ShowEditorTerraformToolBar()
	{
		Window.AllocateWindowDescFront(_scen_edit_land_gen_desc, 0);
	}

	static void ToolbarScenGenLand(Window w)
	{
		w.HandleButtonClick(11);
		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();

		ShowEditorTerraformToolBar();
	}

	public static void CcBuildTown(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Sound.SndPlayTileFx(Snd.SND_1F_SPLAT, tile);
			ViewPort.ResetObjectToPlace();
		}
	}

	static void PlaceProc_Town(TileIndex tile)
	{
		Cmd.DoCommandP(tile, 0, 0, Gui::CcBuildTown, Cmd.CMD_BUILD_TOWN | Cmd.CMD_MSG(Str.STR_0236_CAN_T_BUILD_TOWN_HERE));
	}


	static final Widget _scen_edit_town_gen_widgets[] = {
		new Widget(    Window.WWT_CLOSEBOX,  Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,                 Str.STR_018B_CLOSE_WINDOW),
		new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   147,     0,    13, Str.STR_0233_TOWN_GENERATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
		new Widget(    Window.WWT_STICKYBOX, Window.RESIZE_NONE,     7,   148,   159,     0,    13, 0x0,                      Str.STR_STICKY_BUTTON),
		new Widget(    Window.WWT_IMGBTN,    Window.RESIZE_NONE,     7,     0,   159,    14,    81, 0x0,                      Str.STR_NULL),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   157,    16,    27, Str.STR_0234_NEW_TOWN,        Str.STR_0235_CONSTRUCT_NEW_TOWN),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   157,    29,    40, Str.STR_023D_RANDOM_TOWN,     Str.STR_023E_BUILD_TOWN_IN_RANDOM_LOCATION),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   157,    42,    53, Str.STR_MANY_RANDOM_TOWNS,    Str.STR_RANDOM_TOWNS_TIP),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,    53,    68,    79, Str.STR_02A1_SMALL,           Str.STR_02A4_SELECT_TOWN_SIZE),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,    54,   105,    68,    79, Str.STR_02A2_MEDIUM,          Str.STR_02A4_SELECT_TOWN_SIZE),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   106,   157,    68,    79, Str.STR_02A3_LARGE,           Str.STR_02A4_SELECT_TOWN_SIZE),
	};

	static void ScenEditTownGenWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			w.click_state = (w.click_state & ~(1<<7 | 1<<8 | 1<<9) ) | (1 << (Global._new_town_size + 7));
			w.DrawWindowWidgets();
			Gfx.DrawStringCentered(80, 56, Str.STR_02A5_TOWN_SIZE, 0);
			break;

		case WE_CLICK:
			switch (e.widget) {
			case 4: /* new town */
				HandlePlacePushButton(w, 4, Sprite.SPR_CURSOR_TOWN, 1, Gui::PlaceProc_Town);
				break;
			case 5: {/* random town */
				Town t;

				w.HandleButtonClick(5);
				Global._generating_world = true;
				t = Town.CreateRandomTown(20);
				Global._generating_world = false;

				if (t == null) {
					Global.ShowErrorMessage(Str.STR_NO_SPACE_FOR_TOWN, Str.STR_CANNOT_GENERATE_TOWN, 0, 0);
				} else {
					ViewPort.ScrollMainWindowToTile(t.getXy());
				}

				break;
			}
			case 6: {/* many random towns */
				w.HandleButtonClick(6);

				Global._generating_world = true;
				Global._game_mode = GM_NORMAL; // little hack to avoid towns of the same size
				if (!Town.GenerateTowns()) {
					Global.ShowErrorMessage(Str.STR_NO_SPACE_FOR_TOWN, Str.STR_CANNOT_GENERATE_TOWN, 0, 0);
				}
				Global._generating_world = false;

				Global._game_mode = GM_EDITOR;
				break;
			}

			case 7: case 8: case 9:
				Global._new_town_size = e.widget - 7;
				w.SetWindowDirty();
				break;
			}
			break;

		case WE_TIMEOUT:
			w.UnclickSomeWindowButtons(1<<5 | 1<<6);
			break;
		case WE_PLACE_OBJ:
			Global._place_proc.accept(e.tile);
			break;
		case WE_ABORT_PLACE_OBJ:
			w.click_state = 0;
			w.SetWindowDirty();
			break;
		default:
			break;
		}
	}

	static final WindowDesc _scen_edit_town_gen_desc = new WindowDesc(
		-1,-1, 160, 82,
		Window.WC_SCEN_TOWN_GEN,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON,
		_scen_edit_town_gen_widgets,
		Gui::ScenEditTownGenWndProc
	);

	static void ToolbarScenGenTown(Window w)
	{
		w.HandleButtonClick(12);
		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();

		Window.AllocateWindowDescFront(_scen_edit_town_gen_desc, 0);
	}


	static final Widget _scenedit_industry_normal_widgets[] = {
		new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
		new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_023F_INDUSTRY_GENERATION,	Str.STR_NULL),
		new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,     0,   169,    14,   224, 0x0,											Str.STR_NULL),
	
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_MANY_RANDOM_INDUSTRIES,		Str.STR_RANDOM_INDUSTRIES_TIP),
	
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0240_COAL_MINE,			Str.STR_0262_CONSTRUCT_COAL_MINE),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_0241_POWER_STATION,	Str.STR_0263_CONSTRUCT_POWER_STATION),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_0242_SAWMILL,				Str.STR_0264_CONSTRUCT_SAWMILL),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    81,    92, Str.STR_0243_FOREST,					Str.STR_0265_PLANT_FOREST),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    94,   105, Str.STR_0244_OIL_REFINERY,		Str.STR_0266_CONSTRUCT_OIL_REFINERY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   107,   118, Str.STR_0245_OIL_RIG,				Str.STR_0267_CONSTRUCT_OIL_RIG_CAN_ONLY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   120,   131, Str.STR_0246_FACTORY,				Str.STR_0268_CONSTRUCT_FACTORY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   133,   144, Str.STR_0247_STEEL_MILL,			Str.STR_0269_CONSTRUCT_STEEL_MILL),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   146,   157, Str.STR_0248_FARM,						Str.STR_026A_CONSTRUCT_FARM),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   159,   170, Str.STR_0249_IRON_ORE_MINE,	Str.STR_026B_CONSTRUCT_IRON_ORE_MINE),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   172,   183, Str.STR_024A_OIL_WELLS,			Str.STR_026C_CONSTRUCT_OIL_WELLS),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   185,   196, Str.STR_024B_BANK,						Str.STR_026D_CONSTRUCT_BANK_CAN_ONLY),
	};


	static final Widget _scenedit_industry_hilly_widgets[] = {
		new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
		new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_023F_INDUSTRY_GENERATION,	Str.STR_NULL),
		new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,     0,   169,    14,   224, 0x0,											Str.STR_NULL),
	
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_MANY_RANDOM_INDUSTRIES,		Str.STR_RANDOM_INDUSTRIES_TIP),
	
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0240_COAL_MINE,			Str.STR_0262_CONSTRUCT_COAL_MINE),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_0241_POWER_STATION,	Str.STR_0263_CONSTRUCT_POWER_STATION),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_024C_PAPER_MILL,			Str.STR_026E_CONSTRUCT_PAPER_MILL),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    81,    92, Str.STR_0243_FOREST,					Str.STR_0265_PLANT_FOREST),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    94,   105, Str.STR_0244_OIL_REFINERY,		Str.STR_0266_CONSTRUCT_OIL_REFINERY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   107,   118, Str.STR_024D_FOOD_PROCESSING_PLANT,	Str.STR_026F_CONSTRUCT_FOOD_PROCESSING),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   120,   131, Str.STR_024E_PRINTING_WORKS,	Str.STR_0270_CONSTRUCT_PRINTING_WORKS),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   133,   144, Str.STR_024F_GOLD_MINE,			Str.STR_0271_CONSTRUCT_GOLD_MINE),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   146,   157, Str.STR_0248_FARM,						Str.STR_026A_CONSTRUCT_FARM),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   159,   170, Str.STR_024B_BANK,						Str.STR_0272_CONSTRUCT_BANK_CAN_ONLY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   172,   183, Str.STR_024A_OIL_WELLS,			Str.STR_026C_CONSTRUCT_OIL_WELLS),
	};

	static final Widget _scenedit_industry_desert_widgets[] = {
		new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
		new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_023F_INDUSTRY_GENERATION,		Str.STR_NULL),
		new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,     0,   169,    14,   224, 0x0,												Str.STR_NULL),
	
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_MANY_RANDOM_INDUSTRIES,			Str.STR_RANDOM_INDUSTRIES_TIP),
	
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0250_LUMBER_MILL,			Str.STR_0273_CONSTRUCT_LUMBER_MILL_TO),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_0251_FRUIT_PLANTATION,	Str.STR_0274_PLANT_FRUIT_PLANTATION),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_0252_RUBBER_PLANTATION,Str.STR_0275_PLANT_RUBBER_PLANTATION),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    81,    92, Str.STR_0244_OIL_REFINERY,			Str.STR_0266_CONSTRUCT_OIL_REFINERY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    94,   105, Str.STR_024D_FOOD_PROCESSING_PLANT,	Str.STR_026F_CONSTRUCT_FOOD_PROCESSING),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   107,   118, Str.STR_0246_FACTORY,					Str.STR_0268_CONSTRUCT_FACTORY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   120,   131, Str.STR_0253_WATER_SUPPLY,			Str.STR_0276_CONSTRUCT_WATER_SUPPLY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   133,   144, Str.STR_0248_FARM,							Str.STR_026A_CONSTRUCT_FARM),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   146,   157, Str.STR_0254_WATER_TOWER,			Str.STR_0277_CONSTRUCT_WATER_TOWER_CAN),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   159,   170, Str.STR_024A_OIL_WELLS,				Str.STR_026C_CONSTRUCT_OIL_WELLS),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   172,   183, Str.STR_024B_BANK,							Str.STR_0272_CONSTRUCT_BANK_CAN_ONLY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   185,   196, Str.STR_0255_DIAMOND_MINE,			Str.STR_0278_CONSTRUCT_DIAMOND_MINE),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   198,   209, Str.STR_0256_COPPER_ORE_MINE,	Str.STR_0279_CONSTRUCT_COPPER_ORE_MINE),
	};

	static final Widget _scenedit_industry_candy_widgets[] = {
		new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,										Str.STR_018B_CLOSE_WINDOW),
		new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   169,     0,    13, Str.STR_023F_INDUSTRY_GENERATION,Str.STR_NULL),
		new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     7,     0,   169,    14,   224, 0x0,													Str.STR_NULL),
	
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    16,    27, Str.STR_MANY_RANDOM_INDUSTRIES,	Str.STR_RANDOM_INDUSTRIES_TIP),
	
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    42,    53, Str.STR_0257_COTTON_CANDY_FOREST,Str.STR_027A_PLANT_COTTON_CANDY_FOREST),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    55,    66, Str.STR_0258_CANDY_FACTORY,			Str.STR_027B_CONSTRUCT_CANDY_FACTORY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    68,    79, Str.STR_0259_BATTERY_FARM,				Str.STR_027C_CONSTRUCT_BATTERY_FARM),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    81,    92, Str.STR_025A_COLA_WELLS,					Str.STR_027D_CONSTRUCT_COLA_WELLS),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,    94,   105, Str.STR_025B_TOY_SHOP,						Str.STR_027E_CONSTRUCT_TOY_SHOP),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   107,   118, Str.STR_025C_TOY_FACTORY,				Str.STR_027F_CONSTRUCT_TOY_FACTORY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   120,   131, Str.STR_025D_PLASTIC_FOUNTAINS,	Str.STR_0280_CONSTRUCT_PLASTIC_FOUNTAINS),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   133,   144, Str.STR_025E_FIZZY_DRINK_FACTORY,Str.STR_0281_CONSTRUCT_FIZZY_DRINK_FACTORY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   146,   157, Str.STR_025F_BUBBLE_GENERATOR,		Str.STR_0282_CONSTRUCT_BUBBLE_GENERATOR),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   159,   170, Str.STR_0260_TOFFEE_QUARRY,			Str.STR_0283_CONSTRUCT_TOFFEE_QUARRY),
		new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   167,   172,   183, Str.STR_0261_SUGAR_MINE,					Str.STR_0284_CONSTRUCT_SUGAR_MINE),
	};


	static boolean AnyTownExists()
	{
		return Town.anyTownExist();
		/*
		Iterator<Town> ii = Town.getIterator();
		while(ii.hasNext())
		{
			final Town t = ii.next();
			if (t.getXy() != null) return true;
		}
		return false;
		*/
	}

	static boolean TryBuildIndustry(TileIndex tile, int type)
	{
		int n;

		if(null != Industry.CreateNewIndustry(tile, type)) return true;

		n = 100;
		do {
			if (null != Industry.CreateNewIndustry(Landscape.AdjustTileCoordRandomly(tile, 1), type)) return true;
		} while (--n > 0);

		n = 200;
		do {
			if (null != Industry.CreateNewIndustry(Landscape.AdjustTileCoordRandomly(tile, 2), type)) return true;
		} while (--n > 0);

		n = 700;
		do {
			if (null != Industry.CreateNewIndustry(Landscape.AdjustTileCoordRandomly(tile, 4), type)) return true;
		} while (--n > 0);

		return false;
	}


	static final byte _industry_type_list[][] = {
		{0, 1, 2, 3, 4, 5, 6, 8, 9, 18, 11, 12},
		{0, 1, 14, 3, 4, 13, 7, 15, 9, 16, 11, 12},
		{25, 19, 20, 4, 13, 23, 21, 24, 22, 11, 16, 17, 10},
		{26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
	};

	static int _industry_type_to_place;
	public static boolean _ignore_restrictions = false;

	static void ScenEditIndustryWndProc(Window w, WindowEvent e)
	{
		int button;

		switch(e.event) {
		case WE_PAINT:
			w.DrawWindowWidgets();
			break;

		case WE_CLICK:
			if (e.widget == 3) {
				w.HandleButtonClick(3);

				if (!AnyTownExists()) {
					Global.ShowErrorMessage(Str.STR_0286_MUST_BUILD_TOWN_FIRST, Str.STR_CAN_T_GENERATE_INDUSTRIES, 0, 0);
					return;
				}

				Global._generating_world = true;
				Industry.GenerateIndustries();
				Global._generating_world = false;
			}

			if ((button=e.widget) >= 4) {
				if (HandlePlacePushButton(w, button, Sprite.SPR_CURSOR_INDUSTRY, 1, null))
					_industry_type_to_place = _industry_type_list[GameOptions._opt.landscape][button - 4];
			}
			break;
		case WE_PLACE_OBJ: {
			int type;

			// Show error if no town exists at all
			type = _industry_type_to_place;
			if (!AnyTownExists()) {
				Global.SetDParam(0, type + Str.STR_4802_COAL_MINE);
				Global.ShowErrorMessage(Str.STR_0286_MUST_BUILD_TOWN_FIRST,Str.STR_0285_CAN_T_BUILD_HERE,e.pt.x, e.pt.y);
				return;
			}

			PlayerID.setCurrentToNone();
			Global._generating_world = true;
			Gui._ignore_restrictions = true;
			if (!TryBuildIndustry(e.tile,type)) {
				Global.SetDParam(0, type + Str.STR_4802_COAL_MINE);
				Global.ShowErrorMessage(Global._error_message, Str.STR_0285_CAN_T_BUILD_HERE, e.pt.x, e.pt.y);
			}
			Gui._ignore_restrictions = false;
			Global._generating_world = false;
			break;
		}
		case WE_ABORT_PLACE_OBJ:
			w.click_state = 0;
			w.SetWindowDirty();
			break;
		case WE_TIMEOUT:
			w.UnclickSomeWindowButtons(1<<3);
			break;
		default:
			break;
		}
	}

	static final WindowDesc _scenedit_industry_normal_desc = new WindowDesc(
		-1,-1, 170, 225,
		Window.WC_SCEN_INDUSTRY,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_scenedit_industry_normal_widgets,
		Gui::ScenEditIndustryWndProc
	);

	static final WindowDesc _scenedit_industry_hilly_desc = new WindowDesc(
		-1,-1, 170, 225,
		Window.WC_SCEN_INDUSTRY,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_scenedit_industry_hilly_widgets,
		Gui::ScenEditIndustryWndProc
	);

	static final WindowDesc _scenedit_industry_desert_desc = new WindowDesc(
		-1,-1, 170, 225,
		Window.WC_SCEN_INDUSTRY,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_scenedit_industry_desert_widgets,
		Gui::ScenEditIndustryWndProc
	);

	static final WindowDesc _scenedit_industry_candy_desc = new WindowDesc(
		-1,-1, 170, 225,
		Window.WC_SCEN_INDUSTRY,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_scenedit_industry_candy_widgets,
		Gui::ScenEditIndustryWndProc
	);

	static final WindowDesc _scenedit_industry_descs[] = {
		_scenedit_industry_normal_desc,
		_scenedit_industry_hilly_desc,
		_scenedit_industry_desert_desc,
		_scenedit_industry_candy_desc,
	};


	static void ToolbarScenGenIndustry(Window w)
	{
		w.HandleButtonClick(13);
		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();
		Window.AllocateWindowDescFront(_scenedit_industry_descs[GameOptions._opt.landscape],0);
	}

	static void ToolbarScenBuildRoad(Window w)
	{
		w.HandleButtonClick(14);
		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();
		RoadGui.ShowBuildRoadScenToolbar();
	}

	static void ToolbarScenPlantTrees(Window w)
	{
		w.HandleButtonClick(15);
		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();
		MiscGui.ShowBuildTreesScenToolbar();
	}

	static void ToolbarScenPlaceSign(Window w)
	{
		w.HandleButtonClick(16);
		//Sound.SndPlayFx(Snd.SND_15_BEEP);
		ShortSounds.playBlipSound();
		SelectSignTool();
	}

	static void ToolbarBtn_null(Window w)
	{
		/* is empty */
	}


	//typedef void ToolbarButtonProc(Window w);

	static final ToolbarButtonProc _toolbar_button_procs[] = {
		Gui::ToolbarPauseClick,
		Gui::ToolbarFastForwardClick,
		Gui::ToolbarOptionsClick,
		Gui::ToolbarSaveClick,
		Gui::ToolbarMapClick,
		Gui::ToolbarTownClick,
		Gui::ToolbarSubsidiesClick,
		Gui::ToolbarStationsClick,
		Gui::ToolbarMoneyClick,
		Gui::ToolbarPlayersClick,
		Gui::ToolbarGraphsClick,
		Gui::ToolbarLeagueClick,
		Gui::ToolbarIndustryClick,
		Gui::ToolbarTrainClick,
		Gui::ToolbarRoadClick,
		Gui::ToolbarShipClick,
		Gui::ToolbarAirClick,
		Gui::ToolbarZoomInClick,
		Gui::ToolbarZoomOutClick,
		Gui::ToolbarBuildRailClick,
		Gui::ToolbarBuildRoadClick,
		Gui::ToolbarBuildWaterClick,
		Gui::ToolbarBuildAirClick,
		Gui::ToolbarForestClick,
		Gui::ToolbarMusicClick,
		Gui::ToolbarNewspaperClick,
		Gui::ToolbarHelpClick,
	};

	static void MainToolbarWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {

			// Draw brown-red toolbar bg.
			Gfx.GfxFillRect(0, 0, w.width-1, w.height-1, 0xB2);
			Gfx.GfxFillRect(0, 0, w.width-1, w.height-1, 0xB4 | Sprite.PALETTE_MODIFIER_GREYOUT);

			// if spectator, disable things
			if (PlayerID.getCurrent().isSpectator()){
				w.disabled_state |= (1 << 19) | (1<<20) | (1<<21) | (1<<22) | (1<<23);
			} else {
				w.disabled_state &= ~((1 << 19) | (1<<20) | (1<<21) | (1<<22) | (1<<23));
			}

			w.DrawWindowWidgets();
			break;
		}

		case WE_CLICK: {
			if (Global._game_mode != GM_MENU && !BitOps.HASBIT(w.disabled_state, e.widget))
				_toolbar_button_procs[e.widget].accept(w);
		} break;

		case WE_KEYPRESS: {
			//PlayerID 
			int local = (Global.gs._local_player.id != Owner.OWNER_SPECTATOR) ? Global.gs._local_player.id : 0;

			switch (e.keycode) {
			case Window.WKC_F1: case Window.WKC_PAUSE:
				ToolbarPauseClick(w);
				break;
			case Window.WKC_F2: SettingsGui.ShowGameOptions(); break;
			case Window.WKC_F3: MenuClickSaveLoad(0); break;
			case Window.WKC_F4: SmallMapGui.ShowSmallMap(); break;
			case Window.WKC_F5: TownGui.ShowTownDirectory(); break;
			case Window.WKC_F6: Subsidies.ShowSubsidiesList(); break;
			case Window.WKC_F7: StationGui.ShowPlayerStations(local); break;
			case Window.WKC_F8: PlayerGui.ShowPlayerFinances(local); break;
			case Window.WKC_F9: PlayerGui.ShowPlayerCompany(local); break;
			case Window.WKC_F10:GraphGui.ShowOperatingProfitGraph(); break;
			case Window.WKC_F11: GraphGui.ShowCompanyLeagueTable(); break;
			case Window.WKC_F12: Industry.ShowBuildIndustryWindow(); break;
			case Window.WKC_SHIFT | Window.WKC_F1: TrainGui.ShowPlayerTrains(local, Station.INVALID_STATION); break;
			case Window.WKC_SHIFT | Window.WKC_F2: RoadVehGui.ShowPlayerRoadVehicles(local, Station.INVALID_STATION); break;
			case Window.WKC_SHIFT | Window.WKC_F3: ShipGui.ShowPlayerShips(local, Station.INVALID_STATION); break;
			case Window.WKC_SHIFT | Window.WKC_F4: AirCraft.ShowPlayerAircraft(local, Station.INVALID_STATION); break;
			case Window.WKC_SHIFT | Window.WKC_F5: ToolbarZoomInClick(w); break;
			case Window.WKC_SHIFT | Window.WKC_F6: ToolbarZoomOutClick(w); break;
			case Window.WKC_SHIFT | Window.WKC_F7: RailGui.ShowBuildRailToolbar(_last_built_railtype,-1); break;
			case Window.WKC_SHIFT | Window.WKC_F8: RoadGui.ShowBuildRoadToolbar(); break;
			case Window.WKC_SHIFT | Window.WKC_F9: DockGui.ShowBuildDocksToolbar(); break;
			case Window.WKC_SHIFT | Window.WKC_F10:AirportGui.ShowBuildAirToolbar(); break;
			case Window.WKC_SHIFT | Window.WKC_F11: MiscGui.ShowBuildTreesToolbar(); break;
			case Window.WKC_SHIFT | Window.WKC_F12: MusicGui.ShowMusicWindow(); break;
			case Window.WKC_CTRL  | 'S': ScreenShot._make_screenshot = 1; break;
			case Window.WKC_CTRL  | 'G': ScreenShot._make_screenshot = 2; break;
			//case Window.WKC_CTRL | Window.WKC_ALT | 'C': if (!_networking) ShowCheatWindow(); break;
			case 'A': RailGui.ShowBuildRailToolbar(_last_built_railtype, 4); break; /* Invoke Autorail */
			case 'L': Terraform.ShowTerraformToolbar(); break;
			default: return;
			}
			e.cont = false;
		} break;

		case WE_PLACE_OBJ: {
			Global._place_proc.accept(e.tile);
		} break;

		case WE_ABORT_PLACE_OBJ: {
			w.click_state &= ~(1<<25);
			w.SetWindowDirty();
		} break;

		case WE_ON_EDIT_TEXT: HandleOnEditText(e); break;

		case WE_MOUSELOOP:
			if (  ((w.click_state) & 1) != Global._pause) {
				w.click_state ^= (1 << 0);
				w.SetWindowDirty();
			}

			if ( (((w.click_state >> 1) & 1) != 0) != Global._fast_forward) {
				w.click_state ^= (1 << 1);
				w.SetWindowDirty();
			}
			break;

		case WE_TIMEOUT:
			w.UnclickSomeWindowButtons(~(1<<0 | 1<<1));
			break;
		default:
			break;
		}
	}

	static final Widget _toolb_normal_widgets[] = {
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,    21,     0,    21, 0x2D6, Str.STR_0171_PAUSE_GAME),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    22,    43,     0,    21, Sprite.SPR_IMG_FASTFORWARD, Str.STR_FAST_FORWARD),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    44,    65,     0,    21, 0x2EF, Str.STR_0187_OPTIONS),
		new Widget(      Window.WWT_PANEL_2, Window.RESIZE_NONE,    14,    66,    87,     0,    21, 0x2D4, Str.STR_0172_SAVE_GAME_ABANDON_GAME),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    96,   117,     0,    21, 0x2C4, Str.STR_0174_DISPLAY_MAP),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   118,   139,     0,    21, 0xFED, Str.STR_0176_DISPLAY_TOWN_DIRECTORY),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   140,   161,     0,    21, 0x2A7, Str.STR_02DC_DISPLAY_SUBSIDIES),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   162,   183,     0,    21, 0x513, Str.STR_0173_DISPLAY_LIST_OF_COMPANY),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   191,   212,     0,    21, 0x2E1, Str.STR_0177_DISPLAY_COMPANY_FINANCES),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   213,   235,     0,    21, 0x2E7, Str.STR_0178_DISPLAY_COMPANY_GENERAL),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   236,   257,     0,    21, 0x2E9, Str.STR_0179_DISPLAY_GRAPHS),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   258,   279,     0,    21, 0x2AC, Str.STR_017A_DISPLAY_COMPANY_LEAGUE),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   280,   301,     0,    21, 0x2E5, Str.STR_0312_FUND_CONSTRUCTION_OF_NEW),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   310,   331,     0,    21, 0x2DB, Str.STR_017B_DISPLAY_LIST_OF_COMPANY),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   332,   353,     0,    21, 0x2DC, Str.STR_017C_DISPLAY_LIST_OF_COMPANY),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   354,   375,     0,    21, 0x2DD, Str.STR_017D_DISPLAY_LIST_OF_COMPANY),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   376,   397,     0,    21, 0x2DE, Str.STR_017E_DISPLAY_LIST_OF_COMPANY),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   406,   427,     0,    21, 0x2DF, Str.STR_017F_ZOOM_THE_VIEW_IN),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   428,   449,     0,    21, 0x2E0, Str.STR_0180_ZOOM_THE_VIEW_OUT),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   457,   478,     0,    21, 0x2D7, Str.STR_0181_BUILD_RAILROAD_TRACK),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   479,   500,     0,    21, 0x2D8, Str.STR_0182_BUILD_ROADS),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   501,   522,     0,    21, 0x2D9, Str.STR_0183_BUILD_SHIP_DOCKS),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   523,   544,     0,    21, 0x2DA, Str.STR_0184_BUILD_AIRPORTS),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   545,   566,     0,    21, 0xFF3, Str.STR_LANDSCAPING_TOOLBAR_TIP), // tree icon is 0x2E6
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   574,   595,     0,    21, 0x2C9, Str.STR_01D4_SHOW_SOUND_MUSIC_WINDOW),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   596,   617,     0,    21, 0x2A8, Str.STR_0203_SHOW_LAST_MESSAGE_NEWS),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   618,   639,     0,    21, 0x2D3, Str.STR_0186_LAND_BLOCK_INFORMATION),
	};

	static final WindowDesc _toolb_normal_desc = new WindowDesc(
		0, 0, 640, 22,
		Window.WC_MAIN_TOOLBAR,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET,
		_toolb_normal_widgets,
		Gui::MainToolbarWndProc
	);


	static final Widget _toolb_scen_widgets[] = {
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,    21,     0,    21, 0x2D6,				Str.STR_0171_PAUSE_GAME),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    22,    43,     0,    21, Sprite.SPR_IMG_FASTFORWARD, Str.STR_FAST_FORWARD),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    44,    65,     0,    21, 0x2EF,				Str.STR_0187_OPTIONS),
		new Widget(    Window.WWT_PANEL_2,   Window.RESIZE_NONE,    14,    66,    87,     0,    21, 0x2D4,				Str.STR_0297_SAVE_SCENARIO_LOAD_SCENARIO),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    96,   225,     0,    21, 0x0,					Str.STR_NULL),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   233,   362,     0,    21, 0x0,					Str.STR_NULL),
		new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   236,   247,     5,    16, Sprite.SPR_ARROW_DOWN,	Str.STR_029E_MOVE_THE_STARTING_DATE),
		new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   347,   358,     5,    16, Sprite.SPR_ARROW_UP,   Str.STR_029F_MOVE_THE_STARTING_DATE),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   371,   392,     0,    21, 0x2C4,				Str.STR_0175_DISPLAY_MAP_TOWN_DIRECTORY),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   400,   421,     0,    21, 0x2DF,				Str.STR_017F_ZOOM_THE_VIEW_IN),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   422,   443,     0,    21, 0x2E0,				Str.STR_0180_ZOOM_THE_VIEW_OUT),
	
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   452,   473,     0,    21, 0xFF3,				Str.STR_022E_LANDSCAPE_GENERATION),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   474,   495,     0,    21, 0xFED,				Str.STR_022F_TOWN_GENERATION),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   496,   517,     0,    21, 0x2E5,				Str.STR_0230_INDUSTRY_GENERATION),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   518,   539,     0,    21, 0x2D8,				Str.STR_0231_ROAD_CONSTRUCTION),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   540,   561,     0,    21, 0x2E6,				Str.STR_0288_PLANT_TREES),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   562,   583,     0,    21, 0xFF2,				Str.STR_0289_PLACE_SIGN),
	
		new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
		new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
		new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
		new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
		new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
		new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
		new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   596,   617,     0,    21, 0x2C9,				Str.STR_01D4_SHOW_SOUND_MUSIC_WINDOW),
		new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,					Str.STR_NULL),
		new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   618,   639,     0,    21, 0x2D3,				Str.STR_0186_LAND_BLOCK_INFORMATION),
	};

	static final ToolbarButtonProc _scen_toolbar_button_procs[] = {
		Gui::ToolbarPauseClick,
		Gui::ToolbarFastForwardClick,
		Gui::ToolbarOptionsClick,
		Gui::ToolbarScenSaveOrLoad,
		Gui::ToolbarBtn_null,
		Gui::ToolbarBtn_null,
		Gui::ToolbarScenDateBackward,
		Gui::ToolbarScenDateForward,
		Gui::ToolbarScenMapTownDir,
		Gui::ToolbarScenZoomIn,
		Gui::ToolbarScenZoomOut,
		Gui::ToolbarScenGenLand,
		Gui::ToolbarScenGenTown,
		Gui::ToolbarScenGenIndustry,
		Gui::ToolbarScenBuildRoad,
		Gui::ToolbarScenPlantTrees,
		Gui::ToolbarScenPlaceSign,
		null,
		null,
		null,
		null,
		null,
		null,
		null,
		Gui::ToolbarMusicClick,
		null,
		Gui::ToolbarHelpClick,
	};

	static void ScenEditToolbarWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:
			/* XXX look for better place for these */
			if (Global.get_date() <= MinDate) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 6);
			} else {
				w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 6);
			}
			if (Global.get_date() >= MaxDate) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 7);
			} else {
				w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, 7);
			}

			// Draw brown-red toolbar bg.
			Gfx.GfxFillRect(0, 0, w.width-1, w.height-1, 0xB2);
			Gfx.GfxFillRect(0, 0, w.width-1, w.height-1, 0xB4 | Sprite.PALETTE_MODIFIER_GREYOUT);

			w.DrawWindowWidgets();

			Global.SetDParam(0, Global.get_date());
			Gfx.DrawStringCentered(298, 6, Str.STR_00AF, 0);

			Global.SetDParam(0, Global.get_date());
			Gfx.DrawStringCentered(161, 1, Str.STR_0221_OPENTTD, 0);
			Gfx.DrawStringCentered(161, 11,Str.STR_0222_SCENARIO_EDITOR, 0);

			break;

		case WE_CLICK: {
			if (Global._game_mode == GM_MENU) return;
			_scen_toolbar_button_procs[e.widget].accept(w);
		} break;

		case WE_KEYPRESS:
			switch (e.keycode) {
			case Window.WKC_F1: ToolbarPauseClick(w); break;
			case Window.WKC_F2: SettingsGui.ShowGameOptions(); break;
			case Window.WKC_F3: MenuClickSaveLoad(0); break;
			case Window.WKC_F4: ToolbarScenGenLand(w); break;
			case Window.WKC_F5: ToolbarScenGenTown(w); break;
			case Window.WKC_F6: ToolbarScenGenIndustry(w); break;
			case Window.WKC_F7: ToolbarScenBuildRoad(w); break;
			case Window.WKC_F8: ToolbarScenPlantTrees(w); break;
			case Window.WKC_F9: ToolbarScenPlaceSign(w); break;
			case Window.WKC_F10: MusicGui.ShowMusicWindow(); break;
			case Window.WKC_F11: MiscGui.PlaceLandBlockInfo(); break;
			case Window.WKC_CTRL | 'S': ScreenShot._make_screenshot = 1; break;
			case Window.WKC_CTRL | 'G': ScreenShot._make_screenshot = 2; break;
			case 'L': ShowEditorTerraformToolBar(); break;
			}
			break;

		case WE_PLACE_OBJ: {
			Global._place_proc.accept(e.tile);
		} break;

		case WE_ABORT_PLACE_OBJ: {
			w.click_state &= ~(1<<25);
			w.SetWindowDirty();
		} break;

		case WE_ON_EDIT_TEXT: HandleOnEditText(e); break;

		case WE_MOUSELOOP:
			if ( ((w.click_state) & 1) != Global._pause) {
				w.click_state ^= (1 << 0);
				w.SetWindowDirty();
			}

			if( (((w.click_state >> 1) & 1) != 0) != Global._fast_forward) {
				w.click_state ^= (1 << 1);
				w.SetWindowDirty();
			}
			break;

		default:
			break;

		}
	}

	static final WindowDesc _toolb_scen_desc = new WindowDesc(
		0, 0, 640, 22,
		Window.WC_MAIN_TOOLBAR,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_toolb_scen_widgets,
		Gui::ScenEditToolbarWndProc
	);



	static boolean DrawScrollingStatusText(final NewsItem ni, int pos)
	{
		StringID str;
		DrawPixelInfo tmp_dpi = new DrawPixelInfo();
		DrawPixelInfo old_dpi;
		int x;
		
		str = ni.makeString();

		String buf = Strings.GetString(str);

		char [] s = buf.toCharArray();
		char [] d = new char[256];

		int sp = 0;
		int dp = 0;
		
		for (; sp < s.length; sp++) {
			if (s[sp] == '\0') { // kill me?
				d[dp] = '\0';
				break;
			} else if (s[sp] == 0x0D) {
				d[dp+0] = d[dp+1] = d[dp+2] = d[dp+3] = ' ';
				dp += 4;
			} else if (s[sp] >= ' ' && (s[sp] < 0x88 || s[sp] >= 0x99)) {
				d[dp++] = s[sp];
			}
		}

		if (!DrawPixelInfo.FillDrawPixelInfo(tmp_dpi, null, 141, 1, 358, 11)) return true;

		old_dpi = Hal._cur_dpi;
		Hal._cur_dpi = tmp_dpi;

		x = Gfx.DoDrawString( new String( d ), pos, 0, 13);
		Hal._cur_dpi = old_dpi;

		return x > 0;
	}

	static void StatusBarWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			final Player p = (Global.gs._local_player.isSpectator()) ? null : Player.GetPlayer(Global.gs._local_player);

			w.DrawWindowWidgets();
			Global.SetDParam(0, Global.get_date());
			Gfx.DrawStringCentered(
				70, 1, (Global._pause != 0 || Global._patches.status_long_date.get()) ? Str.STR_00AF : Str.STR_00AE, 0
			);

			if (p != null) {
				// Draw player money
				Global.SetDParam64(0, p.getMoney());
				Gfx.DrawStringCentered(570, 1, p.getMoney() >= 0 ? Str.STR_0004 : Str.STR_0005, 0);
			}

			// Draw status bar
			if (w.message != null && 0 != w.message.msg) { // true when saving is active
				Gfx.DrawStringCentered(320, 1, Str.STR_SAVING_GAME, 0);
			} else if (Global._do_autosave) {
				Gfx.DrawStringCentered(320, 1,	Str.STR_032F_AUTOSAVE, 0);
			} else if (Global._pause != 0) {
				Gfx.DrawStringCentered(320, 1,	Str.STR_0319_PAUSED, 0);
			} else if (w.as_def_d().data_1 > -1280 && Window.FindWindowById(Window.WC_NEWS_WINDOW,0) == null && Global._statusbar_news_item.getString_id() != null) {
				// Draw the scrolling news text
				if (!DrawScrollingStatusText(Global._statusbar_news_item, w.as_def_d().data_1))
					w.as_def_d().data_1 = -1280;
			} else {
				if (p != null) {
					// This is the default text
					Global.SetDParam(0, p.getName_1());
					Global.SetDParam(1, p.getName_2());
					Gfx.DrawStringCentered(320, 1,	Str.STR_02BA, 0);
				}
			}

			if (w.as_def_d().data_2 > 0) Gfx.DrawSprite(Sprite.SPR_BLOT | Sprite.PALETTE_TO_RED, 489, 2);
		} break;

		case WE_MESSAGE:
			w.message.msg = e.msg;
			w.SetWindowDirty();
			break;

		case WE_CLICK:
			switch (e.widget) {
				case 1: NewsItem.ShowLastNewsMessage(); break;
				case 2: if (Global.gs._local_player.id != Owner.OWNER_SPECTATOR) PlayerGui.ShowPlayerFinances(Global.gs._local_player.id); break;
				default: ViewPort.ResetObjectToPlace();
			}
			break;

		case WE_TICK: {
			if (Global._pause != 0) return;

			if (w.as_def_d().data_1 > -1280) { /* Scrolling text */
				w.as_def_d().data_1 -= 2;
				w.InvalidateWidget(1);
			}

			if (w.as_def_d().data_2 > 0) { /* Red blot to show there are new unread newsmessages */
				w.as_def_d().data_2 -= 2;
			} else if (w.as_def_d().data_2 < 0) {
				w.as_def_d().data_2 = 0;
				w.InvalidateWidget(1);
			}

			break;
		}
		default:
			break;
		}
	}

	static final Widget _main_status_widgets[] = {
		new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   139,     0,    11, 0x0,	Str.STR_NULL),
		new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   140,   499,     0,    11, 0x0, Str.STR_02B7_SHOW_LAST_MESSAGE_OR_NEWS),
		new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   500,   639,     0,    11, 0x0, Str.STR_NULL),
	};

	static final WindowDesc _main_status_desc = new WindowDesc(
		Window.WDP_CENTER, 0, 640, 12,
		Window.WC_STATUS_BAR,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_main_status_widgets,
		Gui::StatusBarWndProc
	);

	//extern void UpdateAllStationVirtCoord();

	static void MainWindowWndProc(Window w, WindowEvent e) {
		int off_x;

		switch(e.event) {
		case WE_PAINT:
			ViewPort.DrawWindowViewport(w);
			if (Global._game_mode == GM_MENU) {
				off_x = Hal._screen.width / 2;

				Gfx.DrawSprite(Sprite.SPR_OTTD_O, off_x - 120, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_P, off_x -  86, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_E, off_x -  53, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_N, off_x -  22, 50);

				Gfx.DrawSprite(Sprite.SPR_OTTD_T, off_x +  34, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_T, off_x +  65, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_D, off_x +  96, 50);

				/*
				Gfx.DrawSprite(Sprite.SPR_OTTD_R, off_x + 119, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_A, off_x + 148, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_N, off_x + 181, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_S, off_x + 215, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_P, off_x + 246, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_O, off_x + 275, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_R, off_x + 307, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_T, off_x + 337, 50);

				Gfx.DrawSprite(Sprite.SPR_OTTD_T, off_x + 390, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_Y, off_x + 417, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_C, off_x + 447, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_O, off_x + 478, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_O, off_x + 509, 50);
				Gfx.DrawSprite(Sprite.SPR_OTTD_N, off_x + 541, 50);
				*/
			}
			break;

		case WE_KEYPRESS:
			//if (e.keycode == Window.WKC_BACKQUOTE) {
			if (e.ascii == '`') {
				ConsoleFactory.INSTANCE.getCurrentConsole().ifPresent(c -> c.switchState());
				e.cont = false;
				break;
			}

			switch (e.keycode) {
				case 'Q' | Window.WKC_CTRL:
				case 'Q' | Window.WKC_META:
					IntroGui.AskExitGame();
					break;
			}

			if (Global._game_mode == GM_MENU) break;

			//System.out.printf("wkey %x\n", e.keycode );
			
			switch (e.keycode) {
				case 'C':
				case 'Z': {
					Point pt = ViewPort.GetTileBelowCursor();
					if (pt.x != -1) {
						ViewPort.ScrollMainWindowTo(pt.x, pt.y);
						if (e.keycode == 'Z') MaxZoomIn();
					}
					break;
				}

				case Window.WKC_ESC: ViewPort.ResetObjectToPlace(); break;
				case Window.WKC_DELETE: Window.DeleteNonVitalWindows(); break;
				case Window.WKC_DELETE | Window.WKC_SHIFT: Window.DeleteAllNonVitalWindows(); break;
				case 'R' | Window.WKC_CTRL: Hal.MarkWholeScreenDirty(); break;

	/*#if defined(_DEBUG)
				case '0' | Window.WKC_ALT: // Crash the game 
					*(byte*)0 = 0;
					break;

				case '1' | Window.WKC_ALT: // Gimme money 
					/* Server can not cheat in advertise mode either! 
	//#ifdef ENABLE_NETWORK
	//				if (!_networking || !_network_server || !_network_advertise)
	//#endif 
						Cmd.DoCommandP(0, -10000000, 0, null, Cmd.CMD_MONEY_CHEAT);
					break;

				case '2' | Window.WKC_ALT: // Update the coordinates of all station signs 
					UpdateAllStationVirtCoord();
					break;
	#endif */

				case 'X':
					Global._display_opt ^= Global.DO_TRANS_BUILDINGS;
					Hal.MarkWholeScreenDirty();
					break;

	
				case Window.WKC_RETURN: case 'T' | Window.WKC_SHIFT:
					if (Global._networking) ShowNetworkChatQueryWindow(DestType.BROADCAST.ordinal(), 0);
					break;

				default: return;
			}
			e.cont = false;
			break;
		default:
			break;
		}
	}


	static void ShowVitalWindows()
	{
		Window w;

		w = Window.AllocateWindowDesc(_toolb_normal_desc,0);
		w.disabled_state = 1 << 17; // disable zoom-in button (by default game is zoomed in)
		w.flags4 = BitOps.RETCLRBITS(w.flags4, Window.WF_WHITE_BORDER_MASK);

		if (Global._networking) { // if networking, disable fast-forward button
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 1);
			if (!Global._network_server) // if not server, disable pause button
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 0);
		}

		Window.PositionMainToolbar(w); // already WC_MAIN_TOOLBAR passed (&_toolb_normal_desc)

		_main_status_desc.top = Hal._screen.height - 12;
		w = Window.AllocateWindowDesc(_main_status_desc,0);
		w.flags4 = BitOps.RETCLRBITS(w.flags4, Window.WF_WHITE_BORDER_MASK);

		w.as_def_d().data_1 = -1280;
	}

	void GameSizeChanged()
	{
		Global._cur_resolution[0] = Hal._screen.width;
		Global._cur_resolution[1] = Hal._screen.height;
		Window.RelocateAllWindows(Hal._screen.width, Hal._screen.height);
		Gfx.ScreenSizeChanged();
		Hal.MarkWholeScreenDirty();
	}
	

}





