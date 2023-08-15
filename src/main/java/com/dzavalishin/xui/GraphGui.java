package com.dzavalishin.xui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import com.dzavalishin.charts.CargoPaymentRatesChart;
import com.dzavalishin.charts.CompanyValueGraph;
import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Economy;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.SignStruct;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Str;
import com.dzavalishin.tables.SmallMapGuiTables;
import com.dzavalishin.util.ArrayPtr;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Strings;

public class GraphGui 
{

	static int _legend_excludebits;
	static int _legend_cargobits;

	/************************/
	/* GENERIC GRAPH DRAWER */
	/************************/

	static final int GRAPH_NUM = 16;

	static class GraphDrawer {
		int sel; // bitmask of the players *excluded* (e.g. 11111111 means that no players are shown)
		int num_dataset;
		int num_on_x_axis;
		int month;
		int year;
		boolean include_neg;
		int num_vert_lines;
		int unk61A;
		int unk61C;
		int left, top;
		int height;
		//StringID 
		int format_str_y_axis;
		byte color_3, color_2, bg_line_color;
		final byte [] colors = new byte[GRAPH_NUM];
		final long [][] cost = new long[GRAPH_NUM][24]; // last 2 years
	}

	static final int INVALID_VALUE = 0x80000000;

	static void DrawGraph(final GraphDrawer gw)
	{

		int i,j,k;
		int x,y,old_x,old_y;
		int color;
		int right, bottom;
		int num_x, num_dataset;
		long mx;
		int adj_height;
		long y_scaling, tmp;
		long value;
		long cur_val;
		int sel;

		//final long []row_ptr;
		//final long []col_ptr;

		//ArrayPtr<Long> row_ptr;
		//ArrayPtr<Long> col_ptr;

		/* the colors and cost array of GraphDrawer must accomodate
		 * both values for cargo and players. So if any are higher, quit */
		assert(GRAPH_NUM >= AcceptedCargo.NUM_CARGO && GRAPH_NUM >= Global.MAX_PLAYERS);

		color = Global._color_list[gw.bg_line_color].window_color_1b;

		/* draw the vertical lines */
		i = gw.num_vert_lines; assert(i > 0);
		x = gw.left + 66;
		bottom = gw.top + gw.height - 1;
		do {
			Gfx.GfxFillRect(x, gw.top, x, bottom, color);
			x += 22;
		} while (--i > 0);

		/* draw the horizontal lines */
		i = 9;
		x = gw.left + 44;
		y = gw.height + gw.top;
		right = gw.left + 44 + gw.num_vert_lines*22-1;

		do {
			Gfx.GfxFillRect(x, y, right, y, color);
			y -= gw.height >> 3;
		} while (--i > 0);

		/* draw vertical edge line */
		Gfx.GfxFillRect(x, gw.top, x, bottom, gw.color_2);

		adj_height = gw.height;
		if (gw.include_neg) adj_height >>>= 1;

		/* draw horiz edge line */
		y = adj_height + gw.top;
		Gfx.GfxFillRect(x, y, right, y, gw.color_2);

		/* find the max element */
		if (gw.num_on_x_axis == 0)
			return;

		num_dataset = gw.num_dataset;
		assert(num_dataset > 0);

		//return Arrays.stream(array).filter(Objects::nonNull).mapToLong(Long::longValue).toArray();

		//Arrays.stream(gw.cost[0]).mapToObj( (lv) -> new Long(lv) ).toArray();
		//Long[] la = ArrayPtr.toLongArray(gw.cost[0]);
		//row_ptr = new ArrayPtr<Long>( gw.cost[0] );
		//row_ptr = new ArrayPtr<Long>( la );

		mx = 0;
		/* bit selection for the showing of various players, base max element
		 * on to-be shown player-information. This way the graph can scale */
		sel = gw.sel;
		do {
			Long[] la1 = ArrayPtr.toLongArray(gw.cost[i]);
			//row_ptr = 
			ArrayPtr<Long> col_ptr = new ArrayPtr<>( la1 ); // gw.cost[0];

			if (0==(sel&1)) {
				num_x = gw.num_on_x_axis;
				assert(num_x > 0);
				//col_ptr = new ArrayPtr<Long>( row_ptr );
				do 
				{
					if( !col_ptr.hasCurrent() ) break;
					if (col_ptr.r() != INVALID_VALUE) {
						mx = Math.max(mx, Math.abs(col_ptr.r()));
					}
					col_ptr.inc();					
				} while (--num_x > 0);
			}
			sel>>=1;
			//row_ptr.madd(24);
		} while ( --num_dataset > 0 );

		/* setup scaling */
		y_scaling = INVALID_VALUE;
		value = adj_height * 2L;

		if (mx > value) {
			mx = (mx + 7) & ~7;
			y_scaling = (( (value>>>1) << 32) / mx);
			value = mx;
		}

		/* draw text strings on the y axis */
		tmp = value;
		if (gw.include_neg) 
			tmp >>>= 1;

			x = gw.left + 45;
			y = gw.top - 3;
			i = 9;
			do {
				Global.SetDParam(0, gw.format_str_y_axis);
				Global.SetDParam64(1, tmp);
				tmp -= (value >>> 3);
				Gfx.DrawStringRightAligned(x, y, Str.STR_0170, gw.color_3);
				y += gw.height >>> 3;
			} while (--i > 0);

			/* draw strings on the x axis */
			if (gw.month != 0xFF) {
				x = gw.left + 44;
				y = gw.top + gw.height + 1;
				j = gw.month;
				k = gw.year + Global.MAX_YEAR_BEGIN_REAL;
				i = gw.num_on_x_axis;assert(i>0);
				do {
					Global.SetDParam(2, k);
					Global.SetDParam(0, j + Str.STR_0162_JAN);
					Global.SetDParam(1, j + Str.STR_0162_JAN + 2);
					Gfx.DrawString(x, y, j == 0 ? Str.STR_016F : Str.STR_016E, gw.color_3);

					j += 3;
					if (j >= 12) {
						j = 0;
						k++;
					}
					x += 22;
				} while (--i > 0);
			} else {
				x = gw.left + 52;
				y = gw.top + gw.height + 1;
				j = gw.unk61A;
				i = gw.num_on_x_axis;assert(i>0);
				do {
					Global.SetDParam(0, j);
					Gfx.DrawString(x, y, Str.STR_01CB, gw.color_3);
					j += gw.unk61C;
					x += 22;
				} while (--i > 0);
			}

			/* draw lines and dots */
			i = 0;
			sel = gw.sel; // show only selected lines. GraphDrawer qw.sel set in Graph-Legend (_legend_excludebits)
			do {

				//Long[] la1 = (Long[]) Arrays.stream(gw.cost[0]).mapToObj( (lv) -> Long.valueOf(lv) ).toArray();
				Long[] la1 = ArrayPtr.toLongArray(gw.cost[i]);
				//row_ptr = 
				ArrayPtr<Long> col_ptr = new ArrayPtr<>( la1 ); // gw.cost[0];


				if (0!=(sel & 1))
				{
					sel>>=1;
					continue;
				}

				x = gw.left + 55;
				j = gw.num_on_x_axis;assert(j>0);
				//col_ptr = new ArrayPtr<Long>( row_ptr );
				color = gw.colors[i];
				old_y = old_x = INVALID_VALUE;
				do {
					if(!col_ptr.hasCurrent()) break;
					cur_val = col_ptr.rpp(); //System.out.printf("%d ", cur_val);
					if (cur_val != INVALID_VALUE) 
					{
						final long mul = BitOps.BIGMULSS64(cur_val, y_scaling >>> 4, 28);
						y = (int) (adj_height - mul + gw.top);
						System.out.printf("mul %d y %d  ", mul, y);
						Gfx.GfxFillRect(x-1, y-1, x+1, y+1, color);
						if (old_x != INVALID_VALUE)
							Gfx.GfxDrawLine(old_x, old_y, x, y, color);

						old_x = x;
						old_y = y;
					} else {
						old_x = INVALID_VALUE;
					}
					x+=22;
				} while (--j > 0);
				System.out.println();

				sel>>=1;
				//row_ptr.madd(24);
			} while ( ++i < gw.num_dataset);
	}

