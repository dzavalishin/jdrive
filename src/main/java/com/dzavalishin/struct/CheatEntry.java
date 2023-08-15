package com.dzavalishin.struct;

import com.dzavalishin.ids.StringID;

public class CheatEntry {
	//public ce_type type;    // type of selector
	public int flags;		// selector flags
	public StringID str; // string with descriptive text
	public Object variable; // pointer to the variable
	public boolean [] been_used; // has this cheat been used before?
	public CheckButtonClick click_proc; // procedure
	public int min,max; // range for spinbox setting
	public int step;   // step for spinbox
}

//typedef int CheckButtonClick(int, int);

@FunctionalInterface
interface CheckButtonClick
{
	int click(int a, int b);
}
/*
enum ce_type {
	CE_BOOL = 0,
	CE_UINT8 = 1,
	CE_INT16 = 2,
	CE_UINT16 = 3,
	CE_INT32 = 4,
	CE_BYTE = 5,
	CE_CLICK = 6,
}
*/