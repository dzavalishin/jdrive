package game.struct;

import java.io.Serializable;

import game.Vehicle;
import game.util.VehicleQueue;

public class VQueueItem implements Serializable
{
	private static final long serialVersionUID = 1L;

	public Vehicle data;
	
	// Position in queue
	public int position;
	public VQueueItem below;
	public VQueueItem above;

	// Queue item belongs to (so we can have reverse lookups)
	public VehicleQueue queue;
}
