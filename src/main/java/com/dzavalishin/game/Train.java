package com.dzavalishin.game;

import com.dzavalishin.tables.TrainTables;

public class Train extends TrainTables 
{

	/**
	 * Recalculates the cached weight of a train and its vehicles. Should be called each time the cargo on
	 * the consist changes.
	 * @param v First vehicle of the consist.
	 */
	static void TrainCargoChanged(Vehicle  v)
	{
		Vehicle u;
		int weight = 0;

		for (u = v; u != null; u = u.next) {
			final RailVehicleInfo rvi = Engine.RailVehInfo(u.getEngine_type().id);
			int vweight = 0;

			vweight += (Global._cargoc.weights[u.getCargo_type()] * u.cargo_count) / 16;

			// Vehicle weight is not added for articulated parts.
			if (!u.IsArticulatedPart()) {
				// vehicle weight is the sum of the weight of the vehicle and the weight of its cargo
				vweight += rvi.weight;

				// powered wagons have extra weight added
				//if (BitOps.HASBIT(u.rail.flags, Vehicle.VRF_POWEREDWAGON))
				if(u.rail.flags.contains(VehicleRailFlags.PoweredWagon))
					vweight += Engine.RailVehInfo(v.getEngine_type().id).pow_wag_weight;
			}

			// consist weight is the sum of the weight of all vehicles in the consist
			weight += vweight;

			// store vehicle weight in cache
			u.rail.cached_veh_weight = vweight;
		}

		// store consist weight in cache
		v.rail.cached_weight = weight;
	}
	
	
}
