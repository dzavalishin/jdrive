package com.dzavalishin.aystar;

import com.dzavalishin.game.Depot;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.NPFFoundTargetData;
import com.dzavalishin.game.Npf;
import com.dzavalishin.game.Pathfind;
import com.dzavalishin.game.Pbs;
import com.dzavalishin.game.Rail;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.struct.FindLengthOfTunnelResult;
import com.dzavalishin.struct.OpenListNode;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.util.BitOps;

public class NpfAyStar extends AyStar 
{

	public NpfAyStar() {
		setLoops_per_tick(0);
		setMax_path_cost(0);
		setMax_search_nodes(Global._patches.npf_max_search_nodes);
	}

	/* To be called when current contains the (shortest route to) the target node.
	 * Will fill the contents of the NPFFoundTargetData using
	 * AyStarNode[NPF_TRACKDIR_CHOICE].
	 */
	//static void NPFSaveTargetData(AyStar  as, OpenListNode  current)
	@Override
	protected void foundEndNode(OpenListNode current)
	{
		NPFFoundTargetData ftd = user_path;
		ftd.best_trackdir = current.path.node.user_data[Npf.NPF_TRACKDIR_CHOICE];
		ftd.best_path_dist = current.g;
		ftd.best_bird_dist = 0;
		ftd.node = current.path.node;
		ftd.path = current.path;
	}








