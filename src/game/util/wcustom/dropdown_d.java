package game.util.wcustom;

import game.StringID;
import game.WindowClass;
import game.WindowNumber;

public class dropdown_d extends AbstractWinCustom  {
	public int disabled_state;
	public int hidden_state;
	public WindowClass parent_wnd_class;
	public WindowNumber parent_wnd_num;
	public byte parent_button;
	public byte num_items;
	public byte selected_index;
	public StringID items[];
	public byte click_delay;
	public boolean drag_mode;
} ;
