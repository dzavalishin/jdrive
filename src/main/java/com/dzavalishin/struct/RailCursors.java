package com.dzavalishin.struct;

import com.dzavalishin.ids.CursorID;

public class RailCursors {
	public final CursorID rail_ns;
	public final CursorID rail_swne;
	public final CursorID rail_ew;
	public final CursorID rail_nwse;
	public final CursorID autorail;
	public final CursorID depot;
	public final CursorID tunnel;
	public final CursorID convert;

	public RailCursors(int[] cur) {
		int i = 0;
		 rail_ns = CursorID.get( cur[i++]);
		 rail_swne = CursorID.get( cur[i++]);
		 rail_ew = CursorID.get( cur[i++]);
		 rail_nwse = CursorID.get( cur[i++]);
		 autorail = CursorID.get( cur[i++]);
		 depot = CursorID.get( cur[i++]);
		 tunnel = CursorID.get( cur[i++]);
		 convert = CursorID.get( cur[i++]);
	}
}
