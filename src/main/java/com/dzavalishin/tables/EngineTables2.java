package com.dzavalishin.tables;

import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.AircraftVehicleInfo;
import com.dzavalishin.game.RailVehicleInfo;
import com.dzavalishin.game.RoadVehicleInfo;
import com.dzavalishin.game.ShipVehicleInfo;
import com.dzavalishin.struct.EngineInfo;

public class EngineTables2 
{




	/** @file table/engines.h
	 * This file contains all the data for vehicles
	 */

	//#include "../sound.h"

	/* * Writes the properties of a vehicle into the EngineInfo struct.
	 * @see EngineInfo
	 * @param a Introduction date
	 * @param e Rail Type of the vehicle
	 * @param f Bitmask of the climates
	 */
	//#define MK(a, b, c, d, e, f) { a, b, c, d, e, f, 0 }
	/* * Writes the properties of a train carriage into the EngineInfo struct.
	 * @see EngineInfo
	 * @param a Introduction date
	 * @param e Rail Type of the vehicle
	 * @param f Bitmask of the climates
	 * @note the 0x80 in parameter b sets the "is carriage bit"
	 */
	//#define MW(a, b, c, d, e, f) { a, b | 0x80, c, d, e, f, 0 }

	// Rail types
	// R = Conventional railway
	// M = Monorail
	// L = MagLev
	private static final int R = 0;
	private static final int M = 1;
	private static final int L = 2;
	// Climates
	// T = Temperate
	// A = Arctic
	// S = Sub-Tropic
	// Y = Toyland
	private static final int T = 1;
	private static final int A = 2;
	private static final int S = 4;
	private static final int Y = 8;

