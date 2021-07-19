package game.util.wcustom;

import game.EngineID;
import game.StringID;
import game.VehicleID;

// base class for Window attachments of diff kinds
public class AbstractWinCustom {
	// empty
}


class void_d extends AbstractWinCustom {
	byte [] data;
} ;

class tree_d extends AbstractWinCustom {
	int base;
	int count;
} ;

class plstations_d extends AbstractWinCustom {
	byte refresh_counter;
} ;

class tooltips_d extends AbstractWinCustom {
	StringID string_id;
} ;

class replaceveh_d extends AbstractWinCustom {
	byte vehicletype;
	byte [] sel_index = new byte[2];
	EngineID [] sel_engine = new EngineID[2];
	int [] count = new int[2];
} ;

class  traindepot_d extends AbstractWinCustom {
	VehicleID sel;
} ;

class order_d extends AbstractWinCustom {
	int sel;
} ;

class traindetails_d extends AbstractWinCustom {
	byte tab;
} ;

class facesel_d extends AbstractWinCustom {
	int face;
	byte gender;
} ;

class refit_d extends AbstractWinCustom {
	int sel;
	byte cargo;
} ;

// vp2_d is the same as vp_d, except for the data_# values..
class vp2_d extends AbstractWinCustom {
	int follow_vehicle;
	int scrollpos_x;
	int scrollpos_y;
	byte data_1;
	byte data_2;
	byte data_3;
} ;

class highscore_d extends AbstractWinCustom {
	int background_img;
	int rank;
} ;

