package game;

public class Order {
	int  type;
	int  flags;
	int station;

	Order next;   //! Pointer to next order. If NULL, end of list

	int index;         //! Index of the order, is not saved or anything, just for reference


	public static final int OT_NOTHING       = 0;
	public static final int OT_GOTO_STATION  = 1;
	public static final int OT_GOTO_DEPOT    = 2;
	public static final int OT_LOADING       = 3;
	public static final int OT_LEAVESTATION  = 4;
	public static final int OT_DUMMY         = 5;
	public static final int OT_GOTO_WAYPOINT = 6;

	/** Order flag masks - these are for direct bit operations */

	//Flags for stations:
	/** vehicle will transfer cargo (i. e. not deliver to nearby industry/town even if accepted there) */
	public static final int OF_TRANSFER           = 0x1;
	/** If OF_TRANSFER is not set, drop any cargo loaded. If accepted, deliver, otherwise cargo remains at the station.
	 * No new cargo is loaded onto the vehicle whatsoever */
	public static final int OF_UNLOAD             = 0x2;
	/** Wait for full load of all vehicles, or of at least one cargo type, depending on patch setting
	 * @todo make this two different flags */
	public static final int OF_FULL_LOAD          = 0x4;

	//Flags for depots:
	/** The current depot-order was initiated because it was in the vehicle's order list */
	public static final int OF_PART_OF_ORDERS	  = 0x2;
	/** if OF_PART_OF_ORDERS is not set, this will cause the vehicle to be stopped in the depot */
	public static final int OF_HALT_IN_DEPOT      = 0x4;
	/** if OF_PART_OF_ORDERS is set, this will cause the order only be come active if the vehicle needs servicing */
	public static final int OF_SERVICE_IF_NEEDED  = 0x4; //used when OF_PART_OF_ORDERS is set.

	//Common flags
	/** This causes the vehicle not to stop at intermediate OR the destination station (depending on patch settings)
	 * @todo make this two different flags */
	public static final int OF_NON_STOP           = 0x8;



	/** Order flags bits - these are for the *BIT macros
	 * for descrption of flags, see OrderFlagMasks
	 * @see OrderFlagMasks
	 */
	public static final int OFB_TRANSFER          = 0;
	public static final int OFB_UNLOAD            = 1;
	public static final int OFB_FULL_LOAD         = 2;
	public static final int OFB_PART_OF_ORDERS    = 1;
	public static final int OFB_HALT_IN_DEPOT     = 2;
	public static final int OFB_SERVICE_IF_NEEDED = 2;
	public static final int OFB_NON_STOP          = 3;



	/* Possible clone options */
	public static final int CO_SHARE   = 0;
	public static final int CO_COPY    = 1;
	public static final int CO_UNSHARE = 2;

	/* Modes for the order checker */
	public static final int OC_INIT     = 0; //the order checker can initialize a news message
	public static final int OC_VALIDATE = 1; //the order checker validates a news message


}
