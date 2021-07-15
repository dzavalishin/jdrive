package game;

public class Industry {

	TileIndex xy;
	byte width; /* swapped order of w/h with town */
	byte height;
	Town town;
	
	byte produced_cargo[];
	int cargo_waiting[];
	byte production_rate[];
	byte accepts_cargo[];
	
	byte prod_level;
	
	int last_mo_production[];
	int last_mo_transported[];
	byte pct_transported[];
	int total_production[];
	int total_transported[];
	
	int counter;

	byte type;
	byte owner;
	byte color_map;
	byte last_prod_year;
	byte was_cargo_delivered;

	int index;
	
	
	public Industry() {
		produced_cargo = new byte[2];
		cargo_waiting = new int[2];
		production_rate = new byte[2];
		accepts_cargo = new byte[3];
		last_mo_production = new int[2];
		last_mo_transported = new int[2];
		pct_transported = new byte[2];
		total_production = new int[2];
		total_transported = new int[2];
	}
}
