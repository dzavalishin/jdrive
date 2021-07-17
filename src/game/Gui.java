public class Gui
{


//enum { // max 32 - 4 = 28 types
	static final int GUI_PlaceProc_DemolishArea    = 0 << 4;
	static final int GUI_PlaceProc_LevelArea       = 1 << 4;
	static final int GUI_PlaceProc_DesertArea      = 2 << 4;
	static final int GUI_PlaceProc_WaterArea       = 3 << 4;
	static final int GUI_PlaceProc_ConvertRailArea = 4 << 4;
	static final int GUI_PlaceProc_RockyArea       = 5 << 4;
//};


/*	FIOS_TYPE_FILE, FIOS_TYPE_OLDFILE etc. different colours */
static byte _fios_colors[];


//enum {
	static final int ZOOM_IN = 0;
	static final int ZOOM_OUT = 1;
	static final int ZOOM_NONE = 2; // hack, used to update the button status
//};


/* main_gui.c */
 byte _station_show_coverage;
 PlaceProc *_place_proc;






void SetupColorsAndInitialWindow()
{
	uint i;
	Window w;
	int width,height;

	for (i = 0; i != 16; i++) {
		const byte* b = GetNonSprite(0x307 + i);

		assert(b);
		_color_list[i] = *(const ColorList*)(b + 0xC6);
	}

	width = _screen.width;
	height = _screen.height;

	// XXX: these are not done
	switch (Global._game_mode) {
	case GameModes.GM_MENU:
		w = AllocateWindow(0, 0, width, height, MainWindowWndProc, WC_MAIN_WINDOW, NULL);
		AssignWindowViewport(w, 0, 0, width, height, TileXY(32, 32), 0);
		ShowSelectGameWindow();
		break;
	case GM_NORMAL:
		w = AllocateWindow(0, 0, width, height, MainWindowWndProc, WC_MAIN_WINDOW, NULL);
		AssignWindowViewport(w, 0, 0, width, height, TileXY(32, 32), 0);

		ShowVitalWindows();

		// Bring joining GUI to front till the client is really joined 
		if (_networking && !_network_server)
			ShowJoinStatusWindowAfterJoin();

		break;
	case GM_EDITOR:
		w = AllocateWindow(0, 0, width, height, MainWindowWndProc, WC_MAIN_WINDOW, NULL);
		AssignWindowViewport(w, 0, 0, width, height, 0, 0);

		w = AllocateWindowDesc(&_toolb_scen_desc);
		w->disabled_state = 1 << 9;
		CLRBITS(w->flags4, WF_WHITE_BORDER_MASK);

		PositionMainToolbar(w); // already WC_MAIN_TOOLBAR passed (&_toolb_scen_desc)
		break;
	default:
		NOT_REACHED();
	}
}


}