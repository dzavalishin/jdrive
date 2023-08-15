package com.dzavalishin.game;

import java.util.Iterator;
import java.util.function.Consumer;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.struct.GetNewVehiclePosResult;
import com.dzavalishin.struct.TileIndexDiff;
import com.dzavalishin.tables.DisasterTables;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Sound;

public class DisasterCmd extends DisasterTables 
{

	static void DisasterClearSquare(TileIndex tile)
	{
		if (!tile.EnsureNoVehicle()) return;

		switch (tile.GetTileType()) {
		case MP_RAILWAY:
			if (tile.GetTileOwner().IS_HUMAN_PLAYER() && !tile.IsRailWaypoint()) 
				Landscape.DoClearSquare(tile);
			break;

		case MP_HOUSE: {
			PlayerID p = PlayerID.getCurrent();
			PlayerID.setCurrentToNone();
			Cmd.DoCommandByTile(tile, 0, 0, Cmd.DC_EXEC, Cmd.CMD_LANDSCAPE_CLEAR);
			PlayerID.setCurrent(p);
			break;
		}

		case MP_TREES:
		case MP_CLEAR:
			Landscape.DoClearSquare(tile);
			break;

		default:
			break;
		}
	}



	static void DisasterVehicleUpdateImage(Vehicle v)
	{
		int img = v.disaster.image_override;
		if (img == 0)
			img = _disaster_images[v.subtype][v.direction];
		v.cur_image = img;
	}

	static void InitializeDisasterVehicle(Vehicle v, int x, int y, int z, int direction, int subtype)
	{
		v.type = Vehicle.VEH_Disaster;
		v.x_pos = x;
		v.y_pos = y;
		v.z_pos = z;
		v.tile = TileIndex.TileVirtXY(x, y);
		v.direction = direction;
		v.subtype = subtype;
		v.x_offs = -1;
		v.y_offs = -1;
		v.sprite_width = 2;
		v.sprite_height = 2;
		v.z_height = 5;
		v.owner = PlayerID.getNone();
		v.assignStatus( Vehicle.VS_UNCLICKABLE );
		
		v.disaster = new VehicleDisaster();
		v.disaster.image_override = 0;
		
		v.getCurrent_order().type = Order.OT_NOTHING;
		v.getCurrent_order().flags = 0;
		v.getCurrent_order().station = 0;

		DisasterVehicleUpdateImage(v);
		v.VehiclePositionChanged();
		v.BeginVehicleMove();
		v.EndVehicleMove();
	}

	static void DeleteDisasterVeh(Vehicle v)
	{
		v.DeleteVehicleChain();
	}

	static void SetDisasterVehiclePos(Vehicle v, int x, int y, int z)
	{
		Vehicle u;
		int yt;

		v.BeginVehicleMove();
		v.x_pos = x;
		v.y_pos = y;
		v.z_pos = z;
		v.tile = TileIndex.TileVirtXY(x, y);

		DisasterVehicleUpdateImage(v);
		v.VehiclePositionChanged();
		v.EndVehicleMove();

		if ( (u=v.next) != null) {
			u.BeginVehicleMove();

			u.x_pos = x;
			u.y_pos = yt = y - 1 - (Math.max(z - Landscape.GetSlopeZ(x, y-1), 0) >> 3);
			u.z_pos = Landscape.GetSlopeZ(x,yt);
			u.direction = v.direction;

			DisasterVehicleUpdateImage(u);
			u.VehiclePositionChanged();
			u.EndVehicleMove();

			if ( (u=u.next) != null) {
				u.BeginVehicleMove();
				u.x_pos = x;
				u.y_pos = y;
				u.z_pos = z + 5;
				u.VehiclePositionChanged();
				u.EndVehicleMove();
			}
		}
	}


	static void DisasterTick_Zeppeliner(Vehicle v)
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();
		Station st;
		int x,y;
		int z;
		TileIndex tile;

		++v.tick_counter;

