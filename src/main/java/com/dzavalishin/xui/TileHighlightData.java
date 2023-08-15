package com.dzavalishin.xui;

import com.dzavalishin.game.TileIndex;
import com.dzavalishin.struct.Point;

public class TileHighlightData {

	Point size = new Point(0, 0);
	Point outersize = new Point(0, 0);
	Point pos = new Point(0, 0);
	final Point offs = new Point(0, 0);

	final Point new_pos = new Point(0, 0);
	final Point new_size = new Point(0, 0);
	final Point new_outersize = new Point(0, 0);

	final Point selend = new Point(0, 0);
	final Point selstart = new Point(0, 0);

	int dirty;
	int sizelimit;

	int drawstyle;      // lower bits 0-3 are reserved for detailed highlight information information
	int new_drawstyle;  // only used in UpdateTileSelection() to as a buffer to compare if there was a change between old and new
	int next_drawstyle; // queued, but not yet drawn style

	public int place_mode;
	boolean make_square_red;

	public int window_class;
	int window_number;

	int userdata;
	TileIndex redsq;
	
	public TileIndex getEndTile() {
		return TileIndex.TileVirtXY(selend.x, selend.y);
	}

	public TileIndex getStartTile() {
		return TileIndex.TileVirtXY(selstart.x, selstart.y);
	}

	public boolean isRect() {
		return drawstyle == ViewPort.HT_RECT;
	}

	public int getTrackState() {
		return drawstyle & 0xF;  // 0..5
	}

	public boolean hasRail() {
		return 0 != (drawstyle & ViewPort.HT_RAIL);
	}

	public void markPosDirty() {
		ViewPort.MarkTileDirty(pos.x, pos.y); 		
	}

}
