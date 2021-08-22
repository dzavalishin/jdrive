package game.xui;

import game.Sprite;
import game.ids.CursorID;
import game.struct.Point;
import game.util.AnimCursor;

public class CursorVars 
{
	final Point pos = new Point(0, 0);
	final Point size = new Point(0, 0);
	final Point offs = new Point(0, 0);
	final Point delta = new Point(0, 0);
	final Point draw_pos = new Point(0, 0);
	final Point draw_size = new Point(0, 0);
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

		if (sprite == cursor) return;

		p = game.SpriteCache.GetSprite(cursor.id & Sprite.SPRITE_MASK);
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

		CursorID sprite = CursorID.get( animate_list[animate_pos].spriteId );
		animate_timeout = animate_list[animate_pos].time;
		//animate_pos += 2;
		animate_pos++;

		SetCursorSprite(sprite);
		/*
		CursorID[] cur = cv.animate_cur;
		CursorID sprite;

		// ANIM_CURSOR_END is 0xFFFF in table/animcursors.h
		if (cur[0] == null || cur[0].id == 0xFFFF) cur = cv.animate_list;

		sprite = cur[0];
		cv.animate_timeout = cur[1].id;
		cv.animate_cur = new CursorId( cur.id + 2);

		SetCursorSprite(sprite);
		 */
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


}


