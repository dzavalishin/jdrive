package game;

import game.util.Prices;

public class Engine {
	int intro_date;
	int age;
	int reliability;
	int reliability_spd_dec;
	int reliability_start, reliability_max, reliability_final;
	int duration_phase_1, duration_phase_2, duration_phase_3;
	byte lifelength;
	byte flags;
	byte preview_player;
	byte preview_wait;
	byte railtype;
	byte player_avail;

	// type, ie Vehicle.VEH_Road, Vehicle.VEH_Train, etc. Same as in vehicle.h
	byte type;				

	static public final int INVALID_ENGINE  = Vehicle.INVALID_ENGINE;
	static public final EngineID INVALID_ENGINE_ID  = new EngineID( Vehicle.INVALID_ENGINE );



	public static final int CALLBACK_FAILED = 0xFFFF;















	//static StringID GetEngineCategoryName(EngineID engine)
	static int GetEngineCategoryName(int engine)
	{
		if (engine < Global.NUM_TRAIN_ENGINES) {
			switch (GetEngine(engine).railtype) {
			case RAILTYPE_RAIL:   return Str.STR_8102_RAILROAD_LOCOMOTIVE;
			case RAILTYPE_MONO:   return Str.STR_8106_MONORAIL_LOCOMOTIVE;
			case RAILTYPE_MAGLEV: return Str.STR_8107_MAGLEV_LOCOMOTIVE;
			}
		}

		if (engine < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES)
			return Str.STR_8103_ROAD_VEHICLE;

		if (engine < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES + Global.NUM_SHIP_ENGINES)
			return Str.STR_8105_SHIP;

		return Str.STR_8104_AIRCRAFT;
	}

