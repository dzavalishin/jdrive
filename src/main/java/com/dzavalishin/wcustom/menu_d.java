package com.dzavalishin.wcustom;

import com.dzavalishin.ids.StringID;

public class menu_d implements AbstractWinCustom 
{
	public int item_count; /* follow_vehicle */
	public int sel_index;		/* scrollpos_x */
	public int main_button; /* scrollpos_y */
	public int action_id;
	public StringID string_id; /* unk30 */
	public int checked_items; /* unk32 */
	public int disabled_items;
}


/*
	public byte item_count; // follow_vehicle 
	public byte sel_index;		// scrollpos_x 
	public byte main_button; // scrollpos_y 
	public byte action_id;
	public StringID string_id; // unk30 
	public int checked_items; // unk32 
	public byte disabled_items;

*/