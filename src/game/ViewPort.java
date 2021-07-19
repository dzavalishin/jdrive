package game;

public class ViewPort {
	int left,top;												// screen coordinates for the viewport
	int width, height;									// screen width/height for the viewport

	int virtual_left, virtual_top;			// virtual coordinates
	int virtual_width, virtual_height;	// these are just width << zoom, height << zoom

	byte zoom;

	public static TileHighlightData _thd;

};
