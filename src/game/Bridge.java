package game;

import game.Widget;
import game.Window.WindowEvent;

/** Struct containing information about a single bridge type
 */

public class Bridge 
{
	byte avail_year;     ///< the year in which the bridge becomes available
	byte min_length;     ///< the minimum length of the bridge (not counting start and end tile)
	byte max_length;     ///< the maximum length of the bridge (not counting start and end tile)
	int price;        ///< the relative price of the bridge
	int speed;        ///< maximum travel speed
	PalSpriteID sprite;  ///< the sprite which is used in the GUI (possibly with a recolor sprite)
	StringID material;   ///< the string that contains the bridge description
	//PalSpriteID **sprite_table; ///< table of sprites for drawing the bridge
	PalSpriteID [][] sprite_table; ///< table of sprites for drawing the bridge
	byte flags;          ///< bit 0 set: disable drawing of far pillars.

	static public final int MAX_BRIDGES = 13;

	
	final static Bridge orig_bridge[] = new Bridge[MAX_BRIDGES];
	static Bridge _bridge[] = new Bridge[MAX_BRIDGES];


	static BridgeData  _bridgedata;

	void CcBuildBridge(boolean success, TileIndex tile, int p1, int p2)
	{
		// TODO if (success) SndPlayTileFx(SoundFx.SND_27_BLACKSMITH_ANVIL, tile);
	}

	static void BuildBridge(Window w, int i)
	{
		w.DeleteWindow();
		Cmd.DoCommandP(_bridgedata.end_tile, _bridgedata.start_tile,
			_bridgedata.indexes[i] | (_bridgedata.type << 8), CcBuildBridge,
			Cmd.CMD_BUILD_BRIDGE | Cmd.CMD_AUTO | Cmd.CMD_MSG(STR_5015_CAN_T_BUILD_BRIDGE_HERE));
	}

	static void BuildBridgeWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			int i;

			w.DrawWindowWidgets();

			for (i = 0; i < 4 && i + w.vscroll.pos < _bridgedata.count; i++) {
				final Bridge b = _bridge[_bridgedata.indexes[i + w.vscroll.pos]];

				Global.SetDParam(2, _bridgedata.costs[i + w.vscroll.pos]);
				Global.SetDParam(1, (b.speed >> 4) * 10);
				Global.SetDParam(0, b.material);
				Gfx.DrawSprite(b.sprite.id, 3, 15 + i * 22);

				Gfx.DrawString(44, 15 + i * 22 , STR_500D, 0);
			}
		} break;

		case WE_KEYPRESS: {
			int i = e.keycode - '1';
			if (i < 9 && i < _bridgedata.count) {
				e.cont = false;
				BuildBridge(w, i);
			}

			break;
		}

		case WE_CLICK:
			if (e.widget == 2) {
				int ind = ((int)e.pt.y - 14) / 22;
				if (ind < 4 && (ind += w.vscroll.pos) < _bridgedata.count)
					BuildBridge(w, ind);
			}
			break;
		}
	}

	static final Widget _build_bridge_widgets[] = {
	new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, STR_00C5,										STR_018B_CLOSE_WINDOW),
	new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   199,     0,    13, STR_100D_SELECT_RAIL_BRIDGE,	STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(     Window.WWT_MATRIX,   Window.RESIZE_NONE,     7,     0,   187,    14,   101, 0x401,												STR_101F_BRIDGE_SELECTION_CLICK),
	new Widget(  Window.WWT_SCROLLBAR,   Window.RESIZE_NONE,     7,   188,   199,    14,   101, 0x0,													STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget(   WIDGETS_END),
	};

	static final WindowDesc _build_bridge_desc = new WindowDesc(
		-1, -1, 200, 102,
		Window.WC_BUILD_BRIDGE,Window.WC_BUILD_TOOLBAR,
		WDF_STD_TOOLTIPS | WDF_STD_BTN | WDF_DEF_WIDGET,
		_build_bridge_widgets,
		Bridge::BuildBridgeWndProc
	);


	static final Widget _build_road_bridge_widgets[] = {
	new Widget(   WWT_CLOSEBOX,   Window.RESIZE_NONE,     7,     0,    10,     0,    13, STR_00C5,										STR_018B_CLOSE_WINDOW),
	new Widget(    WWT_CAPTION,   Window.RESIZE_NONE,     7,    11,   199,     0,    13, STR_1803_SELECT_ROAD_BRIDGE,	STR_018C_WINDOW_TITLE_DRAG_THIS),
	new Widget(     WWT_MATRIX,   Window.RESIZE_NONE,     7,     0,   187,    14,   101, 0x401,												STR_101F_BRIDGE_SELECTION_CLICK),
	new Widget(  WWT_SCROLLBAR,   Window.RESIZE_NONE,     7,   188,   199,    14,   101, 0x0,													STR_0190_SCROLL_BAR_SCROLLS_LIST),
	new Widget(   WIDGETS_END),
	};

	static final WindowDesc _build_road_bridge_desc = new WindowDesc(
		-1, -1, 200, 102,
		WC_BUILD_BRIDGE,WC_BUILD_TOOLBAR,
		WDF_STD_TOOLTIPS | WDF_STD_BTN | WDF_DEF_WIDGET,
		Bridge::_build_road_bridge_widgets,
		Bridge::BuildBridgeWndProc
	);


	void ShowBuildBridgeWindow(TileIndex start, TileIndex end, byte bridge_type)
	{
		int j = 0;
		int ret;
		StringID errmsg;

		Window.DeleteWindowById(Window.WC_BUILD_BRIDGE, 0);

		_bridgedata.type = bridge_type;
		_bridgedata.start_tile = start;
		_bridgedata.end_tile = end;

		errmsg = INVALID_STRING_ID;

		// only query bridge building possibility once, result is the same for all bridges!
		// returns CMD_ERROR on failure, and price on success
		ret = DoCommandByTile(end, start, (bridge_type << 8), Cmd.DC_AUTO | Cmd.DC_QUERY_COST, Cmd.CMD_BUILD_BRIDGE);

		if (Cmd.CmdFailed(ret)) {
			errmsg = new StringID(Global._error_message);
		} else {
			// check which bridges can be built
			int bridge_len;			// length of the middle parts of the bridge
			int tot_bridgedata_len;	// total length of bridge

			// get absolute bridge length
			bridge_len = GetBridgeLength(start, end);
			tot_bridgedata_len = bridge_len + 2;

			tot_bridgedata_len = CalcBridgeLenCostFactor(tot_bridgedata_len);

			for (bridge_type = 0; bridge_type != MAX_BRIDGES; bridge_type++) {	// loop for all bridgetypes
				if (CheckBridge_Stuff(bridge_type, bridge_len)) {
					final Bridge b = _bridge[bridge_type];
					// bridge is accepted, add to list
					// add to terraforming & bulldozing costs the cost of the bridge itself (not computed with DC_QUERY_COST)
					_bridgedata.costs[j] = ret + (((long)tot_bridgedata_len * _price.build_bridge * b.price) >> 8);
					_bridgedata.indexes[j] = bridge_type;
					j++;
				}
			}
		}

		_bridgedata.count = j;

		if (j != 0) {
			Window w = Window.AllocateWindowDesc( 0 != (_bridgedata.type & 0x80) ? _build_road_bridge_desc : _build_bridge_desc, 0 );
			w.vscroll.cap = 4;
			w.vscroll.count = (byte)j;
		} else {
			ShowErrorMessage(errmsg, STR_5015_CAN_T_BUILD_BRIDGE_HERE, end.TileX() * 16, end.TileY() * 16);
		}
	}

	
	
}
