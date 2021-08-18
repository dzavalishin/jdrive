package strgen;

public class CmdStruct 
{
	String cmd;
	ParseCmdProc proc;
	long value;
	int consumes;
	int flags;

	
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
	void accept(Emitter e, String buf, int value);
}