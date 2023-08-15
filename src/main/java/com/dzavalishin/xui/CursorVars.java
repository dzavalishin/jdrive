package com.dzavalishin.xui;

import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.SpriteCache;
import com.dzavalishin.ids.CursorID;
import com.dzavalishin.struct.Point;
import com.dzavalishin.util.AnimCursor;

public class CursorVars 
{
	final Point pos = new Point(0, 0);
	final Point size = new Point(0, 0);
	final Point offs = new Point(0, 0);
	final Point delta = new Point(0, 0);
	final Point draw_pos = new Point(0, 0);
	final Point draw_size = new Point(0, 0);
	
	public Point scrollRef; // reference point for right mouse button scroll
	private boolean scrollingViewport = false;

	CursorID sprite;

	int wheel; // mouse wheel movement
	AnimCursor[] animate_list;
	int animate_pos;
	int animate_timeout;

	boolean visible;
	boolean dirty;
	boolean fix_at;

	public void SetCursorSprite(CursorID cursor)
	{
		Sprite p;

 		if( sprite != null && sprite.equals(cursor) ) return;

		p = SpriteCache.GetSprite(cursor.id & Sprite.SPRITE_MASK);
		sprite = cursor;
		size.y = p.getHeight();
		size.x = p.getWidth();
		offs.x = p.getX_offs();
		offs.y = p.getY_offs();

		dirty = true;
	}

	public void SwitchAnimatedCursor()
	{

		if(
				(animate_pos >= animate_list.length )
				||
				(animate_list[animate_pos] == null)
				||
				(animate_list[animate_pos].spriteId == 0xFFFF)
				)
		{
			animate_pos = 0;
		}

		CursorID csprite = CursorID.get( animate_list[animate_pos].spriteId );
		animate_timeout = animate_list[animate_pos].time;
		//animate_pos += 2;
		animate_pos++;

		SetCursorSprite(csprite);
	}

	public void setCursor(AnimCursor[] animcursors) 
	{
		animate_list = animcursors;
		animate_pos = 0;
		SwitchAnimatedCursor();
	}

	public void setCursor(CursorID cursor) 
	{
		// Turn off animation
		animate_timeout = 0;
		// Set cursor
		SetCursorSprite(cursor);
	}

	public void tick() 
	{
		if (animate_timeout != 0 && --animate_timeout == 0)
			SwitchAnimatedCursor();
	}

	public boolean isVisible() { return visible; }

	public void setDelta(int x, int y) 
	{
		delta.x = x;
		delta.y = y;
	}

	public boolean xBetween(int l, int r) {
		return 
				draw_pos.x + draw_size.x >= l &&
				draw_pos.x <= r;
	}

	public boolean yBetween(int u, int d) {
		return 
				draw_pos.y + draw_size.y >= u
				&& draw_pos.y <= d;
	}

	public void processMouse(int x, int y) {
		if (fix_at) {
			int dx = x - pos.x;
			int dy = y - pos.y;
			if (dx != 0 || dy != 0) {
				delta.x += dx;
				delta.y += dy;

				/* TODO set cursor pos
				pt.x = _cursor.pos.x;
				pt.y = _cursor.pos.y;

				if (_wnd.double_size) {
					pt.x *= 2;
					pt.y *= 2;
				}
				ClientToScreen(hwnd, &pt);
				SetCursorPos(pt.x, pt.y);
				 */
			}
		} else {
			delta.x += x - pos.x;
			delta.y += y - pos.y;
			pos.x = x;
			pos.y = y;
			dirty = true;
		}
	}

	public void setWheel(int wheelRotation) {
		wheel = wheelRotation;		
	}

	public void startViewportScrolling() 
	{
		if (scrollingViewport)
			return;
		scrollingViewport = true;
		Hal._cursor.scrollRef = new Point( Hal._cursor.pos );
	}

	public void stopViewportScrolling() {
		//Hal._cursor.fix_at = false;
		Hal._cursor.scrollRef = null;
		scrollingViewport = false;
	}

	public boolean isScrollingViewport() { return scrollingViewport; }

	public Point getViewportScrollStep() {
		int dx = -(Hal._cursor.pos.x - Hal._cursor.scrollRef.x);
		int dy = -(Hal._cursor.pos.y - Hal._cursor.scrollRef.y);
		Hal._cursor.scrollRef = new Point(Hal._cursor.pos);

		return new Point( dx, dy );
	}


}


