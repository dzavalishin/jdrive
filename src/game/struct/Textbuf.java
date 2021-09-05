package game.struct;

import java.io.Serializable;

import game.Global;
import game.xui.Gfx;
import game.xui.Widget;
import game.xui.Window;

public class Textbuf implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	private char [] buf;                  /* buffer in which text is saved */
	public int maxlength; /* the maximum size of the buffer.  */
	public int maxwidth; // Maxwidth specifies screensize in pixels
	private int length, width;       /* the current size of the buffer. Width specifies screensize in pixels */
	private boolean caret;                 /* is the caret ("_") visible or not */
	private int caretpos;            /* the current position of the caret in the buffer */
	private int caretxoffs;          /* the current position of the caret in pixels */

	
	public Textbuf(int i) {
		allocate(i);
	}


	public Textbuf() {
		allocate(128);
	}


	void DelChar()
	{
		width -= Gfx.GetCharacterWidth(buf[caretpos]);
		//memmove(buf + caretpos, buf + caretpos + 1, length - caretpos);
		
		if(caretpos == length-1)
			buf[caretpos] = 0;
		else
			System.arraycopy(buf, caretpos + 1, buf, caretpos, length - caretpos);
		length--;
	}

	
	/**
	 * Delete a character from a textbuffer, either with 'Delete' or 'Backspace'
	 * The character is delete from the position the caret is at
	 *
	 * @param delmode Type of deletion, either @Window.WKC_BACKSPACE or @Window.WKC_DELETE
	 * @return Return true on successfull change of Textbuf, or false otherwise
	 */
	public boolean DeleteTextBufferChar(int delmode)
	{
		if (delmode == Window.WKC_BACKSPACE && caretpos != 0) {
			caretpos--;
			caretxoffs -= Gfx.GetCharacterWidth(buf[caretpos]);

			DelChar();
			return true;
		} else if (delmode == Window.WKC_DELETE && caretpos < length) {
			DelChar();
			return true;
		}

		return false;
	}

	
	/**
	 * Delete every character in the textbuffer
	 */
	public void DeleteTextBufferAll()
	{
		//memset(tb.buf, 0, tb.maxlength);
		buf[0] = 0;
		length = width = 0;
		caretpos = caretxoffs = 0;
	}

	
	/**
	 * Insert a character to a textbuffer. If maxlength is zero, we don't care about
	 * the screenlength but only about the physical length of the string
	 * @param key Character to be inserted
	 * @return Return true on successfull change of Textbuf, or false otherwise
	 */
	public boolean InsertTextBufferChar(char key)
	{
		final int charwidth = Gfx.GetCharacterWidth(key);
		if (length < maxlength && (maxwidth == 0 || width + charwidth <= maxwidth)) {
			//memmove(buf + caretpos + 1, buf + caretpos, (length - caretpos) + 1);
			System.arraycopy(buf, caretpos, buf, caretpos + 1, (length - caretpos) + 1);
			buf[caretpos] = key;
			length++;
			width += charwidth;

			caretpos++;
			caretxoffs += charwidth;
			return true;
		}
		return false;
	}

	
	/**
	 * Handle text navigation with arrow keys left/right.
	 * This defines where the caret will blink and the next characer interaction will occur
	 * @param navmode Direction in which navigation occurs @Window.WKC_LEFT, @Window.WKC_RIGHT, @Window.WKC_END, @Window.WKC_HOME
	 * @return Return true on successfull change of Textbuf, or false otherwise
	 */
	public boolean MoveTextBufferPos(int navmode)
	{
		switch (navmode) {
		case Window.WKC_LEFT:
			if (caretpos != 0) {
				caretpos--;
				caretxoffs -= Gfx.GetCharacterWidth(buf[caretpos]);
				return true;
			}
			break;
		case Window.WKC_RIGHT:
			if (caretpos < length) {
				caretxoffs += Gfx.GetCharacterWidth(buf[caretpos]);
				caretpos++;
				return true;
			}
			break;
		case Window.WKC_HOME:
			caretpos = 0;
			caretxoffs = 0;
			return true;
		case Window.WKC_END:
			caretpos = length;
			caretxoffs = width;
			return true;
		}

		return false;
	}

	/**
	 * Update @Textbuf type with its actual physical character and screenlength
	 * Get the count of characters in the string as well as the width in pixels.
	 * Useful when copying in a larger amount of text at once
	 */
	public void UpdateTextBufferSize()
	{
		//final char* buf;
		int bp = 0;

		length = 0;
		width = 0;

		for (; bp < buf.length && buf[bp] != '\0' && length <= maxlength; bp++) 
		{
			length++;
			width += Gfx.GetCharacterWidth(buf[bp]);
		}

		caretpos = length;
		caretxoffs = width;
	}

	public boolean HandleCaret()
	{
		/* caret changed? */
		boolean b = 0 != (Global._caret_timer & 0x20);

		if (b != caret) {
			caret = b;
			return true;
		}
		return false;
	}


	public void drawToWidget(Widget wi) {
		Gfx.DoDrawString( String.valueOf( buf ), wi.left+2, wi.top+1, 8);
		if(caret)
			Gfx.DoDrawString("_", wi.left + 2 + caretxoffs, wi.top + 1, 12);
	}


	public char[] getBuf() {
		return buf;
	}


	public void setText(String str) {
		buf = new char[256]; // TODO size?
		char[] src = str.toCharArray();		
		System.arraycopy(src, 0, buf, 0, src.length);
		UpdateTextBufferSize();
	}


	public void setCaret(boolean b) {
		caret = b;		
	}


	public void allocate(int i) {
		buf = new char[i];
		
	}


	public String getString() { return new String(buf, 0, length); }
	public boolean isCaret() { return caret; }
	public int getCaretXOffs() { return caretxoffs; }
	public int getWidth() { return width; }
	
}
