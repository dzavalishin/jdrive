package com.dzavalishin.util;

public class WindowConstants {

	
	
	//enum WindowKeyCodes {
		public static final int WKC_SHIFT = 0x8000;
		public static final int WKC_CTRL  = 0x4000;
		public static final int WKC_ALT   = 0x2000;
		public static final int WKC_META  = 0x1000;

		// Special ones
		public static final int WKC_NONE = 0;
		public static final int WKC_ESC=1;
		public static final int WKC_BACKSPACE = 2;
		public static final int WKC_INSERT = 3;
		public static final int WKC_DELETE = 4;

		public static final int WKC_PAGEUP = 5;
		public static final int WKC_PAGEDOWN = 6;
		public static final int WKC_END = 7;
		public static final int WKC_HOME = 8;

		// Arrow keys
		public static final int WKC_LEFT = 9;
		public static final int WKC_UP = 10;
		public static final int WKC_RIGHT = 11;
		public static final int WKC_DOWN = 12;

		// Return & tab
		public static final int WKC_RETURN = 13;
		public static final int WKC_TAB = 14;

		// Numerical keyboard
		public static final int WKC_NUM_0 = 16;
		public static final int WKC_NUM_1 = 17;
		public static final int WKC_NUM_2 = 18;
		public static final int WKC_NUM_3 = 19;
		public static final int WKC_NUM_4 = 20;
		public static final int WKC_NUM_5 = 21;
		public static final int WKC_NUM_6 = 22;
		public static final int WKC_NUM_7 = 23;
		public static final int WKC_NUM_8 = 24;
		public static final int WKC_NUM_9 = 25;
		public static final int WKC_NUM_DIV = 26;
		public static final int WKC_NUM_MUL = 27;
		public static final int WKC_NUM_MINUS = 28;
		public static final int WKC_NUM_PLUS = 29;
		public static final int WKC_NUM_ENTER = 30;
		public static final int WKC_NUM_DECIMAL = 31;

		// Space
		public static final int WKC_SPACE = 32;

		// Function keys
		public static final int WKC_F1 = 33;
		public static final int WKC_F2 = 34;
		public static final int WKC_F3 = 35;
		public static final int WKC_F4 = 36;
		public static final int WKC_F5 = 37;
		public static final int WKC_F6 = 38;
		public static final int WKC_F7 = 39;
		public static final int WKC_F8 = 40;
		public static final int WKC_F9 = 41;
		public static final int WKC_F10 = 42;
		public static final int WKC_F11 = 43;
		public static final int WKC_F12 = 44;

		// backquote is the key left of "1"
		// we only store this key here; no matter what character is really mapped to it
		// on a particular keyboard. (US keyboard: ` and ~ ; German keyboard: ^ and ([dz] some char))
		public static final int WKC_BACKQUOTE = 45;
		public static final int WKC_PAUSE     = 46;

