package game.tables;

public class TunnelBridgeTables {

	final Bridge orig_bridge[] = {
			/*
				   year of availablity
				   |  minimum length
				   |  |   maximum length
				   |  |   |    price
				   |  |   |    |    maximum speed
				   |  |   |    |    |  sprite to use in GUI                string with description
				   |  |   |    |    |  |                                   |                            */
				{  0, 0, 16,  80,  32, 0xA24                             , Str.STR_5012_WOODEN             , null, 0 },
				{  0, 0,  2, 112,  48, 0xA26 | PALETTE_TO_STRUAcceptedCargo.CT_RED     , Str.STR_5013_CONCRETE           , null, 0 },
				{ 10, 0,  5, 144,  64, 0xA25                             , Str.STR_500F_GIRDER_STEEL       , null, 0 },
				{  0, 2, 10, 168,  80, 0xA22 | PALETTE_TO_STRUAcceptedCargo.CT_CONCRETE, Str.STR_5011_SUSPENSION_CONCRETE, null, 0 },
				{ 10, 3, 16, 185,  96, 0xA22                             , Str.STR_500E_SUSPENSION_STEEL   , null, 0 },
				{ 10, 3, 16, 192, 112, 0xA22 | PALETTE_TO_STRUAcceptedCargo.CT_YELLOW  , Str.STR_500E_SUSPENSION_STEEL   , null, 0 },
				{ 10, 3,  7, 224, 160, 0xA23                             , Str.STR_5010_CANTILEVER_STEEL   , null, 0 },
				{ 10, 3,  8, 232, 208, 0xA23 | PALETTE_TO_STRUAcceptedCargo.CT_BROWN   , Str.STR_5010_CANTILEVER_STEEL   , null, 0 },
				{ 10, 3,  9, 248, 240, 0xA23 | PALETTE_TO_STRUAcceptedCargo.CT_RED     , Str.STR_5010_CANTILEVER_STEEL   , null, 0 },
				{ 10, 0,  2, 240, 256, 0xA27                             , Str.STR_500F_GIRDER_STEEL       , null, 0 },
				{ 75, 2, 16, 255, 320, 0xA28                             , Str.STR_5014_TUBULAR_STEEL      , null, 0 },
				{ 85, 2, 32, 380, 512, 0xA28 | PALETTE_TO_STRUAcceptedCargo.CT_YELLOW  , Str.STR_5014_TUBULAR_STEEL      , null, 0 },
				{ 90, 2, 32, 510, 608, 0xA28 | PALETTE_TO_STRUAcceptedCargo.CT_GREY    , Str.STR_BRIDGE_TUBULAR_SILICON  , null, 0 }
			};

	static final byte _bridge_foundations[][] = {
			// 0 1  2  3  4 5 6 7  8 9 10 11 12 13 14 15
			{1,16,18,3,20,5,0,7,22,0,10,11,12,13,14},
			{1,15,17,0,19,5,6,7,21,9,10,11, 0,13,14},
	};
	
	static final int _new_data_table[] = {0x1002, 0x1001, 0x2005, 0x200A, 0, 0, 0, 0};
	
}
