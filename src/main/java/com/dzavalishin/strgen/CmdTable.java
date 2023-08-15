package com.dzavalishin.strgen;

public class CmdTable {

	static final CmdStruct _cmd_structs[] = {
			// Update position
			new CmdStruct("SETX",  Emitter::EmitSetX,  1, 0, 0),
			new CmdStruct("SETXY", Emitter::EmitSetXY, 2, 0, 0),
	
			// Font size
			new CmdStruct("TINYFONT", Emitter::EmitSingleByte, 8, 0, 0),
			new CmdStruct("BIGFONT",  Emitter::EmitSingleByte, 9, 0, 0),
	
			// New line
			new CmdStruct("", Emitter::EmitSingleByte, 10, 0, Main.C_DONTCOUNT),
	
			new CmdStruct("{", Emitter::EmitSingleByte, '{', 0, Main.C_DONTCOUNT),
	
			// Colors
			new CmdStruct("BLUE",    Emitter::EmitSingleByte, 15, 0, 0),
			new CmdStruct("SILVER",  Emitter::EmitSingleByte, 16, 0, 0),
			new CmdStruct("GOLD",    Emitter::EmitSingleByte, 17, 0, 0),
			new CmdStruct("RED",     Emitter::EmitSingleByte, 18, 0, 0),
			new CmdStruct("PURPLE",  Emitter::EmitSingleByte, 19, 0, 0),
			new CmdStruct("LTBROWN", Emitter::EmitSingleByte, 20, 0, 0),
			new CmdStruct("ORANGE",  Emitter::EmitSingleByte, 21, 0, 0),
			new CmdStruct("GREEN",   Emitter::EmitSingleByte, 22, 0, 0),
			new CmdStruct("YELLOW",  Emitter::EmitSingleByte, 23, 0, 0),
			new CmdStruct("DKGREEN", Emitter::EmitSingleByte, 24, 0, 0),
			new CmdStruct("CREAM",   Emitter::EmitSingleByte, 25, 0, 0),
			new CmdStruct("BROWN",   Emitter::EmitSingleByte, 26, 0, 0),
			new CmdStruct("WHITE",   Emitter::EmitSingleByte, 27, 0, 0),
			new CmdStruct("LTBLUE",  Emitter::EmitSingleByte, 28, 0, 0),
			new CmdStruct("GRAY",    Emitter::EmitSingleByte, 29, 0, 0),
			new CmdStruct("DKBLUE",  Emitter::EmitSingleByte, 30, 0, 0),
			new CmdStruct("BLACK",   Emitter::EmitSingleByte, 31, 0, 0),
	
			// 0x85
			new CmdStruct("CURRCOMPACT",   Emitter::EmitEscapedByte, 0, 1, 0), // compact currency (32 bits)
			new CmdStruct("REV",           Emitter::EmitEscapedByte, 2, 0, 0), // openttd revision string
			new CmdStruct("SHORTCARGO",    Emitter::EmitEscapedByte, 3, 2, 0), // short cargo description, only ### tons, or ### litres
			new CmdStruct("CURRCOMPACT64", Emitter::EmitEscapedByte, 4, 2, 0), // compact currency 64 bits
	
			new CmdStruct("COMPANY", Emitter::EmitEscapedByte, 5, 1, 0),				// company string. This is actually a new CmdStruct(STRING1)
			// The first string includes the second string.
	
			new CmdStruct("PLAYERNAME", Emitter::EmitEscapedByte, 5, 1, 0),		// playername string. This is actually a new CmdStruct(STRING1)
			// The first string includes the second string.
	
			new CmdStruct("VEHICLE", Emitter::EmitEscapedByte, 5, 1, 0),		// playername string. This is actually a new CmdStruct(STRING1)
			// The first string includes the second string.
	
	
			new CmdStruct("STRING1", Emitter::EmitEscapedByte, 5, 1, Main.C_CASE),				// included string that consumes ONE argument
			new CmdStruct("STRING2", Emitter::EmitEscapedByte, 6, 2, Main.C_CASE),				// included string that consumes TWO arguments
			new CmdStruct("STRING3", Emitter::EmitEscapedByte, 7, 3, Main.C_CASE),				// included string that consumes THREE arguments
			new CmdStruct("STRING4", Emitter::EmitEscapedByte, 8, 4, Main.C_CASE),				// included string that consumes FOUR arguments
			new CmdStruct("STRING5", Emitter::EmitEscapedByte, 9, 5, Main.C_CASE),				// included string that consumes FIVE arguments
	
			new CmdStruct("STATIONFEATURES", Emitter::EmitEscapedByte, 10, 1, 0), // station features string, icons of the features
			new CmdStruct("INDUSTRY",        Emitter::EmitEscapedByte, 11, 1, 0), // industry, takes an industry #
			new CmdStruct("VOLUME",          Emitter::EmitEscapedByte, 12, 1, 0),
			new CmdStruct("DATE_TINY",       Emitter::EmitEscapedByte, 14, 1, 0),
			new CmdStruct("CARGO",           Emitter::EmitEscapedByte, 15, 2, 0),
	
			new CmdStruct("P", Emitter::EmitPlural, 0, 0, Main.C_DONTCOUNT),					// plural specifier
			new CmdStruct("G", Emitter::EmitGender, 0, 0, Main.C_DONTCOUNT),					// gender specifier
	
			new CmdStruct("DATE_LONG",  Emitter::EmitSingleByte, 0x82, 1, 0),
			new CmdStruct("DATE_SHORT", Emitter::EmitSingleByte, 0x83, 1, 0),
	
			new CmdStruct("VELOCITY", Emitter::EmitSingleByte, 0x84, 1, 0),
	
			new CmdStruct("SKIP", Emitter::EmitSingleByte, 0x86, 1, 0),
	
			new CmdStruct("STRING", Emitter::EmitSingleByte, 0x88, 1, Main.C_CASE),
	
			// Numbers
			new CmdStruct("COMMA", Emitter::EmitSingleByte, 0x8B, 1, 0), // Number with comma
			new CmdStruct("NUM",   Emitter::EmitSingleByte, 0x8E, 1, 0), // Signed number
	
			new CmdStruct("CURRENCY", Emitter::EmitSingleByte, 0x8F, 1, 0),
	
			new CmdStruct("WAYPOINT",   Emitter::EmitSingleByte, 0x99, 1, 0), // waypoint name
			new CmdStruct("STATION",    Emitter::EmitSingleByte, 0x9A, 1, 0),
			new CmdStruct("TOWN",       Emitter::EmitSingleByte, 0x9B, 1, 0),
			new CmdStruct("CURRENCY64", Emitter::EmitSingleByte, 0x9C, 2, 0),
			// 0x9D is used for the pseudo command SETCASE
			// 0x9E is used for case switching
	
			// 0x9E=158 is the LAST special character we may use.
	
			new CmdStruct("UPARROW", Emitter::EmitSingleByte, 0x80, 0, 0),
	
			new CmdStruct("NBSP",       Emitter::EmitSingleByte, 0xA0, 0, Main.C_DONTCOUNT),
			new CmdStruct("POUNDSIGN",  Emitter::EmitSingleByte, 0xA3, 0, 0),
			new CmdStruct("YENSIGN",    Emitter::EmitSingleByte, 0xA5, 0, 0),
			new CmdStruct("COPYRIGHT",  Emitter::EmitSingleByte, 0xA9, 0, 0),
			new CmdStruct("DOWNARROW",  Emitter::EmitSingleByte, 0xAA, 0, 0),
			new CmdStruct("CHECKMARK",  Emitter::EmitSingleByte, 0xAC, 0, 0),
			new CmdStruct("CROSS",      Emitter::EmitSingleByte, 0xAD, 0, 0),
			new CmdStruct("RIGHTARROW", Emitter::EmitSingleByte, 0xAF, 0, 0),
	
			new CmdStruct("TRAIN", Emitter::EmitSingleByte, 0x94, 0, 0),
			new CmdStruct("LORRY", Emitter::EmitSingleByte, 0x95, 0, 0),
			new CmdStruct("BUS",   Emitter::EmitSingleByte, 0x96, 0, 0),
			new CmdStruct("PLANE", Emitter::EmitSingleByte, 0x97, 0, 0),
			new CmdStruct("SHIP",  Emitter::EmitSingleByte, 0x98, 0, 0),
	
			new CmdStruct("SMALLUPARROW",   Emitter::EmitSingleByte, 0x90, 0, 0),
			new CmdStruct("SMALLDOWNARROW", Emitter::EmitSingleByte, 0x91, 0, 0)
	};

}
