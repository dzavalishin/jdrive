package com.dzavalishin.tables;

import com.dzavalishin.game.AcceptedCargo;

public class EngineTables extends EngineTables2  
{

	//enum {
	public static final int ENGINE_AVAILABLE = 1;
	public static final int ENGINE_INTRODUCING = 2;
	public static final int ENGINE_PREVIEWING = 4;
	//};
	
	//typedef enum VehicleTrigger {
	public static final int VEHICLE_TRIGGER_NEW_CARGO = 1;
		// Externally triggered only for the first vehicle in chain
	public static final int VEHICLE_TRIGGER_DEPOT = 2;
		// Externally triggered only for the first vehicle in chain, only if whole chain is empty
	public static final int VEHICLE_TRIGGER_EMPTY = 4;
		// Not triggered externally (called for the whole chain if we got NEW_CARGO)
	public static final int VEHICLE_TRIGGER_ANY_NEW_CARGO = 8;
	//} VehicleTrigger;
	

	//enum {
	public static final int RVI_MULTIHEAD = 1;
	public static final int RVI_WAGON = 2;
	//};

	//enum {
	public static final int NUM_VEHICLE_TYPES = 6;
	//};


	//enum GlobalCargo {
	public static final int GC_PASSENGERS   =   0;
	public static final int GC_COAL         =   1;
	public static final int GC_MAIL         =   2;
	public static final int GC_OIL          =   3;
	public static final int GC_LIVESTOCK    =   4;
	public static final int GC_GOODS        =   5;
	public static final int GC_GRAIN        =   6; // public static final int GC_WHEAT / public static final int GC_MAIZE
	public static final int GC_WOOD         =   7;
	public static final int GC_IRON_ORE     =   8;
	public static final int GC_STEEL        =   9;
	public static final int GC_VALUABLES    =  10; // public static final int GC_GOLD / public static final int GC_DIAMONDS
	public static final int GC_PAPER        =  11;
	public static final int GC_FOOD         =  12;
	public static final int GC_FRUIT        =  13;
	public static final int GC_COPPER_ORE   =  14;
	public static final int GC_WATER        =  15;
	public static final int GC_RUBBER       =  16;
	public static final int GC_SUGAR        =  17;
	public static final int GC_TOYS         =  18;
	public static final int GC_BATTERIES    =  19;
	public static final int GC_CANDY        =  20;
	public static final int GC_TOFFEE       =  21;
	public static final int GC_COLA         =  22;
	public static final int GC_COTTON_CANDY =  23;
	public static final int GC_BUBBLES      =  24;
	public static final int GC_PLASTIC      =  25;
	public static final int GC_FIZZY_DRINKS =  26;
	public static final int GC_PAPER_TEMP   =  27;
	public static final int GC_UNDEFINED    =  28; // undefined; unused slot in arctic climate
	public static final int GC_DEFAULT      =  29;
	public static final int GC_PURCHASE     =  30;
	public static final int GC_INVALID      = 255;
	public static final int NUM_GLOBAL_CID  =  31;
	//};

	
	
	
	
	
	/** TRANSLATE FROM LOCAL CARGO TO GLOBAL CARGO ID'S.
	 * This maps the per-landscape cargo ID's to globally unique cargo ID's usable ie. in
	 * the custom GRF  files. It is basically just a transcribed table from TTDPatch's newgrf.txt.
	 */
	//final CargoID _global_cargo_id[NUM_LANDSCAPE][NUM_CARGO] = 
	public final static int _global_cargo_id[][] = 
		{
			/* Landscape.LT_NORMAL */ {GC_PASSENGERS, GC_COAL,  GC_MAIL, GC_OIL, GC_LIVESTOCK, GC_GOODS, GC_GRAIN, GC_WOOD, GC_IRON_ORE,    GC_STEEL,  GC_VALUABLES, GC_PAPER_TEMP},
			/* Landscape.LT_HILLY */  {GC_PASSENGERS, GC_COAL,  GC_MAIL, GC_OIL, GC_LIVESTOCK, GC_GOODS, GC_GRAIN, GC_WOOD, GC_INVALID,     GC_PAPER,  GC_VALUABLES, GC_FOOD },
			/* Landscape.LT_DESERT */ {GC_PASSENGERS, GC_RUBBER,GC_MAIL, GC_OIL, GC_FRUIT,     GC_GOODS, GC_GRAIN, GC_WOOD, GC_COPPER_ORE,  GC_WATER,  GC_VALUABLES, GC_FOOD },
			/* Landscape.LT_CANDY */  {GC_PASSENGERS, GC_SUGAR, GC_MAIL, GC_TOYS,GC_BATTERIES, GC_CANDY, GC_TOFFEE,GC_COLA, GC_COTTON_CANDY,GC_BUBBLES,GC_PLASTIC,   GC_FIZZY_DRINKS },
			/**
			 * - GC_INVALID (255) means that  cargo is not available for that climate
			 * - GC_PAPER_TEMP (27) is paper in  temperate climate in TTDPatch
			 * Following can  be renumbered:
			 * - GC_DEFAULT (29) is the defa ult cargo for the purpose of spritesets
			 * - GC_PURCHASE (30) is the purchase list image (the equivalent of 0xff) for the purpose of spritesets
			 */
	};

