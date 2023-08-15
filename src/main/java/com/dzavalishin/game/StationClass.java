package com.dzavalishin.game;

import java.util.Arrays;

import com.dzavalishin.enums.StationClassID;

/**
 *
 * Functions for dealing with station classes and custom stations.
 **/

public class StationClass 
{
	int id;          ///< ID of this class, e.g. 'DFLT', 'WAYP', etc.
	String name;         ///< Name of this class.
	int stations;      ///< Number of stations in this class.
	StationSpec [] spec; ///< Array of station specifications.

	
	
	





	private static final int STAT_CLASS_MAX = 16;


	static final StationClass [] station_classes = new StationClass[STAT_CLASS_MAX];

	/**
	 * Reset station classes to their default state.
	 * This includes initialising the Default and Waypoint classes with an empty
	 * entry, for standard stations and waypoints.
	 */
	void ResetStationClasses()
	{
		/*StationClassID*/ int  i;
		for (i = 0; i < STAT_CLASS_MAX; i++) {
			station_classes[i].id = 0;

			//free(station_classes[i].name);
			station_classes[i].name = null;

			station_classes[i].stations = 0;

			//free(station_classes[i].spec);
			station_classes[i].spec = null;
		}

		// Set up initial data
		station_classes[0].id = 0x44464C54; // 'DFLT';
		station_classes[0].name = "Default";
		station_classes[0].stations = 1;
		station_classes[0].spec = new StationSpec[1]; //malloc(sizeof(*station_classes[0].spec));
		station_classes[0].spec[0] = null;

		station_classes[1].id =  0x57415950; // 'WAYP';
		station_classes[1].name = "Waypoints";
		station_classes[1].stations = 1;
		station_classes[1].spec = new StationSpec[1]; // malloc(sizeof(*station_classes[1].spec));
		station_classes[1].spec[0] = null;
	}

	/**
	 * Allocate a station class for the given class id.
	 * @param classid A 32 bit value identifying the class.
	 * @return Index into station_classes of allocated class.
	 */
	/*StationClassID*/ static StationClassID  AllocateStationClass(int sclass)
	{
		/*StationClassID*/ int  i;

		for (i = 0; i < STAT_CLASS_MAX; i++) {
			if (station_classes[i].id == sclass) {
				// ClassID is already allocated, so reuse it.
				return StationClassID.values[i];
			} else if (station_classes[i].id == 0) {
				// This class is empty, so allocate it to the ClassID.
				station_classes[i].id = sclass;
				return StationClassID.values[i];
			}
		}

		Global.DEBUG_grf( 2, "StationClassAllocate: Already allocated %d classes, using default.", STAT_CLASS_MAX);
		return StationClassID.STAT_CLASS_DFLT;
	}

	/**
	 * Return the number of stations for the given station class.
	 * @param sclass Index of the station class.
	 * @return Number of stations in the class.
	 */
	static int GetNumCustomStations(StationClassID sclass)
	{
		assert(sclass.ordinal() < STAT_CLASS_MAX);
		return station_classes[sclass.ordinal()].stations;
	}

	/**
	 * Tie a station spec to its station class.
	 * @param spec The station spec.
	 */
	static void SetCustomStation(StationSpec spec)
	{
		StationClass station_class;
		int i;

		assert(spec.sclass.ordinal() < STAT_CLASS_MAX);
		station_class = station_classes[spec.sclass.ordinal()];

		i = station_class.stations++;
		//station_class.spec = realloc(station_class.spec, station_class.stations * sizeof(*station_class.spec));
		station_class.spec = Arrays.copyOf(station_class.spec, station_class.stations);

		station_class.spec[i] = spec;
	}

	/**
	 * Retrieve a station spec from a class.
	 * @param sclass Index of the station class.
	 * @param station The station index with the class.
	 * @return The station spec.
	 */
	static StationSpec GetCustomStation(StationClassID sclass, int station)
	{
		assert(sclass.ordinal() < STAT_CLASS_MAX);
		if (station < station_classes[sclass.ordinal()].stations)
			return station_classes[sclass.ordinal()].spec[station];

		// If the custom station isn't defined any more, then the GRF file
		// probably was not loaded.
		return null;
	}
	
	
}
