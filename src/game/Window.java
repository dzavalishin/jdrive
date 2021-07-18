package game;

import game.util.WindowConstants;
import game.util.wcustom.AbstractWinCustom;

abstract public class Window extends WindowConstants
{
	int flags4;
	WindowClass window_class;
	WindowNumber window_number;

	int left, top;
	int width, height;

	Scrollbar hscroll, vscroll, vscroll2;
	ResizeInfo resize;

	byte caption_color;

	int click_state, disabled_state, hidden_state;

	ViewPort viewport;
	Widget original_widget;
	Widget widget;
	int desc_flags;

	WindowMessage message;
	//byte custom[WINDOW_CUSTOM_SIZE];
	//byte custom[];
	AbstractWinCustom custom;

	//WindowProc *wndproc;
	abstract void WindowProc( WindowEvent e);
	void wndproc(WindowEvent e) { WindowProc(e); }

	void CallWindowEventNP(WindowEvents event)
	{
		WindowEvent e = new WindowEvent();

		e.event = event;
		wndproc(e);
	}


	void SetWindowDirty()
	{
		Global.hal.SetDirtyBlocks(left, top, left + width, top + height);
	}

	/** Returns the index for the widget located at the given position
	 * relative to the window. It includes all widget-corner pixels as well.
	 * @param *w Window to look inside
	 * @param  x,y Window client coordinates
	 * @return A widget index, or -1 if no widget was found.
	 */
	int GetWidgetFromPos(int x, int y)
	{
		Widget wi;
		int index, found_index = -1;

		// Go through the widgets and check if we find the widget that the coordinate is
		// inside.
		for (index = 0,wi = widget; wi.type != WWT_LAST; index++, wi++) {
			if (wi.type == WWT_EMPTY || wi.type == WWT_FRAME) continue;

			if (x >= wi.left && x <= wi.right && y >= wi.top &&  y <= wi.bottom &&
					!BitOps.HASBIT(w.hidden_state,index)) {
				found_index = index;
			}
		}

		return found_index;
	}

	
	
	
	
	/* How the resize system works:
    First, you need to add a WWT_RESIZEBOX to the widgets, and you need
     to add the flag WDF_RESIZABLE to the window. Now the window is ready
     to resize itself.
    As you may have noticed, all widgets have a RESIZE_XXX in their line.
     This lines controls how the widgets behave on resize. RESIZE_NONE means
     it doesn't do anything. Any other option let's one of the borders
     move with the changed width/height. So if a widget has
     RESIZE_RIGHT, and the window is made 5 pixels wider by the user,
     the right of the window will also be made 5 pixels wider.
    Now, what if you want to clamp a widget to the bottom? Give it the flag
     RESIZE_TB. This is RESIZE_TOP + RESIZE_BOTTOM. Now if the window gets
     5 pixels bigger, both the top and bottom gets 5 bigger, so the whole
     widgets moves downwards without resizing, and appears to be clamped
     to the bottom. Nice aint it?
   You should know one more thing about this system. Most windows can't
    handle an increase of 1 pixel. So there is a step function, which
    let the windowsize only be changed by X pixels. You configure this
    after making the window, like this:
      w->resize.step_height = 10;
    Now the window will only change in height in steps of 10.
   You can also give a minimum width and height. The default value is
    the default height/width of the window itself. You can change this
    AFTER window-creation, with:
     w->resize.width or w->resize.height.
   That was all.. good luck, and enjoy :) -- TrueLight */

	public static final int RESIZE_NONE   = 0;

	public static final int RESIZE_LEFT   = 1;
	public static final int RESIZE_RIGHT  = 2;
	public static final int RESIZE_TOP    = 4;
	public static final int RESIZE_BOTTOM = 8;

	public static final int RESIZE_LR     = RESIZE_LEFT  | RESIZE_RIGHT;
	public static final int RESIZE_RB     = RESIZE_RIGHT | RESIZE_BOTTOM;
	public static final int RESIZE_TB     = RESIZE_TOP   | RESIZE_BOTTOM;
	public static final int RESIZE_LRB    = RESIZE_LEFT  | RESIZE_RIGHT  | RESIZE_BOTTOM;
	public static final int RESIZE_LRTB   = RESIZE_LEFT  | RESIZE_RIGHT  | RESIZE_TOP | RESIZE_BOTTOM;
	public static final int RESIZE_RTB    = RESIZE_RIGHT | RESIZE_TOP    | RESIZE_BOTTOM;


}

class WindowMessage {
	int msg;
	int wparam;
	int lparam;
}


class ResizeInfo {
	int width; /* Minimum width and height */
	int height;

	int step_width; /* In how big steps the width and height go */
	int step_height;
} 


class WindowClass  {
	int v;
}


class WindowNumber {
	int n;
}


class Scrollbar {
	int count, cap, pos;
} ;




class Widget {
	byte type;
	byte resize_flag;
	byte color;
	int left, right, top, bottom;
	//int unkA;
	StringID tooltips;
} ;





class ViewPort {
	int left,top;												// screen coordinates for the viewport
	int width, height;									// screen width/height for the viewport

	int virtual_left, virtual_top;			// virtual coordinates
	int virtual_width, virtual_height;	// these are just width << zoom, height << zoom

	byte zoom;
};







/* XXX - outside "byte event" so you can set event directly without going into
 * the union elements at first. Because of this every first element of the union
 * MUST BE 'byte event'. Whoever did this must get shot! Scheduled for immediate
 * rewrite after 0.4.0 */
class WindowEvent {
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
