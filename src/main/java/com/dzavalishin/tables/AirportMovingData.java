package com.dzavalishin.tables;

public class AirportMovingData {
	public final int x;
    public final int y;
	public final int flag;
	public final int direction;

	AirportMovingData(int x, int y, int flag, int dir)
	{
		this.x = x;
		this.y = y;
		this.flag = flag;
		this.direction = dir;
	}

	// flags
	public static final int AMED_NOSPDCLAMP	= 1<<0;
	public static final int AMED_TAKEOFF	= 1<<1;
	public static final int AMED_SLOWTURN	= 1<<2;
	public static final int AMED_LAND		= 1<<3;
	public static final int AMED_EXACTPOS	= 1<<4;
	public static final int AMED_BRAKE		= 1<<5;
	public static final int AMED_HELI_RAISE	= 1<<6;
	public static final int AMED_HELI_LOWER	= 1<<7;

	//public static final int MAX_ELEMENTS = 255;
	public static final int MAX_HEADINGS = 18;


	///////////////////////////////////////////////////////////////////////
	/////*********Movement Positions on Airports********************///////
	//Country Airfield (small) 4x3
	public static final AirportMovingData _airport_moving_data_country[] = {
			new AirportMovingData(  53, 3,AMED_EXACTPOS,3),						// 00 In Hangar
			new AirportMovingData(  53, 27,0,0),								// 01 Taxi to right outside depot
			new AirportMovingData(  32, 23,AMED_EXACTPOS,7),					// 02 Terminal 1
			new AirportMovingData(  10, 23,AMED_EXACTPOS,7),					// 03 Terminal 2
			new AirportMovingData(  43, 37,0,0),								// 04 Going towards terminal 2
			new AirportMovingData(  24, 37,0,0),								// 05 Going towards terminal 2
			new AirportMovingData(  53, 37,0,0),								// 06 Going for takeoff
			new AirportMovingData(  61, 40,AMED_EXACTPOS,1),					// 07 Taxi to start of runway (takeoff)
			new AirportMovingData(   3, 40,AMED_NOSPDCLAMP,0),					// 08 Accelerate to end of runway
			new AirportMovingData( -79, 40,AMED_NOSPDCLAMP | AMED_TAKEOFF,0),	// 09 Take off
			new AirportMovingData( 177, 40,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 10 Fly to landing position in air
			new AirportMovingData(  56, 40,AMED_NOSPDCLAMP | AMED_LAND,0),		// 11 Going down for land
			new AirportMovingData(   3, 40,AMED_NOSPDCLAMP | AMED_BRAKE,0),		// 12 Just landed, brake until end of runway
			new AirportMovingData(   7, 40,0,0),								// 13 Just landed, turn around and taxi 1 square
			new AirportMovingData(  53, 40,0,0),								// 14 Taxi from runway to crossing
			new AirportMovingData( 145,-58,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 15 Fly around waiting for a landing spot (north-east)
			new AirportMovingData( 260,-58,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 16 Fly around waiting for a landing spot (north-west)
			new AirportMovingData( 291, 17,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 17 Fly around waiting for a landing spot (south-west)
			new AirportMovingData( 177, 40,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 18 Fly around waiting for a landing spot (south)
			new AirportMovingData(  44, 37,AMED_HELI_RAISE,0),					// 19 Helicopter takeoff
			new AirportMovingData(  44, 40,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 20 In position above landing spot helicopter
			new AirportMovingData(  44, 40,AMED_HELI_LOWER,0)					// 21 Helicopter landing
	};

	//City Airport (large) 6x6
	public static final AirportMovingData _airport_moving_data_town[] = {
			new AirportMovingData(  85,  3,AMED_EXACTPOS,3),										// 00 In Hangar
			new AirportMovingData(  85, 27,0,0),																// 01 Taxi to right outside depot
			new AirportMovingData(  26, 41,AMED_EXACTPOS,5),										// 02 Terminal 1
			new AirportMovingData(  56, 20,AMED_EXACTPOS,3),										// 03 Terminal 2
			new AirportMovingData(  38,  8,AMED_EXACTPOS,5),										// 04 Terminal 3
			new AirportMovingData(  65,  6,0,0),																// 05 Taxi to right in infront of terminal 2/3
			new AirportMovingData(  80, 27,0,0),																// 06 Taxiway terminals 2-3
			new AirportMovingData(  44, 63,0,0),																// 07 Taxi to Airport center
			new AirportMovingData(  58, 71,0,0),																// 08 Towards takeoff
			new AirportMovingData(  72, 85,0,0),																// 09 Taxi to runway (takeoff)
			new AirportMovingData(  89, 85,AMED_EXACTPOS,1),										// 10 Taxi to start of runway (takeoff)
			new AirportMovingData(   3, 85,AMED_NOSPDCLAMP,0),									// 11 Accelerate to end of runway
			new AirportMovingData( -79, 85,AMED_NOSPDCLAMP | AMED_TAKEOFF,0),		// 12 Take off
			new AirportMovingData( 177, 85,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 13 Fly to landing position in air
			new AirportMovingData(  89, 85,AMED_NOSPDCLAMP | AMED_LAND,0),			// 14 Going down for land
			new AirportMovingData(   3, 85,AMED_NOSPDCLAMP | AMED_BRAKE,0),			// 15 Just landed, brake until end of runway
			new AirportMovingData(  20, 87,0,0),																// 16 Just landed, turn around and taxi 1 square
			new AirportMovingData(  36, 71,0,0),																// 17 Taxi from runway to crossing
			new AirportMovingData( 145,-13,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 19 Fly around waiting for a landing spot (north-east)
			new AirportMovingData( 260,-13,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 20 Fly around waiting for a landing spot (north-west)
			new AirportMovingData( 291, 62,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 21 Fly around waiting for a landing spot (south-west)
			new AirportMovingData( 177, 85,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 22 Fly around waiting for a landing spot (south)
			new AirportMovingData(  44, 63,AMED_HELI_RAISE,0),									// 22 Helicopter takeoff
			new AirportMovingData(  28, 74,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 23 In position above landing spot helicopter
			new AirportMovingData(  28, 74,AMED_HELI_LOWER,0)										// 24 Helicopter landing
	};

	//Metropolitan Airport (metropolitan) - 2 runways
	public static final AirportMovingData _airport_moving_data_metropolitan[] = {
			new AirportMovingData(  85,  3,AMED_EXACTPOS,3),										// 00 In Hangar
			new AirportMovingData(  85, 27,0,0),																// 01 Taxi to right outside depot
			new AirportMovingData(  26, 41,AMED_EXACTPOS,5),										// 02 Terminal 1
			new AirportMovingData(  56, 20,AMED_EXACTPOS,3),										// 03 Terminal 2
			new AirportMovingData(  38,  8,AMED_EXACTPOS,5),										// 04 Terminal 3
			new AirportMovingData(  65,  6,0,0),																// 05 Taxi to right in infront of terminal 2/3
			new AirportMovingData(  70, 33,0,0),																// 06 Taxiway terminals 2-3
			new AirportMovingData(  44, 58,0,0),																// 07 Taxi to Airport center
			new AirportMovingData(  72, 58,0,0),																// 08 Towards takeoff
			new AirportMovingData(  72, 69,0,0),																// 09 Taxi to runway (takeoff)
			new AirportMovingData(  89, 69,AMED_EXACTPOS,1),										// 10 Taxi to start of runway (takeoff)
			new AirportMovingData(   3, 69,AMED_NOSPDCLAMP,0),									// 11 Accelerate to end of runway
			new AirportMovingData( -79, 69,AMED_NOSPDCLAMP | AMED_TAKEOFF,0),		// 12 Take off
			new AirportMovingData( 177, 85,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 13 Fly to landing position in air
			new AirportMovingData(  89, 85,AMED_NOSPDCLAMP | AMED_LAND,0),			// 14 Going down for land
			new AirportMovingData(   3, 85,AMED_NOSPDCLAMP | AMED_BRAKE,0),			// 15 Just landed, brake until end of runway
			new AirportMovingData(  21, 85,0,0),																// 16 Just landed, turn around and taxi 1 square
			new AirportMovingData(  21, 69,0,0),																// 17 On Runway-out taxiing to In-Way
			new AirportMovingData(  21, 54,AMED_EXACTPOS,5),										// 18 Taxi from runway to crossing
			new AirportMovingData( 145,-13,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 19 Fly around waiting for a landing spot (north-east)
			new AirportMovingData( 260,-13,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 20 Fly around waiting for a landing spot (north-west)
			new AirportMovingData( 291, 62,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 21 Fly around waiting for a landing spot (south-west)
			new AirportMovingData( 177, 85,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 22 Fly around waiting for a landing spot (south)
			new AirportMovingData(  44, 58,0,0),																// 23 Helicopter takeoff spot on ground (to clear airport sooner)
			new AirportMovingData(  44, 63,AMED_HELI_RAISE,0),									// 24 Helicopter takeoff
			new AirportMovingData(  15, 54,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 25 Get in position above landing spot helicopter
			new AirportMovingData(  15, 54,AMED_HELI_LOWER,0)										// 26 Helicopter landing
	};

	//International Airport (international) - 2 runways, 6 terminals, dedicated helipod
	public static final AirportMovingData _airport_moving_data_international[] = {
			new AirportMovingData(   7, 55,AMED_EXACTPOS,3),										// 00 In Hangar 1
			new AirportMovingData( 100, 21,AMED_EXACTPOS,3),										// 01 In Hangar 2
			new AirportMovingData(   7, 70,0,0),																// 02 Taxi to right outside depot
			new AirportMovingData( 100, 36,0,0),																// 03 Taxi to right outside depot
			new AirportMovingData(  38, 70,AMED_EXACTPOS,5),										// 04 Terminal 1
			new AirportMovingData(  38, 54,AMED_EXACTPOS,5),										// 05 Terminal 2
			new AirportMovingData(  38, 38,AMED_EXACTPOS,5),										// 06 Terminal 3
			new AirportMovingData(  70, 70,AMED_EXACTPOS,1),										// 07 Terminal 4
			new AirportMovingData(  70, 54,AMED_EXACTPOS,1),										// 08 Terminal 5
			new AirportMovingData(  70, 38,AMED_EXACTPOS,1),										// 09 Terminal 6
			new AirportMovingData( 104, 71,AMED_EXACTPOS,1),										// 10 Helipad 1
			new AirportMovingData( 104, 55,AMED_EXACTPOS,1),										// 11 Helipad 2
			new AirportMovingData(  22, 87,0,0),																// 12 Towards Terminals 4/5/6, Helipad 1/2
			new AirportMovingData(  60, 87,0,0),																// 13 Towards Terminals 4/5/6, Helipad 1/2
			new AirportMovingData(  66, 87,0,0),																// 14 Towards Terminals 4/5/6, Helipad 1/2
			new AirportMovingData(  86, 87,AMED_EXACTPOS,7),										// 15 Towards Terminals 4/5/6, Helipad 1/2
			new AirportMovingData(  86, 70,0,0),																// 16 In Front of Terminal 4 / Helipad 1
			new AirportMovingData(  86, 54,0,0),																// 17 In Front of Terminal 5 / Helipad 2
			new AirportMovingData(  86, 38,0,0),																// 18 In Front of Terminal 6
			new AirportMovingData(  86, 22,0,0),																// 19 Towards Terminals Takeoff (Taxiway)
			new AirportMovingData(  66, 22,0,0),																// 20 Towards Terminals Takeoff (Taxiway)
			new AirportMovingData(  60, 22,0,0),																// 21 Towards Terminals Takeoff (Taxiway)
			new AirportMovingData(  38, 22,0,0),																// 22 Towards Terminals Takeoff (Taxiway)
			new AirportMovingData(  22, 70,0,0),																// 23 In Front of Terminal 1
			new AirportMovingData(  22, 58,0,0),																// 24 In Front of Terminal 2
			new AirportMovingData(  22, 38,0,0),																// 25 In Front of Terminal 3
			new AirportMovingData(  22, 22,AMED_EXACTPOS,7),										// 26 Going for Takeoff
			new AirportMovingData(  22,  6,0,0),																// 27 On Runway-out, prepare for takeoff
			new AirportMovingData(   3,  6,AMED_EXACTPOS,5),										// 28 Accelerate to end of runway
			new AirportMovingData(  60,  6,AMED_NOSPDCLAMP,0),									// 29 Release control of runway, for smoother movement
			new AirportMovingData( 105,  6,AMED_NOSPDCLAMP,0),									// 30 End of runway
			new AirportMovingData( 190,  6,AMED_NOSPDCLAMP | AMED_TAKEOFF,0),		// 31 Take off
			new AirportMovingData( 193,104,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 32 Fly to landing position in air
			new AirportMovingData( 105,104,AMED_NOSPDCLAMP | AMED_LAND,0),			// 33 Going down for land
			new AirportMovingData(   3,104,AMED_NOSPDCLAMP | AMED_BRAKE,0),			// 34 Just landed, brake until end of runway
			new AirportMovingData(  12,104,0,0),																// 35 Just landed, turn around and taxi 1 square
			new AirportMovingData(   7, 84,0,0),																// 36 Taxi from runway to crossing
			new AirportMovingData( 193,  6,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 37 Fly around waiting for a landing spot (north-east)
			new AirportMovingData( 388,  6,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 38 Fly around waiting for a landing spot (north-west)
			new AirportMovingData( 419, 81,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 39 Fly around waiting for a landing spot (south-west)
			new AirportMovingData( 305,104,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 40 Fly around waiting for a landing spot (south)
			// Helicopter
			new AirportMovingData( 128, 80,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 41 Bufferspace before helipad
			new AirportMovingData( 128, 80,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 42 Bufferspace before helipad
			new AirportMovingData(  96, 71,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 43 Get in position for Helipad1
			new AirportMovingData(  96, 55,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 44 Get in position for Helipad2
			new AirportMovingData(  96, 71,AMED_HELI_LOWER,0),									// 45 Land at Helipad1
			new AirportMovingData(  96, 55,AMED_HELI_LOWER,0),									// 46 Land at Helipad2
			new AirportMovingData( 104, 71,AMED_HELI_RAISE,0),									// 47 Takeoff Helipad1
			new AirportMovingData( 104, 55,AMED_HELI_RAISE,0),									// 48 Takeoff Helipad2
			new AirportMovingData( 104, 32,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 49 Go to position for Hangarentrance in air
			new AirportMovingData( 104, 32,AMED_HELI_LOWER,0)										// 50 Land in HANGAR2_AREA to go to hangar
	};

	//Heliport (heliport)
	public static final AirportMovingData _airport_moving_data_heliport[] = {
			new AirportMovingData(   5,  9,AMED_EXACTPOS,1),										// 0 - At heliport terminal
			new AirportMovingData(   2,  9,AMED_HELI_RAISE,0),									// 1 - Take off (play sound)
			new AirportMovingData(  -3,  9,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 2 - In position above landing spot helicopter
			new AirportMovingData(  -3,  9,AMED_HELI_LOWER,0),									// 3 - Land
			new AirportMovingData(   2,  9,0,0),																// 4 - Goto terminal on ground
			new AirportMovingData( -31, 59,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 5 - Circle #1 (north-east)
			new AirportMovingData( -31,-49,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 6 - Circle #2 (north-west)
			new AirportMovingData(  49,-49,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 7 - Circle #3 (south-west)
			new AirportMovingData(  70,  9,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 8 - Circle #4 (south)
	};

	//Oilrig
	public static final AirportMovingData _airport_moving_data_oilrig[] = {
			new AirportMovingData(  31,  9,AMED_EXACTPOS,1),										// 0 - At oilrig terminal
			new AirportMovingData(  28,  9,AMED_HELI_RAISE,0),									// 1 - Take off (play sound)
			new AirportMovingData(  23,  9,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 2 - In position above landing spot helicopter
			new AirportMovingData(  23,  9,AMED_HELI_LOWER,0),									// 3 - Land
			new AirportMovingData(  28,  9,0,0),																// 4 - Goto terminal on ground
			new AirportMovingData( -31, 69,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 5 - circle #1 (north-east)
			new AirportMovingData( -31,-49,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 6 - circle #2 (north-west)
			new AirportMovingData(  69,-49,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 7 - circle #3 (south-west)
			new AirportMovingData(  70,  9,AMED_NOSPDCLAMP | AMED_SLOWTURN,0),	// 8 - circle #4 (south)
	};

}
