package game.util;

import game.Global;
import game.Hal;
import game.Player;
import game.Str;
import game.enums.GameModes;
import game.ids.PlayerID;
import game.xui.DrawPixelInfo;
import game.xui.Gfx;
import game.xui.ViewPort;

public class ScreenShot {


	static String _screenshot_format_name;
	static int _num_screenshot_formats;
	static int _cur_screenshot_format;

	// called by the ScreenShot proc to generate screenshot lines.
	//typedef void ScreenshotCallback(void *userdata, Pixel *buf, int y, int pitch, int n);
	//typedef boolean ScreenshotHandlerProc(final String name, ScreenshotCallback *callb, void *userdata, int w, int h, int pixelformat, final Colour *palette);

	static ScreenshotFormat [] _screenshot_formats = {
			new PngScreenshotFormat()
	};
	
	//************************************************
	//*** SCREENSHOT CODE FOR WINDOWS BITMAP (.BMP)
	//************************************************
	/*
	typedef struct BitmapFileHeader {
		uint16 type;
		uint32 size;
		uint32 reserved;
		uint32 off_bits;
	} GCC_PACK BitmapFileHeader;
	assert_compile(sizeof(BitmapFileHeader) == 14);

	#if defined(_MSC_VER) || defined(__WATCOMC__)
	#pragma pack(pop)
	#endif

	typedef struct BitmapInfoHeader {
		uint32 size;
		int32 width, height;
		uint16 planes, bitcount;
		uint32 compression, sizeimage, xpels, ypels, clrused, clrimp;
	} BitmapInfoHeader;
	assert_compile(sizeof(BitmapInfoHeader) == 40);

	typedef struct RgbQuad {
		byte blue, green, red, reserved;
	} RgbQuad;
	assert_compile(sizeof(RgbQuad) == 4);

	// generic .BMP writer
	static boolean MakeBmpImage(final String name, ScreenshotCallback *callb, void *userdata, int w, int h, int pixelformat, final Colour *palette)
	{
		BitmapFileHeader bfh;
		BitmapInfoHeader bih;
		RgbQuad rq[256];
		Pixel *buff;
		FILE *f;
		int i, padw;
		int n, maxlines;

		// only implemented for 8bit images so far.
		if (pixelformat != 8)
			return false;

		f = fopen(name, "wb");
		if (f == NULL) return false;

		// each scanline must be aligned on a 32bit boundary
		padw = ALIGN(w, 4);

		// setup the file header
		bfh.type = TO_LE16('MB');
		bfh.size = TO_LE32(sizeof(bfh) + sizeof(bih) + sizeof(RgbQuad) * 256 + padw * h);
		bfh.reserved = 0;
		bfh.off_bits = TO_LE32(sizeof(bfh) + sizeof(bih) + sizeof(RgbQuad) * 256);

		// setup the info header
		bih.size = TO_LE32(sizeof(BitmapInfoHeader));
		bih.width = TO_LE32(w);
		bih.height = TO_LE32(h);
		bih.planes = TO_LE16(1);
		bih.bitcount = TO_LE16(8);
		bih.compression = 0;
		bih.sizeimage = 0;
		bih.xpels = 0;
		bih.ypels = 0;
		bih.clrused = 0;
		bih.clrimp = 0;

		// convert the palette to the windows format
		for (i = 0; i != 256; i++) {
			rq[i].red   = palette[i].r;
			rq[i].green = palette[i].g;
			rq[i].blue  = palette[i].b;
			rq[i].reserved = 0;
		}

		// write file header and info header and palette
		if (fwrite(&bfh, sizeof(bfh), 1, f) != 1) return false;
		if (fwrite(&bih, sizeof(bih), 1, f) != 1) return false;
		if (fwrite(rq, sizeof(rq), 1, f) != 1) return false;

		// use by default 64k temp memory
		maxlines = clamp(65536 / padw, 16, 128);

		// now generate the bitmap bits
		buff = malloc(padw * maxlines); // by default generate 128 lines at a time.
		if (buff == NULL) {
			fclose(f);
			return false;
		}
		memset(buff, 0, padw * maxlines); // zero the buffer to have the padding bytes set to 0

		// start at the bottom, since bitmaps are stored bottom up.
		do {
			// determine # lines
			n = min(h, maxlines);
			h -= n;

			// render the pixels
			callb(userdata, buff, h, padw, n);

			// write each line
			while (n)
				if (fwrite(buff + (--n) * padw, padw, 1, f) != 1) {
					free(buff);
					fclose(f);
					return false;
				}
		} while (h != 0);

		free(buff);
		fclose(f);

		return true;
	}

	//********************************************************
	//*** SCREENSHOT CODE FOR PORTABLE NETWORK GRAPHICS (.PNG)
	//********************************************************
	/*
	static void PNGAPI png_my_error(png_structp png_ptr, png_const_charp message)
	{
		DEBUG(misc, 0) ("ERROR(libpng): %s - %s", message, (String )png_get_error_ptr(png_ptr));
		longjmp(png_ptr.jmpbuf, 1);
	}

	static void PNGAPI png_my_warning(png_structp png_ptr, png_const_charp message)
	{
		DEBUG(misc, 0) ("WARNING(libpng): %s - %s", message, (String )png_get_error_ptr(png_ptr));
	}

	static boolean MakePNGImage(final String name, ScreenshotCallback *callb, void *userdata, int w, int h, int pixelformat, final Colour *palette)
	{
		png_color rq[256];
		Pixel *buff;
		FILE *f;
		int i, y, n;
		int maxlines;
		png_structp png_ptr;
		png_infop info_ptr;

		// only implemented for 8bit images so far.
		if (pixelformat != 8)
			return false;

		f = fopen(name, "wb");
		if (f == NULL) return false;

		png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, (String )name, png_my_error, png_my_warning);

		if (png_ptr == NULL) {
			fclose(f);
			return false;
		}

		info_ptr = png_create_info_struct(png_ptr);
		if (info_ptr == NULL) {
			png_destroy_write_struct(&png_ptr, (png_infopp)NULL);
			fclose(f);
			return false;
		}

		if (setjmp(png_jmpbuf(png_ptr))) {
			png_destroy_write_struct(&png_ptr, &info_ptr);
			fclose(f);
			return false;
		}

		png_init_io(png_ptr, f);

		png_set_filter(png_ptr, 0, PNG_FILTER_NONE);

		png_set_IHDR(png_ptr, info_ptr, w, h, pixelformat, PNG_COLOR_TYPE_PALETTE,
			PNG_INTERLACE_NONE, PNG_COMPRESSION_TYPE_DEFAULT, PNG_FILTER_TYPE_DEFAULT);

		// convert the palette to the .PNG format.
		for (i = 0; i != 256; i++) {
			rq[i].red   = palette[i].r;
			rq[i].green = palette[i].g;
			rq[i].blue  = palette[i].b;
		}

		png_set_PLTE(png_ptr, info_ptr, rq, 256);
		png_write_info(png_ptr, info_ptr);
		png_set_flush(png_ptr, 512);

		// use by default 64k temp memory
		maxlines = clamp(65536 / w, 16, 128);

		// now generate the bitmap bits
		buff = malloc(w * maxlines); // by default generate 128 lines at a time.
		if (buff == NULL) {
			png_destroy_write_struct(&png_ptr, &info_ptr);
			fclose(f);
			return false;
		}
		memset(buff, 0, w * maxlines); // zero the buffer to have the padding bytes set to 0

		y = 0;
		do {
			// determine # lines to write
			n = min(h - y, maxlines);

			// render the pixels into the buffer
			callb(userdata, buff, y, w, n);
			y += n;

			// write them to png
			for (i = 0; i != n; i++)
				png_write_row(png_ptr, buff + i * w);
		} while (y != h);

		png_write_end(png_ptr, info_ptr);
		png_destroy_write_struct(&png_ptr, &info_ptr);

		free(buff);
		fclose(f);
		return true;
	}
	*/

