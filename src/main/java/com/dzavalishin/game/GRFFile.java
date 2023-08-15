package com.dzavalishin.game;

import com.dzavalishin.tables.TunnelBridgeTables;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.FileIO;

/* TTDPatch extended GRF format codec
 * (c) Petr Baudis 2004 (GPL'd)
 * Changes by Florian octo Forster are (c) by the OpenTTD development team.
 *
 * Contains portions of documentation by TTDPatch team.
 * Thanks especially to Josef Drexler for the documentation as well as a lot
 * of help at #tycoon. Also thanks to Michael Blunck for is GRF files which
 * served as subject to the initial testing of this codec. */


public class GRFFile 
{
	static final String NEW_SPRITE_GROUP = "NewSpriteGroup";
	String filename;
	int grfid;
	int flags;
	private int sprite_offset;
	//SpriteID first_spriteset; ///< Holds the first spriteset's sprite offset.
	GRFFile next;




	static int _custom_sprites_base = Integer.MAX_VALUE; // [dz] added MAX_VALUE for checkis for image id below it work
	static int _skip_sprites; // XXX
	static int _file_index; // XXX
	//static  int _traininfo_vehicle_pitch;

	static GRFFile _cur_grffile;
	public static GRFFile _first_grffile;
	static int _cur_spriteid;
	//private static int _cur_stage;

	/* 32 * 8 = 256 flags. Apparently TTDPatch uses this many.. */
	static final int[] _ttdpatch_flags = new int[8];









	/* Debugging messages policy:
	 *
	 * These should be the severities used for direct DEBUG() calls
	 * (there is room for exceptions, but you have to have a good cause):
	 *
	 * 0..2 - dedicated to grfmsg()
	 * 3
	 * 4
	 * 5
	 * 6 - action handler entry reporting - one per action
	 * 7 - basic action progress reporting - in loops, only single one allowed
	 * 8 - more detailed progress reporting - less important stuff, in deep loops etc
	 * 9 - extremely detailed progress reporting - detailed reports inside of deep loops and so
	 */

	static final String [] severitystr = {
			"Notice",
			"Warning",
			"Error",
			"Fatal"
	};

	// TODO put strings into enum
	enum severity {
		GMS_NOTICE,
		GMS_WARN,
		GMS_ERROR,
		GMS_FATAL,
	} 


	static void grfmsg(severity severity, String str, Object ... args)
	{
		int export_severity = 0;
		String buf;

		buf = String.format(str, args);

		export_severity = 2 - (severity == GRFFile.severity.GMS_FATAL ? 2 : severity.ordinal());
		Global.DEBUG_grf( export_severity, "[%s][%s] %s", _cur_grffile.getFilename(), severitystr[severity.ordinal()], buf);
	}










	static GRFFile GetFileByGRFID(int grfid)
	{
		GRFFile file;

		for (file = _first_grffile; file != null; file = file.getNext()) {
			if (file.grfid == grfid) break;
		}
		return file;
	}

	static GRFFile GetFileByFilename(final String filename)
	{
		GRFFile file;

		for (file = _first_grffile; file != null; file = file.getNext()) {
			if( file.getFilename().equals(filename) ) 
				break;
		}
		return file;
	}





	static final String msgstr[] = {
			"Requires at least pseudo-TTDPatch version %s.",
			"This file is for %s version of TTD.",
			"Designed to be used with %s.",
			"Invalid parameter %s.",

			"TODO err msg 4 - %s.",
			"TODO err msg 5 - %s.",
			"TODO err msg 6 - %s.",
	};

