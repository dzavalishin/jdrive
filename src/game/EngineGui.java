package game;

import java.util.Iterator;

import game.tables.EngineTables;
import game.util.BitOps;

public class EngineGui extends EngineTables 
{





	static EngineInfo 			[] _engine_info = new EngineInfo[Global.TOTAL_NUM_ENGINES];
	static RailVehicleInfo 		[] _rail_vehicle_info = new RailVehicleInfo[Global.NUM_TRAIN_ENGINES];
	static ShipVehicleInfo 		[] _ship_vehicle_info = new ShipVehicleInfo[Global.NUM_SHIP_ENGINES];
	static AircraftVehicleInfo 	[] _aircraft_vehicle_info = new AircraftVehicleInfo[Global.NUM_AIRCRAFT_ENGINES];
	static RoadVehicleInfo 		[] _road_vehicle_info = new RoadVehicleInfo [Global.NUM_ROAD_ENGINES];

	
	
	static  final RailVehicleInfo  RailVehInfo(int e)
	{
		assert(e < _rail_vehicle_info.length);
		return _rail_vehicle_info[e];
	}

	static  final ShipVehicleInfo  ShipVehInfo(int e ) //EngineID e)
	{
		assert(e >= Global.SHIP_ENGINES_INDEX && e < Global.SHIP_ENGINES_INDEX + _ship_vehicle_info.length);
		return _ship_vehicle_info[e - Global.SHIP_ENGINES_INDEX];
	}

	static  final AircraftVehicleInfo  AircraftVehInfo(int e)
	{
		assert(e >= Global.AIRCRAFT_ENGINES_INDEX && e < Global.AIRCRAFT_ENGINES_INDEX + _aircraft_vehicle_info.length);
		return _aircraft_vehicle_info[e - Global.AIRCRAFT_ENGINES_INDEX];
	}

	static  final RoadVehicleInfo  RoadVehInfo(int e)
	{
		assert(e >= Global.ROAD_ENGINES_INDEX && e < Global.ROAD_ENGINES_INDEX + _road_vehicle_info.length);
		return _road_vehicle_info[e - Global.ROAD_ENGINES_INDEX];
	}







	//void ShowEnginePreviewWindow(EngineID engine);

	static void DeleteCustomEngineNames()
	{
		int i;
		StringID old;

		for (i = 0; i != Global.TOTAL_NUM_ENGINES; i++) {
			old = _engine_name_strings[i];
			_engine_name_strings[i] = i + Str.STR_8000_KIRBY_PAUL_TANK_STEAM;
			DeleteName(old);
		}

		_vehicle_design_names &= ~1;
	}

	static void LoadCustomEngineNames()
	{
		// XXX: not done */
		DEBUG(misc, 1) ("LoadCustomEngineNames: not done");
	}

	static void SetupEngineNames()
	{
		//for (name = _engine_name_strings; name != endof(_engine_name_strings); name++)
		for (int i = 0; i < _engine_name_strings.length; i++)
			_engine_name_strings[i] = Str.STR_SV_EMPTY;

		DeleteCustomEngineNames();
		LoadCustomEngineNames();
	}

