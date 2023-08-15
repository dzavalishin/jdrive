package com.dzavalishin.game;

import java.io.File;
import java.io.IOException;

import com.dzavalishin.ai.Ai;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.net.Net;
import com.dzavalishin.net.NetDefs;
import com.dzavalishin.net.NetUDP;
import com.dzavalishin.console.ConsoleFactory;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.SaveOrLoadResult;
import com.dzavalishin.enums.SwitchModes;
import com.dzavalishin.enums.ThreadMsg;
import com.dzavalishin.exceptions.InvalidFileFormat;
import com.dzavalishin.exceptions.InvalidSpriteFormat;
import com.dzavalishin.net.NetGui;
import com.dzavalishin.struct.SmallFiosItem;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.FileIO;
import com.dzavalishin.util.Music;
import com.dzavalishin.util.ScreenShot;
import com.dzavalishin.util.ShortSounds;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.Strings;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.GfxInit;
import com.dzavalishin.xui.Gui;
import com.dzavalishin.xui.MiscGui;
import com.dzavalishin.xui.MusicGui;
import com.dzavalishin.xui.SettingsGui;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.Window;
import gnu.getopt.Getopt;

public class Main {

	public static final SmallFiosItem _file_to_saveload = new SmallFiosItem();



	/* TODO: usrerror() for errors which are not of an internal nature but
	 * caused by the user, i.e. missing files or fatal configuration errors.
	 * Post-0.4.0 since Celestar doesn't want this in SVN before. --pasky */


	public static void  error( String  s, Object ... args)
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


