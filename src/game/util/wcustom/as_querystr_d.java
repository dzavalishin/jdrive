package game.util.wcustom;

import game.ids.StringID;
import game.struct.Textbuf;

public class as_querystr_d implements AbstractWinCustom
{
	private static final long serialVersionUID = 1L;
	
	public StringID caption;
	public int wnd_class;
	public int wnd_num;
	public final Textbuf text = new Textbuf();
	public String orig;
}