	public final static EngineInfo orig_engine_info[] = {
			new EngineInit(  1827,  20,  15,  30, R, T      ), /*   0 Kirby Paul Tank (Steam) */
			new EngineInit( 12784,  20,  22,  30, R,   A|S  ), /*   1 MJS 250 (Diesel) */
			new EngineInit(  9497,  20,  20,  50, R,       Y), /*   2 Ploddyphut Choo-Choo */
			new EngineInit( 11688,  20,  20,  30, R,       Y), /*   3 Powernaut Choo-Choo */
			new EngineInit( 16802,  20,  20,  30, R,       Y), /*   4 Mightymover Choo-Choo */
			new EngineInit( 18993,  20,  20,  30, R,       Y), /*   5 Ploddyphut Diesel */
			new EngineInit( 20820,  20,  20,  30, R,       Y), /*   6 Powernaut Diesel */
			new EngineInit(  8766,  20,  20,  30, R,   A|S  ), /*   7 Wills 2-8-0 (Steam) */
			new EngineInit(  5114,  20,  21,  30, R, T      ), /*   8 Chaney 'Jubilee' (Steam) */
			new EngineInit(  5479,  20,  20,  30, R, T      ), /*   9 Ginzu 'A4' (Steam) */
			new EngineInit( 12419,  20,  23,  25, R, T      ), /*  10 SH '8P' (Steam) */
			new EngineInit( 13149,  20,  12,  30, R, T      ), /*  11 Manley-Morel DMU (Diesel) */
			new EngineInit( 23376,  20,  15,  35, R, T      ), /*  12 'Dash' (Diesel) */
			new EngineInit( 14976,  20,  18,  28, R, T      ), /*  13 SH/Hendry '25' (Diesel) */
			new EngineInit( 14245,  20,  20,  30, R, T      ), /*  14 UU '37' (Diesel) */
			new EngineInit( 15341,  20,  22,  33, R, T      ), /*  15 Floss '47' (Diesel) */
			new EngineInit( 14976,  20,  20,  25, R,   A|S  ), /*  16 CS 4000 (Diesel) */
			new EngineInit( 16437,  20,  20,  30, R,   A|S  ), /*  17 CS 2400 (Diesel) */
			new EngineInit( 18993,  20,  22,  30, R,   A|S  ), /*  18 Centennial (Diesel) */
			new EngineInit( 13880,  20,  22,  30, R,   A|S  ), /*  19 Kelling 3100 (Diesel) */
			new EngineInit( 20454,  20,  22,  30, R,   A|S  ), /*  20 Turner Turbo (Diesel) */
			new EngineInit( 16071,  20,  22,  30, R,   A|S  ), /*  21 MJS 1000 (Diesel) */
			new EngineInit( 20820,  20,  20,  25, R, T      ), /*  22 SH '125' (Diesel) */
			new EngineInit( 16437,  20,  23,  30, R, T      ), /*  23 SH '30' (Electric) */
			new EngineInit( 19359,  20,  23,  80, R, T      ), /*  24 SH '40' (Electric) */
			new EngineInit( 23376,  20,  25,  30, R, T      ), /*  25 'T.I.M.' (Electric) */
			new EngineInit( 26298,  20,  25,  50, R, T      ), /*  26 'AsiaStar' (Electric) */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A|S|Y), /*  27 Passenger Carriage */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A|S|Y), /*  28 Mail Van */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A    ), /*  29 Coal Truck */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A|S  ), /*  30 Oil Tanker */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A    ), /*  31 Livestock Van */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A|S  ), /*  32 Goods Van */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A|S  ), /*  33 Grain Hopper */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A|S  ), /*  34 Wood Truck */
			new CarriageInfo(  1827,  20,  20,  50, R, T      ), /*  35 Iron Ore Hopper */
			new CarriageInfo(  1827,  20,  20,  50, R, T      ), /*  36 Steel Truck */
			new CarriageInfo(  1827,  20,  20,  50, R, T|A|S  ), /*  37 Armoured Van */
			new CarriageInfo(  1827,  20,  20,  50, R,   A|S  ), /*  38 Food Van */
			new CarriageInfo(  1827,  20,  20,  50, R,   A    ), /*  39 Paper Truck */
			new CarriageInfo(  1827,  20,  20,  50, R,     S  ), /*  40 Copper Ore Hopper */
			new CarriageInfo(  1827,  20,  20,  50, R,     S  ), /*  41 Water Tanker */
			new CarriageInfo(  1827,  20,  20,  50, R,     S  ), /*  42 Fruit Truck */
			new CarriageInfo(  1827,  20,  20,  50, R,     S  ), /*  43 Rubber Truck */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  44 Sugar Truck */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  45 Candyfloss Hopper */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  46 Toffee Hopper */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  47 Bubble Van */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  48 Cola Tanker */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  49 Sweet Van */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  50 Toy Van */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  51 Battery Truck */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  52 Fizzy Drink Truck */
			new CarriageInfo(  1827,  20,  20,  50, R,       Y), /*  53 Plastic Truck */
			new EngineInit( 28490,  20,  20,  50, M, T|A|S  ), /*  54 'X2001' (Electric) */
			new EngineInit( 31047,  20,  20,  50, M, T|A|S  ), /*  55 'Millennium Z1' (Electric) */
			new EngineInit( 28855,  20,  20,  50, M,       Y), /*  56 Wizzowow Z99 */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A|S|Y), /*  57 Passenger Carriage */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A|S|Y), /*  58 Mail Van */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A    ), /*  59 Coal Truck */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A|S  ), /*  60 Oil Tanker */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A    ), /*  61 Livestock Van */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A|S  ), /*  62 Goods Van */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A|S  ), /*  63 Grain Hopper */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A|S  ), /*  64 Wood Truck */
			new CarriageInfo(  1827,  20,  20,  50, M, T      ), /*  65 Iron Ore Hopper */
			new CarriageInfo(  1827,  20,  20,  50, M, T      ), /*  66 Steel Truck */
			new CarriageInfo(  1827,  20,  20,  50, M, T|A|S  ), /*  67 Armoured Van */
			new CarriageInfo(  1827,  20,  20,  50, M,   A|S  ), /*  68 Food Van */
			new CarriageInfo(  1827,  20,  20,  50, M,   A    ), /*  69 Paper Truck */
			new CarriageInfo(  1827,  20,  20,  50, M,     S  ), /*  70 Copper Ore Hopper */
			new CarriageInfo(  1827,  20,  20,  50, M,     S  ), /*  71 Water Tanker */
			new CarriageInfo(  1827,  20,  20,  50, M,     S  ), /*  72 Fruit Truck */
			new CarriageInfo(  1827,  20,  20,  50, M,     S  ), /*  73 Rubber Truck */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  74 Sugar Truck */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  75 Candyfloss Hopper */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  76 Toffee Hopper */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  77 Bubble Van */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  78 Cola Tanker */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  79 Sweet Van */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  80 Toy Van */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  81 Battery Truck */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  82 Fizzy Drink Truck */
			new CarriageInfo(  1827,  20,  20,  50, M,       Y), /*  83 Plastic Truck */
			new EngineInit( 36525,  20,  20,  50, L, T|A|S  ), /*  84 Lev1 'Leviathan' (Electric) */
			new EngineInit( 39447,  20,  20,  50, L, T|A|S  ), /*  85 Lev2 'Cyclops' (Electric) */
			new EngineInit( 42004,  20,  20,  50, L, T|A|S  ), /*  86 Lev3 'Pegasus' (Electric) */
			new EngineInit( 42735,  20,  20,  50, L, T|A|S  ), /*  87 Lev4 'Chimaera' (Electric) */
			new EngineInit( 36891,  20,  20,  60, L,       Y), /*  88 Wizzowow Rocketeer */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A|S|Y), /*  89 Passenger Carriage */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A|S|Y), /*  90 Mail Van */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A    ), /*  91 Coal Truck */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A|S  ), /*  92 Oil Tanker */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A    ), /*  93 Livestock Van */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A|S  ), /*  94 Goods Van */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A|S  ), /*  95 Grain Hopper */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A|S  ), /*  96 Wood Truck */
			new CarriageInfo(  1827,  20,  20,  50, L, T      ), /*  97 Iron Ore Hopper */
			new CarriageInfo(  1827,  20,  20,  50, L, T      ), /*  98 Steel Truck */
			new CarriageInfo(  1827,  20,  20,  50, L, T|A|S  ), /*  99 Armoured Van */
			new CarriageInfo(  1827,  20,  20,  50, L,   A|S  ), /* 100 Food Van */
			new CarriageInfo(  1827,  20,  20,  50, L,   A    ), /* 101 Paper Truck */
			new CarriageInfo(  1827,  20,  20,  50, L,     S  ), /* 102 Copper Ore Hopper */
			new CarriageInfo(  1827,  20,  20,  50, L,     S  ), /* 103 Water Tanker */
			new CarriageInfo(  1827,  20,  20,  50, L,     S  ), /* 104 Fruit Truck */
			new CarriageInfo(  1827,  20,  20,  50, L,     S  ), /* 105 Rubber Truck */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 106 Sugar Truck */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 107 Candyfloss Hopper */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 108 Toffee Hopper */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 109 Bubble Van */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 110 Cola Tanker */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 111 Sweet Van */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 112 Toy Van */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 113 Battery Truck */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 114 Fizzy Drink Truck */
			new CarriageInfo(  1827,  20,  20,  50, L,       Y), /* 115 Plastic Truck */
			new EngineInit(  3378,  20,  12,  40, 0, T|A|S  ), /* 116 MPS Regal Bus */
			new EngineInit( 16071,  20,  15,  30, 0, T|A|S  ), /* 117 Hereford Leopard Bus */
			new EngineInit( 24107,  20,  15,  40, 0, T|A|S  ), /* 118 Foster Bus */
			new EngineInit( 32142,  20,  15,  80, 0, T|A|S  ), /* 119 Foster MkII Superbus */
			new EngineInit(  9132,  20,  15,  40, 0,       Y), /* 120 Ploddyphut MkI Bus */
			new EngineInit( 18993,  20,  15,  40, 0,       Y), /* 121 Ploddyphut MkII Bus */
			new EngineInit( 32873,  20,  15,  80, 0,       Y), /* 122 Ploddyphut MkIII Bus */
			new EngineInit(  5479,  20,  15,  55, 0, T|A    ), /* 123 Balogh Coal Truck */
			new EngineInit( 20089,  20,  15,  55, 0, T|A    ), /* 124 Uhl Coal Truck */
			new EngineInit( 33969,  20,  15,  85, 0, T|A    ), /* 125 DW Coal Truck */
			new EngineInit(  5479,  20,  15,  55, 0, T|A|S  ), /* 126 MPS Mail Truck */
			new EngineInit( 21550,  20,  15,  55, 0, T|A|S  ), /* 127 Reynard Mail Truck */
			new EngineInit( 35795,  20,  15,  85, 0, T|A|S  ), /* 128 Perry Mail Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 129 MightyMover Mail Truck */
			new EngineInit( 21550,  20,  15,  55, 0,       Y), /* 130 Powernaught Mail Truck */
			new EngineInit( 35795,  20,  15,  85, 0,       Y), /* 131 Wizzowow Mail Truck */
			new EngineInit(  5479,  20,  15,  55, 0, T|A|S  ), /* 132 Witcombe Oil Tanker */
			new EngineInit( 19359,  20,  15,  55, 0, T|A|S  ), /* 133 Foster Oil Tanker */
			new EngineInit( 31047,  20,  15,  85, 0, T|A|S  ), /* 134 Perry Oil Tanker */
			new EngineInit(  5479,  20,  15,  55, 0, T|A    ), /* 135 Talbott Livestock Van */
			new EngineInit( 21915,  20,  15,  55, 0, T|A    ), /* 136 Uhl Livestock Van */
			new EngineInit( 37256,  20,  15,  85, 0, T|A    ), /* 137 Foster Livestock Van */
			new EngineInit(  5479,  20,  15,  55, 0, T|A|S  ), /* 138 Balogh Goods Truck */
			new EngineInit( 19724,  20,  15,  55, 0, T|A|S  ), /* 139 Craighead Goods Truck */
			new EngineInit( 31047,  20,  15,  85, 0, T|A|S  ), /* 140 Goss Goods Truck */
			new EngineInit(  5479,  20,  15,  55, 0, T|A|S  ), /* 141 Hereford Grain Truck */
			new EngineInit( 21185,  20,  15,  55, 0, T|A|S  ), /* 142 Thomas Grain Truck */
			new EngineInit( 32873,  20,  15,  85, 0, T|A|S  ), /* 143 Goss Grain Truck */
			new EngineInit(  5479,  20,  15,  55, 0, T|A|S  ), /* 144 Witcombe Wood Truck */
			new EngineInit( 19724,  20,  15,  55, 0, T|A|S  ), /* 145 Foster Wood Truck */
			new EngineInit( 35430,  20,  15,  85, 0, T|A|S  ), /* 146 Moreland Wood Truck */
			new EngineInit(  5479,  20,  15,  55, 0, T      ), /* 147 MPS Iron Ore Truck */
			new EngineInit( 20820,  20,  15,  55, 0, T      ), /* 148 Uhl Iron Ore Truck */
			new EngineInit( 33238,  20,  15,  85, 0, T      ), /* 149 Chippy Iron Ore Truck */
			new EngineInit(  5479,  20,  15,  55, 0, T      ), /* 150 Balogh Steel Truck */
			new EngineInit( 21185,  20,  15,  55, 0, T      ), /* 151 Uhl Steel Truck */
			new EngineInit( 31777,  20,  15,  85, 0, T      ), /* 152 Kelling Steel Truck */
			new EngineInit(  5479,  20,  15,  55, 0, T|A|S  ), /* 153 Balogh Armoured Truck */
			new EngineInit( 22281,  20,  15,  55, 0, T|A|S  ), /* 154 Uhl Armoured Truck */
			new EngineInit( 33603,  20,  15,  85, 0, T|A|S  ), /* 155 Foster Armoured Truck */
			new EngineInit(  5479,  20,  15,  55, 0,   A|S  ), /* 156 Foster Food Van */
			new EngineInit( 18628,  20,  15,  55, 0,   A|S  ), /* 157 Perry Food Van */
			new EngineInit( 30681,  20,  15,  85, 0,   A|S  ), /* 158 Chippy Food Van */
			new EngineInit(  5479,  20,  15,  55, 0,   A    ), /* 159 Uhl Paper Truck */
			new EngineInit( 21185,  20,  15,  55, 0,   A    ), /* 160 Balogh Paper Truck */
			new EngineInit( 31777,  20,  15,  85, 0,   A    ), /* 161 MPS Paper Truck */
			new EngineInit(  5479,  20,  15,  55, 0,     S  ), /* 162 MPS Copper Ore Truck */
			new EngineInit( 20820,  20,  15,  55, 0,     S  ), /* 163 Uhl Copper Ore Truck */
			new EngineInit( 33238,  20,  15,  85, 0,     S  ), /* 164 Goss Copper Ore Truck */
			new EngineInit(  5479,  20,  15,  55, 0,     S  ), /* 165 Uhl Water Tanker */
			new EngineInit( 20970,  20,  15,  55, 0,     S  ), /* 166 Balogh Water Tanker */
			new EngineInit( 33388,  20,  15,  85, 0,     S  ), /* 167 MPS Water Tanker */
			new EngineInit(  5479,  20,  15,  55, 0,     S  ), /* 168 Balogh Fruit Truck */
			new EngineInit( 21335,  20,  15,  55, 0,     S  ), /* 169 Uhl Fruit Truck */
			new EngineInit( 33753,  20,  15,  85, 0,     S  ), /* 170 Kelling Fruit Truck */
			new EngineInit(  5479,  20,  15,  55, 0,     S  ), /* 171 Balogh Rubber Truck */
			new EngineInit( 20604,  20,  15,  55, 0,     S  ), /* 172 Uhl Rubber Truck */
			new EngineInit( 33023,  20,  15,  85, 0,     S  ), /* 173 RMT Rubber Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 174 MightyMover Sugar Truck */
			new EngineInit( 19724,  20,  15,  55, 0,       Y), /* 175 Powernaught Sugar Truck */
			new EngineInit( 33238,  20,  15,  85, 0,       Y), /* 176 Wizzowow Sugar Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 177 MightyMover Cola Truck */
			new EngineInit( 20089,  20,  15,  55, 0,       Y), /* 178 Powernaught Cola Truck */
			new EngineInit( 33603,  20,  15,  85, 0,       Y), /* 179 Wizzowow Cola Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 180 MightyMover Candyfloss Truck */
			new EngineInit( 20454,  20,  15,  55, 0,       Y), /* 181 Powernaught Candyfloss Truck */
			new EngineInit( 33969,  20,  15,  85, 0,       Y), /* 182 Wizzowow Candyfloss Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 183 MightyMover Toffee Truck */
			new EngineInit( 20820,  20,  15,  55, 0,       Y), /* 184 Powernaught Toffee Truck */
			new EngineInit( 34334,  20,  15,  85, 0,       Y), /* 185 Wizzowow Toffee Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 186 MightyMover Toy Van */
			new EngineInit( 21185,  20,  15,  55, 0,       Y), /* 187 Powernaught Toy Van */
			new EngineInit( 34699,  20,  15,  85, 0,       Y), /* 188 Wizzowow Toy Van */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 189 MightyMover Sweet Truck */
			new EngineInit( 21550,  20,  15,  55, 0,       Y), /* 190 Powernaught Sweet Truck */
			new EngineInit( 35064,  20,  15,  85, 0,       Y), /* 191 Wizzowow Sweet Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 192 MightyMover Battery Truck */
			new EngineInit( 19874,  20,  15,  55, 0,       Y), /* 193 Powernaught Battery Truck */
			new EngineInit( 35430,  20,  15,  85, 0,       Y), /* 194 Wizzowow Battery Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 195 MightyMover Fizzy Drink Truck */
			new EngineInit( 20239,  20,  15,  55, 0,       Y), /* 196 Powernaught Fizzy Drink Truck */
			new EngineInit( 35795,  20,  15,  85, 0,       Y), /* 197 Wizzowow Fizzy Drink Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 198 MightyMover Plastic Truck */
			new EngineInit( 20604,  20,  15,  55, 0,       Y), /* 199 Powernaught Plastic Truck */
			new EngineInit( 32873,  20,  15,  85, 0,       Y), /* 200 Wizzowow Plastic Truck */
			new EngineInit(  5479,  20,  15,  55, 0,       Y), /* 201 MightyMover Bubble Truck */
			new EngineInit( 20970,  20,  15,  55, 0,       Y), /* 202 Powernaught Bubble Truck */
			new EngineInit( 33023,  20,  15,  85, 0,       Y), /* 203 Wizzowow Bubble Truck */
			new EngineInit(  2922,   5,  30,  50, 0, T|A|S  ), /* 204 MPS Oil Tanker */
			new EngineInit( 17167,   5,  30,  90, 0, T|A|S  ), /* 205 CS-Inc. Oil Tanker */
			new EngineInit(  2192,   5,  30,  55, 0, T|A|S  ), /* 206 MPS Passenger Ferry */
			new EngineInit( 18628,   5,  30,  90, 0, T|A|S  ), /* 207 FFP Passenger Ferry */
			new EngineInit( 17257,  10,  25,  90, 0, T|A|S  ), /* 208 Bakewell 300 Hovercraft */
			new EngineInit(  9587,   5,  30,  40, 0,       Y), /* 209 Chugger-Chug Passenger Ferry */
			new EngineInit( 20544,   5,  30,  90, 0,       Y), /* 210 Shivershake Passenger Ferry */
			new EngineInit(  2557,   5,  30,  55, 0, T|A|S  ), /* 211 Yate Cargo ship */
			new EngineInit( 19724,   5,  30,  98, 0, T|A|S  ), /* 212 Bakewell Cargo ship */
			new EngineInit(  9587,   5,  30,  45, 0,       Y), /* 213 Mightymover Cargo ship */
			new EngineInit( 22371,   5,  30,  90, 0,       Y), /* 214 Powernaut Cargo ship */
			new EngineInit(  2922,  20,  20,  20, 0, T|A|S  ), /* 215 Sampson U52 */
			new EngineInit(  9922,  20,  24,  20, 0, T|A|S  ), /* 216 Coleman Count */
			new EngineInit( 12659,  20,  18,  20, 0, T|A|S  ), /* 217 FFP Dart */
			new EngineInit( 17652,  20,  25,  35, 0, T|A|S  ), /* 218 Yate Haugan */
			new EngineInit(  4929,  20,  30,  30, 0, T|A|S  ), /* 219 Bakewell Cotswald LB-3 */
			new EngineInit( 13695,  20,  23,  25, 0, T|A|S  ), /* 220 Bakewell Luckett LB-8 */
			new EngineInit( 16341,  20,  26,  30, 0, T|A|S  ), /* 221 Bakewell Luckett LB-9 */
			new EngineInit( 21395,  20,  25,  30, 0, T|A|S  ), /* 222 Bakewell Luckett LB80 */
			new EngineInit( 18263,  20,  20,  30, 0, T|A|S  ), /* 223 Bakewell Luckett LB-10 */
			new EngineInit( 25233,  20,  25,  30, 0, T|A|S  ), /* 224 Bakewell Luckett LB-11 */
			new EngineInit( 15371,  20,  22,  25, 0, T|A|S  ), /* 225 Yate Aerospace YAC 1-11 */
			new EngineInit( 15461,  20,  25,  25, 0, T|A|S  ), /* 226 Darwin 100 */
			new EngineInit( 16952,  20,  22,  25, 0, T|A|S  ), /* 227 Darwin 200 */
			new EngineInit( 17227,  20,  25,  30, 0, T|A|S  ), /* 228 Darwin 300 */
			new EngineInit( 22371,  20,  25,  35, 0, T|A|S  ), /* 229 Darwin 400 */
			new EngineInit( 22341,  20,  25,  30, 0, T|A|S  ), /* 230 Darwin 500 */
			new EngineInit( 27209,  20,  25,  30, 0, T|A|S  ), /* 231 Darwin 600 */
			new EngineInit( 17988,  20,  20,  30, 0, T|A|S  ), /* 232 Guru Galaxy */
			new EngineInit( 18993,  20,  24,  35, 0, T|A|S  ), /* 233 Airtaxi A21 */
			new EngineInit( 22401,  20,  24,  30, 0, T|A|S  ), /* 234 Airtaxi A31 */
			new EngineInit( 24472,  20,  24,  30, 0, T|A|S  ), /* 235 Airtaxi A32 */
			new EngineInit( 26724,  20,  24,  30, 0, T|A|S  ), /* 236 Airtaxi A33 */
			new EngineInit( 22005,  20,  25,  30, 0, T|A|S  ), /* 237 Yate Aerospace YAe46 */
			new EngineInit( 24107,  20,  20,  35, 0, T|A|S  ), /* 238 Dinger 100 */
			new EngineInit( 29310,  20,  25,  60, 0, T|A|S  ), /* 239 AirTaxi A34-1000 */
			new EngineInit( 35520,  20,  22,  30, 0, T|A|S  ), /* 240 Yate Z-Shuttle */
			new EngineInit( 36981,  20,  22,  30, 0, T|A|S  ), /* 241 Kelling K1 */
			new EngineInit( 38807,  20,  22,  50, 0, T|A|S  ), /* 242 Kelling K6 */
			new EngineInit( 42094,  20,  25,  30, 0, T|A|S  ), /* 243 Kelling K7 */
			new EngineInit( 44651,  20,  23,  30, 0, T|A|S  ), /* 244 Darwin 700 */
			new EngineInit( 40268,  20,  25,  30, 0, T|A|S  ), /* 245 FFP Hyperdart 2 */
			new EngineInit( 33693,  20,  25,  50, 0, T|A|S  ), /* 246 Dinger 200 */
			new EngineInit( 32963,  20,  20,  60, 0, T|A|S  ), /* 247 Dinger 1000 */
			new EngineInit(  9222,  20,  20,  35, 0,       Y), /* 248 Ploddyphut 100 */
			new EngineInit( 12874,  20,  20,  35, 0,       Y), /* 249 Ploddyphut 500 */
			new EngineInit( 16892,  20,  20,  35, 0,       Y), /* 250 Flashbang X1 */
			new EngineInit( 21275,  20,  20,  99, 0,       Y), /* 251 Juggerplane M1 */
			new EngineInit( 23832,  20,  20,  99, 0,       Y), /* 252 Flashbang Wizzer */
			new EngineInit( 13575,  20,  20,  40, 0, T|A|S  ), /* 253 Tricario Helicopter */
			new EngineInit( 28215,  20,  20,  30, 0, T|A|S  ), /* 254 Guru X2 Helicopter */
			new EngineInit( 13575,  20,  20,  99, 0,       Y), /* 255  */
	};


	private static final int MM = EngineTables.RVI_MULTIHEAD;
	private static final int W = EngineTables.RVI_WAGON;
	
	//final RailVehicleInfo orig_rail_vehicle_info[NUM_TRAIN_ENGINES] = {
	public final static RailVehicleInfo orig_rail_vehicle_info[] = {
                               // image_index  max_speed (kph)      running_cost_base                 callbackmask    shortened factor
                               // |  flags     |        power (hp)  |    running_cost_class           |   powered wagons power
                               // |  |    base_cost     |    weight      |    capacity                |   |   powered wagons weight
                               // |  |    |    |        |    |      |    |    |    cargo_type         |   |   |   visual effects
                               // |  |    |    |        |    |      |    |    |    |                  |   |   |   |   |
			new RailVehicleInfo(  2, 0,   7,  64,     300,  47,    50,   0,   0,   0               ,  0,  0,  0,  0,  0 ), /*   0 */
			new RailVehicleInfo( 19, 0,   8,  80,     600,  65,    65,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*   1 */
			new RailVehicleInfo(  2, 0,  10,  72,     400,  85,    90,   0,   0,   0               ,  0,  0,  0,  0,  0 ), /*   2 */
			new RailVehicleInfo(  0, 0,  15,  96,     900, 130,   130,   0,   0,   0               ,  0,  0,  0,  0,  0 ), /*   3 */
			new RailVehicleInfo(  1, 0,  19, 112,    1000, 140,   145,   0,   0,   0               ,  0,  0,  0,  0,  0 ), /*   4 */
			new RailVehicleInfo( 12, 0,  16, 120,    1400,  95,   125,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*   5 */
			new RailVehicleInfo( 14, 0,  20, 152,    2000, 120,   135,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*   6 */
			new RailVehicleInfo(  3, 0,  14,  88,    1100, 145,   130,   0,   0,   0               ,  0,  0,  0,  0,  0 ), /*   7 */
			new RailVehicleInfo(  0, 0,  13, 112,    1000, 131,   120,   0,   0,   0               ,  0,  0,  0,  0,  0 ), /*   8 */
			new RailVehicleInfo(  1, 0,  19, 128,    1200, 162,   140,   0,   0,   0               ,  0,  0,  0,  0,  0 ), /*   9 */
			new RailVehicleInfo(  0, 0,  22, 144,    1600, 170,   130,   0,   0,   0               ,  0,  0,  0,  0,  0 ), /*  10 */
			new RailVehicleInfo(  8, MM,  11, 112,   600/2,32/2,  85/2,   1,  38,   AcceptedCargo.CT_PASSENGERS   ,  0,  0,  0,  0,  0 ), /*  11 */
			new RailVehicleInfo( 10, MM,  14, 120,   700/2,38/2,  70/2,   1,  40,   AcceptedCargo.CT_PASSENGERS   ,  0,  0,  0,  0,  0 ), /*  12 */
			new RailVehicleInfo(  4, 0,  15, 128,    1250,  72,    95,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  13 */
			new RailVehicleInfo(  5, 0,  17, 144,    1750, 101,   120,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  14 */
			new RailVehicleInfo(  4, 0,  18, 160,    2580, 112,   140,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  15 */
			new RailVehicleInfo( 14, 0,  23,  96,    4000, 150,   135,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  16 */
			new RailVehicleInfo( 12, 0,  16, 112,    2400, 120,   105,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  17 */
			new RailVehicleInfo( 13, 0,  30, 112,    6600, 207,   155,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  18 */
			new RailVehicleInfo( 15, 0,  18, 104,    1500, 110,   105,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  19 */
			new RailVehicleInfo( 16, MM,  35, 160,  3500/2,95/2, 205/2,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  20 */
			new RailVehicleInfo( 18, 0,  21, 104,    2200, 120,   145,   1,   0,   0               ,  0,  0,  0,  0,  0 ), /*  21 */
			new RailVehicleInfo(  6, MM,  20, 200,  4500/2,70/2, 190/2,   1,   4,   AcceptedCargo.CT_MAIL         ,  0,  0,  0,  0,  0 ), /*  22 */
			new RailVehicleInfo( 20, 0,  26, 160,    3600,  84,   180,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  23 */
			new RailVehicleInfo( 20, 0,  30, 176,    5000,  82,   205,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  24 */
			new RailVehicleInfo( 21, MM,  40, 240,  7000/2,90/2, 240/2,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  25 */
			new RailVehicleInfo( 23, MM,  43, 264,  8000/2,95/2, 250/2,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  26 */
			new RailVehicleInfo( 33, W, 247,   0,       0,  25,     0,   0,  40,   AcceptedCargo.CT_PASSENGERS   ,  0,  0,  0,  0,  0 ), /*  27 */
			new RailVehicleInfo( 35, W, 228,   0,       0,  21,     0,   0,  30,   AcceptedCargo.CT_MAIL         ,  0,  0,  0,  0,  0 ), /*  28 */
			new RailVehicleInfo( 34, W, 176,   0,       0,  18,     0,   0,  30,   AcceptedCargo.CT_COAL         ,  0,  0,  0,  0,  0 ), /*  29 */
			new RailVehicleInfo( 36, W, 200,   0,       0,  24,     0,   0,  30,   AcceptedCargo.CT_OIL          ,  0,  0,  0,  0,  0 ), /*  30 */
			new RailVehicleInfo( 37, W, 192,   0,       0,  20,     0,   0,  25,   AcceptedCargo.CT_LIVESTOCK    ,  0,  0,  0,  0,  0 ), /*  31 */
			new RailVehicleInfo( 38, W, 190,   0,       0,  21,     0,   0,  25,   AcceptedCargo.CT_GOODS        ,  0,  0,  0,  0,  0 ), /*  32 */
			new RailVehicleInfo( 39, W, 182,   0,       0,  19,     0,   0,  30,   AcceptedCargo.CT_GRAIN        ,  0,  0,  0,  0,  0 ), /*  33 */
			new RailVehicleInfo( 40, W, 181,   0,       0,  16,     0,   0,  30,   AcceptedCargo.CT_WOOD         ,  0,  0,  0,  0,  0 ), /*  34 */
			new RailVehicleInfo( 41, W, 179,   0,       0,  19,     0,   0,  30,   AcceptedCargo.CT_IRON_ORE     ,  0,  0,  0,  0,  0 ), /*  35 */
			new RailVehicleInfo( 42, W, 196,   0,       0,  18,     0,   0,  20,   AcceptedCargo.CT_STEEL        ,  0,  0,  0,  0,  0 ), /*  36 */
			new RailVehicleInfo( 43, W, 255,   0,       0,  30,     0,   0,  20,   AcceptedCargo.CT_VALUABLES    ,  0,  0,  0,  0,  0 ), /*  37 */
			new RailVehicleInfo( 44, W, 191,   0,       0,  22,     0,   0,  25,   AcceptedCargo.CT_FOOD         ,  0,  0,  0,  0,  0 ), /*  38 */
			new RailVehicleInfo( 45, W, 196,   0,       0,  18,     0,   0,  20,   AcceptedCargo.CT_PAPER        ,  0,  0,  0,  0,  0 ), /*  39 */
			new RailVehicleInfo( 46, W, 179,   0,       0,  19,     0,   0,  30,   AcceptedCargo.CT_COPPER_ORE   ,  0,  0,  0,  0,  0 ), /*  40 */
			new RailVehicleInfo( 47, W, 199,   0,       0,  25,     0,   0,  25,   AcceptedCargo.CT_WATER        ,  0,  0,  0,  0,  0 ), /*  41 */
			new RailVehicleInfo( 48, W, 182,   0,       0,  18,     0,   0,  25,   AcceptedCargo.CT_FRUIT        ,  0,  0,  0,  0,  0 ), /*  42 */
			new RailVehicleInfo( 49, W, 185,   0,       0,  19,     0,   0,  21,   AcceptedCargo.CT_RUBBER       ,  0,  0,  0,  0,  0 ), /*  43 */
			new RailVehicleInfo( 50, W, 176,   0,       0,  19,     0,   0,  30,   AcceptedCargo.CT_SUGAR        ,  0,  0,  0,  0,  0 ), /*  44 */
			new RailVehicleInfo( 51, W, 178,   0,       0,  20,     0,   0,  30,   AcceptedCargo.CT_COTTON_CANDY ,  0,  0,  0,  0,  0 ), /*  45 */
			new RailVehicleInfo( 52, W, 192,   0,       0,  20,     0,   0,  30,   AcceptedCargo.CT_TOFFEE       ,  0,  0,  0,  0,  0 ), /*  46 */
			new RailVehicleInfo( 53, W, 190,   0,       0,  21,     0,   0,  20,   AcceptedCargo.CT_BUBBLES      ,  0,  0,  0,  0,  0 ), /*  47 */
			new RailVehicleInfo( 54, W, 182,   0,       0,  24,     0,   0,  25,   AcceptedCargo.CT_COLA         ,  0,  0,  0,  0,  0 ), /*  48 */
			new RailVehicleInfo( 55, W, 181,   0,       0,  21,     0,   0,  25,   AcceptedCargo.CT_CANDY        ,  0,  0,  0,  0,  0 ), /*  49 */
			new RailVehicleInfo( 56, W, 183,   0,       0,  21,     0,   0,  20,   AcceptedCargo.CT_TOYS         ,  0,  0,  0,  0,  0 ), /*  50 */
			new RailVehicleInfo( 57, W, 196,   0,       0,  18,     0,   0,  22,   AcceptedCargo.CT_BATTERIES    ,  0,  0,  0,  0,  0 ), /*  51 */
			new RailVehicleInfo( 58, W, 193,   0,       0,  18,     0,   0,  25,   AcceptedCargo.CT_FIZZY_DRINKS ,  0,  0,  0,  0,  0 ), /*  52 */
			new RailVehicleInfo( 59, W, 191,   0,       0,  18,     0,   0,  30,   AcceptedCargo.CT_PLASTIC      ,  0,  0,  0,  0,  0 ), /*  53 */
			new RailVehicleInfo( 25, 0,  52, 304,    9000,  95,   230,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  54 */
			new RailVehicleInfo( 26, MM,  60, 336, 10000/2,85/2, 240/2,   2,  25,   AcceptedCargo.CT_PASSENGERS   ,  0,  0,  0,  0,  0 ), /*  55 */
			new RailVehicleInfo( 26, 0,  53, 320,    5000,  95,   230,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  56 */
			new RailVehicleInfo( 60, W, 247,   0,       0,  25,     0,   0,  45,   AcceptedCargo.CT_PASSENGERS   ,  0,  0,  0,  0,  0 ), /*  57 */
			new RailVehicleInfo( 62, W, 228,   0,       0,  21,     0,   0,  35,   AcceptedCargo.CT_MAIL         ,  0,  0,  0,  0,  0 ), /*  58 */
			new RailVehicleInfo( 61, W, 176,   0,       0,  18,     0,   0,  35,   AcceptedCargo.CT_COAL         ,  0,  0,  0,  0,  0 ), /*  59 */
			new RailVehicleInfo( 63, W, 200,   0,       0,  24,     0,   0,  35,   AcceptedCargo.CT_OIL          ,  0,  0,  0,  0,  0 ), /*  60 */
			new RailVehicleInfo( 64, W, 192,   0,       0,  20,     0,   0,  30,   AcceptedCargo.CT_LIVESTOCK    ,  0,  0,  0,  0,  0 ), /*  61 */
			new RailVehicleInfo( 65, W, 190,   0,       0,  21,     0,   0,  30,   AcceptedCargo.CT_GOODS        ,  0,  0,  0,  0,  0 ), /*  62 */
			new RailVehicleInfo( 66, W, 182,   0,       0,  19,     0,   0,  35,   AcceptedCargo.CT_GRAIN        ,  0,  0,  0,  0,  0 ), /*  63 */
			new RailVehicleInfo( 67, W, 181,   0,       0,  16,     0,   0,  35,   AcceptedCargo.CT_WOOD         ,  0,  0,  0,  0,  0 ), /*  64 */
			new RailVehicleInfo( 68, W, 179,   0,       0,  19,     0,   0,  35,   AcceptedCargo.CT_IRON_ORE     ,  0,  0,  0,  0,  0 ), /*  65 */
			new RailVehicleInfo( 69, W, 196,   0,       0,  18,     0,   0,  25,   AcceptedCargo.CT_STEEL        ,  0,  0,  0,  0,  0 ), /*  66 */
			new RailVehicleInfo( 70, W, 255,   0,       0,  30,     0,   0,  25,   AcceptedCargo.CT_VALUABLES    ,  0,  0,  0,  0,  0 ), /*  67 */
			new RailVehicleInfo( 71, W, 191,   0,       0,  22,     0,   0,  30,   AcceptedCargo.CT_FOOD         ,  0,  0,  0,  0,  0 ), /*  68 */
			new RailVehicleInfo( 72, W, 196,   0,       0,  18,     0,   0,  25,   AcceptedCargo.CT_PAPER        ,  0,  0,  0,  0,  0 ), /*  69 */
			new RailVehicleInfo( 73, W, 179,   0,       0,  19,     0,   0,  35,   AcceptedCargo.CT_COPPER_ORE   ,  0,  0,  0,  0,  0 ), /*  70 */
			new RailVehicleInfo( 47, W, 199,   0,       0,  25,     0,   0,  30,   AcceptedCargo.CT_WATER        ,  0,  0,  0,  0,  0 ), /*  71 */
			new RailVehicleInfo( 48, W, 182,   0,       0,  18,     0,   0,  30,   AcceptedCargo.CT_FRUIT        ,  0,  0,  0,  0,  0 ), /*  72 */
			new RailVehicleInfo( 49, W, 185,   0,       0,  19,     0,   0,  26,   AcceptedCargo.CT_RUBBER       ,  0,  0,  0,  0,  0 ), /*  73 */
			new RailVehicleInfo( 50, W, 176,   0,       0,  19,     0,   0,  35,   AcceptedCargo.CT_SUGAR        ,  0,  0,  0,  0,  0 ), /*  74 */
			new RailVehicleInfo( 51, W, 178,   0,       0,  20,     0,   0,  35,   AcceptedCargo.CT_COTTON_CANDY ,  0,  0,  0,  0,  0 ), /*  75 */
			new RailVehicleInfo( 52, W, 192,   0,       0,  20,     0,   0,  35,   AcceptedCargo.CT_TOFFEE       ,  0,  0,  0,  0,  0 ), /*  76 */
			new RailVehicleInfo( 53, W, 190,   0,       0,  21,     0,   0,  25,   AcceptedCargo.CT_BUBBLES      ,  0,  0,  0,  0,  0 ), /*  77 */
			new RailVehicleInfo( 54, W, 182,   0,       0,  24,     0,   0,  30,   AcceptedCargo.CT_COLA         ,  0,  0,  0,  0,  0 ), /*  78 */
			new RailVehicleInfo( 55, W, 181,   0,       0,  21,     0,   0,  30,   AcceptedCargo.CT_CANDY        ,  0,  0,  0,  0,  0 ), /*  79 */
			new RailVehicleInfo( 56, W, 183,   0,       0,  21,     0,   0,  25,   AcceptedCargo.CT_TOYS         ,  0,  0,  0,  0,  0 ), /*  80 */
			new RailVehicleInfo( 57, W, 196,   0,       0,  18,     0,   0,  27,   AcceptedCargo.CT_BATTERIES    ,  0,  0,  0,  0,  0 ), /*  81 */
			new RailVehicleInfo( 58, W, 193,   0,       0,  18,     0,   0,  30,   AcceptedCargo.CT_FIZZY_DRINKS ,  0,  0,  0,  0,  0 ), /*  82 */
			new RailVehicleInfo( 59, W, 191,   0,       0,  18,     0,   0,  35,   AcceptedCargo.CT_PLASTIC      ,  0,  0,  0,  0,  0 ), /*  83 */
			new RailVehicleInfo( 28, 0,  70, 400,   10000, 105,   250,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  84 */
			new RailVehicleInfo( 29, 0,  74, 448,   12000, 120,   253,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  85 */
			new RailVehicleInfo( 30, 0,  82, 480,   15000, 130,   254,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  86 */
			new RailVehicleInfo( 31, MM,  95, 640, 20000/2,150/2,255/2,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  87 */
			new RailVehicleInfo( 28, 0,  70, 480,   10000, 120,   250,   2,   0,   0               ,  0,  0,  0,  0,  0 ), /*  88 */
			new RailVehicleInfo( 60, W, 247,   0,       0,  25,     0,   0,  47,   AcceptedCargo.CT_PASSENGERS   ,  0,  0,  0,  0,  0 ), /*  89 */
			new RailVehicleInfo( 62, W, 228,   0,       0,  21,     0,   0,  37,   AcceptedCargo.CT_MAIL         ,  0,  0,  0,  0,  0 ), /*  90 */
			new RailVehicleInfo( 61, W, 176,   0,       0,  18,     0,   0,  37,   AcceptedCargo.CT_COAL         ,  0,  0,  0,  0,  0 ), /*  91 */
			new RailVehicleInfo( 63, W, 200,   0,       0,  24,     0,   0,  37,   AcceptedCargo.CT_OIL          ,  0,  0,  0,  0,  0 ), /*  92 */
			new RailVehicleInfo( 64, W, 192,   0,       0,  20,     0,   0,  32,   AcceptedCargo.CT_LIVESTOCK    ,  0,  0,  0,  0,  0 ), /*  93 */
			new RailVehicleInfo( 65, W, 190,   0,       0,  21,     0,   0,  32,   AcceptedCargo.CT_GOODS        ,  0,  0,  0,  0,  0 ), /*  94 */
			new RailVehicleInfo( 66, W, 182,   0,       0,  19,     0,   0,  37,   AcceptedCargo.CT_GRAIN        ,  0,  0,  0,  0,  0 ), /*  95 */
			new RailVehicleInfo( 67, W, 181,   0,       0,  16,     0,   0,  37,   AcceptedCargo.CT_WOOD         ,  0,  0,  0,  0,  0 ), /*  96 */
			new RailVehicleInfo( 68, W, 179,   0,       0,  19,     0,   0,  37,   AcceptedCargo.CT_IRON_ORE     ,  0,  0,  0,  0,  0 ), /*  97 */
			new RailVehicleInfo( 69, W, 196,   0,       0,  18,     0,   0,  27,   AcceptedCargo.CT_STEEL        ,  0,  0,  0,  0,  0 ), /*  98 */
			new RailVehicleInfo( 70, W, 255,   0,       0,  30,     0,   0,  27,   AcceptedCargo.CT_VALUABLES    ,  0,  0,  0,  0,  0 ), /*  99 */
			new RailVehicleInfo( 71, W, 191,   0,       0,  22,     0,   0,  32,   AcceptedCargo.CT_FOOD         ,  0,  0,  0,  0,  0 ), /* 100 */
			new RailVehicleInfo( 72, W, 196,   0,       0,  18,     0,   0,  27,   AcceptedCargo.CT_PAPER        ,  0,  0,  0,  0,  0 ), /* 101 */
			new RailVehicleInfo( 73, W, 179,   0,       0,  19,     0,   0,  37,   AcceptedCargo.CT_COPPER_ORE   ,  0,  0,  0,  0,  0 ), /* 102 */
			new RailVehicleInfo( 47, W, 199,   0,       0,  25,     0,   0,  32,   AcceptedCargo.CT_WATER        ,  0,  0,  0,  0,  0 ), /* 103 */
			new RailVehicleInfo( 48, W, 182,   0,       0,  18,     0,   0,  32,   AcceptedCargo.CT_FRUIT        ,  0,  0,  0,  0,  0 ), /* 104 */
			new RailVehicleInfo( 49, W, 185,   0,       0,  19,     0,   0,  28,   AcceptedCargo.CT_RUBBER       ,  0,  0,  0,  0,  0 ), /* 105 */
			new RailVehicleInfo( 50, W, 176,   0,       0,  19,     0,   0,  37,   AcceptedCargo.CT_SUGAR        ,  0,  0,  0,  0,  0 ), /* 106 */
			new RailVehicleInfo( 51, W, 178,   0,       0,  20,     0,   0,  37,   AcceptedCargo.CT_COTTON_CANDY ,  0,  0,  0,  0,  0 ), /* 107 */
			new RailVehicleInfo( 52, W, 192,   0,       0,  20,     0,   0,  37,   AcceptedCargo.CT_TOFFEE       ,  0,  0,  0,  0,  0 ), /* 108 */
			new RailVehicleInfo( 53, W, 190,   0,       0,  21,     0,   0,  27,   AcceptedCargo.CT_BUBBLES      ,  0,  0,  0,  0,  0 ), /* 109 */
			new RailVehicleInfo( 54, W, 182,   0,       0,  24,     0,   0,  32,   AcceptedCargo.CT_COLA         ,  0,  0,  0,  0,  0 ), /* 110 */
			new RailVehicleInfo( 55, W, 181,   0,       0,  21,     0,   0,  32,   AcceptedCargo.CT_CANDY        ,  0,  0,  0,  0,  0 ), /* 111 */
			new RailVehicleInfo( 56, W, 183,   0,       0,  21,     0,   0,  27,   AcceptedCargo.CT_TOYS         ,  0,  0,  0,  0,  0 ), /* 112 */
			new RailVehicleInfo( 57, W, 196,   0,       0,  18,     0,   0,  29,   AcceptedCargo.CT_BATTERIES    ,  0,  0,  0,  0,  0 ), /* 113 */
			new RailVehicleInfo( 58, W, 193,   0,       0,  18,     0,   0,  32,   AcceptedCargo.CT_FIZZY_DRINKS ,  0,  0,  0,  0,  0 ), /* 114 */
			new RailVehicleInfo( 59, W, 191,   0,       0,  18,     0,   0,  37,   AcceptedCargo.CT_PLASTIC      ,  0,  0,  0,  0,  0 ), /* 115 */
	};
	//#undef W
	//#undef M

	//final ShipVehicleInfo orig_ship_vehicle_info[NUM_SHIP_ENGINES] = {
	public final static ShipVehicleInfo orig_ship_vehicle_info[] = {
			// image_index  cargo_type     cargo_amount                 refittable
			// |  base_cost |              |    running_cost            |
			// |  |    max_speed           |    |    sfx                |
			// |  |    |    |              |    |    |                  |
			new ShipVehicleInfo(  1, 160, 48,  AcceptedCargo.CT_OIL,        220, 140, 0 /* SND_06_SHIP_HORN */,  0 ), /*  0 */
			new ShipVehicleInfo(  1, 176, 80,  AcceptedCargo.CT_OIL,        350, 125, 0 /* SND_06_SHIP_HORN */,  0 ), /*  1 */
			new ShipVehicleInfo(  2, 96,  64,  AcceptedCargo.CT_PASSENGERS, 100, 90,  0 /* SND_07_FERRY_HORN */, 0 ), /*  2 */
			new ShipVehicleInfo(  2, 112, 128, AcceptedCargo.CT_PASSENGERS, 130, 80,  0 /* SND_07_FERRY_HORN */, 0 ), /*  3 */
			new ShipVehicleInfo(  3, 148, 224, AcceptedCargo.CT_PASSENGERS, 100, 190, 0 /* SND_07_FERRY_HORN */, 0 ), /*  4 */
			new ShipVehicleInfo(  2, 96,  64,  AcceptedCargo.CT_PASSENGERS, 100, 90,  0 /* SND_07_FERRY_HORN */, 0 ), /*  5 */
			new ShipVehicleInfo(  2, 112, 128, AcceptedCargo.CT_PASSENGERS, 130, 80,  0 /* SND_07_FERRY_HORN */, 0 ), /*  6 */
			new ShipVehicleInfo(  0, 128, 48,  AcceptedCargo.CT_GOODS,      160, 150, 0 /* SND_06_SHIP_HORN */,  1 ), /*  7 */
			new ShipVehicleInfo(  0, 144, 80,  AcceptedCargo.CT_GOODS,      190, 113, 0 /* SND_06_SHIP_HORN */,  1 ), /*  8 */
			new ShipVehicleInfo(  0, 128, 48,  AcceptedCargo.CT_GOODS,      160, 150, 0 /* SND_06_SHIP_HORN */,  1 ), /*  9 */
			new ShipVehicleInfo(  0, 144, 80,  AcceptedCargo.CT_GOODS,      190, 113, 0 /* SND_06_SHIP_HORN */,  1 ), /* 10 */
	};

	private static final int SND_09_JET = 0;
	private static final int SND_08_PLANE_TAKE_OFF = 0;
	private static final int SND_3B_JET_OVERHEAD = 0;
	private static final int SND_3D_ANOTHER_JET_OVERHEAD = 0;
	private static final int SND_45_PLANE_CRASHING = 0;
	private static final int SND_46_PLANE_ENGINE_SPUTTERING = 0;
	
	/* subtype: &1: regular aircraft (else chopper); &2: crashes easily on small airports */
	/* sfx from somewhere around SND_45_PLANE_CRASHING are toyland plane-sounds */
	public final static AircraftVehicleInfo orig_aircraft_vehicle_info[] = {
			// image_index         sfx                         acceleration
			// |   base_cost       |                           |   max_speed
			// |   |    running_cost                           |   |    mail_capacity
			// |   |    |  subtype |                           |   |    |    passenger_capacity
			// |   |    |  |       |                           |   |    |    |
			new AircraftVehicleInfo(  1, 14,  85, 1, SND_08_PLANE_TAKE_OFF,          18,  37,  4,  25 ), /*  0 */
			new AircraftVehicleInfo(  0, 15, 100, 1, SND_08_PLANE_TAKE_OFF,          20,  37,  8,  65 ), /*  1 */
			new AircraftVehicleInfo(  2, 16, 130, 3, SND_09_JET,                     35,  74, 10,  90 ), /*  2 */
			new AircraftVehicleInfo(  8, 75, 250, 3, SND_3B_JET_OVERHEAD,            50, 181, 20, 100 ), /*  3 */
			new AircraftVehicleInfo(  5, 15,  98, 1, SND_08_PLANE_TAKE_OFF,          20,  37,  6,  30 ), /*  4 */
			new AircraftVehicleInfo(  6, 18, 240, 3, SND_09_JET,                     40,  74, 30, 200 ), /*  5 */
			new AircraftVehicleInfo(  2, 17, 150, 1, SND_09_JET,                     35,  74, 15, 100 ), /*  6 */
			new AircraftVehicleInfo(  2, 18, 245, 3, SND_09_JET,                     40,  74, 30, 150 ), /*  7 */
			new AircraftVehicleInfo(  3, 19, 192, 3, SND_09_JET,                     40,  74, 40, 220 ), /*  8 */
			new AircraftVehicleInfo(  3, 20, 190, 3, SND_09_JET,                     40,  74, 25, 230 ), /*  9 */
			new AircraftVehicleInfo(  2, 16, 135, 3, SND_09_JET,                     35,  74, 10,  95 ), /* 10 */
			new AircraftVehicleInfo(  2, 18, 240, 3, SND_09_JET,                     40,  74, 35, 170 ), /* 11 */
			new AircraftVehicleInfo(  4, 17, 155, 3, SND_09_JET,                     40,  74, 15, 110 ), /* 12 */
			new AircraftVehicleInfo(  7, 30, 253, 3, SND_3D_ANOTHER_JET_OVERHEAD,    40,  74, 50, 300 ), /* 13 */
			new AircraftVehicleInfo(  4, 18, 210, 3, SND_09_JET,                     40,  74, 25, 200 ), /* 14 */
			new AircraftVehicleInfo(  4, 19, 220, 3, SND_09_JET,                     40,  74, 25, 240 ), /* 15 */
			new AircraftVehicleInfo(  4, 27, 230, 3, SND_09_JET,                     40,  74, 40, 260 ), /* 16 */
			new AircraftVehicleInfo(  3, 25, 225, 3, SND_09_JET,                     40,  74, 35, 240 ), /* 17 */
			new AircraftVehicleInfo(  4, 20, 235, 3, SND_09_JET,                     40,  74, 30, 260 ), /* 18 */
			new AircraftVehicleInfo(  4, 19, 220, 3, SND_09_JET,                     40,  74, 25, 210 ), /* 19 */
			new AircraftVehicleInfo(  4, 18, 170, 3, SND_09_JET,                     40,  74, 20, 160 ), /* 20 */
			new AircraftVehicleInfo(  4, 26, 210, 3, SND_09_JET,                     40,  74, 20, 220 ), /* 21 */
			new AircraftVehicleInfo(  6, 16, 125, 1, SND_09_JET,                     50,  74, 10,  80 ), /* 22 */
			new AircraftVehicleInfo(  2, 17, 145, 1, SND_09_JET,                     40,  74, 10,  85 ), /* 23 */
			new AircraftVehicleInfo( 11, 16, 130, 3, SND_09_JET,                     40,  74, 10,  75 ), /* 24 */
			new AircraftVehicleInfo( 10, 16, 149, 3, SND_09_JET,                     40,  74, 10,  85 ), /* 25 */
			new AircraftVehicleInfo( 15, 17, 170, 3, SND_09_JET,                     40,  74, 18,  65 ), /* 26 */
			new AircraftVehicleInfo( 12, 18, 210, 3, SND_09_JET,                     40,  74, 25, 110 ), /* 27 */
			new AircraftVehicleInfo( 13, 20, 230, 3, SND_09_JET,                     40,  74, 60, 180 ), /* 28 */
			new AircraftVehicleInfo( 14, 21, 220, 3, SND_09_JET,                     40,  74, 65, 150 ), /* 29 */
			new AircraftVehicleInfo( 16, 19, 160, 3, SND_09_JET,                     40, 181, 45,  85 ), /* 30 */
			new AircraftVehicleInfo( 17, 24, 248, 3, SND_3D_ANOTHER_JET_OVERHEAD,    40,  74, 80, 400 ), /* 31 */
			new AircraftVehicleInfo( 18, 80, 251, 3, SND_3B_JET_OVERHEAD,            50, 181, 45, 130 ), /* 32 */
			new AircraftVehicleInfo( 20, 13,  85, 1, SND_45_PLANE_CRASHING,          18,  37,  5,  25 ), /* 33 */
			new AircraftVehicleInfo( 21, 18, 100, 1, SND_46_PLANE_ENGINE_SPUTTERING, 20,  37,  9,  60 ), /* 34 */
			new AircraftVehicleInfo( 22, 25, 140, 1, SND_09_JET,                     40,  74, 12,  90 ), /* 35 */
			new AircraftVehicleInfo( 23, 32, 220, 3, SND_3D_ANOTHER_JET_OVERHEAD,    40,  74, 40, 200 ), /* 36 */
			new AircraftVehicleInfo( 24, 80, 255, 3, SND_3B_JET_OVERHEAD,            50, 181, 30, 100 ), /* 37 */
			new AircraftVehicleInfo(  9, 15,  81, 0, SND_09_JET,                     20,  25, 15,  40 ), /* 38 */
			new AircraftVehicleInfo( 19, 17,  77, 0, SND_09_JET,                     20,  40, 20,  55 ), /* 39 */
			new AircraftVehicleInfo( 25, 15,  80, 0, SND_09_JET,                     20,  25, 10,  40 ), /* 40 */
	};

	
	private static final int SND_19_BUS_START_PULL_AWAY = 0;
	private static final int SND_1C_TRUCK_START_2 = 0;
	private static final int SND_1B_TRUCK_START = 0;
	private static final int SND_3C_COMEDY_CAR = 0;
	private static final int SND_3E_COMEDY_CAR_2 = 0;
	private static final int SND_3F_COMEDY_CAR_3 = 0;
	private static final int SND_40_COMEDY_CAR_START_AND_PULL_AWAY = 0;
	
	/* I hope I got the cargo types right, figuring out which is which for which
	 * climate is a bitch */
	public static final RoadVehicleInfo orig_road_vehicle_info[] = {
			// image_index       sfx                                 max_speed
			// |    base_cost    |                                   |   capacity
			// |    |    running_cost                                |   |  cargo_type
			// |    |    |       |                                   |   |  |
			new RoadVehicleInfo(  0, 120,  91, SND_19_BUS_START_PULL_AWAY,            112, 31, AcceptedCargo.CT_PASSENGERS   ), /*  0 */
			new RoadVehicleInfo( 17, 140, 128, SND_1C_TRUCK_START_2,                  176, 35, AcceptedCargo.CT_PASSENGERS   ), /*  1 */
			new RoadVehicleInfo( 17, 150, 178, SND_1B_TRUCK_START,                    224, 37, AcceptedCargo.CT_PASSENGERS   ), /*  2 */
			new RoadVehicleInfo( 34, 160, 240, SND_1B_TRUCK_START,                    255, 40, AcceptedCargo.CT_PASSENGERS   ), /*  3 */
			new RoadVehicleInfo( 51, 120,  91, SND_3C_COMEDY_CAR,                     112, 30, AcceptedCargo.CT_PASSENGERS   ), /*  4 */
			new RoadVehicleInfo( 51, 140, 171, SND_3E_COMEDY_CAR_2,                   192, 35, AcceptedCargo.CT_PASSENGERS   ), /*  5 */
			new RoadVehicleInfo( 51, 160, 240, SND_3C_COMEDY_CAR,                     240, 38, AcceptedCargo.CT_PASSENGERS   ), /*  6 */
			new RoadVehicleInfo(  1, 108,  90, SND_19_BUS_START_PULL_AWAY,             96, 20, AcceptedCargo.CT_COAL         ), /*  7 */
			new RoadVehicleInfo( 18, 128, 168, SND_19_BUS_START_PULL_AWAY,            176, 25, AcceptedCargo.CT_COAL         ), /*  8 */
			new RoadVehicleInfo( 35, 138, 240, SND_19_BUS_START_PULL_AWAY,            224, 28, AcceptedCargo.CT_COAL         ), /*  9 */
			new RoadVehicleInfo(  2, 115,  90, SND_19_BUS_START_PULL_AWAY,             96, 22, AcceptedCargo.CT_MAIL         ), /* 10 */
			new RoadVehicleInfo( 19, 135, 168, SND_19_BUS_START_PULL_AWAY,            176, 28, AcceptedCargo.CT_MAIL         ), /* 11 */
			new RoadVehicleInfo( 36, 145, 240, SND_19_BUS_START_PULL_AWAY,            224, 30, AcceptedCargo.CT_MAIL         ), /* 12 */
			new RoadVehicleInfo( 57, 115,  90, SND_3E_COMEDY_CAR_2,                    96, 22, AcceptedCargo.CT_MAIL         ), /* 13 */
			new RoadVehicleInfo( 57, 135, 168, SND_3C_COMEDY_CAR,                     176, 28, AcceptedCargo.CT_MAIL         ), /* 14 */
			new RoadVehicleInfo( 57, 145, 240, SND_3E_COMEDY_CAR_2,                   224, 30, AcceptedCargo.CT_MAIL         ), /* 15 */
			new RoadVehicleInfo(  3, 110,  90, SND_19_BUS_START_PULL_AWAY,             96, 21, AcceptedCargo.CT_OIL          ), /* 16 */
			new RoadVehicleInfo( 20, 140, 168, SND_19_BUS_START_PULL_AWAY,            176, 25, AcceptedCargo.CT_OIL          ), /* 17 */
			new RoadVehicleInfo( 37, 150, 240, SND_19_BUS_START_PULL_AWAY,            224, 27, AcceptedCargo.CT_OIL          ), /* 18 */
			new RoadVehicleInfo(  4, 105,  90, SND_19_BUS_START_PULL_AWAY,             96, 14, AcceptedCargo.CT_LIVESTOCK    ), /* 19 */
			new RoadVehicleInfo( 21, 130, 168, SND_19_BUS_START_PULL_AWAY,            176, 16, AcceptedCargo.CT_LIVESTOCK    ), /* 20 */
			new RoadVehicleInfo( 38, 140, 240, SND_19_BUS_START_PULL_AWAY,            224, 18, AcceptedCargo.CT_LIVESTOCK    ), /* 21 */
			new RoadVehicleInfo(  5, 107,  90, SND_19_BUS_START_PULL_AWAY,             96, 14, AcceptedCargo.CT_GOODS        ), /* 22 */
			new RoadVehicleInfo( 22, 130, 168, SND_19_BUS_START_PULL_AWAY,            176, 16, AcceptedCargo.CT_GOODS        ), /* 23 */
			new RoadVehicleInfo( 39, 140, 240, SND_19_BUS_START_PULL_AWAY,            224, 18, AcceptedCargo.CT_GOODS        ), /* 24 */
			new RoadVehicleInfo(  6, 114,  90, SND_19_BUS_START_PULL_AWAY,             96, 20, AcceptedCargo.CT_GRAIN        ), /* 25 */
			new RoadVehicleInfo( 23, 133, 168, SND_19_BUS_START_PULL_AWAY,            176, 25, AcceptedCargo.CT_GRAIN        ), /* 26 */
			new RoadVehicleInfo( 40, 143, 240, SND_19_BUS_START_PULL_AWAY,            224, 30, AcceptedCargo.CT_GRAIN        ), /* 27 */
			new RoadVehicleInfo(  7, 118,  90, SND_19_BUS_START_PULL_AWAY,             96, 20, AcceptedCargo.CT_WOOD         ), /* 28 */
			new RoadVehicleInfo( 24, 137, 168, SND_19_BUS_START_PULL_AWAY,            176, 22, AcceptedCargo.CT_WOOD         ), /* 29 */
			new RoadVehicleInfo( 41, 147, 240, SND_19_BUS_START_PULL_AWAY,            224, 24, AcceptedCargo.CT_WOOD         ), /* 30 */
			new RoadVehicleInfo(  8, 121,  90, SND_19_BUS_START_PULL_AWAY,             96, 22, AcceptedCargo.CT_IRON_ORE     ), /* 31 */
			new RoadVehicleInfo( 25, 140, 168, SND_19_BUS_START_PULL_AWAY,            176, 25, AcceptedCargo.CT_IRON_ORE     ), /* 32 */
			new RoadVehicleInfo( 42, 150, 240, SND_19_BUS_START_PULL_AWAY,            224, 27, AcceptedCargo.CT_IRON_ORE     ), /* 33 */
			new RoadVehicleInfo(  9, 112,  90, SND_19_BUS_START_PULL_AWAY,             96, 15, AcceptedCargo.CT_STEEL        ), /* 34 */
			new RoadVehicleInfo( 26, 135, 168, SND_19_BUS_START_PULL_AWAY,            176, 18, AcceptedCargo.CT_STEEL        ), /* 35 */
			new RoadVehicleInfo( 43, 145, 240, SND_19_BUS_START_PULL_AWAY,            224, 20, AcceptedCargo.CT_STEEL        ), /* 36 */
			new RoadVehicleInfo( 10, 145,  90, SND_19_BUS_START_PULL_AWAY,             96, 12, AcceptedCargo.CT_VALUABLES    ), /* 37 */
			new RoadVehicleInfo( 27, 170, 168, SND_19_BUS_START_PULL_AWAY,            176, 15, AcceptedCargo.CT_VALUABLES    ), /* 38 */
			new RoadVehicleInfo( 44, 180, 240, SND_19_BUS_START_PULL_AWAY,            224, 16, AcceptedCargo.CT_VALUABLES    ), /* 39 */
			new RoadVehicleInfo( 11, 112,  90, SND_19_BUS_START_PULL_AWAY,             96, 17, AcceptedCargo.CT_FOOD         ), /* 40 */
			new RoadVehicleInfo( 28, 134, 168, SND_19_BUS_START_PULL_AWAY,            176, 20, AcceptedCargo.CT_FOOD         ), /* 41 */
			new RoadVehicleInfo( 45, 144, 240, SND_19_BUS_START_PULL_AWAY,            224, 22, AcceptedCargo.CT_FOOD         ), /* 42 */
			new RoadVehicleInfo( 12, 112,  90, SND_19_BUS_START_PULL_AWAY,             96, 15, AcceptedCargo.CT_PAPER        ), /* 43 */
			new RoadVehicleInfo( 29, 135, 168, SND_19_BUS_START_PULL_AWAY,            176, 18, AcceptedCargo.CT_PAPER        ), /* 44 */
			new RoadVehicleInfo( 46, 145, 240, SND_19_BUS_START_PULL_AWAY,            224, 20, AcceptedCargo.CT_PAPER        ), /* 45 */
			new RoadVehicleInfo( 13, 121,  90, SND_19_BUS_START_PULL_AWAY,             96, 22, AcceptedCargo.CT_COPPER_ORE   ), /* 46 */
			new RoadVehicleInfo( 30, 140, 168, SND_19_BUS_START_PULL_AWAY,            176, 25, AcceptedCargo.CT_COPPER_ORE   ), /* 47 */
			new RoadVehicleInfo( 47, 150, 240, SND_19_BUS_START_PULL_AWAY,            224, 27, AcceptedCargo.CT_COPPER_ORE   ), /* 48 */
			new RoadVehicleInfo( 14, 111,  90, SND_19_BUS_START_PULL_AWAY,             96, 21, AcceptedCargo.CT_WATER        ), /* 49 */
			new RoadVehicleInfo( 31, 141, 168, SND_19_BUS_START_PULL_AWAY,            176, 25, AcceptedCargo.CT_WATER        ), /* 50 */
			new RoadVehicleInfo( 48, 151, 240, SND_19_BUS_START_PULL_AWAY,            224, 27, AcceptedCargo.CT_WATER        ), /* 51 */
			new RoadVehicleInfo( 15, 118,  90, SND_19_BUS_START_PULL_AWAY,             96, 18, AcceptedCargo.CT_FRUIT        ), /* 52 */
			new RoadVehicleInfo( 32, 148, 168, SND_19_BUS_START_PULL_AWAY,            176, 20, AcceptedCargo.CT_FRUIT        ), /* 53 */
			new RoadVehicleInfo( 49, 158, 240, SND_19_BUS_START_PULL_AWAY,            224, 23, AcceptedCargo.CT_FRUIT        ), /* 54 */
			new RoadVehicleInfo( 16, 117,  90, SND_19_BUS_START_PULL_AWAY,             96, 17, AcceptedCargo.CT_RUBBER       ), /* 55 */
			new RoadVehicleInfo( 33, 147, 168, SND_19_BUS_START_PULL_AWAY,            176, 19, AcceptedCargo.CT_RUBBER       ), /* 56 */
			new RoadVehicleInfo( 50, 157, 240, SND_19_BUS_START_PULL_AWAY,            224, 22, AcceptedCargo.CT_RUBBER       ), /* 57 */
			new RoadVehicleInfo( 52, 117,  90, SND_3F_COMEDY_CAR_3,                    96, 17, AcceptedCargo.CT_SUGAR        ), /* 58 */
			new RoadVehicleInfo( 52, 147, 168, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 176, 19, AcceptedCargo.CT_SUGAR        ), /* 59 */
			new RoadVehicleInfo( 52, 157, 240, SND_3F_COMEDY_CAR_3,                   224, 22, AcceptedCargo.CT_SUGAR        ), /* 60 */
			new RoadVehicleInfo( 53, 117,  90, SND_40_COMEDY_CAR_START_AND_PULL_AWAY,  96, 17, AcceptedCargo.CT_COLA         ), /* 61 */
			new RoadVehicleInfo( 53, 147, 168, SND_3F_COMEDY_CAR_3,                   176, 19, AcceptedCargo.CT_COLA         ), /* 62 */
			new RoadVehicleInfo( 53, 157, 240, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 224, 22, AcceptedCargo.CT_COLA         ), /* 63 */
			new RoadVehicleInfo( 54, 117,  90, SND_3F_COMEDY_CAR_3,                    96, 17, AcceptedCargo.CT_COTTON_CANDY ), /* 64 */
			new RoadVehicleInfo( 54, 147, 168, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 176, 19, AcceptedCargo.CT_COTTON_CANDY ), /* 65 */
			new RoadVehicleInfo( 54, 157, 240, SND_3F_COMEDY_CAR_3,                   224, 22, AcceptedCargo.CT_COTTON_CANDY ), /* 66 */
			new RoadVehicleInfo( 55, 117,  90, SND_40_COMEDY_CAR_START_AND_PULL_AWAY,  96, 17, AcceptedCargo.CT_TOFFEE       ), /* 67 */
			new RoadVehicleInfo( 55, 147, 168, SND_3F_COMEDY_CAR_3,                   176, 19, AcceptedCargo.CT_TOFFEE       ), /* 68 */
			new RoadVehicleInfo( 55, 157, 240, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 224, 22, AcceptedCargo.CT_TOFFEE       ), /* 69 */
			new RoadVehicleInfo( 56, 117,  90, SND_3F_COMEDY_CAR_3,                    96, 17, AcceptedCargo.CT_TOYS         ), /* 70 */
			new RoadVehicleInfo( 56, 147, 168, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 176, 19, AcceptedCargo.CT_TOYS         ), /* 71 */
			new RoadVehicleInfo( 56, 157, 240, SND_3F_COMEDY_CAR_3,                   224, 22, AcceptedCargo.CT_TOYS         ), /* 72 */
			new RoadVehicleInfo( 58, 117,  90, SND_40_COMEDY_CAR_START_AND_PULL_AWAY,  96, 17, AcceptedCargo.CT_CANDY        ), /* 73 */
			new RoadVehicleInfo( 58, 147, 168, SND_3F_COMEDY_CAR_3,                   176, 19, AcceptedCargo.CT_CANDY        ), /* 74 */
			new RoadVehicleInfo( 58, 157, 240, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 224, 22, AcceptedCargo.CT_CANDY        ), /* 75 */
			new RoadVehicleInfo( 59, 117,  90, SND_3F_COMEDY_CAR_3,                    96, 17, AcceptedCargo.CT_BATTERIES    ), /* 76 */
			new RoadVehicleInfo( 59, 147, 168, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 176, 19, AcceptedCargo.CT_BATTERIES    ), /* 77 */
			new RoadVehicleInfo( 59, 157, 240, SND_3F_COMEDY_CAR_3,                   224, 22, AcceptedCargo.CT_BATTERIES    ), /* 78 */
			new RoadVehicleInfo( 60, 117,  90, SND_40_COMEDY_CAR_START_AND_PULL_AWAY,  96, 17, AcceptedCargo.CT_FIZZY_DRINKS ), /* 79 */
			new RoadVehicleInfo( 60, 147, 168, SND_3F_COMEDY_CAR_3,                   176, 19, AcceptedCargo.CT_FIZZY_DRINKS ), /* 80 */
			new RoadVehicleInfo( 60, 157, 240, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 224, 22, AcceptedCargo.CT_FIZZY_DRINKS ), /* 81 */
			new RoadVehicleInfo( 61, 117,  90, SND_3F_COMEDY_CAR_3,                    96, 17, AcceptedCargo.CT_PLASTIC      ), /* 82 */
			new RoadVehicleInfo( 61, 147, 168, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 176, 19, AcceptedCargo.CT_PLASTIC      ), /* 83 */
			new RoadVehicleInfo( 61, 157, 240, SND_3F_COMEDY_CAR_3,                   224, 22, AcceptedCargo.CT_PLASTIC      ), /* 84 */
			new RoadVehicleInfo( 62, 117,  90, SND_40_COMEDY_CAR_START_AND_PULL_AWAY,  96, 17, AcceptedCargo.CT_BUBBLES      ), /* 85 */
			new RoadVehicleInfo( 62, 147, 168, SND_3F_COMEDY_CAR_3,                   176, 19, AcceptedCargo.CT_BUBBLES      ), /* 86 */
			new RoadVehicleInfo( 62, 157, 240, SND_40_COMEDY_CAR_START_AND_PULL_AWAY, 224, 22, AcceptedCargo.CT_BUBBLES      ), /* 87 */
	};



}

class CarriageInfo extends EngineInfo
{
	/** Writes the properties of a train carriage into the EngineInfo struct.
	 * @see EngineInfo
	 * @param a Introduction date
	 * @param e Rail Type of the vehicle
	 * @param f Bitmask of the climates
	 *
	 * @apiNote the 0x80 in parameter b sets the "is carriage bit"
	 */
	//#define MW(a, b, c, d, e, f) { a, b | 0x80, c, d, e, f, 0 }
	public CarriageInfo(int a, int b, int c, int d, int e, int f) 
	{
		base_intro = a;
		unk2 =  (b | 0x80);              ///< Carriages have the highest bit set in this one
		lifelength =  c;
		base_life =  d;
		railtype =  e;
		climates =  f;
		refit_mask = 0;

	}
}




class EngineInit extends EngineInfo
{
	/** Writes the properties of a vehicle into the EngineInfo struct.
	 * @see EngineInfo
	 * @param a Introduction date
	 * @param e Rail Type of the vehicle
	 * @param f Bitmask of the climates
	 */
	//#define MK(a, b, c, d, e, f) { a, b, c, d, e, f, 0 }
	public EngineInit(int a, int b, int c, int d, int e, int f) 
	{
		base_intro = a;
		unk2 =  b;              ///< Carriages have the highest bit set in this one
		lifelength =  c;
		base_life =  d;
		railtype =  e;
		climates =  f;
		refit_mask = 0;
	}
}


