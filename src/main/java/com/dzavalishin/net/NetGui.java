package com.dzavalishin.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.SwitchModes;
import com.dzavalishin.enums.WindowEvents;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Main;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Version;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.struct.FiosItem;
import com.dzavalishin.struct.Textbuf;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.FileIO;
import com.dzavalishin.util.Strings;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.GraphGui;
import com.dzavalishin.xui.Gui;
import com.dzavalishin.xui.IntroGui;
import com.dzavalishin.xui.MiscGui;
import com.dzavalishin.xui.Widget;
import com.dzavalishin.xui.Window;
import com.dzavalishin.xui.WindowDesc;
import com.dzavalishin.xui.WindowEvent;

public class NetGui extends Net implements NetDefs 
{


	private static final int  BGC = 5;
	private static final int BTC = 15;
	private static final int MAX_QUERYSTR_LEN = 64;
	//static char _edit_str_buf[MAX_QUERYStr.STR_LEN*2];

	static final String NOREV_STRING = "norev000";
	
	static int _selected_field;
	static boolean _first_time_show_network_game_window = true;

	static final int _connection_types_dropdown[] = {
		Str.STR_NETWORK_LAN_INTERNET,
		Str.STR_NETWORK_INTERNET_ADVERTISE,
		Str.INVALID_STRING_ID().id
	};

	static final int _lan_internet_types_dropdown[] = {
		Str.STR_NETWORK_LAN,
		Str.STR_NETWORK_INTERNET,
		Str.INVALID_STRING_ID().id
	};

	//enum {
		private static final int NET_PRC__OFFSET_TOP_WIDGET					= 74;
				private static final int NET_PRC__OFFSET_TOP_WIDGET_COMPANY	= 42;
				private static final int NET_PRC__SIZE_OF_ROW								= 14;
				private static final int NET_PRC__SIZE_OF_ROW_COMPANY				= 12;
	//};

	static NetworkGameList _selected_item = null;
	static int _selected_company_item = -1;

	//extern final char _openttd_revision[];

	static FiosItem _selected_map = null; // to highlight slected map

	// called when a new server is found on the network
	static void UpdateNetworkGameWindow(boolean unselect)
	{
		Window w = Window.FindWindowById(Window.WC_NETWORK_WINDOW, 0);

		if (w != null) {
			if (unselect) _selected_item = null;
			w.vscroll.setCount(_network_game_count);
			w.SetWindowDirty();
		}
	}

	static void NetworkGameWindowWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_CREATE: /* focus input box */
			_selected_field = 3;
			_selected_item = null;
			break;