	/** BEGIN --- TRANSLATE FROM GLOBAL CARGO TO LOCAL CARGO ID'S **/
	/** Map global cargo ID's to local-cargo ID's */
	//final CargoID _local_cargo_id_ctype[NUM_GLOBAL_CID] = {
	public final static int _local_cargo_id_ctype[] = {
			AcceptedCargo.CT_PASSENGERS,AcceptedCargo.CT_COAL,   AcceptedCargo.CT_MAIL,        AcceptedCargo.CT_OIL,      AcceptedCargo.CT_LIVESTOCK,AcceptedCargo.CT_GOODS,  AcceptedCargo.CT_GRAIN,      AcceptedCargo.CT_WOOD,         /*  0- 7 */
			AcceptedCargo.CT_IRON_ORE,  AcceptedCargo.CT_STEEL,  AcceptedCargo.CT_VALUABLES,   AcceptedCargo.CT_PAPER,    AcceptedCargo.CT_FOOD,     AcceptedCargo.CT_FRUIT,  AcceptedCargo.CT_COPPER_ORE, AcceptedCargo.CT_WATER,        /*  8-15 */
			AcceptedCargo.CT_RUBBER,    AcceptedCargo.CT_SUGAR,  AcceptedCargo.CT_TOYS,        AcceptedCargo.CT_BATTERIES,AcceptedCargo.CT_CANDY,    AcceptedCargo.CT_TOFFEE, AcceptedCargo.CT_COLA,       AcceptedCargo.CT_COTTON_CANDY, /* 16-23 */
			AcceptedCargo.CT_BUBBLES,   AcceptedCargo.CT_PLASTIC,AcceptedCargo.CT_FIZZY_DRINKS,AcceptedCargo.CT_PAPER     /* unsup. */,AcceptedCargo.CT_HILLY_UNUSED,                           /* 24-28 */
			AcceptedCargo.CT_INVALID,   AcceptedCargo.CT_INVALID                                                                                      /* 29-30 */
	};

	private static int MC(int cargo) { return (1 << cargo); }
	
	/** Bitmasked value where the global cargo ID is available in landscape
	 * 0: Landscape.LT_NORMAL, 1: Landscape.LT_HILLY, 2: Landscape.LT_DESERT, 3: Landscape.LT_CANDY */
	
	//final int _landscape_global_cargo_mask[NUM_LANDSCAPE] =
	public final static int _landscape_global_cargo_mask[] =
{ /* Landscape.LT_NORMAL: temperate */
		MC(GC_PASSENGERS)|MC(GC_COAL)|MC(GC_MAIL)|MC(GC_OIL)|MC(GC_LIVESTOCK)|MC(GC_GOODS)|MC(GC_GRAIN)|MC(GC_WOOD)|
		MC(GC_IRON_ORE)|MC(GC_STEEL)|MC(GC_VALUABLES),
		/* Landscape.LT_HILLY: arctic */
		MC(GC_PASSENGERS)|MC(GC_COAL)|MC(GC_MAIL)|MC(GC_OIL)|MC(GC_LIVESTOCK)|MC(GC_GOODS)|
		MC(GC_GRAIN)|MC(GC_WOOD)|MC(GC_VALUABLES)|MC(GC_PAPER)|MC(GC_FOOD),
		/* Landscape.LT_DESERT: rainforest/desert */
		MC(GC_PASSENGERS)|MC(GC_MAIL)|MC(GC_OIL)|MC(GC_GOODS)|MC(GC_GRAIN)|MC(GC_WOOD)|
		MC(GC_VALUABLES)|MC(GC_FOOD)|MC(GC_FRUIT)|MC(GC_COPPER_ORE)|MC(GC_WATER)|MC(GC_RUBBER),
		/* Landscape.LT_CANDY: toyland */
		MC(GC_PASSENGERS)|MC(GC_MAIL)|MC(GC_SUGAR)|MC(GC_TOYS)|MC(GC_BATTERIES)|MC(GC_CANDY)|
		MC(GC_TOFFEE)|MC(GC_COLA)|MC(GC_COTTON_CANDY)|MC(GC_BUBBLES)|MC(GC_PLASTIC)|MC(GC_FIZZY_DRINKS)
};
	/** END   --- TRANSLATE FROM GLOBAL CARGO TO LOCAL CARGO ID'S **/