		// 0-9 are mapped to 48-57
		// A-Z are mapped to 65-90
		// a-z are mapped to 97-122
//	};
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static final int WC_MAIN_WINDOW = 0x0;
	public static final int WC_MAIN_TOOLBAR = 0x1;
	public static final int WC_STATUS_BAR = 0x2;
	public static final int WC_BUILD_TOOLBAR = 0x3;
	public static final int WC_NEWS_WINDOW = 0x4;
	public static final int WC_TOWN_DIRECTORY = 0x5;
	public static final int WC_STATION_LIST = 0x6;
	public static final int WC_TOWN_VIEW = 0x7;
	public static final int WC_SMALLMAP = 0x8;
	public static final int WC_TRAINS_LIST = 0x9;
	public static final int WC_ROADVEH_LIST = 0xA;
	public static final int WC_SHIPS_LIST = 0xB;
	public static final int WC_AIRCRAFT_LIST = 0xC;
	public static final int WC_VEHICLE_VIEW = 0xD;
	public static final int WC_VEHICLE_DETAILS = 0xE;
	public static final int WC_VEHICLE_REFIT = 0xF;
	public static final int WC_VEHICLE_ORDERS = 0x10;
	public static final int WC_STATION_VIEW = 0x11;
	public static final int WC_VEHICLE_DEPOT = 0x12;
	public static final int WC_BUILD_VEHICLE = 0x13;
	public static final int WC_BUILD_BRIDGE = 0x14;
	public static final int WC_ERRMSG = 0x15;
	public static final int WC_ASK_ABANDON_GAME = 0x16;
	public static final int WC_QUIT_GAME = 0x17;
	public static final int WC_BUILD_STATION = 0x18;
	public static final int WC_BUS_STATION = 0x19;
	public static final int WC_TRUCK_STATION = 0x1A;
	public static final int WC_BUILD_DEPOT = 0x1B;
	public static final int WC_COMPANY = 0x1D;
	public static final int WC_FINANCES = 0x1E;
	public static final int WC_PLAYER_COLOR = 0x1F;
	public static final int WC_QUERY_STRING = 0x20;
	public static final int WC_SAVELOAD = 0x21;
	public static final int WC_SELECT_GAME = 0x22;
	public static final int WC_TOOLBAR_MENU = 0x24;
	public static final int WC_INCOME_GRAPH = 0x25;
	public static final int WC_OPERATING_PROFIT = 0x26;
	public static final int WC_TOOLTIPS = 0x27;
	public static final int WC_INDUSTRY_VIEW = 0x28;
	public static final int WC_PLAYER_FACE = 0x29;
	public static final int WC_LAND_INFO = 0x2A;
	public static final int WC_TOWN_AUTHORITY = 0x2B;
	public static final int WC_SUBSIDIES_LIST = 0x2C;
	public static final int WC_GRAPH_LEGEND = 0x2D;
	public static final int WC_DELIVERED_CARGO = 0x2E;
	public static final int WC_PERFORMANCE_HISTORY = 0x2F;
	public static final int WC_COMPANY_VALUE = 0x30;
	public static final int WC_COMPANY_LEAGUE = 0x31;
	public static final int WC_BUY_COMPANY = 0x32;
	public static final int WC_PAYMENT_RATES = 0x33;
	public static final int WC_ENGINE_PREVIEW = 0x35;
	public static final int WC_MUSIC_WINDOW = 0x36;
	public static final int WC_MUSIC_TRACK_SELECTION = 0x37;
	public static final int WC_SCEN_LAND_GEN = 0x38; // also used for landscaping toolbar
	public static final int WC_ASK_RESET_LANDSCAPE = 0x39;
	public static final int WC_SCEN_TOWN_GEN = 0x3A;
	public static final int WC_SCEN_INDUSTRY = 0x3B;
	public static final int WC_SCEN_BUILD_ROAD = 0x3C;
	public static final int WC_BUILD_TREES = 0x3D;
	public static final int WC_SEND_NETWORK_MSG = 0x3E;
	public static final int WC_DROPDOWN_MENU = 0x3F;
	public static final int WC_BUILD_INDUSTRY = 0x40;
	public static final int WC_GAME_OPTIONS = 0x41;
	public static final int WC_NETWORK_WINDOW = 0x42;
	public static final int WC_INDUSTRY_DIRECTORY = 0x43;
	public static final int WC_MESSAGE_HISTORY = 0x44;
	public static final int WC_CHEATS = 0x45;
	public static final int WC_PERFORMANCE_DETAIL = 0x46;
	public static final int WC_CONSOLE = 0x47;
	public static final int WC_EXTRA_VIEW_PORT = 0x48;
	public static final int WC_CLIENT_LIST = 0x49;
	public static final int WC_NETWORK_STATUS_WINDOW = 0x4A;
	public static final int WC_CUSTOM_CURRENCY = 0x4B;
	public static final int WC_REPLACE_VEHICLE = 0x4C;
	public static final int WC_HIGHSCORE = 0x4D;
	public static final int WC_ENDSCREEN = 0x4E;
	public static final int WC_SIGN_LIST = 0x4F;
	public static final int WC_BUILD_SIGNALS = 0x50;



	/****************** THESE ARE NOT WIDGET TYPES!!!!! *******************/
	//enum WindowWidgetBehaviours {
	public static final int WWB_PUSHBUTTON = 1 << 5;
	public static final int WWB_NODISBUTTON = 2 << 5;
	//};




