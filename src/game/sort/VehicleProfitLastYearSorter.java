package game.sort;

import game.Vehicle;
import game.struct.SortStruct;
import game.xui.VehicleGui;

public class VehicleProfitLastYearSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		final Vehicle va = Vehicle.GetVehicle(a.index);
		final Vehicle vb = Vehicle.GetVehicle(b.index);
		int r = va.getProfit_last_year() - vb.getProfit_last_year();

		r = VEHICLEUNITNUMBERSORTER(r, va, vb);

		return VehicleGui._internal_sort_order ? -r : r;
	}
}