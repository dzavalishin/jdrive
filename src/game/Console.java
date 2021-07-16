package game;

public class Console {
	// maximum length of a typed in command
	public static final int ICON_CMDLN_SIZE = 255;
	// maximum length of a totally expanded command
	public static final int  ICON_MAX_STREAMSIZE = 1024;
	
	// ** console parser ** //
	static IConsoleCmd   _iconsole_cmds;    // list of registred commands
	static IConsoleVar   _iconsole_vars;    // list of registred vars
	static IConsoleAlias _iconsole_aliases; // list of registred aliases

	// ** console colors/modes ** //
	static byte _icolour_def;
	static byte _icolour_err;
	static byte _icolour_warn;
	static byte _icolour_dbg;
	static byte _icolour_cmd;
	static IConsoleModes _iconsole_mode;

	
}






enum IConsoleVarTypes {
	ICONSOLE_VAR_BOOLEAN,
	ICONSOLE_VAR_BYTE,
	ICONSOLE_VAR_UINT16,
	ICONSOLE_VAR_UINT32,
	ICONSOLE_VAR_INT16,
	ICONSOLE_VAR_INT32,
	ICONSOLE_VAR_STRING
} 

enum IConsoleModes {
	ICONSOLE_FULL,
	ICONSOLE_OPENED,
	ICONSOLE_CLOSED
}

enum IConsoleHookTypes {
	ICONSOLE_HOOK_ACCESS,
	ICONSOLE_HOOK_PRE_ACTION,
	ICONSOLE_HOOK_POST_ACTION
}

/** --Hooks--
 * Hooks are certain triggers get get accessed/executed on either
 * access, before execution/change or after execution/change. This allows
 * for general flow of permissions or special action needed in some cases
 */
//typedef bool IConsoleHook(void);
abstract class IConsoleHooks
{
	abstract boolean access(); // trigger when accessing the variable/command
	abstract boolean pre();    // trigger before the variable/command is changed/executed
	abstract boolean post();   // trigger after the variable/command is changed/executed
} 

/** --Commands--
 * Commands are commands, or functions. They get executed once and any
 * effect they produce are carried out. The arguments to the commands
 * are given to them, each input word seperated by a double-quote (") is an argument
 * If you want to handle multiple words as one, enclose them in double-quotes
 * eg. 'say "hello sexy boy"'
 */
//typedef bool (IConsoleCmdProc)(byte argc, char *argv[]);

//struct IConsoleCmd;
class IConsoleCmd {
	String name;               // name of command
	IConsoleCmd next; // next command in list

	// TODO fixme
	//IConsoleCmdProc proc;    // process executed when command is typed
	IConsoleHooks hook;       // any special trigger action that needs executing
} 

/** --Variables--
 * Variables are pointers to real ingame variables which allow for
 * changing while ingame. After changing they keep their new value
 * and can be used for debugging, gameplay, etc. It accepts:
 * - no arguments; just print out current value
 * - '= <new value>' to assign a new value to the variable
 * - '++' to increase value by one
 * - '--' to decrease value by one
 */
class IConsoleVar {
	String name;               // name of the variable
	IConsoleVar next; // next variable in list

	// TODO fixme
	//void *addr;               // the address where the variable is pointing at
	int size;              // size of the variable, used for strings
	String help;               // the optional help string shown when requesting information
	IConsoleVarTypes type;    // type of variable (for correct assignment/output)
	// TODO fixme
	//IConsoleCmdProc proc;    // some variables need really special handling, use a callback function for that
	IConsoleHooks hook;       // any special trigger action that needs executing
}

/** --Aliases--
 * Aliases are like shortcuts for complex functions, variable assignments,
 * etc. You can use a simple alias to rename a longer command (eg 'lv' for
 * 'list_vars' for example), or concatenate more commands into one
 * (eg. 'ng' for 'load %A; unpause; debug_level 5'). Aliases can parse the arguments
 * given to them in the command line.
 * - "%A - %Z" substitute arguments 1 t/m 26
 * - "%+" lists all parameters keeping them seperated
 * - "%!" also lists all parameters but presenting them to the aliased command as one argument
 * - ";" allows for combining commands (see example 'ng')
 */
class IConsoleAlias {
	String name;                 // name of the alias
	IConsoleAlias next; // next alias in list

	String cmdline;              // command(s) that is/are being aliased
}


