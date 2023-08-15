package com.dzavalishin.sort;

import java.util.Comparator;

import com.dzavalishin.game.Vehicle;
import com.dzavalishin.struct.SortStruct;


/* Variables you need to set before calling this function!
 * 1. (byte)_internal_sort_type:					sorting criteria to sort on
 * 2. (boolean)_internal_sort_order:				sorting order, descending/ascending
 * 3. (int)_internal_name_sorter_id:	default StringID of the vehicle when no name is set. e.g.
 *    Str.STR_SV_TRAIN_NAME for trains or Str.STR_SV_AIRCRAFT_NAME for aircraft
 */


public abstract class AbstractVehicleSorter implements Comparator<SortStruct>
{
	protected AbstractVehicleSorter() { } 
	
	// if the sorting criteria had the same value, sort vehicle by unitnumber
	protected static int VEHICLEUNITNUMBERSORTER(int r, Vehicle a, Vehicle b)
	{
		if (r == 0) 
			return a.getUnitnumber().id - b.getUnitnumber().id;
		return r;
	}

}
