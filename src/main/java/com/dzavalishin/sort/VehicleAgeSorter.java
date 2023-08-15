package com.dzavalishin.sort;

import com.dzavalishin.game.Vehicle;
import com.dzavalishin.struct.SortStruct;
import com.dzavalishin.xui.VehicleGui;

public class VehicleAgeSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		final Vehicle va = Vehicle.GetVehicle(a.index);
		final Vehicle vb = Vehicle.GetVehicle(b.index);
		int r = va.getAge() - vb.getAge();

		r = VEHICLEUNITNUMBERSORTER(r, va, vb);

		return VehicleGui._internal_sort_order ? -r : r;
	}
}