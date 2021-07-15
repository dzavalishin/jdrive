abstract public class Window
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
	byte custom[];


	//WindowProc *wndproc;
    abstract void WindowProc( WindowEvent e);
    void wndproc(WindowEvent e) { WindowProc(e); }

    void CallWindowEventNP(int event)
{
	WindowEvent e = new WindowEvent();

	e.event = event;
	wndproc(e);
}


void SetWindowDirty()
{
	Global.hal.SetDirtyBlocks(w->left, w->top, w->left + w->width, w->top + w->height);
}



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
	int event;
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
