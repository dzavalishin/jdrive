package game;

import game.aystar.AyStarNode;
import game.struct.PathNode;

/* Meant to be stored in AyStar.userpath */
public class NPFFoundTargetData 
{
	int best_bird_dist;	/* The best heuristic found. Is 0 if the target was found */
	int best_path_dist;	/* The shortest path. Is (int)-1 if no path is found */
	//Trackdir 
	int best_trackdir;	/* The trackdir that leads to the shortest path/closest birds dist */
	AyStarNode node;	/* The node within the target the search led us to */
	PathNode path;

	
	public static final NPFFoundTargetData ON_TARGET = createOnTarget();

	private static NPFFoundTargetData createOnTarget() 
	{
		NPFFoundTargetData d = new NPFFoundTargetData();
		d.best_trackdir = 0xff;
		return d;
	}
	
}


