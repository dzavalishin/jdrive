package game.xui;

import java.util.ArrayList;
import java.util.List;

import game.Global;
import game.ids.StringID;
import game.util.BitOps;

public class MultilineString 
{
	List<String> strings = new ArrayList<>();
	List<Integer> heights = new ArrayList<>();
	private int lastBase;
	private int mt;
	

	// FormatStringLinebreaks
	private void breakLines(String str, int maxw)
	{
		//int num = 0;
		int base = Gfx._stringwidth_base;
		int w;
		int last_space;
		char c;
		int lineStart = 0;

		char sc[] = str.toCharArray();
		int sp = 0;

		for(;;) {
			w = 0;
			last_space = 0;

			for(;;) {
				if (sp >= sc.length) 
				{
					addLine( new String( sc, lineStart, sp-lineStart), base );
					return; // num + (base << 16);
				}
				
				c = sc[sp++]; // *str++;
				if (c == Gfx.ASCII_LETTERSTART) last_space = sp;

				if (c >= Gfx.ASCII_LETTERSTART) {
					w += Gfx.GetCharacterWidth(base + (byte)c);
					if (w > maxw) {
						sp = last_space;
						// [dz] break out if last_space == 0? Or else loop forever 
						if(sp >= sc.length || sc[sp] == 0 || last_space == 0) 
						{// (str == null)
							addLine( new String( sc, lineStart, sp-lineStart), base );
							return; // num + (base << 16);
						}
						break;
					}
				} else {
					if (sp >= sc.length || c == 0) 
					{
						addLine( new String( sc, lineStart, sp-lineStart), base );
						return; // num + (base << 16);
					}
					if (c == Gfx.ASCII_NL) break;

					if (c == Gfx.ASCII_SETX) sp++;
					else if (c == Gfx.ASCII_SETXY) sp += 2;
					else if (c == Gfx.ASCII_TINYFONT) base = 224;
					else if (c == Gfx.ASCII_BIGFONT) base = 448;
				}
			}

			//num++;
			//str[-1] = '\0'; TODO XXX why?
			//if(sp > 0 ) sc[sp-1] = 0;
			addLine( new String( sc, lineStart, sp-lineStart), base );
			lineStart = sp;
		}
	}


	private void addLine(String string, int base) {
		strings.add(string);
		heights.add(base);
		lastBase = base;
	}

	public static void DrawStringMultiCenter(int x, int y, int str, int maxw)
	{
		MultilineString ml = new MultilineString();
		ml._DrawStringMultiCenter(x, y, str, maxw);
	}
	
	private void baseToMt() {
		switch (lastBase) {
		case   0: mt = 10; break;
		case 244: mt =  6; break;
		default:  mt = 18; break;
		}
	}


	private void _DrawStringMultiCenter(int x, int y, int str, int maxw)
	{
		breakLines(Global.GetString(str), maxw);
		baseToMt();

		y -= (mt >> 1) * getNum();

		for(String src : strings) 
		{
			int w = Gfx.GetStringWidth(src);
			Gfx.DoDrawString(src, x - (w>>1), y, 0xFE);
			Gfx._stringwidth_base = Gfx._stringwidth_out;

			y += mt;					
		}
		Gfx._stringwidth_base = 0;

	}

	public static void DrawStringMultiLine(int x, int y, StringID str, int maxw)
	{
		MultilineString ml = new MultilineString();
		ml._DrawStringMultiLine(x, y, str, maxw);		
	}	
	
	private void _DrawStringMultiLine(int x, int y, StringID str, int maxw)
	{
		breakLines(Global.GetString(str), maxw);
		baseToMt();

		for(String src : strings) 
		{
			Gfx.DoDrawString(src, x, y, 0xFE);
			Gfx._stringwidth_base = Gfx._stringwidth_out;

			y += mt;
		}
		Gfx._stringwidth_base = 0;
	}

	private int getNum() {
		return strings.size();
	}

	
	
}