	static void InitializeGRFSpecial()
	{
		/* FIXME: We should rather reflect reality in _ttdpatch_flags[]. */

		_ttdpatch_flags[0] = (Global._patches.always_small_airport ? (1 << 0x0C) : 0)  /* keepsmallairport */
				| (1 << 0x0E)  /* largestations */
				| (Global._patches.longbridges ? (1 << 0x0F) : 0)           /* longbridges */
				| (1 << 0x12)  /* presignals */
				| (1 << 0x13)  /* extpresignals */
				| (Global._patches.never_expire_vehicles.get() ? (1 << 0x16) : 0) /* enginespersist */
				| (1 << 0x1B); /* multihead */
		_ttdpatch_flags[1] = (Global._patches.mammoth_trains ? (1 << 0x08) : 0)        /* mammothtrains */
				| (1 << 0x09)  /* trainrefit */
				| (1 << 0x14)  /* bridgespeedlimits */
				| (1 << 0x16)  /* eternalgame */
				| (1 << 0x17)  /* newtrains */
				| (1 << 0x18)  /* newrvs */
				| (1 << 0x19)  /* newships */
				| (1 << 0x1A)  /* newplanes */
				| (Global._patches.signal_side ? (1 << 0x1B) : 0);          /* signalsontrafficside */
		/* Uncomment following if you want to fool the GRF file.
		 * Some GRF files will refuse to load without this
		 * but you can still squeeze something from them even
		 * without the support - i.e. USSet. --pasky */
		//| (1 << 0x1C); /* electrifiedrailway */

		_ttdpatch_flags[2] = (Global._patches.build_on_slopes ? (1 << 0x0D) : 0)       /* buildonslopes */
				| (Global._patches.build_on_slopes ? (1 << 0x15) : 0)       /* buildoncoasts */
				| (1 << 0x16)  /* canals */
				| (1 << 0x17)  /* newstartyear */
				| (1 << 0x1A)  /* newbridges */
				| (Global._patches.wagon_speed_limits ? (1 << 0x1D) : 0);   /* wagonspeedlimits */
		_ttdpatch_flags[3] = (1 << 0x03)  /* pathbasedsignalling */
				| (1 << 0x0C)  /* enhancemultiplayer */
				| (1 << 0x0E)  /* irregularstations */
				| (1 << 0x10); /* autoreplace */
	}

	/**
	 * Unload unused sprite groups from the specified GRF file.
	 * Called after loading each GRF file.
	 * @param file GRF file
	 * [dz] meaningless with GC/
	static void ReleaseSpriteGroups(GRFFile file)
	{
		//int i;

		// Bail out if no spritegroups were defined.
		if (file.spritegroups == null)
			return;

		Global.DEBUG_grf( 6, "ReleaseSpriteGroups: Releasing for `%s'.", file.getFilename());
		/*
		for (i = 0; i < file.spritegroups_count; i++) {
			if (file.spritegroups[i] != null)
				UnloadSpriteGroup(&file.spritegroups[i]);
		}* /
		//free(file.spritegroups);
		file.spritegroups = null;
		file.spritegroups_count = 0;
	} */

	static void ResetCustomStations()
	{
		/*
		GRFFile file;
		int i;
		//CargoID c;

		for (file = _first_grffile; file != null; file = file.next) {
			for (i = 0; i < file.stations.length; i++) {
				if (file.stations[i].grfid != file.grfid) continue;


				// Release this stations sprite groups.
				for (c = 0; c < NUM_GLOBAL_CID; c++) {
					if (file.stations[i].spritegroup[c] != null)
						UnloadSpriteGroup(&file.stations[i].spritegroup[c]);
				}
			}
		}
		 */
	}







	/**
	 * Reset all NewGRF loaded data
	 * TODO
	 */
	public static void ResetNewGRFData()
	{
		//int i;
		/*
		// Copy/reset original engine info data
		memcpy(&_engine_info, &orig_engine_info, sizeof(orig_engine_info));
		memcpy(&_rail_vehicle_info, &orig_rail_vehicle_info, sizeof(orig_rail_vehicle_info));
		memcpy(&_ship_vehicle_info, &orig_ship_vehicle_info, sizeof(orig_ship_vehicle_info));
		memcpy(&_aircraft_vehicle_info, &orig_aircraft_vehicle_info, sizeof(orig_aircraft_vehicle_info));
		memcpy(&_road_vehicle_info, &orig_road_vehicle_info, sizeof(orig_road_vehicle_info));
		 */

		// Copy/reset original bridge info data
		/*
		// First, free sprite table data
		for (i = 0; i < MAX_BRIDGES; i++) {
			if (_bridge[i].sprite_table != null) {
				byte j;
				for (j = 0; j < 7; j++)
					free(_bridge[i].sprite_table[j]);
				free(_bridge[i].sprite_table);
			}
		}*/

		//memcpy(&_bridge, &orig_bridge, sizeof(_bridge));

		System.arraycopy( TunnelBridgeTables.orig_bridge, 0, Bridge._bridge, 0, Bridge._bridge.length );  

		// Reset refit/cargo class data
		// TODO memset(&cargo_allowed, 0, sizeof(cargo_allowed));
		// TODO memset(&cargo_disallowed, 0, sizeof(cargo_disallowed));


		// Unload sprite group data
		Engine.UnloadWagonOverrides();
		Engine.UnloadCustomEngineSprites();
		Engine.UnloadCustomEngineNames();

		// Reset price base data
		Economy.ResetPriceBaseMultipliers();

		// Reset station classes
		// TODO StationNewgrf.ResetStationClasses();
		// TODO ResetCustomStations();
	}