	static final Widget _engine_preview_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     5,     0,    10,     0,    13, Str.STR_00C5,			Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,     5,    11,   299,     0,    13, Str.STR_8100_MESSAGE_FROM_VEHICLE_MANUFACTURE, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,     5,     0,   299,    14,   191, 0x0,						Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     5,    85,   144,   172,   183, Str.STR_00C9_NO,		Str.STR_NULL),
			new Widget( Window.WWT_PUSHTXTBTN,   Window.RESIZE_NONE,     5,   155,   214,   172,   183, Str.STR_00C8_YES,	Str.STR_NULL),
	};


	//static void DrawTrainEngineInfo(EngineID engine, int x, int y, int maxw);
	//static void DrawRoadVehEngineInfo(EngineID engine, int x, int y, int maxw);
	//static void DrawShipEngineInfo(EngineID engine, int x, int y, int maxw);
	//static void DrawAircraftEngineInfo(EngineID engine, int x, int y, int maxw);

	static final DrawEngineInfo _draw_engine_list[] = {
			{DrawTrainEngine,DrawTrainEngineInfo},
			{DrawRoadVehEngine,DrawRoadVehEngineInfo},
			{DrawShipEngine,DrawShipEngineInfo},
			{DrawAircraftEngine,DrawAircraftEngineInfo},
	};

	static void EnginePreviewWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			//EngineID engine = w.window_number;
			int engine = w.window_number;
			final DrawEngineInfo dei;
			int width;

			w.DrawWindowWidgets();

			Global.SetDParam(0, GetEngineCategoryName(engine));
			Gfx.DrawStringMultiCenter(150, 44, Str.STR_8101_WE_HAVE_JUST_DESIGNED_A, 296);

			Gfx.DrawStringCentered(w.width >> 1, 80, GetCustomEngineName(engine), 0x10);

			if(engine < Global.NUM_TRAIN_ENGINES) 
				dei = _draw_engine_list[0];
			else if(engine < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES)
				dei = _draw_engine_list[1];
			if(engine < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES + Global.NUM_SHIP_ENGINES)
				dei = _draw_engine_list[2];
			else
				dei = _draw_engine_list[3];

			width = w.width;
			dei.engine_proc(width >> 1, 100, engine, 0);
			dei.info_proc(engine, width >> 1, 130, width - 52);
			break;
		}

		case WE_CLICK:
			switch (e.widget) {
			case 3:
				w.DeleteWindow();
				break;

			case 4:
				Cmd.DoCommandP(0, w.window_number, 0, null, Cmd.CMD_WANT_ENGINE_PREVIEW);
				w.DeleteWindow();
				break;
			}
			break;
		}
	}

	static final WindowDesc _engine_preview_desc = new WindowDesc(
			Window.WDP_CENTER, Window.WDP_CENTER, 300, 192,
			Window.WC_ENGINE_PREVIEW,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_engine_preview_widgets,
			Engine::EnginePreviewWndProc
	);


	//void ShowEnginePreviewWindow(EngineID engine)
	void ShowEnginePreviewWindow(int engine)
	{
		Window w;

		w = Window.AllocateWindowDesc(_engine_preview_desc,0);
		w.window_number = engine;
	}

	static void DrawTrainEngineInfo(EngineID engine, int x, int y, int maxw)
	{
		final RailVehicleInfo rvi = RailVehInfo(engine);
		int multihead = (rvi.flags & RVI_MULTIHEAD) ? 1 : 0;

		Global.SetDParam(0, (Global._price.build_railvehicle >> 3) * rvi.base_cost >> 5);
		Global.SetDParam(2, rvi.max_speed * 10 >> 4);
		Global.SetDParam(3, rvi.power << multihead);
		Global.SetDParam(1, rvi.weight << multihead);

		Global.SetDParam(4, rvi.running_cost_base * Global._price.running_rail[rvi.engclass] >> 8 << multihead);

		if (rvi.capacity != 0) {
			Global.SetDParam(5, Global._cargoc.names_long[rvi.cargo_type]);
			Global.SetDParam(6, rvi.capacity << multihead);
		} else {
			Global.SetDParam(5, Str.STR_8838_N_A);
		}
		Gfx.DrawStringMultiCenter(x, y, Str.STR_885B_COST_WEIGHT_T_SPEED_POWER, maxw);
	}

	static void DrawNewsNewTrainAvail(Window w)
	{
		EngineID engine;

		NewsItem.DrawNewsBorder(w);

		engine = w.as_news_d().ni.string_id;
		Global.SetDParam(0, GetEngineCategoryName(engine));
		Gfx.DrawStringMultiCenter(w.width >> 1, 20, Str.STR_8859_NEW_NOW_AVAILABLE, w.width - 2);

		Gfx.GfxFillRect(25, 56, w.width - 25, w.height - 2, 10);

		Global.SetDParam(0, GetCustomEngineName(engine));
		Gfx.DrawStringMultiCenter(w.width >> 1, 57, Str.STR_885A, w.width - 2);

		DrawTrainEngine(w.width >> 1, 88, engine, 0);
		Gfx.GfxFillRect(25, 56, w.width - 56, 112, 0x323 | Sprite.USE_COLORTABLE);
		DrawTrainEngineInfo(engine, w.width >> 1, 129, w.width - 52);
	}

	//StringID GetNewsStringNewTrainAvail(final NewsItem ni)
	static int GetNewsStringNewTrainAvail(final NewsItem ni)
	{
		EngineID engine = ni.string_id;
		Global.SetDParam(0, Str.STR_8859_NEW_NOW_AVAILABLE);
		Global.SetDParam(1, GetEngineCategoryName(engine));
		Global.SetDParam(2, GetCustomEngineName(engine));
		return Str.STR_02B6;
	}

	static void DrawAircraftEngineInfo(EngineID engine, int x, int y, int maxw)
	{
		final AircraftVehicleInfo avi = AircraftVehInfo(engine);
		Global.SetDParam(0, (Global._price.aircraft_base >> 3) * avi.base_cost >> 5);
		Global.SetDParam(1, avi.max_speed << 3);
		Global.SetDParam(2, avi.passenger_capacity);
		Global.SetDParam(3, avi.mail_capacity);
		Global.SetDParam(4, avi.running_cost * Global._price.aircraft_running >> 8);

		Gfx.DrawStringMultiCenter(x, y, Str.STR_A02E_COST_MAX_SPEED_CAPACITY, maxw);
	}

	static void DrawNewsNewAircraftAvail(Window w)
	{
		EngineID engine;

		NewsItem.DrawNewsBorder(w);

		engine = w.as_news_d().ni.string_id;

		Gfx.DrawStringMultiCenter(w.width >> 1, 20, Str.STR_A02C_NEW_AIRCRAFT_NOW_AVAILABLE, w.width - 2);
		Gfx.GfxFillRect(25, 56, w.width - 25, w.height - 2, 10);

		Global.SetDParam(0, GetCustomEngineName(engine));
		Gfx.DrawStringMultiCenter(w.width >> 1, 57, Str.STR_A02D, w.width - 2);

		DrawAircraftEngine(w.width >> 1, 93, engine, 0);
		Gfx.GfxFillRect(25, 56, w.width - 56, 110, 0x323 | USE_COLORTABLE);
		DrawAircraftEngineInfo(engine, w.width >> 1, 131, w.width - 52);
	}

	static int GetNewsStringNewAircraftAvail(final NewsItem ni)
	{
		EngineID engine = ni.string_id;
		Global.SetDParam(0, Str.STR_A02C_NEW_AIRCRAFT_NOW_AVAILABLE);
		Global.SetDParam(1, GetCustomEngineName(engine));
		return Str.STR_02B6;
	}

	static void DrawRoadVehEngineInfo(EngineID engine, int x, int y, int maxw)
	{
		final RoadVehicleInfo rvi = RoadVehInfo(engine);

		Global.SetDParam(0, (Global._price.roadveh_base >> 3) * rvi.base_cost >> 5);
		Global.SetDParam(1, rvi.max_speed * 10 >> 5);
		Global.SetDParam(2, rvi.running_cost * Global._price.roadveh_running >> 8);

		Global.SetDParam(4, rvi.capacity);
		Global.SetDParam(3, _cargoc.names_long[rvi.cargo_type]);

		Gfx.DrawStringMultiCenter(x, y, Str.STR_902A_COST_SPEED_RUNNING_COST, maxw);
	}

	static void DrawNewsNewRoadVehAvail(Window w)
	{
		EngineID engine;

		NewsItem.DrawNewsBorder(w);

		engine = w.as_news_d().ni.string_id;
		Gfx.DrawStringMultiCenter(w.width >> 1, 20, Str.STR_9028_NEW_ROAD_VEHICLE_NOW_AVAILABLE, w.width - 2);
		Gfx.GfxFillRect(25, 56, w.width - 25, w.height - 2, 10);

		Global.SetDParam(0, GetCustomEngineName(engine));
		Gfx.DrawStringMultiCenter(w.width >> 1, 57, Str.STR_9029, w.width - 2);

		DrawRoadVehEngine(w.width >> 1, 88, engine, 0);
		Gfx.GfxFillRect(25, 56, w.width - 56, 112, 0x323 | Sprite.USE_COLORTABLE);
		DrawRoadVehEngineInfo(engine, w.width >> 1, 129, w.width - 52);
	}

	static /*StringID*/ int GetNewsStringNewRoadVehAvail(final NewsItem ni)
	{
		EngineID engine = ni.string_id;
		Global.SetDParam(0, Str.STR_9028_NEW_ROAD_VEHICLE_NOW_AVAILABLE);
		Global.SetDParam(1, GetCustomEngineName(engine));
		return Str.STR_02B6;
	}

	static void DrawShipEngineInfo(EngineID engine, int x, int y, int maxw)
	{
		final ShipVehicleInfo svi = ShipVehInfo(engine);
		Global.SetDParam(0, svi.base_cost * (Global._price.ship_base >> 3) >> 5);
		Global.SetDParam(1, svi.max_speed * 10 >> 5);
		Global.SetDParam(2, _cargoc.names_long[svi.cargo_type]);
		Global.SetDParam(3, svi.capacity);
		Global.SetDParam(4, svi.running_cost * Global._price.ship_running >> 8);
		Gfx.DrawStringMultiCenter(x, y, Str.STR_982E_COST_MAX_SPEED_CAPACITY, maxw);
	}

	static void DrawNewsNewShipAvail(Window w)
	{
		EngineID engine;

		NewsItem.DrawNewsBorder(w);

		engine = w.as_news_d().ni.string_id;

		Gfx.DrawStringMultiCenter(w.width >> 1, 20, Str.STR_982C_NEW_SHIP_NOW_AVAILABLE, w.width - 2);
		Gfx.GfxFillRect(25, 56, w.width - 25, w.height - 2, 10);

		Global.SetDParam(0, GetCustomEngineName(engine));
		Gfx.DrawStringMultiCenter(w.width >> 1, 57, Str.STR_982D, w.width - 2);

		DrawShipEngine(w.width >> 1, 93, engine, 0);
		Gfx.GfxFillRect(25, 56, w.width - 56, 110, 0x323 | Sprite.USE_COLORTABLE);
		DrawShipEngineInfo(engine, w.width >> 1, 131, w.width - 52);
	}

	static int GetNewsStringNewShipAvail(final NewsItem ni)
	{
		EngineID engine = ni.string_id;
		Global.SetDParam(0, Str.STR_982C_NEW_SHIP_NOW_AVAILABLE);
		Global.SetDParam(1, GetCustomEngineName(engine));
		return Str.STR_02B6;
	}


}



/*
typedef void DrawEngineProc(int x, int y, EngineID engine, int image_ormod);
typedef void DrawEngineInfoProc(EngineID, int x, int y, int maxw);

class DrawEngineInfo {
	DrawEngineProc *engine_proc;
	DrawEngineInfoProc *info_proc;
} 

*/