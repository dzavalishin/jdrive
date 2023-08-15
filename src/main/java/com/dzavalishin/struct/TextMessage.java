package com.dzavalishin.struct;

public class TextMessage 
{
	public String message;
	public int color;
	public int end_date;

	public TextMessage(String buf, int clr, int date) {
		message = buf;
		color = clr;
		end_date = date;		
	}
}
