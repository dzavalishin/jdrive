static int DoDrawString(final String string, int x, int y, int real_color)
{
	DrawPixelInfo dpi = Hal._cur_dpi;
	int base = _stringwidth_base;
	char c;
	byte color;
	int xo = x, yo = y;
	int sp = 0;
	char sc[] = string.toCharArray();

	color = (byte) (real_color & 0xFF);

	if (color != 0xFE) {
		if (x >= dpi.left + dpi.width ||
				x + Hal._screen.width*2 <= dpi.left ||
				y >= dpi.top + dpi.height ||
				y + Hal._screen.height <= dpi.top)
			return x;

		if (color != 0xFF) {
			switch_color:;
			if(0 != (real_color & IS_PALETTE_COLOR) ) {
				_string_colorremap[1] = color;
				_string_colorremap[2] = (byte) 215;
			} else {
				_string_colorremap[1] = _string_colormap[color].text;
				_string_colorremap[2] = _string_colormap[color].shadow;
			}
			_color_remap_ptr = _string_colorremap;
		}
	}

	check_bounds:
		if (y + 19 <= dpi.top || dpi.top + dpi.height <= y) {
			skip_char:;
			for(;;) {
				c = sc[sp++]; //*string++;
				if (c < ASCII_LETTERSTART) goto skip_cont;
			}
		}

	for(;;) {
		c = sc[sp++]; //*string++;
		skip_cont:;
		if (c == 0) {
			_stringwidth_out = base;
			return x;
		}
		if (c >= ASCII_LETTERSTART) {
			if (x >= dpi.left + dpi.width) goto skip_char;
			if (x + 26 >= dpi.left) {
				GfxMainBlitter(GetSprite(base 
					+ 2 + c - ASCII_LETTERSTART), x, y, 1);
			}
			x += GetCharacterWidth(base + c);
		} else if (c == ASCII_NL) { // newline = {}
			x = xo;
			y += 10;
			if (base != 0) {
				y -= 4;
				if (base != 0xE0)
					y += 12;
			}
			goto check_bounds;
		} else if (c >= ASCII_COLORSTART) { // change color?
			color = (byte)(c - ASCII_COLORSTART);
			goto switch_color;
		} else if (c == ASCII_SETX) { // {SETX}
			x = xo + (byte)sc[sp++]; //*string++;
		} else if (c == ASCII_SETXY) {// {SETXY}
			x = xo + (byte)sc[sp++]; // *string++;
			y = yo + (byte)sc[sp++]; // *string++;
		} else if (c == ASCII_TINYFONT) { // {TINYFONT}
			base = 0xE0;
		} else if (c == ASCII_BIGFONT) { // {BIGFONT}
			base = 0x1C0;
		} else {
			Global.error("Unknown string command character %d\n", c);
		}
	}
}
