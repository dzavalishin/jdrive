package com.dzavalishin.struct;

import com.dzavalishin.ids.SpriteID;

public class RailGuiSprites {
	public final SpriteID build_ns_rail;      ///< button for building single rail in N-S direction
	public final SpriteID build_x_rail;       ///< button for building single rail in X direction
	public final SpriteID build_ew_rail;      ///< button for building single rail in E-W direction
	public final SpriteID build_y_rail;       ///< button for building single rail in Y direction
	public final SpriteID auto_rail;          ///< button for the autorail construction
	public final SpriteID build_depot;        ///< button for building depots
	public final SpriteID build_tunnel;       ///< button for building a tunnel
	public final SpriteID convert_rail;       ///< button for converting rail

	public RailGuiSprites(int[] spr) 
	{
		int i = 0;
		build_ns_rail = SpriteID.get( spr[i++]);      
		build_x_rail = SpriteID.get( spr[i++]);       
		build_ew_rail = SpriteID.get( spr[i++]);      
		build_y_rail = SpriteID.get( spr[i++]);       
		auto_rail = SpriteID.get( spr[i++]);          
		build_depot = SpriteID.get( spr[i++]);        
		build_tunnel = SpriteID.get( spr[i++]);       
		convert_rail = SpriteID.get( spr[i++]);       
	}

}
