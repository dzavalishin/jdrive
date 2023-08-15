package com.dzavalishin.game;

import java.util.Arrays;

import com.dzavalishin.ids.EngineID;
import com.dzavalishin.struct.DrawTileSeqStruct;
import com.dzavalishin.struct.DrawTileSprites;
import com.dzavalishin.struct.EngineInfo;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.TrainGui;

public abstract class NewGrfActionProcessor 
{
	final int sprite_offset;

	
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

	private int spriteset_start;
	private int spriteset_numsets;
	private int spriteset_numents;
	private int spriteset_feature;
	
	private int spritegroups_count;
	private SpriteGroup [] spritegroups;

	private int first_spriteset; ///< Holds the first spriteset's sprite offset.
	
	private final StationSpec[] stations = new StationSpec[256];

	private final int[] param = new int[0x80];
	private int param_end; /// one more than the highest set parameter

	// [dz] These two were static - why?
	private byte [] last_engines;
	private int last_engines_count;


	private int stage;

	
	// -------------------------------------------------------------------
	// Entry points
	// -------------------------------------------------------------------
	

	public NewGrfActionProcessor(int sprite_offset) {
		this.sprite_offset = sprite_offset;
	}
	
	public void processAction(byte action, DataLoader bufp, int stage) 
	{
		this.stage = stage;
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
		
		if (action >= handlers.length) {
			Global.DEBUG_grf( 7, "Skipping unknown action 0x%02X", action);
		} else if (!BitOps.HASBIT(action_mask, action)) {
			Global.DEBUG_grf( 7, "Skipping action 0x%02X in stage %d", action, stage);
		} else if (handlers[action] == null) {
			Global.DEBUG_grf( 7, "Skipping unsupported Action 0x%02X", action);
		} else {
			Global.DEBUG_grf( 7, "Handling action 0x%02X in stage %d", action, stage);
			handlers[action].accept(this, bufp);
		}

	}

	
	// -------------------------------------------------------------------
	// Constants and tables
	// -------------------------------------------------------------------
	
	
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

	
	static final VCI_Handler handler[] = {
			/* GSF_TRAIN */    NewGrfActionProcessor::RailVehicleChangeInfo,
			/* GSF_ROAD */     NewGrfActionProcessor::RoadVehicleChangeInfo,
			/* GSF_SHIP */     NewGrfActionProcessor::ShipVehicleChangeInfo,
			/* GSF_AIRCRAFT */ NewGrfActionProcessor::AircraftVehicleChangeInfo,
			/* GSF_STATION */  NewGrfActionProcessor::StationChangeInfo,
			/* GSF_CANAL */    null,
			/* GSF_BRIDGE */   NewGrfActionProcessor::BridgeChangeInfo,
			/* GSF_TOWNHOUSE */null,
			/* GSF_GLOBALVAR */NewGrfActionProcessor::GlobalVarChangeInfo,
	};
	static final int [] cargo_allowed		= new int[Global.TOTAL_NUM_ENGINES];
	static final int [] cargo_disallowed	= new int[Global.TOTAL_NUM_ENGINES];
	//static CargoID _local_cargo_id_ctype[Global.NUM_GLOBAL_CID];
	

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

	
	
	
	// -------------------------------------------------------------------
	// GRF processing
	// -------------------------------------------------------------------
	
	
	
	static void dewagonize(int condition, int engine)
	{
		EngineInfo ei = Global._engine_info[engine];
		RailVehicleInfo rvi = Global._rail_vehicle_info[engine];
	
		if (condition != 0) {
			ei.unk2 &= ~0x80;
			//rvi.flags &= ~2;
			rvi.setWagon(false);
		} else {
			ei.unk2 |= 0x80;
			//rvi.flags |= 2;
			rvi.setWagon(true);
		}
	}

