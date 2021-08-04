package game.util.wcustom;


import game.WindowClass;
import game.WindowNumber;

public class dropdown_d extends AbstractWinCustom  {
	public int disabled_state;
	public int hidden_state;
	public WindowClass parent_wnd_class;
	public WindowNumber parent_wnd_num;
	public int parent_button;
	public int num_items;
	public int selected_index;
	//public StringID items[];
	public int items[];
	public int click_delay;
	public boolean drag_mode;
} ;