	static void showhelp()
	{
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
						"  -f                  = Fork into the background (dedicated only)\n" +
						"  -i                  = Force to use the DOS palette (use this if you see a lot of pink)\n" +
						"  -p #player          = Player as #player (deprecated) (network only)\n" +
						"  -c config_file      = Use 'config_file' instead of 'openttd.cfg'\n"
						;

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




	static void LoadIntroGame()
	{
		Global._game_mode = GameModes.GM_MENU;
		// don't make buildings transparent in intro
		Global._display_opt = (byte) BitOps.RETCLRBITS( Global._display_opt, Global.DO_TRANS_BUILDINGS );
		GameOptions._opt_ptr = GameOptions._opt_newgame;

		GfxInit.GfxLoadSprites();
		Gfx.LoadStringWidthTable();

		// Setup main window
		Window.ResetWindowSystem();
		Gui.SetupColorsAndInitialWindow();

		// Generate a world.
		String filename = String.format( "%sopntitle.dat",  Global._path.data_dir );
		// TODO if (SaveLoad.SaveOrLoad(filename, SaveLoad.SL_LOAD) != SaveOrLoadResult.SL_OK) 
		{
			/*#if defined SECOND_DATA_DIR
			sprintf(filename, "%sopntitle.dat",  _path.second_data_dir);
			if (SaveOrLoad(filename, SL_LOAD) != SL_OK)
			#endif*/
			//GenerateWorld.doGenerateWorld(1, 256, 256); // if failed loading, make empty world.
			GenerateWorld.doGenerateWorld(0, 256, 256); // if failed loading, make empty world.
		}

		Global._pause = 0;
		Global.gs._local_player = PlayerID.getNone();
		Hal.MarkWholeScreenDirty();

		// Play main theme
		// TODO if (_music_driver.is_song_playing()) ResetMusic();
	}


	public static void main(String[] argv) throws IOException, InvalidFileFormat, InvalidSpriteFormat 
	{
		boolean network = false;
		String network_conn = null;
		//final String optformat;
		//int resolution[] = {0,0};
		int startdate = -1;
		boolean dedicated = false;

		Global._game_mode = GameModes.GM_MENU;
		Global._switch_mode = SwitchModes.SM_MENU;
		Global._switch_mode_errorstr = Str.INVALID_STRING_ID();
		Global._dedicated_forks = false;
		dedicated = false;
		Global._path.config_file = null;

		// The last param of the following function means this:
		//   a letter means: it accepts that param (e.g.: -h)
		//   a ':' behind it means: it need a param (e.g.: -m<driver>)
		//   a '::' behind it means: it can optional have a param (e.g.: -d<debug>)

		//optformat = "bm:s:v:hDfn::eit:d::r:g::G:p:c:";

		Getopt g = new Getopt("NextTTD", argv, "behfc:t:g::n::"); //"d::r:G:p:");

		int c;
		while ((c = g.getopt()) != -1)
		{
			switch(c)
			{
			case 'h': showhelp(); return;

			case 'f': Global._dedicated_forks = true; break;
			case 'e': Global._switch_mode = SwitchModes.SM_EDITOR; break;
			case 'b': Ai._ai.network_client = true; break;

			case 'c': Global._path.config_file = g.getOptarg(); break;
			
			case 'n': {
				network = true;
				// Optional, you can give an IP
				network_conn = g.getOptarg();
			} break; 

			case 'p': {
				int netp = Integer.parseInt(g.getOptarg());
				// Play as an other player in network games
				if(BitOps.IS_INT_INSIDE(netp, 1, Global.MAX_PLAYERS)) 
					Global._network_playas = netp;
			} break;

			case 't': startdate = Integer.parseInt(g.getOptarg()); break;

			case 'g':
				if (g.getOptarg() != null) {
					_file_to_saveload.name = g.getOptarg();
					Global._switch_mode = SwitchModes.SM_LOAD;
				} else
					Global._switch_mode = SwitchModes.SM_NEWGAME;
				break;
			}
		}

		/*
		mgo.MyGetOptInit( argv, optformat);

		while ((i = mgo.MyGetOpt()) != -1) {
			switch(i) {
			//case 'r': ParseResolution(resolution, mgo.opt); break;
			case 'd': {
				if (mgo.opt != null) SetDebugString(mgo.opt);
			} break;

			case 'G':
				Global._random_seeds[0][0] = Integer.parseInt(mgo.opt);
				break;
			}
			case -2:
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

		SaveLoad.LoadFromConfig();
		// TODO CheckConfig();
		SaveLoad.LoadFromHighScore();

		// override config?
		// TODO if (resolution[0]) { _cur_resolution[0] = resolution[0]; _cur_resolution[1] = resolution[1]; }
		if (startdate != -1) Global._patches.starting_date = startdate;

		if (Global._dedicated_forks && !dedicated)
			Global._dedicated_forks = false;

		Global.hal.start_video();

		// enumerate language files
		Strings.InitializeLanguagePacks();

		// initialize screenshot formats
		ScreenShot.InitializeScreenshotFormats();

		// initialize airport state machines
		Airport.InitializeAirports();

		/* start the AI */
		Ai.AI_Initialize();

		// Sample catalogue
		Global.DEBUG_misc( 1, "Loading sound effects...");
		Sound.MxInitialize(11025);
		Sound.SoundInitialize("sample.cat");
		//Sound.SoundInitialize("opensfx.cat"); 16 bit samples support is not finished
		//Sound.StartSound(2, 0, 50);
		ShortSounds.preload();
		ShortSounds.playFarmSound();

		// This must be done early, since functions use the InvalidateWindow* calls
		Window.InitWindowSystem();

		//Engine.AddTypeToEngines(); // [dz] added, or StartupEngines crashes
		//Engine.StartupEngines(); // [dz] added, or newgrf load crashes

		GfxInit.GfxLoadSprites();
		Gfx.LoadStringWidthTable();

		//NewGrf test = new NewGrf("xussr.grf");
		//test.load();
		
		// TODO _savegame_sort_order = SORT_BY_DATE | SORT_DESCENDING;

		// initialize network-core
		Net.NetworkStartUp();

		GameOptions._opt_ptr = GameOptions._opt_newgame;

		/* XXX - ugly hack, if diff_level is 9, it means we got no setting from the config file */
		if (GameOptions._opt_newgame.diff_level == 9)
			SettingsGui.SetDifficultyLevel(0, GameOptions._opt_newgame);

		// initialize the ingame console
		ConsoleFactory.INSTANCE.getConsole();
//		Console.IConsoleInit();
		VehicleGui.InitializeGUI();
		ConsoleFactory.INSTANCE.getConsole().IConsoleCmdExec("exec scripts/autoexec.scr 0");

		GenerateWorld.doGenerateWorld(1, 256, 256); // Make the viewport initialization happy

		// GRFFile.CalculateRefitMasks();

		if ((network) && (Global._network_available)) {
			if (network_conn != null) {
				final String [] host = { null };
				final String [] port = { null };
				final String [] player = { null };
				int rport;

				rport = NetDefs.NETWORK_DEFAULT_PORT;

				Net.ParseConnectionString(player, port, host, network_conn);

				if (player != null) Global._network_playas = Integer.parseInt(player[0]);
				if (port != null) rport = Integer.parseInt(port[0]);

				LoadIntroGame();
				Global._switch_mode = SwitchModes.SM_NONE;
				NetGui.NetworkClientConnectGame(host[0], rport);
			}
		}


		Global.hal.main_loop();

		// TODO WaitTillSaved();
		//Console.IConsoleFree();

		if (Global._network_available) {
			// Shut down the network and close any open connections
			Net.NetworkDisconnect();
			NetUDP.NetworkUDPClose();
			Net.NetworkShutDown();
		}

		Global.hal.stop_video();
		Music.stop_song();
		Sound.stop();

		// TODO SaveToConfig();
		SaveLoad.SaveToHighScore();



		/* stop the AI */
		Ai.AI_Uninitialize();

		/* Close all and any open filehandles */
		FileIO.FioCloseAll();

		System.exit(0);
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
	{
		if (b) {
			Global.SetDParamStr(0, ScreenShot._screenshot_name);
			Global.ShowErrorMessage(Str.INVALID_STRING_ID().id, Str.STR_031B_SCREENSHOT_SUCCESSFULLY, 0, 0);
		} else {
			Global.ShowErrorMessage(Str.INVALID_STRING_ID().id, Str.STR_031C_SCREENSHOT_FAILED, 0, 0);
		}

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
			Global.gs._local_player = PlayerID.get( Owner.OWNER_SPECTATOR);
		} else {
			// Create a single player
			Player.DoStartupNewPlayer(false);

			Global.gs._local_player = PlayerID.get(0); 
			PlayerID.setCurrent(Global.gs._local_player);
			Cmd.DoCommandP(null, (Global._patches.autorenew.get() ? (1 << 15) : 0 ) | (Global._patches.autorenew_months << 16) | 4, (int)Global._patches.autorenew_money, null, Cmd.CMD_REPLACE_VEHICLE);
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

		Global.gs._local_player = PlayerID.getNone();
		Hal.MarkWholeScreenDirty();
	}


	/**
	 * Start Scenario starts a new game based on a scenario.
	 * Eg 'New Game' -. select a preset scenario
	 * This starts a scenario based on your current difficulty settings
	 */
	static void StartScenario()
	{
		//*
		Global._game_mode = GameModes.GM_NORMAL;

		// invalid type
		if (_file_to_saveload.mode == SaveLoad.SL_INVALID) {
			Global.error("Savegame is obsolete or invalid format: %s\n", _file_to_saveload.name);
			Global.ShowErrorMessage(Str.INVALID_STRING_ID().id, Str.STR_4009_GAME_LOAD_FAILED, 0, 0);
			Global._game_mode = GameModes.GM_MENU;
			return;
		}

		GfxInit.GfxLoadSprites();

		// Reinitialize windows
		Window.ResetWindowSystem();
		Gfx.LoadStringWidthTable();

		Gui.SetupColorsAndInitialWindow();

		// Load game
		if (SaveLoad.SaveOrLoad(_file_to_saveload.name, _file_to_saveload.mode) != SaveOrLoadResult.SL_OK) {
			LoadIntroGame();
			Global.ShowErrorMessage(Str.INVALID_STRING_ID().id, Str.STR_4009_GAME_LOAD_FAILED, 0, 0);
		}

		GameOptions._opt_ptr = GameOptions._opt;
		//memcpy(&_opt_ptr.diff, &_opt_newgame.diff, sizeof(GameDifficulty));
		GameOptions._opt_ptr.diff = GameOptions._opt_newgame.diff.makeClone();
		GameOptions._opt.diff_level = GameOptions._opt_newgame.diff_level;

		// Inititalize data
		Player.StartupPlayers();
		Engine.StartupEngines();
		DisasterCmd.StartupDisasters();

		Global.gs._local_player = null;
		PlayerID.setCurrent( Global.gs._local_player );
		// TODO arg 2 long truncated
		Cmd.DoCommandP(null, (Global._patches.autorenew.get() ? 1 << 15 : 0 ) | (Global._patches.autorenew_months << 16) | 4, (int)Global._patches.autorenew_money, null, Cmd.CMD_REPLACE_VEHICLE);

		Hal.MarkWholeScreenDirty();
		/* */
	}

	public static boolean SafeSaveOrLoad(final String filename, int mode, GameModes newgm)
	{
		GameModes ogm = Global._game_mode;
		SaveOrLoadResult r;

		Global._game_mode = newgm;
		r = SaveLoad.SaveOrLoad(filename, mode);
		if (r == SaveOrLoadResult.SL_REINIT) {
			switch (ogm) {
			case GM_MENU:   LoadIntroGame();      break;
			case GM_EDITOR: MakeNewEditorWorld(); break;
			default:        MakeNewGame();        break;
			}
			return false;
		} else if (r != SaveOrLoadResult.SL_OK) {
			Global._game_mode = ogm;
			return false;
		} else {
			return true;
		}

	}

	public static void SwitchMode(SwitchModes new_mode)
	{
		
		// If we are saving something, the network stays in his current state
		if (new_mode != SwitchModes.SM_SAVE) {
			// If the network is active, make it not-active
			if (Global._networking) {
				if (Global._network_server && (new_mode == SwitchModes.SM_LOAD || new_mode == SwitchModes.SM_NEWGAME)) {
					Net.NetworkReboot();
					NetUDP.NetworkUDPClose();
				} else {
					Net.NetworkDisconnect();
					NetUDP.NetworkUDPClose();
				}
			}

			// If we are a server, we restart the server
			if (Net._is_network_server) {
				// But not if we are going to the menu
				if (new_mode != SwitchModes.SM_MENU) {
					Net.NetworkServerStart();
				} else {
					// This client no longer wants to be a network-server
					Net._is_network_server = false;
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
				if (Global._network_server)
					Net._network_game_info.map_name = String.format("%s (Loaded scenario)", _file_to_saveload.title);
			StartScenario();
			break;

		case SM_LOAD: { /* Load game, Play Scenario */
			GameOptions._opt_ptr = GameOptions._opt;

			if (!SafeSaveOrLoad(_file_to_saveload.name, _file_to_saveload.mode, GameModes.GM_NORMAL)) {
				LoadIntroGame();
				Global.ShowErrorMessage(Str.INVALID_STRING, Str.STR_4009_GAME_LOAD_FAILED, 0, 0);
			} else {
				// [dz] TODO hack, it's null - saved as null? 
				PlayerID.setCurrent( PlayerID.get(0) );
				Global.gs._local_player = PlayerID.get(0);
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

				Global.gs._local_player = PlayerID.getNone();
				Global._generating_world = true;
				// delete all players.
				for (i = 0; i != Global.MAX_PLAYERS; i++) {
					Economy.ChangeOwnershipOfPlayerItems( PlayerID.get(i), PlayerID.get(Owner.OWNER_SPECTATOR));
					Global.gs._players[i].is_active = false;
				}
				Global._generating_world = false;
				// delete all stations owned by a player
				Station.DeleteAllPlayerStations();
			} else {
				Global.ShowErrorMessage(Str.INVALID_STRING, Str.STR_4009_GAME_LOAD_FAILED, 0, 0);
			}
			break;
		}


		case SM_MENU: /* Switch to game intro menu */
			LoadIntroGame();
			break;

		case SM_SAVE: /* Save game */

			if (SaveLoad.SaveOrLoad(_file_to_saveload.name, SaveLoad.SL_SAVE) != SaveOrLoadResult.SL_OK) {
				Global.ShowErrorMessage(Str.INVALID_STRING, Str.STR_4007_GAME_SAVE_FAILED, 0, 0);
			} else {
				Window.DeleteWindowById(Window.WC_SAVELOAD, 0);
			}
			break;

		case SM_GENRANDLAND: /* Generate random land within scenario editor */
			GenerateWorld.doGenerateWorld(2, 1<<Global._patches.map_x, 1<<Global._patches.map_y);
			// XXX: set date
			Global.gs._local_player = PlayerID.getNone();
			Hal.MarkWholeScreenDirty();
			break;

		default:
			assert false;
			break;
		}

		if (Global._switch_mode_errorstr.isValid())
			Global.ShowErrorMessage(Str.INVALID_STRING_ID(),Global._switch_mode_errorstr,0,0);
	}


	// State controlling game loop.
	// The state must not be changed from anywhere
	// but here.
	// That check is enforced in DoCommand.
	public static void StateGameLoop()
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
			
			PlayerID p =  PlayerID.getCurrent();
			PlayerID.setCurrentToNone();

			//try(PushPlayer pp = new PushPlayer(PlayerID.getNone()))
			//{
				TextEffect.AnimateAnimatedTiles();
				Global.gs.date.IncreaseDate();
				Landscape.RunTileLoop();
				Vehicle.CallVehicleTicks();
				Landscape.CallLandscapeTick();

				Ai.AI_RunGameLoop();

				Window.CallWindowTickEvent();
				NewsItem.NewsLoop();
			//}
			PlayerID.setCurrent( p );
		}
	}

	static void DoAutosave()
	{
		String buf;

		if (Global._patches.keep_all_autosave && Global.gs._local_player.id != Owner.OWNER_SPECTATOR) {
			final Player p = Global.gs._local_player.GetPlayer();
			String s;
			Global.SetDParam(0, p.name_1);
			Global.SetDParam(1, p.name_2);
			Global.SetDParam(2, Global.get_date());
			//s = GetString(buf + strlen(_path.autosave_dir) + strlen(PATHSEP), Str.STR_4004);
			//strcpy(s, ".sav");
			s = Strings.GetString(Str.STR_4004);


			//sprintf(buf, "%s%s", Global._path.autosave_dir, PATHSEP);
			buf = String.format("%s%s%s.sav", Global._path.autosave_dir, File.separator, s);


		} else { /* generate a savegame name and number according to _patches.max_num_autosaves */
			//sprintf(buf, "%s%sautosave%d.sav", _path.autosave_dir, PATHSEP, _autosave_ctr);
			buf = String.format("%s%sautosave%d.sav", Global._path.autosave_dir, File.separator, Global._autosave_ctr);


			Global._autosave_ctr++;
			if (Global._autosave_ctr >= Global._patches.max_num_autosaves) {
				// we reached the limit for numbers of autosaves. We will start over
				Global._autosave_ctr = 0;

			}
		}


		Global.DEBUG_misc( 2, "Autosaving to %s", buf);
		if (SaveLoad.SaveOrLoad(buf, SaveLoad.SL_SAVE) != SaveOrLoadResult.SL_OK)
			Global.ShowErrorMessage(Str.INVALID_STRING, Str.STR_AUTOSAVE_FAILED, 0, 0);

	}

	static void ScrollMainViewport(int x, int y)
	{
		if (Global._game_mode != GameModes.GM_MENU) {
			//Window w = Window.FindWindowById(Window.WC_MAIN_WINDOW, 0);
			//assert(w != null);

			Window.getMain().setScrollPos(x,y);
		}
	}

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
			MiscGui.RedrawAutosave();
		}

		// handle scrolling of the main window
		if (0 != Global._dirkeys) HandleKeyScrolling();

		// make a screenshot?
		int shot = ScreenShot._make_screenshot;
		if(shot != 0) {
			ScreenShot._make_screenshot = 0;
			switch(shot) {
			case 1: // make small screenshot
				Gfx.UndrawMouseCursor();
				ShowScreenshotResult(ScreenShot.MakeScreenshot());
				break;
			case 2: // make large screenshot
				ShowScreenshotResult(ScreenShot.MakeWorldScreenshot(-(int)Global.MapMaxX() * TileInfo.TILE_PIXELS, 0, (Global.MapMaxX() + Global.MapMaxY()) * TileInfo.TILE_PIXELS, (Global.MapMaxX() + Global.MapMaxY()) * TileInfo.TILE_PIXELS >> 1, 0));
				break;
			}
		}

		// switch game mode?
		if ((m=Global._switch_mode) != SwitchModes.SM_NONE) {
			Global._switch_mode = SwitchModes.SM_NONE;
			SwitchMode(m);
		}

		//IncreaseSpriteLRU();
		Hal.InteractiveRandom();

		Window.updateScrollerTimeout();


		Global._caret_timer += 3;
		Global._timer_counter += 8;
		Hal.CursorTick();

		// Check for UDP stuff
		NetUDP.NetworkUDPGameLoop();

		if (Global._networking) {
			// Multiplayer
			Net.NetworkGameLoop();
		} else {
			if (Net._network_reconnect > 0 && --Net._network_reconnect == 0) {
				// This means that we want to reconnect to the last host
				// We do this here, because it means that the network is really closed
				Net.NetworkClientConnectGame(Net._network_last_host, Net._network_last_port);
			}
			// Singleplayer
			StateGameLoop();
		}

		if (0 == Global._pause && 0 != (Global._display_opt & Global.DO_FULL_ANIMATION) ) Gfx.DoPaletteAnimations();

		if (0 == Global._pause || Global._cheats.build_in_pause.value) TextEffect.MoveAllTextEffects();

		Window.InputLoop();
		MusicGui.MusicLoop();

	}





