package com.dzavalishin.xui;

import com.dzavalishin.enums.FiosType;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.SwitchModes;
import com.dzavalishin.enums.WindowEvents;
import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Cmd;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.Main;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.SaveLoad;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TextEffect;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Town;
import com.dzavalishin.game.Tree;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.struct.FiosItem;
import com.dzavalishin.struct.LandInfoData;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.Textbuf;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BinaryString;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.FileIO;
import com.dzavalishin.util.GameDate;
import com.dzavalishin.util.IntContainer;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.Strings;
import com.dzavalishin.util.YearMonthDay;

import java.util.List;

public class MiscGui {

	private static boolean _fios_path_changed;
	private static boolean _savegame_sort_dirty;
	private static int _savegame_sort_order;

	//private static final int SORT_ASCENDING  = 0;
	private static final int SORT_DESCENDING = 1;
	private static final int SORT_BY_DATE    = 0;
	private static final int SORT_BY_NAME    = 2;




	static void LandInfoWndProc(Window w, WindowEvent e)
	{
		if (e.event == WindowEvents.WE_PAINT) {
			final LandInfoData lid;
			//StringID 
			int str;
			int i;

			w.DrawWindowWidgets();

			lid = (LandInfoData) w.as_void_d().data;

			if(lid.td.dparam[0] != null) Global.SetDParam(0, lid.td.dparam[0]);
			else Global.SetDParam(0, 0);
			Gfx.DrawStringCentered(140, 16, lid.td.str, 13);

			Global.SetDParam(0, Str.STR_01A6_N_A);
			if (lid.td.owner != Owner.OWNER_NONE && lid.td.owner != Owner.OWNER_WATER)
				Player.GetNameOfOwner(PlayerID.get(lid.td.owner), lid.tile);
			Gfx.DrawStringCentered(140, 27, Str.STR_01A7_OWNER, 0);

			str = Str.STR_01A4_COST_TO_CLEAR_N_A;
			if (!Cmd.CmdFailed(lid.costclear)) {
				Global.SetDParam(0, lid.costclear);
				str = Str.STR_01A5_COST_TO_CLEAR;
			}
			Gfx.DrawStringCentered(140, 38, str, 0);

			Strings._userstring = new BinaryString( String.format("0x%X", lid.tile.getTile()) );
			Global.SetDParam(0, lid.tile.TileX());
			Global.SetDParam(1, lid.tile.TileY());
			Global.SetDParam(2, Strings.STR_SPEC_USERSTRING);
			Gfx.DrawStringCentered(140, 49, Str.STR_LANDINFO_COORDS, 0);

			Global.SetDParam(0, Str.STR_01A9_NONE);
			if (lid.town != null) {
				Global.SetDParam(0, Str.STR_TOWN);
				Global.SetDParam(1, lid.town.index);
			}
			Gfx.DrawStringCentered(140,60, Str.STR_01A8_LOCAL_AUTHORITY, 0);

			{
				//char buf[512];
				//String p = Global.GetString(Str.STR_01CE_CARGO_ACCEPTED);
				//int pi = 0;
				boolean found = false;
				StringBuilder sb = new StringBuilder();
				sb.append(Strings.GetString(Str.STR_01CE_CARGO_ACCEPTED));

				for (i = 0; i < AcceptedCargo.NUM_CARGO; ++i) {
					if (lid.ac.ct[i] > 0) {
						// Add a comma between each item.
						if (found) {
							//*p++ = ',';
							//*p++ = ' ';
							sb.append(", ");
						}
						found = true;

						// If the accepted value is less than 8, show it in 1/8:ths
						if (lid.ac.ct[i] < 8) {
							Integer [] argv = new Integer[2];
							argv[0] = lid.ac.ct[i];
							argv[1] = Global._cargoc.names_s[i];
							//p = Global.GetStringWithArgs(p, Str.STR_01D1_8, argv);
							sb.append(Strings.GetStringWithArgs( Str.STR_01D1_8, (Object[])argv));
						} else {
							//p = Global.GetString(p, Global._cargoc.names_s[i]);
							sb.append(Strings.GetString( Global._cargoc.names_s[i]));
						}
					}
				}

				if (found) Gfx.DrawStringMultiCenter(140, 76, Strings.BindCString(sb.toString()), 276);
			}

			if (lid.td.build_date != 0) {
				Global.SetDParam(0,lid.td.build_date);
				Gfx.DrawStringCentered(140,71, Str.STR_BUILD_DATE, 0);
			}
		}
	}

