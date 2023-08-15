package com.dzavalishin.game;

import com.dzavalishin.ids.StringID;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.ConsoleFactory;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.struct.TextMessage;
import com.dzavalishin.util.Pixel;
import com.dzavalishin.util.Strings;
import com.dzavalishin.xui.CursorVars;
import com.dzavalishin.xui.DrawPixelInfo;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.ViewPort;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class TextEffect 
{

	StringID string_id;
	int x;
	int y;
	int right;
	int bottom;
	int duration;
	int params_1;
	int params_2;





	private static final int MAX_ANIMATED_TILES = 256;
	private static final int MAX_CHAT_MESSAGES = 10;

	static final List<TextEffect> _text_effect_list = new ArrayList<>();
	static final List<TextMessage> _text_message_list = new ArrayList<>();
	
	static int _textmessage_width = 0;
	static boolean _textmessage_dirty = true;
	static boolean _textmessage_visible = false;

	static final int _textmessage_box_left = 10; // Pixels from left
	static final int _textmessage_box_y = 150;  // Height of box
	static final int _textmessage_box_bottom = 30; // Pixels from bottom
	static final int _textmessage_box_max_width = 400; // Max width of box

	static final byte _textmessage_backup[] = new byte[150 * 400]; // (y * max_width)


	/** 
	 * 
	 * @param color
	 * @param duration is in game-days
	 * @param message
	 * @param args
	 */
	public static void AddTextMessage(int color, int duration, final String message, Object ... args)
	{
		String buf;
		//int length;
		//int i;

		buf = String.format(message, args);


		/* Special color magic */
		if ((color & 0xFF) == 0xC9) color = 0x1CA;

		/* Cut the message till it fits inside the chatbox */
		//length = buf.length();
		//while (GetStringWidth(buf) > _textmessage_width - 9) 
		//	buf[--length] = '\0';


		_text_message_list.add( new TextMessage(buf, color, Global.get_date() + duration) );
		
		
		while(_text_message_list.size() > MAX_CHAT_MESSAGES)
			_text_message_list.remove(0);
		
		_textmessage_dirty = true;
	}

	public static void InitTextMessage()
	{
		_textmessage_width = _textmessage_box_max_width;
	}

	// Hide the textbox
	public static void UndrawTextMessage()
	{
		CursorVars _cursor = Hal._cursor;
		if (_textmessage_visible) {
			// Sometimes we also need to hide the cursor
			//   This is because both textmessage and the cursor take a shot of the
			//   screen before drawing.
			//   Now the textmessage takes his shot and paints his data before the cursor
			//   does, so in the shot of the cursor is the screen-data of the textmessage
			//   included when the cursor hangs somewhere over the textmessage. To
			//   avoid wrong repaints, we undraw the cursor in that case, and everything
			//   looks nicely ;)
			// (and now hope this story above makes sense to you ;))

			/*if (_cursor.isVisible()) {
				if (_cursor.draw_pos.x + _cursor.draw_size.x >= _textmessage_box_left &&
					_cursor.draw_pos.x <= _textmessage_box_left + _textmessage_width &&
					_cursor.draw_pos.y + _cursor.draw_size.y >= Hal._screen.height - _textmessage_box_bottom - _textmessage_box_y &&
					_cursor.draw_pos.y <= Hal._screen.height - _textmessage_box_bottom) {
					Gfx.UndrawMouseCursor();
				}
			}*/

			if (_cursor.isVisible()) 
			{
				if (_cursor.xBetween( _textmessage_box_left,  _textmessage_box_left + _textmessage_width) &&
						_cursor.yBetween(Hal._screen.height - _textmessage_box_bottom - _textmessage_box_y, Hal._screen.height - _textmessage_box_bottom) ) 
				{
					Gfx.UndrawMouseCursor();
				}
			}

			_textmessage_visible = false;
			// Put our 'shot' back to the screen
			Gfx.memcpy_pitch(
					new Pixel(Hal._screen.dst_ptr, _textmessage_box_left + (Hal._screen.height-_textmessage_box_bottom-_textmessage_box_y) * Hal._screen.pitch ),	
					new Pixel(_textmessage_backup),
					_textmessage_width, _textmessage_box_y, _textmessage_width, Hal._screen.pitch);

			// And make sure it is updated next time
			Global.hal.make_dirty(_textmessage_box_left, Hal._screen.height-_textmessage_box_bottom-_textmessage_box_y, _textmessage_width, _textmessage_box_y);

			_textmessage_dirty = true;
		}
	}

	// Check if a message is expired every day
	public static void TextMessageDailyLoop()
	{
		for( ListIterator<TextMessage> i = _text_message_list.listIterator(); i.hasNext(); )
		{
			TextMessage m = i.next();
			
			if (Global.get_date() > m.end_date) {
				i.remove();
				_textmessage_dirty = true;
			}
		}
	}

	// Draw the textmessage-box
	public static void DrawTextMessage()
	{
		//boolean has_message;

		if (!_textmessage_dirty) return;

		// First undraw if needed
		UndrawTextMessage();

		Optional<Console> currentConsole = ConsoleFactory.INSTANCE.getCurrentConsole();
		if (currentConsole.isPresent() && currentConsole.get().isFullSize())
			return;

		if(_text_message_list.isEmpty()) return;
		
		// Make a copy of the screen as it is before painting (for undraw)
		Gfx.memcpy_pitch(
				new Pixel(_textmessage_backup),
				new Pixel(Hal._screen.dst_ptr, _textmessage_box_left + (Hal._screen.height-_textmessage_box_bottom-_textmessage_box_y) * Hal._screen.pitch),
				_textmessage_width, _textmessage_box_y, Hal._screen.pitch, _textmessage_width);

		// Switch to _screen painting
		Hal._cur_dpi = Hal._screen;

		int j = 0;
		
		// Paint the messages
		for(TextMessage m : _text_message_list)
		{
			j++;
			Gfx.GfxFillRect(_textmessage_box_left, Hal._screen.height-_textmessage_box_bottom-j*13-2, _textmessage_box_left+_textmessage_width - 1, Hal._screen.height-_textmessage_box_bottom-j*13+10, /* black, but with some alpha */ 0x322 | Sprite.USE_COLORTABLE);

			Gfx.DoDrawString(m.message, _textmessage_box_left + 2, Hal._screen.height - _textmessage_box_bottom - j * 13 - 1, 0x10);
			Gfx.DoDrawString(m.message, _textmessage_box_left + 3, Hal._screen.height - _textmessage_box_bottom - j * 13, m.color);
		}
		
		// Make sure the data is updated next flush
		Global.hal.make_dirty(_textmessage_box_left, Hal._screen.height-_textmessage_box_bottom-_textmessage_box_y, _textmessage_width, _textmessage_box_y);

		_textmessage_visible = true;
		_textmessage_dirty = false;
	}

	private void MarkTextEffectAreaDirty()
	{
		ViewPort.MarkAllViewportsDirty(
				x,
				y - 1,
				(right - x)*2 + x + 1,
				(bottom - (y - 1)) * 2 + (y - 1) + 1
				);
	}

	public static void AddTextEffect(int msg, int x, int y, int duration)
	{
		AddTextEffect(new StringID( msg ), x, y, duration);
	}

	static void AddTextEffect(StringID msg, int x, int y, int duration)
	{
		TextEffect te = new TextEffect();
		int w;
		String buffer;

		if (Global._game_mode == GameModes.GM_MENU)
			return;

		te.string_id = msg;
		te.duration = duration;
		te.y = y - 5;
		te.bottom = y + 5;
		te.params_1 = (int) Global.GetDParam(0);
		te.params_2 = (int) Global.GetDParam(4);

		buffer = Strings.GetString(msg);
		w = Gfx.GetStringWidth(buffer);

		te.x = x - (w >> 1);
		te.right = x + (w >> 1) - 1;
		te.MarkTextEffectAreaDirty();
		
		_text_effect_list.add( te );
	}

	private void MoveTextEffect()
	{
		if (duration < 8) {
			string_id = Str.INVALID_STRING_ID();
		} else {
			duration -= 8;
			y--;
			bottom--;
		}
		MarkTextEffectAreaDirty();
	}

	static void MoveAllTextEffects()
	{
		for( ListIterator<TextEffect> i = _text_effect_list.listIterator(); i.hasNext(); )
		{
			TextEffect te = i.next();
			
			if (te.string_id.isValid()) 
				te.MoveTextEffect();
			else
				i.remove();
		}
	}

	static void InitTextEffects()
	{
		_text_effect_list.clear();
	}

	public static void DrawTextEffects(DrawPixelInfo dpi)
	{
		if (dpi.zoom < 1) 
		{
			for( TextEffect te : _text_effect_list )
			{
				if (!te.string_id.isValid())
					continue;

				/* intersection? */
				if (dpi.left > te.right ||
						dpi.top > te.bottom ||
						dpi.left + dpi.width <= te.x ||
						dpi.top + dpi.height <= te.y)
					continue;
				ViewPort.AddStringToDraw(te.x, te.y, te.string_id, te.params_1, te.params_2, 0);
			}
		} 
		else if (dpi.zoom == 1) 
		{
 
			for( TextEffect te : _text_effect_list )
			{
				if(!te.string_id.isValid())
					continue;

				/* intersection? */
				if (dpi.left > te.right*2 -  te.x ||
						dpi.top > te.bottom*2 - te.y ||
						(dpi.left + dpi.width) <= te.x ||
						(dpi.top + dpi.height) <= te.y)
					continue;
				ViewPort.AddStringToDraw(te.x, te.y, new StringID(te.string_id.id-1), te.params_1, te.params_2, 0);
			}

		}
	}

	static void DeleteAnimatedTile(TileIndex tile)
	{
		if( Global.gs._animated_tile_list.remove(tile) )
			tile.MarkTileDirtyByTile();
		/*
		for(int i = 0 ; i < Global.gs._animated_tile_list.length; i++)
		{
			if (tile.equals(Global.gs._animated_tile_list[i])) 
			{
				// remove the hole 
				//memmove(ti, ti + 1, endof(_animated_tile_list) - 1 - ti);
				System.arraycopy(Global.gs._animated_tile_list, i+1, Global.gs._animated_tile_list, i, Global.gs._animated_tile_list.length - i - 1);
				// and clear last item 
				//endof(_animated_tile_list)[-1] = 0;
				Global.gs._animated_tile_list[Global.gs._animated_tile_list.length-1] = null;
				tile.MarkTileDirtyByTile();
				return;
			}
		}*/
	}

	static boolean AddAnimatedTile(TileIndex tile)
	{
		if(Global.gs._animated_tile_list.size() > MAX_ANIMATED_TILES)
			return false;
		
		Global.gs._animated_tile_list.add(tile);
		tile.MarkTileDirtyByTile();
		return true;
		/*
		//for (ti = _animated_tile_list; ti != endof(_animated_tile_list); ti++) {
		//for( TileIndex ti : _animated_tile_list)
		for(int i = 0 ; i < Global.gs._animated_tile_list.length; i++)
		{
			if (tile.equals(Global.gs._animated_tile_list[i]) || Global.gs._animated_tile_list[i] == null) {
				Global.gs._animated_tile_list[i] = tile;
				tile.MarkTileDirtyByTile();
				return true;
			}
		}

		return false;*/
	}

	static void AnimateAnimatedTiles()
	{
		// AnimateTile modifies list, hence a copy
		for( TileIndex ti : Global.gs._animated_tile_list.toArray(TileIndex[]::new))
			if( ti != null)
				Landscape.AnimateTile(ti);
	}

	static void InitializeAnimatedTiles()
	{
		Global.gs._animated_tile_list.clear();
	}


	// save/load more? _text_effect_list[] _text_message_list[] - no, it's meaningless

	public static void loadGame(ObjectInputStream oin) throws ClassNotFoundException, IOException
	{
		// Empty
	}

	public static void saveGame(ObjectOutputStream oos) throws IOException 
	{
		// Empty
	}

}