	static boolean AfterLoadGame()
	{
		//Player p;

		// convert road side to my format.
		if (GameOptions._opt.road_side != 0) GameOptions._opt.road_side = 1;

		// Load the sprites
		GfxInit.GfxLoadSprites();

		// Update current year
		Global.gs.date.SetDate(Global.get_date());

		// reinit the landscape variables (landscape might have changed)
		Misc.InitializeLandscapeVariables(true);

		// Update all vehicles
		Vehicle.AfterLoadVehicles();

		WayPoint.UpdateAllWaypointSigns();

		Station.UpdateAllStationVirtCoord();

		// Setup town coords
		Town.AfterLoadTown();
		SignStruct.UpdateAllSignVirtCoords();

		// make sure there is a town in the game
		if (Global._game_mode == GameModes.GM_NORMAL && null == Town.ClosestTownFromTile(TileIndex.get(0), -1))
		{
			Global._error_message = Str.STR_NO_TOWN_IN_SCENARIO;
			return false;
		}

		// Initialize windows
		Window.ResetWindowSystem();
		Gui.SetupColorsAndInitialWindow();

		//Window.afterLoad(); // called in loader



		// If Load Scenario / New (Scenario) Game is used,
		//  a player does not exist yet. So create one here.
		// 1 exeption: network-games. Those can have 0 players
		//   But this exeption is not true for network_servers!
		if (!Global.gs._players[0].is_active && (!Global._networking || (Global._networking && Global._network_server)))
			Player.DoStartupNewPlayer(false);

		Gui.DoZoomInOutWindow(Gui.ZOOM_NONE, Window.getMain()); // update button status
		Hal.MarkWholeScreenDirty();

		for( Player pp: Global.gs._players )
			pp.avail_railtypes = Player.GetPlayerRailtypes(pp.index);

		return true;
	}



