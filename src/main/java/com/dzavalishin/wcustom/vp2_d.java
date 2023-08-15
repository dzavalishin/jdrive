package com.dzavalishin.wcustom;

import com.dzavalishin.game.NewsItem;

// vp2_d is the same as vp_d, except for the data_# values..
public class vp2_d implements AbstractWinCustom 
{
	public int follow_vehicle;
	public int scrollpos_x;
	public int scrollpos_y;
	public int data_1;
	public int data_2;
	public int data_3;
	public NewsItem ni; // work for news_d too
}