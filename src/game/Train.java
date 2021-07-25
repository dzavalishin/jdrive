package game;

import game.tables.TrainTables;

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
			final RailVehicleInfo rvi = RailVehInfo(u.engine_type);
			int vweight = 0;

			vweight += (_cargoc.weights[u.cargo_type] * u.cargo_count) / 16;

			// Vehicle weight is not added for articulated parts.
			if (!IsArticulatedPart(u)) {
				// vehicle weight is the sum of the weight of the vehicle and the weight of its cargo
				vweight += rvi.weight;

				// powered wagons have extra weight added
				if (BitOps.HASBIT(u.rail.flags, VRF_POWEREDWAGON))
					vweight += RailVehInfo(v.engine_type).pow_wag_weight;
			}

			// consist weight is the sum of the weight of all vehicles in the consist
			weight += vweight;

			// store vehicle weight in cache
			u.rail.cached_veh_weight = vweight;
		};

		// store consist weight in cache
		v.rail.cached_weight = weight;
	}
	
	
}
