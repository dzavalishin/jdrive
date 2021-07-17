package game.util.wcustom;

import game.EngineID;
import game.StringID;
import game.VehicleID;

// base class for Window attachments of diff kinds
public class AbstractWinCustom {
	// empty
}


class menu_d extends AbstractWinCustom 
{
	byte item_count; /* follow_vehicle */
	byte sel_index;		/* scrollpos_x */
	byte main_button; /* scrollpos_y */
	byte action_id;
	StringID string_id; /* unk30 */
	int checked_items; /* unk32 */
	byte disabled_items;
}

 
class def_d extends AbstractWinCustom {
	int data_1, data_2, data_3;
	int data_4, data_5;
	boolean close;
	byte byte_1;
} ;

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

class buildtrain_d extends AbstractWinCustom {
	byte railtype;
	byte sel_index;
	EngineID sel_engine;
	EngineID rename_engine;
} ;

class replaceveh_d extends AbstractWinCustom {
	byte vehicletype;
	byte sel_index[2];
	EngineID sel_engine[2];
	int count[2];
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

class smallmap_d extends AbstractWinCustom {
	int scroll_x;
	int scroll_y;
	int subscroll;
} ;

class facesel_d extends AbstractWinCustom {
	int face;
	byte gender;
} ;

class refit_d extends AbstractWinCustom {
	int sel;
	byte cargo;
} ;

class vp_d extends AbstractWinCustom {
	VehicleID follow_vehicle;
	int scrollpos_x;
	int scrollpos_y;
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

class news_d extends AbstractWinCustom {
	int follow_vehicle;
	int scrollpos_x;
	int scrollpos_y;
	NewsItem ni;
} ;

class highscore_d extends AbstractWinCustom {
	int background_img;
	int rank;
} ;

class scroller_d extends AbstractWinCustom {
	int height;
	int counter;
} ;

 enum VehicleListFlags {
	VL_DESC    = 0x01,
	VL_RESORT  = 0x02,
	VL_REBUILD = 0x04
} ;

class vehiclelist_d extends AbstractWinCustom  {
	//SortStruct *sort_list;
	int list_length;
	byte sort_type;
	VehicleListFlags flags;
	int resort_timer;
} ;

class message_d extends AbstractWinCustom  {
	int msg;
	int wparam;
	int lparam;
} ;

class dropdown_d extends AbstractWinCustom  {
	int disabled_state;
	int hidden_state;
	WindowClass parent_wnd_class;
	WindowNumber parent_wnd_num;
	byte parent_button;
	byte num_items;
	byte selected_index;
	StringID items[];
	byte click_delay;
	boolean drag_mode;
} ;
