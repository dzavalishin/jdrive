public class Gui
{

/* main_gui.c */
void SetupColorsAndInitialWindow();
void CcPlaySound10(boolean success, TileIndex tile, int p1, int p2);
void CcBuildCanal(boolean success, TileIndex tile, int p1, int p2);
void CcTerraform(boolean success, TileIndex tile, int p1, int p2);

/* settings_gui.c */
void ShowGameOptions();
void ShowGameDifficulty();
void ShowPatchesSelection();
void ShowNewgrf();
void ShowCustCurrency();

/* graph_gui.c */
void ShowOperatingProfitGraph();
void ShowIncomeGraph();
void ShowDeliveredCargoGraph();
void ShowPerformanceHistoryGraph();
void ShowCompanyValueGraph();
void ShowCargoPaymentRates();
void ShowCompanyLeagueTable();
void ShowPerformanceRatingDetail();

/* news_gui.c */
void ShowLastNewsMessage();
void ShowMessageOptions();
void ShowMessageHistory();

/* traintoolb_gui.c */
void ShowBuildRailToolbar(RailType railtype, int button);
void PlaceProc_BuyLand(TileIndex tile);

/* train_gui.c */
void ShowPlayerTrains(PlayerID player, StationID station);
void ShowTrainViewWindow(const Vehicle *v);
void ShowOrdersWindow(const Vehicle* v);

void ShowRoadVehViewWindow(const Vehicle* v);

/* road_gui.c */
void ShowBuildRoadToolbar();
void ShowBuildRoadScenToolbar();
void ShowPlayerRoadVehicles(PlayerID player, StationID station);

/* dock_gui.c */
void ShowBuildDocksToolbar();
void ShowPlayerShips(PlayerID player, StationID station);

void ShowShipViewWindow( Vehicle v);

/* aircraft_gui.c */
void ShowBuildAirToolbar();
void ShowPlayerAircraft(PlayerID player, StationID station);

/* terraform_gui.c */
void ShowTerraformToolbar();

void PlaceProc_DemolishArea(TileIndex tile);
void PlaceProc_LowerLand(TileIndex tile);
void PlaceProc_RaiseLand(TileIndex tile);
void PlaceProc_LevelLand(TileIndex tile);
boolean GUIPlaceProcDragXY(const WindowEvent *we);

//enum { // max 32 - 4 = 28 types
	static final int GUI_PlaceProc_DemolishArea    = 0 << 4;
	static final int GUI_PlaceProc_LevelArea       = 1 << 4;
	static final int GUI_PlaceProc_DesertArea      = 2 << 4;
	static final int GUI_PlaceProc_WaterArea       = 3 << 4;
	static final int GUI_PlaceProc_ConvertRailArea = 4 << 4;
	static final int GUI_PlaceProc_RockyArea       = 5 << 4;
//};

/* misc_gui.c */
void PlaceLandBlockInfo();
void ShowAboutWindow();
void ShowBuildTreesToolbar();
void ShowBuildTreesScenToolbar();
void ShowTownDirectory();
void ShowIndustryDirectory();
void ShowSubsidiesList();
void ShowPlayerStations(PlayerID player);
void ShowPlayerFinances(PlayerID player);
void ShowPlayerCompany(PlayerID player);
void ShowSignList();

void ShowEstimatedCostOrIncome(int cost, int x, int y);
void ShowErrorMessage(StringID msg_1, StringID msg_2, int x, int y);

void DrawStationCoverageAreaText(int sx, int sy, int mask,int rad);
void CheckRedrawStationCoverage(const Window* w);

void ShowSmallMap();
void ShowExtraViewPortWindow();
void SetVScrollCount(Window *w, int num);
void SetVScroll2Count(Window *w, int num);
void SetHScrollCount(Window *w, int num);

void ShowCheatWindow();
void AskForNewGameToStart();

void DrawEditBox(Window *w, int wid);
void HandleEditBox(Window *w, int wid);
int HandleEditBoxKey(Window *w, int wid, WindowEvent *we);
boolean HandleCaret(Textbuf *tb);

void DeleteTextBufferAll(Textbuf *tb);
boolean DeleteTextBufferChar(Textbuf *tb, int delmode);
boolean InsertTextBufferChar(Textbuf *tb, byte key);
boolean InsertTextBufferClipboard(Textbuf *tb);
boolean MoveTextBufferPos(Textbuf *tb, int navmode);
void UpdateTextBufferSize(Textbuf *tb);

void BuildFileList();
void SetFiosType( byte fiostype);

/*	FIOS_TYPE_FILE, FIOS_TYPE_OLDFILE etc. different colours */
static byte _fios_colors[];

/* network gui */
void ShowNetworkGameWindow();
void ShowChatWindow(StringID str, StringID caption, int maxlen, int maxwidth, WindowClass window_class, WindowNumber window_number);

/* bridge_gui.c */
void ShowBuildBridgeWindow(uint start, uint end, byte type);

//enum {
	static final int ZOOM_IN = 0;
	static final int ZOOM_OUT = 1;
	static final int ZOOM_NONE = 2; // hack, used to update the button status
//};

boolean DoZoomInOutWindow(int how, Window * w);
void ShowBuildIndustryWindow();
void ShowQueryString(StringID str, StringID caption, uint maxlen, uint maxwidth, WindowClass window_class, WindowNumber window_number);
void ShowMusicWindow();

/* main_gui.c */
 byte _station_show_coverage;
 PlaceProc *_place_proc;

/* vehicle_gui.c */
void InitializeGUI();

/* m_airport.c */
void MA_AskAddAirport();

}