	private boolean GlobalVarChangeInfo(int gvid, int numinfo, int prop, DataLoader bufp) // , int len)
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
					Economy.SetPriceBaseMultiplier(price, factor);
				} else {
					GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "GlobalVarChangeInfo: Price %d out of range, ignoring.", price);
				}
			}
		} break;
		default:
			ret = true;
		}
		// *bufp = buf;
		return ret;
	}

	boolean BridgeChangeInfo(int brid, int numinfo, int prop, DataLoader bufp) // , int len)
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
						GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "BridgeChangeInfo: Table %d >= 7, skipping.", table);
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

	boolean StationChangeInfo(int stid, int numinfo, int prop, DataLoader bufp) // , int len)
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
				StationSpec stat = stations[stid + i];
				int classid;
	
				/* classid, for a change, is always little-endian */
				//classid = *(buf++) << 24;
				//classid |= *(buf++) << 16;
				//classid |= *(buf++) << 8;
				//classid |= *(buf++);
				classid = bufp.grf_load_dword_le();
	
				stat.sclass = StationClass.AllocateStationClass(classid);
			}
			break;
		}
		case 0x09:
		{	/* Define sprite layout */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = stations[stid + i];
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
						dts.ground_sprite += first_spriteset;
					} else {
						dts.ground_sprite = ground_sprite;
					}
	
					dts.seq = null;
					//while (buf < *bufp + len)
					//while (bufp.isNotEmpty())
					//while (bufp.has(len))
					while (bufp.has(1))
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
				StationSpec stat = stations[stid + i];
				byte srcid = bufp.grf_load_byte();
				final StationSpec srcstat = stations[srcid];
				int t;
	
				stat.tiles = srcstat.tiles;
				//stat.renderdata = calloc(stat.tiles, sizeof(*stat.renderdata));
				stat.renderdata = new DrawTileSprites[stat.tiles]; 
				for (t = 0; t < stat.tiles; t++) {
					DrawTileSprites dts = stat.renderdata[t];
					final DrawTileSprites sdts = srcstat.renderdata[t];
					//final DrawTileSeqStruct  sdtss = sdts.seq;
					//int seq_count = 0;
	
					dts.ground_sprite = sdts.ground_sprite;
					if (0 == dts.ground_sprite) {
						//static final DrawTileSeqStruct empty = {0x80, 0, 0, 0, 0, 0, 0};
						//dts.seq = empty;
						dts.seq = new DrawTileSeqStruct[1];
						dts.seq[0] = new DrawTileSeqStruct(0x80, 0, 0, 0, 0, 0, 0);
						continue;
					}
					else
					{
						/*
						dts.seq = null;
						while (true) {
							//DrawTileSeqStruct dtss;
	
							// no relative bounding box support
							//dts.seq = realloc((void*)dts.seq, ++seq_count * sizeof(DrawTileSeqStruct));
							//dts.seq = new DrawTileSeqStruct[++seq_count];
							dts.seq = Arrays.copyOf(dts.seq, ++seq_count);
							///dtss = (DrawTileSeqStruct*) &dts.seq[seq_count - 1];
							//dtss = dts.seq[seq_count - 1];
							//*dtss = *sdtss;
							dts.seq[seq_count - 1] = new DrawTileSeqStruct(sdtss);
							if ( (0xFF & sdtss.delta_x) == 0x80) break;
							sdtss++;
						}
						 */
	
						dts.seq = Arrays.copyOf(sdts.seq, sdts.seq.length);
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
				StationSpec stat = stations[stid + i];
	
				stat.allowed_platforms = ~bufp.grf_load_byte();
			}
			break;
		}
		case 0x0D:
		{	/* Platforms length */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = stations[stid + i];
	
				stat.allowed_lengths = ~bufp.grf_load_byte();
			}
			break;
		}
		case 0x0e:
		{	/* Define custom layout */
			for (i = 0; i < numinfo; i++) {
				StationSpec stat = stations[stid + i];
	
				while (bufp.has(1)) {
					byte length = bufp.grf_load_byte();
					byte number = bufp.grf_load_byte();
					//StationLayout [] layout;
					byte[] layout;
					int l, p;
	
					if (length == 0 || number == 0) break;
	
					//debug("l %d > %d ?", length, stat.lengths);
					if (length > stat.lengths) {
						//stat.platforms = realloc(stat.platforms, length);
						stat.platforms = Arrays.copyOf(stat.platforms, length);
	
						//memset(stat.platforms + stat.lengths, 0, length - stat.lengths);
						Arrays.fill(stat.platforms, stat.lengths, length - stat.lengths, (byte)0 );
	
						//stat.layouts = realloc(stat.layouts, length * sizeof(*stat.layouts));
						stat.layouts = Arrays.copyOf(stat.layouts, length);
	
						//memset(stat.layouts + stat.lengths, 0,								(length - stat.lengths) * sizeof(*stat.layouts));
						Arrays.fill(stat.layouts, stat.lengths, length - stat.lengths, null );
	
						stat.lengths = length;
					}
					l = length - 1; // index is zero-based
	
					//debug("p %d > %d ?", number, stat.platforms[l]);
					if (number > stat.platforms[l]) {
						//stat.layouts[l] = realloc(stat.layouts[l], number * sizeof(**stat.layouts));
						stat.layouts[l] = Arrays.copyOf(stat.layouts[l], number);
	
						// We expect null being 0 here, but C99 guarantees that.
						//memset(stat.layouts[l] + stat.platforms[l], 0, (number - stat.platforms[l]) * sizeof(**stat.layouts));
	
						Arrays.fill( stat.layouts[l], stat.platforms[l], number - stat.platforms[l], null );  
	
						stat.platforms[l] = number;
					}
	
					p = 0;
					//layout = new StationLayout[length * number]; // malloc(length * number);
					layout = new byte[length * number]; // malloc(length * number);
					for (l = 0; l < length; l++)
						for (p = 0; p < number; p++)
							layout[l * number + p] = bufp.grf_load_byte();
	
					l--;
					p--;
					assert(p >= 0);
					//free(stat.layouts[l][p]);
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
		return ret != 0;
	}

	boolean AircraftVehicleChangeInfo(int engine, int numinfo, int prop, DataLoader bufp) // , int len)
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
				int spriteid = bufp.grf_load_ubyte();
	
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
				avi[e+i].base_cost = bufp.grf_load_ubyte(); // ?? is it base_cost?
			}
		}	break;
		case 0x0C: {	/* Speed */
			for (i = 0; i < numinfo; i++) {
				avi[e+i].max_speed = bufp.grf_load_ubyte(); // ?? units
			}
		}	break;
		case 0x0D: {	/* Acceleration */
			for (i = 0; i < numinfo; i++) {
				avi[e+i].acceleration = bufp.grf_load_ubyte();
			}
		} break;
		case 0x0E: {	/* Running cost factor */
			for (i = 0; i < numinfo; i++) {
				avi[e+i].running_cost = bufp.grf_load_ubyte();
			}
		} break;
		case 0x0F: {	/* Passenger capacity */
			for (i = 0; i < numinfo; i++) {
	
				avi[e+i].passenger_capacity = bufp.grf_load_word();
			}
		}	break;
		case 0x11: {	/* Mail capacity */
			for (i = 0; i < numinfo; i++) {
				avi[e+i].mail_capacity = bufp.grf_load_ubyte();
			}
		}	break;
		case 0x12: {	/* SFX */
			for (i = 0; i < numinfo; i++) {
				avi[e+i].sfx = bufp.grf_load_ubyte();
			}
		}	break;
		case 0x13: {	/* Cargos available for refitting */
			for (i = 0; i < numinfo; i++) {
	
				Global._engine_info[Global.AIRCRAFT_ENGINES_INDEX + engine + i].refit_mask = bufp.grf_load_dword();
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

	boolean ShipVehicleChangeInfo(int engine, int numinfo, int prop, DataLoader bufp) // , int len)
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
				int spriteid = bufp.grf_load_ubyte();
	
				if (spriteid == 0xFF)
					spriteid = (byte) 0xFD; // ships have different custom id in the GRF file
	
				svi[e+i].image_index = spriteid;
			}
		}	break;
		case 0x09: {	/* Refittable */
			for (i = 0; i < numinfo; i++) {
				svi[e+i].refittable = bufp.grf_load_ubyte();
			}
		}	break;
		case 0x0A: {	/* Cost factor */
			for (i = 0; i < numinfo; i++) {
				svi[e+i].base_cost = bufp.grf_load_ubyte(); // ?? is it base_cost?
			}
		}	break;
		case 0x0B: {	/* Speed */
			for (i = 0; i < numinfo; i++) {
				svi[e+i].max_speed = bufp.grf_load_ubyte(); // ?? units
			}
		}	break;
		case 0x0C: { /* Cargo type */
			for (i = 0; i < numinfo; i++) {
				int cargo = bufp.grf_load_ubyte();
	
				// XXX: Need to consult this with patchman yet.
				/*if(false) {
					// Documentation claims this is already the
					// per-landscape cargo type id, but newships.grf
					// assume otherwise.
					//cargo = local_cargo_id_ctype[cargo];
				}*/
				svi[e+i].cargo_type = cargo;
			}
		}	break;
		case 0x0D: {	/* Cargo capacity */
			for (i = 0; i < numinfo; i++) {
	
				svi[e+i].capacity = bufp.grf_load_word();
			}
		}	break;
		case 0x0F: {	/* Running cost factor */
			for (i = 0; i < numinfo; i++) {
				svi[e+i].running_cost = bufp.grf_load_ubyte();
			}
		} break;
		case 0x10: {	/* SFX */
			for (i = 0; i < numinfo; i++) {
				svi[e+i].sfx = bufp.grf_load_ubyte();
			}
		}	break;
		case 0x11: {	/* Cargos available for refitting */
			for (i = 0; i < numinfo; i++) {
	
				Global._engine_info[Global.SHIP_ENGINES_INDEX + engine + i].refit_mask = bufp.grf_load_dword();
			}
		}	break;
		case 0x18: { /* Cargo classes allowed */
			for (i = 0; i < numinfo; i++) {
				NewGrfActionProcessor.cargo_allowed[Global.SHIP_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x19: { /* Cargo classes disallowed */
			for (i = 0; i < numinfo; i++) {
				NewGrfActionProcessor.cargo_disallowed[Global.SHIP_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
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

	boolean RoadVehicleChangeInfo(int engine, int numinfo, int prop, DataLoader bufp) // , int len)
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
				rvi[e+i].max_speed = bufp.grf_load_ubyte(); // ?? units
			}
		} break;
		case 0x09: { /* Running cost factor */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].running_cost = bufp.grf_load_ubyte();
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
				int spriteid = bufp.grf_load_ubyte();
	
				if (spriteid == 0xFF)
					spriteid = 0xFD; // cars have different custom id in the GRF file
	
				rvi[e+i].image_index = spriteid;
			}
		} break;
		case 0x0F: { /* Cargo capacity */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].capacity = bufp.grf_load_ubyte();
			}
		} break;
		case 0x10: { /* Cargo type */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].cargo_type = bufp.grf_load_ubyte();
			}
		} break;
		case 0x11: { /* Cost factor */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].base_cost = bufp.grf_load_ubyte(); // ?? is it base_cost?
			}
		} break;
		case 0x12: { /* SFX */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].sfx = bufp.grf_load_ubyte();
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
	
				Global._engine_info[Global.ROAD_ENGINES_INDEX + engine + i].refit_mask = bufp.grf_load_dword();
			}
		} break;
		case 0x1D: { /* Cargo classes allowed */
			for (i = 0; i < numinfo; i++) {
				NewGrfActionProcessor.cargo_allowed[Global.ROAD_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x1E: { /* Cargo classes disallowed */
			for (i = 0; i < numinfo; i++) {
				NewGrfActionProcessor.cargo_disallowed[Global.ROAD_ENGINES_INDEX + engine + i] = bufp.grf_load_word();
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

	boolean RailVehicleChangeInfo(int engine, int numinfo, int prop, DataLoader bufp) // , int len)
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
	
				rvi[e+i].setMax_speed( speed );
			}
		} break;
		case 0x0B: { /* Power */
			for (i = 0; i < numinfo; i++) {
				int power = bufp.grf_load_word();
	
				//if(0 != (rvi[e+i].flags & Engine.RVI_MULTIHEAD) )
				if( rvi[e+i].isMulttihead() )
					power /= 2;
	
				rvi[e+i].power = power;
				NewGrfActionProcessor.dewagonize(power, engine + i);
			}
		} break;
		case 0x0D: { /* Running cost factor */
			for (i = 0; i < numinfo; i++) {
				byte runcostfact = bufp.grf_load_byte();
	
				rvi[e+i].running_cost_base = runcostfact;
				NewGrfActionProcessor.dewagonize(runcostfact, engine + i);
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
				NewGrfActionProcessor.dewagonize(base, engine + i);
			}
		} break;
		case 0x12: { /* Sprite ID */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].image_index = bufp.grf_load_ubyte();
			}
		} break;
		case 0x13: { /* Dual-headed */
			for (i = 0; i < numinfo; i++) {
				byte dual = bufp.grf_load_byte();
	
				if (dual != 0) {
					if (!(rvi[e+i].isMulttihead())) // adjust power if needed
						rvi[e+i].power /= 2;
					//rvi[e+i].flags |= Engine.RVI_MULTIHEAD;
					rvi[e+i].setMultihead(true);
				} else {
					if(rvi[e+i].isMulttihead() ) // adjust power if needed
						rvi[e+i].power *= 2;
					//rvi[e+i].flags &= ~Engine.RVI_MULTIHEAD;
					rvi[e+i].setMultihead(false);
				}
			}
		} break;
		case 0x14: { /* Cargo capacity */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].capacity = bufp.grf_load_ubyte();
			}
		} break;
		case 0x15: { /* Cargo type */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].cargo_type = bufp.grf_load_ubyte();
			}
		} break;
		case 0x16: { /* Weight */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].weight = BitOps.RETSB(rvi[e+i].weight, 0, 8, bufp.grf_load_ubyte());
			}
		} break;
		case 0x17: { /* Cost factor */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].base_cost = bufp.grf_load_ubyte();
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
				int traction = bufp.grf_load_ubyte();
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
	
				rvi[e+i].pow_wag_power = bufp.grf_load_word();
			}
		} break;
		case 0x1D: { /* Refit cargo */
			for (i = 0; i < numinfo; i++) {
	
				Global._engine_info[engine + i].refit_mask = bufp.grf_load_dword();
			}
		} break;
		case 0x1E: { /* Callback */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].callbackmask = bufp.grf_load_ubyte();
			}
		} break;
		case 0x21: { /* Shorter vehicle */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].shorten_factor = bufp.grf_load_ubyte();
			}
		} break;
		case 0x22: { /* Visual effect */
			// see note in engine.h about rvi.visual_effect
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].visual_effect = bufp.grf_load_ubyte();
			}
		} break;
		case 0x23: { /* Powered wagons weight bonus */
			for (i = 0; i < numinfo; i++) {
				rvi[e+i].pow_wag_weight = bufp.grf_load_ubyte();
			}
		} break;
		case 0x24: { /* High byte of vehicle weight */
			for (i = 0; i < numinfo; i++) {
				int weight = bufp.grf_load_ubyte();
				if (weight > 4) {
					GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE, "RailVehicleChangeInfo: Nonsensical weight of %d tons, ignoring.", weight << 8);
				} else {
					rvi[e+i].weight = BitOps.RETSB(rvi[e+i].weight, 8, 8, weight);
				}
			}
		} break;
		case 0x28: { /* Cargo classes allowed */
			for (i = 0; i < numinfo; i++) {
				NewGrfActionProcessor.cargo_allowed[engine + i] = bufp.grf_load_word();
			}
		} break;
		case 0x29: { /* Cargo classes disallowed */
			for (i = 0; i < numinfo; i++) {
				NewGrfActionProcessor.cargo_disallowed[engine + i] = bufp.grf_load_word();
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

	/* Action 0x00 */
	private void VehicleChangeInfo(DataLoader bufp) //, int len)
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
	
		if (bufp.hasBytesLeft() == 1) {
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
	
		if (feature >= NewGrfActionProcessor.handler.length || NewGrfActionProcessor.handler[feature] == null) {
			GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "VehicleChangeInfo: Unsupported feature %d, skipping.", feature);
			return;
		}
	
		int e = 0; // TODO [dz] right?
		if (feature <= GSF_AIRCRAFT) {
			if (engine + numinfo > _vehcounts[feature]) {
				GRFFile.grfmsg(GRFFile.severity.GMS_ERROR, "VehicleChangeInfo: Last engine ID %d out of bounds (max %d), skipping.", engine + numinfo, _vehcounts[feature]);
				return;
			}
			if (engine < 0 ) {
				GRFFile.grfmsg(GRFFile.severity.GMS_ERROR, "VehicleChangeInfo: First engine ID %d < 0, skipping.", engine);
				return;
			}
			//ei = Global._engine_info[engine + _vehshifts[feature]];
			ei = Global._engine_info;
			e = engine + _vehshifts[feature];
		}
	
		//buf += 5;
	
		//while (numprops-- && buf < bufend) 
		while (numprops-- > 0 && bufp.has(1)) 
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
	
						ei[e+i].base_intro = bufp.grf_load_word();
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
						ei[e+i].lifelength = bufp.grf_load_ubyte();
					}
				}	break;
				case 0x04: { /* Model life */
					for (i = 0; i < numinfo; i++) {
						ei[e+i].base_life = bufp.grf_load_ubyte();
					}
				}	break;
				case 0x06: { /* Climates available */
					for (i = 0; i < numinfo; i++) {
						ei[e+i].climates = bufp.grf_load_ubyte();
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
					if (NewGrfActionProcessor.handler[feature].accept(this, engine, numinfo, prop, bufp)) //, bufend - buf))
						ignoring = true;
					break;
				}
				break;
	
			default:
				if (NewGrfActionProcessor.handler[feature].accept(this, engine, numinfo, prop, bufp)) //, bufend - buf))
					ignoring = true;
				break;
			}
	
			if (ignoring)
				GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE, "VehicleChangeInfo: Ignoring property %x (not implemented).", prop);
		}
	}

	/* Action 0x01 */
	private void NewSpriteSet(DataLoader bufp) //, int len)
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
	
		spriteset_start = GRFFile._cur_spriteid;
		spriteset_feature = feature;
		spriteset_numsets = num_sets;
		spriteset_numents = num_ents;
	
		Global.DEBUG_grf( 7, 
				"New sprite set at %d of type %d, "+
						"consisting of %d sets with %d views each (total %d)",
						GRFFile._cur_spriteid, feature, num_sets, num_ents, num_sets * num_ents
				);
	
		for (i = 0; i < num_sets * num_ents; i++) {
			//SpriteCache.LoadNextSprite(GRFFile._cur_spriteid++, GRFFile._file_index);
			loadSprite(GRFFile._cur_spriteid++, bufp);
		}
	}

	/* Action 0x02 */
	private void NewSpriteGroup(DataLoader bufp) //, int len)
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
		int numloaded;
		byte numloading;
		SpriteGroup group;
		//RealSpriteGroup rg;
		//byte *loaded_ptr;
		//byte *loading_ptr;
		//int i;
	
		bufp.check_length( 5, GRFFile.NEW_SPRITE_GROUP);
		feature = bufp.r(1);
		setid = bufp.r(2);
		numloaded = bufp.r(3);
		numloading = bufp.r(4);
	
		if (setid >= spritegroups_count) {
			// Allocate memory for new sprite group references.
			//_cur_grffile.spritegroups = realloc(_cur_grffile.spritegroups, (setid + 1) * sizeof(*_cur_grffile.spritegroups));
			spritegroups = Arrays.copyOf(spritegroups, setid + 1);
			// Initialise new space to null
			for (; spritegroups_count < (setid + 1); spritegroups_count++)
				spritegroups[spritegroups_count] = null;
		}
	
		if (numloaded == 0x81 || numloaded == 0x82) {
			DeterministicSpriteGroup dg;
			int groupid;
			int i;
	
			// Ok, this is gonna get a little wild, so hold your breath...
	
			/* This stuff is getting actually evaluated in
			 * EvalDeterministicSpriteGroup(). */
	
			bufp.shift( 4 ); //len -= 4;
			bufp.check_length( 6, "NewSpriteGroup 0x81/0x82");
	
			group = new SpriteGroup();
			group.type = SpriteGroupType.SGT_DETERMINISTIC;
	
			assert group instanceof DeterministicSpriteGroup;
			dg = (DeterministicSpriteGroup)group; //.g.determ;
	
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
					dg.ranges[i].group = ResultSpriteGroup.NewCallBackResultSpriteGroup(groupid);
					dg.ranges[i].group.ref_count++;
				} else if (groupid >= spritegroups_count || spritegroups[groupid] == null) {
					GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "NewSpriteGroup(%02x:0x%x): Groupid %04x does not exist, leaving empty.", setid, numloaded, groupid);
					dg.ranges[i].group = null;
				} else {
					dg.ranges[i].group = spritegroups[groupid];
					dg.ranges[i].group.ref_count++;
				}
	
				dg.ranges[i].low = bufp.grf_load_byte();
				dg.ranges[i].high = bufp.grf_load_byte();
			}
	
			groupid = bufp.grf_load_word();
			if (BitOps.HASBIT(groupid, 15)) {
				dg.default_group = ResultSpriteGroup.NewCallBackResultSpriteGroup(groupid);
				dg.default_group.ref_count++;
			} else if (groupid >= spritegroups_count || spritegroups[groupid] == null) {
				GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "NewSpriteGroup(%02x:0x%x): Groupid %04x does not exist, leaving empty.", setid, numloaded, groupid);
				dg.default_group = null;
			} else {
				dg.default_group = spritegroups[groupid];
				dg.default_group.ref_count++;
			}
	
			/* [dz] can just skip unload? 
			if (_cur_grffile.spritegroups[setid] != null)
				UnloadSpriteGroup(&_cur_grffile.spritegroups[setid]);
			 */
			spritegroups[setid] = group;
			group.ref_count++;
			return;
	
		} else if (numloaded == 0x80 || numloaded == 0x83) {
			RandomizedSpriteGroup rg;
			int i;
	
			/* This stuff is getting actually evaluated in
			 * EvalRandomizedSpriteGroup(). */
	
			bufp.shift( 4 );
			//len -= 4;
			bufp.check_length( 6, "NewSpriteGroup 0x80/0x83");
	
			group = new SpriteGroup();
			group.type = SpriteGroupType.SGT_RANDOMIZED;
	
			assert group instanceof RandomizedSpriteGroup;
			rg = (RandomizedSpriteGroup)group;
	
			rg.var_scope = numloaded == 0x83 ? VarSpriteGroupScope.VSG_SCOPE_PARENT : VarSpriteGroupScope.VSG_SCOPE_SELF;
	
			rg.triggers = bufp.grf_load_byte();
			rg.cmp_mode = RandomizedSpriteGroupCompareMode.values[rg.triggers & 0x80];
			rg.triggers &= 0x7F;
	
			rg.lowest_randbit = bufp.grf_load_byte();
			rg.num_groups = bufp.grf_load_byte();
	
			rg.groups = new SpriteGroup[rg.num_groups]; //calloc(rg.num_groups, sizeof(*rg.groups));
			for (i = 0; i < rg.num_groups; i++) {
				int groupid = bufp.grf_load_word();
	
				if (BitOps.HASBIT(groupid, 15)) {
					rg.groups[i] = ResultSpriteGroup.NewCallBackResultSpriteGroup(groupid);
					rg.groups[i].ref_count++;
				} else if (groupid >= spritegroups_count || spritegroups[groupid] == null) {
					GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "NewSpriteGroup(%02x:0x%x): Groupid %04x does not exist, leaving empty.", setid, numloaded, groupid);
					rg.groups[i] = null;
				} else {
					rg.groups[i] = spritegroups[groupid];
					rg.groups[i].ref_count++;
				}
			}
	
			/* [dz] just ignore? 
			if (_cur_grffile.spritegroups[setid] != null)
				UnloadSpriteGroup(_cur_grffile.spritegroups[setid]);
			 */
			spritegroups[setid] = group;
			group.ref_count++;
			return;
		}
	
		if (0 == spriteset_start) {
			GRFFile.grfmsg(GRFFile.severity.GMS_ERROR, "NewSpriteGroup: No sprite set to work on! Skipping.");
			return;
		}
	
		if (spriteset_feature != feature) {
			GRFFile.grfmsg(GRFFile.severity.GMS_ERROR, "NewSpriteGroup: Group feature %x doesn't match set feature %x! Playing it risky and trying to use it anyway.", feature, spriteset_feature);
			// return; // XXX: we can't because of MB's newstats.grf --pasky
		}
	
		bufp.check_length(5, GRFFile.NEW_SPRITE_GROUP);
		bufp.shift( 5 );
		bufp.check_length(2 * numloaded, GRFFile.NEW_SPRITE_GROUP);
		DataLoader loaded_ptr = new DataLoader( bufp, 0 );
		DataLoader loading_ptr = new DataLoader( bufp, 2 * numloaded );
	
		if (first_spriteset == 0)
			first_spriteset = spriteset_start;
	
		if (numloaded > 16) {
			GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "NewSpriteGroup: More than 16 sprites in group %x, skipping the rest.", setid);
			numloaded = 16;
		}
		if (numloading > 16) {
			GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "NewSpriteGroup: More than 16 sprites in group %x, skipping the rest.", setid);
			numloading = 16;
		}
	
		group = new SpriteGroup();
		group.type = SpriteGroupType.SGT_REAL;
	
		assert group instanceof RealSpriteGroup;
		RealSpriteGroup rg = (RealSpriteGroup) group;
	
		rg.sprites_per_set = spriteset_numents;
		//rg.loaded_count  = numloaded;
		//rg.loading_count = numloading;
	
		Global.DEBUG_grf( 6, "NewSpriteGroup: New SpriteGroup 0x%02hhx, %u views, %u loaded, %u loading, sprites %u - %u",
				setid, rg.sprites_per_set, rg.loaded_count(), rg.loading_count(),
				spriteset_start - sprite_offset,
				spriteset_start + (spriteset_numents * (numloaded + numloading)) - sprite_offset);
	
		for (int i = 0; i < numloaded; i++) {
			int spriteset_id = loaded_ptr.grf_load_word();
			if (BitOps.HASBIT(spriteset_id, 15)) {
				rg.loaded[i] = ResultSpriteGroup.NewCallBackResultSpriteGroup(spriteset_id);
			} else {
				rg.loaded[i] = ResultSpriteGroup.NewResultSpriteGroup(spriteset_start + spriteset_id * spriteset_numents, rg.sprites_per_set);
			}
			rg.loaded[i].ref_count++;
			Global.DEBUG_grf( 8, "NewSpriteGroup: + rg.loaded[%i]  = %u (subset %u)", i, ((ResultSpriteGroup)rg.loaded[i]).result, spriteset_id);
		}
	
		for (int i = 0; i < numloading; i++) {
			int spriteset_id = loading_ptr.grf_load_word();
			if (BitOps.HASBIT(spriteset_id, 15)) {
				rg.loading[i] = ResultSpriteGroup.NewCallBackResultSpriteGroup(spriteset_id);
			} else {
				rg.loading[i] = ResultSpriteGroup.NewResultSpriteGroup(spriteset_start + spriteset_id * spriteset_numents, rg.sprites_per_set);
			}
			rg.loading[i].ref_count++;
			Global.DEBUG_grf( 8, "NewSpriteGroup: + rg.loading[%i] = %u (subset %u)", i, ((ResultSpriteGroup)rg.loading[i]).result, spriteset_id);
		}
	
		/*
		if (_cur_grffile.spritegroups[setid] != null)
			UnloadSpriteGroup(&_cur_grffile.spritegroups[setid]);
		 */
		spritegroups[setid] = group;
		group.ref_count++;
	}

	/* Action 0x03 */
	private void NewVehicle_SpriteGroupMapping(DataLoader bufp) //, int len)
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
	
		int feature;
		int idcount;
		boolean wagover;
		int cidcount;
		int c, i;
	
		bufp.check_length( 7, "VehicleMapSpriteGroup");
		//bufp.grf_load_byte();
		feature = bufp.ur(1); // bufp.grf_load_ubyte(); // buf[1];
		int tmp2 = bufp.ur(2); // bufp.grf_load_ubyte(); // buf[2] 
		idcount = tmp2 & 0x7F; // buf[2] & 0x7F;
		wagover = (tmp2 & 0x80) == 0x80;
		bufp.check_length( 3 + idcount, "VehicleMapSpriteGroup");
		cidcount = bufp.r(3 + idcount);
		bufp.check_length( 4 + idcount + cidcount * 3, "VehicleMapSpriteGroup");
	
		Global.DEBUG_grf( 6, "VehicleMapSpriteGroup: Feature %d, %d ids, %d cids, wagon override %s.",
				feature, idcount, cidcount, wagover);
	
		if (feature > GSF_STATION) {
			GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "VehicleMapSpriteGroup: Unsupported feature %d, skipping.", feature);
			return;
		}
	
	
		if (feature == GSF_STATION) {
			// We do things differently for stations.
	
			for (i = 0; i < idcount; i++) 
			{
				byte stid = bufp.r(3 + i);
				StationSpec stat = stations[stid];
				DataLoader bp = new DataLoader( bufp, 4 + idcount );
	
				for (c = 0; c < cidcount; c++) {
					int ctype = bp.grf_load_ubyte();
					int groupid = bp.grf_load_word();
	
					if (groupid >= spritegroups_count || spritegroups[groupid] == null) {
						GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "VehicleMapSpriteGroup: Spriteset %x out of range %x or empty, skipping.",
								groupid, spritegroups_count);
						return;
					}
	
					if (ctype != 0xFF) {
						/* TODO: No support for any other cargo. */
						continue;
					}
	
					stat.spritegroup[1] = spritegroups[groupid];
					stat.spritegroup[1].ref_count++;
				}
			}
	
			{
				DataLoader bp = new DataLoader(bufp, 4 + idcount + cidcount * 3);
				int groupid = bp.grf_load_word();
	
				if (groupid >= spritegroups_count || spritegroups[groupid] == null) {
					GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "VehicleMapSpriteGroup: Spriteset %x out of range %x or empty, skipping.",
							groupid, spritegroups_count);
					return;
				}
	
				for (i = 0; i < idcount; i++) {
					byte stid = bufp.r(3 + i);
					StationSpec stat = stations[stid];
	
					stat.spritegroup[0] = spritegroups[groupid];
					stat.spritegroup[0].ref_count++;
					stat.grfid = GRFFile._cur_grffile.grfid;
					stat.localidx = stid;
					StationClass.SetCustomStation(stat);
				}
			}
			return;
		}
	
	
		/* If ``n-id'' (or ``idcount'') is zero, this is a ``feature
		 * callback''. I have no idea how this works, so we will ignore it for
		 * now.  --octo */
		if (idcount == 0) {
			GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE, "NewMapping: Feature callbacks not implemented yet.");
			return;
		}
	
		// FIXME: Tropicset contains things like:
		// 03 00 01 19 01 00 00 00 00 - this is missing one 00 at the end,
		// what should we exactly do with that? --pasky
	
		if (0==spriteset_start || null==spritegroups) {
			GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "VehicleMapSpriteGroup: No sprite set to work on! Skipping.");
			return;
		}
	
		if (!wagover && last_engines_count != idcount) {
			//last_engines = realloc(last_engines, idcount);
			last_engines = Arrays.copyOf(last_engines, idcount);
			last_engines_count = idcount;
		}
	
		if (wagover) {
			if (last_engines_count == 0) {
				GRFFile.grfmsg(GRFFile.severity.GMS_ERROR, "VehicleMapSpriteGroup: WagonOverride: No engine to do override with.");
				return;
			}
			Global.DEBUG_grf( 6, "VehicleMapSpriteGroup: WagonOverride: %u engines, %u wagons.",
					last_engines_count, idcount);
		}
	
	
		for (i = 0; i < idcount; i++) {
			byte engine_id = bufp.r(3 + i);
			int engine = engine_id + _vehshifts[feature];
			DataLoader bp = new DataLoader(bufp, 4 + idcount);
	
			if (engine_id > _vehcounts[feature]) {
				GRFFile.grfmsg(GRFFile.severity.GMS_ERROR, "Id %u for feature %x is out of bounds.",
						engine_id, feature);
				return;
			}
	
			Global.DEBUG_grf( 7, "VehicleMapSpriteGroup: [%d] Engine %d...", i, engine);
	
			for (c = 0; c < cidcount; c++) {
				int ctype = bp.grf_load_ubyte();
				int groupid = bp.grf_load_word();
	
				Global.DEBUG_grf( 8, "VehicleMapSpriteGroup: * [%d] Cargo type %x, group id %x", c, ctype, groupid);
	
				if (groupid >= spritegroups_count || spritegroups[groupid] == null) {
					GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "VehicleMapSpriteGroup: Spriteset %x out of range %x or empty, skipping.", groupid, spritegroups_count);
					return;
				}
	
				if (ctype == Engine.GC_INVALID) ctype = Engine.GC_PURCHASE;
	
				if (wagover) {
					// TODO: No multiple cargo types per vehicle yet. --pasky
					Engine.SetWagonOverrideSprites( EngineID.get(engine), spritegroups[groupid], last_engines, last_engines_count);
				} else {
					Engine.SetCustomEngineSprites( EngineID.get(engine), ctype, spritegroups[groupid]);
					last_engines[i] = (byte) engine;
				}
			}
		}
	
		{
			DataLoader bp = new DataLoader(bufp, 4 + idcount + cidcount * 3);
			int groupid = bp.grf_load_word();
	
			Global.DEBUG_grf( 8, "-- Default group id %x", groupid);
	
			for (i = 0; i < idcount; i++) {
				int engine = 0xFF & ( bufp.r(3 + i) + _vehshifts[feature] );
	
				// Don't tell me you don't love duplicated code!
				if (groupid >= spritegroups_count || spritegroups[groupid] == null) {
					GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "VehicleMapSpriteGroup: Spriteset %x out of range %x or empty, skipping.", groupid, spritegroups_count);
					return;
				}
	
				if (wagover) {
					// TODO: No multiple cargo types per vehicle yet. --pasky
					Engine.SetWagonOverrideSprites(EngineID.get(engine), spritegroups[groupid], last_engines, last_engines_count);
				} else {
					Engine.SetCustomEngineSprites(EngineID.get(engine), Engine.GC_DEFAULT, spritegroups[groupid]);
					last_engines[i] = (byte) engine;
				}
			}
		}
	}

	/* Action 0x04 */
	private void VehicleNewName(DataLoader bufp) //, int len)
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
		//final String  name;
	
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
		endid   = id + num;
	
		Global.DEBUG_grf( 6, "VehicleNewName: About to rename engines %d..%d (feature %d) in language 0x%x.",
				id, endid, feature, lang);
	
		if( (lang & 0x80) != 0 ) {
			GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "VehicleNewName: No support for changing in-game texts. Skipping.");
			return;
		}
	
		if (0==(lang & 3)) {
			/* XXX: If non-English name, silently skip it. */
			Global.DEBUG_grf( 7, "VehicleNewName: Skipping non-English name.");
			return;
		}
	
		int len = bufp.hasBytesLeft();
	
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
	void GraphicsNew(DataLoader bufp) //, int len)
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
		bufp.shift(1);
		type = bufp.grf_load_byte();
		num  = bufp.grf_load_extended();
	
		GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE, "GraphicsNew: Custom graphics (type %x) sprite block of length %d (unimplemented, ignoring).\n",
				type, num);
	}

	/* Action 0x06 */
	void CfgApply(DataLoader bufp) //, int len)
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
		GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE, "CfgApply: Ignoring (not implemented).\n");
	}

	void GRFInfo(DataLoader bufp) //, int len)
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
		version = bufp.r(1);
		/* this is de facto big endian - grf_load_dword() unsuitable */
		grfid = bufp.r(2) << 24 | bufp.r(3) << 16 | bufp.r(4) << 8 | bufp.r(5);
	
		{
			int [] skip = {0};
			name = bufp.grf_load_string(6, skip);
	
			//info = name + name.length() + 1; // not really correct? == num of nonzero bytes following bufp cur pos
			info = name + skip[0]; // it counts in final zero
		}
	
	
		GRFFile._cur_grffile.grfid = grfid;
		GRFFile._cur_grffile.flags |= 0x0001; /* set active flag */
	
		Global.DEBUG_grf( 1, "[%s] Loaded GRFv%d set %08x - %s:\n%s",
				GRFFile._cur_grffile.getFilename(), version, grfid, name, info);
	}

	/* Action 0x07 */
	/* Action 0x09 */
	void SkipIf(DataLoader bufp) //, int len)
	{
		/* <07/09> <param-num> <param-size> <condition-type> <value> <num-sprites>
		 *
		 * B param-num
		 * B param-size
		 * B condition-type
		 * V value
		 * B num-sprites */
		/* TODO: More params. More condition types. */
		byte pparam;
		byte paramsize;
		byte condtype;
		byte numsprites;
		int param_val = 0;
		int cond_val = 0;
		boolean result;
	
		bufp.check_length( 6, "SkipIf");
		pparam = bufp.r(1);
		paramsize = bufp.r(2);
		condtype = bufp.r(3);
	
		if (condtype < 2) {
			/* Always 1 for bit tests, the given value should be ignored. */
			paramsize = 1;
		}
	
		bufp.shift(4);
		switch (paramsize) {
		case 4: cond_val = bufp.grf_load_dword(); break;
		case 2: cond_val = bufp.grf_load_word();  break;
		case 1: cond_val = bufp.grf_load_byte();  break;
		default: break;
		}
	
		switch (0xFF & pparam) {
		case 0x83:    /* current climate, 0=temp, 1=arctic, 2=trop, 3=toyland */
			param_val = GameOptions._opt.landscape;
			break;
		case 0x84:    /* .grf loading stage, 0=initialization, 1=activation */
			param_val = stage;
			break;
		case 0x85:    /* TTDPatch flags, only for bit tests */
			param_val = GRFFile._ttdpatch_flags[cond_val / 0x20];
			cond_val %= 0x20;
			break;
		case 0x86:    /* road traffic side, bit 4 clear=left, set=right */
			param_val = GameOptions._opt.road_side << 4;
			break;
		case 0x88: {  /* see if specified GRFID is active */
			param_val = BitOps.b2i(GRFFile.GetFileByGRFID(cond_val) != null);
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
			param_val = 1; // BitOps.b2i(!Global._use_dos_palette);
			break;
		case 0x8E:
			param_val = TrainGui._traininfo_vehicle_pitch;
			break;
		case 0x9D:    /* TTD Platform, 00=TTDPatch, 01=OpenTTD */
			param_val = 1;
			break;
			/* TODO */
		case 0x8F:    /* Track type cost multipliers */
		default:
			if (pparam < 0x80) {
				/* Parameter. */
				param_val = param[pparam];
			} else {
				/* In-game variable. */
				GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "Unsupported in-game variable 0x%02X. Ignoring test.", param);
				return;
			}
		}
	
		Global.DEBUG_grf( 7, "Test condtype %d, param %x, condval %x", condtype, param_val, cond_val);
		switch (condtype) {
		case 0: result = BitOps.i2b(param_val & (1 << cond_val));
		break;
		case 1: result = !BitOps.i2b(param_val & (1 << cond_val));
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
		case 6: result = BitOps.i2b(param_val); /* GRFID is active (only for param-num=88) */
		break;
		case 7: result = !BitOps.i2b(param_val); /* GRFID is not active (only for param-num=88) */
		break;
		default:
			GRFFile.grfmsg(GRFFile.severity.GMS_WARN, "Unsupported test %d. Ignoring.", condtype);
			return;
		}
	
		if (!result) {
			GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE, "Not skipping sprites, test was false.");
			return;
		}
	
		numsprites = bufp.grf_load_byte();
		GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE, "Skipping %d sprites, test was true.", numsprites);
		GRFFile._skip_sprites = numsprites;
		if (GRFFile._skip_sprites == 0) {
			/* Zero means there are no sprites to skip, so
			 * we use -1 to indicate that all further
			 * sprites should be skipped. */
			GRFFile._skip_sprites = -1;
		}
	}

	void SpriteReplace(DataLoader bufp) //, int len)
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
	
		bufp.shift(1); /* skip action byte */
		num_sets = bufp.grf_load_byte();
	
		for (i = 0; i < num_sets; i++) {
			byte num_sprites = bufp.grf_load_byte();
			int first_sprite = bufp.grf_load_word();
			int j;
	
			GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE,
					"SpriteReplace: [Set %d] Changing %d sprites, beginning with %d",
					i, num_sprites, first_sprite
					);
	
			for (j = 0; j < num_sprites; j++) {
				SpriteCache.LoadNextSprite(first_sprite + j, GRFFile._file_index); // XXX
			}
		}
	}

	void GRFError(DataLoader bufp) //, int len)
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
	
		int severity;
		int msgid;
	
		bufp.check_length( 6, "GRFError");
		severity = 0xFF & bufp.r(1);
		msgid = 0xFF & bufp.r(3);
	
		// Undocumented TTDPatch feature.
		if ((severity & 0x80) == 0 && stage == 0)
			return;
		severity &= 0x7F;
	
		if (msgid == 0xFF) {
			GRFFile.grfmsg(/*severity*/ GRFFile.severity.values()[severity], "%s", bufp.grf_load_string(4, null));
		} else {
			GRFFile.grfmsg(GRFFile.severity.values()[severity], GRFFile.msgstr[msgid], bufp.grf_load_string(4, null));
		}
	}

	void GRFComment(DataLoader bufp) //, int len)
	{
		/* <0C> [<ignored...>]
		 *
		 * V ignored       Anything following the 0C is ignored */
	}

	/* Action 0x0D */
	void ParamSet(DataLoader bufp) //, int len)
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
	
		int target;
		int oper;
		int src1;
		int src2;
		int data = 0;
		int res;
	
		bufp.check_length( 5, "ParamSet");
		bufp.shift(1);
		target = bufp.grf_load_ubyte();
		oper = bufp.grf_load_ubyte();
		src1 = bufp.grf_load_ubyte();
		src2 = bufp.grf_load_ubyte();
	
		if (bufp.hasBytesLeft() >= 8) data = bufp.grf_load_dword();
	
		/* You can add 80 to the operation to make it apply only if the target
		 * is not defined yet.  In this respect, a parameter is taken to be
		 * defined if any of the following applies:
		 * - it has been set to any value in the newgrf(w).cfg parameter list
		 * - it OR A PARAMETER WITH HIGHER NUMBER has been set to any value by
		 *   an earlier action D */
		if(0 != (oper & 0x80)) {
			if (param_end < target)
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
			src1 = param_end >= src1 ? param[src1] : 0;
		}
	
		if (src2 == 0xFF) {
			src2 = data;
		} else {
			src2 = param_end >= src2 ? param[src2] : 0;
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
			res = src1 * src2;
			break;
	
		case 0x05:
			if (src2 < 0)
				res = src1 >> -src2;
			else
				res = src1 << src2;
			break;
	
			case 0x06:
				if (src2 < 0)
					res = src1 >> -src2;
				else
					res = src1 << src2;
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
						res = src1 / src2;
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
						res = src1 % src2;
					}
					break;
	
				default:
					GRFFile.grfmsg(GRFFile.severity.GMS_ERROR, "ParamSet: Unknown operation %d, skipping.", oper);
					return;
		}
	
		switch (0xFF & target) {
		case 0x8E: // Y-Offset for train sprites
			TrainGui._traininfo_vehicle_pitch = res;
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
				param[target] = res;
				if (target + 1 > param_end) param_end = target + 1;
			} else {
				Global.DEBUG_grf( 7, "ParamSet: Skipping unknown target 0x%02X", target);
			}
			break;
		}
	}

	void GRFInhibit(DataLoader bufp) //, int len)
	{
		/* <0E> <num> <grfids...>
		 *
		 * B num           Number of GRFIDs that follow
		 * D grfids        GRFIDs of the files to deactivate */
	
		byte num;
		int i;
	
		bufp.check_length( 1, "GRFInhibit");
		bufp.shift(1);
		//len--;
		num = bufp.grf_load_byte(); //len--;
		bufp.check_length( 4 * num, "GRFInhibit");
	
		for (i = 0; i < num; i++) {
			int grfid = bufp.grf_load_dword();
			GRFFile file = GRFFile.GetFileByGRFID(grfid);
	
			/* Unset activation flag */
			if (file != null) {
				GRFFile.grfmsg(GRFFile.severity.GMS_NOTICE, "GRFInhibit: Deactivating file ``%s''", file.getFilename());
				file.flags &= 0xFFFE;
			}
		}
	}

	static final SpecialSpriteHandler handlers[] = {
			/* 0x00 */ NewGrfActionProcessor::VehicleChangeInfo,
			/* 0x01 */ NewGrfActionProcessor::NewSpriteSet,
			/* 0x02 */ NewGrfActionProcessor::NewSpriteGroup,
			/* 0x03 */ NewGrfActionProcessor::NewVehicle_SpriteGroupMapping,
			/* 0x04 */ NewGrfActionProcessor::VehicleNewName,
			/* 0x05 */ NewGrfActionProcessor::GraphicsNew,
			/* 0x06 */ NewGrfActionProcessor::CfgApply,
			/* 0x07 */ NewGrfActionProcessor::SkipIf,
			/* 0x08 */ NewGrfActionProcessor::GRFInfo,
			/* 0x09 */ NewGrfActionProcessor::SkipIf,
			/* 0x0A */ NewGrfActionProcessor::SpriteReplace,
			/* 0x0B */ NewGrfActionProcessor::GRFError,
			/* 0x0C */ NewGrfActionProcessor::GRFComment,
			/* 0x0D */ NewGrfActionProcessor::ParamSet,
			/* 0x0E */ NewGrfActionProcessor::GRFInhibit,
			/* 0x0F */ null, // TODO implement
			/* 0x10 */ null  // TODO implement
	};

	
	abstract protected void loadSprite(int i, DataLoader bufp);

	
	
}





//typedef bool (*VCI_Handler)(uint engine, int numinfo, int prop, byte **buf, int len);

@FunctionalInterface
interface VCI_Handler
{
	boolean accept(NewGrfActionProcessor p, int engine, int numinfo, int prop, DataLoader buf); //, int len);
}

@FunctionalInterface
interface SpecialSpriteHandler
{
	//void accept(DataLoader buf, int len);
	void accept(NewGrfActionProcessor p, DataLoader buf);
}

