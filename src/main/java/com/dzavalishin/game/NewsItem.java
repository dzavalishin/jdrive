package com.dzavalishin.game;

import java.io.Serializable;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dzavalishin.ids.StringID;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.WindowEvents;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.GameDate;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.Strings;
import com.dzavalishin.wcustom.def_d;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Widget;
import com.dzavalishin.xui.Window;
import com.dzavalishin.xui.WindowDesc;
import com.dzavalishin.xui.WindowEvent;
import com.dzavalishin.xui.WindowMessage;

public class NewsItem implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	StringID string_id;
	int  duration;
	int date;
	int flags;
	int display_mode;
	int type;
	int callback;

	TileIndex data_a;
	TileIndex data_b;

	Integer[] params;// = new int[10];

	/* The validation functions for news items get called immediately
	 * before the news are supposed to be shown. If this funcion returns
	 * false, the news item won't be displayed. */
	//boolean (*isValid) ( int data_a, int data_b );
	BiPredicate<Integer, Integer> isValid;





	public NewsItem() {
		clear();
	}


	public NewsItem( NewsItem src )
	{
		clear();
		string_id = src.string_id;
		duration = src.duration;
		date = src.date;
		flags = src.flags;
		display_mode = src.display_mode;
		type = src.type;
		callback = src.callback;
		data_a = src.data_a; 
		data_b = src.data_b;
		params = src.params;
		isValid = src.isValid;
	}

	void clear()
	{
		string_id = null;
		duration = 0;
		date = 0;
		flags = display_mode = type = callback = 0;
		data_a = data_b = null;
		params = new Integer[10];
		isValid = null;
	}


	public static final int NT_ARRIVAL_PLAYER = 0;
	public static final int NT_ARRIVAL_OTHER = 1;
	public static final int NT_ACCIDENT = 2;
	public static final int NT_COMPANY_INFO = 3;
	public static final int NT_ECONOMY = 4;
	public static final int NT_ADVICE = 5;
	public static final int NT_NEW_VEHICLES = 6;
	public static final int NT_ACCEPTANCE = 7;
	public static final int NT_SUBSIDIES = 8;
	public static final int NT_GENERAL = 9;

	public static final int DNC_TRAINAVAIL = 0;
	public static final int DNC_ROADAVAIL = 1;
	public static final int DNC_SHIPAVAIL = 2;
	public static final int DNC_AIRCRAFTAVAIL = 3;
	public static final int DNC_BANKRUPCY = 4;


	//enum NewsMode {
	public static final int NM_SMALL = 0;
	public static final int NM_NORMAL = 1;
	public static final int NM_THIN = 2;
	public static final int NM_CALLBACK = 3;

	//enum NewsFlags {
	public static final int NF_VIEWPORT = 1;
	public static final int NF_TILE = 4;
	public static final int NF_VEHICLE = 8;
	public static final int NF_FORCE_BIG = 0x10;
	public static final int NF_NOEXPIRE = 0x20;
	public static final int NF_INCOLOR = 0x40;


	public static int NEWS_FLAGS(int mode,int flag,int type,int cb) { return ((cb)<<24 | (type)<<16 | (flag)<<8 | (mode)); }



	/* News system
	News system is realized as a FIFO queue (in an array)
	The positions in the queue can't be rearranged, we only access
	the array elements through pointers to the elements. Once the
	array is full, the oldest entry (_oldest_news) is being overwritten
	by the newest (_latest news).

	oldest                   current   lastest
	 |                          |         |
	[O------------F-------------C---------L           ]
	              |
	           forced
	 */

	static final int  MAX_NEWS = 30;
	static final byte   INVALID_NEWS = -1;

	static final NewsItem[] _news_items = new NewsItem[MAX_NEWS];
	static int _current_news = INVALID_NEWS; // points to news item that should be shown next
	static int _oldest_news = 0;    // points to first item in fifo queue
	static int _latest_news = INVALID_NEWS;  // points to last item in fifo queue
	/* if the message being shown was forced by the user, its index is stored in
	 * _forced_news. forced_news is INVALID_NEWS otherwise.
	 * (Users can force messages through history or "last message") */
	static int _forced_news = INVALID_NEWS;

	static int _total_news = 0; // total news count







	/* To add a news item with an attached validation function. This validation function
	 * makes sure that the news item is not outdated when the newspaper pops up. */

	//static public void AddNewsItem(StringID string, int flags, int data_a, int data_b)
	static public void AddValidatedNewsItem( int string, int flags, int data_a, int data_b, BiPredicate<Integer, Integer> valid)
	{
		NewsItem ni;
		Window w;

		if (Global._game_mode == GameModes.GM_MENU)
			return;

		// check the rare case that the oldest (to be overwritten) news item is open
		if (_total_news==MAX_NEWS && (_oldest_news == _current_news || _oldest_news == _forced_news))
			MoveToNexItem();

		_forced_news = INVALID_NEWS;
		if (_total_news < MAX_NEWS) _total_news++;

		// make sure our pointer isn't overflowing
		_latest_news = increaseIndex(_latest_news);

		// overwrite oldest news entry
		if (_oldest_news == _latest_news && _news_items[_oldest_news] != null && _news_items[_oldest_news].string_id != null)
			_oldest_news = increaseIndex(_oldest_news); // but make sure we're not overflowing here

		_news_items[_latest_news] = new NewsItem(); // just make new one
		// add news to _latest_news
		ni = _news_items[_latest_news];
		//memset(ni, 0, sizeof(*ni));

		ni.string_id = new StringID( string );
		ni.display_mode = flags & 0xFF;
		ni.flags = 0xFF & ((flags >> 8) | NF_NOEXPIRE);

		// show this news message in color?
		if (Global.get_date() >= GameDate.ConvertIntDate(Global._patches.colored_news_date))
			ni.flags |= NF_INCOLOR;

		ni.type = (flags >>> 16) & 0xFF;
		ni.callback = (flags >>> 24);
		ni.data_a = new TileIndex( data_a );
		ni.data_b = new TileIndex( data_b );
		ni.date = Global.get_date();
		ni.isValid = valid;
		Global.COPY_OUT_DPARAM(ni.params, 0, ni.params.length);

		w = Window.FindWindowById(Window.WC_MESSAGE_HISTORY, 0);
		if (w == null) return;
		w.SetWindowDirty();
		w.vscroll.setCount(_total_news);
	}

	//public static void AddValidatedNewsItem(StringID string, int flags, int data_a, int data_b, ValidationProc *validation)
	static public void AddNewsItem(StringID string, int flags, int data_a, int data_b)
	{
		AddValidatedNewsItem(string.id, flags, data_a, data_b, null);
	}

	static public void AddNewsItem(int string, int flags, int data_a, int data_b)
	{
		AddValidatedNewsItem(string, flags, data_a, data_b, null);
	}



	static void InitNewsItemStructs()
	{
		_current_news = INVALID_NEWS;
		_oldest_news = 0;
		_latest_news = INVALID_NEWS;
		_forced_news = INVALID_NEWS;
		_total_news = 0;
	}

	static void DrawNewsBorder(final Window w)
	{
		int left = 0;
		int right = w.getWidth() - 1;
		int top = 0;
		int bottom = w.getHeight() - 1;

		Gfx.GfxFillRect(left, top, right, bottom, 0xF);

		Gfx.GfxFillRect(left, top, left, bottom, 0xD7);
		Gfx.GfxFillRect(right, top, right, bottom, 0xD7);
		Gfx.GfxFillRect(left, top, right, top, 0xD7);
		Gfx.GfxFillRect(left, bottom, right, bottom, 0xD7);

		Gfx.DrawString(left + 2, top + 1, Str.STR_00C6, 0);
	}

	static void NewsWindowProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_CREATE: { /* If chatbar is open at creation time, we need to go above it */
			final Window w1 = Window.FindWindowById(Window.WC_SEND_NETWORK_MSG, 0);
			// TODO [dz] what the hell is message?
			w.message = new WindowMessage(); // [dz]
			w.message.msg = (w1 != null) ? w1.getHeight() : 0;
		} break;

		case WE_PAINT: {
			final NewsItem ni = w.as_news_d().ni;
			ViewPort vp;

			switch (ni.display_mode) {
			case NM_NORMAL:
			case NM_THIN: {
				DrawNewsBorder(w);

				Gfx.DrawString(2, 1, Str.STR_00C6, 0);

				Global.SetDParam(0, ni.date);
				Gfx.DrawStringRightAligned(428, 1, Str.STR_01FF, 0);

				if (0 == (ni.flags & NF_VIEWPORT)) {
					Global.COPY_IN_DPARAM(0, ni.params, ni.params.length);
					Gfx.DrawStringMultiCenter(215, ni.display_mode == NM_NORMAL ? 76 : 56,
							ni.string_id.id, 426);
				} else {
					byte bk = Global._display_opt;
					Global._display_opt &= ~Global.DO_TRANS_BUILDINGS;
					w.DrawWindowViewport();
					Global._display_opt = bk;

					/* Shade the viewport into gray, or color */
					vp = w.getViewport();
					Gfx.GfxFillRect(vp.getLeft() - w.getLeft(), vp.getTop() - w.getTop(),
							vp.getLeft() - w.getLeft() + vp.getWidth() - 1, vp.getTop() - w.getTop() + vp.getHeight() - 1,
							(0 !=(ni.flags & NF_INCOLOR) ? 0x322 : 0x323) | Sprite.USE_COLORTABLE
							);

					Global.COPY_IN_DPARAM(0, ni.params, ni.params.length);
					Gfx.DrawStringMultiCenter(w.getWidth() / 2, 20, ni.string_id.id, 428);
				}
				break;
			}

			case NM_CALLBACK: {
				_draw_news_callback[ni.callback].accept(w);
				break;
			}

			default: {
				w.DrawWindowWidgets();
				if (0 == (ni.flags & NF_VIEWPORT)) {
					Global.COPY_IN_DPARAM(0, ni.params, ni.params.length);
					Gfx.DrawStringMultiCenter(140, 38, ni.string_id.id, 276);
				} else {
					w.DrawWindowViewport();
					Global.COPY_IN_DPARAM(0, ni.params, ni.params.length);
					Gfx.DrawStringMultiCenter(w.getWidth() / 2, w.getHeight() - 16, ni.string_id.id, 276);
				}
				break;
			}
			}
		} break;

		case WE_CLICK: {
			switch (e.widget) {
			case 1: {
				NewsItem ni = w.as_news_d().ni;
				w.DeleteWindow();
				ni.duration = 0;
				_forced_news = INVALID_NEWS;
			} break;
			case 0: {
				NewsItem ni = w.as_news_d().ni;
				if( 0 != (ni.flags & NF_VEHICLE) ) {
					Vehicle v = Vehicle.GetVehicle(ni.data_a.tile);
					ViewPort.ScrollMainWindowTo(v.getX_pos(), v.getY_pos());
				} else if( 0 != (ni.flags & NF_TILE)) {
					if (!ViewPort.ScrollMainWindowToTile(ni.data_a) && ni.data_b != null)
						ViewPort.ScrollMainWindowToTile(ni.data_b);
				}
			} break;
			}
		} break;

		case WE_KEYPRESS:
			if (e.keycode == Window.WKC_SPACE) {
				// Don't continue.
				e.cont = false;
				w.DeleteWindow();
			}
			break;

		case WE_MESSAGE: /* The chatbar has notified us that is was either created or closed */
			if( e.msg == WindowEvents.WE_CREATE.ordinal() )
				w.message.msg = e.wparam; 
			if( e.msg == WindowEvents.WE_DESTROY.ordinal() )
				w.message.msg = 0;
			
			break;

		case WE_TICK: { /* Scroll up newsmessages from the bottom in steps of 4 pixels */
			int diff;
			int y = Math.max(w.getTop() - 4, Hal._screen.height - w.getHeight() - 12 - w.message.msg);
			if (y == w.getTop()) return;

			if (w.getViewport() != null)
				w.getViewport().top += y - w.getTop();

			diff = Math.abs(w.getTop() - y);
			w.setTop(y);

			Gfx.SetDirtyBlocks(w.getLeft(), w.getTop() - diff, w.getLeft() + w.getWidth(), w.getTop() + w.getHeight());
		} break;
		default:
			break;
		}
	}

	// returns the correct index in the array
	// (to deal with overflows)
	static int increaseIndex(int i)
	{
		if (i == INVALID_NEWS)
			return 0;
		i++;
		if (i >= MAX_NEWS)
			i = i % MAX_NEWS;
		return i;
	}
	/*
	void AddNewsItem(StringID string, int flags, int data_a, int data_b)
	{
		NewsItem *ni;
		Window w;

		if (_game_mode == GM_MENU)
			return;

		// check the rare case that the oldest (to be overwritten) news item is open
		if (_total_news==MAX_NEWS && (_oldest_news == _current_news || _oldest_news == _forced_news))
			MoveToNexItem();

		_forced_news = INVALID_NEWS;
		if (_total_news < MAX_NEWS) _total_news++;

		// make sure our pointer isn't overflowing
		_latest_news = increaseIndex(_latest_news);

		// overwrite oldest news entry
		if (_oldest_news == _latest_news && _news_items[_oldest_news].string_id != 0)
			_oldest_news = increaseIndex(_oldest_news); // but make sure we're not overflowing here

		// add news to _latest_news
		ni = &_news_items[_latest_news];
		memset(ni, 0, sizeof(*ni));

		ni.string_id = string;
		ni.display_mode = flags;
		ni.flags = (flags >> 8) | NF_NOEXPIRE;

		// show this news message in color?
		if (_date >= ConvertIntDate(Global._patches.colored_news_date))
			ni.flags |= NF_INCOLOR;

		ni.type = (flags >> 16);
		ni.callback = (flags >> 24);
		ni.data_a = data_a;
		ni.data_b = data_b;
		ni.date = _date;
		COPY_OUT_DPARAM(ni.params, 0, lengthof(ni.params));

		w = FindWindowById(WC_MESSAGE_HISTORY, 0);
		if (w == null) return;
		SetWindowDirty(w);
		w.vscroll.count = _total_news;
	}

	/* To add a news item with an attached validation function. This validation function
	 * makes sure that the news item is not outdated when the newspaper pops up. * /
	void AddValidatedNewsItem(StringID string, int flags, int data_a, int data_b, ValidationProc *validation)
	{
		AddNewsItem(string, flags, data_a, data_b);
		_news_items[_latest_news].isValid = validation;
	}
	 */
	// don't show item if it's older than x days
	static final int _news_items_age[] = {60, 60, 90, 60, 90, 30, 150, 30, 90, 180};

	static final Widget _news_type13_widgets[] = {
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    15,     0,   429,     0,   169, 0x0, Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    15,     0,    10,     0,    11, 0x0, Str.STR_NULL),
			//new Widget(   WIDGETS_END),
	};

	static final WindowDesc _news_type13_desc = new WindowDesc(
			Window.WDP_CENTER, 476, 430, 170,
			Window.WC_NEWS_WINDOW, 0,
			WindowDesc.WDF_DEF_WIDGET,
			_news_type13_widgets,
			NewsItem::NewsWindowProc
			);

	static final Widget _news_type2_widgets[] = {
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    15,     0,   429,     0,   129, 0x0, Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    15,     0,    10,     0,    11, 0x0, Str.STR_NULL),
			//new Widget(   WIDGETS_END),
	};

	static final WindowDesc _news_type2_desc = new WindowDesc(
			Window.WDP_CENTER, 476, 430, 130,
			Window.WC_NEWS_WINDOW, 0,
			WindowDesc.WDF_DEF_WIDGET,
			_news_type2_widgets,
			NewsItem::NewsWindowProc
			);

	static final Widget _news_type0_widgets[] = {
			new Widget(       Window.WWT_PANEL,   Window.RESIZE_NONE,     5,     0,   279,    14,    86, 0x0,								Str.STR_NULL),
			new Widget(    Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,     5,     0,    10,     0,    13, Str.STR_00C5,					Str.STR_018B_CLOSE_WINDOW),
			new Widget(     Window.WWT_CAPTION,   Window.RESIZE_NONE,     5,    11,   279,     0,    13, Str.STR_012C_MESSAGE,	Str.STR_NULL),
			new Widget(           Window.WWT_6,   Window.RESIZE_NONE,     5,     2,   277,    16,    64, 0x0,								Str.STR_NULL),
			//new Widget(    WIDGETS_END),
	};

	static final WindowDesc _news_type0_desc = new WindowDesc(
			Window.WDP_CENTER, 476, 280, 87,
			Window.WC_NEWS_WINDOW, 0,
			WindowDesc.WDF_DEF_WIDGET,
			_news_type0_widgets,
			NewsItem::NewsWindowProc
			);

	static final Snd _news_sounds[] = {
			Snd.SND_1D_APPLAUSE,
			Snd.SND_1D_APPLAUSE,
			Snd.SND_NONE,
			Snd.SND_NONE,
			Snd.SND_NONE,
			Snd.SND_NONE,
			Snd.SND_1E_OOOOH,
			Snd.SND_NONE,
			Snd.SND_NONE,
			Snd.SND_NONE
	};

	/** Get the value of an item of the news-display settings. This is
	 * a little tricky since on/off/summary must use 2 bits to store the value
	 * @param item the item whose value is requested
	 * @return return the found value which is between 0-2
	 */
	static int GetNewsDisplayValue(int item)
	{
//		assert(item < 10 && BitOps.GB(Global._news_display_opt, item * 2, 2) <= 2);
		assert item < 10;
		assert BitOps.GB(Global._news_display_opt, item * 2, 2) <= 2;
		return BitOps.GB(Global._news_display_opt, item * 2, 2);
	}

	/** Set the value of an item in the news-display settings. This is
	 * a little tricky since on/off/summary must use 2 bits to store the value
	 * @param item the item whose value is being set
	 * @param val new value
	 */
	static  void SetNewsDisplayValue(int item, int val)
	{
		assert(item < 10 && val <= 2);
		Global._news_display_opt = BitOps.RETSB(Global._news_display_opt, item * 2, 2, val);
	}

	// open up an own newspaper window for the news item
	static void ShowNewspaper(NewsItem ni)
	{
		Window w;
		//SoundFx sound;
		int top;
		ni.flags &= ~(NF_NOEXPIRE | NF_FORCE_BIG);
		ni.duration = 555;

		Snd sound = _news_sounds[ni.type];
		if (sound != Snd.SND_NONE) Sound.SndPlayFx(sound);

		top = Hal._screen.height;
		switch (ni.display_mode) {
		case NM_NORMAL:
		case NM_CALLBACK: {
			_news_type13_desc.top = top;
			w = Window.AllocateWindowDesc(_news_type13_desc);
			if( 0 != (ni.flags & NF_VIEWPORT) )
				ViewPort.AssignWindowViewport( w, 2, 58, 0x1AA, 0x6E,
						ni.data_a.getTile() | ((0 != (ni.flags & NF_VEHICLE)) ? 0x80000000 : 0), 0);
			break;
		}

		case NM_THIN: {
			_news_type2_desc.top = top;
			w = Window.AllocateWindowDesc(_news_type2_desc);
			if( 0 != (ni.flags & NF_VIEWPORT) )
				ViewPort.AssignWindowViewport( w, 2, 58, 0x1AA, 0x46,
						ni.data_a.getTile() | ((0 != (ni.flags & NF_VEHICLE)) ? 0x80000000 : 0), 0);
			break;
		}

		default: {
			_news_type0_desc.top = top;
			w = Window.AllocateWindowDesc(_news_type0_desc, 0);
			if( 0 != (ni.flags & NF_VIEWPORT) )
				ViewPort.AssignWindowViewport(w, 3, 17, 0x112, 0x2F,
						ni.data_a.getTile() | ((0 != (ni.flags & NF_VEHICLE)) ? 0x80000000 : 0), 0);
			break;
		}
		}
		w.as_news_d().ni = _news_items[_forced_news == INVALID_NEWS ? _current_news : _forced_news];
		w.disableVpScroll();
	}

	// show news item in the ticker
	static void ShowTicker(final NewsItem ni)
	{
		if (Global._news_ticker_sound) Sound.SndPlayFx(Snd.SND_16_MORSE);

		Global._statusbar_news_item = new NewsItem(ni);
		Window w = Window.FindWindowById(Window.WC_STATUS_BAR, 0);
		if (w != null) w.as_def_d().data_1 = 360;
	}


	// Are we ready to show another news item?
	// Only if nothing is in the newsticker and no newspaper is displayed
	static boolean ReadyForNextItem()
	{
		final Window w;
		int item = (_forced_news == INVALID_NEWS) ? _current_news : _forced_news;
		NewsItem ni;

		if (item >= MAX_NEWS || item < 0) return true;
		ni = _news_items[item];

		// Ticker message
		// Check if the status bar message is still being displayed?
		w = Window.FindWindowById(Window.WC_STATUS_BAR, 0);
		if (w != null && w.as_def_d().data_1 > -1280) return false;

		// Newspaper message
		// Wait until duration reaches 0
		if (ni.duration != 0) {
			ni.duration--;
			return false;
		}

		// neither newsticker nor newspaper are running
		return true;
	}

	static void MoveToNexItem()
	{
		Window.DeleteWindowById(Window.WC_NEWS_WINDOW, 0);
		_forced_news = INVALID_NEWS;

		// if we're not at the last item, than move on
		if (_current_news != _latest_news) {
			NewsItem ni;

			_current_news = increaseIndex(_current_news);
			ni = _news_items[_current_news];

			// check the date, don't show too old items
			if (Global.get_date() - _news_items_age[ni.type] > ni.date) return;

			// execute the validation function to see if this item is still valid
			if (ni.isValid != null && !ni.isValid.test(ni.data_a.getTile(), ni.data_b.getTile())) return;

			switch (GetNewsDisplayValue(ni.type)) {
			case 0: { /* Off - show nothing only a small reminder in the status bar */
				Window w = Window.FindWindowById(Window.WC_STATUS_BAR, 0);

				if (w != null) {
					w.as_def_d().data_2 = 91;
					w.SetWindowDirty();
				}
				break;
			}

			case 1: /* Summary - show ticker, but if forced big, cascade to full */
				if (0 == (ni.flags & NF_FORCE_BIG)) {
					ShowTicker(ni);
					break;
				}
				/* Fallthrough */

			case 2: /* Full - show newspaper*/
				ShowNewspaper(ni);
				break;
			}
		}
	}

	static void NewsLoop()
	{
		// no news item yet
		if (_total_news == 0) return;

		if (ReadyForNextItem()) MoveToNexItem();
	}

	/* Do a forced show of a specific message */
	static void ShowNewsMessage(int i)
	{
		if (_total_news == 0) return;

		// Delete the news window
		Window.DeleteWindowById(Window.WC_NEWS_WINDOW, 0);

		// setup forced news item
		_forced_news = i;

		if (_forced_news != INVALID_NEWS) {
			NewsItem ni = _news_items[_forced_news];
			ni.duration = 555;
			ni.flags |= NF_NOEXPIRE | NF_FORCE_BIG;
			Window.DeleteWindowById(Window.WC_NEWS_WINDOW, 0);
			ShowNewspaper(ni);
		}
	}

	public static void ShowLastNewsMessage()
	{
		if (_forced_news == INVALID_NEWS) {
			ShowNewsMessage(_current_news);
		} else if (_forced_news != 0) {
			ShowNewsMessage(_forced_news - 1);
		} else {
			ShowNewsMessage(_total_news != MAX_NEWS ? _latest_news : MAX_NEWS - 1);
		}
	}


	/* return news by number, with 0 being the most
	recent news. Returns INVALID_NEWS if end of queue reached. */
	static int getNews(int i)
	{
		if (i >= _total_news) return INVALID_NEWS;

		if (_latest_news < i) {
			i = _latest_news + MAX_NEWS - i;
		} else {
			i = _latest_news - i;
		}

		i %= MAX_NEWS;
		return i;
	}

	/** Draw an unformatted news message truncated to a maximum length. If
	 * length exceeds maximum length it will be postfixed by '...'
	 * @param x,y position of the string
	 * @param color the color the string will be shown in
	 * @param ni NewsItem being printed
	 * @param maxw maximum width of string in pixels
	 */
	static void DrawNewsString(int x, int y, int color, final NewsItem ni, int maxw)
	{
		//char buffer[512], buffer2[512];
		String buffer;
		//char *ptr, *dest;
		StringID str;

		if (ni.display_mode == 3) {
			str = new StringID( _get_news_string_callback[ni.callback].apply(ni) );
		} else {
			Global.COPY_IN_DPARAM(0, ni.params, ni.params.length);
			str = ni.string_id;
		}

		buffer = Strings.GetString(str);
		/* Copy the just gotten string to another buffer to remove any formatting
		 * from it such as big fonts, etc. * /
		for (ptr = buffer, dest = buffer2; *ptr != '\0'; ptr++) {
			if (*ptr == '\r') {
				dest[0] = dest[1] = dest[2] = dest[3] = ' ';
				dest += 4;
			} else if ((byte)*ptr >= ' ' && ((byte)*ptr < 0x88 || (byte)*ptr >= 0x99)) {
		 *dest++ = *ptr;
			}
		}  */

		char[] ca = buffer.toCharArray();

		StringBuilder sb = new StringBuilder();
		for( char c : ca )
		{
			if (c == '\r') {
				sb.append("    ");
			} else if (c >= ' ' && (c < 0x88 || c >= 0x99)) {
				sb.append(c);
			}

		}

		//*dest = '\0';
		/* Truncate and show string; postfixed by '...' if neccessary */
		Gfx.DoDrawStringTruncated( sb.toString(), x, y, color, maxw);
	}


	static void MessageHistoryWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int y = 19;
			int p, show;

			w.DrawWindowWidgets();

			if (_total_news == 0) break;
			show = Math.min(_total_news, w.vscroll.getCap());

			for (p = w.vscroll.getPos(); p < w.vscroll.getPos() + show; p++) {
				// get news in correct order
				final NewsItem ni = _news_items[getNews(p)];

				Global.SetDParam(0, ni.date);
				Gfx.DrawString(4, y, Str.STR_SHORT_DATE, 12);

				DrawNewsString(82, y, 12, ni, w.getWidth() - 95);
				y += 12;
			}
			break;
		}

		case WE_CLICK:
			switch (e.widget) {
			case 3: {
				int y = (e.pt.y - 19) / 12;
				int p, q;


				/*if(false)
				{
					// === DEBUG code only
					for (p = 0; p < _total_news; p++) {
						NewsItem ni;
						//byte buffer[256];
						ni = _news_items[p];
						String s = GetNewsString(ni);
						Global.error("%i\t%i\t%s\n", p, ni.date, s);
					}
					//printf("=========================\n");
				}*/

				p = y + w.vscroll.getPos();
				if (p > _total_news - 1) break;

				if (_latest_news >= p) {
					q = _latest_news - p;
				} else {
					q = _latest_news + MAX_NEWS - p;
				}
				ShowNewsMessage(q);

				break;
			}
			}
			break;

		case WE_RESIZE:
			w.vscroll.setCap(w.vscroll.getCap() + e.diff.y / 12);
			break;
		default:
			break;
		}
	}

	static final Widget _message_history_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,    10,     0,    13, Str.STR_00C5,			Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    13,    11,   387,     0,    13, Str.STR_MESSAGE_HISTORY,	Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    13,   388,   399,     0,    13, 0x0,										Str.STR_STICKY_BUTTON),
			new Widget(     Window.WWT_IMGBTN,     Window.RESIZE_RB,    13,     0,   387,    14,   139, 0x0, Str.STR_MESSAGE_HISTORY_TIP),
			new Widget(  Window.WWT_SCROLLBAR,    Window.RESIZE_LRB,    13,   388,   399,    14,   127, 0x0, Str.STR_0190_SCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    13,   388,   399,   128,   139, 0x0, Str.STR_RESIZE_BUTTON),
			//new Widget(   WIDGETS_END),
	};

	static final WindowDesc _message_history_desc = new WindowDesc(
			240, 22, 400, 140,
			Window.WC_MESSAGE_HISTORY, 0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_message_history_widgets,
			NewsItem::MessageHistoryWndProc
			);

	public static void ShowMessageHistory()
	{
		Window w;

		Window.DeleteWindowById(Window.WC_MESSAGE_HISTORY, 0);
		w = Window.AllocateWindowDesc(_message_history_desc);

		if (w != null) {
			w.vscroll.setCap(10);
			w.vscroll.setCount(_total_news);
			w.resize.step_height = 12;
			w.resize.height = w.getHeight() - 12 * 6; // minimum of 4 items in the list, each item 12 high
			w.resize.step_width = 1;
			w.resize.width = 200; // can't make window any smaller than 200 pixel
			w.SetWindowDirty();
		}
	}

	/** Setup the disabled/enabled buttons in the message window
	 * If the value is 'off' disable the [<] widget, and enable the [>] one
	 * Same-wise for all the others. Starting value of 3 is the first widget
	 * group. These are grouped as [<][>] .. [<][>], etc.
	 */
	static void SetMessageButtonStates(Window w, int value, int element)
	{
		element *= 2;
		switch (value) {
		case 0: /* Off */
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, element + 3);
			w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, element + 3 + 1);
			break;
		case 1: /* Summary */
			w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, element + 3);
			w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, element + 3 + 1);
			break;
		case 2: /* Full */
			w.disabled_state = BitOps.RETSETBIT(w.disabled_state, element + 3 + 1);
			w.disabled_state = BitOps.RETCLRBIT(w.disabled_state, element + 3);
			break;
		default: assert false; //NOT_REACHED();
		}
	}


	private static final int message_opt[] = {Str.STR_OFF, Str.STR_SUMMARY, Str.STR_FULL, Str.INVALID_STRING};
	private static final int message_val[] = {0x0, 0x55555555, 0xAAAAAAAA}; // 0x555.. = 01010101010101010101 (all summary), 286.. 1010... (full)
	private static final int message_dis[] = 
		{
				(1 << 3) | (1 << 5) | (1 << 7) | (1 << 9)  | (1 << 11) | (1 << 13) | (1 << 15) | (1 << 17) | (1 << 19) | (1 << 21),
				0,
				(1 << 4) | (1 << 6) | (1 << 8) | (1 << 10) | (1 << 12) | (1 << 14) | (1 << 16) | (1 << 18) | (1 << 20) | (1 << 22),
		};

	static void MessageOptionsWndProc(Window w, WindowEvent e)
	{

		/* w.as_def_d().data_1 are stores the clicked state of the fake widgets
		 * w.as_def_d().data_2 stores state of the ALL on/off/summary button */
		switch (e.event) {
		case WE_CREATE: {
			int val = Global._news_display_opt;
			int i;
			//w.as_def_d().data_1 = w.as_def_d().data_2 = 0;
			w.custom = new def_d();

			// Set up the initial disabled buttons in the case of 'off' or 'full'
			for (i = 0; i != 10; i++, val >>= 2) SetMessageButtonStates(w, val & 0x3, i);
		} break;

		case WE_PAINT: {
			int val = Global._news_display_opt;
			//int click_state = w.as_def_d().data_1;
			int click_state = ((def_d)w.custom).data_1;
			int i, y;

			if (Global._news_ticker_sound) w.click_state = BitOps.RETSETBIT(w.click_state, 25);
			w.DrawWindowWidgets();
			Gfx.DrawStringCentered(185, 15, Str.STR_0205_MESSAGE_TYPES, 0);

			/* XXX - Draw the fake widgets-buttons. Can't add these to the widget-desc since
			 * openttd currently can only handle 32 widgets. So hack it *g* */
			for (i = 0, y = 26; i != 10; i++, y += 12, click_state >>= 1, val >>= 2) {
				//boolean clicked = !!(click_state & 1);
				int clicked = (click_state & 1) != 0 ? 1 : 0;

				Gfx.DrawFrameRect(13, y, 89, 11 + y, 3, clicked != 0 ? Window.FR_LOWERED : 0);
				Gfx.DrawStringCentered(
						((13 + 89 + 1) >> 1) + clicked, 
						((y + 11 + y + 1) >> 1) - 5 + clicked, message_opt[val & 0x3], 0x10);
				Gfx.DrawString(103, y + 1, i + Str.STR_0206_ARRIVAL_OF_FIRST_VEHICLE, 0);
			}

			Gfx.DrawString(  8, y + 9, message_opt[((def_d)w.custom).data_2], 0x10);
			Gfx.DrawString(103, y + 9, Str.STR_MESSAGES_ALL, 0);
			Gfx.DrawString(103, y + 9 + 12, Str.STR_MESSAGE_SOUND, 0);

		} break;

		case WE_CLICK:
			switch (e.widget) {
			case 2: /* Clicked on any of the fake widgets */
				if (e.pt.x > 13 && e.pt.x < 89 && e.pt.y > 26 && e.pt.y < 146) {
					int element = (e.pt.y - 26) / 12;
					int val = (GetNewsDisplayValue(element) + 1) % 3;

					SetMessageButtonStates(w, val, element);
					SetNewsDisplayValue( element, val);

					//w.as_def_d().data_1 |= (1 << element);
					((def_d)w.custom).data_1 |= (1 << element);
					//w.flags4 |= 5 << Window.WF_TIMEOUT_SHL; // XXX - setup unclick (fake widget)
					w.setTimeout(5);
					w.SetWindowDirty();
				}
				break;
			case 23: case 24: /* Dropdown menu for all settings */
				Window.ShowDropDownMenu( w, message_opt, ((def_d)w.custom).data_2, 24, 0, 0);
				break;
			case 25: /* Change ticker sound on/off */
				Global._news_ticker_sound = !Global._news_ticker_sound;
				w.click_state = BitOps.RETTOGGLEBIT(w.click_state, e.widget);
				w.InvalidateWidget(e.widget);
				break;
			default: { /* Clicked on the [<] .. [>] widgets */
				int wid = e.widget;
				if (wid > 2 && wid < 23) {
					int element = (wid - 3) / 2;
					int val = (GetNewsDisplayValue(element) + (0 != (wid & 1) ? -1 : 1)) % 3;

					SetMessageButtonStates(w, val, element);
					SetNewsDisplayValue(element, val);
					w.SetWindowDirty();
				}
			} break;
			} break;

		case WE_DROPDOWN_SELECT: /* Select all settings for newsmessages */
			((def_d)w.custom).data_2 = e.index;
			Global._news_display_opt = message_val[((def_d)w.custom).data_2];
			w.disabled_state = message_dis[((def_d)w.custom).data_2];
			w.SetWindowDirty();
			break;

		case WE_TIMEOUT: /* XXX - Hack to animate 'fake' buttons */
			((def_d)w.custom).data_1 = 0;
			w.SetWindowDirty();
			break;
		default:
			break;

		}
	}

	static final Widget _message_options_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,   10,     0,    13, Str.STR_00C5,             Str.STR_018B_CLOSE_WINDOW),
			new Widget(     Window.WWT_CAPTION,   Window.RESIZE_NONE,    13,    11,  409,     0,    13, Str.STR_0204_MESSAGE_OPTIONS, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(       Window.WWT_PANEL,   Window.RESIZE_NONE,    13,     0,  409,    14,   184, Str.STR_NULL,             Str.STR_NULL),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,    26,    37, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,    26,    37, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,    38,    49, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,    38,    49, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,    50,    61, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,    50,    61, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,    62,    73, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,    62,    73, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,    74,    85, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,    74,    85, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,    86,    97, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,    86,    97, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,    98,   109, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,    98,   109, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,   110,   121, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,   110,   121, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,   122,   133, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,   122,   133, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,     4,   12,   134,   145, Sprite.SPR_ARROW_LEFT,       Str.STR_HSCROLL_BAR_SCROLLS_LIST),
			new Widget(  Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,     3,    90,   98,   134,   145, Sprite.SPR_ARROW_RIGHT,      Str.STR_HSCROLL_BAR_SCROLLS_LIST),

			new Widget(       Window.WWT_PANEL,   Window.RESIZE_NONE,     3,     4,   86,   154,   165, Str.STR_NULL,             Str.STR_NULL),
			new Widget(     Window.WWT_TEXTBTN,   Window.RESIZE_NONE,     3,    87,   98,   154,   165, Str.STR_0225,             Str.STR_NULL),
			new Widget(           Window.WWT_4,   Window.RESIZE_NONE,     3,     4,   98,   166,   177, Str.STR_02DB_OFF,         Str.STR_NULL),

	};

	static final WindowDesc _message_options_desc = new WindowDesc(
			270, 22, 410, 185,
			Window.WC_GAME_OPTIONS, 0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_message_options_widgets,
			NewsItem::MessageOptionsWndProc
			);

	public static void ShowMessageOptions()
	{
		Window.DeleteWindowById(Window.WC_GAME_OPTIONS, 0);
		Window.AllocateWindowDesc(_message_options_desc, 0);
	}



	static final DrawNewsCallbackProc[] _draw_news_callback = {
			Engine::DrawNewsNewTrainAvail,    /* DNC_TRAINAVAIL */
			Engine::DrawNewsNewRoadVehAvail,  /* DNC_ROADAVAIL */
			Engine::DrawNewsNewShipAvail,     /* DNC_SHIPAVAIL */
			Engine::DrawNewsNewAircraftAvail, /* DNC_AIRCRAFTAVAIL */
			Economy::DrawNewsBankrupcy,        /* DNC_BANKRUPCY */
	};

	static final GetNewsStringCallbackProc _get_news_string_callback[] = {
			Engine::GetNewsStringNewTrainAvail,    /* DNC_TRAINAVAIL */
			Engine::GetNewsStringNewRoadVehAvail,  /* DNC_ROADAVAIL */
			Engine::GetNewsStringNewShipAvail,     /* DNC_SHIPAVAIL */
			Engine::GetNewsStringNewAircraftAvail, /* DNC_AIRCRAFTAVAIL */
			Economy::GetNewsStringBankrupcy,        /* DNC_BANKRUPCY */
	};





	public StringID makeString() 
	{
		if (display_mode == 3) {
			return new StringID( NewsItem._get_news_string_callback[callback].apply(this) );
		} else {
			Global.COPY_IN_DPARAM(0, params, params.length);
			return string_id;
		}
	}


	public StringID getString_id() {
		return string_id;
	}


}



//typedef void DrawNewsCallbackProc(Window *w);
@FunctionalInterface
interface DrawNewsCallbackProc extends Consumer<Window> { }

//typedef StringID GetNewsStringCallbackProc(const NewsItem *ni);
@FunctionalInterface
interface GetNewsStringCallbackProc extends Function<NewsItem,Integer> { }



/*

enum NewsMode {
	NM_SMALL = 0,
			NM_NORMAL = 1,
			NM_THIN = 2,
			NM_CALLBACK = 3,
};

enum NewsFlags {
	NF_VIEWPORT = 1,
			NF_TILE = 4,
			NF_VEHICLE = 8,
			NF_FORCE_BIG = 0x10,
			NF_NOEXPIRE = 0x20,
			NF_INCOLOR = 0x40,
};
 */


