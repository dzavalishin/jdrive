package com.dzavalishin.strgen;

import java.io.IOException;

public class CmdStruct 
{
	final String cmd;
	final ParseCmdProc proc;
	final long value;
	final int consumes;
	final int flags;

	
	public CmdStruct(String cs, ParseCmdProc p, int v, int c, int f) 
	{
		cmd = cs;
		proc = p;
		value = v;
		consumes = c;
		flags = f;
	}
	
}


//typedef void (*ParseCmdProc)(char *buf, int value);
@FunctionalInterface
interface ParseCmdProc
{
	void accept(Emitter e, String buf, long value) throws IOException;
}