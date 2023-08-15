package com.dzavalishin.sort;

import com.dzavalishin.struct.SortStruct;

public class VehicleUnsortedSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		return a.index - b.index;
	}
}