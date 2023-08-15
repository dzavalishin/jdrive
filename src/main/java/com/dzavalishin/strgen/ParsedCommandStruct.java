package com.dzavalishin.strgen;

public class ParsedCommandStruct 
{
	int np;
	final CmdPair [] pairs = new CmdPair[32];
	final CmdStruct [] cmd = new CmdStruct[32]; // ordered by param #

}
