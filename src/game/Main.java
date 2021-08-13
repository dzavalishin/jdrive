package game;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import game.ai.Ai;
import game.util.FileIO;
import game.util.Strings;

public class Main {

	private static SmallFiosItem _file_to_saveload = new SmallFiosItem();



	/* TODO: usrerror() for errors which are not of an internal nature but
	 * caused by the user, i.e. missing files or fatal configuration errors.
	 * Post-0.4.0 since Celestar doesn't want this in SVN before. --pasky */

	
	static void  error( String  s, Object ... args)
	{
		String buf = String.format(s, args);

		Global.hal.ShowOSErrorBox(buf);
		Global.hal.stop_video();

		assert(false);
		System.exit(1);
	}

	void  ShowInfoF( String str, Object ... args)
	{
		String buf = String.format(str, args);
		Hal.ShowInfo(buf);
	}


	public static byte [] ReadFileToMem( String filename, int maxsize)
	{
		byte [] buf = new byte[maxsize];
		if( buf == null )
		{
			error("ReadFileToMem: out of memory");
			return null;
		}

		RandomAccessFile f;
		try {
			f = new RandomAccessFile(filename,"r" );
		} catch (FileNotFoundException e) {
			error("ReadFileToMem: no file '%s'", filename );
			return null;
		}

		int len;
		try {
			len = f.read(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally
		{
			try {
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}


		if( len < 0 )
		{
			error("ReadFileToMem: no data");
			return null;
		}

		byte ret[] = new byte[len];
		System.arraycopy(buf, 0, ret, 0, len);
		buf = null;
		return ret;
	}

	static void showhelp()
	{
		//char buf[4096], *p;

		String help =
				"Command line options:\n" +
				"  -v drv              = Set video driver (see below)\n" +
				"  -s drv              = Set sound driver (see below)\n" +
				"  -m drv              = Set music driver (see below)\n" +
				"  -r res              = Set resolution (for instance 800x600)\n" +
				"  -h                  = Display this help text\n" +
				"  -t date             = Set starting date\n" +
				"  -d [[fac=]lvl[,...]]= Debug mode\n" +
				"  -e                  = Start Editor\n" +
				"  -g [savegame]       = Start new/save game immediately\n" +
				"  -G seed             = Set random seed\n" +
				"  -n [ip#player:port] = Start networkgame\n" +
				"  -D                  = Start dedicated server\n" +
				//#if !defined(__MORPHOS__) && !defined(__AMIGA__)
				//"  -f                  = Fork into the background (dedicated only)\n"
				//#endif
				"  -i                  = Force to use the DOS palette (use this if you see a lot of pink)\n" +
				"  -p #player          = Player as #player (deprecated) (network only)\n" +
				"  -c config_file      = Use 'config_file' instead of 'openttd.cfg'\n"
				;

		// TODO GetDriverList(p);

		Hal.ShowInfo(help);
	}





	/*
	static void ParseResolution(int res[2], final String  s)
	{
		String t = strchr(s, 'x');
		if (t == null) {
			ShowInfoF("Invalid resolution '%s'", s);
			return;
		}

		res[0] = BitOps.clamp(strtoul(s, null, 0), 64, MAX_SCREEN_WIDTH);
		res[1] = BitOps.clamp(strtoul(t + 1, null, 0), 64, MAX_SCREEN_HEIGHT);
	}*/

	static void InitializeDynamicVariables()
	{
		// supposed to be unused in jaba code
		/* Dynamic stuff needs to be initialized somewhere... */
		//_station_sort  = null;
		//_vehicle_sort  = null;
		//_town_sort     = null;
		//_industry_sort = null;
	}

	static void UnInitializeDynamicVariables()
	{
		/* Dynamic stuff needs to be free'd somewhere... */
		// TODO do we need?
		/*
		CleanPool(&_town_pool);
		CleanPool(&_industry_pool);
		CleanPool(&_station_pool);
		CleanPool(&_vehicle_pool);
		CleanPool(&_sign_pool);
		CleanPool(&_order_pool);
		*/
		//free(_station_sort);
		//free(_vehicle_sort);
		//free(_town_sort);
		//free(_industry_sort);
	}

	static void UnInitializeGame()
	{
		Window.UnInitWindowSystem();

		//free(_config_file);
	}

	static void LoadIntroGame()
	{
		String filename;

		Global._game_mode = GameModes.GM_MENU;
		//CLRBITS(_display_opt, DO_TRANS_BUILDINGS); // don't make buildings transparent in intro
		Global._display_opt = 0; // TODO BitOps.RETCLRBITS( Global._display_opt, DO_TRANS_BUILDINGS );
		GameOptions._opt_ptr = GameOptions._opt_newgame;

		GfxInit.GfxLoadSprites();
		Gfx.LoadStringWidthTable();

		// Setup main window
		Window.ResetWindowSystem();
		Gui.SetupColorsAndInitialWindow();

		// Generate a world.
		filename = String.format( "%sopntitle.dat",  Global._path.data_dir );
		// TODO if (SaveOrLoad(filename, SL_LOAD) != SL_OK) 
		{
			/*#if defined SECOND_DATA_DIR
			sprintf(filename, "%sopntitle.dat",  _path.second_data_dir);
			if (SaveOrLoad(filename, SL_LOAD) != SL_OK)
	#endif*/
			GenerateWorld.doGenerateWorld(1, 256, 256); // if failed loading, make empty world.
		}

		Global._pause = 0;
		Global._local_player = null;
		Hal.MarkWholeScreenDirty();

		// Play main theme
		// TODO if (_music_driver.is_song_playing()) ResetMusic();
	}


	public static void main(String[] argv) 
	{
		int argc = argv.length;
		//MyGetOptData mgo = new MyGetOptData();
		int i;
		boolean network = false;
		//String network_conn = null;
		//final String optformat;
		//String musicdriver, sounddriver, videodriver;
		int resolution[] = {0,0};
		int startdate = -1;
		boolean dedicated = false;

		//musicdriver = sounddriver = videodriver = null;

		Global._game_mode = GameModes.GM_MENU;
		Global._switch_mode = SwitchModes.SM_MENU;
		Global._switch_mode_errorstr = Str.INVALID_STRING_ID;
		Global._dedicated_forks = false;
		dedicated = false;
		Global._config_file = null;

		// The last param of the following function means this:
		//   a letter means: it accepts that param (e.g.: -h)
		//   a ':' behind it means: it need a param (e.g.: -m<driver>)
		//   a '::' behind it means: it can optional have a param (e.g.: -d<debug>)
		//#if !defined(__MORPHOS__) && !defined(__AMIGA__) && !defined(WIN32)
		//optformat = "bm:s:v:hDfn::eit:d::r:g::G:p:c:";
		//#else
		//optformat = "bm:s:v:hDn::eit:d::r:g::G:p:c:"; // no fork option
		//#endif

		/*
		mgo.MyGetOptInit( argv, optformat);
		
		while ((i = mgo.MyGetOpt()) != -1) {
			switch(i) {
			case 'm': musicdriver = new String( mgo.opt ); break;
			case 's': sounddriver = new String( mgo.opt ); break;
			case 'v': videodriver = new String( mgo.opt ); break;
			case 'D': {
				musicdriver = "null";
				sounddriver = "null";
				videodriver = "dedicated";
				dedicated = true;
			} break;
			case 'f': {
				Global._dedicated_forks = true;
			}; break;
			case 'n': {
				network = true;
				if (mgo.opt != null)
					// Optional, you can give an IP
					network_conn = mgo.opt;
				else
					network_conn = null;
			} break; 
			case 'b': Ai._ai.network_client = true; break;
			//case 'r': ParseResolution(resolution, mgo.opt); break;
			case 't': startdate = Integer.parseInt(mgo.opt); break;
			case 'd': {
				if (mgo.opt != null) SetDebugString(mgo.opt);
			} break;
			case 'e': Global._switch_mode = SwitchModes.SM_EDITOR; break;
			case 'i': Global._use_dos_palette = true; break;
			case 'g':
				if (mgo.opt != null) {
					_file_to_saveload.name = mgo.opt;
					Global._switch_mode = SwitchModes.SM_LOAD;
				} else
					Global._switch_mode = SwitchModes.SM_NEWGAME;
				break;
			case 'G':
				Global._random_seeds[0][0] = Integer.parseInt(mgo.opt);
				break;
			case 'p': {
				int netp = Integer.parseInt(mgo.opt);
				// Play as an other player in network games
				if (BitOps.IS_INT_INSIDE(i, 1, Global.MAX_PLAYERS)) Global._network_playas = (byte) netp;
				break;
			}
			case 'c':
				Global._config_file = new String(mgo.opt);
				break;
			case -2:
			case 'h':
				showhelp();
				return;
			}
		}
		*/
		if (Ai._ai.network_client && !network) {
			Ai._ai.network_client = false;
			Global.DEBUG_ai( 0, "[AI] Can't enable network-AI, because '-n' is not used\n");
		}

		DeterminePaths();
		// GfxInit.CheckExternalFiles();

		//#if defined(UNIX) && !defined(__MORPHOS__)
		// We must fork here, or we'll end up without some resources we need (like sockets)
		// TODO if (Global._dedicated_forks)
		//	DedicatedFork();
		//#endif

		// TODO LoadFromConfig();
		// TODO CheckConfig();
		// TODO LoadFromHighScore();

		// override config?
		// TODO if (musicdriver[0]) ttd_strlcpy(_ini_musicdriver, musicdriver, sizeof(_ini_musicdriver));
		// TODO if (sounddriver[0]) ttd_strlcpy(_ini_sounddriver, sounddriver, sizeof(_ini_sounddriver));
		// TODO if (videodriver[0]) ttd_strlcpy(_ini_videodriver, videodriver, sizeof(_ini_videodriver));
		// TODO if (resolution[0]) { _cur_resolution[0] = resolution[0]; _cur_resolution[1] = resolution[1]; }
		if (startdate != (int)-1) Global._patches.starting_date = startdate;

		if (Global._dedicated_forks && !dedicated)
			Global._dedicated_forks = false;

		Global.hal.start_video("");
		
		// enumerate language files
		Strings.InitializeLanguagePacks();

		// initialize screenshot formats
		// TODO InitializeScreenshotFormats();

		// initialize airport state machines
		AirportFTAClass.InitializeAirports();

		/* initialize all variables that are allocated dynamically */
		InitializeDynamicVariables();

		/* start the AI */
		Ai.AI_Initialize();

		// Sample catalogue
		Global.DEBUG_misc( 1, "Loading sound effects...");
		// TODO MxInitialize(11025);
		// TODO SoundInitialize("sample.cat");

		// This must be done early, since functions use the InvalidateWindow* calls
		Window.InitWindowSystem();

		GfxInit.GfxLoadSprites();
		Gfx.LoadStringWidthTable();

		Global.DEBUG_misc( 1, "Loading drivers...");
		// TODO LoadDriver(SOUND_DRIVER, _ini_sounddriver);
		// TODO LoadDriver(MUSIC_DRIVER, _ini_musicdriver);
		// TODO LoadDriver(VIDEO_DRIVER, _ini_videodriver); // load video last, to prevent an empty window while sound and music loads
		// TODO _savegame_sort_order = SORT_BY_DATE | SORT_DESCENDING;

		// initialize network-core
		// TODO NetworkStartUp();

		GameOptions._opt_ptr = GameOptions._opt_newgame;

		/* XXX - ugly hack, if diff_level is 9, it means we got no setting from the config file */
		if (GameOptions._opt_newgame.diff_level == 9)
			SettingsGui.SetDifficultyLevel(0, GameOptions._opt_newgame);

		// initialize the ingame console
		// TODO Console.IConsoleInit();
		VehicleGui.InitializeGUI();
		// TODO Console.IConsoleCmdExec("exec scripts/autoexec.scr 0");

		GenerateWorld.doGenerateWorld(1, 256, 256); // Make the viewport initialization happy
		/*
		if ((network) && (_network_available)) {
			if (network_conn != null) {
				final String port = null;
				final String player = null;
				uint16 rport;

				rport = NETWORK_DEFAULT_PORT;

				ParseConnectionString(&player, &port, network_conn);

				if (player != null) _network_playas = Integer.parseInt(player);
				if (port != null) rport = Integer.parseInt(port);

				LoadIntroGame();
				Global._switch_mode = SwitchModes.SM_NONE;
				NetworkClientConnectGame(network_conn, rport);
			}
		}
		 */
		
		// [dz] hacked in
		Hal._screen.width = 1024;
		Hal._screen.height = 768;
		
		Global.hal.main_loop();

		// TODO WaitTillSaved();
		// TODO Console.IConsoleFree();

		/*
		if (_network_available) {
			// Shut down the network and close any open connections
			NetworkDisconnect();
			NetworkUDPClose();
			NetworkShutDown();
		}
		 */

		//_video_driver.stop();
		Global.hal.stop_video();
		//_music_driver.stop(); TODO return
		//_sound_driver.stop(); TODO return

		// TODO SaveToConfig();
		// TODO SaveToHighScore();

		// uninitialize airport state machines
		AirportFTAClass.UnInitializeAirports();

		/* uninitialize variables that are allocated dynamic */
		UnInitializeDynamicVariables();

		/* stop the AI */
		Ai.AI_Uninitialize();

		/* Close all and any open filehandles */
		FileIO.FioCloseAll();
		UnInitializeGame();

		//return 0;
	}

	/** Mutex so that only one thread can communicate with the main program
	 * at any given time */
	static ThreadMsg _message = ThreadMsg.MSG_OTTD_SAVETHREAD_ZERO;

	static  void OTTD_ReleaseMutex() {_message = ThreadMsg.MSG_OTTD_SAVETHREAD_ZERO;}
	static  ThreadMsg OTTD_PollThreadEvent() {return _message;}

	/** Called by running thread to execute some action in the main game.
	 * It will stall as long as the mutex is not freed (handled) by the game */
	void OTTD_SendThreadMessage(ThreadMsg msg)
	{
		if (Global._exit_game) return;
		while (_message != ThreadMsg.MSG_OTTD_SAVETHREAD_ZERO) 
			Global.hal.CSleep(10);

		_message = msg;
	}


	/** Handle the user-messages sent to us
	 * @param message message sent
	 */
	static void ProcessSentMessage(ThreadMsg message)
	{
		switch (message) {
		/* TODO MSG
		case MSG_OTTD_SAVETHREAD_START: SaveFileStart(); break;
		case MSG_OTTD_SAVETHREAD_DONE:  SaveFileDone(); break;
		case MSG_OTTD_SAVETHREAD_ERROR: SaveFileError(); break;
		*/
		default: assert false;
		}

		OTTD_ReleaseMutex(); // release mutex so that other threads, messages can be handled
	}

	static void ShowScreenshotResult(boolean b)
	{/* TODO
		if (b) {
			SetDParamStr(0, _screenshot_name);
			Global.ShowErrorMessage(INVALID_STRING_ID, Str.STR_031B_SCREENSHOT_SUCCESSFULLY, 0, 0);
		} else {
			Global.ShowErrorMessage(INVALID_STRING_ID, Str.STR_031C_SCREENSHOT_FAILED, 0, 0);
		}
		*/
	}

	static void MakeNewGame()
	{
		Global._game_mode = GameModes.GM_NORMAL;

		// Copy in game options
		GameOptions._opt_ptr = GameOptions._opt;
		//memcpy(_opt_ptr, &_opt_newgame, sizeof(*_opt_ptr));
		GameOptions._opt_ptr.assign(GameOptions._opt_newgame);

		GfxInit.GfxLoadSprites();

		// Reinitialize windows
		Window.ResetWindowSystem();
		Gfx.LoadStringWidthTable();

		Gui.SetupColorsAndInitialWindow();

		// Randomize world
		GenerateWorld.doGenerateWorld(0, 1<<Global._patches.map_x, 1<<Global._patches.map_y);
		//GenerateWorld.doGenerateWorld(0, 64, 64);

		// In a dedicated server, the server does not play
		if (Global._network_dedicated) {
			Global._local_player = PlayerID.get( Owner.OWNER_SPECTATOR);
		} else {
			// Create a single player
			Player.DoStartupNewPlayer(false);

			Global._local_player = PlayerID.get(0); 
			Global._current_player = Global._local_player;
			Cmd.DoCommandP(null, (Global._patches.autorenew ? (1 << 15) : 0 ) | (Global._patches.autorenew_months << 16) | 4, (int)Global._patches.autorenew_money, null, Cmd.CMD_REPLACE_VEHICLE);
		}

		Hal.MarkWholeScreenDirty();
	}

	static void MakeNewEditorWorld()
	{
		Global._game_mode = GameModes.GM_EDITOR;

		// Copy in game options
		GameOptions._opt_ptr = GameOptions._opt;
		//memcpy(_opt_ptr, &_opt_newgame, sizeof(GameOptions));
		GameOptions._opt_ptr.assign(GameOptions._opt_newgame);

		GfxInit.GfxLoadSprites();

		// Re-init the windowing system
		Window.ResetWindowSystem();

		// Create toolbars
		Gui.SetupColorsAndInitialWindow();

		// Startup the game system
		GenerateWorld.doGenerateWorld(1, 1 << Global._patches.map_x, 1 << Global._patches.map_y);

		Global._local_player = PlayerID.get(Owner.OWNER_NONE);
		Hal.MarkWholeScreenDirty();
	}

	//void StartupPlayers();
	//void StartupDisasters();

	/**
	 * Start Scenario starts a new game based on a scenario.
	 * Eg 'New Game' -. select a preset scenario
	 * This starts a scenario based on your current difficulty settings
	 */
	static void StartScenario()
	{
		/*
		Global._game_mode = GameModes.GM_NORMAL;

		// invalid type
		if (_file_to_saveload.mode == SL_INVALID) {
			Global.error("Savegame is obsolete or invalid format: %s\n", _file_to_saveload.name);
			Global.ShowErrorMessage(INVALID_STRING_ID, Str.STR_4009_GAME_LOAD_FAILED, 0, 0);
			Global._game_mode = GameModes.GM_MENU;
			return;
		}

		GfxInit.GfxLoadSprites();

		// Reinitialize windows
		Window.ResetWindowSystem();
		Gfx.LoadStringWidthTable();

		Gui.SetupColorsAndInitialWindow();

		// Load game
		if (SaveOrLoad(_file_to_saveload.name, _file_to_saveload.mode) != SL_OK) {
			LoadIntroGame();
			Global.ShowErrorMessage(Str.INVALID_STRING_ID, Str.STR_4009_GAME_LOAD_FAILED, 0, 0);
		}

		GameOptions._opt_ptr = GameOptions._opt;
		//memcpy(&_opt_ptr.diff, &_opt_newgame.diff, sizeof(GameDifficulty));
		GameOptions._opt_ptr.diff = GameOptions._opt_newgame.diff.makeClone();
		GameOptions._opt.diff_level = GameOptions._opt_newgame.diff_level;

		// Inititalize data
		Player.StartupPlayers();
		Engine.StartupEngines();
		// TODO StartupDisasters();

		Global._local_player = null;
		Global._current_player = Global._local_player;
		DoCommandP(0, (Global._patches.autorenew ? 1 << 15 : 0 ) | (Global._patches.autorenew_months << 16) | 4, Global._patches.autorenew_money, null, CMD_REPLACE_VEHICLE);

		Global.hal.MarkWholeScreenDirty();
		*/
	}

	static boolean SafeSaveOrLoad(final String filename, int mode, GameModes newgm)
	{
		return false;
		/*
		GameModes ogm = Global._game_mode;
		int r;

		Global._game_mode = newgm;
		r = SaveOrLoad(filename, mode);
		if (r == SL_REINIT) {
			switch (ogm) {
			case GameModes.GM_MENU:   LoadIntroGame();      break;
			case GameModes.GM_EDITOR: MakeNewEditorWorld(); break;
			default:        MakeNewGame();        break;
			}
			return false;
		} else if (r != SL_OK) {
			Global._game_mode = ogm;
			return false;
		} else {
			return true;
		}
		*/
	}

	static void SwitchMode(SwitchModes new_mode)
	{
		/*
		// If we are saving something, the network stays in his current state
		if (new_mode != SM_SAVE) {
			// If the network is active, make it not-active
			if (_networking) {
				if (_network_server && (new_mode == SM_LOAD || new_mode == SM_NEWGAME)) {
					NetworkReboot();
					NetworkUDPClose();
				} else {
					NetworkDisconnect();
					NetworkUDPClose();
				}
			}

			// If we are a server, we restart the server
			if (_is_network_server) {
				// But not if we are going to the menu
				if (new_mode != SM_MENU) {
					NetworkServerStart();
				} else {
					// This client no longer wants to be a network-server
					_is_network_server = false;
				}
			}
		}
 		/* ENABLE_NETWORK */

		switch (new_mode) {
		case SM_EDITOR: /* Switch to scenario editor */
			MakeNewEditorWorld();
			break;

		case SM_NEWGAME: /* New Game -. 'Random game' */
			/*
			if (_network_server)
				snprintf(_network_game_info.map_name, NETWORK_NAME_LENGTH, "Random Map");
			 */
			MakeNewGame();
			break;

		case SM_START_SCENARIO: /* New Game -. Choose one of the preset scenarios */
			/*#ifdef ENABLE_NETWORK
				if (_network_server)
					snprintf(_network_game_info.map_name, NETWORK_NAME_LENGTH, "%s (Loaded scenario)", _file_to_saveload.title);
			/* ENABLE_NETWORK */
			StartScenario();
			break;

		case SM_LOAD: { /* Load game, Play Scenario */
			GameOptions._opt_ptr = GameOptions._opt;

			if (!SafeSaveOrLoad(_file_to_saveload.name, _file_to_saveload.mode, GameModes.GM_NORMAL)) {
				LoadIntroGame();
				Global.ShowErrorMessage(Str.INVALID_STRING_ID.id, Str.STR_4009_GAME_LOAD_FAILED, 0, 0);
			} else {
				Global._local_player = null;
				Cmd.DoCommandP(null, 0, 0, null, Cmd.CMD_PAUSE); // decrease pause counter (was increased from opening load dialog)
				/*
				if (_network_server)
					snprintf(_network_game_info.map_name, NETWORK_NAME_LENGTH, "%s (Loaded game)", _file_to_saveload.title);
				/* ENABLE_NETWORK */
			}
			break;
		}

		case SM_LOAD_SCENARIO: { /* Load scenario from scenario editor */
			if (SafeSaveOrLoad(_file_to_saveload.name, _file_to_saveload.mode, GameModes.GM_EDITOR)) {
				//PlayerID 
				int i;

				GameOptions._opt_ptr = GameOptions._opt;

				Global._local_player = PlayerID.get(Owner.OWNER_NONE);
				Global._generating_world = true;
				// delete all players.
				for (i = 0; i != Global.MAX_PLAYERS; i++) {
					Economy.ChangeOwnershipOfPlayerItems( PlayerID.get(i), PlayerID.get(Owner.OWNER_SPECTATOR));
					Global._players[i].is_active = false;
				}
				Global._generating_world = false;
				// delete all stations owned by a player
				Station.DeleteAllPlayerStations();
			} else {
				Global.ShowErrorMessage(Str.INVALID_STRING_ID.id, Str.STR_4009_GAME_LOAD_FAILED, 0, 0);
			}
			break;
		}


		case SM_MENU: /* Switch to game intro menu */
			LoadIntroGame();
			break;

		case SM_SAVE: /* TODO Save game */
			/*
			if (SaveOrLoad(_file_to_saveload.name, SL_SAVE) != SL_OK) {
				Global.ShowErrorMessage(Str.INVALID_STRING_ID.id, Str.STR_4007_GAME_SAVE_FAILED, 0, 0);
			} else {
				Window.DeleteWindowById(Window.WC_SAVELOAD, 0);
			}*/
			break;

		case SM_GENRANDLAND: /* Generate random land within scenario editor */
			GenerateWorld.doGenerateWorld(2, 1<<Global._patches.map_x, 1<<Global._patches.map_y);
			// XXX: set date
			Global._local_player = PlayerID.get( Owner.OWNER_NONE );
			Hal.MarkWholeScreenDirty();
			break;
		}

		if (Global._switch_mode_errorstr != Str.INVALID_STRING_ID)
			Global.ShowErrorMessage(Str.INVALID_STRING_ID,Global._switch_mode_errorstr,0,0);
	}


	// State controlling game loop.
	// The state must not be changed from anywhere
	// but here.
	// That check is enforced in DoCommand.
	static void StateGameLoop()
	{
		// dont execute the state loop during pause
		if (Global._pause != 0) return;

		// _frame_counter is increased somewhere else when in network-mode
		//  Sidenote: _frame_counter is ONLY used for _savedump in non-MP-games
		//    Should that not be deleted? If so, the next 2 lines can also be deleted
		if (!Global._networking) Global._frame_counter++;

		/* TODO dump
		if (_savedump_path[0] && (int)_frame_counter >= _savedump_first && (int)(_frame_counter -_savedump_first) % _savedump_freq == 0 ) {
			String buf;
			buf = String.format( "%s%.5d.sav", Global._savedump_path, Global._frame_counter);
			SaveOrLoad(buf, SL_SAVE);
			if ((int)Global._frame_counter >= _savedump_last) exit(1);
		} */

		if (Global._game_mode == GameModes.GM_EDITOR) {
			Landscape.RunTileLoop();
			Vehicle.CallVehicleTicks();
			Landscape.CallLandscapeTick();
			Window.CallWindowTickEvent();
			NewsItem.NewsLoop();
		} else {
			// All these actions has to be done from OWNER_NONE
			//  for multiplayer compatibility
			PlayerID p =  Global._current_player;
			Global._current_player = PlayerID.get( Owner.OWNER_NONE );

			TextEffect.AnimateAnimatedTiles();
			Global.IncreaseDate();
			Landscape.RunTileLoop();
			Vehicle.CallVehicleTicks();
			Landscape.CallLandscapeTick();

			// TODO Ai.AI_RunGameLoop();

			Window.CallWindowTickEvent();
			NewsItem.NewsLoop();
			Global._current_player = p;
		}
	}

	static void DoAutosave()
	{
		String buf;

		if (Global._patches.keep_all_autosave && Global._local_player.id != Owner.OWNER_SPECTATOR) {
			final Player p = Global._local_player.GetPlayer();
			String s;
			Global.SetDParam(0, p.name_1);
			Global.SetDParam(1, p.name_2);
			Global.SetDParam(2, Global._date);
			//s = GetString(buf + strlen(_path.autosave_dir) + strlen(PATHSEP), Str.STR_4004);
			//strcpy(s, ".sav");
			s = Global.GetString(Str.STR_4004);

			
			//sprintf(buf, "%s%s", Global._path.autosave_dir, PATHSEP);
			buf = String.format("%s%s%s.sav", Global._path.autosave_dir, File.pathSeparator, s);

			
		} else { /* generate a savegame name and number according to _patches.max_num_autosaves */
			//sprintf(buf, "%s%sautosave%d.sav", _path.autosave_dir, PATHSEP, _autosave_ctr);
			// TODO buf = String.format("%s%sautosave%d.sav", Global._path.autosave_dir, File.pathSeparator, Global._autosave_ctr);

			/* TODO
			Global._autosave_ctr++;
			if (Global._autosave_ctr >= Global._patches.max_num_autosaves) {
				// we reached the limit for numbers of autosaves. We will start over
				Global._autosave_ctr = 0;
			
			}*/
		}

		/* TODO
		Global.DEBUG_misc( 2, "Autosaving to %s", buf);
		if (SaveOrLoad(buf, SL_SAVE) != SL_OK)
			Global.ShowErrorMessage(INVALID_STRING_ID, Str.STR_AUTOSAVE_FAILED, 0, 0);
		*/
	}

	static void ScrollMainViewport(int x, int y)
	{
		if (Global._game_mode != GameModes.GM_MENU) {
			Window w = Window.FindWindowById(Window.WC_MAIN_WINDOW, 0);
			assert(w != null);

			w.as_vp_d().scrollpos_x += x << w.viewport.zoom;
			w.as_vp_d().scrollpos_y += y << w.viewport.zoom;
		}
	}

	//static final byte scrollamt[16][2] = {
	static final byte scrollamt[][] = {
			{ 0, 0},
			{-2, 0}, // 1:left
			{ 0,-2}, // 2:up
			{-2,-1}, // 3:left + up
			{ 2, 0}, // 4:right
			{ 0, 0}, // 5:left + right
			{ 2,-1}, // 6:right + up
			{ 0,-2}, // 7:left + right + up = up
			{ 0 ,2}, // 8:down
			{-2 ,1}, // 9:down+left
			{ 0, 0}, // 10:impossible
			{-2, 0}, // 11:left + up + down = left
			{ 2, 1}, // 12:down+right
			{ 0, 2}, // 13:left + right + down = down
			{ 0,-2}, // 14:left + right + up = up
			{ 0, 0}, // 15:impossible
	};

	static void HandleKeyScrolling()
	{
		if (Global._dirkeys != 0 && 0 == Global._no_scroll) {
			int factor = Global._shift_pressed ? 50 : 10;
			ScrollMainViewport(scrollamt[Global._dirkeys][0] * factor, scrollamt[Global._dirkeys][1] * factor);
		}
	}

	static void GameLoop()
	{
		SwitchModes m;
		ThreadMsg message;

		if ((message = OTTD_PollThreadEvent()) != ThreadMsg.MSG_OTTD_SAVETHREAD_ZERO) 
			ProcessSentMessage(message);

		// autosave game?
		if (Global._do_autosave) {
			Global._do_autosave = false;
			DoAutosave();
			// TODO RedrawAutosave();
		}

		// handle scrolling of the main window
		if (0 != Global._dirkeys) HandleKeyScrolling();

		/* TODO // make a screenshot?
		if ((m=_make_screenshot) != 0) {
			_make_screenshot = 0;
			switch(m) {
			case 1: // make small screenshot
				UndrawMouseCursor();
				ShowScreenshotResult(MakeScreenshot());
				break;
			case 2: // make large screenshot
				ShowScreenshotResult(MakeWorldScreenshot(-(int)MapMaxX() * TILE_PIXELS, 0, (MapMaxX() + MapMaxY()) * TILE_PIXELS, (MapMaxX() + MapMaxY()) * TILE_PIXELS >> 1, 0));
				break;
			}
		} */

		// switch game mode?
		if ((m=Global._switch_mode) != SwitchModes.SM_NONE) {
			Global._switch_mode = SwitchModes.SM_NONE;
			SwitchMode(m);
		}

		//IncreaseSpriteLRU();
		Hal.InteractiveRandom();

		if (Window._scroller_click_timeout > 3) {
			Window._scroller_click_timeout -= 3;
		} else {
			Window._scroller_click_timeout = 0;
		}

		Global._caret_timer += 3;
		Global._timer_counter += 8;
		Hal.CursorTick();

		/* #ifdef ENABLE_NETWORK
		// Check for UDP stuff
		NetworkUDPGameLoop();

		if (_networking) {
			// Multiplayer
			NetworkGameLoop();
		} else {
			if (_network_reconnect > 0 && --_network_reconnect == 0) {
				// This means that we want to reconnect to the last host
				// We do this here, because it means that the network is really closed
				NetworkClientConnectGame(_network_last_host, _network_last_port);
			}
			// Singleplayer
			StateGameLoop();
		}
	#else */
		StateGameLoop();
		//#endif /* ENABLE_NETWORK */

		if (0 == Global._pause && 0 != (Global._display_opt & Global.DO_FULL_ANIMATION) ) Gfx.DoPaletteAnimations();

		if (0 == Global._pause || Global._cheats.build_in_pause.value) TextEffect.MoveAllTextEffects();

		Window.InputLoop();

		// TODO MusicLoop();
	}

	void BeforeSaveGame()
	{
		final Window w = Window.FindWindowById(Window.WC_MAIN_WINDOW, 0);

		if (w != null) {
			// TODO Global._saved_scrollpos_x = ((vp_d)w.custom).scrollpos_x;
			//_saved_scrollpos_y = ((vp_d)w.custom).scrollpos_y;
			//_saved_scrollpos_zoom = w.viewport.zoom;
		}
	}

	/*unused
	static void ConvertTownOwner()
	{
		TileIndex tile;

		for (tile = 0; tile != MapSize(); tile++) {
			if (IsTileType(tile, MP_STREET)) {
				if (IsLevelCrossing(tile) && _m[tile].m3 & 0x80) _m[tile].m3 = Owner.OWNER_TOWN;

				if (_m[tile].m1 & 0x80) SetTileOwner(tile, Owner.OWNER_TOWN);
			} else if (IsTileType(tile, MP_TUNNELBRIDGE)) {
				if (_m[tile].m1 & 0x80) SetTileOwner(tile, Owner.OWNER_TOWN);
			}
		}
	}*/

	// before savegame version 4, the name of the company determined if it existed
	static void CheckIsPlayerActive()
	{

		//FOR_ALL_PLAYERS(p) 
		for( Player p: Global._players )
		{
			if (p.name_1 != 0) p.is_active = true;
		}
	}

	/* unused
	// since savegame version 4.1, exclusive transport rights are stored at towns
	static void UpdateExclusiveRights()
	{
		Town* t;

		FOR_ALL_TOWNS(t) {
			if (t.xy != 0) t.exclusivity = (byte)-1;
		}

		/* FIXME old exclusive rights status is not being imported (stored in s.blocked_months_obsolete)
				could be implemented this way:
				1.) Go through all stations
						Build an array town_blocked[ town_id ][ player_id ]
					 that stores if at least one station in that town is blocked for a player
				2.) Go through that array, if you find a town that is not blocked for
						one player, but for all others, then give him exclusivity.
	 * /
	} */

	/* unused
	static final byte convert_currency[] = {
		 0,  1, 12,  8,  3,
		10, 14, 19,  4,  5,
		 9, 11, 13,  6, 17,
		16, 22, 21,  7, 15,
		18,  2, 20, };

	// since savegame version 4.2 the currencies are arranged differently
	static void UpdateCurrencies()
	{
		_opt.currency = convert_currency[_opt.currency];
	} */

	/* Up to revision 1413 the invisible tiles at the southern border have not been
	 * MP_VOID, even though they should have. This is fixed by this function
	 * unused/
	static void UpdateVoidTiles()
	{
		int i;

		for (i = 0; i < MapMaxY(); ++i)
			SetTileType(i * MapSizeX() + MapMaxX(), MP_VOID);
		for (i = 0; i < MapSizeX(); ++i)
			SetTileType(MapSizeX() * MapMaxY() + i, MP_VOID);
	}

	/* unused
	// since savegame version 6.0 each sign has an "owner", signs without owner (from old games are set to 255)
	static void UpdateSignOwner()
	{
		SignStruct *ss;

		FOR_ALL_SIGNS(ss) ss.owner = OWNER_NONE;
	}*/


	static boolean AfterLoadGame(int version)
	{
		Window w;
		ViewPort vp;
		Player p;

		// in version 2.1 of the savegame, town owner was unified.
		//if (CheckSavegameVersionOldStyle(2, 1)) ConvertTownOwner();

		// from version 4.1 of the savegame, exclusive rights are stored at towns
		//if (CheckSavegameVersionOldStyle(4, 1)) UpdateExclusiveRights();

		// from version 4.2 of the savegame, currencies are in a different order
		//if (CheckSavegameVersionOldStyle(4, 2)) UpdateCurrencies();

		// from version 6.1 of the savegame, signs have an "owner"
		//if (CheckSavegameVersionOldStyle(6, 1)) UpdateSignOwner();

		/* In old version there seems to be a problem that water is owned by
		    OWNER_NONE, not OWNER_WATER.. I can't replicate it for the current
		    (4.3) version, so I just check when versions are older, and then
		    walk through the whole map.. */
		/* TODO check me
		if (CheckSavegameVersionOldStyle(4, 3)) {
			TileIndex tile = new TileIndex(0, 0);
			int w = MapSizeX();
			int h = MapSizeY();

			BEGIN_TILE_LOOP(tile_cur, w, h, tile)
			if (IsTileType(tile_cur, MP_WATER) && GetTileOwner(tile_cur) >= Global.MAX_PLAYERS)
				SetTileOwner(tile_cur, OWNER_WATER);
			END_TILE_LOOP(tile_cur, w, h, tile)
		}*/

		// convert road side to my format.
		if (GameOptions._opt.road_side != 0) GameOptions._opt.road_side = 1;

		// Load the sprites
		GfxInit.GfxLoadSprites();

		// Update current year
		Global.SetDate(Global._date);

		// reinit the landscape variables (landscape might have changed)
		Misc.InitializeLandscapeVariables(true);

		// Update all vehicles
		Vehicle.AfterLoadVehicles();

		// FIXME KILLME Update all waypoints
		// TODO if (CheckSavegameVersion(12)) FixOldWaypoints();

		WayPoint.UpdateAllWaypointSigns();

		// FIXME KILLME in version 2.2 of the savegame, we have new airports
		// TODO if (CheckSavegameVersionOldStyle(2, 2)) UpdateOldAircraft();

		// FIXME KILLME ?
		Station.UpdateAllStationVirtCoord();

		// Setup town coords
		Town.AfterLoadTown();
		SignStruct.UpdateAllSignVirtCoords();

		// make sure there is a town in the game
		if (Global._game_mode == GameModes.GM_NORMAL && null == Town.ClosestTownFromTile(TileIndex.get(0), (int)-1)) 
		{
			Global._error_message = Str.STR_NO_TOWN_IN_SCENARIO;
			return false;
		}

		// Initialize windows
		Window.ResetWindowSystem();
		Gui.SetupColorsAndInitialWindow();

		w = Window.FindWindowById(Window.WC_MAIN_WINDOW, 0);

		//w.as_vp_d().scrollpos_x = _saved_scrollpos_x;
		//w.as_vp_d().scrollpos_y = _saved_scrollpos_y;

		// TODO ((vp_d)w.custom).scrollpos_x = Global._saved_scrollpos_x;
		// TODO ((vp_d)w.custom).scrollpos_y = Global._saved_scrollpos_y;

		vp = w.viewport;
		// TODO vp.zoom = _saved_scrollpos_zoom;
		vp.virtual_width = vp.width << vp.zoom;
		vp.virtual_height = vp.height << vp.zoom;

		// // FIXME KILLME 
		// in version 4.1 of the savegame, is_active was introduced to determine
		// if a player does exist, rather then checking name_1
		// TODO if (CheckSavegameVersionOldStyle(4, 1)) CheckIsPlayerActive();

		// the void tiles on the southern border used to belong to a wrong class (pre 4.3).
		//if (CheckSavegameVersionOldStyle(4, 3)) UpdateVoidTiles();

		// If Load Scenario / New (Scenario) Game is used,
		//  a player does not exist yet. So create one here.
		// 1 exeption: network-games. Those can have 0 players
		//   But this exeption is not true for network_servers!
		if (!Global._players[0].is_active && (!Global._networking || (Global._networking && Global._network_server)))
			Player.DoStartupNewPlayer(false);

		Gui.DoZoomInOutWindow(Gui.ZOOM_NONE, w); // update button status
		Hal.MarkWholeScreenDirty();

		// // FIXME KILLME In 5.1, Oilrigs have been moved (again)
		//if (CheckSavegameVersionOldStyle(5, 1)) UpdateOilRig();

		/* // FIXME KILLME In version 6.1 we put the town index in the map-array. To do this, we need
		 *  to use m2 (16bit big), so we need to clean m2, and that is where this is
		 *  all about ;) * /
		if (CheckSavegameVersionOldStyle(6, 1)) {
			BEGIN_TILE_LOOP(tile, MapSizeX(), MapSizeY(), 0) {
				if (IsTileType(tile, MP_HOUSE)) {
					_m[tile].m4 = _m[tile].m2;
					//XXX magic
					SetTileType(tile, MP_VOID);
					_m[tile].m2 = ClosestTownFromTile(tile,(int)-1).index;
					SetTileType(tile, MP_HOUSE);
				} else if (IsTileType(tile, MP_STREET)) {
					//XXX magic
					_m[tile].m4 |= (_m[tile].m2 << 4);
					if (IsTileOwner(tile, Owner.OWNER_TOWN)) {
						SetTileType(tile, MP_VOID);
						_m[tile].m2 = ClosestTownFromTile(tile,(int)-1).index;
						SetTileType(tile, MP_STREET);
					} else {
						_m[tile].m2 = 0;
					}
				}
			} END_TILE_LOOP(tile, MapSizeX(), MapSizeY(), 0);
		}
		*/
		/* // FIXME KILLME From version 9.0, we update the max passengers of a town (was sometimes negative
		 *  before that. * /
		if (CheckSavegameVersion(9)) {
			Town *t;
			FOR_ALL_TOWNS(t) UpdateTownMaxPass(t);
		}*/

		/* // FIXME KILLME From version 15.0, we moved a semaphore bit from bit 2 to bit 3 in m4, making
		 *  room for PBS. While doing that, clean some blocks that should be empty, for PBS. */
		/*
		if (CheckSavegameVersion(15)) {
			BEGIN_TILE_LOOP(tile, MapSizeX(), MapSizeY(), 0) {
				if (IsTileType(tile, MP_RAILWAY) && HasSignals(tile) && HASBIT(_m[tile].m4, 2)) {
					CLRBIT(_m[tile].m4, 2);
					SETBIT(_m[tile].m4, 3);
				}
				// Clear possible junk data in PBS bits.
				if (IsTileType(tile, MP_RAILWAY) && !HASBIT(_m[tile].m5, 7))
					SB(_m[tile].m4, 4, 4, 0);
			} END_TILE_LOOP(tile, MapSizeX(), MapSizeY(), 0);
		}*/

		/* // FIXME KILLME From version 16.0, we included autorenew on engines, which are now saved, but
		 *  of course, we do need to initialize them for older savegames. */
		/*
		if (CheckSavegameVersion(16)) {
			FOR_ALL_PLAYERS(p) {
				InitialiseEngineReplacement(p);
				p.engine_renew = false;
				p.engine_renew_months = -6;
				p.engine_renew_money = 100000;
			}
			if (_local_player < Global.MAX_PLAYERS) {
				// Set the human controlled player to the patch settings
				// Scenario editor do not have any companies
				p = _local_player.GetPlayer();
				p.engine_renew = Global._patches.autorenew;
				p.engine_renew_months = Global._patches.autorenew_months;
				p.engine_renew_money = Global._patches.autorenew_money;
			}
		}

		/* // FIXME KILLME In version 16.1 of the savegame, trains became aware of station lengths
			need to initialized to the invalid state
			players needs to set renew_keep_length too * /
		if (CheckSavegameVersionOldStyle(16, 1)) {
			Vehicle *v;
			FOR_ALL_PLAYERS(p) {
				p.renew_keep_length = false;
			}

			FOR_ALL_VEHICLES(v) {
				if (v.type == VEH_Train) {
					v.u.rail.shortest_platform[0] = 255;
					v.u.rail.shortest_platform[1] = 0;
				}
			}
		}

		/* // FIXME KILLME In version 17, ground type is moved from m2 to m4 for depots and
		 * waypoints to make way for storing the index in m2. The custom graphics
		 * id which was stored in m4 is now saved as a grf/id reference in the
		 * waypoint struct. * /
		if (CheckSavegameVersion(17)) {
			Waypoint *wp;

			FOR_ALL_WAYPOINTS(wp) {
				if (wp.xy != 0 && wp.deleted == 0) {
					final StationSpec *spec = null;

					if (HASBIT(_m[wp.xy].m3, 4))
						spec = GetCustomStation(STAT_CLASS_WAYP, _m[wp.xy].m4 + 1);

					if (spec != null) {
						wp.stat_id = _m[wp.xy].m4 + 1;
						wp.grfid = spec.grfid;
						wp.localidx = spec.localidx;
					} else {
						// No custom graphics set, so set to default.
						wp.stat_id = 0;
						wp.grfid = 0;
						wp.localidx = 0;
					}

					// Move ground type bits from m2 to m4.
					_m[wp.xy].m4 = GB(_m[wp.xy].m2, 0, 4);
					// Store waypoint index in the tile.
					_m[wp.xy].m2 = wp.index;
				}
			}
		} else {
			/* As of version 17, we recalculate the custom graphic ID of waypoints
			 * from the GRF ID / station index. * /
			UpdateAllWaypointCustomGraphics();
		}
		*/
		//FOR_ALL_PLAYERS(p) 
		for( Player pp: Global._players )
			pp.avail_railtypes = Player.GetPlayerRailtypes(pp.index);

		return true;
	}


	
	static void DeterminePaths()
	{
		//String s;
		//String cfg;

		String cwd = null;
		try {
			cwd = new java.io.File(".").getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error(e.toString());
		}
		
		Global.printf("Start in '%s'", cwd);
		
		String slcwd = cwd + File.separator;
		
		Global._path.personal_dir = Global._path.game_data_dir = slcwd;


		Global._path.save_dir = slcwd+"save";
		Global._path.autosave_dir = Global._path.save_dir + File.separator +  "autosave";
		Global._path.scenario_dir = slcwd+"scenario";
		Global._path.gm_dir = slcwd+"gm"+ File.separator;
		Global._path.data_dir = slcwd+"resources"+ File.separator;
		//Global._path.lang_dir = slcwd+"lang"+ File.separator;
		Global._path.lang_dir = slcwd+"resources"+ File.separator;

		if (Global._config_file == null)
			Global._config_file =  Global._path.personal_dir + "openttd.cfg";

		/* TODO paths
		_highscore_file = str_fmt("%shs.dat", _path.personal_dir);
		_log_file = str_fmt("%sopenttd.log", _path.personal_dir);

		// make (auto)save and scenario folder
		CreateDirectory(_path.save_dir, NULL);
		CreateDirectory(_path.autosave_dir, NULL);
		CreateDirectory(_path.scenario_dir, NULL);
		*/
	}
	

}



/*
class MyGetOptData 
{
	String opt;
	int numleft;
	String [] argv;
	String arg;
	String options;
	//String cont;
	
	int curarg;
	private int argpos;


	public void MyGetOptInit( String []argv, final String options)
	{
		//this.cont = null;
		this.numleft = argv.length;
		this.argv = argv;
		this.options = options;
		
		this.curarg = 0;
		this.argpos = 0;
		
		this.arg = argv[0];
	}

	private boolean argEmpty()
	{
		return argpos >= arg.length();
	}
	
	private char argChar()
	{
		return arg.charAt(argpos);
	}
	
	public int MyGetOpt()
	{
		//String s;
		String r;
		String t;

		//s = cont;
		//if (s != null)			goto md_continue_here;

		for (;;) {

			if( argEmpty() )
			{
				//s = *argv++;
				if (--numleft < 0) return -1;
				arg = argv[++curarg];
				argpos = 0;
			}
			
			if(argChar() == '-') {
				//md_continue_here:;
				argpos++;
				if (!argEmpty()) {
					// Found argument, try to locate it in options.
					if (argChar() == ':' || (r = strchr(options, argChar())) == null) {
						// ERROR!
						return -2;
					}
					if (r[1] == ':') {
						// Item wants an argument. Check if the argument follows, or if it comes as a separate arg.
						if (!*(t = s + 1)) {
							// It comes as a separate arg. Check if out of args?
							if (--numleft < 0 || *(t = *argv) == '-') {
								// Check if item is optional?
								if (r[2] != ':')
									return -2;
								numleft++;
								t = null;
							} else {
								argv++;
							}
						}
						opt = t;
						cont = null;
						return argChar();
					}
					opt = null;
					cont = s;
					return argChar();
				}
			} else {
				// This is currently not supported.
				return -2;
			}
		}
	}

}
*/


//Deals with the type of the savegame, independent of extension
class SmallFiosItem 
{
	int mode;             // savegame/scenario type (old, new)
	String name;  // name
	String title;      // internal name of the game
} 