	public static final int WWT_EMPTY = 0;

	public static final int WWT_IMGBTN = 1;						/* button with image */
	public static final int WWT_PANEL =  WWT_IMGBTN;
	public static final int WWT_PANEL_2 = 2;					/* button with diff image when clicked */

	public static final int WWT_TEXTBTN = 3;					/* button with text */
	public static final int WWT_4 = 4;								/* button with diff text when clicked */
	public static final int WWT_5 = 5;								/* label */
	public static final int WWT_6 = 6;								/* combo box text area */
	public static final int WWT_MATRIX = 7;
	public static final int WWT_SCROLLBAR = 8;
	public static final int WWT_FRAME = 9;						/* frame */
	public static final int WWT_CAPTION = 10;

	public static final int WWT_HSCROLLBAR = 11;
	public static final int WWT_STICKYBOX = 12;
	public static final int WWT_SCROLL2BAR = 13;				/* 2nd vertical scrollbar*/
	public static final int WWT_RESIZEBOX = 14;
	public static final int WWT_CLOSEBOX = 15;
	public static final int WWT_LAST = 16;						/* Last Item. use WIDGETS_END to fill up padding!! */

	public static final int WWT_MASK = 31;

	public static final int WWT_PUSHTXTBTN	=  WWT_TEXTBTN	| WWB_PUSHBUTTON;
	public static final int WWT_PUSHIMGBTN	=  WWT_IMGBTN	| WWB_PUSHBUTTON;
	public static final int WWT_NODISTXTBTN =  WWT_TEXTBTN	| WWB_NODISBUTTON;






	public static final int  WF_TIMEOUT_SHL = 0;
	public static final int  WF_TIMEOUT_MASK = 7;
	public static final int  WF_DRAGGING = 1 << 3;
	public static final int  WF_SCROLL_UP = 1 << 4;
	public static final int  WF_SCROLL_DOWN = 1 << 5;
	public static final int  WF_SCROLL_MIDDLE = 1 << 6;
	public static final int  WF_HSCROLL = 1 << 7;
	public static final int  WF_SIZING = 1 << 8;
	public static final int  WF_STICKY = 1 << 9;

	public static final int  WF_DISABLE_VP_SCROLL = 1 << 10;

	public static final int  WF_WHITE_BORDER_ONE = 1 << 11;
	public static final int  WF_WHITE_BORDER_MASK = 3 << 11;
	public static final int  WF_SCROLL2 = 1 << 13;




	// enum FrameFlags {
	public static final int  FR_TRANSPARENT  = 0x01;  ///< Makes the background transparent if set
	public static final int  FR_NOBORDER     = 0x08;  ///< Hide border (draws just a solid box)
	public static final int  FR_BORDERONLY   = 0x10;  ///< Draw border only; no background
	public static final int  FR_LOWERED      = 0x20;  ///< If set the frame is lowered and the background color brighter (ie. buttons when pressed)
	public static final int  FR_DARKENED     = 0x40;  ///< If set the background is darker; allows for lowered frames with normal background color when used with FR_LOWERED (ie. dropdown boxes)



	//enum SpecialMouseMode {
	public static final int  WSM_NONE = 0;
	public static final int  WSM_DRAGDROP = 1;
	public static final int  WSM_SIZING = 2;
	public static final int  WSM_PRESIZE = 3;
	//}




	/* How the resize system works:
    First, you need to add a WWT_RESIZEBOX to the widgets, and you need
     to add the flag WDF_RESIZABLE to the window. Now the window is ready
     to resize itself.
    As you may have noticed, all widgets have a RESIZE_XX in their line.
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
      w.resize.step_height = 10;
    Now the window will only change in height in steps of 10.
   You can also give a minimum width and height. The default value is
    the default height/width of the window itself. You can change this
    AFTER window-creation, with:
     w.resize.width or w.resize.height.
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

	/* can be used as x or y coordinates to cause a specific placement */
	public static final int WDP_AUTO = -1;
	public static final int WDP_CENTER = -2;

	
	
	
	
	
	

}
