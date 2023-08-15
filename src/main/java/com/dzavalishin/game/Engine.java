package com.dzavalishin.game;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import com.dzavalishin.ids.EngineID;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.struct.DrawEngineInfo;
import com.dzavalishin.struct.EngineInfo;
import com.dzavalishin.tables.EngineTables;
import com.dzavalishin.tables.EngineTables2;
import com.dzavalishin.util.BinaryString;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Strings;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.Widget;
import com.dzavalishin.xui.Window;
import com.dzavalishin.xui.WindowDesc;
import com.dzavalishin.xui.WindowEvent;

public class Engine extends EngineTables implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int intro_date;
	int age;
	int reliability;
	int reliability_spd_dec;
	int reliability_start, reliability_max, reliability_final;
	int duration_phase_1, duration_phase_2, duration_phase_3;
	int lifelength;
	int flags;
	int preview_player;
	int preview_wait;
	int railtype;
	int player_avail;

	// type, ie Vehicle.VEH_Road, Vehicle.VEH_Train, etc. Same as in vehicle.h
	int type;

	private int index; // [dz] index in array				

	
	
	static public final int INVALID_ENGINE  = Vehicle.INVALID_ENGINE;
	static public final EngineID INVALID_ENGINE_ID  = EngineID.get( Vehicle.INVALID_ENGINE );
	public static final int CALLBACK_FAILED = 0xFFFF;



	
	
	
	
	
	
	
	
	
	
	

	
	public static RailVehicleInfo  RailVehInfo(int e)
	{
		assert(e >= 0 && e < Global._rail_vehicle_info.length);
		return Global._rail_vehicle_info[e];
	}

	public static ShipVehicleInfo  ShipVehInfo(int e ) //EngineID e)
	{
		assert(e >= Global.SHIP_ENGINES_INDEX && e < Global.SHIP_ENGINES_INDEX + Global._ship_vehicle_info.length);
		return Global._ship_vehicle_info[e - Global.SHIP_ENGINES_INDEX];
	}

	public static AircraftVehicleInfo  AircraftVehInfo(int e)
	{
		assert(e >= Global.AIRCRAFT_ENGINES_INDEX && e < Global.AIRCRAFT_ENGINES_INDEX + Global._aircraft_vehicle_info.length);
		return Global._aircraft_vehicle_info[e - Global.AIRCRAFT_ENGINES_INDEX];
	}

	public static RoadVehicleInfo  RoadVehInfo(int e)
	{
		assert(e >= Global.ROAD_ENGINES_INDEX && e < Global.ROAD_ENGINES_INDEX + Global._road_vehicle_info.length);
		return Global._road_vehicle_info[e - Global.ROAD_ENGINES_INDEX];
	}







	//void ShowEnginePreviewWindow(EngineID engine);

	public static void DeleteCustomEngineNames()
	{
		int i;
		//StringID 
		int old;

		for (i = 0; i != Global.TOTAL_NUM_ENGINES; i++) {
			old = _engine_name_strings[i];
			_engine_name_strings[i] = i + Str.STR_8000_KIRBY_PAUL_TANK_STEAM;
			Global.DeleteName(old);
		}

		Global._vehicle_design_names &= ~1;
	}

	public static void LoadCustomEngineNames()
	{
		// XXX: not done */
		Global.DEBUG_misc( 1, "LoadCustomEngineNames: not done");
	}

	static void SetupEngineNames()
	{
		//for (name = _engine_name_strings; name != endof(_engine_name_strings); name++)
		Arrays.fill(_engine_name_strings, Str.STR_SV_EMPTY);

		DeleteCustomEngineNames();
		LoadCustomEngineNames();
	}

	static void CalcEngineReliability(Engine e)
	{
		int age = e.age;

		if (age < e.duration_phase_1) {
			int start = e.reliability_start;
			e.reliability = age * (e.reliability_max - start) / e.duration_phase_1 + start;
		} else if ((age -= e.duration_phase_1) < e.duration_phase_2) {
			e.reliability = e.reliability_max;
		} else if ((age -= e.duration_phase_2) < e.duration_phase_3) {
			int max = e.reliability_max;
			e.reliability = age * (e.reliability_final - max) / e.duration_phase_3 + max;
		} else {
			// time's up for this engine
			// make it either available to all players (if never_expire_vehicles is enabled and if it was available earlier)
			// or disable this engine completely
			e.player_avail =  ((Global._patches.never_expire_vehicles.get() && e.player_avail != 0)? -1 : 0);
			e.reliability = e.reliability_final;
		}
	}

	static void AddTypeToEngines()
	{
		Engine [] e = Global.gs._engines;
		int i = 0;

		for( i = 0; i < Global.gs._engines.length; i++ )
			e[i] = new Engine();
		
		i = 0;
		do e[i].type = Vehicle.VEH_Train;    while (++i < Global.ROAD_ENGINES_INDEX);
		do e[i].type = Vehicle.VEH_Road;     while (++i < Global.SHIP_ENGINES_INDEX);
		do e[i].type = Vehicle.VEH_Ship;     while (++i < Global.AIRCRAFT_ENGINES_INDEX);
		do e[i].type = Vehicle.VEH_Aircraft; while (++i < Global.TOTAL_NUM_ENGINES);
		if( i < Global.gs._engines.length )
		{
			do e[i].type = Vehicle.VEH_Special;  while (++i < Global.gs._engines.length);
		}
	}

	public static void StartupEngines()
	{
		SetupEngineNames();

		for (int i = 0; i < Global.gs._engines.length; i++ ) 
		{
			final Engine e = Global.gs._engines[i];
			
			Global._engine_info[i] = new EngineInfo( EngineTables2.orig_engine_info[i] );
			
			final EngineInfo ei = Global._engine_info[i];

			int r;

			e.index = i;
			
			e.age = 0;
			e.railtype = ei.railtype;
			e.flags = 0;
			e.player_avail = 0;

			r = Hal.Random();
			e.intro_date = BitOps.GB(r, 0, 9) + ei.base_intro;
			if (e.intro_date <= Global.get_date()) {
				e.age = (Global.get_date() - e.intro_date) >> 5;
				e.player_avail = -1;
				e.flags |= ENGINE_AVAILABLE;
			}

			e.reliability_start = BitOps.GB(r, 16, 14) + 0x7AE0;
			r = Hal.Random();
			e.reliability_max   = BitOps.GB(r,  0, 14) + 0xBFFF;
			e.reliability_final = BitOps.GB(r, 16, 14) + 0x3FFF;

			r = Hal.Random();
			e.duration_phase_1 = BitOps.GB(r, 0, 5) + 7;
			e.duration_phase_2 = BitOps.GB(r, 5, 4) + ei.base_life * 12 - 96;
			e.duration_phase_3 = BitOps.GB(r, 9, 7) + 120;

			e.reliability_spd_dec = (ei.unk2&0x7F) << 2;

			/* my invented flag for something that is a wagon */
			if(0 != (ei.unk2 & 0x80)) {
				e.age = 0xFFFF;
			} else {
				CalcEngineReliability(e);
			}

			e.lifelength =  (ei.lifelength + Global._patches.extend_vehicle_life);

			// prevent certain engines from ever appearing.
			if (!BitOps.HASBIT(ei.climates, GameOptions._opt.landscape)) {
				e.flags |= ENGINE_AVAILABLE;
				e.player_avail = 0;
			}

			/* This sets up type for the engine
		   It is needed if you want to ask the engine what type it is
		   It should hopefully be the same as when you ask a vehicle what it is
		   but using this, you can ask what type an engine number is
		   even if it is not a vehicle (yet)*/
		}

		Airport.AdjustAvailAircraft();
	}

	// TODO: We don't support cargo-specific wagon overrides. Pretty exotic... ;-) --pasky

	static class WagonOverride {
		byte [] train_id;
		int trains;
		SpriteGroup group;
	}

	static class WagonOverrides {
		int overrides_count = 0;
		WagonOverride [] overrides;
	}

	static final WagonOverrides[] _engine_wagon_overrides = new WagonOverrides[Global.TOTAL_NUM_ENGINES];

	static void SetWagonOverrideSprites(EngineID engine, SpriteGroup group, byte [] train_id,
			int trains)
	{
		WagonOverrides wos;
		WagonOverride wo;

		wos = _engine_wagon_overrides[engine.id];
		wos.overrides_count++;
		//wos.overrides = realloc(wos.overrides,				wos.overrides_count * sizeof(*wos.overrides));
		//wos.overrides = new WagonOverride[wos.overrides_count];
		WagonOverride [] newo = new WagonOverride[wos.overrides_count];
		System.arraycopy(wos.overrides, 0, newo, 0, wos.overrides_count-1);
		wos.overrides = newo;
		
		wo = wos.overrides[wos.overrides_count - 1];
		/* FIXME: If we are replacing an override, release original SpriteGroup
		 * to prevent leaks. But first we need to refcount the SpriteGroup.
		 * --pasky */
		wo.group = group;
		group.ref_count++;
		wo.trains = trains;
		wo.train_id = new byte[trains];
		//memcpy(wo.train_id, train_id, trains);
		System.arraycopy(train_id, 0, wo.train_id, 0, trains);
	}

	static SpriteGroup GetWagonOverrideSpriteSet(EngineID engine, int overriding_engine)
	{
		final WagonOverrides wos = _engine_wagon_overrides[engine.id];
		int i;

		// XXX: This could turn out to be a timesink on profiles. We could
		// always just dedicate 65535 bytes for an [engine][train] trampoline
		// for O(1). Or O(logMlogN) and searching binary tree or smt. like
		// that. --pasky

		for (i = 0; i < wos.overrides_count; i++) {
			final WagonOverride wo = wos.overrides[i];
			int j;

			for (j = 0; j < wo.trains; j++) {
				if (wo.train_id[j] == overriding_engine)
					return wo.group;
			}
		}
		return null;
	}

	/**
	 * Unload all wagon override sprite groups.
	 */
	public static void UnloadWagonOverrides()
	{
		WagonOverrides wos;
		WagonOverride wo;
		//EngineID 
		int engine;
		int i;

		for (engine = 0; engine < Global.TOTAL_NUM_ENGINES; engine++) 
		{
			if( _engine_wagon_overrides[engine] == null )
			{
				_engine_wagon_overrides[engine] = new WagonOverrides();
				continue;
			}
			wos = _engine_wagon_overrides[engine];
			for (i = 0; i < wos.overrides_count; i++) {
				wo = wos.overrides[i];
				Sprite.UnloadSpriteGroup(wo.group);
				//free(wo.train_id);
			}
			//free(wos.overrides);
			wos.overrides_count = 0;
			wos.overrides = null;
		}
	}

	// 0 - 28 are cargos, 29 is default, 30 is the advert (purchase list)
	// (It isn't and shouldn't be like this in the GRF files since new cargo types
	// may appear in future - however it's more convenient to store it like this in
	// memory. --pasky)
	static final SpriteGroup [][] engine_custom_sprites = new SpriteGroup[Global.TOTAL_NUM_ENGINES][NUM_GLOBAL_CID];

	static void SetCustomEngineSprites(EngineID engine, int cargo, SpriteGroup group)
	{
		if (engine_custom_sprites[engine.id][cargo] != null) {
			Global.DEBUG_grf( 6,"SetCustomEngineSprites: engine `%d' cargo `%d' already has group -- removing.", engine, cargo);
			Sprite.UnloadSpriteGroup(engine_custom_sprites[engine.id][cargo]);
		}
		engine_custom_sprites[engine.id][cargo] = group;
		group.ref_count++;
	}

	/**
	 * Unload all engine sprite groups.
	 */
	public static void UnloadCustomEngineSprites()
	{
		//EngineID 
		int engine;
		///CargoID 
		int cargo;

		for (engine = 0; engine < Global.TOTAL_NUM_ENGINES; engine++) 
		{
			for (cargo = 0; cargo < NUM_GLOBAL_CID; cargo++) 
			{
				if (engine_custom_sprites[engine][cargo] != null) {
					Global.DEBUG_grf( 6,"UnloadCustomEngineSprites: Unloading group for engine `%d' cargo `%d'.", engine, cargo);
					Sprite.UnloadSpriteGroup(engine_custom_sprites[engine][cargo]);
				}
			}
		}
	}

	static int MapOldSubType(final Vehicle v)
	{
		if (v.type != Vehicle.VEH_Train) return v.subtype;
		if (v.IsTrainEngine()) return 0;
		if (v.IsFreeWagon()) return 4;
		return 2;
	}

	/*
	typedef SpriteGroup *(*resolve_callback)(final SpriteGroup *spritegroup,
			final Vehicle veh, int callback_info, void *resolve_func); /* data pointer used as function pointer */

	static SpriteGroup ResolveVehicleSpriteGroup(
			final SpriteGroup spritegroup,
			Vehicle veh, int callback_info, 
			resolve_callback resolve_func)
	{
		if (spritegroup == null)
			return null;

		//debug("spgt %d", spritegroup.type);
		switch (spritegroup.type) {
		case SGT_REAL:
		case SGT_CALLBACK:
			return spritegroup;

		case SGT_DETERMINISTIC: {
			final DeterministicSpriteGroup dsg = (DeterministicSpriteGroup)spritegroup;
			final SpriteGroup target;
			int value = -1;

			//debug("[%p] Having fun resolving variable %x", veh, dsg.variable);
			if (dsg.variable == 0x0C) {
				/* Callback ID */
				value = callback_info & 0xFF;
			} else if (dsg.variable == 0x10) {
				value = (callback_info >> 8) & 0xFF;
			} else if ((dsg.variable >> 6) == 0) {
				/* General property */
				value = Sprite.GetDeterministicSpriteValue(dsg.variable);
			} else {
				/* Vehicle-specific property. */

				if (veh == null) {
					/* We are in a purchase list of something,
					 * and we are checking for something undefined.
					 * That means we should get the first target
					 * (NOT the default one). */
					if (dsg.num_ranges > 0) {
						target = dsg.ranges[0].group;
					} else {
						target = dsg.default_group;
					}
					return resolve_func.apply(target, null, callback_info, resolve_func);
				}

				if (dsg.var_scope == VarSpriteGroupScope.VSG_SCOPE_PARENT) {
					/* First engine in the vehicle chain */
					if (veh.type == Vehicle.VEH_Train)
						veh = veh.GetFirstVehicleInChain();
				}

				if (dsg.variable == 0x40 || dsg.variable == 0x41) {
					if (veh.type == Vehicle.VEH_Train) {
						Vehicle u = veh.GetFirstVehicleInChain();
						byte chain_before = 0, chain_after = 0;

						while (u != veh) {
							chain_before++;
							if( dsg.variable == 0x41 && !u.getEngine_type().equals(veh.getEngine_type()) )
								chain_before = 0;
							u = u.next;
						}
						while (u.next != null && (dsg.variable == 0x40 || u.next.getEngine_type().equals(veh.getEngine_type()) )) 
						{
							chain_after++;
							u = u.next;
						}

						value = chain_before | chain_after << 8
								| (chain_before + chain_after) << 16;
					} else {
						value = 1; /* 1 vehicle in the chain */
					}

				} else {
					// TTDPatch runs on little-endian arch;
					// Variable is 0x80 + offset in TTD's vehicle structure
					switch (dsg.variable - 0x80) {
					//#define veh_prop(id_, value_) case (id_): value = (value_); break
					case 0x00: value =  veh.type; break;
					case 0x01: value =  MapOldSubType(veh); break;
					case 0x04: value =  veh.index; break;
					case 0x05: value =  veh.index & 0xFF; break;
					/* XXX? Is THIS right? */
					case 0x0A: value =  Order.PackOrder(veh.getCurrent_order()); break;
					case 0x0B: value =  Order.PackOrder(veh.getCurrent_order()) & 0xff; break;
					case 0x0C: value =  veh.num_orders; break;
					case 0x0D: value =  veh.cur_order_index; break;
					case 0x10: value =  veh.load_unload_time_rem; break;
					case 0x11: value =  veh.load_unload_time_rem & 0xFF; break;
					case 0x12: value =  veh.date_of_last_service; break;
					case 0x13: value =  veh.date_of_last_service & 0xFF; break;
					case 0x14: value =  veh.service_interval; break;
					case 0x15: value =  veh.service_interval & 0xFF; break;
					case 0x16: value =  veh.last_station_visited; break;
					case 0x17: value =  veh.tick_counter; break;
					case 0x18: value =  veh.getMax_speed(); break;
					case 0x19: value =  veh.getMax_speed() & 0xFF; break;
					case 0x1F: value =  veh.direction; break;
					case 0x28: value =  veh.cur_image; break;
					case 0x29: value =  veh.cur_image & 0xFF; break;
					case 0x32: value =  veh.getStatus(); break;
					case 0x33: value =  veh.getStatus(); break;
					case 0x34: value =  veh.cur_speed; break;
					case 0x35: value =  veh.cur_speed & 0xFF; break;
					case 0x36: value =  veh.subspeed; break;
					case 0x37: value =  veh.acceleration; break;
					case 0x39: value =  veh.getCargo_type(); break;
					case 0x3A: value =  veh.getCargo_cap(); break;
					case 0x3B: value =  veh.getCargo_cap() & 0xFF; break;
					case 0x3C: value =  veh.cargo_count; break;
					case 0x3D: value =  veh.cargo_count & 0xFF; break;
					case 0x3E: value =  veh.getCargo_source(); break; // Probably useless; so what
					case 0x3F: value =  veh.cargo_days; break;
					case 0x40: value =  veh.age; break;
					case 0x41: value =  veh.age & 0xFF; break;
					case 0x42: value =  veh.max_age; break;
					case 0x43: value =  veh.max_age & 0xFF; break;
					case 0x44: value =  veh.getBuild_year(); break;
					case 0x45: value =  veh.unitnumber.id; break;
					case 0x46: value =  veh.getEngine_type().id; break;
					case 0x47: value =  veh.getEngine_type().id & 0xFF; break;
					case 0x48: value =  veh.spritenum; break;
					case 0x49: value =  veh.day_counter; break;
					case 0x4A: value =  veh.breakdowns_since_last_service; break;
					case 0x4B: value =  veh.breakdown_ctr; break;
					case 0x4C: value =  veh.breakdown_delay; break;
					case 0x4D: value =  veh.breakdown_chance; break;
					case 0x4E: value =  veh.reliability; break;
					case 0x4F: value =  veh.reliability & 0xFF; break;
					case 0x50: value =  veh.reliability_spd_dec; break;
					case 0x51: value =  veh.reliability_spd_dec & 0xFF; break;
					case 0x52: value =  veh.profit_this_year; break;
					case 0x53: value =  veh.profit_this_year & 0xFFFFFF; break;
					case 0x54: value =  veh.profit_this_year & 0xFFFF; break;
					case 0x55: value =  veh.profit_this_year & 0xFF; break;
					case 0x56: value =  veh.profit_last_year; break;
					case 0x57: value =  veh.profit_last_year & 0xFF; break;
					case 0x58: value =  veh.profit_last_year; break;
					case 0x59: value =  veh.profit_last_year & 0xFF; break;
					case 0x5A: value =  veh.next == null ? Vehicle.INVALID_VEHICLE : veh.next.index; break;
					case 0x5C: value =  veh.value; break;
					case 0x5D: value =  veh.value & 0xFFFFFF; break;
					case 0x5E: value =  veh.value & 0xFFFF; break;
					case 0x5F: value =  veh.value & 0xFF; break;
					case 0x60: value =  veh.string_id; break;
					case 0x61: value =  veh.string_id & 0xFF; break;
					/* 00h..07h=sub image? 40h=in tunnel; actually some kind of status
					 * aircraft: >=13h when in flight
					 * train, ship: 80h=in depot
					 * rv: 0feh=in depot */
					/* TODO veh_prop(0x62, veh.???); */

					/* TODO: The rest is per-vehicle, I hope no GRF file looks so far.
					 * But they won't let us have an easy ride so surely *some* GRF
					 * file does. So someone needs to do this too. --pasky */

					//#undef veh_prop
					}
				}
			}

			target = value != -1 ? Sprite.EvalDeterministicSpriteGroup(dsg, value) : dsg.default_group;
			//debug("Resolved variable %x: %d, %p", dsg.variable, value, callback);
			return resolve_func.apply(target, veh, callback_info, resolve_func);
		}

		case SGT_RANDOMIZED: {
			final RandomizedSpriteGroup rsg = (RandomizedSpriteGroup)spritegroup;

			if (veh == null) {
				// Purchase list of something. Show the first one. 
				assert(rsg.num_groups > 0);
				//debug("going for %p: %d", rsg.groups[0], rsg.groups[0].type);
				return resolve_func.apply(rsg.groups[0], null, callback_info, resolve_func);
			}

			if (rsg.var_scope == VarSpriteGroupScope.VSG_SCOPE_PARENT) {
				// First engine in the vehicle chain 
				if (veh.type == Vehicle.VEH_Train)
					veh = veh.GetFirstVehicleInChain();
			}

			return resolve_func.apply(Sprite.EvalRandomizedSpriteGroup(rsg, veh.random_bits), veh, callback_info, resolve_func);
		}

		default:
			Global.error("I don't know how to handle such a spritegroup %d!", spritegroup.type);
			return null;
		}
	}

	static SpriteGroup GetVehicleSpriteGroup(EngineID engine, final Vehicle v)
	{
		SpriteGroup group;
		int cargo = GC_PURCHASE;

		if (v != null) {
			cargo =  _global_cargo_id[GameOptions._opt.landscape][v.getCargo_type()];
			assert(cargo != GC_INVALID);
		}

		group = engine_custom_sprites[engine.id][cargo];

		if (v != null && v.type == Vehicle.VEH_Train) {
			final SpriteGroup overset = GetWagonOverrideSpriteSet(engine, v.rail.first_engine.id);

			if (overset != null) group = overset;
		}

		return group;
	}

	static int GetCustomEngineSprite(EngineID engine, final Vehicle v, int direction)
	{
		SpriteGroup group;
		final RealSpriteGroup rsg;
		int cargo = GC_PURCHASE;
		int loaded = 0;
		boolean in_motion = false;
		int totalsets, spriteset;
		int r;

		if (v != null) {
			int capacity = v.getCargo_cap();

			cargo =  _global_cargo_id[GameOptions._opt.landscape][v.getCargo_type()];
			assert(cargo != GC_INVALID);

			if (capacity == 0) capacity = 1;
			loaded =  ((v.cargo_count * 100) / capacity);
			in_motion = (v.cur_speed != 0);
		}

		group = GetVehicleSpriteGroup(engine, v);
		group = ResolveVehicleSpriteGroup(group, v, 0, Engine::ResolveVehicleSpriteGroup);

		if (group == null && cargo != GC_DEFAULT) {
			// This group is empty but perhaps there'll be a default one.
			group = ResolveVehicleSpriteGroup(engine_custom_sprites[engine.id][GC_DEFAULT], v, 0,
					Engine::ResolveVehicleSpriteGroup);
		}

		if (group == null)
			return 0;

		assert(group.type == SpriteGroupType.SGT_REAL);
		rsg = (RealSpriteGroup)group;

		if (0 == rsg.sprites_per_set) {
			// This group is empty. This function users should therefore
			// look up the sprite number in _engine_original_sprites.
			return 0;
		}

		assert(rsg.sprites_per_set <= 8);
		direction %= rsg.sprites_per_set;

		//totalsets = in_motion ? rsg.loaded_count : rsg.loading_count;
		totalsets = in_motion ? rsg.loaded.length : rsg.loading.length;

		// My aim here is to make it possible to visually determine absolutely
		// empty and totally full vehicles. --pasky
		if (loaded == 100 || totalsets == 1) { // full
			spriteset = totalsets - 1;
		} else if (loaded == 0 || totalsets == 2) { // empty
			spriteset = 0;
		} else { // something inbetween
			spriteset = loaded * (totalsets - 2) / 100 + 1;
			// correct possible rounding errors
			if (0 == spriteset)
				spriteset = 1;
			else if (spriteset == totalsets - 1)
				spriteset--;
		}

		r = (in_motion ? ((ResultSpriteGroup)rsg.loaded[spriteset]).result : ((ResultSpriteGroup)rsg.loading[spriteset]).result) + direction;
		return r;
	}

	/**
	 * Check if a wagon is currently using a wagon override
	 * @param v The wagon to check
	 * @return true if it is using an override, false otherwise
	 */
	static boolean UsesWagonOverride(final Vehicle  v)
	{
		assert(v.type == Vehicle.VEH_Train);
		return GetWagonOverrideSpriteSet(v.getEngine_type(), v.rail.first_engine.id) != null;
	}

	/**
	 * Evaluates a newgrf callback
	 * @param callback_info info about which callback to evaluate
	 *  (bit 0-7)  = CallBack id of the callback to use, see CallBackId enum
	 *  (bit 8-15) = Other info some callbacks need to have, callback specific, see CallBackId enum, not used yet
	 * @param engine Engine type of the vehicle to evaluate the callback for
	 * @param vehicle The vehicle to evaluate the callback for, null if it doesnt exist (yet)
	 * @return The value the callback returned, or CALLBACK_FAILED if it failed
	 */
	static int GetCallBackResult(int callback_info, EngineID engine, final Vehicle v)
	{
		SpriteGroup group;
		int cargo = GC_DEFAULT;

		if (v != null)
			cargo =  _global_cargo_id[GameOptions._opt.landscape][v.getCargo_type()];

		group = engine_custom_sprites[engine.id][cargo];

		if (v != null && v.type == Vehicle.VEH_Train) {
			final SpriteGroup overset = GetWagonOverrideSpriteSet(engine, v.rail.first_engine.id);

			if (overset != null) group = overset;
		}

		group = ResolveVehicleSpriteGroup(group, v, callback_info, Engine::ResolveVehicleSpriteGroup);

		if (group == null && cargo != GC_DEFAULT) {
			// This group is empty but perhaps there'll be a default one.
			group = ResolveVehicleSpriteGroup(engine_custom_sprites[engine.id][GC_DEFAULT], v, callback_info,
					Engine::ResolveVehicleSpriteGroup);
		}

		if (group == null || group.type != SpriteGroupType.SGT_CALLBACK)
			return CALLBACK_FAILED;

		return ((CallbackResultSpriteGroup)group).result;
	}



	// Global variables are evil, yes, but we would end up with horribly overblown
	// calling convention otherwise and this should be 100% reentrant.
	static int _vsg_random_triggers;
	static byte _vsg_bits_to_reseed;

	static SpriteGroup TriggerVehicleSpriteGroup(final SpriteGroup spritegroup,
												 Vehicle veh, int callback_info, resolve_callback resolve_func)
	{
		if (spritegroup == null)
			return null;

		if (spritegroup.type == SpriteGroupType.SGT_RANDOMIZED) 
		{
			byte[] trigPtr = {(byte) veh.waiting_triggers};
			
			_vsg_bits_to_reseed |= Sprite.RandomizedSpriteGroupTriggeredBits(
					(RandomizedSpriteGroup)spritegroup,
					_vsg_random_triggers,
					trigPtr
					);
			veh.waiting_triggers = trigPtr[0];
		}

		return ResolveVehicleSpriteGroup(spritegroup, veh, callback_info, resolve_func);
	}

	//static void DoTriggerVehicle(Vehicle veh, VehicleTrigger trigger, byte base_random_bits, boolean first)
	static void DoTriggerVehicle(Vehicle veh, int trigger, int base_random_bits, boolean first)
	{
		SpriteGroup group;
		final RealSpriteGroup rsg;
		byte new_random_bits;

		_vsg_random_triggers = trigger;
		_vsg_bits_to_reseed = 0;
		group = TriggerVehicleSpriteGroup(GetVehicleSpriteGroup(veh.getEngine_type(), veh), veh, 0,
				Engine::TriggerVehicleSpriteGroup);

		if (group == null && veh.getCargo_type() != GC_DEFAULT) {
			// This group turned out to be empty but perhaps there'll be a default one.
			group = TriggerVehicleSpriteGroup(engine_custom_sprites[veh.getEngine_type().id][GC_DEFAULT], veh, 0,
					Engine::TriggerVehicleSpriteGroup);
		}

		if (group == null)
			return;

		assert(group.type == SpriteGroupType.SGT_REAL);
		rsg = (RealSpriteGroup)group;

		new_random_bits = (byte) Hal.Random();
		veh.random_bits &= ~_vsg_bits_to_reseed;
		veh.random_bits |= (first ? new_random_bits : base_random_bits) & _vsg_bits_to_reseed;

		switch (trigger) {
		case VEHICLE_TRIGGER_NEW_CARGO:
			/* All vehicles in chain get ANY_NEW_CARGO trigger now.
			 * So we call it for the first one and they will recurse. */
			/* Indexing part of vehicle random bits needs to be
			 * same for all triggered vehicles in the chain (to get
			 * all the random-cargo wagons carry the same cargo,
			 * i.e.), so we give them all the NEW_CARGO triggered
			 * vehicle's portion of random bits. */
			assert(first);
			DoTriggerVehicle(veh.GetFirstVehicleInChain(), VEHICLE_TRIGGER_ANY_NEW_CARGO, new_random_bits, false);
			break;
		case VEHICLE_TRIGGER_DEPOT:
			/* We now trigger the next vehicle in chain recursively.
			 * The random bits portions may be different for each
			 * vehicle in chain. */
			if (veh.next != null)
				DoTriggerVehicle(veh.next, trigger, 0, true);
			break;
		case VEHICLE_TRIGGER_EMPTY:
			/* We now trigger the next vehicle in chain
			 * recursively.  The random bits portions must be same
			 * for each vehicle in chain, so we give them all
			 * first chained vehicle's portion of random bits. */
			if (veh.next != null)
				DoTriggerVehicle(veh.next, trigger, first ? new_random_bits : base_random_bits, false);
			break;
		case VEHICLE_TRIGGER_ANY_NEW_CARGO:
			/* Now pass the trigger recursively to the next vehicle
			 * in chain. */
			assert(!first);
			if (veh.next != null)
				DoTriggerVehicle(veh.next, VEHICLE_TRIGGER_ANY_NEW_CARGO, base_random_bits, false);
			break;
		}
	}

	//static void TriggerVehicle(Vehicle veh, VehicleTrigger trigger)
	static void TriggerVehicle(Vehicle veh, int trigger)
	{
		if (trigger == VEHICLE_TRIGGER_DEPOT) {
			// store that the vehicle entered a depot this tick
			veh.VehicleEnteredDepotThisTick();
		}

		DoTriggerVehicle(veh, trigger, 0, true);
	}

	static final String[] _engine_custom_names = new String[Global.TOTAL_NUM_ENGINES];

	static void SetCustomEngineName(EngineID engine, String name)
	{
		_engine_custom_names[engine.id] = name;
	}

	public static void UnloadCustomEngineNames()
	{
		//char **i;
		//free(*i);
		Arrays.fill(_engine_custom_names, null);
	}

	//StringID GetCustomEngineName(EngineID engine)
	public static StringID GetCustomEngineName(int engine)
	{
		if (null == _engine_custom_names[engine])
			return new StringID( _engine_name_strings[engine] );
		//ttd_strlcpy(_userstring, _engine_custom_names[engine], lengthof(_userstring));
		Strings._userstring = new BinaryString(_engine_custom_names[engine]);
		return new StringID(Strings.STR_SPEC_USERSTRING);
	}


	static void AcceptEnginePreview(Engine e, PlayerID player)
	{
		Player p = Player.GetPlayer(player);

		assert(e.railtype < Rail.RAILTYPE_END);
		//e.player_avail = BitOps.RETSETBIT(e.player_avail, player.id);
		e.setAvailableTo(player.id);
		p.avail_railtypes = BitOps.RETSETBIT(p.avail_railtypes, e.railtype);

		e.preview_player =  0xFF;
		Window.InvalidateWindowClasses(Window.WC_BUILD_VEHICLE);
		Window.InvalidateWindowClasses(Window.WC_REPLACE_VEHICLE);
	}

	//static PlayerID GetBestPlayer(PlayerID pp)
	static int GetBestPlayer(int pp)
	{
		int best_hist;
		//PlayerID 
		int best_player;
		int mask = 0;

		do {
			best_hist = -1;
			best_player = Owner.OWNER_SPECTATOR;

			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player p = ii.next();
				
				if (p.is_active && p.block_preview == 0 && !BitOps.HASBIT(mask, p.index.id) &&
						p.old_economy[0].performance_history > best_hist) {
					best_hist = p.old_economy[0].performance_history;
					best_player = p.index.id;
				}
			}

			if (best_player == Owner.OWNER_SPECTATOR) return Owner.OWNER_SPECTATOR;

			mask = BitOps.RETSETBIT(mask, best_player);
		} while (--pp != 0);

		return best_player;
	}

	public static void EnginesDailyLoop()
	{
		if (Global.get_cur_year() >= 130) return;

		for (int i = 0; i != Global.gs._engines.length; i++) {
			Engine  e = Global.gs._engines[i];

			if(0 != (e.flags & ENGINE_INTRODUCING)) {
				if(0 !=  (e.flags & ENGINE_PREVIEWING)) {
					if (e.preview_player != 0xFF && 0 == --e.preview_wait) {
						e.flags &= ~ENGINE_PREVIEWING;
						Window.DeleteWindowById(Window.WC_ENGINE_PREVIEW, i);
						e.preview_player++;
					}
				} else if (e.preview_player != 0xFF) {
					PlayerID best_player = PlayerID.get( GetBestPlayer(e.preview_player) );

					if (best_player.isSpectator()) {
						e.preview_player =  0xFF;
						continue;
					}

					if (!best_player.IS_HUMAN_PLAYER()) {
						/* XXX - TTDBUG: TTD has a bug here ???? */
						AcceptEnginePreview(e, best_player);
					} else {
						e.flags |= ENGINE_PREVIEWING;
						e.preview_wait = 20;
						if (best_player.IS_INTERACTIVE_PLAYER()) ShowEnginePreviewWindow(i);
					}
				}
			}
		}
	}

	/** Accept an engine prototype. XXX - it is possible that the top-player
	 * changes while you are waiting to accept the offer? Then it becomes invalid
	 * @param x,y unused
	 * @param p1 engine-prototype offered
	 * @param p2 unused
	 */
	static int CmdWantEnginePreview(int x, int y, int flags, int p1, int p2)
	{
		Engine e;
		if (!Engine.IsEngineIndex(p1)) return Cmd.CMD_ERROR;

		e = Engine.GetEngine(p1);
		//if (GetBestPlayer(e.preview_player) != Global.gs._current_player.id) return Cmd.CMD_ERROR;
		if (GetBestPlayer(e.preview_player) != PlayerID.getCurrent().id) return Cmd.CMD_ERROR;

		if( 0 != (flags & Cmd.DC_EXEC))
			AcceptEnginePreview(e, PlayerID.getCurrent());

		return 0;
	}

	// Determine if an engine type is a wagon (and not a loco)
	static boolean IsWagon(EngineID index)
	{
		return (index.id < Global.NUM_TRAIN_ENGINES) && RailVehInfo(index.id).isWagon();
	}

	static boolean IsWagon(int index)
	{
		return (index < Global.NUM_TRAIN_ENGINES) && RailVehInfo(index).isWagon();
	}

	static void NewVehicleAvailable(Engine e)
	{
		//EngineID 
		//int index = e - Global._engines;
		int index = e.index;

		// In case the player didn't build the vehicle during the intro period,
		// prevent that player from getting future intro periods for a while.
		if(0 != (e.flags & ENGINE_INTRODUCING)) {

			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player p = ii.next();
				int block_preview = p.block_preview;

				if (!e.isAvailableTo(p.index)) continue;

				/* We assume the user did NOT build it.. prove me wrong ;) */
				p.block_preview = 20;

				Iterator<Vehicle> it = Vehicle.getIterator();
				while(it.hasNext())
				{
					Vehicle v = it.next();
					if (v.type == Vehicle.VEH_Train || v.type == Vehicle.VEH_Road || v.type == Vehicle.VEH_Ship ||
							(v.type == Vehicle.VEH_Aircraft && v.subtype <= 2)) 
					{
						if(v.owner.equals(p.index) && v.getEngine_type().id == index) 
						{
							/* The user did prove me wrong, so restore old value */
							p.block_preview =  block_preview;
							break;
						}
					}
				}
			}
		}

		e.flags =  ((e.flags & ~ENGINE_INTRODUCING) | ENGINE_AVAILABLE);
		Window.InvalidateWindowClasses(Window.WC_BUILD_VEHICLE);
		Window.InvalidateWindowClasses(Window.WC_REPLACE_VEHICLE);

		// Now available for all players
		e.player_avail = -1;

		// Do not introduce new rail wagons
		if (IsWagon(index)) return;

		// make maglev / monorail available
		Player.forEach( (p) ->
		{
			if (p.is_active) {
				assert(e.railtype < Rail.RAILTYPE_END);
				p.avail_railtypes = BitOps.RETSETBIT(p.avail_railtypes, e.railtype);
			}
		});

		if (index < Global.NUM_TRAIN_ENGINES) {
			NewsItem.AddNewsItem(index, NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_NEW_VEHICLES, NewsItem.DNC_TRAINAVAIL), 0, 0);
		} else if (index < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES) {
			NewsItem.AddNewsItem(index, NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_NEW_VEHICLES, NewsItem.DNC_ROADAVAIL), 0, 0);
		} else if (index < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES + Global.NUM_SHIP_ENGINES) {
			NewsItem.AddNewsItem(index, NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_NEW_VEHICLES, NewsItem.DNC_SHIPAVAIL), 0, 0);
		} else {
			NewsItem.AddNewsItem(index, NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_NEW_VEHICLES, NewsItem.DNC_AIRCRAFTAVAIL), 0, 0);
		}
	}

	public static void EnginesMonthlyLoop()
	{
		if (Global.get_cur_year() < 130) {
			for (int i = 0 ; i < Global.gs._engines.length; i++) 
			{
				Engine e = Global.gs._engines[i];
				// Age the vehicle
				if(0 != (e.flags & ENGINE_AVAILABLE) && e.age != 0xFFFF) {
					e.age++;
					CalcEngineReliability(e);
				}

				if (0 == (e.flags & ENGINE_AVAILABLE) && (Global.get_date() - Math.min(Global.get_date(), 365)) >= e.intro_date) {
					// Introduce it to all players
					NewVehicleAvailable(e);
				} else if (0 == (e.flags & (ENGINE_AVAILABLE|ENGINE_INTRODUCING)) && Global.get_date() >= e.intro_date) {
					// Introduction date has passed.. show introducing dialog to one player.
					e.flags |= ENGINE_INTRODUCING;

					// Do not introduce new rail wagons
					if (!IsWagon(i))
						e.preview_player = 1; // Give to the player with the highest rating.
				}
			}
		}
		Airport.AdjustAvailAircraft();
	}

	/** Rename an engine.
	 * @param x,y unused
	 * @param p1 engine ID to rename
	 * @param p2 unused
	 */
	public static int CmdRenameEngine(int x, int y, int flags, int p1, int p2)
	{
		StringID str;

		if (!IsEngineIndex(p1) || Global._cmd_text == null) return Cmd.CMD_ERROR;

		str = Global.AllocateNameUnique(Global._cmd_text, 0);
		if (str == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			StringID old_str = new StringID( _engine_name_strings[p1] );
			_engine_name_strings[p1] = str.id;
			Global.DeleteName(old_str);
			Global._vehicle_design_names |= 3;
			Hal.MarkWholeScreenDirty();
		} else {
			Global.DeleteName(str);
		}

		return 0;
	}

	
	/*

	static final SaveLoad _engine_desc[] = {
			SLE_VAR(Engine,intro_date,						SLE_UINT16),
			SLE_VAR(Engine,age,										SLE_UINT16),
			SLE_VAR(Engine,reliability,						SLE_UINT16),
			SLE_VAR(Engine,reliability_spd_dec,		SLE_UINT16),
			SLE_VAR(Engine,reliability_start,			SLE_UINT16),
			SLE_VAR(Engine,reliability_max,				SLE_UINT16),
			SLE_VAR(Engine,reliability_final,			SLE_UINT16),
			SLE_VAR(Engine,duration_phase_1,			SLE_UINT16),
			SLE_VAR(Engine,duration_phase_2,			SLE_UINT16),
			SLE_VAR(Engine,duration_phase_3,			SLE_UINT16),

			SLE_VAR(Engine,lifelength,						SLE_UINT8),
			SLE_VAR(Engine,flags,									SLE_UINT8),
			SLE_VAR(Engine,preview_player,				SLE_UINT8),
			SLE_VAR(Engine,preview_wait,					SLE_UINT8),
			SLE_VAR(Engine,railtype,							SLE_UINT8),
			SLE_VAR(Engine,player_avail,					SLE_UINT8),

			// reserve extra space in savegame here. (currently 16 bytes)
			SLE_CONDARR(NullStruct,null,SLE_FILE_U64 | SLE_VAR_null, 2, 2, 255),

			SLE_END()
	};

	static void Save_ENGN()
	{
		int i;

		for (i = 0; i != lengthof(_engines); i++) {
			SlSetArrayIndex(i);
			SlObject(&_engines[i], _engine_desc);
		}
	}

	static void Load_ENGN()
	{
		int index;
		while ((index = SlIterateArray()) != -1) {
			SlObject(GetEngine(index), _engine_desc);
		}
	}

	static void LoadSave_ENGS()
	{
		SlArray(_engine_name_strings, lengthof(_engine_name_strings), SLE_STRINGID);
	}

	final ChunkHandler _engine_chunk_handlers[] = {
			{ 'ENGN', Save_ENGN, Load_ENGN, CH_ARRAY},
			{ 'ENGS', LoadSave_ENGS, LoadSave_ENGS, CH_RIFF | CH_LAST},
	};
	*/

	/*
	 * returns true if an engine is valid, of the specified type, and buildable by
	 * the current player, false otherwise
	 *
	 * engine = index of the engine to check
	 * type   = the type the engine should be of (Vehicle.VEH_xxx)
	 */
	static boolean IsEngineBuildable(int engine, int type)
	{
		final Engine e;

		// check if it's an engine that is in the engine array
		if (!Engine.IsEngineIndex(engine)) return false;

		e = Engine.GetEngine(engine);

		// check if it's an engine of specified type
		if (e.type != type) return false;

		// check if it's available
		return e.isAvailableTo(PlayerID.getCurrent());
	}



	
	
	
	
	
	
	
	
	









	
	
	
	public static final /*StringID*/ int [] _engine_name_strings = new int[Global.TOTAL_NUM_ENGINES];

	public static Engine GetEngine(EngineID i)
	{
	  assert(i.id < Global.gs._engines.length);
	  return Global.gs._engines[i.id];
	}

	public static Engine GetEngine(int i)
	{
	  assert(i < Global.gs._engines.length);
	  return Global.gs._engines[i];
	}

	public static boolean IsEngineIndex(int index)
	{
		return index >= 0 && index < Global.TOTAL_NUM_ENGINES;
	}



	//static StringID GetEngineCategoryName(EngineID engine)
	static int GetEngineCategoryName(int engine)
	{
		if (engine < Global.NUM_TRAIN_ENGINES) {
			switch (GetEngine(engine).railtype) {
			case Train.RAILTYPE_RAIL:   return Str.STR_8102_RAILROAD_LOCOMOTIVE;
			case Train.RAILTYPE_MONO:   return Str.STR_8106_MONORAIL_LOCOMOTIVE;
			case Train.RAILTYPE_MAGLEV: return Str.STR_8107_MAGLEV_LOCOMOTIVE;
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
			new DrawEngineInfo( TrainCmd::DrawTrainEngine, Engine::DrawTrainEngineInfo ),
			new DrawEngineInfo( RoadVehCmd::DrawRoadVehEngine, Engine::DrawRoadVehEngineInfo ),
			new DrawEngineInfo( Ship::DrawShipEngine, Engine::DrawShipEngineInfo ),
			new DrawEngineInfo( AirCraft::DrawAircraftEngine, Engine::DrawAircraftEngineInfo ),
	};

	static void EnginePreviewWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			//EngineID engine = w.window_number;
			int engine = w.window_number;
			DrawEngineInfo dei;
			int width = w.getWidth();

			w.DrawWindowWidgets();

			Global.SetDParam(0, GetEngineCategoryName(engine));
			Gfx.DrawStringMultiCenter(150, 44, Str.STR_8101_WE_HAVE_JUST_DESIGNED_A, 296);

			Gfx.DrawStringCentered(width >> 1, 80, GetCustomEngineName(engine), 0x10);

			if(engine < Global.NUM_TRAIN_ENGINES) 
				dei = _draw_engine_list[0];
			else if(engine < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES)
				dei = _draw_engine_list[1];
			else if(engine < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES + Global.NUM_SHIP_ENGINES)
				dei = _draw_engine_list[2];
			else
				dei = _draw_engine_list[3];

			dei.engine_proc.accept(width >> 1, 100, engine, 0);
			dei.info_proc.accept(engine, width >> 1, 130, width - 52);
			break;
		}

		case WE_CLICK:
			switch (e.widget) {
			case 3:
				w.DeleteWindow();
				break;

			case 4:
				Cmd.DoCommandP( null, w.window_number, 0, null, Cmd.CMD_WANT_ENGINE_PREVIEW );
				w.DeleteWindow();
				break;
			}
			break;
		default:
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


	static //void ShowEnginePreviewWindow(EngineID engine)
	void ShowEnginePreviewWindow(int engine)
	{
		Window w;

		w = Window.AllocateWindowDesc(_engine_preview_desc,0);
		w.window_number = engine;
	}

	static void DrawTrainEngineInfo(int engine, int x, int y, int maxw)
	{
		final RailVehicleInfo rvi = RailVehInfo(engine);
		int multihead = rvi.isMulttihead() ? 1 : 0;

		Global.SetDParam(0, (((int)Global._price.build_railvehicle) >> 3) * rvi.base_cost >> 5);
		Global.SetDParam(2, rvi.getMax_speed() * 10 >> 4);
		Global.SetDParam(3, rvi.power << multihead);
		Global.SetDParam(1, rvi.weight << multihead);

		Global.SetDParam(4, rvi.running_cost_base * ((int)Global._price.running_rail[rvi.engclass]) >> 8 << multihead);

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
		
		int width = w.getWidth();
		
		engine = EngineID.get( w.as_news_d().ni.getString_id().id ); // TODO add field of EngineID type?
		Global.SetDParam(0, GetEngineCategoryName(engine.id));
		Gfx.DrawStringMultiCenter(width >> 1, 20, Str.STR_8859_NEW_NOW_AVAILABLE, width - 2);

		Gfx.GfxFillRect(25, 56, width - 25, w.getHeight() - 2, 10);

		Global.SetDParam(0, GetCustomEngineName(engine.id).id);
		Gfx.DrawStringMultiCenter(width >> 1, 57, Str.STR_885A, width - 2);

		TrainCmd.DrawTrainEngine(width >> 1, 88, engine.id, 0);
		Gfx.GfxFillRect(25, 56, width - 56, 112, 0x323 | Sprite.USE_COLORTABLE);
		DrawTrainEngineInfo(engine.id, width >> 1, 129, width - 52);
	}

	//StringID GetNewsStringNewTrainAvail(final NewsItem ni)
	static int GetNewsStringNewTrainAvail(final NewsItem ni)
	{
		//EngineID
		int engine = ni.getString_id().id;
		Global.SetDParam(0, Str.STR_8859_NEW_NOW_AVAILABLE);
		Global.SetDParam(1, GetEngineCategoryName(engine));
		Global.SetDParam(2, GetCustomEngineName(engine).id);
		return Str.STR_02B6;
	}

	static void DrawAircraftEngineInfo(int engine, int x, int y, int maxw)
	{
		final AircraftVehicleInfo avi = AircraftVehInfo(engine);
		Global.SetDParam(0, (((int)Global._price.aircraft_base) >> 3) * avi.base_cost >> 5);
		Global.SetDParam(1, avi.max_speed << 3);
		Global.SetDParam(2, avi.passenger_capacity);
		Global.SetDParam(3, avi.mail_capacity);
		Global.SetDParam(4, avi.running_cost * ((int)Global._price.aircraft_running) >> 8);

		Gfx.DrawStringMultiCenter(x, y, Str.STR_A02E_COST_MAX_SPEED_CAPACITY, maxw);
	}

	static void DrawNewsNewAircraftAvail(Window w)
	{
		//EngineID 
		int engine;

		NewsItem.DrawNewsBorder(w);

		int width = w.getWidth();

		engine = w.as_news_d().ni.getString_id().id;

		Gfx.DrawStringMultiCenter(width >> 1, 20, Str.STR_A02C_NEW_AIRCRAFT_NOW_AVAILABLE, width - 2);
		Gfx.GfxFillRect(25, 56, width - 25, w.getHeight() - 2, 10);

		Global.SetDParam(0, GetCustomEngineName(engine).id);
		Gfx.DrawStringMultiCenter(width >> 1, 57, Str.STR_A02D, width - 2);

		AirCraft.DrawAircraftEngine(width >> 1, 93, engine, 0);
		Gfx.GfxFillRect(25, 56, width - 56, 110, 0x323 | Sprite.USE_COLORTABLE);
		DrawAircraftEngineInfo( engine, width >> 1, 131, width - 52);
	}

	static int GetNewsStringNewAircraftAvail(final NewsItem ni)
	{
		//EngineID 
		int engine = ni.getString_id().id;
		Global.SetDParam(0, Str.STR_A02C_NEW_AIRCRAFT_NOW_AVAILABLE);
		Global.SetDParam(1, GetCustomEngineName(engine).id);
		return Str.STR_02B6;
	}

	static void DrawRoadVehEngineInfo(int engine, int x, int y, int maxw)
	{
		final RoadVehicleInfo rvi = RoadVehInfo(engine);

		Global.SetDParam(0, (((int)Global._price.roadveh_base) >> 3) * rvi.base_cost >> 5);
		Global.SetDParam(1, rvi.max_speed * 10 >> 5);
		Global.SetDParam(2, rvi.running_cost * ((int)Global._price.roadveh_running) >> 8);

		Global.SetDParam(4, rvi.capacity);
		Global.SetDParam(3, Global._cargoc.names_long[rvi.cargo_type] );

		Gfx.DrawStringMultiCenter(x, y, Str.STR_902A_COST_SPEED_RUNNING_COST, maxw);
	}

	static void DrawNewsNewRoadVehAvail(Window w)
	{
		//EngineID
		int engine;

		NewsItem.DrawNewsBorder(w);

		int width = w.getWidth();

		engine = w.as_news_d().ni.getString_id().id;
		Gfx.DrawStringMultiCenter(width >> 1, 20, Str.STR_9028_NEW_ROAD_VEHICLE_NOW_AVAILABLE, width - 2);
		Gfx.GfxFillRect(25, 56, width - 25, w.getHeight() - 2, 10);

		Global.SetDParam(0, GetCustomEngineName(engine).id);
		Gfx.DrawStringMultiCenter(width >> 1, 57, Str.STR_9029, width - 2);

		RoadVehCmd.DrawRoadVehEngine(width >> 1, 88, engine, 0);
		Gfx.GfxFillRect(25, 56, width - 56, 112, 0x323 | Sprite.USE_COLORTABLE);
		DrawRoadVehEngineInfo( engine, width >> 1, 129, width - 52);
	}

	static /*StringID*/ int GetNewsStringNewRoadVehAvail(final NewsItem ni)
	{
		//EngineID 
		int engine = ni.getString_id().id;
		Global.SetDParam(0, Str.STR_9028_NEW_ROAD_VEHICLE_NOW_AVAILABLE);
		Global.SetDParam(1, GetCustomEngineName(engine).id);
		return Str.STR_02B6;
	}

	static void DrawShipEngineInfo(int engine, int x, int y, int maxw)
	{
		final ShipVehicleInfo svi = ShipVehInfo(engine);
		Global.SetDParam(0, svi.base_cost * (((int)Global._price.ship_base) >> 3) >> 5);
		Global.SetDParam(1, svi.max_speed * 10 >> 5);
		Global.SetDParam(2, Global._cargoc.names_long[svi.cargo_type]);
		Global.SetDParam(3, svi.capacity);
		Global.SetDParam(4, svi.running_cost * ((int)Global._price.ship_running) >> 8);
		Gfx.DrawStringMultiCenter(x, y, Str.STR_982E_COST_MAX_SPEED_CAPACITY, maxw);
	}

	static void DrawNewsNewShipAvail(Window w)
	{
		//EngineID 
		int engine;

		NewsItem.DrawNewsBorder(w);
		int width = w.getWidth();

		engine = w.as_news_d().ni.getString_id().id;

		Gfx.DrawStringMultiCenter(width >> 1, 20, Str.STR_982C_NEW_SHIP_NOW_AVAILABLE, width - 2);
		Gfx.GfxFillRect(25, 56, width - 25, w.getHeight() - 2, 10);

		Global.SetDParam(0, GetCustomEngineName(engine).id);
		Gfx.DrawStringMultiCenter(width >> 1, 57, Str.STR_982D, width - 2);

		Ship.DrawShipEngine(width >> 1, 93, engine, 0);
		Gfx.GfxFillRect(25, 56, width - 56, 110, 0x323 | Sprite.USE_COLORTABLE);
		DrawShipEngineInfo( engine, width >> 1, 131, width - 52);
	}

	static int GetNewsStringNewShipAvail(final NewsItem ni)
	{
		//EngineID 
		int engine = ni.getString_id().id;
		Global.SetDParam(0, Str.STR_982C_NEW_SHIP_NOW_AVAILABLE);
		Global.SetDParam(1, GetCustomEngineName(engine).id);
		return Str.STR_02B6;
	}

	
	public static int GetCustomVehicleSprite(Vehicle v, int direction) 
	{
		return GetCustomEngineSprite(v.engine_type, v, direction);
	}
	
	public static int GetCustomVehicleIcon(int et, int direction) 
	{
		return GetCustomEngineSprite( EngineID.get(et), null, direction);
	}
	
	public static void loadGame(ObjectInputStream oin)
	{
		//Global.gs._engines = (Engine[]) oin.readObject();
		Airport.AdjustAvailAircraft();
	}

	public static void saveGame(ObjectOutputStream oos) 
	{
		//oos.writeObject(Global.gs._engines);		
	}

	public int getIntro_date() {
		return intro_date;
	}

	public int getLifelength() {
		return lifelength;
	}

	public int getReliability() {
		return reliability;
	}

	public int getRailtype() {
		return railtype;
	}

	public boolean isAvailableTo(int playerId)
	{
		return BitOps.HASBIT(player_avail, playerId);
	}

	public boolean isAvailableTo(PlayerID p)
	{
		return isAvailableTo(p.id);
	}
	
	public boolean isAvailableToMe()
	{
		return isAvailableTo(Global.gs._local_player.id);
	}
	
	public void setAvailableTo(int playerId)
	{
		player_avail = BitOps.RETSETBIT(player_avail, playerId);
	}

	public boolean flagIsAvailable() {
		return 0 != (flags & 1);
	}

	public int getType() {
		return type;
	}
	
}




@FunctionalInterface
interface resolve_callback 
{
	SpriteGroup apply
		(
				SpriteGroup spritegroup,
				Vehicle veh, int callback_info, 
				resolve_callback resolve_func
	);
}





