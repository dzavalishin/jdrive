package game;

public class Widget {
	byte type;
	byte resize_flag;
	byte color;
	int left, right, top, bottom;
	int unkA; // TODO string id
	//StringID tooltips;
	int tooltips;

	public Widget(
			int type,
			int resize_flag,
			int color,
			int left, int right, int top, int bottom,
			int unkA, 
			//StringID tooltips
			int tooltips
			) {

		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;

		this.unkA = unkA;
		this.tooltips = tooltips;
		
		this.type = (byte) type;
		this.resize_flag = (byte) resize_flag;
		this.color = (byte) color;
	}
}