	static final Widget _land_info_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,	Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   279,     0,    13, Str.STR_01A3_LAND_AREA_INFORMATION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   279,    14,    92, 0x0,				Str.STR_NULL),
	};

	static final WindowDesc _land_info_desc = new WindowDesc(
			-1, -1, 280, 93,
			Window.WC_LAND_INFO,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_land_info_widgets,
			MiscGui::LandInfoWndProc
			);

	static void Place_LandInfo(TileIndex tile)
	{
		Player p;
		LandInfoData lid = new LandInfoData();
		Window w;
		long old_money;

		Window.DeleteWindowById(Window.WC_LAND_INFO, 0);

		w = Window.AllocateWindowDesc(_land_info_desc);
		w.as_void_d().data = lid;

		lid.tile = tile;
		lid.town = Town.ClosestTownFromTile(tile, Global._patches.dist_local_authority);

		p = Player.GetPlayer(Global.gs._local_player.id < Global.MAX_PLAYERS ? Global.gs._local_player.id : 0);

		old_money = p.getMoney();
		p.setMoney( 0x7fffffff );
		lid.costclear = Cmd.DoCommandByTile(tile, 0, 0, 0, Cmd.CMD_LANDSCAPE_CLEAR);
		p.setMoney( old_money );
		//p.UpdatePlayerMoney32();

		// Becuase build_date is not set yet in every TileDesc, we make sure it is empty
		lid.td.build_date = 0;

		lid.ac = Landscape.GetAcceptedCargo(tile);
		lid.td = Landscape.GetTileDesc(tile);
		
		IntContainer ic  = new IntContainer();
		int sl = tile.GetTileSlope(ic);

		//#if defined(_DEBUG)
		Global.DEBUG_misc( 0, "TILE: %x (%d,%d)", tile.hashCode(), tile.TileX(), tile.TileY());
		//Global.DEBUG_misc( 0, "TILE: %x ", tile.hashCode());
		Global.DEBUG_misc( 0, "type         = %x", tile.getMap().type);
		Global.DEBUG_misc( 0, "height       = %d", tile.getMap().height);
		Global.DEBUG_misc( 0, "m1           = %x", tile.getMap().m1);
		Global.DEBUG_misc( 0, "m2           = %x", tile.getMap().m2);
		Global.DEBUG_misc( 0, "m3           = %x", tile.getMap().m3);
		Global.DEBUG_misc( 0, "m4           = %x", tile.getMap().m4);
		Global.DEBUG_misc( 0, "m5           = %x", tile.getMap().m5);
		Global.DEBUG_misc( 0, "extra        = %x", tile.getMap().extra);
		Global.DEBUG_misc( 0, "slope/h      = %d/%d", sl, ic.v);
		//#endif
	}

	static void PlaceLandBlockInfo()
	{
		if (Hal._cursor.sprite.id == Sprite.SPR_CURSOR_QUERY) {
			ViewPort.ResetObjectToPlace();
		} else {
			Global._place_proc = MiscGui::Place_LandInfo;
			ViewPort.SetObjectToPlace(Sprite.SPR_CURSOR_QUERY, 1, 1, 0);
		}
	}

	static final String credits[] = {
			/*************************************************************************
			 *                      maximum length of string which fits in window   -^*/
			"Original design by Chris Sawyer",
			"Original graphics by Simon Foster",
			"",
			"The OpenTTD team (in alphabetical order):",
			"  Bjarni Corfitzen (Bjarni) - MacOSX port, coder",
			"  Matthijs Kooijman (blathijs) - Pathfinder-god",
			"  Victor Fischer (Celestar) - Programming everywhere you need him to",
			"  Tam.s Farag. (Darkvater) - Lead coder",
			"  Kerekes Miham (MiHaMiX) - Translator system, and Nightlies host",
			"  Owen Rudge (orudge) - Forum- and masterserver host, OS/2 port",
			"  Peter Nelson (peter1138) - Spiritual descendant from newgrf gods",
			"  Christoph Mallon (Tron) - Programmer, code correctness police",
			"  Patric Stout (TrueLight) - Coder, network guru, SVN- and website host",
			"",
			"Retired Developers:",
			"  Ludvig Strigeus (ludde) - OpenTTD author, main coder (0.1 - 0.3.3)",
			"  Serge Paquet (vurlix) - Assistant project manager, coder (0.1 - 0.3.3)",
			"  Dominik Scherer (dominik81) - Lead programmer, GUI expert (0.3.0 - 0.3.6)",
			"",
			"Special thanks go out to:",
			"  Josef Drexler - For his great work on TTDPatch",
			"  Marcin Grzegorczyk - For his documentation of TTD internals",
			"  Petr Baudis (pasky) - Many patches, newgrf support",
			"  Stefan Mei.ner (sign_de) - For his work on the console",
			"  Simon Sasburg (HackyKid) - Many bugfixes he has blessed us with (and PBS)",
			"  Cian Duffy (MYOB) - BeOS port / manual writing",
			"  Christian Rosentreter (tokaiz) - MorphOS / AmigaOS port",
			"",
			"  Michael Blunck - Pre-Signals and Semaphores  2003",
			"  George - Canal/Lock graphics  2003-2004",
			"  Marcin Grzegorczyk - Foundations for Tracks on Slopes",
			"  All Translators - Who made OpenTTD a truly international game",
			"  Bug Reporters - Without whom OpenTTD would still be full of bugs!",
			"",
			"  NextTTD Java port by Dmitry Zavalishin, 2021",
			"",
			"And last but not least:",
			"  Chris Sawyer - For an amazing game!"
	};

	static void AboutWindowProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_CREATE: /* Set up window counter and start position of scroller */
			w.as_scroller_d().counter = 0;
			w.as_scroller_d().height = w.height - 40;
			break;
		case WE_PAINT: {
			int i;
			int y = w.as_scroller_d().height;
			w.DrawWindowWidgets();

			// Show original copyright and revision version
			Gfx.DrawStringCentered(210, 17, Str.STR_00B6_ORIGINAL_COPYRIGHT, 0);
			Gfx.DrawStringCentered(210, 17 + 10, Str.STR_00B7_VERSION, 0);

			// Show all scrolling credits
			for (i = 0; i < credits.length; i++) {
				if (y >= 50 && y < (w.height - 40)) {
					Gfx.DoDrawString(credits[i], 10, y, 0x10);
				}
				y += 10;
			}

			// If the last text has scrolled start anew from the start
			if (y < 50) w.as_scroller_d().height = w.height - 40;

			Gfx.DoDrawStringCentered(210, w.height - 25, "Website: http://www.openttd.org", 16);
			Gfx.DrawStringCentered(210, w.height - 15, Str.STR_00BA_COPYRIGHT_OPENTTD, 0);
		}	break;
		case WE_MOUSELOOP: /* Timer to scroll the text and adjust the new top */
			if (w.as_scroller_d().counter++ % 3 == 0) {
				w.as_scroller_d().height--;
				w.SetWindowDirty();
			}
			break;
		default:
			break;
		}
	}

	static final Widget _about_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,					Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   419,     0,    13, Str.STR_015B_OPENTTD,	Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   419,    14,   271, 0x0,								Str.STR_NULL),
			new Widget(      Window.WWT_FRAME,   Window.RESIZE_NONE,    14,     5,   414,    40,   245, Str.STR_NULL,					Str.STR_NULL),
			//{    WIDGETS_END},
	};

	static final WindowDesc _about_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 420, 272,
			Window.WC_GAME_OPTIONS,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_about_widgets,
			MiscGui::AboutWindowProc
			);


	static void ShowAboutWindow()
	{
		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		Window.AllocateWindowDesc(_about_desc);
	}

	static int _tree_to_plant;

	static final int _tree_sprites[] = {
			0x655,0x663,0x678,0x62B,0x647,0x639,0x64E,0x632,0x67F,0x68D,0x69B,0x6A9,
			0x6AF,0x6D2,0x6D9,0x6C4,0x6CB,0x6B6,0x6BD,0x6E0,
			0x72E,0x734,0x74A,0x74F,0x76B,0x78F,0x788,0x77B,0x75F,0x774,0x720,0x797,
			0x79E,0x7A5 | Sprite.PALETTE_TO_GREEN,0x7AC | Sprite.PALETTE_TO_RED,0x7B3,0x7BA,0x7C1 | Sprite.PALETTE_TO_RED,0x7C8 | Sprite.PALETTE_TO_PALE_GREEN,0x7CF | Sprite.PALETTE_TO_YELLOW,0x7D6 | Sprite.PALETTE_TO_RED
	};

	static void BuildTreesWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int x,y;
			int i, count;

			w.DrawWindowWidgets();

			w.as_tree_d().base = i = Tree._tree_base_by_landscape[GameOptions._opt.landscape];
			w.as_tree_d().count = count = Tree._tree_count_by_landscape[GameOptions._opt.landscape];

			x = 18;
			y = 54;
			do {
				Gfx.DrawSprite(_tree_sprites[i], x, y);
				x += 35;
				if (0==(++i & 3)) {
					x -= 35 * 4;
					y += 47;
				}
			} while (--count > 0);
		} break;

		case WE_CLICK: {
			int wid = e.widget;

			switch (wid) {
			case 0:
				ViewPort.ResetObjectToPlace();
				break;

			case 3: case 4: case 5: case 6:
			case 7: case 8: case 9: case 10:
			case 11:case 12: case 13: case 14:
				if (wid - 3 >= w.as_tree_d().count) break;

				if (Gui.HandlePlacePushButton(w, wid, Sprite.SPR_CURSOR_TREE, 1, null))
					_tree_to_plant = w.as_tree_d().base + wid - 3;
				break;

			case 15: // tree of random type.
				if (Gui.HandlePlacePushButton(w, 15, Sprite.SPR_CURSOR_TREE, 1, null))
					_tree_to_plant = -1;
				break;

			case 16: /* place trees randomly over the landscape*/
				w.click_state |= 1 << 16;
				w.flags4 |= 5 << Window.WF_TIMEOUT_SHL;
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				Tree.PlaceTreesRandomly();
				Hal.MarkWholeScreenDirty();
				break;
			}
		} break;

		case WE_PLACE_OBJ:
			ViewPort.VpStartPlaceSizing(e.tile, ViewPort.VPM_X_AND_Y_LIMITED);
			ViewPort.VpSetPlaceSizingLimit(20);
			break;

		case WE_PLACE_DRAG:
			ViewPort.VpSelectTilesWithMethod(e.pt.x, e.pt.y, e.userdata);
			return;

		case WE_PLACE_MOUSEUP:
			if (e.pt.x != -1) {
				Cmd.DoCommandP(e.tile, _tree_to_plant, e.starttile.getTile(), null,
						Cmd.CMD_PLANT_TREE | Cmd.CMD_AUTO | Cmd.CMD_MSG(Str.STR_2805_CAN_T_PLANT_TREE_HERE));
			}
			break;

		case WE_TIMEOUT:
			w.UnclickSomeWindowButtons(1<<16);
			break;

		case WE_ABORT_PLACE_OBJ:
			w.click_state = 0;
			w.SetWindowDirty();
			break;
		default:
			break;
		}
	}

	static final Widget _build_trees_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,				Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   142,     0,    13, Str.STR_2802_TREES,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   142,    14,   170, 0x0,							Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     2,    35,    16,    61, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    37,    70,    16,    61, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    72,   105,    16,    61, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   107,   140,    16,    61, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     2,    35,    63,   108, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    37,    70,    63,   108, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    72,   105,    63,   108, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   107,   140,    63,   108, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     2,    35,   110,   155, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    37,    70,   110,   155, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    72,   105,   110,   155, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   107,   140,   110,   155, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   2,   140,   157,   168, Str.STR_TREES_RANDOM_TYPE, Str.STR_TREES_RANDOM_TYPE_TIP),
	};

	static final WindowDesc _build_trees_desc = new WindowDesc(
			497, 22, 143, 171,
			Window.WC_BUILD_TREES, Window.WC_SCEN_LAND_GEN,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_trees_widgets,
			MiscGui::BuildTreesWndProc
			);

	static final Widget _build_trees_scen_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,				Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   142,     0,    13, Str.STR_2802_TREES,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     7,     0,   142,    14,   183, 0x0,							Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     2,    35,    16,    61, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    37,    70,    16,    61, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    72,   105,    16,    61, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   107,   140,    16,    61, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     2,    35,    63,   108, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    37,    70,    63,   108, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    72,   105,    63,   108, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   107,   140,    63,   108, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     2,    35,   110,   155, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    37,    70,   110,   155, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,    72,   105,   110,   155, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,   107,   140,   110,   155, 0x0,							Str.STR_280D_SELECT_TREE_TYPE_TO_PLANT),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   140,   157,   168, Str.STR_TREES_RANDOM_TYPE,	Str.STR_TREES_RANDOM_TYPE_TIP),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     2,   140,   170,   181, Str.STR_028A_RANDOM_TREES,	Str.STR_028B_PLANT_TREES_RANDOMLY_OVER),
	};

	static final WindowDesc _build_trees_scen_desc = new WindowDesc(
			-1, -1, 143, 184,
			Window.WC_BUILD_TREES,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_build_trees_scen_widgets,
			MiscGui::BuildTreesWndProc
			);


	public static void ShowBuildTreesToolbar()
	{
		Window.AllocateWindowDescFront(_build_trees_desc, 0);
	}

	static void ShowBuildTreesScenToolbar()
	{
		Window.AllocateWindowDescFront(_build_trees_scen_desc, 0);
	}

	static final Integer [] _errmsg_decode_params = new Integer[20];
	static StringID _errmsg_message_1, _errmsg_message_2;
	static int _errmsg_duration = 0;


	static final Widget _errmsg_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     4,     0,    10,     0,    13, Str.STR_00C5,					Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     4,    11,   239,     0,    13, Str.STR_00B2_MESSAGE,	Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     4,     0,   239,    14,    45, 0x0,								Str.STR_NULL),
	};

	static final Widget _errmsg_face_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     4,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     4,    11,   333,     0,    13, Str.STR_00B3_MESSAGE_FROM,	Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,     4,     0,   333,    14,   136, 0x0,										Str.STR_NULL),
	};

	static void ErrmsgWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			Global.COPY_IN_DPARAM(0, _errmsg_decode_params, _errmsg_decode_params.length);
			w.DrawWindowWidgets();
			Global.COPY_IN_DPARAM(0, _errmsg_decode_params, _errmsg_decode_params.length);
			if (!Window.IsWindowOfPrototype(w, _errmsg_face_widgets)) {
				Gfx.DrawStringMultiCenter(
						120,
						(_errmsg_message_1.isValid() ? 15 : 25),
						_errmsg_message_2.id,
						238);
				if (_errmsg_message_1.isValid())
					Gfx.DrawStringMultiCenter(
							120,
							30,
							_errmsg_message_1.id,
							238);
			} else {
				final Player p = Player.GetPlayer(Global.GetDParamX(_errmsg_decode_params,2));
				p.DrawPlayerFace();

				Gfx.DrawStringMultiCenter(
						214,
						(_errmsg_message_1.isValid() ? 45 : 65),
						_errmsg_message_2.id,
						238);
				if (_errmsg_message_1.isValid())
					Gfx.DrawStringMultiCenter(
							214,
							90,
							_errmsg_message_1.id,
							238);
			}
			break;

		case WE_MOUSELOOP:
			if (Window._right_button_down) w.DeleteWindow();
			break;

		case WE_4:
			if (--_errmsg_duration == 0) w.DeleteWindow();
			break;

		case WE_DESTROY:
			ViewPort.SetRedErrorSquare(null);
			Global._switch_mode_errorstr = StringID.getInvalid();
			break;

		case WE_KEYPRESS:
			if (e.keycode == Window.WKC_SPACE) {
				// Don't continue.
				e.cont = false;
				w.DeleteWindow();
			}
			break;
		default:
			break;
		}
	}

	public static void ShowErrorMessage(StringID msg_1, StringID msg_2, int x, int y)
	{
		Window w;
		ViewPort vp = null;
		Point pt = new Point(0, 0);

		Window.DeleteWindowById(Window.WC_ERRMSG, 0);

		//assert(msg_2);
		if (msg_2.id == 0) msg_2 = new StringID( Str.STR_EMPTY );

		_errmsg_message_1 = msg_1;
		_errmsg_message_2 = msg_2;
		Global.COPY_OUT_DPARAM(_errmsg_decode_params, 0, _errmsg_decode_params.length);
		_errmsg_duration = Global._patches.errmsg_duration.get();
		if (0== _errmsg_duration)
			return;

		if (_errmsg_message_1.id != Str.STR_013B_OWNED_BY || Global.GetDParamX(_errmsg_decode_params,2) >= 8) {

			if ( (x|y) != 0) {
				pt = Point.RemapCoords2(x, y);

				//for(w=Window._windows; w.window_class != Window.WC_MAIN_WINDOW; w++) {}
				/*
				for(Window w : Window._windows )
				{
					if(w.window_class.v == Window.WC_MAIN_WINDOW)
					{
						vp = w.viewport;
						break;
					}
				}
				 */
				vp = Window.getMain().getViewport();

				assert vp != null;

				// move x pos to opposite corner
				pt.x = ((pt.x - vp.virtual_left) >> vp.zoom) + vp.left;
				pt.x = (pt.x < (Hal._screen.width >> 1)) ? Hal._screen.width - 260 : 20;

				// move y pos to opposite corner
				pt.y = ((pt.y - vp.virtual_top) >> vp.zoom) + vp.top;
				pt.y = (pt.y < (Hal._screen.height >> 1)) ? Hal._screen.height - 80 : 100;

			} else {
				pt.x = (Hal._screen.width - 240) >> 1;
						pt.y = (Hal._screen.height - 46) >> 1;
			}
			w = Window.AllocateWindow(pt.x, pt.y, 240, 46, MiscGui::ErrmsgWndProc, Window.WC_ERRMSG, _errmsg_widgets);
		} else {
			if ( (x|y) != 0) {
				pt = Point.RemapCoords2(x, y);
				//for(w=_windows; w.window_class != Window.WC_MAIN_WINDOW; w++) {}
				//vp = w.viewport;
				vp = Window.getMain().getViewport();

				pt.x = BitOps.clamp(((pt.x - vp.virtual_left) >> vp.zoom) + vp.left - (334/2), 0, Hal._screen.width - 334);
				pt.y = BitOps.clamp(((pt.y - vp.virtual_top) >> vp.zoom) + vp.top - (137/2), 22, Hal._screen.height - 137);
			} else {
				pt.x = (Hal._screen.width - 334) >> 1;
				pt.y = (Hal._screen.height - 137) >> 1;
			}
			w = Window.AllocateWindow(pt.x, pt.y, 334, 137, MiscGui::ErrmsgWndProc, Window.WC_ERRMSG, _errmsg_face_widgets);
		}

		w.desc_flags = WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET;
	}


	public static void ShowEstimatedCostOrIncome(int cost, int x, int y)
	{
		//StringID 
		int msg = Str.STR_0805_ESTIMATED_COST;

		if (cost < 0) {
			cost = -cost;
			msg = Str.STR_0807_ESTIMATED_INCOME;
		}
		Global.SetDParam(0, cost);
		ShowErrorMessage(Str.INVALID_STRING_ID(), new StringID(msg), x, y);
	}

	public static void ShowCostOrIncomeAnimation(int x, int y, int z, int cost)
	{
		//StringID 
		int msg;
		Point pt = Point.RemapCoords(x,y,z);

		msg = Str.STR_0801_COST;
		if (cost < 0) {
			cost = -cost;
			msg = Str.STR_0803_INCOME;
		}
		Global.SetDParam(0, cost);
		Global.SetDParam(4, 0); // [dz] code below wants it
		TextEffect.AddTextEffect(msg, pt.x, pt.y, 0x250);
	}

	public static void ShowFeederIncomeAnimation(int x, int y, int z, int cost)
	{
		Point pt = Point.RemapCoords(x,y,z);

		Global.SetDParam(0, cost);
		TextEffect.AddTextEffect(Str.STR_FEEDER, pt.x, pt.y, 0x250);
	}

	static final Widget _tooltips_widgets[] = {
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   199,     0,    31, 0x0, Str.STR_NULL),
	};


	static void TooltipsWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT:
			Gfx.GfxFillRect(0, 0, w.width - 1, w.height - 1, 0);
			Gfx.GfxFillRect(1, 1, w.width - 2, w.height - 2, 0x44);
			Gfx.DrawStringMultiCenter((w.width >> 1), (w.height >> 1) - 5, w.as_tooltips_d().string_id, 197);
			break;

		case WE_MOUSELOOP:
			if (!Window._right_button_down) w.DeleteWindow();
			break;
		default:
			break;
		}
	}

	static void GuiShowTooltips(/*StringID*/ int string_id)
	{
		Window w;
		int right,bottom;
		int x,y;

		if (string_id == 0) return;

		w = Window.FindWindowById(Window.WC_TOOLTIPS, 0);
		if (w != null) {
			if (w.as_tooltips_d().string_id == string_id)
				return;
			w.DeleteWindow();
		}

		String buffer = Strings.GetString(string_id);
		right = Gfx.GetStringWidth(buffer) + 4;

		bottom = 14;
		if (right > 200) {
			bottom += ((right - 4) / 176) * 10;
			right = 200;
		}

		y = Hal._cursor.pos.y + 30;
		if (y < 22) y = 22;

		if (y > (Hal._screen.height - 44) && (y-=52) > (Hal._screen.height - 44))
			y = (Hal._screen.height - 44);

		x = Hal._cursor.pos.x - (right >> 1);
		if (x < 0) x = 0;
		if (x > (Hal._screen.width - right)) x = Hal._screen.width - right;

		w = Window.AllocateWindow(x, y, right, bottom, MiscGui::TooltipsWndProc, Window.WC_TOOLTIPS, _tooltips_widgets);
		w.as_tooltips_d().string_id = string_id;
		w.flags4 &= ~Window.WF_WHITE_BORDER_MASK;
		w.widget.get(0).right = right;
		w.widget.get(0).bottom = bottom;
	}


	static void DrawStationCoverageText(final AcceptedCargo accepts,
			int str_x, int str_y, int mask)
	{
		//char *b = Strings._userstring;
		BinaryString sb = new BinaryString();
		int i;

		//b = InlineString(b, Str.STR_000D_ACCEPTS);
		sb.appendInlineString( Str.STR_000D_ACCEPTS );

		for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
			if (accepts.ct[i] >= 8 && 0 != (mask & 1) ) {
				//b = InlineString(b, Global._cargoc.names_s[i]);
				sb.appendInlineString( Global._cargoc.names_s[i] );
				//*b++ = ',';
				//*b++ = ' ';
				sb.append( ", " );
			}
			mask >>= 1;
		}

		/*
		if (b == Strings._userstring[3]) {
			b = InlineString(b, Str.STR_00D0_NOTHING);
		 *b++ = '\0';
		} else {
			b[-2] = '\0';
		}*/

		String curr = sb.toString();

		if( curr.length() == 3)
		{
			sb.appendInlineString(Str.STR_00D0_NOTHING);
			Strings._userstring = new BinaryString( sb.toString() );
		}
		else
			Strings._userstring = new BinaryString( curr.substring(0, curr.length()-2) );

		Gfx.DrawStringMultiLine(str_x, str_y, new StringID( Strings.STR_SPEC_USERSTRING ), 144);
	}

	public static void DrawStationCoverageAreaText(int sx, int sy, int mask, int rad) {
		int x = ViewPort._thd.pos.x;
		int y = ViewPort._thd.pos.y;
		AcceptedCargo accepts = new AcceptedCargo();
		if (x != -1) {
			Station.GetAcceptanceAroundTiles(accepts, TileIndex.TileVirtXY(x, y), ViewPort._thd.size.x / 16, ViewPort._thd.size.y / 16 , rad);
			DrawStationCoverageText(accepts, sx, sy, mask);
		}
	}

	public static void CheckRedrawStationCoverage(final Window  w)
	{
		if(0 != (ViewPort._thd.dirty & 1)) {
			ViewPort._thd.dirty &= ~1;
			w.SetWindowDirty();
		}
	}




	private static void QueryStringWndProc_press_ok(Window w, boolean [] closed)
	{
		if (w.as_querystr_d().orig != null &&
				(w.as_querystr_d().text.getString().equals(w.as_querystr_d().orig)) ) 
		{
			w.DeleteWindow();
		} else {
			char[] buf = w.as_querystr_d().text.getBuf();
			int wnd_class = w.as_querystr_d().wnd_class;
			int wnd_num = w.as_querystr_d().wnd_num;
			Window parent;

			// Mask the edit-box as closed, so we don't send out a CANCEL
			closed[0] = true;

			w.DeleteWindow();

			parent = Window.FindWindowById(wnd_class, wnd_num);
			if (parent != null) {
				WindowEvent e = new WindowEvent();
				e.event = WindowEvents.WE_ON_EDIT_TEXT;
				e.str = String.valueOf( buf );
				parent.wndproc.accept(parent, e);
			}
		}
	}

	static void QueryStringWndProc(Window w, WindowEvent e)
	{
		boolean [] closed = { false };
		switch (e.event) {
		case WE_CREATE:
			Global._no_scroll = BitOps.RETSETBIT(Global._no_scroll, Global.SCROLL_EDIT);
			closed[0] = false;
			break;

		case WE_PAINT:
			Global.SetDParam(0, w.as_querystr_d().caption.id);
			w.DrawWindowWidgets();

			Textbuf.DrawEditBox(w, 5);
			break;

		case WE_CLICK:
			switch(e.widget) {
			case 3: w.DeleteWindow(); break;
			case 4:
				//press_ok:;
				QueryStringWndProc_press_ok(w, closed);
				break;
			}
			break;

		case WE_MOUSELOOP: {
			if (null == Window.FindWindowById(w.as_querystr_d().wnd_class, w.as_querystr_d().wnd_num)) {
				w.DeleteWindow();
				return;
			}
			Textbuf.HandleEditBox(w, 5);
		} break;

		case WE_KEYPRESS: {
			switch (Textbuf.HandleEditBoxKey(w, 5, e)) {
			case 1: // Return
				//goto press_ok;
				QueryStringWndProc_press_ok(w, closed);
				break;
			case 2: // Escape
				w.DeleteWindow();
				break;
			}
		} break;

		case WE_DESTROY:
			// If the window is not closed yet, it means it still needs to send a CANCEL
			if (!closed[0]) {
				Window parent = Window.FindWindowById(w.as_querystr_d().wnd_class, w.as_querystr_d().wnd_num);
				if (parent != null) {
					WindowEvent we = new WindowEvent();
					we.event = WindowEvents.WE_ON_EDIT_TEXT_CANCEL;
					parent.wndproc.accept(parent, we);
				}
			}
			Global._no_scroll = BitOps.RETCLRBIT(Global._no_scroll, Global.SCROLL_EDIT);
			break;
		default:
			break;
		}
	}

	static final Widget _query_string_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,				Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   259,     0,    13, Str.STR_012D,				Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   259,    14,    29, 0x0,							Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,     0,   129,    30,    41, Str.STR_012E_CANCEL,	Str.STR_NULL),
			new Widget(    Window.WWT_TEXTBTN,   Window.RESIZE_NONE,    14,   130,   259,    30,    41, Str.STR_012F_OK,			Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   257,    16,    27, 0x0,							Str.STR_NULL),
	};

	static final WindowDesc _query_string_desc = new WindowDesc(
			190, 219, 260, 42,
			Window.WC_QUERY_STRING,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_query_string_widgets,
			MiscGui::QueryStringWndProc
			);

	//static char _edit_str_buf[64];
	//static char _orig_str_buf[lengthof(_edit_str_buf)];
	private static String _edit_str_buf = "";
	//static char _orig_str_buf[lengthof(_edit_str_buf)];
	private static String _orig_str_buf = "";

	public static void ShowQueryString(int str, int caption, int maxlen, int maxwidth, int window_class, int window_number)
	{
		ShowQueryString( new StringID(str), new StringID(caption), maxlen, maxwidth, window_class, window_number );
	}	

	
	/*static void ShowQueryString(StringID str, StringID caption, int maxlen, int maxwidth, int window_class, int window_number)
	{
		ShowQueryString( str, caption, maxlen, maxwidth, new WindowClass( window_class ), new WindowNumber(window_number) );
	}*/	

	public static void ShowQueryString(StringID str, StringID caption, int maxlen, int maxwidth, int window_class, int window_number)
	{
		Window w;
		int realmaxlen = maxlen & ~0x1000;

		//assert(realmaxlen < lengthof(_edit_str_buf));

		Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
		Window.DeleteWindowById(Window.WC_SAVELOAD, 0);

		w = Window.AllocateWindowDesc(_query_string_desc);

		_edit_str_buf = Strings.GetString(str);
		//_edit_str_buf[realmaxlen] = '\0';

		if(0 != (maxlen & 0x1000)) {
			w.as_querystr_d().orig = null;
		} else {
			_orig_str_buf = _edit_str_buf;
			w.as_querystr_d().orig = _orig_str_buf;
		}

		w.click_state = 1 << 5;
		w.as_querystr_d().caption = caption;
		w.as_querystr_d().wnd_class = window_class;
		w.as_querystr_d().wnd_num = window_number;
		w.as_querystr_d().text.setCaret(false);
		w.as_querystr_d().text.maxlength = realmaxlen - 1;
		w.as_querystr_d().text.maxwidth = maxwidth;
		w.as_querystr_d().text.setText(_edit_str_buf);
		w.as_querystr_d().text.UpdateTextBufferSize();
	}

	static final Widget _load_dialog_1_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,						Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   256,     0,    13, Str.STR_4001_LOAD_GAME,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,   127,    14,    25, Str.STR_SORT_BY_NAME,		Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   128,   256,    14,    25, Str.STR_SORT_BY_DATE,		Str.STR_SORT_ORDER_TIP),
			new Widget(     Window.WWT_IMGBTN,  Window.RESIZE_RIGHT,    14,     0,   256,    26,    47, 0x0,								Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_RB,    14,     0,   256,    48,   293, 0x0,								Str.STR_NULL),
			new Widget( Window.WWT_PUSHIMGBTN,     Window.RESIZE_LR,    14,   245,   256,    48,    59, Sprite.SPR_HOUSE_ICON,			Str.STR_SAVELOAD_HOME_BUTTON),
			new Widget(          Window.WWT_6,     Window.RESIZE_RB,    14,     2,   243,    50,   291, 0x0,								Str.STR_400A_LIST_OF_DRIVES_DIRECTORIES),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   245,   256,    60,   281, 0x0,								Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   245,   256,   282,   293, 0x0,								Str.STR_RESIZE_BUTTON),
	};

	static final Widget _load_dialog_2_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   256,     0,    13, Str.STR_0298_LOAD_SCENARIO,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,   127,    14,    25, Str.STR_SORT_BY_NAME,				Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   128,   256,    14,    25, Str.STR_SORT_BY_DATE,				Str.STR_SORT_ORDER_TIP),
			new Widget(     Window.WWT_IMGBTN,  Window.RESIZE_RIGHT,    14,     0,   256,    26,    47, 0x0,										Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_RB,    14,     0,   256,    48,   293, 0x0,										Str.STR_NULL),
			new Widget( Window.WWT_PUSHIMGBTN,     Window.RESIZE_LR,    14,   245,   256,    48,    59, Sprite.SPR_HOUSE_ICON,					Str.STR_SAVELOAD_HOME_BUTTON),
			new Widget(          Window.WWT_6,     Window.RESIZE_RB,    14,     2,   243,    50,   291, 0x0,										Str.STR_400A_LIST_OF_DRIVES_DIRECTORIES),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   245,   256,    60,   281, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   245,   256,   282,   293, 0x0,										Str.STR_RESIZE_BUTTON),
	};

	static final Widget _save_dialog_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,						Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   256,     0,    13, Str.STR_4000_SAVE_GAME,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,   127,    14,    25, Str.STR_SORT_BY_NAME,		Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   128,   256,    14,    25, Str.STR_SORT_BY_DATE,		Str.STR_SORT_ORDER_TIP),
			new Widget(     Window.WWT_IMGBTN,  Window.RESIZE_RIGHT,    14,     0,   256,    26,    47, 0x0,								Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_RB,    14,     0,   256,    48,   291, 0x0,								Str.STR_NULL),
			new Widget( Window.WWT_PUSHIMGBTN,     Window.RESIZE_LR,    14,   245,   256,    48,    59, Sprite.SPR_HOUSE_ICON,			Str.STR_SAVELOAD_HOME_BUTTON),
			new Widget(          Window.WWT_6,     Window.RESIZE_RB,    14,     2,   243,    50,   290, 0x0,								Str.STR_400A_LIST_OF_DRIVES_DIRECTORIES),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   245,   256,    60,   291, 0x0,								Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_IMGBTN,    Window.RESIZE_RTB,    14,     0,   256,   292,   307, 0x0,								Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,    Window.RESIZE_RTB,    14,     2,   254,   294,   305, 0x0,								Str.STR_400B_CURRENTLY_SELECTED_NAME),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   127,   308,   319, Str.STR_4003_DELETE,		Str.STR_400C_DELETE_THE_CURRENTLY_SELECTED),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   128,   244,   308,   319, Str.STR_4002_SAVE,			Str.STR_400D_SAVE_THE_CURRENT_GAME_USING),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   245,   256,   308,   319, 0x0,								Str.STR_RESIZE_BUTTON),
	};

	static final Widget _save_dialog_scen_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   256,     0,    13, Str.STR_0299_SAVE_SCENARIO, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,     0,   127,    14,    25, Str.STR_SORT_BY_NAME,				Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   128,   256,    14,    25, Str.STR_SORT_BY_DATE,				Str.STR_SORT_ORDER_TIP),
			new Widget(     Window.WWT_IMGBTN,  Window.RESIZE_RIGHT,    14,     0,   256,    26,    47, 0x0,										Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_RB,    14,     0,   256,    48,   291, 0x0,										Str.STR_NULL),
			new Widget( Window.WWT_PUSHIMGBTN,     Window.RESIZE_LR,    14,   245,   256,    48,    59, Sprite.SPR_HOUSE_ICON,					Str.STR_SAVELOAD_HOME_BUTTON),
			new Widget(          Window.WWT_6,     Window.RESIZE_RB,    14,     2,   243,    50,   290, 0x0,										Str.STR_400A_LIST_OF_DRIVES_DIRECTORIES),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   245,   256,    60,   291, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_IMGBTN,    Window.RESIZE_RTB,    14,     0,   256,   292,   307, 0x0,										Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,    Window.RESIZE_RTB,    14,     2,   254,   294,   305, 0x0,										Str.STR_400B_CURRENTLY_SELECTED_NAME),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,     0,   127,   308,   319, Str.STR_4003_DELETE,				Str.STR_400C_DELETE_THE_CURRENTLY_SELECTED),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   128,   244,   308,   319, Str.STR_4002_SAVE,					Str.STR_400D_SAVE_THE_CURRENT_GAME_USING),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   245,   256,   308,   319, 0x0,										Str.STR_RESIZE_BUTTON),
	};


	// Colors for fios types
	/*	FIOS_TYPE_FILE, FIOS_TYPE_OLDFILE etc. different colours */
	public final static byte _fios_colors[] = {13, 9, 9, 6, 5, 6, 5};
	public static List<FiosItem> _fios_list;
	private static FiosItem o_dir = new FiosItem();

	public static void BuildFileList()
	{
		
		_fios_path_changed = true;

		switch (Global._saveload_mode) {
		case Global.SLD_NEW_GAME:
		case Global.SLD_LOAD_SCENARIO:
		case Global.SLD_SAVE_SCENARIO:
			_fios_list = FileIO.FiosGetScenarioList(Global._saveload_mode);
			break;

		default:
			_fios_list = FileIO.FiosGetSavegameList(Global._saveload_mode);
			break;
		}
		
	}
	
	static void DrawFiosTexts(int maxw)
	{
		final String [] path = { "" };
		int str = Str.STR_4006_UNABLE_TO_READ_DRIVE;
		long [] tot = { 0 };

		if (_fios_path_changed) {
			str = FileIO.FiosGetDescText(path, tot);
			_fios_path_changed = false;
		}

		if (str != Str.STR_4006_UNABLE_TO_READ_DRIVE) 
			Global.SetDParam(0, tot[0] >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(tot[0]) );
		Gfx.DrawString(2, 37, str, 0);
		Gfx.DoDrawStringTruncated(path[0], 2, 27, 16, maxw);
	}
	

	static void MakeSortedSaveGameList()
	{
		/* TODO MakeSortedSaveGameList()
		int sort_start = 0;
		int sort_end = 0;
		int s_amount;
		int i;

		// *	Directories are always above the files (FiosType.DIR)
		// *	Drives (A:\ (windows only) are always under the files (FiosType.DRIVE)
		// *	Only sort savegames/scenarios, not directories
		// *
		for (i = 0; i < _fios_num; i++) {
			switch (_fios_list[i].type) {
			case FiosType.DIR:    sort_start++; break;
			case FiosType.PARENT: sort_start++; break;
			case FiosType.DRIVE:  sort_end++;   break;
			}
		}

		s_amount = _fios_num - sort_start - sort_end;
		if (s_amount > 0)
			qsort(_fios_list + sort_start, s_amount, sizeof(FiosItem), compare_FiosItems);
		 */
	}

	static void GenerateFileName()
	{
		/* Check if we are not a spectator who wants to generate a name..
		    Let's use the name of player #0 for now. */
		final Player  p = Player.GetPlayer(Global.gs._local_player.id < Global.MAX_PLAYERS ? Global.gs._local_player.id : 0);

		/*Global.SetDParam(0, p.name_1);
		Global.SetDParam(1, p.name_2);
		Global.SetDParam(2, Global._date);
		_edit_str_buf = Global.GetString(Str.STR_4004);*/
		_edit_str_buf = p.generateFileName();
	}


	static void SaveLoadDlgWndProc(Window w, WindowEvent e)
	{
		

		switch (e.event) {
		case WE_CREATE: { // Set up OPENTTD button 
			o_dir.type = FiosType.DIRECT;
			switch (Global._saveload_mode) {
			case Global.SLD_SAVE_GAME:
			case Global.SLD_LOAD_GAME:
				o_dir.name = Global._path.save_dir;
				break;

			case Global.SLD_SAVE_SCENARIO:
			case Global.SLD_LOAD_SCENARIO:
				o_dir.name = Global._path.scenario_dir;
				break;

			default:
				o_dir.name = Global._path.personal_dir;
			}
			break;
		}

		case WE_PAINT: {
			int y,pos;
			FiosItem item;

			w.SetVScrollCount(_fios_list.size());
			w.DrawWindowWidgets();
			DrawFiosTexts(w.width);

			if (_savegame_sort_dirty) {
				_savegame_sort_dirty = false;
				MakeSortedSaveGameList();
			}

			Gfx.GfxFillRect(w.widget.get(7).left + 1, w.widget.get(7).top + 1, w.widget.get(7).right, w.widget.get(7).bottom, 0xD7);
			Gfx.DoDrawString(
					0 != (_savegame_sort_order & SORT_DESCENDING) ? Gfx.DOWNARROW : Gfx.UPARROW,
							0 != (_savegame_sort_order & SORT_BY_NAME) ? w.widget.get(2).right - 9 : w.widget.get(3).right - 9,
									15, 16
					);

			y = w.widget.get(7).top + 1;
			pos = w.vscroll.pos;
			while (pos < _fios_list.size()) {
				item = _fios_list.get( pos );
				Gfx.DoDrawStringTruncated(item.title, 4, y, _fios_colors[item.type.ordinal()], w.width - 18);
				pos++;
				y += 10;
				if (y >= w.vscroll.getCap() * 10 + w.widget.get(7).top + 1) break;
			}

			if (Global._saveload_mode == Global.SLD_SAVE_GAME || Global._saveload_mode == Global.SLD_SAVE_SCENARIO) {
				Textbuf.DrawEditBox(w, 10);
			}
			break;
		}
		case WE_CLICK:
			switch (e.widget) {
			case 2: // Sort save names by name 
				_savegame_sort_order = (_savegame_sort_order == SORT_BY_NAME) ?
						SORT_BY_NAME | SORT_DESCENDING : SORT_BY_NAME;
				_savegame_sort_dirty = true;
				w.SetWindowDirty();
				break;

			case 3: // Sort save names by date 
				_savegame_sort_order = (_savegame_sort_order == SORT_BY_DATE) ?
						SORT_BY_DATE | SORT_DESCENDING : SORT_BY_DATE;
				_savegame_sort_dirty = true;
				w.SetWindowDirty();
				break;

			case 6: // OpenTTD 'button', jumps to OpenTTD directory 
				FileIO.FiosBrowseTo(o_dir);
				w.SetWindowDirty();
				BuildFileList();
				break;

			case 7: { // Click the listbox 
				int y = (e.pt.y - w.widget.get(e.widget).top - 1) / 10;
				String name;
				final FiosItem file;

				if (y < 0 || (y += w.vscroll.pos) >= w.vscroll.getCount()) return;

				file = _fios_list.get( y );

				name = FileIO.FiosBrowseTo(file);
				if (name != null) {
					if (Global._saveload_mode == Global.SLD_LOAD_GAME || Global._saveload_mode == Global.SLD_LOAD_SCENARIO) {
						Global._switch_mode = (Global._game_mode == GameModes.GM_EDITOR) ? SwitchModes.SM_LOAD_SCENARIO : SwitchModes.SM_LOAD;

						SetFiosType(file.type);
						//ttd_strlcpy(_file_to_saveload.name, name, sizeof(_file_to_saveload.name));
						//ttd_strlcpy(_file_to_saveload.title, file.title, sizeof(_file_to_saveload.title));
						Main._file_to_saveload.name =  name;
						Main._file_to_saveload.title = file.title;

						w.DeleteWindow();
					} else {
						// Global.SLD_SAVE_GAME, Global.SLD_SAVE_SCENARIO copy clicked name to editbox
						//ttd_strlcpy(w.as_querystr_d().text.buf, file.name, w.as_querystr_d().text.maxlength);
						w.as_querystr_d().text.setText( file.name );
						//UpdateTextBufferSize(w.as_querystr_d().text);
						w.InvalidateWidget(10);
					}
				} else {
					// Changed directory, need repaint.
					w.SetWindowDirty();
					BuildFileList();
				}
				break;
			}

			case 11: case 12: // Delete, Save game 
				break;
			}
			break;
		case WE_MOUSELOOP:
			Textbuf.HandleEditBox(w, 10);
			break;
		case WE_KEYPRESS:
			if (e.keycode == Window.WKC_ESC) {
				w.DeleteWindow();
				return;
			}

			if (Global._saveload_mode == Global.SLD_SAVE_GAME || Global._saveload_mode == Global.SLD_SAVE_SCENARIO) {
				if (Textbuf.HandleEditBoxKey(w, 10, e) == 1) // Press Enter 
					w.HandleButtonClick(12);
			}
			break;
		case WE_TIMEOUT:
			if (BitOps.HASBIT(w.click_state, 11)) { // Delete button clicked 
				if (!FileIO.FiosDelete(w.as_querystr_d().text.getString())) {
					Global.ShowErrorMessage(Str.INVALID_STRING, Str.STR_4008_UNABLE_TO_DELETE_FILE, 0, 0);
				}
				w.SetWindowDirty();
				BuildFileList();
				if (Global._saveload_mode == Global.SLD_SAVE_GAME) {
					GenerateFileName(); // Reset file name to current date 
					w.as_querystr_d().text.UpdateTextBufferSize();
				}
			} else if (BitOps.HASBIT(w.click_state, 12)) { // Save button clicked 
				Global._switch_mode = SwitchModes.SM_SAVE;
				Main._file_to_saveload.name = FileIO.FiosMakeSavegameName(w.as_querystr_d().text.getString());

				// In the editor set up the vehicle engines correctly (date might have changed) 
				if (Global._game_mode == GameModes.GM_EDITOR) Engine.StartupEngines();
			}
			break;
		case WE_DESTROY:
			// pause is only used in single-player, non-editor mode, non menu mode
			if(!Global._networking && (Global._game_mode != GameModes.GM_EDITOR) && (Global._game_mode != GameModes.GM_MENU))
				Cmd.DoCommandP(null, 0, 0, null, Cmd.CMD_PAUSE);
			
			Global._no_scroll = BitOps.RETCLRBIT(Global._no_scroll, Global.SCROLL_SAVE);
			break;
		case WE_RESIZE: {
			// Widget 2 and 3 have to go with halve speed, make it so obiwan 
			int diff = e.diff.x / 2;
			w.widget.get(2).right += diff;
			w.widget.get(3).left  += diff;
			w.widget.get(3).right += e.diff.x;

			// Same for widget 11 and 12 in save-dialog 
			if (Global._saveload_mode == Global.SLD_SAVE_GAME || Global._saveload_mode == Global.SLD_SAVE_SCENARIO) {
				w.widget.get(11).right += diff;
				w.widget.get(12).left  += diff;
				w.widget.get(12).right += e.diff.x;
			}

			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 10);
		} break;
		default:
			break;
		}
		 
	}

	static final WindowDesc _load_dialog_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 257, 294,
			Window.WC_SAVELOAD,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
			_load_dialog_1_widgets,
			MiscGui::SaveLoadDlgWndProc
			);

	static final WindowDesc _load_dialog_scen_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 257, 294,
			Window.WC_SAVELOAD,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
			_load_dialog_2_widgets,
			MiscGui::SaveLoadDlgWndProc
			);

	static final WindowDesc _save_dialog_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 257, 320,
			Window.WC_SAVELOAD,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
			_save_dialog_widgets,
			MiscGui::SaveLoadDlgWndProc
			);

	static final WindowDesc _save_dialog_scen_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 257, 320,
			Window.WC_SAVELOAD,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
			_save_dialog_scen_widgets,
			MiscGui::SaveLoadDlgWndProc
			);

	static final WindowDesc _saveload_dialogs[] = {
			_load_dialog_desc,
			_load_dialog_scen_desc,
			_save_dialog_desc,
			_save_dialog_scen_desc,
	};

	public static void ShowSaveLoadDialog(int mode)
	{
		Window w;

		ViewPort.SetObjectToPlace(Sprite.SPR_CURSOR_ZZZ, 0, 0, 0);
		Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
		Window.DeleteWindowById(Window.WC_SAVELOAD, 0);

		Global._saveload_mode = mode;
		Global._no_scroll = BitOps.RETSETBIT(Global._no_scroll, Global.SCROLL_SAVE);

		switch (mode) {
		case Global.SLD_SAVE_GAME:     GenerateFileName(); break;
		case Global.SLD_SAVE_SCENARIO: _edit_str_buf = "UNNAMED"; break;
		}

		w = Window.AllocateWindowDesc(_saveload_dialogs[mode]);
		w.vscroll.setCap(24);
		w.resize.step_width = 2;
		w.resize.step_height = 10;
		w.resize.height = w.height - 14 * 10; // Minimum of 10 items
		w.click_state = BitOps.RETSETBIT(w.click_state, 7);
		w.as_querystr_d().text.setCaret(false);
		w.as_querystr_d().text.maxlength = _edit_str_buf.length() - 1;
		w.as_querystr_d().text.maxwidth = 240;
		w.as_querystr_d().text.setText(_edit_str_buf);
		w.as_querystr_d().text.UpdateTextBufferSize();

		// pause is only used in single-player, non-editor mode, non-menu mode. It
		// will be unpaused in the WindowEvents.WE_DESTROY event handler.
		if (Global._game_mode != GameModes.GM_MENU && !Global._networking && Global._game_mode != GameModes.GM_EDITOR) {
			Cmd.DoCommandP(null, 1, 0, null, Cmd.CMD_PAUSE);
		}

		BuildFileList();

		ViewPort.ResetObjectToPlace();
	}

	public static void RedrawAutosave()
	{
		Window.FindWindowById(Window.WC_STATUS_BAR, 0).SetWindowDirty();
	}

	static final Widget _select_scenario_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, Str.STR_00C5,						Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,     7,    11,   256,     0,    13, Str.STR_400E_SELECT_NEW_GAME_TYPE, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,  Window.RESIZE_RIGHT,     7,     0,   256,    14,    25, 0x0,								Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     7,     0,   127,    14,    25, Str.STR_SORT_BY_NAME,		Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     7,   128,   256,    14,    25, Str.STR_SORT_BY_DATE,		Str.STR_SORT_ORDER_TIP),
			new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_RB,     7,     0,   244,    26,   319, 0x0,								Str.STR_NULL),
			new Widget(          Window.WWT_6,     Window.RESIZE_RB,     7,     2,   243,    28,   317, 0x0,								Str.STR_400F_SELECT_SCENARIO_GREEN_PRE),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,     7,   245,   256,    26,   307, 0x0,								Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,     7,   245,   256,   308,   319, 0x0,								Str.STR_RESIZE_BUTTON),
			//{   WIDGETS_END},
	};

	static void SelectScenarioWndProc(Window  w, WindowEvent  e)
	{
		final int list_start = 45;

		switch (e.event) {
		case WE_PAINT:
		{
			int y,pos;			

			if (_savegame_sort_dirty) {
				_savegame_sort_dirty = false;
				MakeSortedSaveGameList();
			}

			int _fios_num = _fios_list.size();
			//SetVScrollCount(w, _fios_num);
			w.SetVScrollCount(_fios_num);

			w.DrawWindowWidgets();
			Gfx.DoDrawString(
					0 != (_savegame_sort_order & SORT_DESCENDING) ? Gfx.DOWNARROW : Gfx.UPARROW,
							0 != (_savegame_sort_order & SORT_BY_NAME) ? w.widget.get(3).right - 9 : w.widget.get(4).right - 9,
									15, 16
					);
			Gfx.DrawString(4, 32, Str.STR_4010_GENERATE_RANDOM_NEW_GAME, 9);

			y = list_start;
			pos = w.vscroll.pos;

			while (pos < _fios_num) {
				final FiosItem item = _fios_list.get(pos);
				Gfx.DoDrawString(item.title, 4, y, _fios_colors[item.type.ordinal()]);
				pos++;
				y += 10;
				if (y >= w.vscroll.getCap() * 10 + list_start) break;
			} 
		}
		break;

		case WE_KEYPRESS:
			if(e.keycode == Window.WKC_RETURN)
				IntroGui.GenRandomNewGame(Hal.Random(), Hal.InteractiveRandom());
			break;
			
		case WE_CLICK:
			switch(e.widget) {
			case 3: /* Sort scenario names by name */
				_savegame_sort_order = (_savegame_sort_order == SORT_BY_NAME) ?
						SORT_BY_NAME | SORT_DESCENDING : SORT_BY_NAME;
				_savegame_sort_dirty = true;
				w.SetWindowDirty();
				break;

			case 4: /* Sort scenario names by date */
				_savegame_sort_order = (_savegame_sort_order == SORT_BY_DATE) ?
						SORT_BY_DATE | SORT_DESCENDING : SORT_BY_DATE;
				_savegame_sort_dirty = true;
				w.SetWindowDirty();
				break;

			case 6: /* Click the listbox */
				if (e.pt.y < list_start)
					IntroGui.GenRandomNewGame(Hal.Random(), Hal.InteractiveRandom());
				else {

					String name;
					int y = (e.pt.y - list_start) / 10;
					final FiosItem file;

					if (y < 0 || (y += w.vscroll.pos) >= w.vscroll.getCount())
						return;

					file = _fios_list.get(y);

					name = FileIO.FiosBrowseTo(file);
					if (name != null) {
						SetFiosType(file.type);
						Main._file_to_saveload.name = name;
						w.DeleteWindow();
						IntroGui.StartScenarioEditor(Hal.Random(), Hal.InteractiveRandom());
					}
				}
				break;
			}
			break;
		//case WE_DESTROY:			break;

		case WE_RESIZE: {
			/* Widget 3 and 4 have to go with halve speed, make it so obiwan */
			int diff = e.diff.x / 2;
			w.widget.get(3).right += diff;
			w.widget.get(4).left  += diff;
			w.widget.get(4).right += e.diff.x;

			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 10);
		} break;

		default:
			break;
		}
	}

	public static void SetFiosType(final FiosType type)
	{

		switch (type) {
		case FILE:
		case SCENARIO:
			Main._file_to_saveload.mode = SaveLoad.SL_LOAD;
			break;

		case OLDFILE:
		case OLD_SCENARIO:
			//Main._file_to_saveload.mode = SaveLoad.SL_OLD_LOAD;
			Main._file_to_saveload.mode = SaveLoad.SL_LOAD; // TODO kill me
			break;

		default:
			Main._file_to_saveload.mode = SaveLoad.SL_INVALID;
			break;
		} 
	}

	static final WindowDesc _select_scenario_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 257, 320,
			Window.WC_SAVELOAD,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_RESIZABLE,
			_select_scenario_widgets,
			MiscGui::SelectScenarioWndProc
			);

	static void AskForNewGameToStart()
	{
		Window w;

		Window.DeleteWindowById(Window.WC_QUERY_STRING, 0);
		Window.DeleteWindowById(Window.WC_SAVELOAD, 0);

		Global._saveload_mode = Global.SLD_NEW_GAME;
		BuildFileList();

		w = Window.AllocateWindowDesc(_select_scenario_desc);
		w.vscroll.setCap(27);
		w.resize.step_width = 2;
		w.resize.step_height = 10;
		w.resize.height = w.height - 10 * 17; // Minimum of 10 in the list
	}

	static int ClickMoneyCheat(int p1, int p2)
	{
		Cmd.DoCommandP(null, -10000000, 0, null, Cmd.CMD_MONEY_CHEAT);
		return 1; //true;
	}

	// p1 player to set to, p2 is -1 or +1 (down/up)
	static int ClickChangePlayerCheat(int p1, int p2)
	{
		while (p1 >= 0 && p1 < Global.MAX_PLAYERS) {
			//if (Player._players[p1].is_active) 
			if (Player.GetPlayer(p1).isActive()) 
			{
				Global.gs._local_player = PlayerID.get( p1 );
				Hal.MarkWholeScreenDirty();
				return Global.gs._local_player.id;
			}
			p1 += p2;
		}

		return Global.gs._local_player.id;
	}

	// p1 -1 or +1 (down/up)
	static int ClickChangeClimateCheat(int p1, int p2)
	{
		if (p1 == -1) p1 = 3;
		if (p1 ==  4) p1 = 0;
		GameOptions._opt.landscape =  p1;
		GfxInit.GfxLoadSprites();
		Hal.MarkWholeScreenDirty();
		return GameOptions._opt.landscape;
	}


	// p2 1 (increase) or -1 (decrease)
	static int ClickChangeDateCheat(int p1, int p2)
	{
		YearMonthDay ymd = new YearMonthDay(Global.get_date());
		//YearMonthDay.ConvertDayToYMD(ymd, Global._date);

		if ((ymd.year == 0 && p2 == -1) || (ymd.year == 170 && p2 == 1)) return Global.get_cur_year();

		Global.gs.date.SetDate(GameDate.ConvertYMDToDay(Global.get_cur_year() + p2, ymd.month, ymd.day));
		Engine.EnginesMonthlyLoop();
		Window.FindWindowById(Window.WC_STATUS_BAR, 0).SetWindowDirty();
		return Global.get_cur_year();
	}

	//typedef int CheckButtonClick(int, int);
	/*
	typedef enum ce_type {
		CE_BOOL = 0,
		CE_UINT8 = 1,
		CE_INT16 = 2,
		CE_UINT16 = 3,
		CE_INT32 = 4,
		CE_BYTE = 5,
		CE_CLICK = 6,
	} ce_type;


	static int ReadCE(final CheatEntry* ce)
	{
		switch (ce.type) {
			case CE_BOOL:   return *(boolean*  )ce.variable;
			case CE_UINT8:  return *(byte* )ce.variable;
			case CE_INT16:  return *(int* )ce.variable;
			case CE_UINT16: return *(int*)ce.variable;
			case CE_INT32:  return *(int* )ce.variable;
			case CE_BYTE:   return *(byte*  )ce.variable;
			case CE_CLICK:  return 0;
			default: NOT_REACHED();
		}

		// useless, but avoids compiler warning this way 
		return 0;
	}

	static void WriteCE(final CheatEntry *ce, int val)
	{
		switch (ce.type) {
			case CE_BOOL:   *(boolean*  )ce.variable = (boolean  )val; break;
			case CE_BYTE:   *(byte*  )ce.variable = (byte  )val; break;
			case CE_UINT8:  *(byte* )ce.variable = (byte )val; break;
			case CE_INT16:  *(int* )ce.variable = (int )val; break;
			case CE_UINT16: *(int*)ce.variable = (int)val; break;
			case CE_INT32:  *(int* )ce.variable = val;         break;
			case CE_CLICK: break;
			default: NOT_REACHED();
		}
	}
	 */

	/*
	static final CheatEntry _cheats_ui[] = {
		new CheatEntry(CE_CLICK, 0, Str.STR_CHEAT_MONEY, 					&_cheats.money.value, 					&_cheats.money.been_used, 					&ClickMoneyCheat,					0, 0, 0),
		new CheatEntry(CE_UINT8, 0, Str.STR_CHEAT_CHANGE_PLAYER, 	&Global.gs._local_player, 								&_cheats.switch_player.been_used,		&ClickChangePlayerCheat,	0, 11, 1),
		new CheatEntry(CE_BOOL, 0, Str.STR_CHEAT_EXTRA_DYNAMITE,	&_cheats.magic_bulldozer.value,	&_cheats.magic_bulldozer.been_used, null,											0, 0, 0),
		new CheatEntry(CE_BOOL, 0, Str.STR_CHEAT_CROSSINGTUNNELS,	&_cheats.crossing_tunnels.value,&_cheats.crossing_tunnels.been_used,null,											0, 0, 0),
		new CheatEntry(CE_BOOL, 0, Str.STR_CHEAT_BUILD_IN_PAUSE,	&_cheats.build_in_pause.value,	&_cheats.build_in_pause.been_used,	null,											0, 0, 0),
		new CheatEntry(CE_BOOL, 0, Str.STR_CHEAT_NO_JETCRASH,			&_cheats.no_jetcrash.value,			&_cheats.no_jetcrash.been_used,			null,											0, 0, 0),
		new CheatEntry(CE_BOOL, 0, Str.STR_CHEAT_SETUP_PROD,			&_cheats.setup_prod.value,			&_cheats.setup_prod.been_used,			null,											0, 0, 0),
		new CheatEntry(CE_UINT8, 0, Str.STR_CHEAT_SWITCH_CLIMATE, &GameOptions._opt.landscape, 								&_cheats.switch_climate.been_used,	&ClickChangeClimateCheat,-1, 4, 1),
		new CheatEntry(CE_UINT8, 0, Str.STR_CHEAT_CHANGE_DATE,		&_cur_year,											&_cheats.change_date.been_used,			&ClickChangeDateCheat,	 -1, 1, 1),
	};
	 */

	static final Widget _cheat_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,		Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   399,     0,    13, Str.STR_CHEATS,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   399,    14,   159, 0x0,					Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   399,    14,   159, 0x0,					Str.STR_CHEATS_TIP),
			//{   WIDGETS_END},
	};

	//extern void DrawPlayerIcon(int p, int x, int y);

	static void CheatsWndProc(Window w, WindowEvent e)
	{
		/*
		switch (e.event) {
		case WindowEvents.WE_PAINT: {
			int clk = WP(w,def_d).data_1;
			int x, y;
			int i;

			w.DrawWindowWidgets();

			Gfx.DrawStringMultiCenter(200, 25, Str.STR_CHEATS_WARNING, 350);

			x = 0;
			y = 45;

			for (i = 0; i != lengthof(_cheats_ui); i++) {
				final CheatEntry ce = _cheats_ui[i];

				Gfx.DrawSprite((*ce.been_used) ? Sprite.SPR_BOX_CHECKED : Sprite.SPR_BOX_EMPTY, x + 5, y + 2);

				if (ce.type == CE_BOOL) {
					Gfx.DrawFrameRect(x + 20, y + 1, x + 30 + 9, y + 9, (*(boolean*)ce.variable) ? 6 : 4, (*(boolean*)ce.variable) ? FR_LOWERED : 0);
					Global.SetDParam(0, *(boolean*)ce.variable ? Str.STR_CONFIG_PATCHES_ON : Str.STR_CONFIG_PATCHES_OFF);
				}	else if (ce.type == CE_CLICK) {
					Gfx.DrawFrameRect(x + 20, y + 1, x + 30 + 9, y + 9, 0, (WP(w,def_d).data_1 == i * 2 + 1) ? FR_LOWERED : 0);
					if (i == 0) {
						Global.SetDParam64(0, 10000000);
					} else {
						Global.SetDParam(0, false);
					}
				} else {
					int val;

					Gfx.DrawFrameRect(x + 20, y + 1, x + 20 + 9, y + 9, 3, clk == i * 2 + 1 ? FR_LOWERED : 0);
					Gfx.DrawFrameRect(x + 30, y + 1, x + 30 + 9, y + 9, 3, clk == i * 2 + 2 ? FR_LOWERED : 0);
					Gfx.DrawStringCentered(x + 25, y + 1, Str.STR_6819, 0);
					Gfx.DrawStringCentered(x + 35, y + 1, Str.STR_681A, 0);

					val = ReadCE(ce);

					// set correct string for switch climate cheat
					if (ce.str == Str.STR_CHEAT_SWITCH_CLIMATE) val += Str.STR_TEMPERATE_LANDSCAPE;

					Global.SetDParam(0, val);

					// display date for change date cheat
					if (ce.str == Str.STR_CHEAT_CHANGE_DATE) Global.SetDParam(0, _date);

					// draw colored flag for change player cheat
					if (ce.str == Str.STR_CHEAT_CHANGE_PLAYER) {
						DrawPlayerIcon(PlayerID.getCurrent(), 156, y + 2);
					}
				}

				Gfx.DrawString(50, y + 1, ce.str, 0);

				y += 12;
			}
			break;
		}

		case WindowEvents.WE_CLICK: {
				final CheatEntry *ce;
				int btn = (e.click.pt.y - 46) / 12;
				int val, oval;
				int x = e.click.pt.x;

				// not clicking a button?
				if (!BitOps.IS_INT_INSIDE(x, 20, 40) || btn >= lengthof(_cheats_ui)) break;

				ce = &_cheats_ui[btn];
				oval = val = ReadCE(ce);

		 *ce.been_used = true;

				switch (ce.type) {
					case CE_BOOL:	{
						val ^= 1;
						if (ce.click_proc != null) ce.click_proc(val, 0);
						break;
					}

					case CE_CLICK:	{
						ce.click_proc(val, 0);
						WP(w,def_d).data_1 = btn * 2 + 1;
						break;
					}

					default:	{
						if (x >= 30) {
							//increase
							val += ce.step;
							if (val > ce.max) val = ce.max;
						} else {
							// decrease
							val -= ce.step;
							if (val < ce.min) val = ce.min;
						}

						// take whatever the function returns
						val = ce.click_proc(val, (x >= 30) ? 1 : -1);

						if (val != oval) {
							WP(w,def_d).data_1 = btn * 2 + 1 + ((x >= 30) ? 1 : 0);
						}
						break;
					}
				}

				if (val != oval) {
					WriteCE(ce, val);
					w.SetWindowDirty();
				}

				w.flags4 |= 5 << WF_TIMEOUT_SHL;

				w.SetWindowDirty();
			}
			break;
		case WindowEvents.WE_TIMEOUT:
			WP(w,def_d).data_1 = 0;
			w.SetWindowDirty();
			break;
		}
		 */
	}

	static final WindowDesc _cheats_desc = new WindowDesc(
			240, 22, 400, 160,
			Window.WC_CHEATS,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_cheat_widgets,
			MiscGui::CheatsWndProc
			);


	void ShowCheatWindow()
	{
		Window w;

		Window.DeleteWindowById(Window.WC_CHEATS, 0);
		w = Window.AllocateWindowDesc(_cheats_desc);

		if (w != null) w.SetWindowDirty();
	}


}
