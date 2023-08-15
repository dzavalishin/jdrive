package com.dzavalishin.struct;

/** 
 * Information about a vehicle
 * 
 */

public class EngineInfo 
{
	public int base_intro;
	public int unk2;              ///< Carriages have the highest bit set in this one
	public int lifelength;
	public int base_life;
	public int railtype;
	public int climates;
	public int refit_mask;

	public EngineInfo() {
		// Empty
	}
	
	public EngineInfo( EngineInfo src ) 
	{
		base_intro =   src.base_intro;
		unk2       =   src.unk2;     
		lifelength =   src.lifelength;
		base_life  =   src.base_life;
		railtype   =   src.railtype; 
		climates   =   src.climates; 
		refit_mask =   src.refit_mask;		
	}
}
