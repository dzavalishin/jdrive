package game.struct;

import game.Vehicle;
import game.VehicleQueue;

public class VQueueItem 
{
	public Vehicle data;
	
	// Position in queue
	public int position;
	public VQueueItem below;
	public VQueueItem above;

	// Queue item belongs to (so we can have reverse lookups)
	public VehicleQueue queue;
}