	static void AdjustAvailAircraft()
	{
		int date = _date;
		byte avail = 0;
		if (date >= 12784) avail |= 2; // big airport
		if (date < 14610 || Global._patches.always_small_airport) avail |= 1;  // small airport
		if (date >= 15706) avail |= 4; // enable heliport

		if (avail != _avail_aircraft) {
			_avail_aircraft = avail;
			Window.InvalidateWindow(Window.WC_BUILD_STATION, 0);
		}
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
			e.reliability = (int)age * (int)(e.reliability_final - max) / e.duration_phase_3 + max;
		} else {
			// time's up for this engine
			// make it either available to all players (if never_expire_vehicles is enabled and if it was available earlier)
			// or disable this engine completely
			e.player_avail = (Global._patches.never_expire_vehicles && e.player_avail)? -1 : 0;
			e.reliability = e.reliability_final;
		}
	}

	static void AddTypeToEngines()
	{
		Engine  e = _engines;

		do e.type = Vehicle.VEH_Train;    while (++e < &_engines[ROAD_ENGINES_INDEX]);
		do e.type = Vehicle.VEH_Road;     while (++e < &_engines[SHIP_ENGINES_INDEX]);
		do e.type = Vehicle.VEH_Ship;     while (++e < &_engines[AIRCRAFT_ENGINES_INDEX]);
		do e.type = Vehicle.VEH_Aircraft; while (++e < &_engines[Global.TOTAL_NUM_ENGINES]);
		do e.type = Vehicle.VEH_Special;  while (++e < endof(_engines));
	}

	static void StartupEngines()
	{
		Engine e;
		final EngineInfo *ei;

		SetupEngineNames();

		for (e = _engines, ei = _engine_info; e != endof(_engines); e++, ei++) {
			int r;

			e.age = 0;
			e.railtype = ei.railtype;
			e.flags = 0;
			e.player_avail = 0;

			r = Hal.Random();
			e.intro_date = BitOps.GB(r, 0, 9) + ei.base_intro;
			if (e.intro_date <= _date) {
				e.age = (_date - e.intro_date) >> 5;
				e.player_avail = (byte)-1;
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
			if (ei.unk2 & 0x80) {
				e.age = 0xFFFF;
			} else {
				CalcEngineReliability(e);
			}

			e.lifelength = ei.lifelength + Global._patches.extend_vehicle_life;

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

		AdjustAvailAircraft();
	}

	// TODO: We don't support cargo-specific wagon overrides. Pretty exotic... ;-) --pasky

	static class WagonOverride {
		byte [] train_id;
		int trains;
		SpriteGroup group;
	}

	static class WagonOverrides {
		int overrides_count;
		WagonOverride [] overrides;
	}

	static WagonOverrides[] _engine_wagon_overrides = new WagonOverrides[Global.TOTAL_NUM_ENGINES];

	static void SetWagonOverrideSprites(EngineID engine, SpriteGroup group, byte [] train_id,
			int trains)
	{
		WagonOverrides wos;
		WagonOverride wo;

		wos = _engine_wagon_overrides[engine];
		wos.overrides_count++;
		//wos.overrides = realloc(wos.overrides,				wos.overrides_count * sizeof(*wos.overrides));
		//wos.overrides = new WagonOverride[wos.overrides_count];
		WagonOverride [] newo = = new WagonOverride[wos.overrides_count];
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

	static final SpriteGroup GetWagonOverrideSpriteSet(EngineID engine, byte overriding_engine)
	{
		final WagonOverrides wos = _engine_wagon_overrides[engine];
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
	static void UnloadWagonOverrides()
	{
		WagonOverrides wos;
		WagonOverride wo;
		EngineID engine;
		int i;

		for (engine = 0; engine < Global.TOTAL_NUM_ENGINES; engine++) {
			wos = _engine_wagon_overrides[engine];
			for (i = 0; i < wos.overrides_count; i++) {
				wo = wos.overrides[i];
				UnloadSpriteGroup(wo.group);
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
	static SpriteGroup [][] engine_custom_sprites = new SpriteGroup[Global.TOTAL_NUM_ENGINES][NUM_GLOBAL_CID];

	static void SetCustomEngineSprites(EngineID engine, byte cargo, SpriteGroup group)
	{
		if (engine_custom_sprites[engine][cargo] != null) {
			Global.DEBUG_grf( 6,"SetCustomEngineSprites: engine `%d' cargo `%d' already has group -- removing.", engine, cargo);
			UnloadSpriteGroup(&engine_custom_sprites[engine][cargo]);
		}
		engine_custom_sprites[engine][cargo] = group;
		group.ref_count++;
	}

	/**
	 * Unload all engine sprite groups.
	 */
	static void UnloadCustomEngineSprites()
	{
		EngineID engine;
		CargoID cargo;

		for (engine = 0; engine < Global.TOTAL_NUM_ENGINES; engine++) {
			for (cargo = 0; cargo < NUM_GLOBAL_CID; cargo++) {
				if (engine_custom_sprites[engine][cargo] != null) {
					Global.DEBUG_grf( 6,"UnloadCustomEngineSprites: Unloading group for engine `%d' cargo `%d'.", engine, cargo);
					UnloadSpriteGroup(&engine_custom_sprites[engine][cargo]);
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
			final Vehicle veh, int callback_info, void *resolve_func); /* XXX data pointer used as function pointer */

	static final SpriteGroup ResolveVehicleSpriteGroup(
			final SpriteGroup spritegroup,
			final Vehicle veh, int callback_info, 
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
				value = GetDeterministicSpriteValue(dsg.variable);
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

				if (dsg.var_scope == VSG_SCOPE_PARENT) {
					/* First engine in the vehicle chain */
					if (veh.type == Vehicle.VEH_Train)
						veh = GetFirstVehicleInChain(veh);
				}

				if (dsg.variable == 0x40 || dsg.variable == 0x41) {
					if (veh.type == Vehicle.VEH_Train) {
						final Vehicle u = GetFirstVehicleInChain(veh);
						byte chain_before = 0, chain_after = 0;

						while (u != veh) {
							chain_before++;
							if (dsg.variable == 0x41 && u.engine_type != veh.engine_type)
								chain_before = 0;
							u = u.next;
						}
						while (u.next != null && (dsg.variable == 0x40 || u.next.engine_type == veh.engine_type)) {
							chain_after++;
							u = u.next;
						};

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
					case 0x0A: value =  PackOrder(veh.current_order); break;
					case 0x0B: value =  PackOrder(veh.current_order) & 0xff; break;
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
					case 0x18: value =  veh.max_speed; break;
					case 0x19: value =  veh.max_speed & 0xFF; break;
					case 0x1F: value =  veh.direction; break;
					case 0x28: value =  veh.cur_image; break;
					case 0x29: value =  veh.cur_image & 0xFF; break;
					case 0x32: value =  veh.vehstatus; break;
					case 0x33: value =  veh.vehstatus; break;
					case 0x34: value =  veh.cur_speed; break;
					case 0x35: value =  veh.cur_speed & 0xFF; break;
					case 0x36: value =  veh.subspeed; break;
					case 0x37: value =  veh.acceleration; break;
					case 0x39: value =  veh.cargo_type; break;
					case 0x3A: value =  veh.cargo_cap; break;
					case 0x3B: value =  veh.cargo_cap & 0xFF; break;
					case 0x3C: value =  veh.cargo_count; break;
					case 0x3D: value =  veh.cargo_count & 0xFF; break;
					case 0x3E: value =  veh.cargo_source; break; // Probably useless; so what
					case 0x3F: value =  veh.cargo_days; break;
					case 0x40: value =  veh.age; break;
					case 0x41: value =  veh.age & 0xFF; break;
					case 0x42: value =  veh.max_age; break;
					case 0x43: value =  veh.max_age & 0xFF; break;
					case 0x44: value =  veh.build_year; break;
					case 0x45: value =  veh.unitnumber; break;
					case 0x46: value =  veh.engine_type; break;
					case 0x47: value =  veh.engine_type & 0xFF; break;
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
					case 0x5A: value =  veh.next == null ? INVALID_VEHICLE : veh.next.index; break;
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

			target = value != -1 ? EvalDeterministicSpriteGroup(dsg, value) : dsg.default_group;
			//debug("Resolved variable %x: %d, %p", dsg.variable, value, callback);
			return resolve_func(target, veh, callback_info, resolve_func);
		}

		case SGT_RANDOMIZED: {
			final RandomizedSpriteGroup rsg = (RandomizedSpriteGroup)spritegroup;

			if (veh == null) {
				// Purchase list of something. Show the first one. 
				assert(rsg.num_groups > 0);
				//debug("going for %p: %d", rsg.groups[0], rsg.groups[0].type);
				return resolve_func.apply(rsg.groups[0], null, callback_info, resolve_func);
			}

			if (rsg.var_scope == VSG_SCOPE_PARENT) {
				// First engine in the vehicle chain 
				if (veh.type == Vehicle.VEH_Train)
					veh = GetFirstVehicleInChain(veh);
			}

			return resolve_func.apply(EvalRandomizedSpriteGroup(rsg, veh.random_bits), veh, callback_info, resolve_func);
		}

		default:
			Global.error("I don't know how to handle such a spritegroup %d!", spritegroup.type);
			return null;
		}
	}

	static final SpriteGroup GetVehicleSpriteGroup(EngineID engine, final Vehicle v)
	{
		final SpriteGroup group;
		byte cargo = GC_PURCHASE;

		if (v != null) {
			cargo = _global_cargo_id[GameOptions._opt.landscape][v.cargo_type];
			assert(cargo != GC_INVALID);
		}

		group = engine_custom_sprites[engine][cargo];

		if (v != null && v.type == Vehicle.VEH_Train) {
			final SpriteGroup overset = GetWagonOverrideSpriteSet(engine, v.u.rail.first_engine);

			if (overset != null) group = overset;
		}

		return group;
	}

	static int GetCustomEngineSprite(EngineID engine, final Vehicle v, byte direction)
	{
		final SpriteGroup group;
		final RealSpriteGroup rsg;
		byte cargo = GC_PURCHASE;
		byte loaded = 0;
		boolean in_motion = 0;
		int totalsets, spriteset;
		int r;

		if (v != null) {
			int capacity = v.cargo_cap;

			cargo = _global_cargo_id[GameOptions._opt.landscape][v.cargo_type];
			assert(cargo != GC_INVALID);

			if (capacity == 0) capacity = 1;
			loaded = (v.cargo_count * 100) / capacity;
			in_motion = (v.cur_speed != 0);
		}

		group = GetVehicleSpriteGroup(engine, v);
		group = ResolveVehicleSpriteGroup(group, v, 0, (resolve_callback) ResolveVehicleSpriteGroup);

		if (group == null && cargo != GC_DEFAULT) {
			// This group is empty but perhaps there'll be a default one.
			group = ResolveVehicleSpriteGroup(engine_custom_sprites[engine][GC_DEFAULT], v, 0,
					(resolve_callback) ResolveVehicleSpriteGroup);
		}

		if (group == null)
			return 0;

		assert(group.type == SGT_REAL);
		rsg = &group.g.real;

		if (!rsg.sprites_per_set) {
			// This group is empty. This function users should therefore
			// look up the sprite number in _engine_original_sprites.
			return 0;
		}

		assert(rsg.sprites_per_set <= 8);
		direction %= rsg.sprites_per_set;

		totalsets = in_motion ? rsg.loaded_count : rsg.loading_count;

		// My aim here is to make it possible to visually determine absolutely
		// empty and totally full vehicles. --pasky
		if (loaded == 100 || totalsets == 1) { // full
			spriteset = totalsets - 1;
		} else if (loaded == 0 || totalsets == 2) { // empty
			spriteset = 0;
		} else { // something inbetween
			spriteset = loaded * (totalsets - 2) / 100 + 1;
			// correct possible rounding errors
			if (!spriteset)
				spriteset = 1;
			else if (spriteset == totalsets - 1)
				spriteset--;
		}

		r = (in_motion ? rsg.loaded[spriteset].g.result.result : rsg.loading[spriteset].g.result.result) + direction;
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
		return GetWagonOverrideSpriteSet(v.engine_type, v.u.rail.first_engine) != null;
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
		final SpriteGroup *group;
		byte cargo = GC_DEFAULT;

		if (v != null)
			cargo = _global_cargo_id[GameOptions._opt.landscape][v.cargo_type];

		group = engine_custom_sprites[engine][cargo];

		if (v != null && v.type == Vehicle.VEH_Train) {
			final SpriteGroup *overset = GetWagonOverrideSpriteSet(engine, v.u.rail.first_engine);

			if (overset != null) group = overset;
		}

		group = ResolveVehicleSpriteGroup(group, v, callback_info, (resolve_callback) ResolveVehicleSpriteGroup);

		if (group == null && cargo != GC_DEFAULT) {
			// This group is empty but perhaps there'll be a default one.
			group = ResolveVehicleSpriteGroup(engine_custom_sprites[engine][GC_DEFAULT], v, callback_info,
					(resolve_callback) ResolveVehicleSpriteGroup);
		}

		if (group == null || group.type != SGT_CALLBACK)
			return CALLBACK_FAILED;

		return group.g.callback.result;
	}



	// Global variables are evil, yes, but we would end up with horribly overblown
	// calling convention otherwise and this should be 100% reentrant.
	static byte _vsg_random_triggers;
	static byte _vsg_bits_to_reseed;

	static final SpriteGroup TriggerVehicleSpriteGroup(final SpriteGroup spritegroup,
			Vehicle veh, int callback_info, resolve_callback resolve_func)
	{
		if (spritegroup == null)
			return null;

		if (spritegroup.type == SGT_RANDOMIZED) {
			_vsg_bits_to_reseed |= RandomizedSpriteGroupTriggeredBits(
					spritegroup.g.random,
					_vsg_random_triggers,
					veh.waiting_triggers
					);
		}

		return ResolveVehicleSpriteGroup(spritegroup, veh, callback_info, resolve_func);
	}

	static void DoTriggerVehicle(Vehicle veh, VehicleTrigger trigger, byte base_random_bits, boolean first)
	{
		final SpriteGroup group;
		final RealSpriteGroup rsg;
		byte new_random_bits;

		_vsg_random_triggers = trigger;
		_vsg_bits_to_reseed = 0;
		group = TriggerVehicleSpriteGroup(GetVehicleSpriteGroup(veh.engine_type, veh), veh, 0,
				(resolve_callback) TriggerVehicleSpriteGroup);

		if (group == null && veh.cargo_type != GC_DEFAULT) {
			// This group turned out to be empty but perhaps there'll be a default one.
			group = TriggerVehicleSpriteGroup(engine_custom_sprites[veh.engine_type][GC_DEFAULT], veh, 0,
					(resolve_callback) TriggerVehicleSpriteGroup);
		}

		if (group == null)
			return;

		assert(group.type == SGT_REAL);
		rsg = (RealSpriteGroup)group;

		new_random_bits = Hal.Random();
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
			DoTriggerVehicle(GetFirstVehicleInChain(veh), VEHICLE_TRIGGER_ANY_NEW_CARGO, new_random_bits, false);
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

	void TriggerVehicle(Vehicle veh, VehicleTrigger trigger)
	{
		if (trigger == VEHICLE_TRIGGER_DEPOT) {
			// store that the vehicle entered a depot this tick
			VehicleEnteredDepotThisTick(veh);
		}

		DoTriggerVehicle(veh, trigger, 0, true);
	}

	static String[] _engine_custom_names = new String[Global.TOTAL_NUM_ENGINES];

	void SetCustomEngineName(EngineID engine, final char *name)
	{
		_engine_custom_names[engine] = strdup(name);
	}

	void UnloadCustomEngineNames()
	{
		//char **i;
		for (int i = 0; i < _engine_custom_names.length; i++) {
			//free(*i);
			_engine_custom_names[i] = null;
		}
	}

	//StringID GetCustomEngineName(EngineID engine)
	StringID GetCustomEngineName(int engine)
	{
		if (null == _engine_custom_names[engine])
			return _engine_name_strings[engine];
		//ttd_strlcpy(_userstring, _engine_custom_names[engine], lengthof(_userstring));
		_userstring = _engine_custom_names[engine];
		return Str.STR_SPEC_USERSTRING;
	}


	void AcceptEnginePreview(Engine e, PlayerID player)
	{
		Player p = Player.GetPlayer(player);

		assert(e.railtype < RAILTYPE_END);
		e.player_avail = BitOps.RETSETBIT(e.player_avail, player);
		p.avail_railtypes = BitOps.RETSETBIT(p.avail_railtypes, e.railtype);

		e.preview_player = 0xFF;
		Window.InvalidateWindowClasses(Window.WC_BUILD_VEHICLE);
		Window.InvalidateWindowClasses(Window.WC_REPLACE_VEHICLE);
	}

	//static PlayerID GetBestPlayer(PlayerID pp)
	static int GetBestPlayer(int pp)
	{
		//final Player p;
		int best_hist;
		//PlayerID 
		int best_player;
		int mask = 0;

		do {
			best_hist = -1;
			best_player = Owner.OWNER_SPECTATOR;
			//FOR_ALL_PLAYERS(p)
			Player.forEach( (p) ->
			{
				if (p.is_active && p.block_preview == 0 && !BitOps.HASBIT(mask, p.index) &&
						p.old_economy[0].performance_history > best_hist) {
					best_hist = p.old_economy[0].performance_history;
					best_player = p.index.id;
				}
			});

			if (best_player == Owner.OWNER_SPECTATOR) return Owner.OWNER_SPECTATOR;

			mask = BitOps.RETSETBIT(mask, best_player);
		} while (--pp != 0);

		return best_player;
	}

	void EnginesDailyLoop()
	{
		EngineID i;

		if (Global._cur_year >= 130) return;

		for (i = 0; i != _engines.length; i++) {
			Engine  e = _engines[i];

			if (e.flags & ENGINE_INTRODUCING) {
				if (e.flags & ENGINE_PREVIEWING) {
					if (e.preview_player != 0xFF && !--e.preview_wait) {
						e.flags &= ~ENGINE_PREVIEWING;
						Window.DeleteWindowById(Window.WC_ENGINE_PREVIEW, i);
						e.preview_player++;
					}
				} else if (e.preview_player != 0xFF) {
					PlayerID best_player = GetBestPlayer(e.preview_player);

					if (best_player == Owner.OWNER_SPECTATOR) {
						e.preview_player = 0xFF;
						continue;
					}

					if (!IS_HUMAN_PLAYER(best_player)) {
						/* XXX - TTDBUG: TTD has a bug here ???? */
						AcceptEnginePreview(e, best_player);
					} else {
						e.flags |= ENGINE_PREVIEWING;
						e.preview_wait = 20;
						if (IS_INTERACTIVE_PLAYER(best_player)) ShowEnginePreviewWindow(i);
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
	int CmdWantEnginePreview(int x, int y, int flags, int p1, int p2)
	{
		Engine e;
		if (!Engine.IsEngineIndex(p1)) return Cmd.CMD_ERROR;

		e = Engine.GetEngine(p1);
		if (GetBestPlayer(e.preview_player) != Global._current_player.id) return Cmd.CMD_ERROR;

		if( 0 != (flags & Cmd.DC_EXEC))
			AcceptEnginePreview(e, Global._current_player);

		return 0;
	}

	// Determine if an engine type is a wagon (and not a loco)
	static boolean IsWagon(EngineID index)
	{
		return (index.id < Global.NUM_TRAIN_ENGINES) && 0 != (RailVehInfo(index.id).flags & RVI_WAGON);
	}

	static boolean IsWagon(int index)
	{
		return (index < Global.NUM_TRAIN_ENGINES) && 0 != (RailVehInfo(index).flags & RVI_WAGON);
	}

	static void NewVehicleAvailable(Engine e)
	{
		//Vehicle v;
		//Player p;
		EngineID index = e - _engines;

		// In case the player didn't build the vehicle during the intro period,
		// prevent that player from getting future intro periods for a while.
		if (e.flags & ENGINE_INTRODUCING) {
			//FOR_ALL_PLAYERS(p)
			Player.forEach( (p) ->
			{
				int block_preview = p.block_preview;

				if (!BitOps.HASBIT(e.player_avail, p.index)) continue;

				/* We assume the user did NOT build it.. prove me wrong ;) */
				p.block_preview = 20;

				//FOR_ALL_VEHICLES(v)
				Iterator<Vehicle> it = Vehicle.getIterator();
				while(it.hasNext())
				{
					Vehicle v = it.next();
					if (v.type == Vehicle.VEH_Train || v.type == Vehicle.VEH_Road || v.type == Vehicle.VEH_Ship ||
							(v.type == Vehicle.VEH_Aircraft && v.subtype <= 2)) {
						if (v.owner == p.index && v.engine_type == index) {
							/* The user did prove me wrong, so restore old value */
							p.block_preview = (byte) block_preview;
							break;
						}
					}
				}
			});
		}

		e.flags = (e.flags & ~ENGINE_INTRODUCING) | ENGINE_AVAILABLE;
		Window.InvalidateWindowClasses(Window.WC_BUILD_VEHICLE);
		Window.InvalidateWindowClasses(Window.WC_REPLACE_VEHICLE);

		// Now available for all players
		e.player_avail = (byte)-1;

		// Do not introduce new rail wagons
		if (IsWagon(index)) return;

		// make maglev / monorail available
		//FOR_ALL_PLAYERS(p)
		Player.forEach( (p) ->
		{
			if (p.is_active) {
				assert(e.railtype < RAILTYPE_END);
				p.avail_railtypes = BitOps.RETSETBIT(p.avail_railtypes, e.railtype);
			}
		});

		if (index < Global.NUM_TRAIN_ENGINES) {
			AddNewsItem(index, NEWS_FLAGS(NM_CALLBACK, 0, NT_NEW_VEHICLES, DNC_TRAINAVAIL), 0, 0);
		} else if (index < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES) {
			AddNewsItem(index, NEWS_FLAGS(NM_CALLBACK, 0, NT_NEW_VEHICLES, DNC_ROADAVAIL), 0, 0);
		} else if (index < Global.NUM_TRAIN_ENGINES + Global.NUM_ROAD_ENGINES + Global.NUM_SHIP_ENGINES) {
			AddNewsItem(index, NEWS_FLAGS(NM_CALLBACK, 0, NT_NEW_VEHICLES, DNC_SHIPAVAIL), 0, 0);
		} else {
			AddNewsItem(index, NEWS_FLAGS(NM_CALLBACK, 0, NT_NEW_VEHICLES, DNC_AIRCRAFTAVAIL), 0, 0);
		}
	}

	void EnginesMonthlyLoop()
	{
		Engine e;

		if (_cur_year < 130) {
			for (e = _engines; e != endof(_engines); e++) {
				// Age the vehicle
				if (e.flags & ENGINE_AVAILABLE && e.age != 0xFFFF) {
					e.age++;
					CalcEngineReliability(e);
				}

				if (!(e.flags & ENGINE_AVAILABLE) && (int)(_date - Math.min(_date, 365)) >= e.intro_date) {
					// Introduce it to all players
					NewVehicleAvailable(e);
				} else if (!(e.flags & (ENGINE_AVAILABLE|ENGINE_INTRODUCING)) && _date >= e.intro_date) {
					// Introduction date has passed.. show introducing dialog to one player.
					e.flags |= ENGINE_INTRODUCING;

					// Do not introduce new rail wagons
					if (!IsWagon(e - _engines))
						e.preview_player = 1; // Give to the player with the highest rating.
				}
			}
		}
		AdjustAvailAircraft();
	}

	/** Rename an engine.
	 * @param x,y unused
	 * @param p1 engine ID to rename
	 * @param p2 unused
	 */
	int CmdRenameEngine(int x, int y, int flags, int p1, int p2)
	{
		StringID str;

		if (!IsEngineIndex(p1) || Global._cmd_text[0] == '\0') return Cmd.CMD_ERROR;

		str = AllocateNameUnique(Global._cmd_text, 0);
		if (str == 0) return Cmd.CMD_ERROR;

		if (flags & Cmd.DC_EXEC) {
			StringID old_str = _engine_name_strings[p1];
			_engine_name_strings[p1] = str;
			DeleteName(old_str);
			_vehicle_design_names |= 3;
			MarkWholeScreenDirty();
		} else {
			DeleteName(str);
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
	boolean IsEngineBuildable(int engine, byte type)
	{
		final Engine e;

		// check if it's an engine that is in the engine array
		if (!Engine.IsEngineIndex(engine)) return false;

		e = Engine.GetEngine(engine);

		// check if it's an engine of specified type
		if (e.type != type) return false;

		// check if it's available
		if (!BitOps.HASBIT(e.player_avail, Global._current_player.id)) return false;

		return true;
	}




}

/*
typedef SpriteGroup *(*resolve_callback)
	(final SpriteGroup *spritegroup,
	final Vehicle veh, int callback_info, 
	void *resolve_func); /* XXX data pointer used as function pointer */


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



