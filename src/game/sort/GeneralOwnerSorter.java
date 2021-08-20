package game.sort;

import game.struct.SortStruct;

public class GeneralOwnerSorter extends AbstractVehicleSorter
{
	public int compare (SortStruct a, SortStruct b)
	{
		return a.owner - b.owner;
	}
}