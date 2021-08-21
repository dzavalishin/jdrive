package game.sort;

import game.Global;
import game.Str;
import game.Vehicle;
import game.struct.SortStruct;
import game.xui.VehicleGui;

public class VehicleNameSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		final SortStruct cmp1 = a;
		final SortStruct cmp2 = b;
		final Vehicle va = Vehicle.GetVehicle(cmp1.index);
		final Vehicle vb = Vehicle.GetVehicle(cmp2.index);
		int r;

		Global.SetDParam(0, va.getString_id());
		String buf1 = Global.GetString(Str.STR_JUST_STRING);

		Global.SetDParam(0, vb.getString_id());
		String buf2 = Global.GetString(Str.STR_JUST_STRING);


		r = buf1.compareToIgnoreCase(buf2);

		r = VEHICLEUNITNUMBERSORTER(r, va, vb);

		return VehicleGui._internal_sort_order ? -r : r;
	}
}