	/** Bitmasked values of what type of cargo is refittable for the given vehicle-type.
	 * This coupled with the landscape information (_landscape_global_cargo_mask) gives
	 * us exactly what is refittable and what is not */
	//final int _default_refitmasks[NUM_VEHICLE_TYPES] = {
	public final static int _default_refitmasks[] = {
			/* Trains */
			MC(GC_PASSENGERS)|MC(GC_COAL)|MC(GC_MAIL)|MC(GC_LIVESTOCK)|MC(GC_GOODS)|MC(GC_GRAIN)|MC(GC_WOOD)|MC(GC_IRON_ORE)|
			MC(GC_STEEL)|MC(GC_VALUABLES)|MC(GC_PAPER)|MC(GC_FOOD)|MC(GC_FRUIT)|MC(GC_COPPER_ORE)|MC(GC_WATER)|MC(GC_SUGAR)|
			MC(GC_TOYS)|MC(GC_CANDY)|MC(GC_TOFFEE)|MC(GC_COLA)|MC(GC_COTTON_CANDY)|MC(GC_BUBBLES)|MC(GC_PLASTIC)|MC(GC_FIZZY_DRINKS),
			/* Road vehicles (not refittable by default) */
			0,
			/* Ships */
			MC(GC_COAL)|MC(GC_MAIL)|MC(GC_LIVESTOCK)|MC(GC_GOODS)|MC(GC_GRAIN)|MC(GC_WOOD)|MC(GC_IRON_ORE)|MC(GC_STEEL)|MC(GC_VALUABLES)|
			MC(GC_PAPER)|MC(GC_FOOD)|MC(GC_FRUIT)|MC(GC_COPPER_ORE)|MC(GC_WATER)|MC(GC_RUBBER)|MC(GC_SUGAR)|MC(GC_TOYS)|MC(GC_BATTERIES)|
			MC(GC_CANDY)|MC(GC_TOFFEE)|MC(GC_COLA)|MC(GC_COTTON_CANDY)|MC(GC_BUBBLES)|MC(GC_PLASTIC)|MC(GC_FIZZY_DRINKS),
			/* Aircraft */
			MC(GC_PASSENGERS)|MC(GC_MAIL)|MC(GC_GOODS)|MC(GC_VALUABLES)|MC(GC_FOOD)|MC(GC_FRUIT)|MC(GC_SUGAR)|MC(GC_TOYS)|
			MC(GC_BATTERIES)|MC(GC_CANDY)|MC(GC_TOFFEE)|MC(GC_COLA)|MC(GC_COTTON_CANDY)|MC(GC_BUBBLES)|MC(GC_PLASTIC)|MC(GC_FIZZY_DRINKS),
			/* Special/Disaster */
			0,0
	};

	/**
	 * Bitmask of classes for cargo types.
	 */
	public final static int cargo_classes[] = {
			/* Passengers */ MC(GC_PASSENGERS),
			/* Mail       */ MC(GC_MAIL),
			/* Express    */ MC(GC_GOODS)|MC(GC_FOOD)|MC(GC_CANDY),
			/* Armoured   */ MC(GC_VALUABLES),
			/* Bulk       */ MC(GC_COAL)|MC(GC_GRAIN)|MC(GC_IRON_ORE)|MC(GC_COPPER_ORE)|MC(GC_FRUIT)|MC(GC_SUGAR)|MC(GC_TOFFEE)|MC(GC_COTTON_CANDY),
			/* Piece      */ MC(GC_LIVESTOCK)|MC(GC_WOOD)|MC(GC_STEEL)|MC(GC_PAPER)|MC(GC_TOYS)|MC(GC_BATTERIES)|MC(GC_BUBBLES)|MC(GC_FIZZY_DRINKS),
			/* Liquids    */ MC(GC_OIL)|MC(GC_WATER)|MC(GC_RUBBER)|MC(GC_COLA)|MC(GC_PLASTIC),
			/* Chilled    */ MC(GC_FOOD)|MC(GC_FRUIT),
			/* Undefined  */ 0, 0, 0, 0, 0, 0, 0, 0
	};

	
		

}
