package com.dzavalishin.sort;

import com.dzavalishin.game.Vehicle;
import com.dzavalishin.struct.SortStruct;
import com.dzavalishin.xui.VehicleGui;

public class VehicleMaxSpeedSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		final Vehicle va = Vehicle.GetVehicle(a.index);
		final Vehicle vb = Vehicle.GetVehicle(b.index);

		int max_speed_a = va.getRealMaxSpeed();
		int max_speed_b = vb.getRealMaxSpeed();
		int r = max_speed_a - max_speed_b;

		r = VEHICLEUNITNUMBERSORTER(r, va, vb);

		return VehicleGui._internal_sort_order ? -r : r;
	}
}