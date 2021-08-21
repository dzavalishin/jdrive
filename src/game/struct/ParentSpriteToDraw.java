package game.struct;

import java.util.ArrayList;

public class ParentSpriteToDraw 
{

	public int image;
	
	public int left;
	public int top;
	public int right;
	public int bottom;
	
	public int tile_x;
	public int tile_y;
	public int tile_right;
	public int tile_bottom;
	
	//ChildScreenSpriteToDraw child;
	
	public byte unk16;
	public int tile_z;
	public int tile_z_bottom;

	public ArrayList<ChildScreenSpriteToDraw> children = new ArrayList<ChildScreenSpriteToDraw>();

}