		if (v.getCurrent_order().station < 2) {
			if(0 != (v.tick_counter&1) )
				return;

			v.GetNewVehiclePos(gp);

			SetDisasterVehiclePos(v, gp.x, gp.y, v.z_pos);

			if (v.getCurrent_order().station == 1) {
				if (++v.age == 38) {
					v.getCurrent_order().station = 2;
					v.age = 0;
				}

				if ((v.tick_counter&7)==0) {
					v.CreateEffectVehicleRel(0, -17, 2, Vehicle.EV_SMOKE);
				}
			} else if (v.getCurrent_order().station == 0) {
				tile = v.tile; /**/

				if (tile.IsValidTile() &&
						tile.IsTileType( TileTypes.MP_STATION) &&
						BitOps.IS_INT_INSIDE(tile.getMap().m5, 8, 0x43) &&
						tile.GetTileOwner().IS_HUMAN_PLAYER()) {
					v.getCurrent_order().station = 1;
					v.age = 0;

					Global.SetDParam(0, tile.getMap().m2);
					NewsItem.AddNewsItem(Str.STR_B000_ZEPPELIN_DISASTER_AT,
							NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ACCIDENT, 0),
							v.index,
							0);
				}
			}
			if (v.getY_pos() >= (Global.MapSizeY() + 9) * 16 - 1)
				DeleteDisasterVeh(v);
			return;
		}

		if (v.getCurrent_order().station > 2) {
			if (++v.age <= 13320)
				return;

			tile = v.tile; /**/

			if (tile.IsValidTile() &&
					tile.IsTileType( TileTypes.MP_STATION) &&
					BitOps.IS_INT_INSIDE(tile.getMap().m5, 8, 0x43) &&
					tile.GetTileOwner().IS_HUMAN_PLAYER()) {
				st = Station.GetStation(tile.getMap().m2);
				//st.airport_flags = BitOps.RETCLRBITS(st.airport_flags, Airport.RUNWAY_IN_block);
				st.resetAirportBlocks(Airport.RUNWAY_IN_block);
			}

			SetDisasterVehiclePos(v, v.getX_pos(), v.getY_pos(), v.z_pos);
			DeleteDisasterVeh(v);
			return;
		}

		x = v.getX_pos();
		y = v.getY_pos();
		z = Landscape.GetSlopeZ(x,y);
		if (z < v.z_pos)
			z = v.z_pos - 1;
		SetDisasterVehiclePos(v, x, y, z);

		if (++v.age == 1) {
			v.CreateEffectVehicleRel(0, 7, 8, Vehicle.EV_EXPLOSION_LARGE);
			v.SndPlayVehicleFx(Snd.SND_12_EXPLOSION);
			v.disaster.image_override = Sprite.SPR_BLIMP_CRASHING;
		} else if (v.age == 70) {
			v.disaster.image_override = Sprite.SPR_BLIMP_CRASHED;
		} else if (v.age <= 300) {
			if (0==(v.tick_counter&7)) {
				int r = Hal.Random();

				v.CreateEffectVehicleRel(
						BitOps.GB(r, 0, 4) - 7,
						BitOps.GB(r, 4, 4) - 7,
						BitOps.GB(r, 8, 3) + 5,
						Vehicle.EV_EXPLOSION_SMALL);
			}
		} else if (v.age == 350) {
			v.getCurrent_order().station = 3;
			v.age = 0;
		}

		tile = v.tile;/**/
		if (tile.IsValidTile() &&
				tile.IsTileType( TileTypes.MP_STATION) &&
				BitOps.IS_INT_INSIDE(tile.getMap().m5, 8, 0x43) &&
				tile.GetTileOwner().IS_HUMAN_PLAYER()) {

			st = Station.GetStation(tile.getMap().m2);
			//st.airport_flags = BitOps.RETSETBITS(st.airport_flags, Airport.RUNWAY_IN_block);
			st.setAirportBlocks(Airport.RUNWAY_IN_block);
		}
	}

	// UFO starts in the middle, and flies around a bit until it locates
	// a road vehicle which it targets.
	static void DisasterTick_UFO(Vehicle v)
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();

		int dist;
		int z;

		v.disaster.image_override = 0 != (++v.tick_counter & 8) ? Sprite.SPR_UFO_SMALL_SCOUT_DARKER : Sprite.SPR_UFO_SMALL_SCOUT;

		if (v.getCurrent_order().station == 0) {
			// fly around randomly
			int x = v.dest_tile.TileX() * 16;
			int y = v.dest_tile.TileY() * 16;
			if (Math.abs(x - v.getX_pos()) + Math.abs(y - v.getY_pos()) >= 16) {
				v.direction = Vehicle.GetDirectionTowards(v, x, y);
				v.GetNewVehiclePos( gp);
				SetDisasterVehiclePos(v, gp.x, gp.y, v.z_pos);
				return;
			}
			if (++v.age < 6) {
				v.dest_tile = Hal.RandomTile();
				return;
			}
			v.getCurrent_order().station = 1;


			//FOR_ALL_VEHICLES(u)
			Iterator<Vehicle> ii = Vehicle.getIterator();
			while(ii.hasNext())
			{
				Vehicle u = ii.next();
				if (u.type == Vehicle.VEH_Road && u.owner.IS_HUMAN_PLAYER()) {
					v.dest_tile = TileIndex.get( u.index );
					v.age = 0;
					return;
				}
			}

			DeleteDisasterVeh(v);
		} else {
			// target a vehicle
			Vehicle u = Vehicle.GetVehicle(v.dest_tile.tile);
			if (u.type != Vehicle.VEH_Road) {
				DeleteDisasterVeh(v);
				return;
			}

			dist = Math.abs(v.getX_pos() - u.getX_pos()) + Math.abs(v.getY_pos() - u.getY_pos());

			if (dist < 16 && !u.isHidden() && u.breakdown_ctr==0) {
				u.breakdown_ctr = 3;
				u.breakdown_delay = 140;
			}

			v.direction = Vehicle.GetDirectionTowards(v, u.getX_pos(), u.getY_pos());
			v.GetNewVehiclePos(gp);

			z = v.z_pos;
			if (dist <= 16 && z > u.z_pos) z--;
			SetDisasterVehiclePos(v, gp.x, gp.y, z);

			if (z <= u.z_pos && !u.isHidden() ) {
				v.age++;
				if (u.road.crashed_ctr == 0) {
					u.road.crashed_ctr++;
					u.setCrashed(true);

					NewsItem.AddNewsItem(Str.STR_B001_ROAD_VEHICLE_DESTROYED,
							NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_VEHICLE, NewsItem.NT_ACCIDENT, 0),
							u.index,
							0);
				}
			}

			// destroy?
			if (v.age > 50) {
				v.CreateEffectVehicleRel(0, 7, 8, Vehicle.EV_EXPLOSION_LARGE);
				v.SndPlayVehicleFx(Snd.SND_12_EXPLOSION);
				DeleteDisasterVeh(v);
			}
		}
	}

	static void DestructIndustry(Industry i)
	{

		for (int t = 0; t != Global.MapSize(); t++) 
		{
			TileIndex tile = TileIndex.get(t);

			if (tile.IsTileType( TileTypes.MP_INDUSTRY) && tile.getMap().m2 == i.index) {
				tile.getMap().m1 = 0;
				tile.MarkTileDirtyByTile();
			}
		}
	}

	// Airplane which destroys an oil refinery
	static void DisasterTick_2(Vehicle v)
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();

		v.tick_counter++;
		v.disaster.image_override =
				(v.getCurrent_order().station == 1 && 0 != (v.tick_counter & 4)) ? Sprite.SPR_F_15_FIRING : 0;

		v.GetNewVehiclePos( gp);
		SetDisasterVehiclePos(v, gp.x, gp.y, v.z_pos);

		if (gp.x < -160) {
			DeleteDisasterVeh(v);
			return;
		}

		if (v.getCurrent_order().station == 2) {
			if (0==(v.tick_counter&3)) {
				Industry i = Industry.GetIndustry(v.dest_tile.tile);
				int x = i.xy.TileX() * 16;
				int y = i.xy.TileY() * 16;
				int r = Hal.Random();

				Vehicle.CreateEffectVehicleAbove(
						BitOps.GB(r,  0, 6) + x,
						BitOps.GB(r,  6, 6) + y,
						BitOps.GB(r, 12, 4),
						Vehicle.EV_EXPLOSION_SMALL);

				if (++v.age >= 55)
					v.getCurrent_order().station = 3;
			}
		} else if (v.getCurrent_order().station == 1) {
			if (++v.age == 112) {
				Industry i;

				v.getCurrent_order().station = 2;
				v.age = 0;

				i = Industry.GetIndustry(v.dest_tile.tile);
				DestructIndustry(i);

				Global.SetDParam(0, i.townId);
				NewsItem.AddNewsItem(Str.STR_B002_OIL_REFINERY_EXPLOSION, NewsItem.NEWS_FLAGS(NewsItem.NM_THIN,NewsItem.NF_VIEWPORT|NewsItem.NF_TILE,NewsItem.NT_ACCIDENT,0), i.xy.getTile(), 0);
				Sound.SndPlayTileFx(Snd.SND_12_EXPLOSION, i.xy);
			}
		} else if (v.getCurrent_order().station == 0) {
			int x,y;
			TileIndex tile;
			int ind;

			x = v.getX_pos() - 15*16;
			y = v.getY_pos();

			if ( x > Global.MapMaxX() * 16-1)
				return;

			tile = TileIndex.TileVirtXY(x, y);
			if (!tile.IsTileType( TileTypes.MP_INDUSTRY))
				return;

			ind = tile.getMap().m2;
			v.dest_tile = TileIndex.get( ind );

			if (Industry.GetIndustry(ind).type == Industry.IT_OIL_REFINERY) {
				v.getCurrent_order().station = 1;
				v.age = 0;
			}
		}
	}

	// Helicopter which destroys a factory
	static void DisasterTick_3(Vehicle v)
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();

		v.tick_counter++;
		v.disaster.image_override =
				(v.getCurrent_order().station == 1 && 0 != (v.tick_counter & 4)) ? Sprite.SPR_AH_64A_FIRING : 0;

		v.GetNewVehiclePos(gp);
		SetDisasterVehiclePos(v, gp.x, gp.y, v.z_pos);

		if (gp.x > Global.MapSizeX() * 16 + 9*16 - 1) {
			DeleteDisasterVeh(v);
			return;
		}

		if (v.getCurrent_order().station == 2) {
			if (0==(v.tick_counter&3)) {
				Industry i = Industry.GetIndustry(v.dest_tile.tile);
				int x = i.xy.TileX() * 16;
				int y = i.xy.TileY() * 16;
				int r = Hal.Random();

				Vehicle.CreateEffectVehicleAbove(
						BitOps.GB(r,  0, 6) + x,
						BitOps.GB(r,  6, 6) + y,
						BitOps.GB(r, 12, 4),
						Vehicle.EV_EXPLOSION_SMALL);

				if (++v.age >= 55)
					v.getCurrent_order().station = 3;
			}
		} else if (v.getCurrent_order().station == 1) {
			if (++v.age == 112) {
				Industry i;

				v.getCurrent_order().station = 2;
				v.age = 0;

				i = Industry.GetIndustry(v.dest_tile.tile);
				DestructIndustry(i);

				Global.SetDParam(0, i.townId);
				NewsItem.AddNewsItem(Str.STR_B003_FACTORY_DESTROYED_IN_SUSPICIOUS, NewsItem.NEWS_FLAGS(NewsItem.NM_THIN,NewsItem.NF_VIEWPORT|NewsItem.NF_TILE,NewsItem.NT_ACCIDENT,0), i.xy.getTile(), 0);
				Sound.SndPlayTileFx(Snd.SND_12_EXPLOSION, i.xy);
			}
		} else if (v.getCurrent_order().station == 0) {
			int x,y;
			TileIndex tile;
			int ind;

			x = v.getX_pos() - 15*16;
			y = v.getY_pos();

			if ( x > Global.MapMaxX() * 16-1)
				return;

			tile = TileIndex.TileVirtXY(x, y);
			if (!tile.IsTileType( TileTypes.MP_INDUSTRY))
				return;

			v.dest_tile = TileIndex.get( ind = tile.getMap().m2 );

			if (Industry.GetIndustry(ind).type == Industry.IT_FACTORY) {
				v.getCurrent_order().station = 1;
				v.age = 0;
			}
		}
	}

	// Helicopter rotor blades
	static void DisasterTick_3b(Vehicle v)
	{
		if(0 != (++v.tick_counter & 1) )
			return;

		if (++v.cur_image > Sprite.SPR_ROTOR_MOVING_3) v.cur_image = Sprite.SPR_ROTOR_MOVING_1;

		v.VehiclePositionChanged();
		v.BeginVehicleMove();
		v.EndVehicleMove();
	}

	// Big UFO which lands on a piece of rail.
	// Will be shot down by a plane
	static void DisasterTick_4(Vehicle v)
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();
		int z;
		Vehicle w;
		Town t;
		TileIndex tile;
		TileIndex tile_org;

		v.tick_counter++;

		if (v.getCurrent_order().station == 1) {
			int x = v.dest_tile.TileX() * 16 + 8;
			int y = v.dest_tile.TileY() * 16 + 8;
			if (Math.abs(v.getX_pos() - x) + Math.abs(v.getY_pos() - y) >= 8) {
				v.direction = Vehicle.GetDirectionTowards(v, x, y);

				v.GetNewVehiclePos(gp);
				SetDisasterVehiclePos(v, gp.x, gp.y, v.z_pos);
				return;
			}

			z = Landscape.GetSlopeZ(v.getX_pos(), v.getY_pos());
			if (z < v.z_pos) {
				SetDisasterVehiclePos(v, v.getX_pos(), v.getY_pos(), v.z_pos - 1);
				return;
			}

			v.getCurrent_order().station = 2;

			Vehicle.forEach( (u) ->
			{
				if (u.type == Vehicle.VEH_Train || u.type == Vehicle.VEH_Road) {
					if (Math.abs(u.getX_pos() - v.getX_pos()) + Math.abs(u.getY_pos() - v.getY_pos()) <= 12*16) {
						u.breakdown_ctr = 5;
						u.breakdown_delay = 0xF0;
					}
				}
			});

			t = Town.ClosestTownFromTile(v.dest_tile, -1);
			Global.SetDParam(0, t.index);
			NewsItem.AddNewsItem(Str.STR_B004_UFO_LANDS_NEAR,
					NewsItem.NEWS_FLAGS(NewsItem.NM_THIN, NewsItem.NF_VIEWPORT|NewsItem.NF_TILE, NewsItem.NT_ACCIDENT, 0),
					v.tile.getTile(),
					0);

			Vehicle u = Vehicle.ForceAllocateSpecialVehicle();
			if (u == null) {
				DeleteDisasterVeh(v);
				return;
			}

			InitializeDisasterVehicle(u, -6*16, v.getY_pos(), 135, 5, 11);
			u.disaster.unk2 = v.index;

			w = Vehicle.ForceAllocateSpecialVehicle();
			if (w == null)
				return;

			u.next = w;
			InitializeDisasterVehicle(w, -6*16, v.getY_pos(), 0, 5, 12);
			w.setDisaster(true);
		} else if (v.getCurrent_order().station < 1) {

			int x = v.dest_tile.TileX() * 16;
			int y = v.dest_tile.TileY() * 16;
			if (Math.abs(x - v.getX_pos()) + Math.abs(y - v.getY_pos()) >= 16) {
				v.direction = Vehicle.GetDirectionTowards(v, x, y);
				v.GetNewVehiclePos( gp);
				SetDisasterVehiclePos(v, gp.x, gp.y, v.z_pos);
				return;
			}

			if (++v.age < 6) {
				v.dest_tile = Hal.RandomTile();
				return;
			}
			v.getCurrent_order().station = 1;

			tile_org = tile = Hal.RandomTile();
			do {
				if (tile.IsTileType( TileTypes.MP_RAILWAY) &&
						(tile.getMap().m5 & ~3) != 0xC0 && tile.GetTileOwner().IS_HUMAN_PLAYER())
					break;
				tile = tile.iadd(1);
				tile.TILE_MASK();
			} while (!tile.equals(tile_org));
			v.dest_tile = tile;
			v.age = 0;
		}
			
	}

	// The plane which will shoot down the UFO
	static void DisasterTick_4b(Vehicle v)
	{
		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();
		Vehicle u;
		int i;

		v.tick_counter++;

		v.GetNewVehiclePos( gp);
		SetDisasterVehiclePos(v, gp.x, gp.y, v.z_pos);

		if (gp.x > Global.MapSizeX() * 16 + 9*16 - 1) {
			DeleteDisasterVeh(v);
			return;
		}

		if (v.getCurrent_order().station == 0) {
			u = Vehicle.GetVehicle(v.disaster.unk2);
			if (Math.abs(v.getX_pos() - u.getX_pos()) > 16)
				return;
			v.getCurrent_order().station = 1;

			u.CreateEffectVehicleRel(0, 7, 8, Vehicle.EV_EXPLOSION_LARGE);
			u.SndPlayVehicleFx(Snd.SND_12_EXPLOSION);

			DeleteDisasterVeh(u);

			for(i=0; i!=80; i++) {
				int r = Hal.Random();
				Vehicle.CreateEffectVehicleAbove(
						BitOps.GB(r, 0, 6) + v.getX_pos() - 32,
						BitOps.GB(r, 5, 6) + v.getY_pos() - 32,
						0,
						Vehicle.EV_EXPLOSION_SMALL);
			}

			//BEGIN_TILE_LOOP(tile, 6, 6, v.tile - TileDiffXY(3, 3))
			TileIndex.forAll(6, 6, v.tile.isub(TileIndex.TileDiffXY(3, 3)), (tile) ->
			{
				tile.TILE_MASK();
				DisasterClearSquare(tile);
				return false;
			});
			//END_TILE_LOOP(tile, 6, 6, v.tile - TileDiffXY(3, 3))
		}
	}

	// Submarine handler
	static void DisasterTick_5_and_6(Vehicle v)
	{

		GetNewVehiclePosResult gp = new GetNewVehiclePosResult();
		TileIndex tile;

		v.tick_counter++;

		if (++v.age > 8880) {
			v.VehiclePositionChanged();
			v.BeginVehicleMove();
			v.EndVehicleMove();
			v.DeleteVehicle();
			return;
		}

		if (0==(v.tick_counter&1))
			return;

		tile = v.tile.iadd( TileIndex.TileOffsByDir(v.direction >> 1) );
		if (tile.IsValidTile() ) 
		{
			int r=Landscape.GetTileTrackStatus(tile,TransportType.Water);
			if( (((r&0xFF) + (0xFF & (r >> 8))) == 0x3F) && !BitOps.CHANCE16(1,90)) 
			{
				v.GetNewVehiclePos(gp);
				SetDisasterVehiclePos(v, gp.x, gp.y, v.z_pos);
				return;
			}
		}

		v.direction = (v.direction + (BitOps.GB(Hal.Random(), 0, 1)!=0 ? 2 : -2) ) & 7;
	}


	static void DisasterTick_null(Vehicle v) { /* is empty */ }

	static final DisasterVehicleTickProc[] _disastervehicle_tick_procs = {
			DisasterCmd::DisasterTick_Zeppeliner, DisasterCmd::DisasterTick_null,
			DisasterCmd::DisasterTick_UFO, DisasterCmd::DisasterTick_null,
			DisasterCmd::DisasterTick_2, DisasterCmd::DisasterTick_null,
			DisasterCmd::DisasterTick_3, DisasterCmd::DisasterTick_null, DisasterCmd::DisasterTick_3b,
			DisasterCmd::DisasterTick_4, DisasterCmd::DisasterTick_null,
			DisasterCmd::DisasterTick_4b, DisasterCmd::DisasterTick_null,
			DisasterCmd::DisasterTick_5_and_6,
			DisasterCmd::DisasterTick_5_and_6,
	};


	public static void DisasterVehicle_Tick(Vehicle v)
	{
		_disastervehicle_tick_procs[v.subtype].accept(v);
	}


	public static void OnNewDay_DisasterVehicle(Vehicle v)
	{
		// not used
	}


	// Zeppeliner which crashes on a small airport
	static void Disaster0_Init()
	{
		Vehicle v = Vehicle.ForceAllocateSpecialVehicle(), u;		
		int x;

		if (v == null)
			return;

		/* Pick a random place, unless we find
		    a small airport */
		TileIndex tile = new TileIndex(Hal.Random());
		x = tile.TileX() * 16 + 8;

		Iterator<Station> ii = Station.getIterator();
		while(ii.hasNext())
		{
			Station st = ii.next();
			if (st.getXy() != null && st.airport_tile != null &&
					st.airport_type <= 1 &&
					st.owner.IS_HUMAN_PLAYER()) 
			{
				x = (st.getXy().TileX() + 2) * 16;
				break;
			}
		}

		InitializeDisasterVehicle(v, x, 0, 135, 3, 0);

		// Allocate shadow too?
		u = Vehicle.ForceAllocateSpecialVehicle();
		if (u != null) {
			v.next = u;
			InitializeDisasterVehicle(u, x, 0, 0, 3, 1);
			u.setDisaster(true);
		}
	}

	static void Disaster1_Init()
	{
		Vehicle v = Vehicle.ForceAllocateSpecialVehicle(), u;
		int x;

		if (v == null)
			return;

		TileIndex tile = new TileIndex(Hal.Random());
		x = tile.TileX() * 16 + 8;

		InitializeDisasterVehicle(v, x, 0, 135, 3, 2);
		v.dest_tile = TileIndex.TileXY(Global.MapSizeX() / 2, Global.MapSizeY() / 2);
		v.age = 0;

		// Allocate shadow too?
		u = Vehicle.ForceAllocateSpecialVehicle();
		if (u != null) {
			v.next = u;
			InitializeDisasterVehicle(u,x,0,0,3,3);
			u.setDisaster(true);
		}
	}

	static void Disaster2_Init()
	{
		Industry found;
		Vehicle v,u;
		int x,y;

		found = null;

		Iterator<Industry> ii = Industry.getIterator();
		while(ii.hasNext())
		{
			Industry i = ii.next();
			if (i.isValid() &&
					i.type == Industry.IT_OIL_REFINERY &&
					(found==null || BitOps.CHANCE16(1,2))) {
				found = i;
			}
		}

		if (found == null)
			return;

		v = Vehicle.ForceAllocateSpecialVehicle();
		if (v == null)
			return;

		x = (Global.MapSizeX() + 9) * 16 - 1;
		y = found.xy.TileY() * 16 + 37;

		InitializeDisasterVehicle(v,x,y, 135,1,4);

		u = Vehicle.ForceAllocateSpecialVehicle();
		if (u != null) {
			v.next = u;
			InitializeDisasterVehicle(u,x,y,0,3,5);
			u.setDisaster(true);
		}
	}

	static void Disaster3_Init()
	{
		Industry found;
		Vehicle v,u,w;
		int x,y;

		found = null;

		Iterator<Industry> ii = Industry.getIterator();
		while(ii.hasNext())
		{
			Industry i = ii.next();
			if (i.isValid() &&
					i.type == Industry.IT_FACTORY &&
					(found==null || BitOps.CHANCE16(1,2))) {
				found = i;
			}
		}

		if (found == null)
			return;

		v = Vehicle.ForceAllocateSpecialVehicle();
		if (v == null)
			return;

		x = -16 * 16;
		y = found.xy.TileY() * 16 + 37;

		InitializeDisasterVehicle(v,x,y, 135,5,6);

		u = Vehicle.ForceAllocateSpecialVehicle();
		if (u != null) {
			v.next = u;
			InitializeDisasterVehicle(u,x,y,0,5,7);
			u.setDisaster(true);

			w = Vehicle.ForceAllocateSpecialVehicle();
			if (w != null) {
				u.next = w;
				InitializeDisasterVehicle(w,x,y,140,5,8);
			}
		}
	}

	static void Disaster4_Init()
	{
		Vehicle v = Vehicle.ForceAllocateSpecialVehicle(), u;
		int x,y;

		if (v == null) return;

		TileIndex tile = new TileIndex(Hal.Random());
		x = tile.TileX() * 16 + 8;

		y = Global.MapMaxX() * 16 - 1;
		InitializeDisasterVehicle(v, x, y, 135, 7, 9);
		v.dest_tile = TileIndex.TileXY(Global.MapSizeX() / 2, Global.MapSizeY() / 2);
		v.age = 0;

		// Allocate shadow too?
		u = Vehicle.ForceAllocateSpecialVehicle();
		if (u != null) {
			v.next = u;
			InitializeDisasterVehicle(u,x,y,0,7,10);
			u.setDisaster(true);
		}
	}

	// Submarine type 1
	static void Disaster5_Init()
	{
		Vehicle v = Vehicle.ForceAllocateSpecialVehicle();
		int x,y;
		byte dir;

		if (v == null) return;

		TileIndex r = new TileIndex(Hal.Random());
		x = r.TileX() * 16 + 8;

		y = 8;
		dir = 3;
		if(0 != (r.getTile() & 0x80000000)) { y = Global.MapMaxX() * 16 - 8 - 1; dir = 7; }
		InitializeDisasterVehicle(v, x, y, 0, dir,13);
		v.age = 0;
	}

	// Submarine type 2
	static void Disaster6_Init()
	{
		Vehicle v = Vehicle.ForceAllocateSpecialVehicle();
		int x,y;
		byte dir;

		if (v == null) return;

		TileIndex r = new TileIndex(Hal.Random());
		x = r.TileX() * 16 + 8;

		y = 8;
		dir = 3;
		if(0 != (r.getTile() & 0x80000000)) { y = Global.MapMaxX() * 16 - 8 - 1; dir = 7; }
		InitializeDisasterVehicle(v, x, y, 0, dir,14);
		v.age = 0;
	}

	static void Disaster7_Init()
	{
		int index = BitOps.GB(Hal.Random(), 0, 4);
		int m;

		for (m = 0; m < 15; m++) {
			Iterator<Industry> ii = Industry.getIterator();
			while(ii.hasNext())
			{
				Industry i = ii.next();
				if (i.isValid() && i.type == Industry.IT_COAL_MINE && --index < 0) 
				{

					Global.SetDParam(0, i.townId);
					NewsItem.AddNewsItem(Str.STR_B005_COAL_MINE_SUBSIDENCE_LEAVES,
							NewsItem.NEWS_FLAGS(NewsItem.NM_THIN,NewsItem.NF_VIEWPORT|NewsItem.NF_TILE,NewsItem.NT_ACCIDENT,0), i.xy.iadd(1, 1).getTile(), 0);

					{
						TileIndex tile = i.xy;
						TileIndexDiff step = TileIndex.TileOffsByDir(BitOps.GB(Hal.Random(), 0, 2));
						int n;

						for (n = 0; n < 30; n++) {
							DisasterClearSquare(tile);
							tile = tile.iadd(step);
							tile.TILE_MASK();
						}
					}
					return;
				}
			}
		}
	}

	static final DisasterInitProc[] _disaster_initprocs = {
			DisasterCmd::Disaster0_Init,
			DisasterCmd::Disaster1_Init,
			DisasterCmd::Disaster2_Init,
			DisasterCmd::Disaster3_Init,
			DisasterCmd::Disaster4_Init,
			DisasterCmd::Disaster5_Init,
			DisasterCmd::Disaster6_Init,
			DisasterCmd::Disaster7_Init,
	};



	static void DoDisaster()
	{
		byte [] buf = new byte[_dis_years.length];
		byte year = (byte) Global.get_cur_year();
		int i;
		int j;

		j = 0;
		for (i = 0; i != _dis_years.length; i++) {
			if (year >= _dis_years[i].min && year < _dis_years[i].max) 
				buf[j++] = (byte) i;
		}

		if (j == 0) return;

		_disaster_initprocs[buf[Hal.RandomRange(j)]].init();
	}


	static void ResetDisasterDelay()
	{
		Global._disaster_delay = BitOps.GB(Hal.Random(), 0, 9) + 730;
	}

	public static void DisasterDailyLoop()
	{
		if (--Global._disaster_delay != 0) return;

		ResetDisasterDelay();

		if (GameOptions._opt.diff.disasters != 0) DoDisaster();
	}

	static void StartupDisasters()
	{
		ResetDisasterDelay();
	}


}


@FunctionalInterface
interface DisasterVehicleTickProc extends Consumer<Vehicle> {}


@FunctionalInterface
interface DisasterInitProc
{
	void init();
}