	//************************************************
	//*** SCREENSHOT CODE FOR ZSOFT PAINTBRUSH (.PCX)
	//************************************************
	/*
	typedef struct {
		byte manufacturer;
		byte version;
		byte rle;
		byte bpp;
		uint32 unused;
		uint16 xmax, ymax;
		uint16 hdpi, vdpi;
		byte pal_small[16*3];
		byte reserved;
		byte planes;
		uint16 pitch;
		uint16 cpal;
		uint16 width;
		uint16 height;
		byte filler[54];
	} PcxHeader;
	assert_compile(sizeof(PcxHeader) == 128);

	static boolean MakePCXImage(final String name, ScreenshotCallback *callb, void *userdata, int w, int h, int pixelformat, final Colour *palette)
	{
		Pixel *buff;
		FILE *f;
		int maxlines;
		int y;
		PcxHeader pcx;

		if (pixelformat != 8 || w == 0)
			return false;

		f = fopen(name, "wb");
		if (f == NULL) return false;

		memset(&pcx, 0, sizeof(pcx));

		// setup pcx header
		pcx.manufacturer = 10;
		pcx.version = 5;
		pcx.rle = 1;
		pcx.bpp = 8;
		pcx.xmax = TO_LE16(w - 1);
		pcx.ymax = TO_LE16(h - 1);
		pcx.hdpi = TO_LE16(320);
		pcx.vdpi = TO_LE16(320);

		pcx.planes = 1;
		pcx.cpal = TO_LE16(1);
		pcx.width = pcx.pitch = TO_LE16(w);
		pcx.height = TO_LE16(h);

		// write pcx header
		if (fwrite(&pcx, sizeof(pcx), 1, f) != 1) {
			fclose(f);
			return false;
		}

		// use by default 64k temp memory
		maxlines = clamp(65536 / w, 16, 128);

		// now generate the bitmap bits
		buff = malloc(w * maxlines); // by default generate 128 lines at a time.
		if (buff == NULL) {
			fclose(f);
			return false;
		}
		memset(buff, 0, w * maxlines); // zero the buffer to have the padding bytes set to 0

		y = 0;
		do {
			// determine # lines to write
			int n = min(h - y, maxlines);
			int i;

			// render the pixels into the buffer
			callb(userdata, buff, y, w, n);
			y += n;

			// write them to pcx
			for (i = 0; i != n; i++) {
				final Pixel* bufp = buff + i * w;
				byte runchar = bufp[0];
				int runcount = 1;
				int j;

				// for each pixel...
				for (j = 1; j < w; j++) {
					Pixel ch = bufp[j];

					if (ch != runchar || runcount >= 0x3f) {
						if (runcount > 1 || (runchar & 0xC0) == 0xC0)
							if (fputc(0xC0 | runcount, f) == EOF) {
								free(buff);
								fclose(f);
								return false;
							}
						if (fputc(runchar, f) == EOF) {
							free(buff);
							fclose(f);
							return false;
						}
						runcount = 0;
						runchar = ch;
					}
					runcount++;
				}

				// write remaining bytes..
				if (runcount > 1 || (runchar & 0xC0) == 0xC0)
					if (fputc(0xC0 | runcount, f) == EOF) {
						free(buff);
						fclose(f);
						return false;
					}
				if (fputc(runchar, f) == EOF) {
					free(buff);
					fclose(f);
					return false;
				}
			}
		} while (y != h);

		free(buff);

		// write 8-bit color palette
		if (fputc(12, f) == EOF) {
			fclose(f);
			return false;
		}

		{assert_compile(sizeof(*palette) == 3);}
		if (fwrite(palette, 256 * sizeof(*palette), 1, f) != 1) {
			fclose(f);
			return false;
		}
		fclose(f);

		return true;
	}
	*/
	//************************************************
	//*** GENERIC SCREENSHOT CODE
	//************************************************


