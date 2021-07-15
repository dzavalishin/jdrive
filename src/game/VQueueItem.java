package game;

public class VQueueItem 
{
	Vehicle data;
	
	// Position in queue
	int position;
	VQueueItem below;
	VQueueItem above;

	// Queue item belongs to (so we can have reverse lookups)
	VehicleQueue queue;
}
