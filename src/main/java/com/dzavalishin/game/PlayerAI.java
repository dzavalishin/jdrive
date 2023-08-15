package com.dzavalishin.game;

import com.dzavalishin.ids.VehicleID;

public class PlayerAI {
	int state;
	int tick; // Used to determine how often to move
	int state_counter; // Can hold tile index!
	int timeout_counter;

	int state_mode;
	int banned_tile_count;
	int railtype_to_use;

	int cargo_type;
	int num_wagons;
	int build_kind;
	int num_build_rec;
	int num_loco_to_build;
	int num_want_fullload;

	int route_type_mask;

	TileIndex start_tile_a;
	TileIndex cur_tile_a;
	int cur_dir_a;
	int start_dir_a;

	TileIndex start_tile_b;
	TileIndex cur_tile_b;
	int cur_dir_b;
	int start_dir_b;

	Vehicle cur_veh; /* only used by some states */

	AiBuildRec src, dst, mid1, mid2;

	VehicleID[] wagon_list = new VehicleID[9];
	int [] order_list_blocks = new int[20];

	TileIndex [] banned_tiles = new TileIndex[16];
	int [] banned_val = new int[16];
}


class AiBuildRec {
	TileIndex spec_tile;
	TileIndex use_tile;
	int rand_rng;
	int cur_building_rule;
	int unk6;
	int unk7;
	int buildcmd_a;
	int buildcmd_b;
	int direction;
	int cargo;
}