	static void InitNewGRFFile(final String  filename, int sprite_offset)
	{
		GRFFile newfile;

		newfile = GetFileByFilename(filename);
		if (newfile != null) {
			/* We already loaded it once. */
			newfile.sprite_offset = sprite_offset;
			_cur_grffile = newfile;
			return;
		}

		newfile = new GRFFile();// calloc(1, sizeof(*newfile));

		//if (newfile == null)			Global.fail("Out of memory");

		newfile.filename = filename;
		newfile.sprite_offset = sprite_offset;

		if (_first_grffile == null) {
			_cur_grffile = newfile;
			_first_grffile = newfile;
		} else {
			_cur_grffile.next = newfile;
			_cur_grffile = newfile;
		}
	}

	/**
	 * Precalculate refit masks from cargo classes for all vehicles.
	 */
	static void CalculateRefitMasks()
	{
		//EngineID 
		int engine;

		for (engine = 0; engine < Global.TOTAL_NUM_ENGINES; engine++) {
			int mask = 0;
			int not_mask = 0;
			int xor_mask = Global._engine_info[engine].refit_mask;
			byte i;

			if (NewGrfActionProcessor.cargo_allowed[engine] != 0) {
				// Build up the list of cargo types from the set cargo classes.
				for (i = 0; i < Engine.cargo_classes.length; i++) {
					if (BitOps.HASBIT(NewGrfActionProcessor.cargo_allowed[engine], i))
						mask |= Engine.cargo_classes[i];
					if (BitOps.HASBIT(NewGrfActionProcessor.cargo_disallowed[engine], i))
						not_mask |= Engine.cargo_classes[i];
				}
			} else {
				// Don't apply default refit mask to wagons or engines with no capacity
				Engine engineInfo = Engine.GetEngine(engine);
				RailVehicleInfo railVehInfo = Engine.RailVehInfo(engine);
				if (xor_mask == 0 
						&& !(engineInfo.getType() == Vehicle.VEH_Train && (railVehInfo.capacity == 0 || railVehInfo.isWagon() )))
					xor_mask = Engine._default_refitmasks[engineInfo.getType() - Vehicle.VEH_Train];
			}
			Global._engine_info[engine].refit_mask = ((mask & ~not_mask) ^ xor_mask) & Engine._landscape_global_cargo_mask[GameOptions._opt.landscape];
		}
	}





	/* Here we perform initial decoding of some special sprites (as are they
	 * described at http://www.ttdpatch.net/src/newgrf.txt, but this is only a very
	 * partial implementation yet). */
	/* XXX: We consider GRF files trusted. It would be trivial to exploit OTTD by
	 * a crafted invalid GRF file. We should tell that to the user somehow, or
	 * better make this more robust in the future. */
	void DecodeSpecialSprite(NewGrfActionProcessor proc, final String  filename, int num, int stage)
	{
		byte [] buf = FileIO.FioReadBlock(num);
		if (buf == null) Global.fail("DecodeSpecialSprite: Could not allocate memory or read data");

		DataLoader bufp = new DataLoader(buf, sprite_offset);
		byte action = bufp.r(0);
		proc.processAction(action,bufp,stage);
	}

	/** Signature of a container version 2 GRF. */
	private static final int _grf_cont_v2_sig[] = { 0 , 0, 'G', 'R', 'F', 0x82, 0x0D, 0x0A, 0x1A, 0x0A};


