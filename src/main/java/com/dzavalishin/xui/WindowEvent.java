package com.dzavalishin.xui;

import com.dzavalishin.enums.WindowEvents;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.struct.Point;

/* [dz] - supposed to be not actual nymore - XX X - outside "byte event" so you can set event directly without going into
 * the union elements at first. Because of this every first element of the union
 * MUST BE 'byte event'. Whoever did this must get shot! Scheduled for immediate
 * rewrite after 0.4.0 */

public class WindowEvent {

	//int 
	public WindowEvents event;
	public Point pt = new Point(0, 0);

	// click, dragdrop, mouseover
	public int widget;

	// place
	public TileIndex tile;
	public TileIndex starttile;
	public int userdata;

	// sizing
	public Point size;
	public Point diff;

	// edittext
	public String str;

	// popupmenu;

	// dropdown
	public int button;
	public int index;

	// keypress
	public boolean cont;   // continue the search? (default true)
	public int ascii;  // 8-bit ASCII-value of the key
	public int keycode;// untranslated key (including shift-state)

	// message
	public int msg;    // message to be sent
	public int wparam; // additional message-specific information
	public int lparam; // additional message-specific information
}
