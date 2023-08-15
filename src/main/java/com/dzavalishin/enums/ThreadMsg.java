package com.dzavalishin.enums;

/**
 *  To have a concurrently running thread interface with the main program, use
 * the OTTD_SendThreadMessage() function. Actions to perform upon the message are handled
 * in the ProcessSentMessage() function 
 **/

public enum ThreadMsg 
{
	MSG_OTTD_SAVETHREAD_ZERO, // unused, uses ordinal 0
	MSG_OTTD_SAVETHREAD_START,
	MSG_OTTD_SAVETHREAD_DONE,
	MSG_OTTD_SAVETHREAD_ERROR,

	//MSG_OTTD_SAVETHREAD_START  = 1,
	//MSG_OTTD_SAVETHREAD_DONE   = 2,
	//MSG_OTTD_SAVETHREAD_ERROR  = 3,
}
