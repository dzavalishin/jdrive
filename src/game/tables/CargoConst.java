package game.tables;

import game.AcceptedCargo;
import game.StringID;
import game.SpriteID;

// NOSAVE: These can be recalculated from InitializeLandscapeVariables
public class CargoConst 
{
	public static final int NUM_CARGO = AcceptedCargo.NUM_CARGO;

	//public StringID []	names_s				= new StringID[NUM_CARGO];
	//public StringID []	names_long			= new StringID[NUM_CARGO];
	//public StringID []	names_short			= new StringID[NUM_CARGO];

	public int []	names_s					= new int[NUM_CARGO];
	public int []	names_long				= new int[NUM_CARGO];
	public int []	names_short				= new int[NUM_CARGO];
	
	public int []	weights					= new int[NUM_CARGO];
	//public SpriteID []	sprites				= new SpriteID[NUM_CARGO];
	public int []	sprites					= new int[NUM_CARGO];
	public int []		transit_days_1		= new int[NUM_CARGO];
	public int []		transit_days_2		= new int[NUM_CARGO];
	public int [][]		ai_railwagon		= new int[3][NUM_CARGO];
	public int []		ai_roadveh_start	= new int[NUM_CARGO];
	public int []		ai_roadveh_count	= new int[NUM_CARGO];


}
