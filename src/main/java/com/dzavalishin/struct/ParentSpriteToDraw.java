package com.dzavalishin.struct;

import java.util.ArrayList;
import java.util.List;

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

	public final List<ChildScreenSpriteToDraw> children = new ArrayList<>();

}
