package com.dzavalishin.game;

import com.dzavalishin.ids.StringID;
import com.dzavalishin.util.StringTable;

// Just a shortcut
public class Str extends StringTable {
	//public static final StringID  INVALID_STRING_ID = Global.INVALID_STRING_ID;
	public static final int INVALID_STRING = 0xFFFF;

	public static StringID INVALID_STRING_ID() {
		return StringID.getInvalid();
	}

}