		case WE_PAINT: {
			final NetworkGameList sel = _selected_item;

			w.disabled_state = 0;

			if (sel == null) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 17);
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 18);
			} else if (!sel.online) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 17); // Server offline, join button disabled
			} else if (sel.info.clients_on == sel.info.clients_max) {
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 17); // Server full, join button disabled

				// revisions don't match, check if server has no revision; then allow connection
			} else if (!sel.info.server_revision.equals(Version.NAME)) {
				if (!sel.info.server_revision.equals(NOREV_STRING))
					w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 17); // Revision mismatch, join button disabled
			}

			Global.SetDParam(0, 0x00);
			Global.SetDParam(7, _lan_internet_types_dropdown[_network_lan_internet]);
			w.DrawWindowWidgets();

			Textbuf.DrawEditBox(w, 3);

			Gfx.DrawString(9, 23, Str.STR_NETWORK_PLAYER_NAME, 2);
			Gfx.DrawString(9, 43, Str.STR_NETWORK_CONNECTION, 2);

			Gfx.DrawString(15, 63, Str.STR_NETWORK_GAME_NAME, 2);
			Gfx.DrawString(135, 63, Str.STR_NETWORK_CLIENTS_CAPTION, 2);

			{ // draw list of games
				int y = NET_PRC__OFFSET_TOP_WIDGET + 3;
				int n = 0;
				int pos = w.vscroll.getPos();
				//final NetworkGameList cur_item = _network_game_list;
				
				Iterator<NetworkGameList> gli = NetworkGameList._network_game_list.iterator();

				/*while (pos > 0 && cur_item != null) {
					pos--;
					cur_item = cur_item.next;
				}*/

				while (pos > 0 && gli.hasNext()) {
					pos--;
					gli.next();
				}

				//while (cur_item != null) 
				while (gli.hasNext()) 
				{
					final NetworkGameList cur_item = gli.next();
					
					boolean compatible =
						cur_item.info.server_revision.equals(Version.NAME) ||
						cur_item.info.server_revision.equals(NOREV_STRING);

					if (cur_item == sel)
						Gfx.GfxFillRect(11, y - 2, 218, y + 9, 10); // show highlighted item with a different colour

					Strings.SetDParamStr(0, cur_item.info.server_name);
					Gfx.DrawStringTruncated(15, y, new StringID(Str.STR_02BD), 16, 110);

					Global.SetDParam(0, cur_item.info.clients_on);
					Global.SetDParam(1, cur_item.info.clients_max);
					Gfx.DrawString(135, y, Str.STR_NETWORK_CLIENTS_ONLINE, 2);

					// only draw icons if the server is online
					if (cur_item.online) {
						// draw a lock if the server is password protected.
						if (cur_item.info.use_password) Gfx.DrawSprite(Sprite.SPR_LOCK, 186, y - 1);

						// draw red or green icon, depending on compatibility with server.
						Gfx.DrawSprite(Sprite.SPR_BLOT | (compatible ? Sprite.PALETTE_TO_GREEN : Sprite.PALETTE_TO_RED), 195, y);

						// draw flag according to server language
						Gfx.DrawSprite(Sprite.SPR_FLAGS_BASE + cur_item.info.server_lang, 206, y);
					}

					//cur_item = cur_item.next;
					y += NET_PRC__SIZE_OF_ROW;
					if (++n == w.vscroll.getCap()) break; // max number of games in the window
				}
			}

			// right menu
			Gfx.GfxFillRect(252, 23, 478, 65, 157);
			if (sel == null) {
				Gfx.DrawStringMultiCenter(365, 40, Str.STR_NETWORK_GAME_INFO, 0); // TODO Error: word too long in '??????????' 
			} else if (!sel.online) {
				Strings.SetDParamStr(0, sel.info.server_name);
				Gfx.DrawStringMultiCenter(365, 42, Str.STR_ORANGE, 2); // game name

				Gfx.DrawStringMultiCenter(365, 110, Str.STR_NETWORK_SERVER_OFFLINE, 2); // server offline
			} else { // show game info
				int y = 70;

				Gfx.DrawStringMultiCenter(365, 30, Str.STR_NETWORK_GAME_INFO, 0);


				Strings.SetDParamStr(0, sel.info.server_name);
				Gfx.DrawStringCenteredTruncated(w.getWidget(16).left, w.getWidget(16).right, 42, new StringID(Str.STR_ORANGE), 16); // game name

				Strings.SetDParamStr(0, sel.info.map_name);
				Gfx.DrawStringCenteredTruncated(w.getWidget(16).left, w.getWidget(16).right, 54, new StringID(Str.STR_02BD), 16); // map name

				Global.SetDParam(0, sel.info.clients_on);
				Global.SetDParam(1, sel.info.clients_max);
				Gfx.DrawString(260, y, Str.STR_NETWORK_CLIENTS, 2); // clients on the server / maximum slots
				y += 10;

				Global.SetDParam(0, Str.STR_NETWORK_LANG_ANY + sel.info.server_lang);
				Gfx.DrawString(260, y, Str.STR_NETWORK_LANGUAGE, 2); // server language
				y += 10;

				Global.SetDParam(0, Str.STR_TEMPERATE_LANDSCAPE + sel.info.map_set);
				Gfx.DrawString(260, y, Str.STR_NETWORK_TILESET, 2); // tileset
				y += 10;

				Global.SetDParam(0, sel.info.map_width);
				Global.SetDParam(1, sel.info.map_height);
				Gfx.DrawString(260, y, Str.STR_NETWORK_MAP_SIZE, 2); // map size
				y += 10;

				Strings.SetDParamStr(0, sel.info.server_revision);
				Gfx.DrawString(260, y, Str.STR_NETWORK_SERVER_VERSION, 2); // server version
				y += 10;

				Strings.SetDParamStr(0, sel.info.hostname);
				Global.SetDParam(1, sel.port);
				Gfx.DrawString(260, y, Str.STR_NETWORK_SERVER_ADDRESS, 2); // server address
				y += 10;

				Global.SetDParam(0, sel.info.start_date);
				Gfx.DrawString(260, y, Str.STR_NETWORK_START_DATE, 2); // start date
				y += 10;

				Global.SetDParam(0, sel.info.game_date);
				Gfx.DrawString(260, y, Str.STR_NETWORK_CURRENT_DATE, 2); // current date
				y += 10;

				y += 2;

				if (!sel.info.server_revision.equals(Version.NAME)) {
					if (!sel.info.server_revision.equals(NOREV_STRING))
						Gfx.DrawStringMultiCenter(365, y, Str.STR_NETWORK_VERSION_MISMATCH, 2); // server mismatch
				} else if (sel.info.clients_on == sel.info.clients_max) {
					// Show: server full, when clients_on == clients_max
					Gfx.DrawStringMultiCenter(365, y, Str.STR_NETWORK_SERVER_FULL, 2); // server full
				} else if (sel.info.use_password) {
					Gfx.DrawStringMultiCenter(365, y, Str.STR_NETWORK_PASSWORD, 2); // password warning
				}

				y += 10;
			}
		}	break;

		case WE_CLICK:
			_selected_field = e.widget;
			switch (e.widget) {
			case 0: case 14: /* Close 'X' | Cancel button */
				Window.DeleteWindowById(Window.WC_NETWORK_WINDOW, 0);
				break;
			case 4: case 5:
				w.ShowDropDownMenu( _lan_internet_types_dropdown, _network_lan_internet, 5, 0, 0); // do it for widget 5
				break;
			case 9: { /* Matrix to show networkgames */
				int id_v = (e.pt.y - NET_PRC__OFFSET_TOP_WIDGET) / NET_PRC__SIZE_OF_ROW;

				if (id_v >= w.vscroll.getCap()) return; // click out of bounds
				id_v += w.vscroll.getPos();

				{
					//NetworkGameList cur_item = _network_game_list;
					Iterator<NetworkGameList> gli = NetworkGameList._network_game_list.iterator();

					//for (; id_v > 0 && cur_item != null; id_v--)
					//	cur_item = cur_item.next;
					for (; id_v > 0 && gli.hasNext(); id_v--)
						gli.next();

					//if (cur_item == null)
					if(!gli.hasNext())
					{
						// click out of vehicle bounds
						_selected_item = null;
						w.SetWindowDirty();
						return;
					}
					_selected_item = gli.next();
				}
				w.SetWindowDirty();
			} break;
			case 11: /* Find server automatically */
				switch (_network_lan_internet) {
					case 0: NetUDP.NetworkUDPSearchGame(); break;
					case 1: NetUDP.NetworkUDPQueryMasterServer(); break;
				}
				break;
			case 12: { // Add a server
				MiscGui.ShowQueryString(
					Strings.BindCString(_network_default_ip),
					Str.STR_NETWORK_ENTER_IP,
					31 | 0x1000,  // maximum number of characters OR
					250, // characters up to this width pixels, whichever is satisfied first
					w.getWindow_class(),
					w.window_number);
			} break;
			case 13: /* Start server */
				ShowNetworkStartServerWindow();
				break;
			case 17: /* Join Game */
				if (_selected_item != null) {
					_network_game_info = _selected_item.info;
					//snprintf(_network_last_host, sizeof(_network_last_host), "%s", inet_ntoa(*(struct in_addr *)&_selected_item.ip));
					_network_last_port = _selected_item.port;
					ShowNetworkLobbyWindow();
				}
				break;
			case 18: // Refresh
				if (_selected_item != null) {
					NetworkQueryServer(_selected_item.info.hostname, _selected_item.port, true);
				}
				break;

		}	break;

		case WE_DROPDOWN_SELECT: /* we have selected a dropdown item in the list */
			switch(e.button) {
				case 5:
					_network_lan_internet = e.index;
					break;
			}

			w.SetWindowDirty();
			break;

		case WE_MOUSELOOP:
			if (_selected_field == 3) Textbuf.HandleEditBox(w, 3);
			break;

		case WE_KEYPRESS:
			if (_selected_field != 3) {
				if ( e.keycode == Window.WKC_DELETE ) { // press 'delete' to remove servers
					if (_selected_item != null) {
						NetworkGameList.removeItem(_selected_item);
						NetworkRebuildHostList();
						w.SetWindowDirty();
						_network_game_count--;
						// reposition scrollbar
						if (_network_game_count >= w.vscroll.getCap() && w.vscroll.getPos() > _network_game_count-w.vscroll.getCap()) w.vscroll.decrementPos();
						UpdateNetworkGameWindow(false);
						_selected_item = null;
					}
				}
				break;
			}

			if (Textbuf.HandleEditBoxKey(w, 3, e) == 1) break; // enter pressed

			String s = w.as_querystr_d().text.getString();
			// The name is only allowed when it starts with a letter!
			if (!Character.isWhitespace(s.charAt(0))) { 
				_network_player_name = s;
			} else {
				_network_player_name = "Player";
			}

			break;

		case WE_ON_EDIT_TEXT: {
			NetworkAddServer(e.str);
			NetworkRebuildHostList();
		} break;
		default:
			break;
		}
	}

	static final Widget _network_game_window_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,   BGC,     0,    10,     0,    13, Str.STR_00C5,										Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,   BGC,    11,   489,     0,    13, Str.STR_NETWORK_MULTIPLAYER,			Str.STR_NULL),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BGC,     0,   489,    14,   214, 0x0,													Str.STR_NULL),

	/* LEFT SIDE */
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BGC,    90,   231,    22,    33, 0x0,													Str.STR_NETWORK_ENTER_NAME_TIP),

	new Widget(          Window.WWT_6,   Window.RESIZE_NONE,   BGC,    90,   231,    42,    53, Str.STR_NETWORK_COMBO1,					Str.STR_NETWORK_CONNECTION_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,   BGC,   220,   230,    43,    52, Str.STR_0225,										Str.STR_NETWORK_CONNECTION_TIP),

	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BTC,    10,   130,    62,    73, 0x0,													Str.STR_NETWORK_GAME_NAME_TIP ),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BTC,   131,   180,    62,    73, 0x0,													Str.STR_NETWORK_CLIENTS_CAPTION_TIP ),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BTC,   181,   219,    62,    73, 0x0,													Str.STR_NETWORK_INFO_ICONS_TIP ),

	new Widget(     Window.WWT_MATRIX,   Window.RESIZE_NONE,   BGC,    10,   219,    74,   185, 0x801,												Str.STR_NETWORK_CLICK_GAME_TO_SELECT),
	new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,   BGC,   220,   231,    62,   185, 0x0,													Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),

	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,    10,   115,   195,   206, Str.STR_NETWORK_FIND_SERVER,			Str.STR_NETWORK_FIND_SERVER_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   125,   231,   195,   206, Str.STR_NETWORK_ADD_SERVER,			Str.STR_NETWORK_ADD_SERVER_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   250,   360,   195,   206, Str.STR_NETWORK_START_SERVER,		Str.STR_NETWORK_START_SERVER_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   370,   480,   195,   206, Str.STR_012E_CANCEL,							Str.STR_NULL),

	/* RIGHT SIDE */
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BGC,   250,   480,    22,   185, 0x0,					Str.STR_NULL),
	new Widget(          Window.WWT_6,   Window.RESIZE_NONE,   BGC,   251,   479,    23,   184, 0x0,					Str.STR_NULL),

	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   257,   360,   164,   175, Str.STR_NETWORK_JOIN_GAME,					Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   370,   473,   164,   175, Str.STR_NETWORK_REFRESH,					Str.STR_NETWORK_REFRESH_TIP),

	};

	static final WindowDesc _network_game_window_desc = new WindowDesc(
		Window.WDP_CENTER, Window.WDP_CENTER, 490, 215,
		Window.WC_NETWORK_WINDOW,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_network_game_window_widgets,
		NetGui::NetworkGameWindowWndProc
	);

	public static void ShowNetworkGameWindow()
	{
		int i;
		Window w;
		Window.DeleteWindowById(Window.WC_NETWORK_WINDOW, 0);

		/* Only show once */
		if (_first_time_show_network_game_window) {
			_first_time_show_network_game_window = false;
			// add all servers from the config file to our list
			for (i = 0; i != _network_host_list.length; i++) {
				if (_network_host_list[i] == null) break;
				NetworkAddServer(_network_host_list[i]);
			}
		}

		w = Window.AllocateWindowDesc(_network_game_window_desc);
		//_edit_str_buf = _network_player_name;
		w.vscroll.setCap(8);

		w.as_querystr_d().text.setCaret( true );
		w.as_querystr_d().text.maxlength = MAX_QUERYSTR_LEN - 1;
		w.as_querystr_d().text.maxwidth = 120;
		//w.as_querystr_d().text.buf = _edit_str_buf;
		w.as_querystr_d().text.setText(_network_player_name);
		w.as_querystr_d().text.UpdateTextBufferSize();

		UpdateNetworkGameWindow(true);
	}

	static final int _players_dropdown[] = {
		Str.STR_NETWORK_2_CLIENTS,
		Str.STR_NETWORK_3_CLIENTS,
		Str.STR_NETWORK_4_CLIENTS,
		Str.STR_NETWORK_5_CLIENTS,
		Str.STR_NETWORK_6_CLIENTS,
		Str.STR_NETWORK_7_CLIENTS,
		Str.STR_NETWORK_8_CLIENTS,
		Str.STR_NETWORK_9_CLIENTS,
		Str.STR_NETWORK_10_CLIENTS,
		Str.INVALID_STRING_ID().id
	};

	static final int _language_dropdown[] = {
		Str.STR_NETWORK_LANG_ANY,
		Str.STR_NETWORK_LANG_ENGLISH,
		Str.STR_NETWORK_LANG_GERMAN,
		Str.STR_NETWORK_LANG_FRENCH,
		Str.INVALID_STRING_ID().id
	};

	//enum {
		static final int NSSWND_START = 64;
		static final int NSSWND_ROWSIZE = 12;
	//};

	static void NetworkStartServerWindowWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_CREATE: /* focus input box */
			_selected_field = 3;
			_network_game_info.use_password = (_network_server_password.length() != 0);
			break;

		case WE_PAINT: {
			int y = NSSWND_START, pos;			

			Global.SetDParam(7, Str.STR_NETWORK_LAN_INTERNET + BitOps.b2i(_network_advertise));
			Global.SetDParam(9, Str.STR_NETWORK_2_CLIENTS + _network_game_info.clients_max - 2);
			Global.SetDParam(11, Str.STR_NETWORK_LANG_ANY + _network_game_info.server_lang);
			w.DrawWindowWidgets();

			Gfx.GfxFillRect(11, 63, 259, 171, 0xD7);

			Textbuf.DrawEditBox(w, 3);

			Gfx.DrawString(10, 22, Str.STR_NETWORK_NEW_GAME_NAME, 2);

			Gfx.DrawString(10, 43, Str.STR_NETWORK_SELECT_MAP, 2);
			Gfx.DrawString(280, 63, Str.STR_NETWORK_CONNECTION, 2);
			Gfx.DrawString(280, 95, Str.STR_NETWORK_NUMBER_OF_CLIENTS, 2);
			Gfx.DrawString(280, 127, Str.STR_NETWORK_LANGUAGE_SPOKEN, 2);

			if (_network_game_info.use_password) Gfx.DoDrawString("*", 408, 23, 3);

			// draw list of maps
			pos = w.vscroll.getPos();
			//while (pos < _fios_num + 1) 
			while (pos < MiscGui._fios_list.size() + 1) 
			{
				final FiosItem item = pos > 0 ? MiscGui._fios_list.get( pos - 1 ) : null;
				if (item == _selected_map || (pos == 0 && _selected_map == null))
					Gfx.GfxFillRect(11, y - 1, 259, y + 10, 155); // show highlighted item with a different colour

				if (pos == 0) Gfx.DrawString(14, y, Str.STR_4010_GENERATE_RANDOM_NEW_GAME, 9);
				else Gfx.DoDrawString(item.title, 14, y, MiscGui._fios_colors[item.type.ordinal()] );
				pos++;
				y += NSSWND_ROWSIZE;

				if (y >= w.vscroll.getCap() * NSSWND_ROWSIZE + NSSWND_START) break;
			}
		}	break;

		case WE_CLICK:
			_selected_field = e.widget;
			switch (e.widget) {
			case 0: /* Close 'X' */
			case 15: /* Cancel button */
				ShowNetworkGameWindow();
				break;

			case 4: /* Set password button */
				MiscGui.ShowQueryString(Strings.BindCString(_network_server_password),
					Str.STR_NETWORK_SET_PASSWORD, 20, 250, w.getWindow_class(), w.window_number);
				break;

			case 5: { /* Select map */
				int y = (e.pt.y - NSSWND_START) / NSSWND_ROWSIZE;

				y += w.vscroll.getPos();
				if (y >= w.vscroll.getCount()) return;

				_selected_map = (y == 0) ? null : MiscGui._fios_list.get( y - 1 );
				w.SetWindowDirty();
				} break;
			case 7: case 8: /* Connection type */
				w.ShowDropDownMenu( _connection_types_dropdown, BitOps.b2i(_network_advertise), 8, 0, 0); // do it for widget 8
				break;
			case 9: case 10: /* Number of Players */
				w.ShowDropDownMenu( _players_dropdown, _network_game_info.clients_max - 2, 10, 0, 0); // do it for widget 10
				return;
			case 11: case 12: /* Language */
				w.ShowDropDownMenu( _language_dropdown, _network_game_info.server_lang, 12, 0, 0); // do it for widget 12
				return;
			case 13: /* Start game */
				_is_network_server = true;
				_network_server_name = w.as_querystr_d().text.getString();
				w.as_querystr_d().text.UpdateTextBufferSize();
				if (_selected_map == null) { // start random new game
					IntroGui.GenRandomNewGame(Hal.Random(), Hal.InteractiveRandom());
				} else { // load a scenario
					String name = FileIO.FiosBrowseTo(_selected_map);
					if (name != null) {
						MiscGui.SetFiosType(_selected_map.type);
						Main._file_to_saveload.name = name;
						Main._file_to_saveload.title = _selected_map.title;

						w.DeleteWindow();
						IntroGui.StartScenarioEditor(Hal.Random(), Hal.InteractiveRandom());
					}
				}
				break;
			case 14: /* Load game */
				_is_network_server = true;
				_network_server_name = w.as_querystr_d().text.getString();
				w.as_querystr_d().text.UpdateTextBufferSize();
				/* XXX - Window.WC_NETWORK_WINDOW should stay, but if it stays, it gets
				 * copied all the elements of 'load game' and upon closing that, it segfaults */
				Window.DeleteWindowById(Window.WC_NETWORK_WINDOW, 0);
				MiscGui.ShowSaveLoadDialog(Global.SLD_LOAD_GAME);
				break;
			}
			break;

		case WE_DROPDOWN_SELECT: /* we have selected a dropdown item in the list */
			switch(e.button) {
				case 8:
					_network_advertise = (e.index != 0);
					break;
				case 10:
					_network_game_info.clients_max = e.index + 2;
					break;
				case 12:
					_network_game_info.server_lang = e.index;
					break;
			}

			w.SetWindowDirty();
			break;

		case WE_MOUSELOOP:
			if (_selected_field == 3) Textbuf.HandleEditBox(w, 3);
			break;

		case WE_KEYPRESS:
			if (_selected_field == 3) Textbuf.HandleEditBoxKey(w, 3, e);
			break;

		case WE_ON_EDIT_TEXT: {
			_network_server_password = e.str;
			_network_game_info.use_password = _network_server_password.length() != 0;
			w.SetWindowDirty();
		} break;
		default:
			break;
		}
	}

	static final Widget _network_start_server_window_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,   BGC,     0,    10,     0,    13, Str.STR_00C5,											Str.STR_018B_CLOSE_WINDOW ),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,   BGC,    11,   419,     0,    13, Str.STR_NETWORK_START_GAME_WINDOW,	Str.STR_NULL),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BGC,     0,   419,    14,   199, 0x0,														Str.STR_NULL),

	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BGC,   100,   272,    22,    33, 0x0,														Str.STR_NETWORK_NEW_GAME_NAME_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   285,   405,    22,    33, Str.STR_NETWORK_SET_PASSWORD,			Str.STR_NETWORK_PASSWORD_TIP),

	new Widget(          Window.WWT_6,   Window.RESIZE_NONE,   BGC,    10,   271,    62,   172, 0x0,														Str.STR_NETWORK_SELECT_MAP_TIP),
	new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,   BGC,   260,   271,    63,   171, 0x0,														Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),

	new Widget(          Window.WWT_6,   Window.RESIZE_NONE,   BGC,   280,   410,    77,    88, Str.STR_NETWORK_COMBO1,						Str.STR_NETWORK_CONNECTION_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,   BGC,   399,   409,    78,    87, Str.STR_0225,											Str.STR_NETWORK_CONNECTION_TIP),

	new Widget(          Window.WWT_6,   Window.RESIZE_NONE,   BGC,   280,   410,   109,   120, Str.STR_NETWORK_COMBO2,						Str.STR_NETWORK_NUMBER_OF_CLIENTS_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,   BGC,   399,   409,   110,   119, Str.STR_0225,											Str.STR_NETWORK_NUMBER_OF_CLIENTS_TIP),

	new Widget(          Window.WWT_6,   Window.RESIZE_NONE,   BGC,   280,   410,   141,   152, Str.STR_NETWORK_COMBO3,						Str.STR_NETWORK_LANGUAGE_TIP),
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,   BGC,   399,   409,   142,   151, Str.STR_0225,											Str.STR_NETWORK_LANGUAGE_TIP),

	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,    40,   140,   180,   191, Str.STR_NETWORK_START_GAME,				Str.STR_NETWORK_START_GAME_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   150,   250,   180,   191, Str.STR_NETWORK_LOAD_GAME,					Str.STR_NETWORK_LOAD_GAME_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   260,   360,   180,   191, Str.STR_012E_CANCEL,								Str.STR_NULL),

	};

	static final WindowDesc _network_start_server_window_desc = new WindowDesc(
		Window.WDP_CENTER, Window.WDP_CENTER, 420, 200,
		Window.WC_NETWORK_WINDOW,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_network_start_server_window_widgets,
		NetGui::NetworkStartServerWindowWndProc
	);

	static void ShowNetworkStartServerWindow()
	{
		Window w;
		Window.DeleteWindowById(Window.WC_NETWORK_WINDOW, 0);

		w = Window.AllocateWindowDesc(_network_start_server_window_desc);
		//_edit_str_buf = _network_server_name;

		Global._saveload_mode = Global.SLD_NEW_GAME;
		MiscGui.BuildFileList();
		w.vscroll.setCap(9);
		w.vscroll.setCount(MiscGui._fios_list.size()+1);

		w.as_querystr_d().text.setCaret( true );
		w.as_querystr_d().text.maxlength = MAX_QUERYSTR_LEN - 1;
		w.as_querystr_d().text.maxwidth = 160;
		//w.as_querystr_d().text.buf = _edit_str_buf;
		w.as_querystr_d().text.setText(_network_server_name);
		w.as_querystr_d().text.UpdateTextBufferSize();
	}

	static byte NetworkLobbyFindCompanyIndex(int pos)
	{
		byte i;

		/* Scroll through all _network_player_info and get the 'pos' item
		    that is not empty */
		for (i = 0; i < Global.MAX_PLAYERS; i++) {
			if (_network_player_info[i].company_name.length() != 0) {
				if (pos-- == 0) return i;
			}
		}

		return 0;
	}

	static void NetworkLobbyWindowWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int y = NET_PRC__OFFSET_TOP_WIDGET_COMPANY, pos;

			w.disabled_state = (_selected_company_item == -1) ? 1 << 7 : 0;

			if (_network_lobby_company_count == Global.MAX_PLAYERS)
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 8);
			/* You can not join a server as spectator when it has no companies active..
			     it causes some nasty crashes */
			if (_network_lobby_company_count == 0)
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 9);

			w.DrawWindowWidgets();

			Strings.SetDParamStr(0, _selected_item.info.server_name);
			Gfx.DrawString(10, 22, Str.STR_NETWORK_PREPARE_TO_JOIN, 2);

			// draw company list
			Gfx.GfxFillRect(11, 41, 154, 165, 0xD7);
			pos = w.vscroll.getPos();
			while (pos < _network_lobby_company_count) {
				byte index = NetworkLobbyFindCompanyIndex(pos);
				boolean income = false;
				if (_selected_company_item == index)
					Gfx.GfxFillRect(11, y - 1, 154, y + 10, 155); // show highlighted item with a different colour

				Gfx.DoDrawString(_network_player_info[index].company_name, 13, y, 2);
				if (_network_player_info[index].use_password != 0)
					Gfx.DrawSprite(Sprite.SPR_LOCK, 135, y);

				/* If the company's income was positive puts a green dot else a red dot */
				if ((_network_player_info[index].income) >= 0)
					income = true;
				Gfx.DrawSprite(Sprite.SPR_BLOT | (income ? Sprite.PALETTE_TO_GREEN : Sprite.PALETTE_TO_RED), 145, y);

				pos++;
				y += NET_PRC__SIZE_OF_ROW_COMPANY;
				if (pos >= w.vscroll.getCap()) break;
			}

			// draw info about selected company
			Gfx.DrawStringMultiCenter(290, 48, Str.STR_NETWORK_COMPANY_INFO, 0);
			if (_selected_company_item != -1) { // if a company is selected...
				// show company info
				final int x = 183;
				y = 65;

				Strings.SetDParamStr(0, _network_player_info[_selected_company_item].company_name);
				Gfx.DrawString(x, y, Str.STR_NETWORK_COMPANY_NAME, 2);
				y += 10;

				Global.SetDParam(0, _network_player_info[_selected_company_item].inaugurated_year + Global.MAX_YEAR_BEGIN_REAL);
				Gfx.DrawString(x, y, Str.STR_NETWORK_INAUGURATION_YEAR, 2); // inauguration year
				y += 10;

				Global.SetDParam64(0, _network_player_info[_selected_company_item].company_value);
				Gfx.DrawString(x, y, Str.STR_NETWORK_VALUE, 2); // company value
				y += 10;

				Global.SetDParam64(0, _network_player_info[_selected_company_item].money);
				Gfx.DrawString(x, y, Str.STR_NETWORK_CURRENT_BALANCE, 2); // current balance
				y += 10;

				Global.SetDParam64(0, _network_player_info[_selected_company_item].income);
				Gfx.DrawString(x, y, Str.STR_NETWORK_LAST_YEARS_INCOME, 2); // last year's income
				y += 10;

				Global.SetDParam(0, _network_player_info[_selected_company_item].performance);
				Gfx.DrawString(x, y, Str.STR_NETWORK_PERFORMANCE, 2); // performance
				y += 10;

				Global.SetDParam(0, _network_player_info[_selected_company_item].num_vehicle[0]);
				Global.SetDParam(1, _network_player_info[_selected_company_item].num_vehicle[1]);
				Global.SetDParam(2, _network_player_info[_selected_company_item].num_vehicle[2]);
				Global.SetDParam(3, _network_player_info[_selected_company_item].num_vehicle[3]);
				Global.SetDParam(4, _network_player_info[_selected_company_item].num_vehicle[4]);
				Gfx.DrawString(x, y, Str.STR_NETWORK_VEHICLES, 2); // vehicles
				y += 10;

				Global.SetDParam(0, _network_player_info[_selected_company_item].num_station[0]);
				Global.SetDParam(1, _network_player_info[_selected_company_item].num_station[1]);
				Global.SetDParam(2, _network_player_info[_selected_company_item].num_station[2]);
				Global.SetDParam(3, _network_player_info[_selected_company_item].num_station[3]);
				Global.SetDParam(4, _network_player_info[_selected_company_item].num_station[4]);
				Gfx.DrawString(x, y, Str.STR_NETWORK_STATIONS, 2); // stations
				y += 10;

				Strings.SetDParamStr(0, _network_player_info[_selected_company_item].players);
				Gfx.DrawString(x, y, Str.STR_NETWORK_PLAYERS, 2); // players
				y += 10;
			}
		}	break;

		case WE_CLICK:
			switch(e.widget) {
			case 0: case 11: /* Close 'X' | Cancel button */
				ShowNetworkGameWindow();
				break;
			case 3: /* Company list */
				_selected_company_item = (e.pt.y - NET_PRC__OFFSET_TOP_WIDGET_COMPANY) / NET_PRC__SIZE_OF_ROW_COMPANY;

				if (_selected_company_item >= w.vscroll.getCap()) {
					// click out of bounds
					_selected_company_item = -1;
					w.SetWindowDirty();
					return;
				}
				_selected_company_item += w.vscroll.getPos();
				if (_selected_company_item >= _network_lobby_company_count) {
					_selected_company_item = -1;
					w.SetWindowDirty();
					return;
				}

				_selected_company_item = NetworkLobbyFindCompanyIndex(_selected_company_item);

				w.SetWindowDirty();
				break;
			case 7: /* Join company */
				if (_selected_company_item != -1) {
					Global._network_playas = _selected_company_item + 1;
					NetworkClientConnectGame(_network_last_host, _network_last_port);
				}
				break;
			case 8: /* New company */
				Global._network_playas = 0;
				NetworkClientConnectGame(_network_last_host, _network_last_port);
				break;
			case 9: /* Spectate game */
				Global._network_playas = Owner.OWNER_SPECTATOR;
				NetworkClientConnectGame(_network_last_host, _network_last_port);
				break;
			case 10: /* Refresh */
				NetworkQueryServer(_network_last_host, _network_last_port, false);
				break;
			}	break;

		case WE_CREATE:
			_selected_company_item = -1;
		default:
			break;
		}
	}

	static final Widget _network_lobby_window_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,   BGC,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW ),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,   BGC,    11,   419,     0,    13, Str.STR_NETWORK_GAME_LOBBY,		Str.STR_NULL),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BGC,     0,   419,    14,   209, 0x0,												Str.STR_NULL),

	// company list
	new Widget(          Window.WWT_6,   Window.RESIZE_NONE,   BGC,    10,   167,    40,   166, 0x0,												Str.STR_NETWORK_COMPANY_LIST_TIP),
	new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,   BGC,   155,   166,    41,   165, 0x1,												Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),

	// company/player info
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,   BGC,   173,   404,    38,   166, 0x0,					Str.STR_NULL),
	new Widget(          Window.WWT_6,   Window.RESIZE_NONE,   BGC,   174,   403,    39,   165, 0x0,					Str.STR_NULL),

	// buttons
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,    10,   151,   175,   186, Str.STR_NETWORK_JOIN_COMPANY,	Str.STR_NETWORK_JOIN_COMPANY_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,    10,   151,   190,   201, Str.STR_NETWORK_NEW_COMPANY,		Str.STR_NETWORK_NEW_COMPANY_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   158,   268,   175,   186, Str.STR_NETWORK_SPECTATE_GAME,	Str.STR_NETWORK_SPECTATE_GAME_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   158,   268,   190,   201, Str.STR_NETWORK_REFRESH,				Str.STR_NETWORK_REFRESH_TIP),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,   278,   388,   175,   186, Str.STR_012E_CANCEL,						Str.STR_NULL),


	};

	static final WindowDesc _network_lobby_window_desc = new WindowDesc(
		Window.WDP_CENTER, Window.WDP_CENTER, 420, 210,
		Window.WC_NETWORK_WINDOW,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
		_network_lobby_window_widgets,
		NetGui::NetworkLobbyWindowWndProc
	);


	static void ShowNetworkLobbyWindow()
	{
		Window w;
		Window.DeleteWindowById(Window.WC_NETWORK_WINDOW, 0);

		_network_lobby_company_count = 0;

		NetworkQueryServer(_network_last_host, _network_last_port, false);

		w = Window.AllocateWindowDesc(_network_lobby_window_desc);
		//_edit_str_buf = "";
		w.as_querystr_d().text.setText("");
		w.vscroll.setPos(0);
		w.vscroll.setCap(8);
	}




	// The window below gives information about the connected clients
	//  and also makes able to give money to them, kick them (if server)
	//  and stuff like that.

	//extern void DrawPlayerIcon(int p, int x, int y);

	// Every action must be of this form
	//typedef void ClientList_Action_Proc(byte client_no);

	// Max 10 actions per client
	private static final int  MAX_CLIENTLIST_ACTION = 10;

	// Some standard bullshit.. defines variables ;)
	//static void ClientListWndProc(Window w, WindowEvent e);
	//static void ClientListPopupWndProc(Window w, WindowEvent e);
	static int _selected_clientlist_item = 255;
	static int _selected_clientlist_y = 0;
	static String [] _clientlist_action = new String[MAX_CLIENTLIST_ACTION];
	static ClientList_Action_Proc [] _clientlist_proc = new ClientList_Action_Proc [MAX_CLIENTLIST_ACTION];

	//enum {
		static final int CLNWND_OFFSET = 16;
		static final int CLNWND_ROWSIZE = 10;
	//};

	static final Widget _client_list_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,                 Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   249,     0,    13, Str.STR_NETWORK_CLIENT_LIST,  Str.STR_018C_WINDOW_TITLE_DRAG_THIS),

	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   249,    14,    14 + CLNWND_ROWSIZE + 1, 0x0, Str.STR_NULL),
	};

	static final Widget _client_list_popup_widgets[] = {
	new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   99,     0,     0,     0,	Str.STR_NULL),

	};

	static WindowDesc _client_list_desc = new WindowDesc(
		-1, -1, 250, 1,
		Window.WC_CLIENT_LIST,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
		_client_list_widgets,
		NetGui::ClientListWndProc
	);

	// Finds the Xth client-info that is active
	static final NetworkClientInfo NetworkFindClientInfo(int client_no)
	{
		//for (NetworkClientInfo ci : _network_client_info) 
		for (NetworkClientState cs : _clients) 
		{
			// Skip non-active items
			if (cs.ci.client_index == NETWORK_EMPTY_INDEX) continue;
			if (client_no == 0) return cs.ci;
			client_no--;
		}

		return null;
	}

	// Here we start to define the options out of the menu
	static void ClientList_Kick(int client_no)
	{
		if (client_no < Global.MAX_PLAYERS)
			NetServer.NetworkPacketSend_PACKET_SERVER_ERROR_command(_clients.get(client_no), NetworkErrorCode.KICKED);
	}

	static void ClientList_Ban(int client_no)
	{
		int i;
		InetAddress ip = NetworkFindClientInfo(client_no).client_ip;

		for (i = 0; i < _network_ban_list.length; i++) {
			if (_network_ban_list[i] == null || _network_ban_list[i].length() == 0) {
				//_network_ban_list[i] = strdup(inet_ntoa(*(struct in_addr *)&ip));
				_network_ban_list[i] = ip.getHostAddress();
				break;
			}
		}

		if (client_no < Global.MAX_PLAYERS)
			NetServer.NetworkPacketSend_PACKET_SERVER_ERROR_command(_clients.get(client_no), NetworkErrorCode.KICKED);
	}

	static String ntoa(long raw) {
	    byte[] b = new byte[] {(byte)(raw >> 24), (byte)(raw >> 16), (byte)(raw >> 8), (byte)raw};
	    try {
	        return InetAddress.getByAddress(b).getHostAddress();
	    } catch (UnknownHostException e) {
	        //No way here
	        return "(null)";
	    }
	}
	
	static void ClientList_GiveMoney(int client_no)
	{
		if (NetworkFindClientInfo(client_no) != null)
			Gui.ShowNetworkGiveMoneyWindow(NetworkFindClientInfo(client_no).client_playas - 1);
	}

	static void ClientList_SpeakToClient(int client_no)
	{
		if (NetworkFindClientInfo(client_no) != null)
			Gui.ShowNetworkChatQueryWindow(DestType.CLIENT.ordinal(), NetworkFindClientInfo(client_no).client_index);
	}

	static void ClientList_SpeakToPlayer(int client_no)
	{
		if (NetworkFindClientInfo(client_no) != null)
			Gui.ShowNetworkChatQueryWindow(DestType.PLAYER.ordinal(), NetworkFindClientInfo(client_no).client_playas);
	}

	static void ClientList_SpeakToAll(int client_no)
	{
		Gui.ShowNetworkChatQueryWindow(DestType.BROADCAST.ordinal(), 0);
	}

	static void ClientList_None(int client_no)
	{
		// No action ;)
	}



	// Help, a action is clicked! What do we do?
	static void HandleClientListPopupClick(int index, int clientno) {
		// A click on the Popup of the ClientList.. handle the command
		if (index < MAX_CLIENTLIST_ACTION && _clientlist_proc[index] != null) {
			_clientlist_proc[index].proc(clientno);
		}
	}

	// Finds the amount of clients and set the height correct
	static boolean CheckClientListHeight(Window w)
	{
		int num = 0;

		/*/ Should be replaced with a loop through all clients
		for (NetworkClientInfo ci : _network_client_info) {
			// Skip non-active items
			if (ci.client_index == NETWORK_EMPTY_INDEX) continue;
			num++;
		}*/

		for (NetworkClientState cs : _clients) {
			// Skip non-active items
			if (cs.ci.client_index == NETWORK_EMPTY_INDEX) continue;
			num++;
		}

		num *= CLNWND_ROWSIZE;

		// If height is changed
		if (w.getHeight() != CLNWND_OFFSET + num + 1) {
			// XXX - magic unfortunately; (num + 2) has to be one bigger than heigh (num + 1)
			w.SetWindowDirty();
			w.getWidget(2).bottom = w.getWidget(2).top + num + 2;
			w.setHeight( CLNWND_OFFSET + num + 1 );
			w.SetWindowDirty();
			return false;
		}
		return true;
	}

	// Finds the amount of actions in the popup and set the height correct
	static int ClientListPopupHeigth() {
		int i, num = 0;

		// Find the amount of actions
		for (i = 0; i < MAX_CLIENTLIST_ACTION; i++) {
			if (_clientlist_action[i].length() == 0) continue;
			if (_clientlist_proc[i] == null) continue;
			num++;
		}

		num *= CLNWND_ROWSIZE;

		return num + 1;
	}

	// Show the popup (action list)
	static Window PopupClientList(Window w, int client_no, int x, int y)
	{
		int i, h;
		final NetworkClientInfo ci;
		Window.DeleteWindowById(Window.WC_TOOLBAR_MENU, 0);

		// Clean the current actions
		for (i = 0; i < MAX_CLIENTLIST_ACTION; i++) {
			_clientlist_action[i] = "";
			_clientlist_proc[i] = null;
		}

		// Fill the actions this client has
		// Watch is, max 50 chars long!

		ci = NetworkFindClientInfo(client_no);
		if (ci == null) return null;

		i = 0;
		if (_network_own_client_index != ci.client_index) {
			_clientlist_action[i] = Strings.GetString(Str.STR_NETWORK_CLIENTLIST_SPEAK_TO_CLIENT);
			_clientlist_proc[i++] = NetGui::ClientList_SpeakToClient;
		}

		if (ci.client_playas >= 1 && ci.client_playas <= Global.MAX_PLAYERS) {
			_clientlist_action[i] = Strings.GetString(Str.STR_NETWORK_CLIENTLIST_SPEAK_TO_COMPANY);
			_clientlist_proc[i++] = NetGui::ClientList_SpeakToPlayer;
		}
		_clientlist_action[i] = Strings.GetString(Str.STR_NETWORK_CLIENTLIST_SPEAK_TO_ALL);
		_clientlist_proc[i++] = NetGui::ClientList_SpeakToAll;

		if (_network_own_client_index != ci.client_index) {
			if (Global._network_playas >= 1 && Global._network_playas <= Global.MAX_PLAYERS) {
				// We are no spectator
				if (ci.client_playas >= 1 && ci.client_playas <= Global.MAX_PLAYERS) {
					_clientlist_action[i] = Strings.GetString(Str.STR_NETWORK_CLIENTLIST_GIVE_MONEY);
					_clientlist_proc[i++] = NetGui::ClientList_GiveMoney;
				}
			}
		}

		// A server can kick clients (but not hisself)
		if (Global._network_server && _network_own_client_index != ci.client_index) {
			_clientlist_action[i] = Strings.GetString(Str.STR_NETWORK_CLIENTLIST_KICK);
			_clientlist_proc[i++] = NetGui::ClientList_Kick;

			_clientlist_action[i] ="Ban";
			_clientlist_proc[i++] = NetGui::ClientList_Ban;
		}

		if (i == 0) {
			_clientlist_action[i] = Strings.GetString(Str.STR_NETWORK_CLIENTLIST_NONE);
			_clientlist_proc[i++] = NetGui::ClientList_None;
		}

		/* Calculate the height */
		h = ClientListPopupHeigth();

		// Allocate the popup
		w = Window.AllocateWindow(x, y, 100, h + 1, NetGui::ClientListPopupWndProc, Window.WC_TOOLBAR_MENU, _client_list_popup_widgets);
		w.getWidget(0).bottom = w.getWidget(0).top + h;

		w.disableWhiteBorder();
		w.as_menu_d().item_count = 0;
		// Save our client
		w.as_menu_d().main_button = client_no;
		w.as_menu_d().sel_index = 0;
		// We are a popup
		
		Window.activatePopup();

		return w;
	}

	// Main handle for the popup
	static void ClientListPopupWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int i, y, sel;
			byte colour;
			w.DrawWindowWidgets();

			// Draw the actions
			sel = w.as_menu_d().sel_index;
			y = 1;
			for (i = 0; i < MAX_CLIENTLIST_ACTION; i++, y += CLNWND_ROWSIZE) {
				if (_clientlist_action[i].length() == 0) continue;
				if (_clientlist_proc[i] == null) continue;

				if (sel-- == 0) { // Selected item, highlight it
					Gfx.GfxFillRect(1, y, 98, y + CLNWND_ROWSIZE - 1, 0);
					colour = 0xC;
				} else colour = 0x10;

				Gfx.DoDrawString(_clientlist_action[i], 4, y, colour);
			}
		}	break;

		case WE_POPUPMENU_SELECT: {
			// We selected an action
			int index = (e.pt.y - w.getTop()) / CLNWND_ROWSIZE;

			if (index >= 0 && e.pt.y >= w.getTop())
				HandleClientListPopupClick(index, w.as_menu_d().main_button);

			// Sometimes, because of the bad DeleteWindow-proc, the 'w' pointer is
			//  invalid after the last functions (mostly because it kills a window
			//  that is in front of 'w', and because of a silly memmove, the address
			//  'w' was pointing to becomes invalid), so we need to refetch
			//  the right address...
			Window.DeleteWindowById(Window.WC_TOOLBAR_MENU, 0);
		}	break;

		case WE_POPUPMENU_OVER: {
			// Our mouse hoovers over an action? Select it!
			int index = (e.pt.y - w.getTop()) / CLNWND_ROWSIZE;

			if (index == -1 || index == w.as_menu_d().sel_index)
				return;

			w.as_menu_d().sel_index = index;
			w.SetWindowDirty();
		} break;
		default:
			break;

		}
	}

	// Main handle for clientlist
	static void ClientListWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int y, i = 0;
			byte colour;

			// Check if we need to reset the height
			if (!CheckClientListHeight(w)) break;

			w.DrawWindowWidgets();

			y = CLNWND_OFFSET;

			//for (NetworkClientInfo ci : _network_client_info) 
			for (NetworkClientState cs : _clients) 
			{
				NetworkClientInfo ci = cs.ci;
				// Skip non-active items
				if (ci.client_index == NETWORK_EMPTY_INDEX) continue;

				if (_selected_clientlist_item == i++) { // Selected item, highlight it
					Gfx.GfxFillRect(1, y, 248, y + CLNWND_ROWSIZE - 1, 0);
					colour = 0xC;
				} else
					colour = 0x10;

				if (ci.client_index == NETWORK_SERVER_INDEX) {
					Gfx.DrawString(4, y, Str.STR_NETWORK_SERVER, colour);
				} else {
					Gfx.DrawString(4, y, Str.STR_NETWORK_CLIENT, colour);
				}

				// Filter out spectators
				if (ci.client_playas > 0 && ci.client_playas <= Global.MAX_PLAYERS)
					GraphGui.DrawPlayerIcon(ci.client_playas - 1, 44, y + 1);

				Gfx.DoDrawString(ci.client_name, 61, y, colour);

				y += CLNWND_ROWSIZE;
			}
		}	break;

		case WE_CLICK:
			// Show the popup with option
			if (_selected_clientlist_item != 255) {
				PopupClientList(w, _selected_clientlist_item, e.pt.x + w.getLeft(), e.pt.y + w.getTop());
			}

			break;

		case WE_MOUSEOVER:
			// -1 means we left the current window
			if (e.pt.y == -1) {
				_selected_clientlist_y = 0;
				_selected_clientlist_item = 255;
				w.SetWindowDirty();
				break;
			}
			// It did not change.. no update!
			if (e.pt.y == _selected_clientlist_y) break;

			// Find the new selected item (if any)
			_selected_clientlist_y = e.pt.y;
			if (e.pt.y > CLNWND_OFFSET) {
				_selected_clientlist_item = (e.pt.y - CLNWND_OFFSET) / CLNWND_ROWSIZE;
			} else
				_selected_clientlist_item = 255;

			// Repaint
			w.SetWindowDirty();
			break;

		case WE_DESTROY: case WE_CREATE:
			// When created or destroyed, data is reset
			_selected_clientlist_item = 255;
			_selected_clientlist_y = 0;
			break;
		default:
			break;
		}
	}

	public static void ShowClientList()
	{
		Window w = Window.AllocateWindowDescFront(_client_list_desc, 0);
		if (w != null) w.window_number = 0;
	}

	//extern void SwitchMode(int new_mode);

	static void NetworkJoinStatusWindowWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int progress; // used for progress bar
			w.DrawWindowWidgets();

			// TODO are ordinals correct?
			Gfx.DrawStringCentered(125, 35, Str.STR_NETWORK_CONNECTING_1 + _network_join_status.ordinal(), 14);
			switch (_network_join_status) {
				case CONNECTING: case AUTHORIZING:
				case GETTING_COMPANY_INFO:
					progress = 10; // first two stages 10%
					break;
				case WAITING:
					Global.SetDParam(0, _network_join_waiting);
					Gfx.DrawStringCentered(125, 46, Str.STR_NETWORK_CONNECTING_WAITING, 14);
					progress = 15; // third stage is 15%
					break;
				case DOWNLOADING:
					Global.SetDParam(0, _network_join_kbytes);
					Global.SetDParam(1, _network_join_kbytes_total);
					Gfx.DrawStringCentered(125, 46, Str.STR_NETWORK_CONNECTING_DOWNLOADING, 14);
					/* Fallthrough */
				default: /* Waiting is 15%, so the resting receivement of map is maximum 70% */
					progress = 15 + _network_join_kbytes * (100 - 15) / _network_join_kbytes_total;
			}

			/* Draw nice progress bar :) */
			Gfx.DrawFrameRect(20, 18, (int)((w.getWidth() - 20) * progress / 100), 28, 10, 0);
		}	break;

		case WE_CLICK:
			switch (e.widget) {
				case 0: /* Close 'X' */
				case 3: /* Disconnect button */
					NetworkDisconnect();
					Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
					Main.SwitchMode(SwitchModes.SM_MENU);
					ShowNetworkGameWindow();
					break;
			}
			break;
		default:
			break;

		}
	}

	static final Widget _network_join_status_window_widget[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   249,     0,    13, Str.STR_NETWORK_CONNECTING, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   249,    14,    84, 0x0,Str.STR_NULL),
	new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,   BTC,    75,   175,    69,    80, Str.STR_NETWORK_DISCONNECT, Str.STR_NULL),

	};

	static final WindowDesc _network_join_status_window_desc = 	new WindowDesc(
		Window.WDP_CENTER, Window.WDP_CENTER, 250, 85,
		Window.WC_NETWORK_STATUS_WINDOW, 0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET,
		_network_join_status_window_widget,
		NetGui::NetworkJoinStatusWindowWndProc
	);

	static void ShowJoinStatusWindow()
	{
		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
		_network_join_status = NetworkJoinStatus.CONNECTING;
		Window.AllocateWindowDesc(_network_join_status_window_desc);
	}

	public static void ShowJoinStatusWindowAfterJoin()
	{
		/* This is a special instant of ShowJoinStatusWindow, because
		    it is opened after the map is loaded, but the client maybe is not
		    done registering itself to the server */
		Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);
		_network_join_status = NetworkJoinStatus.REGISTERING;
		Window.AllocateWindowDesc(_network_join_status_window_desc);
	}


	static boolean chatClosed = false;

	static void NetChatSend(Window w)
	{
		String buf = w.as_querystr_d().text.getString().strip();
		
		if (buf.length() == 0) {
			w.DeleteWindow();
		} else {
			int wnd_class = w.as_querystr_d().wnd_class;
			int wnd_num = w.as_querystr_d().wnd_num;
			Window parent;

			// Mask the edit-box as closed, so we don't send out a CANCEL
			chatClosed = true;

			w.DeleteWindow();

			parent = Window.FindWindowById(wnd_class, wnd_num);
			if (parent != null) {
				WindowEvent e = new WindowEvent();
				e.event = WindowEvents.WE_ON_EDIT_TEXT;
				e.str = buf;
				//parent.wndproc(parent, e);
				parent.sendEvent(e);
			}
		}
		
	}
	//#define MAX_QUERYSTR_LEN 64

	static void ChatWindowWndProc(Window w, WindowEvent e)
	{

		switch (e.event) {
		case WE_CREATE:
			Window.SendWindowMessage(Window.WC_NEWS_WINDOW, 0, WindowEvents.WE_CREATE.ordinal(), w.getHeight(), 0);
			Global._no_scroll = BitOps.RETSETBIT(Global._no_scroll, Global.SCROLL_CHAT); // do not scroll the game with the arrow-keys
			chatClosed = false;
			break;

		case WE_PAINT:
			w.DrawWindowWidgets();
			Textbuf.DrawEditBox(w, 1);
			break;

		case WE_CLICK:
			switch (e.widget) {
			case 3: w.DeleteWindow(); break; // Cancel
			case 2: // Send
	//press_ok:;
				NetChatSend(w);
				break;
			}
			break;

		case WE_MOUSELOOP: {
			if (null == Window.FindWindowById(w.as_querystr_d().wnd_class, w.as_querystr_d().wnd_num)) {
				w.DeleteWindow();
				return;
			}
			Textbuf.HandleEditBox(w, 1);
		} break;

		case WE_KEYPRESS: {
			switch(Textbuf.HandleEditBoxKey(w, 1, e)) {
			case 1: // Return
				//goto press_ok;
				NetChatSend(w);
				break;
			case 2: // Escape
				w.DeleteWindow();
				break;
			}
		} break;

		case WE_DESTROY:
			Window.SendWindowMessage(Window.WC_NEWS_WINDOW, 0, WindowEvents.WE_DESTROY.ordinal(), 0, 0);
			Global._no_scroll = BitOps.RETCLRBIT(Global._no_scroll, Global.SCROLL_CHAT);
			// If the window is not closed yet, it means it still needs to send a CANCEL
			if (!chatClosed) {
				Window parent = Window.FindWindowById(w.as_querystr_d().wnd_class, w.as_querystr_d().wnd_num);
				if (parent != null) {
					WindowEvent e1 = new WindowEvent();
					e1.event = WindowEvents.WE_ON_EDIT_TEXT_CANCEL;
					//parent.wndproc(parent, e1);
					parent.sendEvent(e1);
				}
			}
			break;
		default:
			break;
		}
	}

	static final Widget _chat_window_widgets[] = {
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   639,     0,    13, Str.STR_NULL,         Str.STR_NULL), // background
	new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   399,     1,    12, Str.STR_NULL,         Str.STR_NULL), // text box
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   400,   519,     1,    12, Str.STR_NETWORK_SEND, Str.STR_NULL), // send button
	new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   520,   639,     1,    12, Str.STR_012E_CANCEL,  Str.STR_NULL), // cancel button
	
	};

	static final WindowDesc _chat_window_desc = new WindowDesc(
		Window.WDP_CENTER, -26, 640, 14, // x, y, width, height
		Window.WC_SEND_NETWORK_MSG,0,
		WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET,
		_chat_window_widgets,
		NetGui::ChatWindowWndProc
	);

	public static void ShowChatWindow(StringID str, StringID caption, int maxlen, int maxwidth, int window_class, int window_number)
	{
		Window w;

	//#define _orig_edit_str_buf (_edit_str_buf+MAX_QUERYSTR_LEN)

		Window.DeleteWindowById(Window.WC_SEND_NETWORK_MSG, 0);

		/*
		_orig_edit_str_buf = Strings.GetString(str);

		//_orig_edit_str_buf[maxlen] = '\0';
		_orig_edit_str_buf = _orig_edit_str_buf.substring(0, maxlen);

		_edit_str_buf = _orig_edit_str_buf;
		*/
		
		//_edit_str_buf = Strings.GetString(str);
		
		w = Window.AllocateWindowDesc(_chat_window_desc);

		w.click_state = 1 << 1;
		w.as_querystr_d().caption = caption;
		w.as_querystr_d().wnd_class = window_class;
		w.as_querystr_d().wnd_num = window_number;
		w.as_querystr_d().text.setCaret(false);
		w.as_querystr_d().text.maxlength = maxlen - 1;
		w.as_querystr_d().text.maxwidth = maxwidth;
		w.as_querystr_d().text.setText( Strings.GetString(str) );
		//w.as_querystr_d().text.buf = _edit_str_buf;
		w.as_querystr_d().text.UpdateTextBufferSize();
	}


}


//typedef void ClientList_Action_Proc(byte client_no);
@FunctionalInterface
interface ClientList_Action_Proc
{
	void proc( int clientNo );
}
