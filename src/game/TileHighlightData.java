package game;

public class TileHighlightData {

	Point size;
	Point outersize;
	Point pos;
	Point offs;

	Point new_pos;
	Point new_size;
	Point new_outersize;

	Point selend, selstart;

	int dirty;
	int sizelimit;

	int drawstyle;      // lower bits 0-3 are reserved for detailed highlight information information
	int new_drawstyle;  // only used in UpdateTileSelection() to as a buffer to compare if there was a change between old and new
	int next_drawstyle; // queued, but not yet drawn style

	int place_mode;
	boolean make_square_red;
	//WindowClass window_class;
	//WindowNumber window_number;
	int window_class;
	int window_number;

	int userdata;
	TileIndex redsq;

}