	void InitializeScreenshotFormats()
	{
		int i, j;
		for (i = 0, j = 0; i != _screenshot_formats.length; i++)
			if (_screenshot_format_name.equals(_screenshot_formats[i].extension)) {
				j = i;
				break;
			}
		_cur_screenshot_format = j;
		_num_screenshot_formats = _screenshot_formats.length;
	}

	final String GetScreenshotFormatDesc(int i)
	{
		return _screenshot_formats[i].name;
	}

	void SetScreenshotFormat(int i)
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
		String base;
		int serial;

		if (Global._game_mode == GameModes.GM_EDITOR || Global._game_mode == GameModes.GM_MENU 
				|| PlayerID.getLocal().isSpectator()) { 
			Global._screenshot_name = "screenshot";
		} else {
			final Player p = PlayerID.getLocal().GetPlayer(); 
			Global.SetDParam(0, p.getName_1());
			Global.SetDParam(1, p.getName_2());
			Global.SetDParam(2, Global.get_date());
			Global._screenshot_name = Strings.GetString(Str.STR_4004);
		}

		//base = strchr(Global._screenshot_name, 0);		base[0] = '.'; strcpy(base + 1, ext);
		
		Global._screenshot_name += ".";
		Global._screenshot_name += ext;

		serial = 0;
		for (;;) {
			filename = String.format("%s%s", Global._path.personal_dir, Global._screenshot_name);
			if (!FileIO.FileExists(filename))
				break;
			base = String.format(" #%d.%s", ++serial, ext);
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
	
	
}

@FunctionalInterface
interface ScreenshotCallback
{
	void get(Object userdata, Pixel buf, int y, int pitch, int n);
}