	static void LoadNewGRFFile(final String  filename, int file_index, int stage)
	{
		int num;

		/* A .grf file is activated only if it was active when the game was
		 * started.  If a game is loaded, only its active .grfs will be
		 * reactivated, unless "loadallgraphics on" is used.  A .grf file is
		 * considered active if its action 8 has been processed, i.e. its
		 * action 8 hasn't been skipped using an action 7.
		 *
		 * During activation, only actions 0, 1, 2, 3, 4, 5, 7, 8, 9, 0A and 0B are
		 * carried out.  All others are ignored, because they only need to be
		 * processed once at initialization.  */
		if (stage != 0) {
			_cur_grffile = GetFileByFilename(filename);
			if (_cur_grffile == null) 
				Global.fail("File ``%s'' lost in cache.\n", filename);

			if (0==(_cur_grffile.flags & 0x0001)) return;
		}

		FileIO.FioOpenFile(file_index, filename);
		_file_index = file_index; // XXX

		Global.DEBUG_grf( 7, "Reading NewGRF-file '%s'", filename);


		boolean newGrfFormat = true;
		for(int c : _grf_cont_v2_sig )
		{
			if( c != FileIO.FioReadByte() )
			{
				newGrfFormat = false;
				break;
			}
		}

		if(newGrfFormat)
		{
			//loadV2Offsets();
			// need code to load it
			Global.error("TODO connect NewGrf loader");
			return;
		}
		else
			FileIO.FioSeekTo(0, FileIO.SEEK_SET);

		/* Skip the first sprite; we don't care about how many sprites this
		 * does contain; newest TTDPatches and George's longvehicles don't
		 * neither, apparently. */
		if (FileIO.FioReadWord() == 4 && FileIO.FioReadByte() == 0xFF) {
			FileIO.FioReadDword();
		} else {
			Global.error("Custom .grf has invalid format.");
			return;
		}

		NewGrfActionProcessor proc = new NewGrfActionProcessorOld(_cur_grffile.sprite_offset);				

		_skip_sprites = 0; // XXX

		while ((num = FileIO.FioReadWord()) != 0) {
			int type = FileIO.FioReadByte();

			if (type == 0xFF) {
				if (_skip_sprites == 0) {
					_cur_grffile.DecodeSpecialSprite(proc, filename, num, stage);
					continue;
				} else {
					FileIO.FioSkipBytes(num);
				}
			} else {
				if (_skip_sprites == 0) Global.DEBUG_grf( 7, "Skipping unexpected sprite");

				FileIO.FioSkipBytes(7);
				num -= 8;

				if(0 != (type & 2) ) {
					FileIO.FioSkipBytes(num);
				} else {
					while (num > 0) {
						int i = FileIO.FioReadByte();
						if (i >= 0) {
							num -= i;
							FileIO.FioSkipBytes(i);
						} else {
							i = -(i >> 3);
							num -= i;
							FileIO.FioReadByte();
						}
					}
				}
			}

			if (_skip_sprites > 0) _skip_sprites--;
		}

		// Release our sprite group references.
		// Any groups that are referenced elsewhere will be cleaned up later.
		// This removes groups that aren't used. (Perhaps skipped?)
		//ReleaseSpriteGroups(_cur_grffile);
	}


	/**
	 * Parse the sprite section of GRFs.
	 * /
	private static void loadV2Offsets() {

		//if (file.GetContainerVersion() >= 2) 
		
		//* Seek to sprite section of the GRF. 
		int data_offset = FileIO.FioReadDword();
		long old_pos = FileIO.FioGetPos();
		FileIO.FioSeekTo(data_offset, FileIO.SEEK_CUR);

		/* Loop over all sprite section entries and store the file
		 * offset for each newly encountered ID. * /
		int id, prev_id = 0;
		while ((id = FileIO.FioReadDword()) != 0) {
			//if (id != prev_id) _grf_sprite_offsets[id] = file.GetPos() - 4;
			prev_id = id;
			Global.DEBUG_grf( 7, "Sprite id %d", id);
			int len = FileIO.FioReadDword();
			FileIO.FioSkipBytes(len);
		}

		//* Continue processing the data section. *
		FileIO.FioSeekTo(old_pos, FileIO.SEEK_SET);

	} */

	static boolean initialized = false; // XXX yikes
	static final String [] _newgrf_files = //new String[32];
		{
				"russian.grf",	
				"xussr.grf"	
		};


	public static void LoadNewGRF(int load_index, int file_index)
	{
		int stage;

		if (!initialized) {
			InitializeGRFSpecial();
			initialized = true;
		}

		ResetNewGRFData();

		/* Load newgrf sprites
		 * in each loading stage, (try to) open each file specified in the config
		 * and load information from it. */
		_custom_sprites_base = load_index;
		for (stage = 0; stage < 2; stage++) {
			int slot = file_index;
			int j;

			//_cur_stage = stage;
			_cur_spriteid = load_index;
			for (j = 0; j != _newgrf_files.length && _newgrf_files[j] != null; j++) {
				if (!FileIO.FiosCheckFileExists(_newgrf_files[j])) {
					// TODO: usrerror()
					Global.error("NewGRF file missing: %s", _newgrf_files[j]);
				}
				if (stage == 0) InitNewGRFFile(_newgrf_files[j], _cur_spriteid);
				LoadNewGRFFile(_newgrf_files[j], slot++, stage);
				Global.DEBUG_spritecache( 2, "Currently %i sprites are loaded", load_index);
			}
		}

		// Pre-calculate all refit masks after loading GRF files
		// [dz] call later 
		// TODO return and fix me 
		//CalculateRefitMasks();
	}






	public GRFFile getNext() {		return next;	}
	public String getFilename() {		return filename;	}
	public int getGrfid() {		return grfid;	}

}