	/****************/
	/* GRAPH LEGEND */
	/****************/

	public static void DrawPlayerIcon(int p, int x, int y)
	{
		Gfx.DrawSprite(Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(p) + 0x2EB), x, y);
	}

	static void GraphLegendWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT:

			Player.forEach( (p) ->
			{
				if (!p.isActive()) _legend_excludebits = BitOps.RETSETBIT(_legend_excludebits, p.getIndex().id);
			});

			w.click_state = (~_legend_excludebits) << 3;
			w.DrawWindowWidgets();

			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player p = ii.next();
				if (!p.isActive()) continue;

				DrawPlayerIcon(p.getIndex().id, 4, 18+p.getIndex().id*12);

				Global.SetDParam(0, p.getName_1());
				Global.SetDParam(1, p.getName_2());
				Global.SetDParam(2, Player.GetPlayerNameString(p.getIndex(), 3));
				Gfx.DrawString(21,17+p.getIndex().id*12,Str.STR_7021,BitOps.HASBIT(_legend_excludebits, p.getIndex().id) ? 0x10 : 0xC);
			}
			break;

		case WE_CLICK:
			if (BitOps.IS_INT_INSIDE(e.widget, 3, 11)) {
				_legend_excludebits ^= (1 << (e.widget - 3));
				w.SetWindowDirty();
				Window.InvalidateWindow(Window.WC_INCOME_GRAPH, 0);
				Window.InvalidateWindow(Window.WC_OPERATING_PROFIT, 0);
				Window.InvalidateWindow(Window.WC_DELIVERED_CARGO, 0);
				Window.InvalidateWindow(Window.WC_PERFORMANCE_HISTORY, 0);
				Window.InvalidateWindow(Window.WC_COMPANY_VALUE, 0);
			}
			break;
		default:
			break;
		}
	}

	static final Widget _graph_legend_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   249,     0,    13, Str.STR_704E_KEY_TO_COMPANY_GRAPHS, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   249,    14,   113, 0x0,Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   247,    16,    27, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   247,    28,    39, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   247,    40,    51, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   247,    52,    63, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   247,    64,    75, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   247,    76,    87, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   247,    88,    99, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   247,   100,   111, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
	};

	static final WindowDesc _graph_legend_desc = new WindowDesc(
			-1, -1, 250, 114,
			Window.WC_GRAPH_LEGEND,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_graph_legend_widgets,
			GraphGui::GraphLegendWndProc
			);

	static void ShowGraphLegend()
	{
		Window.AllocateWindowDescFront(_graph_legend_desc, 0);
	}

	/********************/
	/* OPERATING PROFIT */
	/********************/

	static void SetupGraphDrawerForPlayers(GraphDrawer gd)
	{
		int [] excludebits = {_legend_excludebits};
		int mo,yr;

		// Exclude the players which aren't valid
		Player.forEach((p) ->
		{
			if (!p.isActive()) excludebits[0] = BitOps.RETSETBIT(excludebits[0],p.getIndex().id);
		});

		gd.sel = excludebits[0];
		gd.num_vert_lines = 24;

		int [] nums = {0};
		Player.forEach((p) ->
		{
			if (p.isActive()) nums[0] = Math.max(nums[0],p.num_valid_stat_ent);
		});

		gd.num_on_x_axis = Math.min(nums[0],24);

		mo = (Global.get_cur_month()/3-nums[0])*3;
		yr = Global.get_cur_year();
		while (mo < 0) {
			yr--;
			mo += 12;
		}

		gd.year = yr;
		gd.month = mo;
	}

	static void OperatingProfitWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			GraphDrawer gd = new GraphDrawer();
			//final Player  p;
			//int i;
			//int numd;

			w.DrawWindowWidgets();

			gd.left = 2;
			gd.top = 18;
			gd.height = 136;
			gd.include_neg = true;
			gd.format_str_y_axis = Str.STR_CURRCOMPACT;
			gd.color_3 = 0x10;
			gd.color_2 = (byte) 0xD7;
			gd.bg_line_color = 0xE;

			SetupGraphDrawerForPlayers(gd);

			int [] numd = {0};

			Player.forEach((p) ->
			{
				if (p.isActive()) {
					gd.colors[numd[0]] = (byte) Global._color_list[p.getColor()].window_color_bgb;
					for(int j=gd.num_on_x_axis,i=0; --j >= 0; i++ ) 
					{
						gd.cost[numd[0]][i] = (j >= p.num_valid_stat_ent) ? INVALID_VALUE : (long)(p.old_economy[j].income + p.old_economy[j].expenses);
					}
				}
				numd[0]++;
			});

			gd.num_dataset = numd[0];

			DrawGraph(gd);
		}	break;
		case WE_CLICK:
			if (e.widget == 2) /* Clicked on Legend */
				ShowGraphLegend();
			break;
		default:
			break;
		}
	}

	static final Widget _operating_profit_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,												Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   525,     0,    13, Str.STR_7025_OPERATING_PROFIT_GRAPH, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   526,   575,     0,    13, Str.STR_704C_KEY,										Str.STR_704D_SHOW_KEY_TO_GRAPHS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   575,    14,   173, 0x0,															Str.STR_NULL),
	};

	static final WindowDesc _operating_profit_desc = new WindowDesc(
			-1, -1, 576, 174,
			Window.WC_OPERATING_PROFIT,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_operating_profit_widgets,
			GraphGui::OperatingProfitWndProc
			);


	static void ShowOperatingProfitGraph()
	{
		if (null != Window.AllocateWindowDescFront(_operating_profit_desc, 0)) {
			Window.InvalidateWindow(Window.WC_GRAPH_LEGEND, 0);
		}
	}


	/****************/
	/* INCOME GRAPH */
	/****************/

	static void IncomeGraphWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			GraphDrawer gd = new GraphDrawer();
			//final Player  p;
			int i,j;
			int numd;

			w.DrawWindowWidgets();

			gd.left = 2;
			gd.top = 18;
			gd.height = 104;
			gd.include_neg = false;
			gd.format_str_y_axis = Str.STR_CURRCOMPACT;
			gd.color_3 = 0x10;
			gd.color_2 = (byte) 0xD7;
			gd.bg_line_color = 0xE;
			SetupGraphDrawerForPlayers(gd);

			numd = 0;
			//FOR_ALL_PLAYERS(p) 
			//Player.forEach((p) ->
			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player p = ii.next();

				if (p.isActive()) {
					gd.colors[numd] = (byte) Global._color_list[p.getColor()].window_color_bgb;
					for(j=gd.num_on_x_axis,i=0; --j >= 0;) {
						gd.cost[numd][i] = (j >= p.num_valid_stat_ent) ? INVALID_VALUE : (long)p.old_economy[j].income;
						i++;
					}
				}
				numd++;
			}

			gd.num_dataset = numd;

			DrawGraph(gd);
			break;
		}

		case WE_CLICK:
			if (e.widget == 2)
				ShowGraphLegend();
			break;
		default:
			break;
		}
	}

	static final Widget _income_graph_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,							Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   525,     0,    13, Str.STR_7022_INCOME_GRAPH, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   526,   575,     0,    13, Str.STR_704C_KEY,					Str.STR_704D_SHOW_KEY_TO_GRAPHS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   575,    14,   141, 0x0,										Str.STR_NULL),
	};

	static final WindowDesc _income_graph_desc = new WindowDesc(
			-1, -1, 576, 142,
			Window.WC_INCOME_GRAPH,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_income_graph_widgets,
			GraphGui::IncomeGraphWndProc
			);

	static void ShowIncomeGraph()
	{
		if (null != Window.AllocateWindowDescFront(_income_graph_desc, 0)) {
			Window.InvalidateWindow(Window.WC_GRAPH_LEGEND, 0);
		}
	}

	/*******************/
	/* DELIVERED CARGO */
	/*******************/

	static void DeliveredCargoGraphWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			GraphDrawer gd = new GraphDrawer();
			//final Player  p;
			int i,j;
			int numd;

			w.DrawWindowWidgets();

			gd.left = 2;
			gd.top = 18;
			gd.height = 104;
			gd.include_neg = false;
			gd.format_str_y_axis = Str.STR_7024;
			gd.color_3 = 0x10;
			gd.color_2 = (byte) 0xD7;
			gd.bg_line_color = 0xE;
			SetupGraphDrawerForPlayers(gd);

			numd = 0;
			//FOR_ALL_PLAYERS(p) 
			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player p = ii.next();

				if (p.isActive()) {
					gd.colors[numd] = (byte) Global._color_list[p.getColor()].window_color_bgb;
					for(j=gd.num_on_x_axis,i=0; --j >= 0;) {
						gd.cost[numd][i] = (j >= p.num_valid_stat_ent) ? INVALID_VALUE : (long)p.old_economy[j].delivered_cargo;
						i++;
					}
				}
				numd++;
			}

			gd.num_dataset = numd;

			DrawGraph(gd);
			break;
		}

		case WE_CLICK:
			if (e.widget == 2)
				ShowGraphLegend();
			break;
		default:
			break;
		}
	}

	static final Widget _delivered_cargo_graph_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,													Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   525,     0,    13, Str.STR_7050_UNITS_OF_CARGO_DELIVERED, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   526,   575,     0,    13, Str.STR_704C_KEY,											Str.STR_704D_SHOW_KEY_TO_GRAPHS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   575,    14,   141, 0x0,																Str.STR_NULL),
	};

	static final WindowDesc _delivered_cargo_graph_desc = new WindowDesc(
			-1, -1, 576, 142,
			Window.WC_DELIVERED_CARGO,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_delivered_cargo_graph_widgets,
			GraphGui::DeliveredCargoGraphWndProc
			);

	static void ShowDeliveredCargoGraph()
	{
		if (null != Window.AllocateWindowDescFront(_delivered_cargo_graph_desc, 0)) {
			Window.InvalidateWindow(Window.WC_GRAPH_LEGEND, 0);
		}
	}

	/***********************/
	/* PERFORMANCE HISTORY */
	/***********************/

	static void PerformanceHistoryWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			GraphDrawer gd = new GraphDrawer();
			//final Player  p;
			int i,j;
			int numd;

			w.DrawWindowWidgets();

			gd.left = 2;
			gd.top = 18;
			gd.height = 200;
			gd.include_neg = false;
			gd.format_str_y_axis = Str.STR_7024;
			gd.color_3 = 0x10;
			gd.color_2 = (byte) 0xD7;
			gd.bg_line_color = 0xE;
			SetupGraphDrawerForPlayers(gd);

			numd = 0;
			//FOR_ALL_PLAYERS(p) 
			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player p = ii.next();

				if (p.isActive()) {
					gd.colors[numd] = (byte) Global._color_list[p.getColor()].window_color_bgb;
					for(j=gd.num_on_x_axis,i=0; --j >= 0;) {
						gd.cost[numd][i] = (j >= p.num_valid_stat_ent) ? INVALID_VALUE : (long)p.old_economy[j].performance_history;
						i++;
					}
				}
				numd++;
			}

			gd.num_dataset = numd;

			DrawGraph(gd);
			break;
		}

		case WE_CLICK:
			if (e.widget == 2)
				ShowGraphLegend();
			if (e.widget == 3)
				ShowPerformanceRatingDetail();
			break;
		default:
			break;
		}
	}

	static final Widget _performance_history_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,															Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   475,     0,    13, Str.STR_7051_COMPANY_PERFORMANCE_RATINGS,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   526,   575,     0,    13, Str.STR_704C_KEY,													Str.STR_704D_SHOW_KEY_TO_GRAPHS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   476,   525,     0,    13, Str.STR_PERFORMANCE_DETAIL_KEY,						Str.STR_704D_SHOW_KEY_TO_GRAPHS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   575,    14,   237, 0x0,																		Str.STR_NULL),
	};

	static final WindowDesc _performance_history_desc = new WindowDesc(
			-1, -1, 576, 238,
			Window.WC_PERFORMANCE_HISTORY,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_performance_history_widgets,
			GraphGui::PerformanceHistoryWndProc
			);

	static void ShowPerformanceHistoryGraph()
	{
		if (null != Window.AllocateWindowDescFront(_performance_history_desc, 0)) {
			Window.InvalidateWindow(Window.WC_GRAPH_LEGEND, 0);
		}
	}

	/*****************/
	/* COMPANY VALUE */
	/***************** /

	static void CompanyValueGraphWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			GraphDrawer gd = new GraphDrawer();
			//final Player  p;
			int i,j;
			int numd;

			w.DrawWindowWidgets();

			gd.left = 2;
			gd.top = 18;
			gd.height = 200;
			gd.include_neg = false;
			gd.format_str_y_axis = Str.STR_CURRCOMPACT;
			gd.color_3 = 0x10;
			gd.color_2 = (byte) 0xD7;
			gd.bg_line_color = 0xE;
			SetupGraphDrawerForPlayers(gd);

			numd = 0;

			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player p = ii.next();

				if (p.isActive()) {
					gd.colors[numd] = (byte) Global._color_list[p.getColor()].window_color_bgb;
					for(j=gd.num_on_x_axis,i=0; --j >= 0;) {
						gd.cost[numd][i] = (j >= p.num_valid_stat_ent) ? INVALID_VALUE : (long)p.old_economy[j].company_value;
						i++;
					}
				}
				numd++;
			}

			gd.num_dataset = numd;

			DrawGraph(gd);
			break;
		}

		case WE_CLICK:
			if (e.widget == 2)
				ShowGraphLegend();
			break;
		default:
			break;
		}
	}

	static final Widget _company_value_graph_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   525,     0,    13, Str.STR_7052_COMPANY_VALUES,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,    14,   526,   575,     0,    13, Str.STR_704C_KEY,						Str.STR_704D_SHOW_KEY_TO_GRAPHS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   575,    14,   237, 0x0,											Str.STR_NULL),
	};

	static final WindowDesc _company_value_graph_desc = new WindowDesc(
			-1, -1, 576, 238,
			Window.WC_COMPANY_VALUE,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_company_value_graph_widgets,
			GraphGui::CompanyValueGraphWndProc
			);
	 */
	static void ShowCompanyValueGraph()
	{
		/*
		if (null == Window.AllocateWindowDescFront(_company_value_graph_desc, 0)) {
			Window.InvalidateWindow(Window.WC_GRAPH_LEGEND, 0);
		}*/
		CompanyValueGraph.showChart();
	}

	/*****************/
	/* PAYMENT RATES */
	/***************** /

	static final int _cargo_legend_colors[] = {152, 32, 15, 174, 208, 194, 191, 84, 184, 10, 202, 215};

	static void CargoPaymentRatesWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			int i, j, x, y;
			GraphDrawer gd = new GraphDrawer();

			gd.sel = _legend_cargobits;
			w.click_state = (~_legend_cargobits) << 3;
			w.DrawWindowWidgets();

			x = 495;
			y = 25;

			for(i=0; i!=AcceptedCargo.NUM_CARGO; i++) {
				Gfx.GfxFillRect(x, y, x+8, y+5, 0);
				Gfx.GfxFillRect(x+1, y+1, x+7, y+4, _cargo_legend_colors[i]);
				Global.SetDParam(0, Global._cargoc.names_s[i]);
				Gfx.DrawString(x+14, y, Str.STR_7065, 0);
				y += 8;
			}

			gd.left = 2;
			gd.top = 24;
			gd.height = 104;
			gd.include_neg = false;
			gd.format_str_y_axis = Str.STR_CURRCOMPACT;
			gd.color_3 = 16;
			gd.color_2 = (byte) 215;
			gd.bg_line_color = 14;
			gd.num_dataset = AcceptedCargo.NUM_CARGO;
			gd.num_on_x_axis = 20;
			gd.num_vert_lines = 20;
			gd.month = 0xFF;
			gd.unk61A = 10;
			gd.unk61C = 10;

			for(i=0; i!=AcceptedCargo.NUM_CARGO; i++) {
				gd.colors[i] = (byte) _cargo_legend_colors[i];
				for(j=0; j!=20; j++) {
					gd.cost[i][j] = (long)Economy.GetTransportedGoodsIncome(10, 20, j*6+6,i);
				}
			}

			DrawGraph(gd);

			Gfx.DrawString(2 + 46, 24 + gd.height + 7, Str.STR_7062_DAYS_IN_TRANSIT, 0);
			Gfx.DrawString(2 + 84, 24 - 9, Str.STR_7063_PAYMENT_FOR_DELIVERING, 0);
		} break;

		case WE_CLICK: {
			switch(e.widget) {
			case 3: case 4: case 5: case 6:
			case 7: case 8: case 9: case 10:
			case 11: case 12: case 13: case 14:
				_legend_cargobits ^= 1 << (e.widget - 3);
				w.SetWindowDirty();
				break;
			}
		} break;
		default:
			break;
		}
	}

	static final Widget _cargo_payment_rates_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,											Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   567,     0,    13, Str.STR_7061_CARGO_PAYMENT_RATES,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    14,     0,   567,    14,   141, 0x0,														Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    24,    31, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    32,    39, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    40,    47, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    48,    55, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    56,    63, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    64,    71, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    72,    79, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    80,    87, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    88,    95, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,    96,   103, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,   104,   111, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    12,   493,   562,   112,   119, 0x0,														Str.STR_7064_TOGGLE_GRAPH_FOR_CARGO),
	};

	static final WindowDesc _cargo_payment_rates_desc = new WindowDesc(
			-1, -1, 568, 142,
			Window.WC_PAYMENT_RATES,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_cargo_payment_rates_widgets,
			GraphGui::CargoPaymentRatesWndProc
			);

	 */
	static void ShowCargoPaymentRates()
	{
		//Window.AllocateWindowDescFront(_cargo_payment_rates_desc, 0);
		CargoPaymentRatesChart.showChart();
	}


	static  /*StringID*/ int GetPerformanceTitleFromValue(int value)
	{
		//return SmallMapGuiTables._performance_titles[minu(value, 1000) >> 6];
		return SmallMapGuiTables._performance_titles[Math.min(value, 1000) >> 6];
	}

	static class PerfHistComp implements Comparator<Player>
	{
		public int  compare(Player p1, Player p2)
		{
			if( p1 == null ) return 1;
			if( p2 == null ) return -1;

			return p2.old_economy[1].performance_history - p1.old_economy[1].performance_history;
		}
	}

	static void CompanyLeagueWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			final Player[]  plist = new Player[Global.MAX_PLAYERS];
			int i;

			w.DrawWindowWidgets();

			int[] pl_num = {0};

			Player.forEach( (p) ->
			{
				if (p.isActive()) plist[pl_num[0]++] = p;
			});

			Arrays.sort( plist, new PerfHistComp() );

			for (i = 0; i != pl_num[0]; i++) 
			{
				Player p = plist[i];
				Global.SetDParam(0, i + Str.STR_01AC_1ST);
				Global.SetDParam(1, p.getName_1());
				Global.SetDParam(2, p.getName_2());
				Global.SetDParam(3, Player.GetPlayerNameString(p.getIndex(), 4));
				//Global.SetDParam(5, GetPerformanceTitleFromValue(p.old_economy[1].performance_history));
				Global.SetDParam(5, GetPerformanceTitleFromValue(p.old_economy[0].performance_history));

				Gfx.DrawString(2, 15 + i * 10, i == 0 ? Str.STR_7054 : Str.STR_7055, 0);
				DrawPlayerIcon(p.getIndex().id, 27, 16 + i * 10);
			}

			break;
		}
		default:
			break;
		}
	}


	static final Widget _company_league_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,											Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   399,     0,    13, Str.STR_7053_COMPANY_LEAGUE_TABLE,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   399,    14,    96, 0x0,														Str.STR_NULL),
	};

	static final WindowDesc _company_league_desc = new WindowDesc(
			-1, -1, 400, 97,
			Window.WC_COMPANY_LEAGUE,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_company_league_widgets,
			GraphGui::CompanyLeagueWndProc
			);

	static void ShowCompanyLeagueTable()
	{
		Window.AllocateWindowDescFront(_company_league_desc,0);
	}

	/*****************************/
	/* PERFORMANCE RATING DETAIL */
	/*****************************/

	static void PerformanceRatingDetailWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			long val, needed, score; 
			int x;
			int i;
			int owner; 
			int y=14;
			int total_score = 0;
			int color_done, color_notdone;

			// Draw standard stuff
			w.DrawWindowWidgets();

			// The player of which we check the detail performance rating
			owner = BitOps.FindFirstBit(w.click_state) - 13;

			// Paint the player icons
			for (i=0;i<Global.MAX_PLAYERS;i++) {
				if (!Player.GetPlayer(i).isActive()) {
					// Check if we have the player as an active player
					if (0==(w.disabled_state & (1 << (i+13)))) {
						// Bah, player gone :(
						w.disabled_state += 1 << (i+13);
						// Is this player selected? If so, select first player (always save? :s)
						if (w.click_state == 1 << (i + 13))
							w.click_state = 1 << 13;
						// We need a repaint
						w.SetWindowDirty();
					}
					continue;
				}

				// Check if we have the player marked as inactive
				if(0 != (w.disabled_state & (1 << (i+13)))) {
					// New player! Yippie :p
					w.disabled_state -= 1 << (i+13);
					// We need a repaint
					w.SetWindowDirty();
				}

				if (i == owner) x = 1; else x = 0;
				DrawPlayerIcon(i, (i * 37 + 13 + x), (16 + x) ); // TODO long truncated
			}

			// The colors used to show how the progress is going
			color_done = Global._color_list[6].window_color_1b;
			color_notdone = Global._color_list[4].window_color_1b;

			// Draw all the score parts
			for (i=0; i< Economy.NUM_SCORE; i++) {
				y += 20;
				val = Economy._score_part[owner][i];
				needed = Economy._score_info[i].needed;
				score = Economy._score_info[i].score;
				// SCORE_TOTAL has his own rulez ;)
				if (i == Economy.SCORE_TOTAL) {
					needed = total_score;
					score = Economy.SCORE_MAX;
				} else
					total_score += score;

				Gfx.DrawString(7, y, Str.STR_PERFORMANCE_DETAIL_VEHICLES + i, 0);

				// Draw the score
				Global.SetDParam(0, (int)score); // TODO long -> int
				Gfx.DrawStringRightAligned(107, y, Str.SET_PERFORMANCE_DETAIL_INT, 0);

				// Calculate the %-bar
				if (val > needed) x = 50;
				else if (val == 0) x = 0;
				else x = (int) ((val * 50) / needed); // TODO long -> int

				// SCORE_LOAN is inversed
				if (val < 0 && i == Economy.SCORE_LOAN)
					x = 0;

				// Draw the bar
				if (x != 0)
					Gfx.GfxFillRect(112, y-2, x + 112, y+10, color_done);
				if (x != 50)
					Gfx.GfxFillRect(x + 112, y-2, 50 + 112, y+10, color_notdone);

				// Calculate the %
				if (val > needed) x = 100;
				else x = (int)((val * 100) / needed); // TODO long -> int

				// SCORE_LOAN is inversed
				if (val < 0 && i == Economy.SCORE_LOAN)
					x = 0;

				// Draw it
				Global.SetDParam(0, x);
				Gfx.DrawStringCentered(137, y, Str.STR_PERFORMANCE_DETAIL_PERCENT, 0);

				// Economy.SCORE_LOAN is inversed
				if (i == Economy.SCORE_LOAN)
					val = needed - val;

				// Draw the amount we have against what is needed
				//  For some of them it is in currency format
				Global.SetDParam(0, (int)val); // TODO long -> int
				Global.SetDParam(1, (int)needed); // TODO long -> int
				switch (i) {
				case Economy.SCORE_MIN_PROFIT:
				case Economy.SCORE_MIN_INCOME:
				case Economy.SCORE_MAX_INCOME:
				case Economy.SCORE_MONEY:
				case Economy.SCORE_LOAN:
					Gfx.DrawString(167, y, Str.STR_PERFORMANCE_DETAIL_AMOUNT_CURRENCY, 0);
					break;
				default:
					Gfx.DrawString(167, y, Str.STR_PERFORMANCE_DETAIL_AMOUNT_INT, 0);
				}
			}

			break;
		}

		case WE_CLICK:
			// Check which button is clicked
			if (BitOps.IS_INT_INSIDE(e.widget, 13, 21)) {
				// Is it no on disable?
				if ((w.disabled_state & (1 << e.widget)) == 0) {
					w.click_state = 1 << e.widget;
					w.SetWindowDirty();
				}
			}
			break;

		case WE_CREATE:
		{
			int i;
			//Player p2;
			w.hidden_state = 0;
			w.disabled_state = 0;

			// Hide the player who are not active
			for (i=0;i<Global.MAX_PLAYERS;i++) {
				if (!Player.GetPlayer(i).isActive()) {
					w.disabled_state += 1 << (i+13);
				}
			}
			// Update all player stats with the current data
			//  (this is because _score_info is not saved to a savegame)
			//FOR_ALL_PLAYERS(p2)
			Player.forEach( (p2) ->
			{
				if (p2.isActive())
					Economy.UpdateCompanyRatingAndValue(p2, false);
			});

			w.custom_array[0] = Global.DAY_TICKS;
			w.custom_array[1] = 5;

			w.click_state = 1 << 13;

			w.SetWindowDirty();
		}
		break;
		case WE_TICK:
		{
			// Update the player score every 5 days
			if (--w.custom_array[0] == 0) {
				w.custom_array[0] = Global.DAY_TICKS;
				if (--w.custom_array[1] == 0) {
					//Player p2;
					w.custom_array[1] = 5;

					//FOR_ALL_PLAYERS(p2)
					// Skip if player is not active
					Player.forEach( (p2) ->
					{
						if (p2.isActive())
							Economy.UpdateCompanyRatingAndValue(p2, false);
					});

					w.SetWindowDirty();
				}
			}
		}
		break;
		default:
			break;
		}
	}

	static final Widget _performance_rating_detail_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,								Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   298,     0,    13, Str.STR_PERFORMANCE_DETAIL,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,    14,    27, 0x0,											Str.STR_NULL),

			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,    28,    47, 0x0,Str.STR_PERFORMANCE_DETAIL_VEHICLES_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,    48,    67, 0x0,Str.STR_PERFORMANCE_DETAIL_STATIONS_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,    68,    87, 0x0,Str.STR_PERFORMANCE_DETAIL_MIN_PROFIT_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,    88,   107, 0x0,Str.STR_PERFORMANCE_DETAIL_MIN_INCOME_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,   108,   127, 0x0,Str.STR_PERFORMANCE_DETAIL_MAX_INCOME_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,   128,   147, 0x0,Str.STR_PERFORMANCE_DETAIL_DELIVERED_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,   148,   167, 0x0,Str.STR_PERFORMANCE_DETAIL_CARGO_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,   168,   187, 0x0,Str.STR_PERFORMANCE_DETAIL_MONEY_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,   188,   207, 0x0,Str.STR_PERFORMANCE_DETAIL_LOAN_TIP),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   298,   208,   227, 0x0,Str.STR_PERFORMANCE_DETAIL_TOTAL_TIP),

			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,    38,    14,    26, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    39,    75,    14,    26, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    76,   112,    14,    26, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   113,   149,    14,    26, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   150,   186,    14,    26, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   187,   223,    14,    26, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   224,   260,    14,    26, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   261,   297,    14,    26, 0x0,Str.STR_704F_CLICK_HERE_TO_TOGGLE_COMPANY),
	};

	static final WindowDesc _performance_rating_detail_desc = new WindowDesc(
			-1, -1, 299, 228,
			Window.WC_PERFORMANCE_DETAIL,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_performance_rating_detail_widgets,
			GraphGui::PerformanceRatingDetailWndProc
			);

	static void ShowPerformanceRatingDetail()
	{
		Window.AllocateWindowDescFront(_performance_rating_detail_desc, 0);
	}


	static int _num_sign_sort;

	//static char _bufcache[64];
	//static int _last_sign_idx;

	static class SignNameSorter implements Comparator<Integer>
	{
		public int  compare(Integer cmp1, Integer cmp2)	
		{
			//char buf1[64];
			SignStruct ss;

			ss = SignStruct.GetSign(cmp1);
			String buf1 = Strings.GetString(ss.getString());

			ss = SignStruct.GetSign(cmp2);
			String buf2 = Strings.GetString(ss.getString());

			return buf1.compareTo(buf2);
		}
	}

	static Integer [] _sign_sort;

	static void GlobalSortSignList()
	{
		//final SignStruct ss;
		int [] n = {0};

		_num_sign_sort = 0;

		/* Create array for sorting */
		_sign_sort = new Integer[SignStruct.GetSignPoolSize()]; //realloc(_sign_sort, GetSignPoolSize() * sizeof(_sign_sort[0]));
		if (_sign_sort == null)
			Global.error("Could not allocate memory for the sign-sorting-list");

		SignStruct.forEach( (ss) ->
		{
			if(ss.getString().id != Str.STR_NULL) {
				_sign_sort[n[0]++] = ss.getIndex();
				_num_sign_sort++;
			}
		});

		//qsort(_sign_sort, n, sizeof(_sign_sort[0]), SignNameSorter);

		Arrays.sort( _sign_sort, new SignNameSorter() );

		SignStruct._sign_sort_dirty = false;

		Global.DEBUG_misc( 1, "Resorting global sign list...");
	}

	static void SignListWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int y = 16; // offset from top of widget

			if (SignStruct._sign_sort_dirty)
				GlobalSortSignList();

			w.SetVScrollCount( _num_sign_sort);

			Global.SetDParam(0, w.vscroll.getCount());
			w.DrawWindowWidgets();

			/* No signs? */
			if (w.vscroll.getCount() == 0) {
				Gfx.DrawString(2, y, Str.STR_304A_NONE, 0);
				return;
			}

			{	
				// SignStruct ss;
				int i;

				/* Start drawing the signs */
				for (i = w.vscroll.pos; i < w.vscroll.getCap() + w.vscroll.pos && i < w.vscroll.getCount(); i++) 
				{
					SignStruct ss = SignStruct.GetSign(_sign_sort[i]);

					if (ss.getOwner().isNotNone())
						DrawPlayerIcon(ss.getOwner().id, 4, y + 1);

					Gfx.DrawString(22, y, ss.getString(), 8);
					y += 10;
				}
			}
		} break;

		case WE_CLICK: {
			switch (e.widget) {
			case 3: {
				int id_v = (e.pt.y - 15) / 10;
				SignStruct ss;

				if (id_v >= w.vscroll.getCap())
					return;

				id_v += w.vscroll.pos;

				if (id_v >= w.vscroll.getCount())
					return;

				ss = SignStruct.GetSign(_sign_sort[id_v]);
				ViewPort.ScrollMainWindowToTile(ss.getTile());
			} break;
			}
		} break;

		case WE_RESIZE:
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 10);
			break;
		default:
			break;
		}
	}

	static final Widget _sign_list_widget[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,									Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   345,     0,    13, Str.STR_SIGN_LIST_CAPTION,		Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   346,   357,     0,    13, 0x0,											Str.STR_STICKY_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_RB,    14,     0,   345,    14,   137, 0x0,											Str.STR_NULL),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    14,   346,   357,    14,   125, 0x0,											Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   346,   357,   126,   137, 0x0,											Str.STR_RESIZE_BUTTON),
	};

	static final WindowDesc _sign_list_desc = new WindowDesc(
			-1, -1, 358, 138,
			Window.WC_SIGN_LIST,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_sign_list_widget,
			GraphGui::SignListWndProc
			);


	static void ShowSignList()
	{
		Window w;

		w = Window.AllocateWindowDescFront(_sign_list_desc, 0);
		if (w != null) {
			w.vscroll.setCap(12);
			w.resize.step_height = 10;
			w.resize.height = w.height - 10 * 7; // minimum if 5 in the list
		}
	}


}
