package com.dzavalishin.xui;

import java.io.Serializable;

public class WindowMessage implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public int msg;
	public int wparam;
	public int lparam;
}