package com.dzavalishin.util;

import com.dzavalishin.enums.GameModes;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.Str;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.xui.DrawPixelInfo;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.ViewPort;

public class ScreenShot 
{
	static String _screenshot_format_name;
	public static int _num_screenshot_formats;
	public static int _cur_screenshot_format;


	static ScreenshotFormat [] _screenshot_formats = {
			new JpgScreenshotFormat(),
			new PngScreenshotFormat(),
	};
	
	//************************************************
	//*** GENERIC SCREENSHOT CODE
	//************************************************


	public static void InitializeScreenshotFormats()
	{
		int i, j;
		for (i = 0, j = 0; i != _screenshot_formats.length; i++)
			if (_screenshot_format_name != null && _screenshot_format_name.equals(_screenshot_formats[i].extension)) {
				j = i;
				break;
			}
		_cur_screenshot_format = j;
		_num_screenshot_formats = _screenshot_formats.length;
	}

	static String GetScreenshotFormatDesc(int i)
	{
		return _screenshot_formats[i].name;
	}

	public static void SetScreenshotFormat(int i)
	{
		_cur_screenshot_format = i;
		_screenshot_format_name = _screenshot_formats[i].extension;
	}

	// screenshot generator that dumps the current video buffer
	static void CurrentScreenCallback(Object userdata, Pixel buf, int y, int pitch, int n)
	{
		Pixel src = new Pixel(Hal._screen.dst_ptr);
		buf.copyFrom(Hal._screen.dst_ptr, n);
		for (; n > 0; --n) {
			//memcpy(buf, Hal._screen.dst_ptr + y * _screen.pitch, _screen.width);
			src.setPos(y * Hal._screen.pitch);
			buf.copyFrom(src, Hal._screen.width);
			++y;
			//buf += pitch;
			buf.shift(pitch);
		}
	}

	// generate a large piece of the world
	static void LargeWorldCallback(Object userdata, Pixel buf, int y, int pitch, int n)
	{
		ViewPort vp = (ViewPort)userdata;
		DrawPixelInfo dpi = new DrawPixelInfo();

		DrawPixelInfo old_dpi = Hal._cur_dpi;
		Hal._cur_dpi = dpi;

		dpi.dst_ptr = buf;
		dpi.height = n;
		dpi.width = vp.getWidth();
		dpi.pitch = pitch;
		dpi.zoom = 0;
		dpi.left = 0;
		dpi.top = y;

		int left = 0;
		while (vp.getWidth() - left != 0) {
			int wx = Integer.min(vp.getWidth() - left, 1600);
			left += wx;

			final int zoom = vp.getZoom();
			ViewPort.ViewportDoDraw(vp,
				((left - wx - vp.left) << zoom) + vp.getVirtual_left(),
				((y - vp.top) << zoom) + vp.getVirtual_top(),
				((left - vp.left) << zoom) + vp.getVirtual_left(),
				(((y + n) - vp.top) << zoom) + vp.getVirtual_top()
			);
		}

		Hal._cur_dpi = old_dpi;
	}

	static String filename; //[256];
	
	static String MakeScreenshotName(final String ext)
	{
		if (Global._game_mode == GameModes.GM_EDITOR || Global._game_mode == GameModes.GM_MENU
				|| PlayerID.getLocal().isSpectator()) {
			ScreenShot._screenshot_name = "screenshot";
		} else {
			final Player p = PlayerID.getLocal().GetPlayer();
			Global.SetDParam(0, p.getName_1());
			Global.SetDParam(1, p.getName_2());
			Global.SetDParam(2, Global.get_date());
			ScreenShot._screenshot_name = Strings.GetString(Str.STR_4004);
		}

		//base = strchr(Global._screenshot_name, 0);		base[0] = '.'; strcpy(base + 1, ext);
		
		//Global._screenshot_name += ".";
		//Global._screenshot_name += ext;

		int serial = 0;
		for (;;) {
			filename = String.format("%s%s.%s", Global._path.personal_dir, ScreenShot._screenshot_name, ext);
			if (!FileIO.FileExists(filename))
				break;
			//base = String.format(" #%d.%s", ++serial, ext);
			filename = String.format("%s%s_%d.%s", Global._path.personal_dir, ScreenShot._screenshot_name, ++serial, ext);			
		}

		return filename;
	}

	public static boolean MakeScreenshot()
	{
		final ScreenshotFormat sf = _screenshot_formats[_cur_screenshot_format];
		return sf.proc(MakeScreenshotName(sf.extension), ScreenShot::CurrentScreenCallback, null, Hal._screen.width, Hal._screen.height, 8, Gfx._cur_palette);
	}

	public static boolean MakeWorldScreenshot(int left, int top, int width, int height, int zoom)
	{
		ViewPort vp = new ViewPort(left, top, width, height, zoom);
		final ScreenshotFormat sf = _screenshot_formats[_cur_screenshot_format];
		return sf.proc(MakeScreenshotName(sf.extension), ScreenShot::LargeWorldCallback, vp, vp.getWidth(), vp.getHeight(), 8, Gfx._cur_palette);
	}

	public static String _screenshot_name;
	public static int _make_screenshot;
	
	
}

@FunctionalInterface
interface ScreenshotCallback
{
	void get(Object userdata, Pixel buf, int y, int pitch, int n);
}
