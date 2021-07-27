package game;

/** Information about a vehicle
 * @see table/engines.h
 */

public class EngineInfo {
	public int base_intro;
	public byte unk2;              ///< Carriages have the highest bit set in this one
	public byte lifelength;
	public byte base_life;
	public byte railtype;
	public byte climates;
	public int refit_mask;

}
