package game;

import java.util.Arrays;
import java.util.Comparator;

import game.util.BitOps;
import game.util.Strings;

// extends to have finalants too
public abstract class TownGui extends Town 
{

	// used to get a sorted list of the towns
	static int _num_town_sort;

	//static char _bufcache[64];
	//static int _last_town_idx;
	//static int[] _town_sort;
	static Integer[] _town_sort;


	static final Widget _town_authority_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,    10,     0,    13, Str.STR_00C5,				Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    13,    11,   316,     0,    13, Str.STR_2022_LOCAL_AUTHORITY, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    13,     0,   316,    14,   105, 0x0,							Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    13,     0,   306,   106,   157, 0x0,							Str.STR_2043_LIST_OF_THINGS_TO_DO_AT),
			new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,    13,   305,   316,   106,   157, 0x0,							Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    13,     0,   316,   158,   209, 0x0,							Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,     0,   316,   210,   221, Str.STR_2042_DO_IT,	Str.STR_2044_CARRY_OUT_THE_HIGHLIGHTED),
			//new Widget(   WIDGETS_END},
	};

	//extern final byte _town_action_costs[8];
	//extern void DrawPlayerIcon(int p, int x, int y);

	/** Get a list of available actions to do at a town.
	 * @param *nump if not null add put the number of available actions in it
	 * @param pid the player that is querying the town
	 * @param *t the town that is queried
	 * @return bitmasked value of enabled actions
	 */
	static int GetMaskOfTownActions(int [] nump, PlayerID pid, final Town t)
	{
		int avail, ref;
		int num = 0;
		int avail_buttons = 0x7F; // by default all buttons except bribe are enabled.
		int buttons = 0;

		if (pid.id != Owner.OWNER_SPECTATOR) {
			int i;

			// bribe option enabled?
			if (Global._patches.bribe) {
				// if unwanted, disable everything.
				if (t.unwanted[pid.id] != 0) {
					avail_buttons = 0;
				} else if (t.ratings[pid.id] < RATING_BRIBE_MAXIMUM) {
					avail_buttons = BitOps.RETSETBIT(avail_buttons, 7); // Allow bribing
				}
			}

			// Things worth more than this are not shown
			avail = Player.GetPlayer(pid).player_money + Global._price.station_value * 200;
			ref = Global._price.build_industry >> 8;

				for (i = 0; i != _town_action_costs.length; i++, avail_buttons >>= 1) {
					if (BitOps.HASBIT(avail_buttons, 0) && avail >= _town_action_costs[i] * ref) {
						buttons = BitOps.RETSETBIT(buttons, i);
						num++;
					}
				}

				/* Disable build statue if already built */
				if (BitOps.HASBIT(t.statues, pid.id)) {
					buttons = BitOps.RETCLRBIT(buttons, 4);
					num--;
				}

		}

		if (nump != null) nump[0] = num;
		return buttons;
	}

	static int GetNthSetBit(int bits, int n)
	{
		int i = 0;

		if (n >= 0) {
			do {
				if ((0!=(bits & 1)) && --n < 0) return i;
				i++;
			} while( (bits >>= 1) != 0 );
		}
		return -1;
	}

	static void TownAuthorityWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			final Town t = GetTown(w.window_number.n);
			int [] numact = {0};
			int buttons = GetMaskOfTownActions(numact, Global._local_player, t);

			SetVScrollCount(w, numact[0] + 1);

			if (w.as_def_d().data_1 != -1 && !BitOps.HASBIT(buttons, w.as_def_d().data_1))
				w.as_def_d().data_1 = -1;

			w.disabled_state = (w.as_def_d().data_1 == -1) ? (1 << 6) : 0;

			{
				int y;
				//final Player  p;
				int r;
				//StringID 
				int str;

				Global.SetDParam(0, w.window_number.n);
				w.DrawWindowWidgets();

				Gfx.DrawString(2, 15, Str.STR_2023_TRANSPORT_COMPANY_RATINGS, 0);

				// Draw list of players
				y = 25;
				//FOR_ALL_PLAYERS(p)
				Player.forEach( (p) ->
				{
					if (p.is_active && (BitOps.HASBIT(t.have_ratings, p.index.id) || t.exclusivity.id == p.index.id)) {
						DrawPlayerIcon(p.index, 2, y);

						Global.SetDParam(0, p.name_1);
						Global.SetDParam(1, p.name_2);
						Global.SetDParam(2, Player.GetPlayerNameString(p.index, 3));

						r = t.ratings[p.index.id];
						str = Str.STR_3035_APPALLING; // Apalling

						if( r > RATING_APPALLING) {
							str++;
							if(r > RATING_VERYPOOR) {											// Very Poor
								str++;
								if(r > RATING_POOR) {												// Poor
									str++;
									if( r > RATING_MEDIOCRE) {											// Mediocore
										str++;						
										if(r > RATING_GOOD) {											// Good
											str++;						
											if(r > RATING_VERYGOOD) {											// Very Good
												str++;
												if(r <= RATING_EXCELLENT) 											// Excellent
													str++;														// Outstanding
											}}}}}}

						Global.SetDParam(4, str);
						if (t.exclusivity.id == p.index.id) // red icon for player with exclusive rights
							Gfx.DrawSprite(Sprite.SPR_BLOT | Sprite.PALETTE_TO_RED, 18, y);

						Gfx.DrawString(28, y, Str.STR_2024, 0);
						y += 10;
					}
				});
			}

			// Draw actions list
			{
				int y = 107, i;
				int pos = w.vscroll.pos;

				if (--pos < 0) {
					Gfx.DrawString(2, y, Str.STR_2045_ACTIONS_AVAILABLE, 0);
					y += 10;
				}
				for (i = 0; buttons != 0; i++) {
					if (pos <= -5) break;

					if ( (0!=(buttons&1)) && --pos < 0) {
						Gfx.DrawString(3, y, Str.STR_2046_SMALL_ADVERTISING_CAMPAIGN + i, 6);
						y += 10;
					}
					buttons >>= 1;
				}
			}

			{
				int i = w.as_def_d().data_1;

				if (i != -1) {
					Global.SetDParam(1, (Global._price.build_industry >> 8) * _town_action_costs[i]);
					Global.SetDParam(0, Str.STR_2046_SMALL_ADVERTISING_CAMPAIGN + i);
					Gfx.DrawStringMultiLine(2, 159, Str.STR_204D_INITIATE_A_SMALL_LOCAL + i, 313);
				}
			}

		} break;

		case WE_CLICK:
			switch (e.widget) {
			case 3: { /* listbox */
				final Town t = GetTown(w.window_number.n);
				int y = (e.pt.y - 0x6B) / 10;

				if (!BitOps.IS_INT_INSIDE(y, 0, 5)) return;

				y = GetNthSetBit(GetMaskOfTownActions(null, Global._local_player, t), y + w.vscroll.pos - 1);
				if (y >= 0) {
					w.as_def_d().data_1 = y;
					w.SetWindowDirty();
				}
				break;
			}

			case 6: { /* carry out the action */
				Cmd.DoCommandP(GetTown(w.window_number.n).xy, w.window_number, w.as_def_d().data_1, null, Cmd.CMD_DO_TOWN_ACTION | Cmd.CMD_MSG(Str.STR_00B4_CAN_T_DO_THIS));
				break;
			}
			}
			break;

		case WE_4:
			w.SetWindowDirty();
			break;
		}
	}

	static final WindowDesc _town_authority_desc = new WindowDesc(
			-1, -1, 317, 222,
			Window.WC_TOWN_AUTHORITY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_town_authority_widgets,
			TownGui::TownAuthorityWndProc
			);

	static void ShowTownAuthorityWindow(int town)
	{
		Window  w = Window.AllocateWindowDescFront(_town_authority_desc, town);

		if (w != null) {
			w.vscroll.cap = 5;
			w.as_def_d().data_1 = -1;
		}
	}

	static void TownViewWndProc(Window w, WindowEvent e)
	{
		Town t = GetTown(w.window_number.n);

		switch (e.event) {
		case WE_PAINT:
			// disable renaming town in network games if you are not the server
			if (Global._networking && !Global._network_server) 
				w.disabled_state = BitOps.RETSETBIT(w.disabled_state, 8);

			Global.SetDParam(0, t.index);
			w.DrawWindowWidgets();

			Global.SetDParam(0, t.population);
			Global.SetDParam(1, t.num_houses);
			Gfx.DrawString(2, 107, Str.STR_2006_POPULATION, 0);

			Global.SetDParam(0, t.act_pass);
			Global.SetDParam(1, t.max_pass);
			Gfx.DrawString(2, 117, Str.STR_200D_PASSENGERS_LAST_MONTH_MAX, 0);

			Global.SetDParam(0, t.act_mail);
			Global.SetDParam(1, t.max_mail);
			Gfx.DrawString(2, 127, Str.STR_200E_MAIL_LAST_MONTH_MAX, 0);

			w.DrawWindowViewport();
			break;

		case WE_CLICK:
			switch (e.widget) {
			case 6: /* scroll to location */
				ViewPort.ScrollMainWindowToTile(t.xy);
				break;

			case 7: /* town authority */
				ShowTownAuthorityWindow(w.window_number.n);
				break;

			case 8: /* rename */
				Global.SetDParam(0, w.window_number.n);
				MiscGui.ShowQueryString(Str.STR_TOWN, Str.STR_2007_RENAME_TOWN, 31, 130, w.window_class, w.window_number);
				break;

			case 9: /* expand town */
				t.ExpandTown();
				break;

			case 10: /* delete town */
				t.DeleteTown();
				break;
			case 11:
				mAirport.MA_EditorAddAirport(t);
				Console.IConsolePrintF(100100100, "it works");
			}
			break;

		case WE_ON_EDIT_TEXT:
			if (e.str.length() != 0) {
				Global._cmd_text = e.str;
				Cmd.DoCommandP(null, w.window_number.n, 0, null,
						Cmd.CMD_RENAME_TOWN | Cmd.CMD_MSG(Str.STR_2008_CAN_T_RENAME_TOWN));
			}
			break;
		}
	}


	static final Widget _town_view_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    13,    11,   247,     0,    13, Str.STR_2005, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    13,   248,   259,     0,    13, 0x0,      Str.STR_STICKY_BUTTON),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    13,     0,   259,    14,   105, 0x0,      Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    13,     2,   257,    16,   103, 0x0,      Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    13,     0,   259,   106,   137, 0x0,      Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,     0,    85,   138,   149, Str.STR_00E4_LOCATION,				Str.STR_200B_CENTER_THE_MAIN_VIEW_ON),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,    86,   171,   138,   149, Str.STR_2020_LOCAL_AUTHORITY,Str.STR_2021_SHOW_INFORMATION_ON_LOCAL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,   172,   259,   138,   149, Str.STR_0130_RENAME,					Str.STR_200C_CHANGE_TOWN_NAME),
			//{   WIDGETS_END},
	};

	static final WindowDesc _town_view_desc = new WindowDesc(
			-1, -1, 260, 150,
			Window.WC_TOWN_VIEW,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON,
			_town_view_widgets,
			TownGui::TownViewWndProc
			);

	static final Widget _town_view_scen_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,    10,     0,    13, Str.STR_00C5,					Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    13,    11,   172,     0,    13, Str.STR_2005,					Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    13,   248,   259,     0,    13, 0x0,               Str.STR_STICKY_BUTTON),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    13,     0,   259,    14,   105, 0x0,								Str.STR_NULL),
			new Widget(          Window.WWT_6,   Window.RESIZE_NONE,    13,     2,   257,    16,   103, 0x0,								Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    13,     0,   259,   106,   137, 0x0,								Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,     0,    85,   138,   149, Str.STR_00E4_LOCATION,	Str.STR_200B_CENTER_THE_MAIN_VIEW_ON),
			new Widget(      Window.WWT_EMPTY,   Window.RESIZE_NONE,     0,     0,     0,     0,     0, 0x0,								Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,   173,   247,     0,    13, Str.STR_0130_RENAME,		Str.STR_200C_CHANGE_TOWN_NAME),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,    86,   171,   138,   149, Str.STR_023C_EXPAND,		Str.STR_023B_INCREASE_SIZE_OF_TOWN),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,   172,   243,   138,   149, Str.STR_0290_DELETE,		Str.STR_0291_DELETE_THIS_TOWN_COMPLETELY),
			new Widget( Window.WWT_PUSHTXTBTN,	Window.RESIZE_NONE,	13,	  244,	 259,	138,   149,		  Str.STR_PLANE,		Str.STR_MA_AIRPORT_BUTTON_TOOLTIP),	 								
			//{   WIDGETS_END},
	};

	static final WindowDesc _town_view_scen_desc = new WindowDesc(
			-1, -1, 260, 150,
			Window.WC_TOWN_VIEW,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON,
			_town_view_scen_widgets,
			TownGui::TownViewWndProc
			);

	void ShowTownViewWindow(int town)
	{
		Window w;

		if (Global._game_mode != GameModes.GM_EDITOR) {
			w = Window.AllocateWindowDescFront(_town_view_desc, town);
		} else {
			w = Window.AllocateWindowDescFront(_town_view_scen_desc, town);
		}

		if (w != null) {
			w.flags4 |= Window.WF_DISABLE_VP_SCROLL;
			ViewPort.AssignWindowViewport(w, 3, 17, 0xFE, 0x56, GetTown(town).xy.tile, 1);
		}
	}

	static final Widget _town_directory_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    13,    11,   195,     0,    13, Str.STR_2000_TOWNS,					Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,   Window.RESIZE_NONE,    13,   196,   207,     0,    13, 0x0,										Str.STR_STICKY_BUTTON),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,     0,    98,    14,    25, Str.STR_SORT_BY_NAME,				Str.STR_SORT_ORDER_TIP),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    13,    99,   195,    14,    25, Str.STR_SORT_BY_POPULATION,	Str.STR_SORT_ORDER_TIP),
			new Widget(     Window.WWT_IMGBTN, Window.RESIZE_BOTTOM,    13,     0,   195,    26,   189, 0x0,										Str.STR_200A_TOWN_NAMES_CLICK_ON_NAME),
			new Widget(  Window.WWT_SCROLLBAR, Window.RESIZE_BOTTOM,    13,   196,   207,    14,   189, 0x0,										Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(    Window.WWT_PANEL,   	Window.RESIZE_TB,    	13,  		0,   195,    190,  201, 0x0,				Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,     Window.RESIZE_TB,    13,   196,   207,   190,   201, 0x0,										Str.STR_RESIZE_BUTTON),
			//{   WIDGETS_END},
	};



	static class TownNameSorter implements Comparator<Integer> {
		public int compare(Integer a, Integer b) 
		{
			//char buf1[64];
			//int val;
			int r;
			int [] argv = new int[1];

			argv[0] = a;
			String buf1 = Strings.GetStringWithArgs(Str.STR_TOWN, argv);

			argv[0] = b;
			String buf2 = Strings.GetStringWithArgs(Str.STR_TOWN, argv);

			r = buf1.compareTo(buf2);
			if(0 != (_town_sort_order & 1)) r = -r;
			return r;
		}
	}


	static class TownPopSorter implements Comparator<Integer> {
		public int compare(Integer a, Integer b) {
			final Town ta = GetTown(a);
			final Town tb = GetTown(b);
			int r = ta.population - tb.population;
			if(0 !=  (_town_sort_order & 1)) r = -r;
			return r;
		}
	}


	static void MakeSortedTownList()
	{
		//final Town t;
		int n = 0;

		/* Create array for sorting */
		//_town_sort = realloc(_town_sort, GetTownPoolSize() * sizeof(_town_sort[0]));
		_town_sort = new Integer[GetTownPoolSize()];
		if (_town_sort == null)
			Global.error("Could not allocate memory for the town-sorting-list");

		//FOR_ALL_TOWNS(t)
		Town.forEach( (t) ->
		{
			if (t.xy != null) _town_sort[n++] = t.index;
		});

		_num_town_sort = n;

		//_last_town_idx = 0; // used for "cache"
		//qsort(_town_sort, n, sizeof(_town_sort[0]), _town_sort_order & 2 ? TownPopSorter : TownNameSorter);
		Comparator<Integer> sorter = (0 != (_town_sort_order & 2)) ? new TownPopSorter() : new TownNameSorter();
		Arrays.sort(_town_sort, sorter );

		Global.DEBUG_misc( 1, "Resorting Towns list...");
	}


	static void TownDirectoryWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			if (_town_sort_dirty) {
				_town_sort_dirty = false;
				MakeSortedTownList();
			}

			SetVScrollCount(w, _num_town_sort);

			w.DrawWindowWidgets();
			Gfx.DoDrawString((_town_sort_order & 1) != 0 ? Gfx.DOWNARROW : Gfx.UPARROW, (_town_sort_order <= 1) ? 88 : 187, 15, 0x10);

			{
				final Town t;
				int n = 0;
				int i = w.vscroll.pos;
				int y = 28;

				while (i < _num_town_sort) {
					t = GetTown(_town_sort[i]);

					assert(t.xy != null);

					Global.SetDParam(0, t.index);
					Global.SetDParam(1, t.population);
					Gfx.DrawString(2, y, Str.STR_2057, 0);

					y += 10;
					i++;
					if (++n == w.vscroll.cap) break; // max number of towns in 1 window
				}
				Global.SetDParam(0, GetWorldPopulation());
				Gfx.DrawString(3, w.height - 12 + 2, Str.STR_TOWN_POPULATION, 0);
			}
		} break;

		case WE_CLICK:
			switch(e.widget) {
			case 3: { /* Sort by Name ascending/descending */
				_town_sort_order = (_town_sort_order == 0) ? 1 : 0;
				_town_sort_dirty = true;
				w.SetWindowDirty();
			} break;

			case 4: { /* Sort by Population ascending/descending */
				_town_sort_order = (_town_sort_order == 2) ? 3 : 2;
				_town_sort_dirty = true;
				w.SetWindowDirty();
			} break;

			case 5: { /* Click on Town Matrix */
				int id_v = (e.pt.y - 28) / 10;

				if (id_v >= w.vscroll.cap) return; // click out of bounds

				id_v += w.vscroll.pos;

				if (id_v >= _num_town_sort) return; // click out of town bounds

				{
					final Town t = GetTown(_town_sort[id_v]);
					assert(t.xy != null);

					ViewPort.ScrollMainWindowToTile(t.xy);
				}
			}	break;
			}
			break;

		case WE_4:
			w.SetWindowDirty();
			break;

		case WE_RESIZE:
			w.vscroll.cap += e.diff.y / 10;
			break;
		}
	}

	static final WindowDesc _town_directory_desc = new WindowDesc(
			-1, -1, 208, 202,
			Window.WC_TOWN_DIRECTORY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_town_directory_widgets,
			TownGui::TownDirectoryWndProc
			);


	void ShowTownDirectory()
	{
		Window  w = Window.AllocateWindowDescFront(_town_directory_desc, 0);

		if (w != null) {
			w.vscroll.cap = 16;
			w.resize.step_height = 10;
			w.resize.height = w.height - 10 * 6; // minimum of 10 items in the list, each item 10 high
		}
	}



}
