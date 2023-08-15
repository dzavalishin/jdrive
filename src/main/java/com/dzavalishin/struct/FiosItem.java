package com.dzavalishin.struct;

import com.dzavalishin.enums.FiosType;

// Deals with finding savegames
public class FiosItem 
{
	public FiosType type;
	public long mtime;
	public String title; //[64];
	public String name ;// [256-12-64];

}
