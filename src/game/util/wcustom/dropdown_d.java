package game.util.wcustom;

import game.StringID;

public class dropdown_d extends AbstractWinCustom  {
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
