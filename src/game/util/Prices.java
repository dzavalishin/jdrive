package game.util;

public class Prices {

	public static final int NUM_PRICES = 49;
	
	public int station_value;
	public int build_rail;
	public int build_road;
	public int build_signals;
	public int build_bridge;
	public int build_train_depot;
	public int build_road_depot;
	public int build_ship_depot;
	public int build_tunnel;
	public int train_station_track;
	public int train_station_length;
	public int build_airport;
	public int build_bus_station;
	public int build_truck_station;
	public int build_dock;
	public int build_railvehicle;
	public int build_railwagon;
	public int aircraft_base;
	public int roadveh_base;
	public int ship_base;
	public int build_trees;
	public int terraform;
	public int clear_1;
	public int purchase_land;
	public int clear_2;
	public int clear_3;
	public int remove_trees;
	public int remove_rail;
	public int remove_signals;
	public int clear_bridge;
	public int remove_train_depot;
	public int remove_road_depot;
	public int remove_ship_depot;
	public int clear_tunnel;
	public int clear_water;
	public int remove_rail_station;
	public int remove_airport;
	public int remove_bus_station;
	public int remove_truck_station;
	public int remove_dock;
	public int remove_house;
	public int remove_road;
	public int [] running_rail = new int[3];
	public int aircraft_running;
	public int roadveh_running;
	public int ship_running;
	public int build_industry;

	
	
	public int getPrice(int i)
	{
		switch(i)
		{
		case 0: return  station_value;
		case 1: return  build_rail;
		case 2: return  build_road;
		case 3: return  build_signals;
		case 4: return  build_bridge;
		case 5: return  build_train_depot;
		case 6: return  build_road_depot;
		case 7: return  build_ship_depot;
		case 8: return  build_tunnel;
		case 9: return  train_station_track;
		case 10: return  train_station_length;
		case 11: return  build_airport;
		case 12: return  build_bus_station;
		case 13: return  build_truck_station;
		case 14: return  build_dock;
		case 15: return  build_railvehicle;
		case 16: return  build_railwagon;
		case 17: return  aircraft_base;
		case 18: return  roadveh_base;
		case 19: return  ship_base;
		case 20: return  build_trees;
		case 21: return  terraform;
		case 22: return  clear_1;
		case 23: return  purchase_land;
		case 24: return  clear_2;
		case 25: return  clear_3;
		case 26: return  remove_trees;
		case 27: return  remove_rail;
		case 28: return  remove_signals;
		case 29: return  clear_bridge;
		case 30: return  remove_train_depot;
		case 31: return  remove_road_depot;
		case 32: return  remove_ship_depot;
		case 33: return  clear_tunnel;
		case 34: return  clear_water;
		case 35: return  remove_rail_station;
		case 36: return  remove_airport;
		case 37: return  remove_bus_station;
		case 38: return  remove_truck_station;
		case 39: return  remove_dock;
		case 40: return  remove_house;
		case 41: return  remove_road;
		case 42: return  running_rail[0];
		case 43: return  running_rail[1];
		case 44: return  running_rail[2];
		case 45: return  aircraft_running;
		case 46: return  roadveh_running;
		case 47: return  ship_running;
		case 48: return  build_industry;
		
		}
		
		assert false;
		return 0;
	}
	

	public void setPrice(int i, int price)
	{
		switch(i)
		{
		case 0: station_value = price; return;
		case 1: build_rail = price; return;
		case 2: build_road = price; return;
		case 3: build_signals = price; return;
		case 4: build_bridge = price; return;
		case 5: build_train_depot = price; return;
		case 6: build_road_depot = price; return;
		case 7: build_ship_depot = price; return;
		case 8: build_tunnel = price; return;
		case 9: train_station_track = price; return;
		case 10: train_station_length = price; return;
		case 11: build_airport = price; return;
		case 12: build_bus_station = price; return;
		case 13: build_truck_station = price; return;
		case 14: build_dock = price; return;
		case 15: build_railvehicle = price; return;
		case 16: build_railwagon = price; return;
		case 17: aircraft_base = price; return;
		case 18: roadveh_base = price; return;
		case 19: ship_base = price; return;
		case 20: build_trees = price; return;
		case 21: terraform = price; return;
		case 22: clear_1 = price; return;
		case 23: purchase_land = price; return;
		case 24: clear_2 = price; return;
		case 25: clear_3 = price; return;
		case 26: remove_trees = price; return;
		case 27: remove_rail = price; return;
		case 28: remove_signals = price; return;
		case 29: clear_bridge = price; return;
		case 30: remove_train_depot = price; return;
		case 31: remove_road_depot = price; return;
		case 32: remove_ship_depot = price; return;
		case 33: clear_tunnel = price; return;
		case 34: clear_water = price; return;
		case 35: remove_rail_station = price; return;
		case 36: remove_airport = price; return;
		case 37: remove_bus_station = price; return;
		case 38: remove_truck_station = price; return;
		case 39: remove_dock = price; return;
		case 40: remove_house = price; return;
		case 41: remove_road = price; return;
		case 42: running_rail[0] = price; return;
		case 43: running_rail[1] = price; return;
		case 44: running_rail[2] = price; return;
		case 45: aircraft_running = price; return;
		case 46: roadveh_running = price; return;
		case 47: ship_running = price; return;
		case 48: build_industry = price; return;
		
		}
		
		assert false;
	}
	
	
}
