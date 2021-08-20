package game.sort;

import java.util.Comparator;

import game.Global;
import game.Str;
import game.Vehicle;
import game.struct.SortStruct;
import game.xui.VehicleGui;


/* Variables you need to set before calling this function!
 * 1. (byte)_internal_sort_type:					sorting criteria to sort on
 * 2. (boolean)_internal_sort_order:				sorting order, descending/ascending
 * 3. (int)_internal_name_sorter_id:	default StringID of the vehicle when no name is set. eg
 *    Str.STR_SV_TRAIN_NAME for trains or Str.STR_SV_AIRCRAFT_NAME for aircraft
 */


public abstract class AbstractVehicleSorter implements Comparator<SortStruct>
{

	// if the sorting criteria had the same value, sort vehicle by unitnumber
	protected static int VEHICLEUNITNUMBERSORTER(int r, Vehicle a, Vehicle b) 
	{
		if (r == 0) 
			return a.getUnitnumber().id - b.getUnitnumber().id;
		return r;
	}

}





class VehicleNameSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		final SortStruct cmp1 = a;
		final SortStruct cmp2 = b;
		final Vehicle va = Vehicle.GetVehicle(cmp1.index);
		final Vehicle vb = Vehicle.GetVehicle(cmp2.index);
		int r;

		Global.SetDParam(0, va.string_id);
		String buf1 = Global.GetString(Str.STR_JUST_STRING);

		Global.SetDParam(0, vb.string_id);
		String buf2 = Global.GetString(Str.STR_JUST_STRING);


		r = buf1.compareToIgnoreCase(buf2);

		r = VEHICLEUNITNUMBERSORTER(r, va, vb);

		return VehicleGui._internal_sort_order ? -r : r;
	}
}

class VehicleMaxSpeedSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		final Vehicle va = Vehicle.GetVehicle(a.index);
		final Vehicle vb = Vehicle.GetVehicle(b.index);
		int max_speed_a = 0xFFFF, max_speed_b = 0xFFFF;
		int r;
		Vehicle ua = va, ub = vb;

		if (va.getType() == Vehicle.VEH_Train && vb.getType() == Vehicle.VEH_Train) {
			do {
				if (Engine.RailVehInfo(ua.engine_type.id).max_speed != 0)
					max_speed_a = Math.min(max_speed_a, Engine.RailVehInfo(ua.engine_type.id).max_speed);
			} while ((ua = ua.next) != null);

			do {
				if (Engine.RailVehInfo(ub.engine_type.id).max_speed != 0)
					max_speed_b = Math.min(max_speed_b, Engine.RailVehInfo(ub.engine_type.id).max_speed);
			} while ((ub = ub.next) != null);

			r = max_speed_a - max_speed_b;
		} else {
			r = va.max_speed - vb.max_speed;
		}

		r = VEHICLEUNITNUMBERSORTER(r, va, vb);

		return VehicleGui._internal_sort_order ? -r : r;
	}
}
