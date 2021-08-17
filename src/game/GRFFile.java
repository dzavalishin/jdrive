package game;

import java.util.Arrays;

import game.ids.CargoID;
import game.ids.EngineID;
import game.ids.SpriteID;
import game.struct.EngineInfo;
import game.util.BitOps;
import game.util.Pixel;

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
	String filename;
	int grfid;
	int flags;
	int sprite_offset;
	//SpriteID first_spriteset; ///< Holds the first spriteset's sprite offset.
	int first_spriteset; ///< Holds the first spriteset's sprite offset.
	GRFFile next;

	/* A sprite group contains all sprites of a given vehicle (or multiple
	 * vehicles) when carrying given cargo. It consists of several sprite
	 * sets.  Group ids are refered as "cargo id"s by TTDPatch
	 * documentation, contributing to the global confusion.
	 *
	 * A sprite set contains all sprites of a given vehicle carrying given
	 * cargo at a given *stage* - that is usually its load stage. Ie. you
	 * can have a spriteset for an empty wagon, wagon full of coal,
	 * half-filled wagon etc.  Each spriteset contains eight sprites (one
	 * per direction) or four sprites if the vehicle is symmetric. */

	int spriteset_start;
	int spriteset_numsets;
	int spriteset_numents;
	int spriteset_feature;

	int spritegroups_count;
	SpriteGroup [][] spritegroups;

	StationSpec stations[] = new StationSpec[256];

	int param[] = new int[0x80];
	int param_end; /// one more than the highest set parameter




	static int _custom_sprites_base = Integer.MAX_VALUE; // [dz] added MAX_VALUE for checkis for image id below it work
	static int _skip_sprites; // XXX
	static int _file_index; // XXX
	//static  int _traininfo_vehicle_pitch;

	static GRFFile _cur_grffile;
	static GRFFile _first_grffile;
	static int _cur_spriteid;
	static int _cur_stage;

	/* 32 * 8 = 256 flags. Apparently TTDPatch uses this many.. */
	static int[] _ttdpatch_flags = new int[8];







	static final int _vehcounts[] = {
			/* GSF_TRAIN */    Global.NUM_TRAIN_ENGINES,
			/* GSF_ROAD */     Global.NUM_ROAD_ENGINES,
			/* GSF_SHIP */     Global.NUM_SHIP_ENGINES,
			/* GSF_AIRCRAFT */ Global.NUM_AIRCRAFT_ENGINES
	};

	static final int _vehshifts[] = {
			/* GSF_TRAIN */    0,
			/* GSF_ROAD */     Global.ROAD_ENGINES_INDEX,
			/* GSF_SHIP */     Global.SHIP_ENGINES_INDEX,
			/* GSF_AIRCRAFT */ Global.AIRCRAFT_ENGINES_INDEX,
	};



	//enum grfspec_feature {
	public static final int GSF_TRAIN		= 0;
	public static final int GSF_ROAD		= 1;
	public static final int GSF_SHIP		= 2;
	public static final int GSF_AIRCRAFT	= 3;
	public static final int GSF_STATION		= 4;
	public static final int GSF_CANAL		= 5;
	public static final int GSF_BRIDGE		= 6;
	public static final int GSF_TOWNHOUSE	= 7;
	public static final int GSF_GLOBALVAR	= 8;
	//}		


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

		export_severity = 2 - (severity == severity.GMS_FATAL ? 2 : severity.ordinal());
		Global.DEBUG_grf( export_severity, "[%s][%s] %s", _cur_grffile.filename, severitystr[severity.ordinal()], buf);
	}










	static GRFFile GetFileByGRFID(int grfid)
	{
		GRFFile file;

		for (file = _first_grffile; file != null; file = file.next) {
			if (file.grfid == grfid) break;
		}
		return file;
	}

	static GRFFile GetFileByFilename(final String filename)
	{
		GRFFile file;

		for (file = _first_grffile; file != null; file = file.next) {
			if( file.filename.equals(filename) ) 
				break;
		}
		return file;
	}


	//typedef boolean (*VCI_Handler)(int engine, int numinfo, int prop, byte **buf, int len);


	static void dewagonize(int condition, int engine)
	{
		EngineInfo ei = Global._engine_info[engine];
		RailVehicleInfo rvi = Global._rail_vehicle_info[engine];

		if (condition != 0) {
			ei.unk2 &= ~0x80;
			rvi.flags &= ~2;
		} else {
			ei.unk2 |= 0x80;
			rvi.flags |= 2;
		}
	}

	static int [] cargo_allowed		= new int[Global.TOTAL_NUM_ENGINES];
	static int [] cargo_disallowed	= new int[Global.TOTAL_NUM_ENGINES];


	static boolean RailVehicleChangeInfo(int engine, int numinfo, int prop, DataLoader bufp, int len)
	{
		//EngineInfo ei = Global._engine_info[engine];
		//RailVehicleInfo rvi = Global._rail_vehicle_info[engine];
		EngineInfo [] ei = Global._engine_info;
		RailVehicleInfo [] rvi = Global._rail_vehicle_info;
		// byte *buf = *bufp;
		int i;
		int e = engine;
		boolean ret = false;

		switch (prop) {
		case 0x05: { /* Track type */
			for (i = 0; i < numinfo; i++) {
				byte tracktype = bufp.grf_load_byte();

				ei[e+i].railtype = tracktype;
			}
		} break;
		case 0x08: { /* AI passenger service */
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = true;
		} break;
		case 0x09: { /* Speed */
			for (i = 0; i < numinfo; i++) {
				int speed = bufp.grf_load_word();
				if (speed == 0xFFFF)
					speed = 0;

				rvi[e+i].max_speed = speed;
			}
		} break;
		case 0x0B: { /* Power */
			for (i = 0; i < numinfo; i++) {
				int power = bufp.grf_load_word();

				if(0 != (rvi[e+i].flags & Engine.RVI_MULTIHEAD) )
					power /= 2;

				rvi[e+i].power = power;
				dewagonize(power, engine + i);
			}
		} break;
		case 0x0D: { /* Running cost factor */
			for (i = 0; i < numinfo; i++) {
				byte runcostfact = bufp.grf_load_byte();

				rvi[e+i].running_cost_base = runcostfact;
				dewagonize(runcostfact, engine + i);
			}
		} break;
		case 0x0E: { /* Running cost base */
			for (i = 0; i < numinfo; i++) {
				int base = bufp.grf_load_dword();

				switch (base) {
				case 0x4C30: rvi[e+i].engclass = 0; break;
				case 0x4C36: rvi[e+i].engclass = 1; break;
				case 0x4C3C: rvi[e+i].engclass = 2; break;
				}
				dewagonize(base, engine + i);
			}
		} break;
		case 0x12: { /* Sprite ID */
			for (i = 0; i < numinfo; i++) {
				byte spriteid = bufp.grf_load_byte();

				rvi[e+i].image_index = spriteid;
			}
		} break;
		case 0x13: { /* Dual-headed */
			for (i = 0; i < numinfo; i++) {
				byte dual = bufp.grf_load_byte();

				if (dual != 0) {
					if (0==(rvi[e+i].flags & Engine.RVI_MULTIHEAD)) // adjust power if needed
						rvi[e+i].power /= 2;
					rvi[e+i].flags |= Engine.RVI_MULTIHEAD;
				} else {
					if(0 != (rvi[e+i].flags & Engine.RVI_MULTIHEAD) ) // adjust power if needed
						rvi[e+i].power *= 2;
					rvi[e+i].flags &= ~Engine.RVI_MULTIHEAD;
				}
			}
		} break;
		case 0x14: { /* Cargo capacity */
			for (i = 0; i < numinfo; i++) {
				byte capacity = bufp.grf_load_byte();

				rvi[e+i].capacity = capacity;
			}
		} break;
		case 0x15: { /* Cargo type */
			for (i = 0; i < numinfo; i++) {
				byte ctype = bufp.grf_load_byte();

				rvi[e+i].cargo_type = ctype;
			}
		} break;
		case 0x16: { /* Weight */
			for (i = 0; i < numinfo; i++) {
				byte weight = bufp.grf_load_byte();

				rvi[e+i].weight = BitOps.RETSB(rvi[e+i].weight, 0, 8, weight);
			}
		} break;
		case 0x17: { /* Cost factor */
			for (i = 0; i < numinfo; i++) {
				byte cfactor = bufp.grf_load_byte();

				rvi[e+i].base_cost = cfactor;
			}
		} break;
		case 0x18: { /* AI rank */
			/* TODO: _railveh_score should be merged to _rail_vehicle_info. */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = true;
		} break;
		case 0x19: { /* Engine traction type */
			/* TODO: What do the individual numbers mean?
			 * XXX: And in what base are they, in fact? --pasky */
			for (i = 0; i < numinfo; i++) {
				byte traction = bufp.grf_load_byte();
				int engclass;

				if (traction <= 0x07)
					engclass = 0;
				else if (traction <= 0x27)
					engclass = 1;
				else if (traction <= 0x31)
					engclass = 2;
				else
					break;

				rvi[e+i].engclass = engclass;
			}
		} break;
		case 0x1B: { /* Powered wagons power bonus */
			for (i = 0; i < numinfo; i++) {
				int wag_power = bufp.grf_load_word();

				rvi[e+i].pow_wag_power = wag_power;
			}
		} break;
		case 0x1D: { /* Refit cargo */
			for (i = 0; i < numinfo; i++) {
				int refit_mask = bufp.grf_load_dword();

				Global._engine_info[engine + i].refit_mask = refit_mask;
			}
		} break;
		case 0x1E: { /* Callback */
			for (i = 0; i < numinfo; i++) {
				byte callbacks = bufp.grf_load_byte();

				rvi[e+i].callbackmask = callbacks;
			}
		} break;
		case 0x21: { /* Shorter vehicle */
			for (i = 0; i < numinfo; i++) {
				byte shorten_factor = bufp.grf_load_byte();

				rvi[e+i].shorten_factor = shorten_factor;
			}
		} break;
		case 0x22: { /* Visual effect */
			// see note in engine.h about rvi.visual_effect
			for (i = 0; i < numinfo; i++) {
				byte visual = bufp.grf_load_byte();

				rvi[e+i].visual_effect = visual;
			}
		} break;
		case 0x23: { /* Powered wagons weight bonus */
			for (i = 0; i < numinfo; i++) {
				byte wag_weight = bufp.grf_load_byte();

				rvi[e+i].pow_wag_weight = wag_weight;
			}
		} break;
		case 0x24: { /* High byte of vehicle weight */
			for (i = 0; i < numinfo; i++) {
				byte weight = bufp.grf_load_byte();

				if (weight > 4) {
					grfmsg(severity.GMS_NOTICE, "RailVehicleChangeInfo: Nonsensical weight of %d tons, ignoring.", weight << 8);
				} else {
					rvi[e+i].weight = BitOps.RETSB(rvi[e+i].weight, 8, 8, weight);
				}
			}
		} break;
		case 0x28: { /* Cargo classes allowed */
			for (i = 0; i < numinfo; i++) {
				cargo_allowed[engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x29: { /* Cargo classes disallowed */
			for (i = 0; i < numinfo; i++) {
				cargo_disallowed[engine + i] = bufp.grf_load_word();
			}
		} break;
		/* TODO */
		/* Fall-through for unimplemented one byte long properties. */
		case 0x1A:	/* Sort order */
		case 0x1C:	/* Refit cost */
		case 0x1F:	/* Tractive effort */
		case 0x20:	/* Air drag */
		case 0x25:	/* User-defined bit mask to set when checking veh. var. 42 */
		case 0x26:	/* Retire vehicle early */
		{
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = true;
		}	break;
		default:
			ret = true;
		}
		// *bufp = buf;
		return ret;
	}

	static boolean RoadVehicleChangeInfo(int engine, int numinfo, int prop, DataLoader bufp, int len)
	{
		//RoadVehicleInfo rvi = Global._road_vehicle_info[engine];
		RoadVehicleInfo [] rvi = Global._road_vehicle_info;
		// byte *buf = *bufp;
		int i;
		int e = engine;
		boolean ret = false;

		switch (prop) {
		case 0x08: { /* Speed */
			for (i = 0; i < numinfo; i++) {
				byte speed = bufp.grf_load_byte();

				rvi[e+i].max_speed = speed; // ?? units
			}
		} break;
		case 0x09: { /* Running cost factor */
			for (i = 0; i < numinfo; i++) {
				byte runcost = bufp.grf_load_byte();

				rvi[e+i].running_cost = runcost;
			}
		} break;
		case 0x0A: { /* Running cost base */
			/* TODO: I have no idea. --pasky */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_dword();
			}
			ret = true;
		} break;
		case 0x0E: { /* Sprite ID */
			for (i = 0; i < numinfo; i++) {
				byte spriteid = bufp.grf_load_byte();

				if (spriteid == 0xFF)
					spriteid = (byte) 0xFD; // cars have different custom id in the GRF file

				rvi[e+i].image_index = spriteid;
			}
		} break;
		case 0x0F: { /* Cargo capacity */
			for (i = 0; i < numinfo; i++) {
				int capacity = bufp.grf_load_byte();

				rvi[e+i].capacity = capacity;
			}
		} break;
		case 0x10: { /* Cargo type */
			for (i = 0; i < numinfo; i++) {
				byte cargo = bufp.grf_load_byte();

				rvi[e+i].cargo_type = cargo;
			}
		} break;
		case 0x11: { /* Cost factor */
			for (i = 0; i < numinfo; i++) {
				byte cost_factor = bufp.grf_load_byte();

				rvi[e+i].base_cost = cost_factor; // ?? is it base_cost?
			}
		} break;
		case 0x12: { /* SFX */
			for (i = 0; i < numinfo; i++) {
				byte sfx = bufp.grf_load_byte();

				rvi[e+i].sfx = sfx;
			}
		} break;
		case 0x13:      /* Power in 10hp */
		case 0x14:      /* Weight in 1/4 tons */
		case 0x15:      /* Speed in mph*0.8 */
			/* TODO: Support for road vehicles realistic power
			 * computations (called rvpower in TTDPatch) is just
			 * missing in OTTD yet. --pasky */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = true;
			break;
		case 0x16: { /* Cargos available for refitting */
			for (i = 0; i < numinfo; i++) {
				int refit_mask = bufp.grf_load_dword();

				Global._engine_info[Global.ROAD_ENGINES_INDEX + engine + i].refit_mask = refit_mask;
			}
		} break;
		case 0x1D: { /* Cargo classes allowed */
			for (i = 0; i < numinfo; i++) {
				cargo_allowed[Global.ROAD_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x1E: { /* Cargo classes disallowed */
			for (i = 0; i < numinfo; i++) {
				cargo_disallowed[Global.ROAD_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x17:      /* Callback */
		case 0x18:      /* Tractive effort */
		case 0x19:      /* Air drag */
		case 0x1A:      /* Refit cost */
		case 0x1B:      /* Retire vehicle early */
		case 0x1C:      /* Miscellaneous flags */
		{
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = true;
			break;
		}
		default:
			ret = true;
		}

		// *bufp = buf;
		return ret;
	}

	static boolean ShipVehicleChangeInfo(int engine, int numinfo, int prop, DataLoader bufp, int len)
	{
		ShipVehicleInfo [] svi = Global._ship_vehicle_info;
		// byte *buf = *bufp;
		int i;
		int e = engine;
		boolean ret = false;

		//printf("e %x prop %x?\n", engine, prop);
		switch (prop) {
		case 0x08: {	/* Sprite ID */
			for (i = 0; i < numinfo; i++) {
				byte spriteid = bufp.grf_load_byte();

				if (spriteid == 0xFF)
					spriteid = (byte) 0xFD; // ships have different custom id in the GRF file

				svi[e+i].image_index = spriteid;
			}
		}	break;
		case 0x09: {	/* Refittable */
			for (i = 0; i < numinfo; i++) {
				byte refittable = bufp.grf_load_byte();

				svi[e+i].refittable = refittable;
			}
		}	break;
		case 0x0A: {	/* Cost factor */
			for (i = 0; i < numinfo; i++) {
				byte cost_factor = bufp.grf_load_byte();

				svi[e+i].base_cost = cost_factor; // ?? is it base_cost?
			}
		}	break;
		case 0x0B: {	/* Speed */
			for (i = 0; i < numinfo; i++) {
				byte speed = bufp.grf_load_byte();

				svi[e+i].max_speed = speed; // ?? units
			}
		}	break;
		case 0x0C: { /* Cargo type */
			for (i = 0; i < numinfo; i++) {
				byte cargo = bufp.grf_load_byte();

				// XXX: Need to consult this with patchman yet.
				if(false) {
					// Documentation claims this is already the
					// per-landscape cargo type id, but newships.grf
					// assume otherwise.
					cargo = local_cargo_id_ctype[cargo];
				}
				svi[e+i].cargo_type = cargo;
			}
		}	break;
		case 0x0D: {	/* Cargo capacity */
			for (i = 0; i < numinfo; i++) {
				int capacity = bufp.grf_load_word();

				svi[e+i].capacity = capacity;
			}
		}	break;
		case 0x0F: {	/* Running cost factor */
			for (i = 0; i < numinfo; i++) {
				byte runcost = bufp.grf_load_byte();

				svi[e+i].running_cost = runcost;
			}
		} break;
		case 0x10: {	/* SFX */
			for (i = 0; i < numinfo; i++) {
				byte sfx = bufp.grf_load_byte();

				svi[e+i].sfx = sfx;
			}
		}	break;
		case 0x11: {	/* Cargos available for refitting */
			for (i = 0; i < numinfo; i++) {
				int refit_mask = bufp.grf_load_dword();

				Global._engine_info[Global.SHIP_ENGINES_INDEX + engine + i].refit_mask = refit_mask;
			}
		}	break;
		case 0x18: { /* Cargo classes allowed */
			for (i = 0; i < numinfo; i++) {
				cargo_allowed[Global.SHIP_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x19: { /* Cargo classes disallowed */
			for (i = 0; i < numinfo; i++) {
				cargo_disallowed[Global.SHIP_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x12: /* Callback */
		case 0x13: /* Refit cost */
		case 0x14: /* Ocean speed fraction */
		case 0x15: /* Canal speed fraction */
		case 0x16: /* Retire vehicle early */
		case 0x17: /* Miscellaneous flags */
		{
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = true;
		}	break;
		default:
			ret = true;
		}

		// *bufp = buf;
		return ret;
	}


















	static boolean AircraftVehicleChangeInfo(int engine, int numinfo, int prop, DataLoader bufp, int len)
	{
		AircraftVehicleInfo [] avi = Global._aircraft_vehicle_info;
		// byte *buf = *bufp;
		int e = engine;
		int i;
		boolean ret = false;

		//printf("e %x prop %x?\n", engine, prop);
		switch (prop) {
		case 0x08: {	/* Sprite ID */
			for (i = 0; i < numinfo; i++) {
				byte spriteid = bufp.grf_load_byte();

				if (spriteid == 0xFF)
					spriteid = (byte) 0xFD; // ships have different custom id in the GRF file

				avi[e+i].image_index = spriteid;
			}
		}	break;
		case 0x09: {	/* Helicopter */
			for (i = 0; i < numinfo; i++) {
				byte heli = bufp.grf_load_byte();
				avi[e+i].subtype &= ~0x01; // remove old property
				avi[e+i].subtype |= (heli == 0) ? 0 : 1;
			}
		}	break;
		case 0x0A: {	/* Large */
			for (i = 0; i < numinfo; i++) {
				byte large = bufp.grf_load_byte();
				avi[e+i].subtype &= ~0x02; // remove old property
				avi[e+i].subtype |= (large == 1) ? 2 : 0;
			}
		}	break;
		case 0x0B: {	/* Cost factor */
			for (i = 0; i < numinfo; i++) {
				byte cost_factor = bufp.grf_load_byte();

				avi[e+i].base_cost = cost_factor; // ?? is it base_cost?
			}
		}	break;
		case 0x0C: {	/* Speed */
			for (i = 0; i < numinfo; i++) {
				byte speed = bufp.grf_load_byte();

				avi[e+i].max_speed = speed; // ?? units
			}
		}	break;
		case 0x0D: {	/* Acceleration */
			for (i = 0; i < numinfo; i++) {
				byte accel = bufp.grf_load_byte();

				avi[e+i].acceleration = accel;
			}
		} break;
		case 0x0E: {	/* Running cost factor */
			for (i = 0; i < numinfo; i++) {
				byte runcost = bufp.grf_load_byte();

				avi[e+i].running_cost = runcost;
			}
		} break;
		case 0x0F: {	/* Passenger capacity */
			for (i = 0; i < numinfo; i++) {
				int capacity = bufp.grf_load_word();

				avi[e+i].passenger_capacity = capacity;
			}
		}	break;
		case 0x11: {	/* Mail capacity */
			for (i = 0; i < numinfo; i++) {
				byte capacity = bufp.grf_load_byte();

				avi[e+i].mail_capacity = capacity;
			}
		}	break;
		case 0x12: {	/* SFX */
			for (i = 0; i < numinfo; i++) {
				byte sfx = bufp.grf_load_byte();

				avi[e+i].sfx = sfx;
			}
		}	break;
		case 0x13: {	/* Cargos available for refitting */
			for (i = 0; i < numinfo; i++) {
				int refit_mask = bufp.grf_load_dword();

				Global._engine_info[Global.AIRCRAFT_ENGINES_INDEX + engine + i].refit_mask = refit_mask;
			}
		}	break;
		case 0x18: { /* Cargo classes allowed */
			for (i = 0; i < numinfo; i++) {
				cargo_allowed[Global.AIRCRAFT_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x19: { /* Cargo classes disallowed */
			for (i = 0; i < numinfo; i++) {
				cargo_disallowed[Global.AIRCRAFT_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x14: /* Callback */
		case 0x15: /* Refit cost */
		case 0x16: /* Retire vehicle early */
		case 0x17: /* Miscellaneous flags */
		{
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = true;
		}	break;
		default:
			ret = true;
		}

		// *bufp = buf;
		return ret;
	}

	static boolean StationChangeInfo(int stid, int numinfo, int prop, DataLoader bufp, int len)
	{
		// byte *buf = *bufp;
		int i;
		int ret = 0;

		/* This is one single huge TODO. It doesn't handle anything more than
		 * just waypoints for now. */

		//printf("sci %d %d [0x%02x]\n", stid, numinfo, prop);
		switch (prop) {
		case 0x08:
		{	/* Class ID */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = _cur_grffile.stations[stid + i];
				int classid;

				/* classid, for a change, is always little-endian */
				//classid = *(buf++) << 24;
				//classid |= *(buf++) << 16;
				//classid |= *(buf++) << 8;
				//classid |= *(buf++);
				classid = bufp.grf_load_dword_le();

				stat.sclass = AllocateStationClass(classid);
			}
			break;
		}
		case 0x09:
		{	/* Define sprite layout */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = _cur_grffile.stations[stid + i];
				int t;

				stat.tiles = bufp.grf_load_extended();
				stat.renderdata = new DrawTileSprites[stat.tiles]; //calloc(stat.tiles, sizeof(*stat.renderdata));
				for (t = 0; t < stat.tiles; t++) {
					DrawTileSprites dts = stat.renderdata[t];
					int seq_count = 0;
					//PalSpriteID 
					int ground_sprite;

					ground_sprite = bufp.grf_load_dword();
					if (ground_sprite == 0) {
						//static final DrawTileSeqStruct empty = {0x80, 0, 0, 0, 0, 0, 0};
						//dts.seq = empty;
						dts.seq = new DrawTileSeqStruct[1];
						dts.seq[0] = new DrawTileSeqStruct(0x80, 0, 0, 0, 0, 0, 0);
						continue;
					}

					if (BitOps.HASBIT(ground_sprite, 31)) {
						// Bit 31 indicates that we should use a custom sprite.
						dts.ground_sprite = BitOps.GB(ground_sprite, 0, 15) - 0x42D;
						dts.ground_sprite += _cur_grffile.first_spriteset;
					} else {
						dts.ground_sprite = ground_sprite;
					}

					dts.seq = null;
					//while (buf < *bufp + len)
					//while (bufp.isNotEmpty())
					while (bufp.has(len))
					{
						DrawTileSeqStruct dtss;

						// no relative bounding box support
						//dts.seq = realloc((void*)dts.seq, ++seq_count * sizeof(DrawTileSeqStruct));
						++seq_count;
						dts.seq = Arrays.copyOf(dts.seq, seq_count);

						dtss = dts.seq[seq_count - 1];

						dtss.delta_x = bufp.grf_load_byte();
						if ((byte) dtss.delta_x == 0x80) break;
						dtss.delta_y = bufp.grf_load_byte();
						dtss.delta_z = bufp.grf_load_byte();
						dtss.width = bufp.grf_load_byte();
						dtss.height = bufp.grf_load_byte();
						dtss.unk = bufp.grf_load_byte();
						dtss.image = bufp.grf_load_dword() - 0x42d;
					}
				}
			}
			break;
		}
		case 0x0a:
		{	/* Copy sprite layout */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = _cur_grffile.stations[stid + i];
				byte srcid = bufp.grf_load_byte();
				final StationSpec srcstat = _cur_grffile.stations[srcid];
				int t;

				stat.tiles = srcstat.tiles;
				//stat.renderdata = calloc(stat.tiles, sizeof(*stat.renderdata));
				stat.renderdata = new DrawTileSprites[stat.tiles]; 
				for (t = 0; t < stat.tiles; t++) {
					DrawTileSprites dts = stat.renderdata[t];
					final DrawTileSprites sdts = srcstat.renderdata[t];
					final DrawTileSeqStruct  sdtss = sdts.seq;
					int seq_count = 0;

					dts.ground_sprite = sdts.ground_sprite;
					if (0 == dts.ground_sprite) {
						//static final DrawTileSeqStruct empty = {0x80, 0, 0, 0, 0, 0, 0};
						//dts.seq = empty;
						dts.seq = new DrawTileSeqStruct[1];
						dts.seq[0] = new DrawTileSeqStruct(0x80, 0, 0, 0, 0, 0, 0);
						continue;
					}

					dts.seq = null;
					while (1) {
						DrawTileSeqStruct dtss;

						// no relative bounding box support
						dts.seq = realloc((void*)dts.seq, ++seq_count * sizeof(DrawTileSeqStruct));
						dtss = (DrawTileSeqStruct*) &dts.seq[seq_count - 1];
						*dtss = *sdtss;
						if ((byte) dtss.delta_x == 0x80) break;
						sdtss++;
					}
				}
			}
			break;
		}
		case 0x0b:
		{	/* Callback */
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = 1;
			break;
		}
		case 0x0C:
		{	/* Platforms number */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = _cur_grffile.stations[stid + i];

				stat.allowed_platforms = ~bufp.grf_load_byte();
			}
			break;
		}
		case 0x0D:
		{	/* Platforms length */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = _cur_grffile.stations[stid + i];

				stat.allowed_lengths = ~bufp.grf_load_byte();
			}
			break;
		}
		case 0x0e:
		{	/* Define custom layout */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = _cur_grffile.stations[stid + i];

				while (bufp.has(len)) {
					byte length = bufp.grf_load_byte();
					byte number = bufp.grf_load_byte();
					StationLayout [] layout;
					int l, p;

					if (length == 0 || number == 0) break;

					//debug("l %d > %d ?", length, stat.lengths);
					if (length > stat.lengths) {
						stat.platforms = realloc(stat.platforms, length);
						memset(stat.platforms + stat.lengths, 0, length - stat.lengths);

						stat.layouts = realloc(stat.layouts, length * sizeof(*stat.layouts));
						//memset(stat.layouts + stat.lengths, 0,								(length - stat.lengths) * sizeof(*stat.layouts));
						Arrays.fill(stat.layouts, stat.lengths, length - stat.lengths, null );

						stat.lengths = length;
					}
					l = length - 1; // index is zero-based

					//debug("p %d > %d ?", number, stat.platforms[l]);
					if (number > stat.platforms[l]) {
						stat.layouts[l] = realloc(stat.layouts[l],								number * sizeof(**stat.layouts));
						
						// We expect null being 0 here, but C99 guarantees that.
						memset(stat.layouts[l] + stat.platforms[l], 0,
								(number - stat.platforms[l]) * sizeof(**stat.layouts));

						stat.platforms[l] = number;
					}

					p = 0;
					layout = new StationLayout[length * number]; // malloc(length * number);
					for (l = 0; l < length; l++)
						for (p = 0; p < number; p++)
							layout[l * number + p] = bufp.grf_load_byte();

					l--;
					p--;
					assert(p >= 0);
					free(stat.layouts[l][p]);
					stat.layouts[l][p] = layout;
				}
			}
			break;
		}
		case 0x0f:
		{	/* Copy custom layout */
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_byte();
			}
			ret = 1;
			break;
		}
		case 0x10:
		{	/* Little/lots cargo threshold */
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_word();
			}
			ret = 1;
			break;
		}
		case 0x11:
		{	/* Pylon placement */
			/* TODO; makes sense only for electrified tracks */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_word();
			}
			ret = 1;
			break;
		}
		case 0x12:
		{	/* Cargo types for random triggers */
			/* TODO */
			for (i = 0; i < numinfo; i++) {
				bufp.grf_load_dword();
			}
			ret = 1;
			break;
		}
		default:
			ret = 1;
			break;
		}

		// *bufp = buf;
		return ret;
	}

	static boolean BridgeChangeInfo(int brid, int numinfo, int prop, DataLoader bufp, int len)
	{
		// byte *buf = *bufp;
		int i;
		boolean ret = false;

		switch (prop) {
		case 0x08: /* Year of availability */
			for (i = 0; i < numinfo; i++) {
				Bridge._bridge[brid + i].avail_year = bufp.grf_load_byte();
			}
			break;

		case 0x09: /* Minimum length */
			for (i = 0; i < numinfo; i++) {
				Bridge._bridge[brid + i].min_length = bufp.grf_load_byte();
			}
			break;

		case 0x0A: /* Maximum length */
			for (i = 0; i < numinfo; i++) {
				Bridge._bridge[brid + i].max_length = bufp.grf_load_byte();
			}
			break;

		case 0x0B: /* Cost factor */
			for (i = 0; i < numinfo; i++) {
				Bridge._bridge[brid + i].price = bufp.grf_load_byte();
			}
			break;

		case 0x0C: /* Maximum speed */
			for (i = 0; i < numinfo; i++) {
				Bridge._bridge[brid + i].speed = bufp.grf_load_word();
			}
			break;

		case 0x0D: /* Bridge sprite tables */
			for (i = 0; i < numinfo; i++) {
				Bridge bridge = Bridge._bridge[brid + i];
				byte tableid = bufp.grf_load_byte();
				byte numtables = bufp.grf_load_byte();
				byte table, sprite;

				if (bridge.sprite_table == null) {
					/* Allocate memory for sprite table pointers and set to null */
					//bridge.sprite_table = malloc(7 * sizeof(*bridge.sprite_table));
					bridge.sprite_table = new int[7][32]; // malloc(7 * sizeof(*bridge.sprite_table));
					for (table = 0; table < 7; table++)
						bridge.sprite_table[table] = null;
				}

				for (table = tableid; table < tableid + numtables; table++) {
					if (table < 7) {
						//if (bridge.sprite_table[table] == null) {
						//	bridge.sprite_table[table] = malloc(32 * sizeof(**bridge.sprite_table));
						//}

						for (sprite = 0; sprite < 32; sprite++)
							bridge.sprite_table[table][sprite] = bufp.grf_load_dword();
					} else {
						grfmsg(severity.GMS_WARN, "BridgeChangeInfo: Table %d >= 7, skipping.", table);
						// Skip past invalid data.
						for (sprite = 0; sprite < 32; sprite++)
							bufp.grf_load_dword();
					}
				}
			}
			break;

		case 0x0E: /* Flags; bit 0 - disable far pillars */
			for (i = 0; i < numinfo; i++) {
				Bridge._bridge[brid + i].flags = bufp.grf_load_byte();
			}
			break;

		default:
			ret = true;
		}

		// *bufp = buf;
		return ret;
	}

	static boolean GlobalVarChangeInfo(int gvid, int numinfo, int prop, DataLoader bufp, int len)
	{
		// byte *buf = *bufp;
		int i;
		boolean ret = false;

		switch (prop) {
		case 0x08: { /* Cost base factor */
			for (i = 0; i < numinfo; i++) {
				byte factor = bufp.grf_load_byte();
				int price = gvid + i;

				if (price < Global.NUM_PRICES) {
					SetPriceBaseMultiplier(price, factor);
				} else {
					grfmsg(severity.GMS_WARN, "GlobalVarChangeInfo: Price %d out of range, ignoring.", price);
				}
			}
		} break;
		default:
			ret = true;
		}
		// *bufp = buf;
		return ret;
	}

	static final VCI_Handler handler[] = {
			/* GSF_TRAIN */    GRFFile::RailVehicleChangeInfo,
			/* GSF_ROAD */     GRFFile::RoadVehicleChangeInfo,
			/* GSF_SHIP */     GRFFile::ShipVehicleChangeInfo,
			/* GSF_AIRCRAFT */ GRFFile::AircraftVehicleChangeInfo,
			/* GSF_STATION */  GRFFile::StationChangeInfo,
			/* GSF_CANAL */    null,
			/* GSF_BRIDGE */   GRFFile::BridgeChangeInfo,
			/* GSF_TOWNHOUSE */null,
			/* GSF_GLOBALVAR */GRFFile::GlobalVarChangeInfo,
	};

	/* Action 0x00 */
	static void VehicleChangeInfo(DataLoader bufp, int len)
	{
		//byte *bufend = buf + len;
		int i;

		/* <00> <feature> <num-props> <num-info> <id> (<property <new-info>)...
		 *
		 * B feature       0, 1, 2 or 3 for trains, road vehicles, ships or planes
		 *                 4 for defining new train station sets
		 * B num-props     how many properties to change per vehicle/station
		 * B num-info      how many vehicles/stations to change
		 * B id            ID of first vehicle/station to change, if num-info is
		 *                 greater than one, this one and the following
		 *                 vehicles/stations will be changed
		 * B property      what property to change, depends on the feature
		 * V new-info      new bytes of info (variable size; depends on properties) */
		/* TODO: Bridges, town houses. */


		byte feature;
		byte numprops;
		byte numinfo;
		byte engine;
		EngineInfo [] ei = null;

		if (len == 1) {
			Global.DEBUG_grf( 8, "Silently ignoring one-byte special sprite 0x00.");
			return;
		}

		bufp.check_length( 6, "VehicleChangeInfo");
		bufp.grf_load_byte();
		feature = bufp.grf_load_byte(); // buf[1];
		numprops = bufp.grf_load_byte(); // buf[2];
		numinfo = bufp.grf_load_byte(); // buf[3];
		engine = bufp.grf_load_byte(); // buf[4];

		Global.DEBUG_grf( 6, "VehicleChangeInfo: Feature %d, %d properties, to apply to %d+%d",
				feature, numprops, engine, numinfo);

		if (feature >= handler.length || handler[feature] == null) {
			grfmsg(severity.GMS_WARN, "VehicleChangeInfo: Unsupported feature %d, skipping.", feature);
			return;
		}

		int e;
		if (feature <= GSF_AIRCRAFT) {
			if (engine + numinfo > _vehcounts[feature]) {
				grfmsg(severity.GMS_ERROR, "VehicleChangeInfo: Last engine ID %d out of bounds (max %d), skipping.", engine + numinfo, _vehcounts[feature]);
				return;
			}
			//ei = Global._engine_info[engine + _vehshifts[feature]];
			ei = Global._engine_info;
			e = engine + _vehshifts[feature];
		}

		//buf += 5;

		//while (numprops-- && buf < bufend) 
		while (numprops-- && bufp.has(len)) 
		{
			byte prop = bufp.grf_load_byte();
			boolean ignoring = false;

			switch (feature) {
			case GSF_TRAIN:
			case GSF_ROAD:
			case GSF_SHIP:
			case GSF_AIRCRAFT:
				/* Common properties for vehicles */
				switch (prop) {
				case 0x00: { /* Introduction date */
					for (i = 0; i < numinfo; i++) {
						int date = bufp.grf_load_word();

						ei[e+i].base_intro = date;
					}
				}	break;
				case 0x02: { /* Decay speed */
					for (i = 0; i < numinfo; i++) {
						byte decay = bufp.grf_load_byte();

						ei[e+i].unk2 &= 0x80;
						ei[e+i].unk2 |= decay & 0x7f;
					}
				}	break;
				case 0x03: { /* Vehicle life */
					for (i = 0; i < numinfo; i++) {
						byte life = bufp.grf_load_byte();

						ei[e+i].lifelength = life;
					}
				}	break;
				case 0x04: { /* Model life */
					for (i = 0; i < numinfo; i++) {
						byte life = bufp.grf_load_byte();

						ei[e+i].base_life = life;
					}
				}	break;
				case 0x06: { /* Climates available */
					for (i = 0; i < numinfo; i++) {
						byte climates = bufp.grf_load_byte();

						ei[e+i].climates = climates;
					}
				}	break;
				case 0x07: { /* Loading speed */
					/* TODO */
					/* Hyronymus explained me what does
					 * this mean and insists on having a
					 * credit ;-). --pasky */
					/* TODO: This needs to be supported by
					 * LoadUnloadVehicle() first. */
					for (i = 0; i < numinfo; i++) {
						bufp.grf_load_byte();
					}
					ignoring = true;
					break;
				}

				default:
					if (handler[feature].accept(engine, numinfo, prop, bufp, bufend - buf))
						ignoring = true;
					break;
				}
				break;

			default:
				if (handler[feature].accept(engine, numinfo, prop, bufp, bufend - buf))
					ignoring = true;
				break;
			}

			if (ignoring)
				grfmsg(severity.GMS_NOTICE, "VehicleChangeInfo: Ignoring property %x (not implemented).", prop);
		}
	}


	/**
	 * Creates a spritegroup representing a callback result
	 * @param value The value that was used to represent this callback result
	 * @return A spritegroup representing that callback result
	 */
	static SpriteGroup NewCallBackResultSpriteGroup(int value)
	{
		SpriteGroup group = new SpriteGroup(); //calloc(1, sizeof(*group));

		group.type = SpriteGroupType.SGT_CALLBACK;

		// Old style callback results have the highest byte 0xFF so signify it is a callback result
		// New style ones only have the highest bit set (allows 15-bit results, instead of just 8)
		if ((value >> 8) == 0xFF)
			value &= 0xFF;
		else
			value &= ~0x8000;

		group.g.callback.result = value;

		return group;
	}

	/**
	 * Creates a spritegroup representing a sprite number result.
	 * @param value The sprite number.
	 * @param sprites The number of sprites per set.
	 * @return A spritegroup representing the sprite number result.
	 */
	static SpriteGroup NewResultSpriteGroup(int value, byte sprites)
	{
		SpriteGroup group = new SpriteGroup();
		group.type = SpriteGroupType.SGT_RESULT;
		group.g.result.result = value;
		group.g.result.sprites = sprites;
		return group;
	}

	/* Action 0x01 */
	static void NewSpriteSet(DataLoader bufp, int len)
	{
		/* <01> <feature> <num-sets> <num-ent>
		 *
		 * B feature       feature to define sprites for
		 *                 0, 1, 2, 3: veh-type, 4: train stations
		 * B num-sets      number of sprite sets
		 * E num-ent       how many entries per sprite set
		 *                 For vehicles, this is the number of different
		 *                         vehicle directions in each sprite set
		 *                         Set num-dirs=8, unless your sprites are symmetric.
		 *                         In that case, use num-dirs=4.
		 *                 For stations, must be 12 (hex) for the eighteen
		 *                         different sprites that make up a station */
		/* TODO: No stations support. */
		byte feature;
		int num_sets;
		int num_ents;
		int i;

		bufp.check_length(4, "NewSpriteSet");
		bufp.grf_load_byte();
		
		feature  = bufp.grf_load_byte();
		num_sets = bufp.grf_load_byte();
		num_ents = bufp.grf_load_extended();

		_cur_grffile.spriteset_start = _cur_spriteid;
		_cur_grffile.spriteset_feature = feature;
		_cur_grffile.spriteset_numsets = num_sets;
		_cur_grffile.spriteset_numents = num_ents;

		Global.DEBUG_grf( 7, 
				"New sprite set at %d of type %d, "+
						"consisting of %d sets with %d views each (total %d)",
						_cur_spriteid, feature, num_sets, num_ents, num_sets * num_ents
				);

		for (i = 0; i < num_sets * num_ents; i++) {
			SpriteCache.LoadNextSprite(_cur_spriteid++, _file_index);
		}
	}

	/* Action 0x02 */
	static void NewSpriteGroup(DataLoader bufp, int len)
	{
		//byte *bufend = buf + len;

		/* <02> <feature> <set-id> <type/num-entries> <feature-specific-data...>
		 *
		 * B feature       see action 1
		 * B set-id        ID of this particular definition
		 * B type/num-entries
		 *                 if 80 or greater, this is a randomized or variational
		 *                 list definition, see below
		 *                 otherwise it specifies a number of entries, the exact
		 *                 meaning depends on the feature
		 * V feature-specific-data (huge mess, don't even look it up --pasky) */
		/* TODO: No 0x80-types (ugh). */
		/* TODO: Also, empty sprites aren't handled for now. Need to investigate
		 * the "opacity" rules for these, that is which sprite to fall back to
		 * when. --pasky */
		byte feature;
		byte setid;
		/* XXX: For stations, these two are "little cargo" and "lotsa cargo" sets. */
		byte numloaded;
		byte numloading;
		SpriteGroup group;
		//RealSpriteGroup rg;
		//byte *loaded_ptr;
		//byte *loading_ptr;
		//int i;

		bufp.check_length( 5, "NewSpriteGroup");
		feature = bufp.r(1);
		setid = bufp.r(2);
		numloaded = bufp.r(3);
		numloading = bufp.r(4);

		if (setid >= _cur_grffile.spritegroups_count) {
			// Allocate memory for new sprite group references.
			//_cur_grffile.spritegroups = realloc(_cur_grffile.spritegroups, (setid + 1) * sizeof(*_cur_grffile.spritegroups));
			_cur_grffile.spritegroups = Arrays.copyOf(_cur_grffile.spritegroups, setid + 1);
			// Initialise new space to null
			for (; _cur_grffile.spritegroups_count < (setid + 1); _cur_grffile.spritegroups_count++)
				_cur_grffile.spritegroups[_cur_grffile.spritegroups_count] = null;
		}

		if (numloaded == 0x81 || numloaded == 0x82) {
			DeterministicSpriteGroup dg;
			int groupid;
			int i;

			// Ok, this is gonna get a little wild, so hold your breath...

			/* This stuff is getting actually evaluated in
			 * EvalDeterministicSpriteGroup(). */

			bufp.shift( 4 ); len -= 4;
			bufp.check_length( 6, "NewSpriteGroup 0x81/0x82");

			group = new SpriteGroup();
			group.type = SpriteGroupType.SGT_DETERMINISTIC;
			dg = group.g.determ;

			/* XXX: We don't free() anything, assuming that if there was
			 * some action here before, it got associated by action 3.
			 * We should perhaps keep some refcount? --pasky */

			dg.var_scope = numloaded == 0x82 ? VarSpriteGroupScope.VSG_SCOPE_PARENT : VarSpriteGroupScope.VSG_SCOPE_SELF;
			dg.variable = bufp.grf_load_byte();
			/* Variables 0x60 - 0x7F include an extra parameter */
			if (BitOps.IS_BYTE_INSIDE(dg.variable, 0x60, 0x80))
				dg.parameter = bufp.grf_load_byte();

			dg.shift_num = bufp.grf_load_byte();
			dg.and_mask = bufp.grf_load_byte();
			dg.operation = DeterministicSpriteGroupOperation.values[ dg.shift_num >> 6 ]; /* w00t */
			dg.shift_num &= 0x3F;
			if (dg.operation != DeterministicSpriteGroupOperation.DSG_OP_NONE) {
				dg.add_val = bufp.grf_load_byte();
				dg.divmod_val = bufp.grf_load_byte();
			}

			/* (groupid & 0x8000) means this is callback result. */

			dg.num_ranges = bufp.grf_load_byte();
			dg.ranges = new DeterministicSpriteGroupRange[dg.num_ranges]; //calloc(dg.num_ranges, sizeof(*dg.ranges));
			for (i = 0; i < dg.num_ranges; i++) {
				groupid = bufp.grf_load_word();
				if (BitOps.HASBIT(groupid, 15)) {
					dg.ranges[i].group = NewCallBackResultSpriteGroup(groupid);
					dg.ranges[i].group.ref_count++;
				} else if (groupid >= _cur_grffile.spritegroups_count || _cur_grffile.spritegroups[groupid] == null) {
					grfmsg(severity.GMS_WARN, "NewSpriteGroup(%02x:0x%x): Groupid %04x does not exist, leaving empty.", setid, numloaded, groupid);
					dg.ranges[i].group = null;
				} else {
					dg.ranges[i].group = _cur_grffile.spritegroups[groupid];
					dg.ranges[i].group.ref_count++;
				}

				dg.ranges[i].low = bufp.grf_load_byte();
				dg.ranges[i].high = bufp.grf_load_byte();
			}

			groupid = bufp.grf_load_word();
			if (BitOps.HASBIT(groupid, 15)) {
				dg.default_group = NewCallBackResultSpriteGroup(groupid);
				dg.default_group.ref_count++;
			} else if (groupid >= _cur_grffile.spritegroups_count || _cur_grffile.spritegroups[groupid] == null) {
				grfmsg(severity.GMS_WARN, "NewSpriteGroup(%02x:0x%x): Groupid %04x does not exist, leaving empty.", setid, numloaded, groupid);
				dg.default_group = null;
			} else {
				dg.default_group = _cur_grffile.spritegroups[groupid];
				dg.default_group.ref_count++;
			}

			/* [dz] can just skip unload? 
			if (_cur_grffile.spritegroups[setid] != null)
				UnloadSpriteGroup(&_cur_grffile.spritegroups[setid]);
			*/
			_cur_grffile.spritegroups[setid] = group;
			group.ref_count++;
			return;

		} else if (numloaded == 0x80 || numloaded == 0x83) {
			RandomizedSpriteGroup rg;
			int i;

			/* This stuff is getting actually evaluated in
			 * EvalRandomizedSpriteGroup(). */

			bufp.shift( 4 );
			len -= 4;
			bufp.check_length( 6, "NewSpriteGroup 0x80/0x83");

			group = new SpriteGroup();
			group.type = SpriteGroupType.SGT_RANDOMIZED;
			rg = group.g.random;

			/* XXX: We don't free() anything, assuming that if there was
			 * some action here before, it got associated by action 3.
			 * We should perhaps keep some refcount? --pasky */

			rg.var_scope = numloaded == 0x83 ? VarSpriteGroupScope.VSG_SCOPE_PARENT : VarSpriteGroupScope.VSG_SCOPE_SELF;

			rg.triggers = bufp.grf_load_byte();
			rg.cmp_mode = rg.triggers & 0x80;
			rg.triggers &= 0x7F;

			rg.lowest_randbit = bufp.grf_load_byte();
			rg.num_groups = bufp.grf_load_byte();

			rg.groups = new SpriteGroup[rg.num_groups]; //calloc(rg.num_groups, sizeof(*rg.groups));
			for (i = 0; i < rg.num_groups; i++) {
				int groupid = bufp.grf_load_word();

				if (BitOps.HASBIT(groupid, 15)) {
					rg.groups[i] = NewCallBackResultSpriteGroup(groupid);
					rg.groups[i].ref_count++;
				} else if (groupid >= _cur_grffile.spritegroups_count || _cur_grffile.spritegroups[groupid] == null) {
					grfmsg(severity.GMS_WARN, "NewSpriteGroup(%02x:0x%x): Groupid %04x does not exist, leaving empty.", setid, numloaded, groupid);
					rg.groups[i] = null;
				} else {
					rg.groups[i] = _cur_grffile.spritegroups[groupid];
					rg.groups[i].ref_count++;
				}
			}

			/* [dz] just ignore? 
			if (_cur_grffile.spritegroups[setid] != null)
				UnloadSpriteGroup(_cur_grffile.spritegroups[setid]);
			*/
			_cur_grffile.spritegroups[setid] = group;
			group.ref_count++;
			return;
		}

		if (0 == _cur_grffile.spriteset_start) {
			grfmsg(severity.GMS_ERROR, "NewSpriteGroup: No sprite set to work on! Skipping.");
			return;
		}

		if (_cur_grffile.spriteset_feature != feature) {
			grfmsg(severity.GMS_ERROR, "NewSpriteGroup: Group feature %x doesn't match set feature %x! Playing it risky and trying to use it anyway.", feature, _cur_grffile.spriteset_feature);
			// return; // XXX: we can't because of MB's newstats.grf --pasky
		}

		bufp.check_length(5, "NewSpriteGroup");
		bufp.shift( 5 );
		bufp.check_length(2 * numloaded, "NewSpriteGroup");
		DataLoader loaded_ptr = new DataLoader( bufp );
		DataLoader loading_ptr = new DataLoader( bufp, 2 * numloaded );

		if (_cur_grffile.first_spriteset == 0)
			_cur_grffile.first_spriteset = _cur_grffile.spriteset_start;

		if (numloaded > 16) {
			grfmsg(severity.GMS_WARN, "NewSpriteGroup: More than 16 sprites in group %x, skipping the rest.", setid);
			numloaded = 16;
		}
		if (numloading > 16) {
			grfmsg(severity.GMS_WARN, "NewSpriteGroup: More than 16 sprites in group %x, skipping the rest.", setid);
			numloading = 16;
		}

		group = new SpriteGroup();
		group.type = SpriteGroupType.SGT_REAL;
		RealSpriteGroup rg = (RealSpriteGroup) group;

		rg.sprites_per_set = _cur_grffile.spriteset_numents;
		//rg.loaded_count  = numloaded;
		//rg.loading_count = numloading;

		Global.DEBUG_grf( 6, "NewSpriteGroup: New SpriteGroup 0x%02hhx, %u views, %u loaded, %u loading, sprites %u - %u",
				setid, rg.sprites_per_set, rg.loaded_count(), rg.loading_count(),
				_cur_grffile.spriteset_start - _cur_grffile.sprite_offset,
				_cur_grffile.spriteset_start + (_cur_grffile.spriteset_numents * (numloaded + numloading)) - _cur_grffile.sprite_offset);

		for (int i = 0; i < numloaded; i++) {
			int spriteset_id = loaded_ptr.grf_load_word();
			if (BitOps.HASBIT(spriteset_id, 15)) {
				rg.loaded[i] = NewCallBackResultSpriteGroup(spriteset_id);
			} else {
				rg.loaded[i] = NewResultSpriteGroup(_cur_grffile.spriteset_start + spriteset_id * _cur_grffile.spriteset_numents, rg.sprites_per_set);
			}
			rg.loaded[i].ref_count++;
			Global.DEBUG_grf( 8, "NewSpriteGroup: + rg.loaded[%i]  = %u (subset %u)", i, rg.loaded[i].g.result.result, spriteset_id);
		}

		for (int i = 0; i < numloading; i++) {
			int spriteset_id = loading_ptr.grf_load_word();
			if (BitOps.HASBIT(spriteset_id, 15)) {
				rg.loading[i] = NewCallBackResultSpriteGroup(spriteset_id);
			} else {
				rg.loading[i] = NewResultSpriteGroup(_cur_grffile.spriteset_start + spriteset_id * _cur_grffile.spriteset_numents, rg.sprites_per_set);
			}
			rg.loading[i].ref_count++;
			Global.DEBUG_grf( 8, "NewSpriteGroup: + rg.loading[%i] = %u (subset %u)", i, rg.loading[i].g.result.result, spriteset_id);
		}

		/*
		if (_cur_grffile.spritegroups[setid] != null)
			UnloadSpriteGroup(&_cur_grffile.spritegroups[setid]);
		*/
		_cur_grffile.spritegroups[setid] = group;
		group.ref_count++;
	}

	/* Action 0x03 */
	static void NewVehicle_SpriteGroupMapping(DataLoader bufp, int len)
	{
		/* <03> <feature> <n-id> <ids>... <num-cid> [<cargo-type> <cid>]... <def-cid>
		 * id-list	:= [<id>] [id-list]
		 * cargo-list	:= <cargo-type> <cid> [cargo-list]
		 *
		 * B feature       see action 0
		 * B n-id          bits 0-6: how many IDs this definition applies to
		 *                 bit 7: if set, this is a wagon override definition (see below)
		 * B ids           the IDs for which this definition applies
		 * B num-cid       number of cargo IDs (sprite group IDs) in this definition
		 *                 can be zero, in that case the def-cid is used always
		 * B cargo-type    type of this cargo type (e.g. mail=2, wood=7, see below)
		 * W cid           cargo ID (sprite group ID) for this type of cargo
		 * W def-cid       default cargo ID (sprite group ID) */
		/* TODO: Bridges, town houses. */
		/* TODO: Multiple cargo support could be useful even for trains/cars -
		 * cargo id 0xff is used for showing images in the build train list. */

		static byte [] last_engines;
		static int last_engines_count;
		int feature;
		int idcount;
		boolean wagover;
		int cidcount;
		int c, i;

		bufp.check_length( 7, "VehicleMapSpriteGroup");
		bufp.grf_load_byte();
		feature = bufp.grf_load_ubyte(); // buf[1];
		int tmp2 = bufp.grf_load_ubyte(); // buf[2] 
		idcount = tmp2 & 0x7F; // buf[2] & 0x7F;
		wagover = (tmp2 & 0x80) == 0x80;
		bufp.check_length( 3 + idcount, "VehicleMapSpriteGroup");
		cidcount = buf[3 + idcount];
		bufp.check_length( 4 + idcount + cidcount * 3, "VehicleMapSpriteGroup");

		Global.DEBUG_grf( 6, "VehicleMapSpriteGroup: Feature %d, %d ids, %d cids, wagon override %d.",
				feature, idcount, cidcount, wagover);

		if (feature > GSF_STATION) {
			grfmsg(severity.GMS_WARN, "VehicleMapSpriteGroup: Unsupported feature %d, skipping.", feature);
			return;
		}


		if (feature == GSF_STATION) {
			// We do things differently for stations.

			for (i = 0; i < idcount; i++) 
			{
				byte stid = buf[3 + i];
				StationSpec stat = _cur_grffile.stations[stid];
				DataLoader bp = new DataLoader( bufp, 4 + idcount );

				for (c = 0; c < cidcount; c++) {
					byte ctype = bp.grf_load_byte();
					int groupid = bp.grf_load_word();

					if (groupid >= _cur_grffile.spritegroups_count || _cur_grffile.spritegroups[groupid] == null) {
						grfmsg(severity.GMS_WARN, "VehicleMapSpriteGroup: Spriteset %x out of range %x or empty, skipping.",
								groupid, _cur_grffile.spritegroups_count);
						return;
					}

					if (ctype != 0xFF) {
						/* TODO: No support for any other cargo. */
						continue;
					}

					stat.spritegroup[1] = _cur_grffile.spritegroups[groupid];
					stat.spritegroup[1].ref_count++;
				}
			}

			{
				byte *bp = buf + 4 + idcount + cidcount * 3;
				int groupid = grf_load_word(&bp);

				if (groupid >= _cur_grffile.spritegroups_count || _cur_grffile.spritegroups[groupid] == null) {
					grfmsg(severity.GMS_WARN, "VehicleMapSpriteGroup: Spriteset %x out of range %x or empty, skipping.",
							groupid, _cur_grffile.spritegroups_count);
					return;
				}

				for (i = 0; i < idcount; i++) {
					byte stid = buf[3 + i];
					StationSpec stat = _cur_grffile.stations[stid];

					stat.spritegroup[0] = _cur_grffile.spritegroups[groupid];
					stat.spritegroup[0].ref_count++;
					stat.grfid = _cur_grffile.grfid;
					stat.localidx = stid;
					SetCustomStation(stat);
				}
			}
			return;
		}


		/* If ``n-id'' (or ``idcount'') is zero, this is a ``feature
		 * callback''. I have no idea how this works, so we will ignore it for
		 * now.  --octo */
		if (idcount == 0) {
			grfmsg(severity.GMS_NOTICE, "NewMapping: Feature callbacks not implemented yet.");
			return;
		}

		// FIXME: Tropicset contains things like:
		// 03 00 01 19 01 00 00 00 00 - this is missing one 00 at the end,
		// what should we exactly do with that? --pasky

		if (!_cur_grffile.spriteset_start || !_cur_grffile.spritegroups) {
			grfmsg(severity.GMS_WARN, "VehicleMapSpriteGroup: No sprite set to work on! Skipping.");
			return;
		}

		if (!wagover && last_engines_count != idcount) {
			//last_engines = realloc(last_engines, idcount);
			last_engines = Arrays.copyOf(last_engines, idcount);
			last_engines_count = idcount;
		}

		if (wagover) {
			if (last_engines_count == 0) {
				grfmsg(severity.GMS_ERROR, "VehicleMapSpriteGroup: WagonOverride: No engine to do override with.");
				return;
			}
			Global.DEBUG_grf( 6, "VehicleMapSpriteGroup: WagonOverride: %u engines, %u wagons.",
					last_engines_count, idcount);
		}


		for (i = 0; i < idcount; i++) {
			byte engine_id = buf[3 + i];
			byte engine = engine_id + _vehshifts[feature];
			byte *bp = &buf[4 + idcount];

			if (engine_id > _vehcounts[feature]) {
				grfmsg(severity.GMS_ERROR, "Id %u for feature %x is out of bounds.",
						engine_id, feature);
				return;
			}

			Global.DEBUG_grf( 7, "VehicleMapSpriteGroup: [%d] Engine %d...", i, engine);

			for (c = 0; c < cidcount; c++) {
				byte ctype = grf_load_byte(&bp);
				int groupid = grf_load_word(&bp);

				Global.DEBUG_grf( 8, "VehicleMapSpriteGroup: * [%d] Cargo type %x, group id %x", c, ctype, groupid);

				if (groupid >= _cur_grffile.spritegroups_count || _cur_grffile.spritegroups[groupid] == null) {
					grfmsg(severity.GMS_WARN, "VehicleMapSpriteGroup: Spriteset %x out of range %x or empty, skipping.", groupid, _cur_grffile.spritegroups_count);
					return;
				}

				if (ctype == GC_INVALID) ctype = GC_PURCHASE;

				if (wagover) {
					// TODO: No multiple cargo types per vehicle yet. --pasky
					SetWagonOverrideSprites(engine, _cur_grffile.spritegroups[groupid], last_engines, last_engines_count);
				} else {
					SetCustomEngineSprites(engine, ctype, _cur_grffile.spritegroups[groupid]);
					last_engines[i] = engine;
				}
			}
		}

		{
			byte *bp = buf + 4 + idcount + cidcount * 3;
			int groupid = grf_load_word(&bp);

			Global.DEBUG_grf( 8, "-- Default group id %x", groupid);

			for (i = 0; i < idcount; i++) {
				byte engine = buf[3 + i] + _vehshifts[feature];

				// Don't tell me you don't love duplicated code!
				if (groupid >= _cur_grffile.spritegroups_count || _cur_grffile.spritegroups[groupid] == null) {
					grfmsg(severity.GMS_WARN, "VehicleMapSpriteGroup: Spriteset %x out of range %x or empty, skipping.", groupid, _cur_grffile.spritegroups_count);
					return;
				}

				if (wagover) {
					// TODO: No multiple cargo types per vehicle yet. --pasky
					SetWagonOverrideSprites(engine, _cur_grffile.spritegroups[groupid], last_engines, last_engines_count);
				} else {
					SetCustomEngineSprites(engine, GC_DEFAULT, _cur_grffile.spritegroups[groupid]);
					last_engines[i] = engine;
				}
			}
		}
	}

	/* Action 0x04 */
	static void VehicleNewName(DataLoader bufp, int len)
	{
		/* <04> <veh-type> <language-id> <num-veh> <offset> <data...>
		 *
		 * B veh-type      see action 0
		 * B language-id   language ID with bit 7 cleared (see below)
		 * B num-veh       number of vehicles which are getting a new name
		 * B/W offset      number of the first vehicle that gets a new name
		 * S data          new texts, each of them zero-terminated, after
		 *                 which the next name begins. */
		/* TODO: No support for changing non-vehicle text. Perhaps we shouldn't
		 * implement it at all, but it could be useful for some "modpacks"
		 * (completely new scenarios changing all graphics and logically also
		 * factory names etc). We should then also support all languages (by
		 * name), not only the original four ones. --pasky */
		/* TODO: Support for custom station class/type names. */

		byte feature;
		byte lang;
		byte num;
		int id;
		int endid;
		final String  name;

		bufp.check_length( 6, "VehicleNewName");
		
		bufp.grf_load_byte(); // buf++;
		
		feature  = bufp.grf_load_byte();
		lang     = bufp.grf_load_byte();
		num      = bufp.grf_load_byte();
		id       = (lang & 0x80) != 0 ? bufp.grf_load_word() : bufp.grf_load_byte();

		if (feature > 3) {
			Global.DEBUG_grf( 7, "VehicleNewName: Unsupported feature %d, skipping", feature);
			return;
		}

		id      += _vehshifts[feature];
		endid    = id + num;

		Global.DEBUG_grf( 6, "VehicleNewName: About to rename engines %d..%d (feature %d) in language 0x%x.",
				id, endid, feature, lang);

		if( (lang & 0x80) != 0 ) {
			grfmsg(severity.GMS_WARN, "VehicleNewName: No support for changing in-game texts. Skipping.");
			return;
		}

		if (0==(lang & 3)) {
			/* XXX: If non-English name, silently skip it. */
			Global.DEBUG_grf( 7, "VehicleNewName: Skipping non-English name.");
			return;
		}

		//name = (final String )buf;
		len -= (lang & 0x80) != 0 ? 6 : 5;
		for (; id < endid && len > 0; id++) 
		{
			String vname = bufp.grf_load_string();
			int ofs = vname.length() + 1;

			if (ofs < 128) {
				Global.DEBUG_grf( 8, "VehicleNewName: %d <- %s", id, vname);
				Engine.SetCustomEngineName(EngineID.get(id), vname);
			} else {
				Global.DEBUG_grf( 7, "VehicleNewName: Too long a name (%d)", ofs);
			}
			//name += ofs;
			len -= ofs;
		}
	}

	/* Action 0x05 */
	static void GraphicsNew(DataLoader bufp, int len)
	{
		/* <05> <graphics-type> <num-sprites> <other data...>
		 *
		 * B graphics-type What set of graphics the sprites define.
		 * E num-sprites   How many sprites are in this set?
		 * V other data    Graphics type specific data.  Currently unused. */
		/* TODO */

		byte type;
		int num;

		bufp.check_length( 2, "GraphicsNew");
		buf++;
		type = bufp.grf_load_byte();
		num  = bufp.grf_load_extended();

		grfmsg(severity.GMS_NOTICE, "GraphicsNew: Custom graphics (type %x) sprite block of length %d (unimplemented, ignoring).\n",
				type, num);
	}

	/* Action 0x06 */
	static void CfgApply(DataLoader bufp, int len)
	{
		/* <06> <param-num> <param-size> <offset> ... <FF>
		 *
		 * B param-num     Number of parameter to substitute (First = "zero")
		 *                 Ignored if that parameter was not specified in newgrf.cfg
		 * B param-size    How many bytes to replace.  If larger than 4, the
		 *                 bytes of the following parameter are used.  In that
		 *                 case, nothing is applied unless *all* parameters
		 *                 were specified.
		 * B offset        Offset into data from beginning of next sprite
		 *                 to place where parameter is to be stored. */
		/* TODO */
		grfmsg(severity.GMS_NOTICE, "CfgApply: Ignoring (not implemented).\n");
	}

	/* Action 0x07 */
	/* Action 0x09 */
	static void SkipIf(DataLoader bufp, int len)
	{
		/* <07/09> <param-num> <param-size> <condition-type> <value> <num-sprites>
		 *
		 * B param-num
		 * B param-size
		 * B condition-type
		 * V value
		 * B num-sprites */
		/* TODO: More params. More condition types. */
		byte param;
		byte paramsize;
		byte condtype;
		byte numsprites;
		int param_val = 0;
		int cond_val = 0;
		boolean result;

		bufp.check_length( 6, "SkipIf");
		param = buf[1];
		paramsize = buf[2];
		condtype = buf[3];

		if (condtype < 2) {
			/* Always 1 for bit tests, the given value should be ignored. */
			paramsize = 1;
		}

		buf += 4;
		switch (paramsize) {
		case 4: cond_val = bufp.grf_load_dword(); break;
		case 2: cond_val = bufp.grf_load_word();  break;
		case 1: cond_val = bufp.grf_load_byte();  break;
		default: break;
		}

		switch (param) {
		case 0x83:    /* current climate, 0=temp, 1=arctic, 2=trop, 3=toyland */
			param_val = _opt.landscape;
			break;
		case 0x84:    /* .grf loading stage, 0=initialization, 1=activation */
			param_val = _cur_stage;
			break;
		case 0x85:    /* TTDPatch flags, only for bit tests */
			param_val = _ttdpatch_flags[cond_val / 0x20];
			cond_val %= 0x20;
			break;
		case 0x86:    /* road traffic side, bit 4 clear=left, set=right */
			param_val = _opt.road_side << 4;
			break;
		case 0x88: {  /* see if specified GRFID is active */
			param_val = (GetFileByGRFID(cond_val) != null);
		}	break;

		case 0x8B: { /* TTDPatch version */
			int major    = 2;
			int minor    = 0;
			int revision = 10; // special case: 2.0.1 is 2.0.10
			int build    = 49;
			param_val = (major << 24) | (minor << 20) | (revision << 16) | (build * 10);
			break;
		}

		case 0x8D:    /* TTD Version, 00=DOS, 01=Windows */
			param_val = !_use_dos_palette;
			break;
		case 0x8E:
			param_val = _traininfo_vehicle_pitch;
			break;
		case 0x9D:    /* TTD Platform, 00=TTDPatch, 01=OpenTTD */
			param_val = 1;
			break;
			/* TODO */
		case 0x8F:    /* Track type cost multipliers */
		default:
			if (param < 0x80) {
				/* Parameter. */
				param_val = _cur_grffile.param[param];
			} else {
				/* In-game variable. */
				grfmsg(severity.GMS_WARN, "Unsupported in-game variable 0x%02X. Ignoring test.", param);
				return;
			}
		}

		Global.DEBUG_grf( 7, "Test condtype %d, param %x, condval %x", condtype, param_val, cond_val);
		switch (condtype) {
		case 0: result = !!(param_val & (1 << cond_val));
		break;
		case 1: result = !(param_val & (1 << cond_val));
		break;
		/* TODO: For the following, make it to work with paramsize>1. */
		case 2: result = (param_val == cond_val);
		break;
		case 3: result = (param_val != cond_val);
		break;
		case 4: result = (param_val < cond_val);
		break;
		case 5: result = (param_val > cond_val);
		break;
		case 6: result = !!param_val; /* GRFID is active (only for param-num=88) */
		break;
		case 7: result = !param_val; /* GRFID is not active (only for param-num=88) */
		break;
		default:
			grfmsg(severity.GMS_WARN, "Unsupported test %d. Ignoring.", condtype);
			return;
		}

		if (!result) {
			grfmsg(severity.GMS_NOTICE, "Not skipping sprites, test was false.");
			return;
		}

		numsprites = bufp.grf_load_byte();
		grfmsg(severity.GMS_NOTICE, "Skipping %d sprites, test was true.", numsprites);
		_skip_sprites = numsprites;
		if (_skip_sprites == 0) {
			/* Zero means there are no sprites to skip, so
			 * we use -1 to indicate that all further
			 * sprites should be skipped. */
			_skip_sprites = -1;
		}
	}

	static void GRFInfo(DataLoader bufp, int len)
	{
		/* <08> <version> <grf-id> <name> <info>
		 *
		 * B version       newgrf version, currently 06
		 * 4*B grf-id      globally unique ID of this .grf file
		 * S name          name of this .grf set
		 * S info          string describing the set, and e.g. author and copyright */
		/* TODO: Check version. (We should have own versioning done somehow.) */
		byte version;
		int grfid;
		final String name;
		final String info;

		bufp.check_length( 8, "GRFInfo");
		version = buf[1];
		/* this is de facto big endian - grf_load_dword() unsuitable */
		grfid = buf[2] << 24 | buf[3] << 16 | buf[4] << 8 | buf[5];
		name = (final String )(buf + 6);
		info = name + strlen(name) + 1;

		_cur_grffile.grfid = grfid;
		_cur_grffile.flags |= 0x0001; /* set active flag */

		DEBUG(grf, 1) ("[%s] Loaded GRFv%d set %08lx - %s:\n%s",
				_cur_grffile.filename, version, grfid, name, info);
	}

	static void SpriteReplace(DataLoader bufp, int len)
	{
		/* <0A> <num-sets> <set1> [<set2> ...]
		 * <set>: <num-sprites> <first-sprite>
		 *
		 * B num-sets      How many sets of sprites to replace.
		 * Each set:
		 * B num-sprites   How many sprites are in this set
		 * W first-sprite  First sprite number to replace */
		byte num_sets;
		int i;

		buf++; /* skip action byte */
		num_sets = bufp.grf_load_byte();

		for (i = 0; i < num_sets; i++) {
			byte num_sprites = bufp.grf_load_byte();
			int first_sprite = bufp.grf_load_word();
			int j;

			grfmsg(severity.GMS_NOTICE,
					"SpriteReplace: [Set %d] Changing %d sprites, beginning with %d",
					i, num_sprites, first_sprite
					);

			for (j = 0; j < num_sprites; j++) {
				LoadNextSprite(first_sprite + j, _file_index); // XXX
			}
		}
	}

	static void GRFError(DataLoader bufp, int len)
	{
		/* <0B> <severity> <language-id> <message-id> [<message...> 00] [<data...>] 00 [<parnum>]
		 *
		 * B severity      00: notice, contine loading grf file
		 *                 01: warning, continue loading grf file
		 *                 02: error, but continue loading grf file, and attempt
		 *                     loading grf again when loading or starting next game
		 *                 03: error, abort loading and prevent loading again in
		 *                     the future (only when restarting the patch)
		 * B language-id   see action 4, use 1F for built-in error messages
		 * B message-id    message to show, see below
		 * S message       for custom messages (message-id FF), text of the message
		 *                 not present for built-in messages.
		 * V data          additional data for built-in (or custom) messages
		 * B parnum        see action 6, only used with built-in message 03 */
		/* TODO: For now we just show the message, sometimes incomplete and never translated. */

		static final String  final msgstr[4] = {
				"Requires at least pseudo-TTDPatch version %s.",
				"This file is for %s version of TTD.",
				"Designed to be used with %s.",
				"Invalid parameter %s.",
		};
		byte severity;
		byte msgid;

		bufp.check_length( 6, "GRFError");
		severity = buf[1];
		msgid = buf[3];

		// Undocumented TTDPatch feature.
		if ((severity & 0x80) == 0 && _cur_stage == 0)
			return;
		severity &= 0x7F;

		if (msgid == 0xFF) {
			grfmsg(severity, "%s", buf+4);
		} else {
			grfmsg(severity, msgstr[msgid], buf+4);
		}
	}

	static void GRFComment(DataLoader bufp, int len)
	{
		/* <0C> [<ignored...>]
		 *
		 * V ignored       Anything following the 0C is ignored */
	}

	/* Action 0x0D */
	static void ParamSet(DataLoader bufp, int len)
	{
		/* <0D> <target> <operation> <source1> <source2> [<data>]
		 *
		 * B target        parameter number where result is stored
		 * B operation     operation to perform, see below
		 * B source1       first source operand
		 * B source2       second source operand
		 * D data          data to use in the calculation, not necessary
		 *                 if both source1 and source2 refer to actual parameters
		 *
		 * Operations
		 * 00      Set parameter equal to source1
		 * 01      Addition, source1 + source2
		 * 02      Subtraction, source1 - source2
		 * 03      Unsigned multiplication, source1 * source2 (both unsigned)
		 * 04      Signed multiplication, source1 * source2 (both signed)
		 * 05      Unsigned bit shift, source1 by source2 (source2 taken to be a
		 *         signed quantity; left shift if positive and right shift if
		 *         negative, source1 is unsigned)
		 * 06      Signed bit shift, source1 by source2
		 *         (source2 like in 05, and source1 as well)
		 */

		byte target;
		byte oper;
		int src1;
		int src2;
		int data = 0;
		int res;

		bufp.check_length( 5, "ParamSet");
		buf++;
		target = bufp.grf_load_byte();
		oper = bufp.grf_load_byte();
		src1 = bufp.grf_load_byte();
		src2 = bufp.grf_load_byte();

		if (len >= 8) data = bufp.grf_load_dword();

		/* You can add 80 to the operation to make it apply only if the target
		 * is not defined yet.  In this respect, a parameter is taken to be
		 * defined if any of the following applies:
		 * - it has been set to any value in the newgrf(w).cfg parameter list
		 * - it OR A PARAMETER WITH HIGHER NUMBER has been set to any value by
		 *   an earlier action D */
		if (oper & 0x80) {
			if (_cur_grffile.param_end < target)
				oper &= 0x7F;
			else
				return;
		}

		/* The source1 and source2 operands refer to the grf parameter number
		 * like in action 6 and 7.  In addition, they can refer to the special
		 * variables available in action 7, or they can be FF to use the value
		 * of <data>.  If referring to parameters that are undefined, a value
		 * of 0 is used instead.  */
		if (src1 == 0xFF) {
			src1 = data;
		} else {
			src1 = _cur_grffile.param_end >= src1 ? _cur_grffile.param[src1] : 0;
		}

		if (src2 == 0xFF) {
			src2 = data;
		} else {
			src2 = _cur_grffile.param_end >= src2 ? _cur_grffile.param[src2] : 0;
		}

		/* TODO: You can access the parameters of another GRF file by using
		 * source2=FE, source1=the other GRF's parameter number and data=GRF
		 * ID.  This is only valid with operation 00 (set).  If the GRF ID
		 * cannot be found, a value of 0 is used for the parameter value
		 * instead. */

		switch (oper) {
		case 0x00:
			res = src1;
			break;

		case 0x01:
			res = src1 + src2;
			break;

		case 0x02:
			res = src1 - src2;
			break;

		case 0x03:
			res = src1 * src2;
			break;

		case 0x04:
			res = (int32)src1 * (int32)src2;
			break;

		case 0x05:
			if ((int32)src2 < 0)
				res = src1 >> -(int32)src2;
			else
				res = src1 << src2;
			break;

			case 0x06:
				if ((int32)src2 < 0)
					res = (int32)src1 >> -(int32)src2;
				else
					res = (int32)src1 << src2;
				break;

				case 0x07: /* Bitwise AND */
					res = src1 & src2;
					break;

				case 0x08: /* Bitwise OR */
					res = src1 | src2;
					break;

				case 0x09: /* Unsigned division */
					if (src2 == 0) {
						res = src1;
					} else {
						res = src1 / src2;
					}
					break;

				case 0x0A: /* Signed divison */
					if (src2 == 0) {
						res = src1;
					} else {
						res = (int32)src1 / (int32)src2;
					}
					break;

				case 0x0B: /* Unsigned modulo */
					if (src2 == 0) {
						res = src1;
					} else {
						res = src1 % src2;
					}
					break;

				case 0x0C: /* Signed modulo */
					if (src2 == 0) {
						res = src1;
					} else {
						res = (int32)src1 % (int32)src2;
					}
					break;

				default:
					grfmsg(severity.GMS_ERROR, "ParamSet: Unknown operation %d, skipping.", oper);
					return;
		}

		switch (target) {
		case 0x8E: // Y-Offset for train sprites
			_traininfo_vehicle_pitch = res;
			break;

			// TODO implement
		case 0x8F: // Rail track type cost factors
		case 0x93: // Tile refresh offset to left
		case 0x94: // Tile refresh offset to right
		case 0x95: // Tile refresh offset upwards
		case 0x96: // Tile refresh offset downwards
		case 0x97: // Snow line height
		case 0x99: // Global ID offset
			Global.DEBUG_grf( 7, "ParamSet: Skipping unimplemented target 0x%02X", target);
			break;

		default:
			if (target < 0x80) {
				_cur_grffile.param[target] = res;
				if (target + 1U > _cur_grffile.param_end) _cur_grffile.param_end = target + 1;
			} else {
				Global.DEBUG_grf( 7, "ParamSet: Skipping unknown target 0x%02X", target);
			}
			break;
		}
	}

	static void GRFInhibit(DataLoader bufp, int len)
	{
		/* <0E> <num> <grfids...>
		 *
		 * B num           Number of GRFIDs that follow
		 * D grfids        GRFIDs of the files to deactivate */

		byte num;
		int i;

		bufp.check_length( 1, "GRFInhibit");
		buf++, len--;
		num = bufp.grf_load_byte(); len--;
		bufp.check_length( 4 * num, "GRFInhibit");

		for (i = 0; i < num; i++) {
			int grfid = bufp.grf_load_dword();
			GRFFile file = GetFileByGRFID(grfid);

			/* Unset activation flag */
			if (file != null) {
				grfmsg(severity.GMS_NOTICE, "GRFInhibit: Deactivating file ``%s''", file.filename);
				file.flags &= 0xFFFE;
			}
		}
	}


	static void InitializeGRFSpecial()
	{
		/* FIXME: We should rather reflect reality in _ttdpatch_flags[]. */

		_ttdpatch_flags[0] = (Global._patches.always_small_airport ? (1 << 0x0C) : 0)  /* keepsmallairport */
				| (1 << 0x0E)  /* largestations */
				| (Global._patches.longbridges ? (1 << 0x0F) : 0)           /* longbridges */
				| (1 << 0x12)  /* presignals */
				| (1 << 0x13)  /* extpresignals */
				| (Global._patches.never_expire_vehicles ? (1 << 0x16) : 0) /* enginespersist */
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
	 */
	static void ReleaseSpriteGroups(GRFFile file)
	{
		int i;

		// Bail out if no spritegroups were defined.
		if (file.spritegroups == null)
			return;

		DEBUG(grf, 6)("ReleaseSpriteGroups: Releasing for `%s'.", file.filename);
		for (i = 0; i < file.spritegroups_count; i++) {
			if (file.spritegroups[i] != null)
				UnloadSpriteGroup(&file.spritegroups[i]);
		}
		free(file.spritegroups);
		file.spritegroups = null;
		file.spritegroups_count = 0;
	}

	static void ResetCustomStations()
	{
		GRFFile file;
		int i;
		CargoID c;

		for (file = _first_grffile; file != null; file = file.next) {
			for (i = 0; i < lengthof(file.stations); i++) {
				if (file.stations[i].grfid != file.grfid) continue;

				// TODO: Release renderdata, platforms and layouts

				// Release this stations sprite groups.
				for (c = 0; c < NUM_GLOBAL_CID; c++) {
					if (file.stations[i].spritegroup[c] != null)
						UnloadSpriteGroup(&file.stations[i].spritegroup[c]);
				}
			}
		}
	}







	/**
	 * Reset all NewGRF loaded data
	 * TODO
	 */
	static void ResetNewGRFData()
	{
		int i;
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

		System.arraycopy( Bridge.orig_bridge, 0, Bridge._bridge, 0, Bridge._bridge.length );  

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

		if (newfile == null)
			error ("Out of memory");

		newfile.filename = strdup(filename);
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
		EngineID engine;

		for (engine = 0; engine < TOTAL_NUM_ENGINES; engine++) {
			int mask = 0;
			int not_mask = 0;
			int xor_mask = _engine_info[engine].refit_mask;
			byte i;

			if (cargo_allowed[engine] != 0) {
				// Build up the list of cargo types from the set cargo classes.
				for (i = 0; i < lengthof(cargo_classes); i++) {
					if (HASBIT(cargo_allowed[engine], i))
						mask |= cargo_classes[i];
					if (HASBIT(cargo_disallowed[engine], i))
						not_mask |= cargo_classes[i];
				}
			} else {
				// Don't apply default refit mask to wagons or engines with no capacity
				if (xor_mask == 0 && !(GetEngine(engine).type == VEH_Train && (RailVehInfo(engine).capacity == 0 || RailVehInfo(engine).flags & RVI_WAGON)))
					xor_mask = _default_refitmasks[GetEngine(engine).type - VEH_Train];
			}
			_engine_info[engine].refit_mask = ((mask & ~not_mask) ^ xor_mask) & _landscape_global_cargo_mask[_opt.landscape];
		}
	}



	/* Here we perform initial decoding of some special sprites (as are they
	 * described at http://www.ttdpatch.net/src/newgrf.txt, but this is only a very
	 * partial implementation yet). */
	/* XXX: We consider GRF files trusted. It would be trivial to exploit OTTD by
	 * a crafted invalid GRF file. We should tell that to the user somehow, or
	 * better make this more robust in the future. */
	static void DecodeSpecialSprite(final String  filename, int num, int stage)
	{
		/* XXX: There is a difference between staged loading in TTDPatch and
		 * here.  In TTDPatch, for some reason actions 1 and 2 are carried out
		 * during stage 0, whilst action 3 is carried out during stage 1 (to
		 * "resolve" cargo IDs... wtf). This is a little problem, because cargo
		 * IDs are valid only within a given set (action 1) block, and may be
		 * overwritten after action 3 associates them. But overwriting happens
		 * in an earlier stage than associating, so...  We just process actions
		 * 1 and 2 in stage 1 now, let's hope that won't get us into problems.
		 * --pasky */
		int action_mask = (stage == 0) ? 0x0001FF40 : 0x0001FFBF;
		static final SpecialSpriteHandler handlers[] = {
				/* 0x00 */ VehicleChangeInfo,
				/* 0x01 */ NewSpriteSet,
				/* 0x02 */ NewSpriteGroup,
				/* 0x03 */ NewVehicle_SpriteGroupMapping,
				/* 0x04 */ VehicleNewName,
				/* 0x05 */ GraphicsNew,
				/* 0x06 */ CfgApply,
				/* 0x07 */ SkipIf,
				/* 0x08 */ GRFInfo,
				/* 0x09 */ SkipIf,
				/* 0x0A */ SpriteReplace,
				/* 0x0B */ GRFError,
				/* 0x0C */ GRFComment,
				/* 0x0D */ ParamSet,
				/* 0x0E */ GRFInhibit,
				/* 0x0F */ null, // TODO implement
				/* 0x10 */ null  // TODO implement
		};

		byte* buf = malloc(num);
		byte action;

		if (buf == null) error("DecodeSpecialSprite: Could not allocate memory");

		FioReadBlock(buf, num);
		action = buf[0];

		if (action >= lengthof(handlers)) {
			Global.DEBUG_grf( 7, "Skipping unknown action 0x%02X", action);
		} else if (!HASBIT(action_mask, action)) {
			Global.DEBUG_grf( 7, "Skipping action 0x%02X in stage %d", action, stage);
		} else if (handlers[action] == null) {
			Global.DEBUG_grf( 7, "Skipping unsupported Action 0x%02X", action);
		} else {
			Global.DEBUG_grf( 7, "Handling action 0x%02X in stage %d", action, stage);
			handlers[action](buf, num);
		}
		free(buf);
	}


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
			if (_cur_grffile == null) error("File ``%s'' lost in cache.\n", filename);
			if (!(_cur_grffile.flags & 0x0001)) return;
		}

		FioOpenFile(file_index, filename);
		_file_index = file_index; // XXX

		Global.DEBUG_grf( 7, "Reading NewGRF-file '%s'", filename);

		/* Skip the first sprite; we don't care about how many sprites this
		 * does contain; newest TTDPatches and George's longvehicles don't
		 * neither, apparently. */
		if (FioReadWord() == 4 && FioReadByte() == 0xFF) {
			FioReadDword();
		} else {
			error("Custom .grf has invalid format.");
		}

		_skip_sprites = 0; // XXX

		while ((num = FioReadWord()) != 0) {
			byte type = FioReadByte();

			if (type == 0xFF) {
				if (_skip_sprites == 0) {
					DecodeSpecialSprite(filename, num, stage);
					continue;
				} else {
					FioSkipBytes(num);
				}
			} else {
				if (_skip_sprites == 0) Global.DEBUG_grf( 7, "Skipping unexpected sprite");

				FioSkipBytes(7);
				num -= 8;

				if (type & 2) {
					FioSkipBytes(num);
				} else {
					while (num > 0) {
						int8 i = FioReadByte();
						if (i >= 0) {
							num -= i;
							FioSkipBytes(i);
						} else {
							i = -(i >> 3);
							num -= i;
							FioReadByte();
						}
					}
				}
			}

			if (_skip_sprites > 0) _skip_sprites--;
		}

		// Release our sprite group references.
		// Any groups that are referenced elsewhere will be cleaned up later.
		// This removes groups that aren't used. (Perhaps skipped?)
		ReleaseSpriteGroups(_cur_grffile);
	}



	void LoadNewGRF(int load_index, int file_index)
	{
		static boolean initialized = false; // XXX yikes
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

			_cur_stage = stage;
			_cur_spriteid = load_index;
			for (j = 0; j != lengthof(_newgrf_files) && _newgrf_files[j] != null; j++) {
				if (!FiosCheckFileExists(_newgrf_files[j])) {
					// TODO: usrerror()
					error("NewGRF file missing: %s", _newgrf_files[j]);
				}
				if (stage == 0) InitNewGRFFile(_newgrf_files[j], _cur_spriteid);
				LoadNewGRFFile(_newgrf_files[j], slot++, stage);
				DEBUG(spritecache, 2) ("Currently %i sprites are loaded", load_index);
			}
		}

		// Pre-calculate all refit masks after loading GRF files
		CalculateRefitMasks();
	}

}




class DataLoader extends Pixel
{
	//Pixel ptr; // TODO Pixel is a misleading name. It is a byte[] pointer

	public DataLoader(byte[] start) {
		super(start);
	}

	

	byte grf_load_byte()
	{
		return rpp();//*(*buf)++;
	}


	int grf_load_ubyte()
	{
		return urpp();//*(*buf)++;
	}

	public boolean has(int len) {
		return hasBytesLeft() > len;
	}

	public int grf_load_dword_le() 
	{
		int v;
		v =  urpp() << 24; //*(buf++) << 24;
		v |= urpp() << 16; //*(buf++) << 16;
		v |= urpp() << 8; //*(buf++) << 8;
		v |= urpp() << 0; //*(buf++);
		return v;
	}

	int grf_load_word()
	{
		int val;

		val  = urpp();
		val |= urpp() << 8;

		return val;
	}

	int grf_load_extended()
	{
		int val;
		val = grf_load_ubyte();
		if (val == 0xFF) val = grf_load_word();
		return val;
	}

	int grf_load_dword()
	{
		int val;

		val  = urpp();
		val |= urpp() << 8;
		val |= urpp() << 16;
		val |= urpp() << 24;

		return val;
	}

	// zero terminated
	public String grf_load_string() 
	{
		StringBuilder sb = new StringBuilder();
		byte c;
		while(true) {
			c = grf_load_byte();
			if( c == 0 ) break;
			sb.append((char) c);
		}
		return sb.toString();
	}

	public void check_length( int wanted, String where ) 
	{

		if (hasBytesLeft() < wanted) { 
			grfmsg(GRFFile.severity.GMS_ERROR, "%s/%d: Invalid special sprite length %d (expected %d)!", 
					where, _cur_spriteid - _cur_grffile.sprite_offset, real, wanted); 
			throw new GrfLoadException();
		} 
	} 

}





//typedef bool (*VCI_Handler)(uint engine, int numinfo, int prop, byte **buf, int len);

@FunctionalInterface
interface VCI_Handler
{
	boolean accept(int engine, int numinfo, int prop, DataLoader buf, int len);
}

