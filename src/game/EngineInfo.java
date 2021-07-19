package game;

/** Information about a vehicle
 * @see table/engines.h
 */

public class EngineInfo {
	int base_intro;
	byte unk2;              ///< Carriages have the highest bit set in this one
	byte lifelength;
	byte base_life;
	byte railtype;
	byte climates;
	int refit_mask;

}
