package com.dzavalishin.sort;

import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.struct.SortStruct;
import com.dzavalishin.xui.VehicleGui;

public class VehicleCargoSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		final Vehicle va = Vehicle.GetVehicle(a.index);
		final Vehicle  vb = Vehicle.GetVehicle(b.index);
		//Vehicle  v;
		//AcceptedCargo cargoa = new AcceptedCargo();
		//AcceptedCargo cargob = new AcceptedCargo();
		int r = 0;
		int i;

		AcceptedCargo cargoa = va.countTotalCargo();
		AcceptedCargo cargob = vb.countTotalCargo();

		//for (v = va; v != null; v = v.next) cargoa.ct[v.cargo_type] += v.cargo_cap;
		//for (v = vb; v != null; v = v.next) cargob.ct[v.cargo_type] += v.cargo_cap;

		for (i = 0; i < AcceptedCargo.NUM_CARGO; i++) {
			r = cargoa.ct[i] - cargob.ct[i];
			if (r != 0) break;
		}

		r = VEHICLEUNITNUMBERSORTER(r, va, vb);

		return VehicleGui._internal_sort_order ? -r : r;
	}
}