	/* Will just follow the results of GetTileTrackStatus concerning where we can
	 * go and where not. Uses AyStar.user_data[NPF_TYPE] as the transport type and
	 * an argument to GetTileTrackStatus. Will skip tunnels, meaning that the
	 * entry and exit are neighbours. Will fill
	 * AyStarNode.user_data[NPF_TRACKDIR_CHOICE] with an appropriate value, and
	 * copy AyStarNode.user_data[NPF_NODE_FLAGS] from the parent */
	//static void NPFFollowTrack(AyStar  aystar, OpenListNode  current)
	@Override
	protected void getNeighbours(OpenListNode current)
	{
		/*Trackdir*/ /*Trackdir*/
		int src_trackdir = current.path.node.direction;
		TileIndex src_tile = current.path.node.tile;
		/*DiagDirection*/ int src_exitdir = Rail.TrackdirToExitdir(src_trackdir);
		FindLengthOfTunnelResult flotr;
		TileIndex dst_tile;
		int i;
		/*TrackdirBits*/ int trackdirbits, ts;
		//TransportType type = aystar.user_data[NPF_TYPE];
		TransportType type = this.userTransportType;// user_data[Npf.NPF_TYPE];
		/* Initialize to 0, so we can jump out (return) somewhere an have no neighbours */
		this.num_neighbours = 0;
		Global.DEBUG_npf( 4, "Expanding: (%d, %d, %d) [%d]", src_tile.TileX(), src_tile.TileY(), src_trackdir, src_tile.getTile());

		endNodeCheck(current);

		/* Find dest tile */
		if (src_tile.IsTileType(TileTypes.MP_TUNNELBRIDGE) && BitOps.GB(src_tile.getMap().m5, 4, 4) == 0 &&
				BitOps.GB(src_tile.getMap().m5, 0, 2) == src_exitdir) {
			/* This is a tunnel. We know this tunnel is our type,
			 * otherwise we wouldn't have got here. It is also facing us,
			 * so we should skip it's body */
			flotr = Pathfind.FindLengthOfTunnel(src_tile, src_exitdir);
			dst_tile = flotr.tile;
		} else {
			if (type != TransportType.Water && (src_tile.IsRoadStationTile() || src_tile.IsTileDepotType(type)))
			{
				/* This is a road station or a train or road depot. We can enter and exit
				 * those from one side only. Trackdirs don't support that (yet), so we'll
				 * do this here. */

				/*DiagDirection*/ int exitdir;
				/* Find out the exit direction first */
				if (src_tile.IsRoadStationTile())
					exitdir = Station.GetRoadStationDir(src_tile);
				else /* Train or road depot. Direction is stored the same for both, in map5 */
					exitdir = Depot.GetDepotDirection(src_tile, type);

				/* Let's see if were headed the right way into the depot, and reverse
				 * otherwise (only for trains, since only with trains you can
				 * (sometimes) reach tiles after reversing that you couldn't reach
				 * without reversing. */
				if (src_trackdir == Rail.DiagdirToDiagTrackdir(Rail.ReverseDiagdir(exitdir)) && type == TransportType.Rail)
					/* We are headed inwards. We can only reverse here, so we'll not
					 * consider this direction, but jump ahead to the reverse direction.
					 * It would be nicer to return one neighbour here (the reverse
					 * trackdir of the one we are considering now) and then considering
					 * that one to return the tracks outside of the depot. But, because
					 * the code layout is cleaner this way, we will just pretend we are
					 * reversed already */
					src_trackdir = Rail.ReverseTrackdir(src_trackdir);
			}
			/* This a normal tile, a bridge, a tunnel exit, etc. */
			dst_tile = TileIndex.AddTileIndexDiffCWrap(src_tile, TileIndex.TileIndexDiffCByDir(Rail.TrackdirToExitdir(src_trackdir)));
			if (!dst_tile.isValid()) {
				/* We reached the border of the map */
				/* TODO Nicer control flow for this */
				return;
			}
		}

		/* I can't enter a tunnel entry/exit tile from a tile above the tunnel. Note
		 * that I can enter the tunnel from a tile below the tunnel entrance. This
		 * solves the problem of vehicles wanting to drive off a tunnel entrance */
		if (dst_tile.IsTileType(TileTypes.MP_TUNNELBRIDGE) && BitOps.GB(dst_tile.getMap().m5, 4, 4) == 0 &&
				dst_tile.GetTileZ() < src_tile.GetTileZ()) {
			return;
		}

		/* check correct rail type (mono, maglev, etc) */
		if (type == TransportType.Rail) {
			/* RailType */ int dst_type = Rail.GetTileRailType( dst_tile, src_trackdir);
			if (!Rail.IsCompatibleRail(this.user_data[Npf.NPF_RAILTYPE], dst_type))
				return;
		}

		/* Check the owner of the tile */
		if (!Npf.VehicleMayEnterTile( PlayerID.get( this.user_data[Npf.NPF_OWNER] ), dst_tile, Rail.TrackdirToExitdir(src_trackdir))) {
			return;
		}

		/* Determine available tracks */
		if (type != TransportType.Water && (dst_tile.IsRoadStationTile() || dst_tile.IsTileDepotType(type)))
		{
			/* Road stations and road and train depots return 0 on GTTS, so we have to do this by hand... */
			/*DiagDirection*/ int exitdir;
			if (dst_tile.IsRoadStationTile())
				exitdir = Station.GetRoadStationDir(dst_tile);
			else /* Road or train depot */
				exitdir = Depot.GetDepotDirection(dst_tile, type);
			/* Find the trackdirs that are available for a depot or station with this
			 * orientation. They are only "inwards", since we are reaching this tile
			 * from some other tile. This prevents vehicles driving into depots from
			 * the back */
			ts = Rail.TrackdirToTrackdirBits(Rail.DiagdirToDiagTrackdir(Rail.ReverseDiagdir(exitdir)));
		} else {
			ts = Landscape.GetTileTrackStatus(dst_tile, type);
		}
		trackdirbits = ts & Rail.TRACKDIR_BIT_MASK; /* Filter out signal status and the unused bits */

		Global.DEBUG_npf( 4, "Next node: (%d, %d) [%d], possible trackdirs: %#x", dst_tile.TileX(), dst_tile.TileY(), dst_tile.getTile(), trackdirbits);
		/* Select only trackdirs we can reach from our current trackdir */
		trackdirbits &= Rail.TrackdirReachesTrackdirs(src_trackdir);
		if (Global._patches.forbid_90_deg && (type == TransportType.Rail || type == TransportType.Water)) /* Filter out trackdirs that would make 90 deg turns for trains */

			trackdirbits &= ~Rail.TrackdirCrossesTrackdirs(src_trackdir);

		if (BitOps.KillFirstBit2x64(trackdirbits) != 0)
			Npf.NPFSetFlag(current.path.node, Npf.NPF_FLAG_PBS_CHOICE, true);

		/* When looking for 'any' route, ie when already inside a pbs block, discard all tracks that would cross
		   other reserved tracks, so we *always* will find a valid route if there is one */
		if (!(Npf.NPFGetFlag(current.path.node, Npf.NPF_FLAG_PBS_EXIT)) && (this.user_data[Npf.NPF_PBS_MODE] == Pbs.PBS_MODE_ANY))
			trackdirbits &= ~Pbs.PBSTileUnavail(dst_tile);

		Global.DEBUG_npf(6,"After filtering: (%d, %d), possible trackdirs: %#x", dst_tile.TileX(), dst_tile.TileY(), trackdirbits);

		i = 0;
		/* Enumerate possible track */
		while (trackdirbits != 0) 
		{
			/*Trackdir*/ int dst_trackdir;
			dst_trackdir =  BitOps.FindFirstBit2x64(trackdirbits);
			trackdirbits = BitOps.KillFirstBit2x64(trackdirbits);
			Global.DEBUG_npf( 5, "Expanded into trackdir: %d, remaining trackdirs: %#x", dst_trackdir, trackdirbits);

			/* Check for oneway signal against us */
			if (dst_tile.IsTileType(TileTypes.MP_RAILWAY) && dst_tile.GetRailTileType() == Rail.RAIL_TYPE_SIGNALS) {
				if (Rail.HasSignalOnTrackdir(dst_tile, Rail.ReverseTrackdir(dst_trackdir)) && !Rail.HasSignalOnTrackdir(dst_tile, dst_trackdir))
					// if one way signal not pointing towards us, stop going in this direction.
					break;
			}
			{
				/* We've found ourselves a neighbour :-) */
				this.neighbours[i] = new AyStarNode(); 
				AyStarNode  neighbour = this.neighbours[i];
				neighbour.tile = dst_tile;
				neighbour.direction = dst_trackdir;
				/* Save user data */
				neighbour.user_data[Npf.NPF_NODE_FLAGS] = current.path.node.user_data[Npf.NPF_NODE_FLAGS];
				Npf.NPFFillTrackdirChoice(neighbour, current);
			}
			i++;
		}
		this.num_neighbours = i;
	}

	
	
	private AyStar_CalculateG CalculateG = null;
	private AyStar_CalculateH CalculateH = null;
	
	
	public void setCalculateH(AyStar_CalculateH func) {
		CalculateH = func;		
	}

	@Override
	protected int calculateH(AyStarNode current, OpenListNode parent) {
		return CalculateH.apply(this, current, parent);
	}


	public void setCalculateG(AyStar_CalculateG func) {
		CalculateG = func;		
	}

	@Override
	protected int calculateG(AyStarNode current, OpenListNode parent) {
		return CalculateG.apply(this, current, parent);
	}

	private AyStar_EndNodeCheck EndNodeCheck = null;
	
	public void setEndNodeCheck(AyStar_EndNodeCheck func) {
		EndNodeCheck = func;		
	}

	@Override
	public int endNodeCheck(OpenListNode current) 
	{
		if( null != EndNodeCheck )
			return EndNodeCheck.apply(this, current);
		return 0;
	}

	private AyStar_BeforeExit BeforeExit;

	public void setBeforeExit(AyStar_BeforeExit func) {
		BeforeExit = func;		
	}

	@Override
	protected void beforeExit() 
	{ 
		if( null != BeforeExit ) BeforeExit.apply(this);
	} 


}
