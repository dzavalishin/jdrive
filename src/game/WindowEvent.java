package game;

/* XXX - outside "byte event" so you can set event directly without going into
 * the union elements at first. Because of this every first element of the union
 * MUST BE 'byte event'. Whoever did this must get shot! Scheduled for immediate
 * rewrite after 0.4.0 */

public class WindowEvent {

	//int 
	WindowEvents event;
	Point pt;

	// click, dragdrop, mouseover
	int widget;

	// place
	TileIndex tile;
	TileIndex starttile;
	int userdata;

	// sizing
	Point size;
	Point diff;

	// edittext
	String str;

	// popupmenu;

	// dropdown
	int button;
	int index;

	// keypress
	boolean cont;   // continue the search? (default true)
	byte ascii;  // 8-bit ASCII-value of the key
	int keycode;// untranslated key (including shift-state)

	// message
	int msg;    // message to be sent
	int wparam; // additional message-specific information
	int lparam; // additional message-specific information
}