	static void DeterminePaths()
	{
		String cwd = null;
		try {
			cwd = new java.io.File(".").getCanonicalPath();
		} catch (IOException e) {

			Global.error(e);
			error(e.toString());
		}

		//Global.printf("Start in '%s'", cwd);

		String slcwd = cwd + File.separator;

		Global._path.personal_dir = Global._path.game_data_dir = slcwd;


		Global._path.save_dir = slcwd+"save";
		Global._path.autosave_dir = Global._path.save_dir + File.separator +  "autosave";
		Global._path.scenario_dir = slcwd+"scenario";
		Global._path.gm_dir = slcwd+"resources"+ File.separator + "gm"+ File.separator;
		Global._path.data_dir = slcwd+"resources"+ File.separator;
		//Global._path.lang_dir = slcwd+"lang"+ File.separator;
		Global._path.lang_dir = slcwd+"resources"+ File.separator;

		if (Global._path.config_file == null)
			Global._path.config_file =  Global._path.personal_dir + "nextttd.cfg";

		/*  paths
		_highscore_file = str_fmt("%shs.dat", _path.personal_dir);
		_log_file = str_fmt("%sopenttd.log", _path.personal_dir);
		 */

		// make (auto)save and scenario folder
		FileIO.mkdir(Global._path.save_dir);
		FileIO.mkdir(Global._path.autosave_dir);
		FileIO.mkdir(Global._path.scenario_dir);
	}


}





