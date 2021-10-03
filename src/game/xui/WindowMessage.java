package game.xui;

import java.io.Serializable;

import game.enums.WindowEvents;

public class WindowMessage implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public int msg;
	public int wparam;
	public int lparam;
}