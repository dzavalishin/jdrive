package game.xui;

public class Widget {
	final int type;
	final int resize_flag;
	int color;
	public int left;
	public int right;
	public int top;
	public int bottom;
	public int unkA; // TODO string id
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
		
		this.type =  type;
		this.resize_flag =  resize_flag;
		this.color =  color;
	}
}
