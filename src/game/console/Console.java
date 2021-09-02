package game.console;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import game.Global;
import game.Hal;
import game.struct.Textbuf;
import game.util.BitOps;
import game.util.Strings;
import game.xui.Gfx;
import game.xui.Widget;
import game.xui.Window;
import game.xui.WindowDesc;
import game.xui.WindowEvent;

public class Console //extends ConsoleCmds 
{
	// maximum length of a typed in command
	public static final int ICON_CMDLN_SIZE = 255;
	// maximum length of a totally expanded command
	public static final int  ICON_MAX_STREAMSIZE = 1024;

	// ** console parser ** //
	static Map<String,IConsoleCmd>   _iconsole_cmds = new HashMap<>();    // list of registred commands
	static Map<String,IConsoleVar>   _iconsole_vars = new HashMap<>();    // list of registred vars
	static Map<String,IConsoleAlias> _iconsole_aliases = new HashMap<>(); // list of registred aliases

	// ** console colors/modes ** //
	static byte _icolour_def;
	static byte _icolour_err;
	static byte _icolour_warn;
	static byte _icolour_dbg;
	static byte _icolour_cmd;
	static IConsoleModes _iconsole_mode = IConsoleModes.ICONSOLE_CLOSED;



	public static final int ICON_BUFFER  = 79;
	public static final int ICON_HISTORY_SIZE = 20;
	public static final int ICON_LINE_HEIGHT = 12;
	public static final int ICON_RIGHT_BORDERWIDTH = 10;
	public static final int ICON_BOTTOM_BORDERWIDTH = 12;
	public static final int ICON_MAX_ALIAS_LINES = 40;
	public static final int ICON_TOKEN_COUNT = 20;




	// ** main console ** //
	static Window _iconsole_win; // Pointer to console window
	static boolean _iconsole_inited;
	static final String[]_iconsole_buffer = new String[ICON_BUFFER + 1];
	static final int[] _iconsole_cbuffer = new int[ICON_BUFFER + 1];
	static final Textbuf _iconsole_cmdline = new Textbuf(128);
	static byte _iconsole_scroll;

	// ** stdlib ** //
	//static final byte _stdlib_developer = 1;
	static final boolean _stdlib_con_developer = false;
	static FileWriter _iconsole_output_file = null;

	// ** main console cmd buffer
	static final String[] _iconsole_history = new String[ICON_HISTORY_SIZE];
	static int _iconsole_historypos;

	/* *************** */
	/*  end of header  */
	/* *************** */

	static void IConsoleClearCommand()
	{
		_iconsole_cmdline.allocate(256); // String();
		_iconsole_cmdline.DeleteTextBufferAll();
		if(_iconsole_win != null) _iconsole_win.SetWindowDirty();
	}

	static  void IConsoleResetHistoryPos() {_iconsole_historypos = ICON_HISTORY_SIZE - 1;}


	static final Widget _iconsole_window_widgets[] = {
			//new Widget( WIDGETS_END )
	};

	static final WindowDesc _iconsole_window_desc = new WindowDesc(
			0, 0, 2, 2,
			Window.WC_CONSOLE, 0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_iconsole_window_widgets,
			Console::windowProc
			);

	public static void IConsoleInit()
	{
		_iconsole_output_file = null;
		_icolour_def  =  1;
		_icolour_err  =  3;
		_icolour_warn = 13;
		_icolour_dbg  =  5;
		_icolour_cmd  =  2;
		_iconsole_scroll = ICON_BUFFER;
		_iconsole_historypos = ICON_HISTORY_SIZE - 1;
		_iconsole_inited = true;
		_iconsole_mode = IConsoleModes.ICONSOLE_CLOSED;
		_iconsole_win = null;

		/*
		#ifdef ENABLE_NETWORK // * Initialize network only variables 
		_redirect_console_to_client = 0;
		#endif
		 */


		_iconsole_cmdline.maxlength = ICON_CMDLN_SIZE - 1;

		IConsolePrintF(13, "NextTTD Game Console Revision 7 - %s", Strings._openttd_revision);
		IConsolePrint(12,  "------------------------------------");
		IConsolePrint(12,  "use \"help\" for more information");
		IConsolePrint(12,  "");
		ConsoleCmds.IConsoleStdLibRegister();
		IConsoleClearCommand();
		IConsoleHistoryAdd("");
	}

	static private void IConsoleClearBuffer()
	{
		int i;
		for (i = 0; i <= ICON_BUFFER; i++) {
			//free(_iconsole_buffer[i]);
			_iconsole_buffer[i] = null;
		}
	}

	static private void IConsoleClear()
	{
		//free(_iconsole_cmdline.buf);
		IConsoleClearBuffer();
	}

	static private void IConsoleWriteToLogFile(final String  string)
	{
		if (_iconsole_output_file != null) {
			// if there is an console output file ... also print it there
			//fwrite(string, strlen(string), 1, _iconsole_output_file);
			//fwrite("\n", 1, 1, _iconsole_output_file);
			try {
				_iconsole_output_file.write(string);
				_iconsole_output_file.write("\n");
			} catch (IOException e) {
				
				Global.error(e);
			}
		}
	}

	static boolean CloseConsoleLogIfActive()
	{
		if (_iconsole_output_file != null) 
		{
			IConsolePrintF(_icolour_def, "file output complete");
			try {
				_iconsole_output_file.close();
			} catch (IOException e) {
				Global.error(e);
			}
			_iconsole_output_file = null;
			return true;
		}

		return false;
	}

	static void IConsoleFree()
	{
		_iconsole_inited = false;
		IConsoleClear();
		CloseConsoleLogIfActive();
	}

	public static void IConsoleResize()
	{
		_iconsole_win = Window.FindWindowById(Window.WC_CONSOLE, 0);

		switch (_iconsole_mode) {
		case ICONSOLE_OPENED:
			_iconsole_win.setSize(Hal.getScreenWidth(), Hal.getScreenHeight() / 3);
			break;
		case ICONSOLE_FULL:
			_iconsole_win.setSize(Hal.getScreenWidth(), Hal.getScreenHeight() - ICON_BOTTOM_BORDERWIDTH );
			break;
		default: break;
		}

		Hal.MarkWholeScreenDirty();
	}

	/**
	 * Turn on/off
	 */
	public static void IConsoleSwitch()
	{
		switch (_iconsole_mode) {
		case ICONSOLE_CLOSED:
			_iconsole_win = Window.AllocateWindowDesc(_iconsole_window_desc,0);
			_iconsole_win.setSize(Hal.getScreenWidth(), Hal.getScreenHeight() / 3);
			_iconsole_mode = IConsoleModes.ICONSOLE_OPENED;
			Global._no_scroll = BitOps.RETSETBIT(Global._no_scroll, Global.SCROLL_CON); // override cursor arrows; the gamefield will not scroll
			break;
		case ICONSOLE_OPENED: case ICONSOLE_FULL:
			Window.DeleteWindowById(Window.WC_CONSOLE, 0);
			_iconsole_win = null;
			_iconsole_mode = IConsoleModes.ICONSOLE_CLOSED;
			Global._no_scroll = BitOps.RETCLRBIT(Global._no_scroll, Global.SCROLL_CON);
			break;
		}

		Hal.MarkWholeScreenDirty();
	}

	public static void IConsoleClose() {if (_iconsole_mode == IConsoleModes.ICONSOLE_OPENED) IConsoleSwitch();}
	static void IConsoleOpen()  {if (_iconsole_mode == IConsoleModes.ICONSOLE_CLOSED) IConsoleSwitch();}

	/**
	 * Add the entered line into the history so you can look it back
	 * scroll, etc. Put it to the beginning as it is the latest text
	 * @param cmd Text to be entered into the 'history'
	 */
	static void IConsoleHistoryAdd(final String cmd)
	{
		//free(_iconsole_history[ICON_HISTORY_SIZE - 1]);

		//memmove(_iconsole_history[1], &_iconsole_history[0],				sizeof(_iconsole_history[0]) * (ICON_HISTORY_SIZE - 1));
		System.arraycopy(_iconsole_history, 0, _iconsole_history, 1, ICON_HISTORY_SIZE - 1);
		_iconsole_history[0] = cmd;
		IConsoleResetHistoryPos();
	}

	/**
	 * Navigate Up/Down in the history of typed commands
	 * @param direction Go further back in history (+1), go to recently typed commands (-1)
	 */
	static void IConsoleHistoryNavigate(int direction)
	{
		int i = _iconsole_historypos + direction;

		if(_iconsole_history[0] == null) return;
		
		// watch out for overflows, just wrap around
		if (i < 0) i = ICON_HISTORY_SIZE - 1;
		if (i >= ICON_HISTORY_SIZE) i = 0;

		if (direction > 0)
			if (_iconsole_history[i] == null) i = 0;

		if (direction < 0) {
			while (i > 0 && _iconsole_history[i] == null) i--;
		}

		_iconsole_historypos = i;
		IConsoleClearCommand();
		// copy history to 'command prompt / bash'
		assert(_iconsole_history[i] != null && BitOps.IS_INT_INSIDE(i, 0, ICON_HISTORY_SIZE));
		//ttd_strlcpy(_iconsole_cmdline.buf, _iconsole_history[i], _iconsole_cmdline.maxlength);
		_iconsole_cmdline.setText(_iconsole_history[i]);
		//UpdateTextBufferSize(_iconsole_cmdline);
	}

	/**
	 * Handle the printing of text entered into the console or redirected there
	 * by any other means. Text can be redirected to other players in a network game
	 * as well as to a logfile. If the network server is a dedicated server, all activities
	 * are also logged. All lines to print are added to a temporary buffer which can be
	 * used as a history to print them onscreen
	 * @param color_code the colour of the command. Red in case of errors, etc.
	 * @param string the message entered or output on the console (notice, error, etc.)
	 */
	static void IConsolePrint(int color_code, final String  string)
	{
		/*#ifdef ENABLE_NETWORK
		if (_redirect_console_to_client != 0) {
			/* Redirect the string to the client * /
			SEND_COMMAND(PACKET_SERVER_RCON)(NetworkFindClientStateFromIndex(_redirect_console_to_client), color_code, string);
			return;
		}
		#endif
		 */

		if (Global._network_dedicated) {
			Global.error("%s\n", string);
			IConsoleWriteToLogFile(string);
			return;
		}

		if (!_iconsole_inited) return;

		/* move up all the strings in the buffer one place and do the same for colour
		 * to accomodate for the new command/message */
		//free(_iconsole_buffer[0]);
		//memmove(&_iconsole_buffer[0], &_iconsole_buffer[1], sizeof(_iconsole_buffer[0]) * ICON_BUFFER);
		System.arraycopy(_iconsole_buffer, 1, _iconsole_buffer, 0, ICON_BUFFER);
		_iconsole_buffer[ICON_BUFFER] = string;

		/*{ // filter out unprintable characters
			char *i;
			for (i = _iconsole_buffer[ICON_BUFFER]; *i != '\0'; i++)
				if (!BitOps.IsValidAsciiChar((byte)*i)) *i = ' ';

			for( int i = 0; i < )
		}*/

		//memmove(&_iconsole_cbuffer[0], &_iconsole_cbuffer[1], sizeof(_iconsole_cbuffer[0]) * ICON_BUFFER);
		System.arraycopy(_iconsole_cbuffer, 1, _iconsole_cbuffer, 0, ICON_BUFFER);
		_iconsole_cbuffer[ICON_BUFFER] = color_code;

		IConsoleWriteToLogFile(string);

		if(_iconsole_win != null) _iconsole_win.SetWindowDirty();
	}

	/**
	 * Handle the printing of text entered into the console or redirected there
	 * by any other means. Uses printf() style format, for more information look
	 * at @IConsolePrint()
	 */
	static void IConsolePrintF(int color_code, final String s, Object ... args)
	{
		//va_list va;
		String buf = String.format(s, args);

		//va_start(va, s);
		//vsnprintf(buf, sizeof(buf), s, va);
		//va_end(va);

		IConsolePrint(color_code, buf);
	}

	/**
	 * It is possible to print debugging information to the console,
	 * which is achieved by using this function. Can only be used by
	 * debug() in debug.c. You need at least a level 2 (developer) for debugging
	 * messages to show up
	 */
	static void IConsoleDebug(final String string)
	{
		// TODO must be value of console var if (_stdlib_developer > 0)if (_stdlib_developer > 1)
			IConsolePrintF(_icolour_dbg, "dbg: %s", string);
	}

	/**
	 * It is possible to print warnings to the console. These are mostly
	 * errors or mishaps, but non-fatal. You need at least a level 1 (developer) for
	 * debugging messages to show up
	 */
	static void IConsoleWarning(final String  string)
	{
		// TODO must be value of console var if (_stdlib_developer > 0)
		//if(Global._debug_misc_level > 0)
			IConsolePrintF(_icolour_warn, "WARNING: %s", string);
	}

	/**
	 * It is possible to print error information to the console. This can include
	 * game errors, or errors in general you would want the user to notice
	 */
	static void IConsoleError(final String  string)
	{
		IConsolePrintF(_icolour_err, "ERROR: %s", string);
	}

	/**
	 * Change a string into its number representation. Supports
	 * decimal and hexadecimal numbers as well as 'on'/'off' 'true'/'false'
	 * @param value the variable a successfull conversion will be put in
	 * @param arg the string to be converted
	 * @return Return true on success or false on failure
	 */
	static boolean GetArgumentInteger(int []value, final String arg)
	{
		//char *endptr;

		if (arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("true")) {
			value[0] = 1;
			return true;
		}
		if (arg.equalsIgnoreCase("off") || arg.equalsIgnoreCase("false")) {
			value[0] = 0;
			return true;
		}

		try {
			value[0] = Integer.parseInt(arg);//strtoul(arg, &endptr, 0);
		} catch(NumberFormatException e)
		{
			return false;
		}
		return true;
	}


	/**
	 * Add a hook to a command that will be triggered at certain points
	 * @param name name of the command that the hook is added to
	 * @param type type of hook that is added (ACCESS, BEFORE and AFTER change)
	 * @param proc function called when the hook criteria is met
	 */
	static void IConsoleCmdHookAdd(final String name, IConsoleHookTypes type, IConsoleHook proc)
	{
		IConsoleCmd cmd = IConsoleCmdGet(name);
		if (cmd == null) return;
		cmd.hook.IConsoleHookAdd(type, proc);
	}

	/**
	 * Add a hook to a variable that will be triggered at certain points
	 * @param name name of the variable that the hook is added to
	 * @param type type of hook that is added (ACCESS, BEFORE and AFTER change)
	 * @param proc function called when the hook criteria is met
	 */
	static void IConsoleVarHookAdd(final String name, IConsoleHookTypes type, IConsoleHook proc)
	{
		IConsoleVar variable = IConsoleVarGet(name);
		if (variable == null) return;
		variable.hook.IConsoleHookAdd(type, proc);
	}

	/**
	 * Perhaps ugly macro, but this saves us the trouble of writing the same function
	 * three types, just with different variables. Yes, templates would be handy. It was
	 * either this define or an even more ugly void* magic function
	 * /
	#define IConsoleAddSorted(_base, item_new, IConsoleType, type)                      \
	{                                                                                   \
		IConsoleType *item, *item_before;                                                 \
		// first command                                                                \
		if (_base == null) {                                                              \
			_base = item_new;                                                               \
			return;                                                                         \
		}                                                                                 \
		\
		item_before = null;                                                               \
		item = _base;                                                                     \
		\
		// BEGIN - Alphabetically insert the commands into the linked list             \
		while (item != null) {                                                            \
			int i = strcmp(item.name, item_new.name);                                     \
			if (i == 0) {                                                                   \
				IConsoleError(type " with this name already exists; insertion aborted");      \
				free(item_new);                                                               \
				return;                                                                       \
			}                                                                               \
			\
			if (i > 0) break; // insert at this position                                  \
			\
			item_before = item;                                                             \
			item = item.next;                                                              \
		}                                                                                 \
		\
		if (item_before == null) {                                                        \
			_base = item_new;                                                               \
		} else                                                                            \
		item_before.next = item_new;                                                   \
		\
		item_new.next = item;                                                            \
		// END - Alphabetical insert                                                    \
	}
	 */

	/**
	 * Register a new command to be used in the console
	 * @param name name of the command that will be used
	 * @param proc function that will be called upon execution of command
	 */
	static void IConsoleCmdRegister(final String name, IConsoleCmdProc proc)
	{
		IConsoleCmd item_new = new IConsoleCmd();

		//item_new.next = null;
		item_new.proc = proc;
		item_new.name = name;

		item_new.hook.access = null;
		item_new.hook.pre = null;
		item_new.hook.post = null;

		//IConsoleAddSorted(_iconsole_cmds, item_new, "a command");
		if( _iconsole_cmds.get(name) != null )
		{
			IConsoleError("a command with this name already exists; insertion aborted");      
			return;                                                                       
		}

		_iconsole_cmds.put(name, item_new);
	}

	/**
	 * Find the command pointed to by its string
	 * @param name command to be found
	 * @return return Cmdstruct of the found command, or null on failure
	 */
	static IConsoleCmd IConsoleCmdGet(final String name)
	{
		/*IConsoleCmd item;
		//for( IConsoleCmd item : _iconsole_cmds )
		for (item = _iconsole_cmds; item != null; item = item.next) 
			if( item.name.equalsIgnoreCase(name) ) return item;
		 */
		return _iconsole_cmds.get(name);
	}

	/**
	 * Register a an alias for an already existing command in the console
	 * @param name name of the alias that will be used
	 * @param cmd name of the command that 'name' will be alias of
	 */
	static void IConsoleAliasRegister(final String name, final String cmd)
	{
		//String new_alias = new String(name);
		//String cmd_aliased = new String(cmd);
		IConsoleAlias item_new = new IConsoleAlias();

		//item_new.next = null;
		item_new.cmdline = cmd;
		item_new.name = name;

		//IConsoleAddSorted(_iconsole_aliases, item_new, IConsoleAlias, "an alias");
		if( _iconsole_aliases.get(name) != null )
		{
			IConsoleError("an alias with this name already exists; insertion aborted");      
			return;                                                                       
		}

		_iconsole_aliases.put(name, item_new);
	}

	/**
	 * Find the alias pointed to by its string
	 * @param name alias to be found
	 * @return return Aliasstruct of the found alias, or null on failure
	 */
	static IConsoleAlias IConsoleAliasGet(final String name)
	{
		return _iconsole_aliases.get(name);
	}

	/** copy in an argument into the alias stream * /
	static  int IConsoleCopyInParams(String dst, final String src, uint bufpos)
	{
		int len = min(ICON_MAX_STREAMSIZE - bufpos, strlen(src));
		strncpy(dst, src, len);

		return len;
	}*/

	/**
	 * An alias is just another name for a command, or for more commands
	 * Execute it as well.
	 * @param alias is the alias of the command
	 * @param tokencount the number of parameters passed
	 * @param tokens are the parameters given to the original command (0 is the first param)
	 */
	static void IConsoleAliasExec(final IConsoleAlias alias, int tokencount, String ... tokens)
	{
	/*
		final String cmdptr;
		String [] aliases = new String[ICON_MAX_ALIAS_LINES]
		String aliasstream[ICON_MAX_STREAMSIZE];
		int i;
		int a_index, astream_i;

		memset(&aliases, 0, sizeof(aliases));
		memset(&aliasstream, 0, sizeof(aliasstream));

		if (_stdlib_con_developer)
			IConsolePrintF(_icolour_dbg, "condbg: requested command is an alias; parsing...");

		aliases[0] = aliasstream;
		for (cmdptr = alias.cmdline, a_index = 0, astream_i = 0; *cmdptr != '\0'; cmdptr++) {
			if (a_index >= lengthof(aliases) || astream_i >= lengthof(aliasstream)) break;

			switch (*cmdptr) {
			case '\'': // ' will double for "" 
				aliasstream[astream_i++] = '"';
				break;
			case ';': // Cmd seperator, start new command 
				aliasstream[astream_i] = '\0';
				aliases[++a_index] = &aliasstream[++astream_i];
				cmdptr++;
				break;
			case '%': // Some or all parameters 
				cmdptr++;
				switch (*cmdptr) {
				case '+': { // All parameters seperated: "[param 1]" "[param 2]" 
					for (i = 0; i != tokencount; i++) {
						aliasstream[astream_i++] = '"';
						astream_i += IConsoleCopyInParams(&aliasstream[astream_i], tokens[i], astream_i);
						aliasstream[astream_i++] = '"';
						aliasstream[astream_i++] = ' ';
					}
				} break;
				case '!': { // Merge the parameters to one: "[param 1] [param 2] [param 3...]" 
					aliasstream[astream_i++] = '"';
					for (i = 0; i != tokencount; i++) {
						astream_i += IConsoleCopyInParams(&aliasstream[astream_i], tokens[i], astream_i);
						aliasstream[astream_i++] = ' ';
					}
					aliasstream[astream_i++] = '"';

				} break;
				default: { // One specific parameter: %A = [param 1] %B = [param 2] ... 
					int param = *cmdptr - 'A';

					if (param < 0 || param >= tokencount) {
						IConsoleError("too many or wrong amount of parameters passed to alias, aborting");
						IConsolePrintF(_icolour_warn, "Usage of alias '%s': %s", alias.name, alias.cmdline);
						return;
					}

					aliasstream[astream_i++] = '"';
					astream_i += IConsoleCopyInParams(&aliasstream[astream_i], tokens[param], astream_i);
					aliasstream[astream_i++] = '"';
				} break;
				} break;

			default:
				aliasstream[astream_i++] = *cmdptr;
				break;
			}
		}

		for (i = 0; i <= (int)a_index; i++) IConsoleCmdExec(aliases[i]); // execute each alias in turn
	*/
	}

	/**
	 * Special function for adding string-type variables. They in addition
	 * also need a 'size' value saying how long their string buffer is.
	 * @param size the length of the string buffer
	 * For more information see @IConsoleVarRegister()
	 * /
	static void IConsoleVarStringRegister(final String name, void *addr, int size, final String help)
	{
		IConsoleVar var;
		IConsoleVarRegister(name, addr, ICONSOLE_VAR_STRING, help);
		var = IConsoleVarGet(name);
		var.size = size;
	}

	/**
	 * Register a new variable to be used in the console
	 * @param name name of the variable that will be used
	 * @param addr memory location the variable will point to
	 * @param help the help string shown for the variable
	 * @param type the type of the variable (simple atomic) so we know which values it can get
	 */
	//static void IConsoleVarRegister(final String name, Object addr, IConsoleVarTypes type, final String help)
	static void IConsoleVarRegister(final String name, IConsoleVarTypes type, final String help)
	{
		IConsoleVar item_new = new IConsoleVar(); //malloc(sizeof(IConsoleVar));

		item_new.help = help;

		//item_new.next = null;
		item_new.name = name;
		//item_new.addr = addr;
		item_new.proc = null;
		item_new.type = type;

		item_new.hook.access = null;
		item_new.hook.pre = null;
		item_new.hook.post = null;

		//IConsoleAddSorted(_iconsole_vars, item_new, IConsoleVar, "a variable");
		if( _iconsole_vars.get(name) != null )
		{
			IConsoleError("a var with this name already exists; insertion aborted");      
			return;                                                                       
		}

		_iconsole_vars.put(name, item_new);
	}

	/**
	 * Find the variable pointed to by its string
	 * @param name variable to be found
	 * @return return Varstruct of the found variable, or null on failure
	 */
	static IConsoleVar IConsoleVarGet(final String name)
	{
		return _iconsole_vars.get(name);
	}





	/**
	 * Print out the value of the variable when asked
	 */
	static void IConsoleVarPrintGetValue(final IConsoleVar variable)
	{
		String value;
		/* Some variables need really specific handling, handle this in its
		 * callback function */
		if (variable.proc != null) {
			//var.proc.accept(0, null);
			variable.proc.accept();
			return;
		}

		value = variable.IConsoleVarGetStringValue();
		IConsolePrintF(_icolour_warn, "Current value for '%s' is:  %s", variable.name, value);
	}


	/**
	 * Execute a variable command. Without any parameters, print out its value
	 * with parameters it assigns a new value to the variable
	 * @param variable the variable that we will be querying/changing
	 * @param tokencount how many additional parameters have been given to the commandline
	 * @param token the actual parameters the variable was called with
	 */
	static void IConsoleVarExec(final IConsoleVar variable, int tokencount, String ... token)
	{
		String tokenptr = token[0];
		int t_index = tokencount;

		if (_stdlib_con_developer)
			IConsolePrintF(_icolour_dbg, "condbg: requested command is a variable");

		if (tokencount == 0) { /* Just print out value */
			IConsoleVarPrintGetValue(variable);
			return;
		}

		/* Use of assignment sign is not mandatory but supported, so just 'ignore it appropiately' */
		if (tokenptr.equalsIgnoreCase("=")) {
			tokencount--;
			tokenptr = token[1];
		}

		if (tokencount == 1) {
			/* Some variables need really special handling, handle it in their callback procedure */
			if (variable.proc != null) {
				variable.proc.accept(token[t_index - tokencount]); // set the new value
				return;
			}
			/* Strings need special processing. No need to convert the argument to
			 * an integer value, just copy over the argument on a one-by-one basis */
			if (variable.type == IConsoleVarTypes.ICONSOLE_VAR_STRING) {
				variable.IConsoleVarSetStringvalue(token[t_index - tokencount]);
				return;
			} else {
				int [] value = {0};

				if (GetArgumentInteger(value, token[t_index - tokencount])) {
					variable.IConsoleVarSetValue(value[0]);
					return;
				}
			}

			/* Increase or decrease the value by one. This of course can only happen to 'number' types */
			if (tokenptr.equals("++") && variable.type != IConsoleVarTypes.ICONSOLE_VAR_STRING) {
				variable.IConsoleVarSetValue(variable.IConsoleVarGetValue() + 1);
				return;
			}

			if (tokenptr.equals("--") && variable.type != IConsoleVarTypes.ICONSOLE_VAR_STRING) {
				variable.IConsoleVarSetValue(variable.IConsoleVarGetValue() - 1);
				return;
			}
		}

		IConsoleError("invalid variable assignment");
	}

	/**
	 * Add a callback function to the variable. Some variables need
	 * very special processing, which can only be done with custom code
	 * @param name name of the variable the callback function is added to
	 * @param proc the function called
	 */
	static void IConsoleVarProcAdd(final String name, IConsoleCmdProc proc)
	{
		IConsoleVar variable = IConsoleVarGet(name);
		if (variable == null) return;
		variable.proc = proc;
	}

	/**
	 * Execute a given command passed to us. First chop it up into
	 * individual tokens (seperated by spaces), then execute it if possible
	 * @param cmdstr string to be parsed and executed
	 */
	static void IConsoleCmdExec(final String cmdstr)
	{
		IConsoleCmd   cmd    = null;
		IConsoleAlias alias  = null;
		//IConsoleVar   variable    = null;

		//final String cmdptr;
		String [] tokens = new String[ICON_TOKEN_COUNT];
		char [] tokenstream = new char[ICON_MAX_STREAMSIZE];
		int t_index, tstream_i;

		boolean longtoken = false;
		boolean foundtoken = false;

		if (cmdstr.length() == 0 || cmdstr.charAt(0) == '#') return; // comments

		for (int i = 0; i < cmdstr.length(); i++) {
			char c = cmdstr.charAt(i);

			if (!BitOps.IsValidAsciiChar(c)) {
				IConsoleError("command contains malformed characters, aborting");
				IConsolePrintF(_icolour_err, "ERROR: command was: '%s'", cmdstr);
				return;
			}
		}

		if (_stdlib_con_developer)
			IConsolePrintF(_icolour_dbg, "condbg: executing cmdline: '%s'", cmdstr);

		//memset(&tokens, 0, sizeof(tokens));
		//memset(&tokenstream, 0, sizeof(tokenstream));
		Arrays.fill(tokens, null);
		Arrays.fill(tokenstream, (char)0);

		/* 1. Split up commandline into tokens, seperated by spaces, commands
		 * enclosed in "" are taken as one token. We can only go as far as the amount
		 * of characters in our stream or the max amount of tokens we can handle */
		t_index = 0;
		tstream_i = 0;
		for (int i = 0; true; i++) {
			
			if(i >= cmdstr.length())
			{
				if(foundtoken) tokens[t_index++] = new String(tokenstream, 0, tstream_i);
				break;
			}

			if (t_index >= tokens.length || tstream_i >= tokenstream.length) 
				break;
			
			char c = cmdstr.charAt(i);

			switch (c) {
			case ' ': /* Token seperator */
				if (!foundtoken) break;

				if (longtoken) {
					tokenstream[tstream_i++] = c;
				} else {
					//tokenstream[tstream_i] = 0;
					tokens[t_index++] = new String(tokenstream, 0, tstream_i);
					tstream_i = 0;
					foundtoken = false;
				}

				//tstream_i++;
				break;
			case '"': /* Tokens enclosed in "" are one token */
				longtoken = !longtoken;
				break;

			default: /* Normal character */
				tokenstream[tstream_i++] = c;

				if (!foundtoken) {
					//tokens[t_index++] = &tokenstream[tstream_i - 1];
					foundtoken = true;
				}
				break;
			}
		}

		if (_stdlib_con_developer) {
			int i;
			for (i = 0; tokens[i] != null; i++)
				IConsolePrintF(_icolour_dbg, "condbg: token %d is: '%s'", i, tokens[i]);
		}

		if (tokens[0] == null) return; // don't execute empty commands
		/* 2. Determine type of command (cmd, alias or variable) and execute
		 * First try commands, then aliases, and finally variables. Execute
		 * the found action taking into account its hooking code
		 */
		cmd = IConsoleCmdGet(tokens[0]);
		if (cmd != null && cmd.hook != null) {
			if (cmd.hook.IConsoleHookHandle(IConsoleHookTypes.ICONSOLE_HOOK_ACCESS)) {
				cmd.hook.IConsoleHookHandle(IConsoleHookTypes.ICONSOLE_HOOK_PRE_ACTION);
				if (cmd.proc.accept(tokens)) { // index started with 0
					cmd.hook.IConsoleHookHandle(IConsoleHookTypes.ICONSOLE_HOOK_POST_ACTION);
				} else { 
					cmd.proc.accept(); // if command failed, give help
				}
			}
			return;
		}

		t_index--; // ignore the variable-name for comfort for both aliases and variaables
		alias = IConsoleAliasGet(tokens[0]);
		if (alias != null) {
			IConsoleAliasExec(alias, t_index, tokens[1]);
			return;
		}

		IConsoleVar variable = IConsoleVarGet(tokens[0]);
		if (variable != null) {
			if (variable.hook.IConsoleHookHandle(IConsoleHookTypes.ICONSOLE_HOOK_ACCESS))
				IConsoleVarExec(variable, t_index, tokens[1], tokens[2]);

			return;
		}

		IConsoleError("command or variable not found");
	}


	private static void windowProc( Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int i = _iconsole_scroll;
			int max = (w.getHeight() / ICON_LINE_HEIGHT) - 1;
			int delta = 0;
			Gfx.GfxFillRect(w.getLeft(), w.getTop(), w.getWidth(), w.getHeight() - 1, 0);
			while ((i > 0) && (i > _iconsole_scroll - max) && (_iconsole_buffer[i] != null)) {
				Gfx.DoDrawString(_iconsole_buffer[i], 5,
						w.getHeight() - (_iconsole_scroll + 2 - i) * ICON_LINE_HEIGHT, _iconsole_cbuffer[i]);
				i--;
			}
			/* If the text is longer than the window, don't show the starting ']' */
			delta = w.getWidth() - 10 - _iconsole_cmdline.getWidth() - ICON_RIGHT_BORDERWIDTH;
			if (delta > 0) {
				Gfx.DoDrawString("]", 5, w.getHeight() - ICON_LINE_HEIGHT, _icolour_cmd);
				delta = 0;
			}

			Gfx.DoDrawString(_iconsole_cmdline.getString(), 10 + delta, w.getHeight() - ICON_LINE_HEIGHT, _icolour_cmd);

			if (_iconsole_cmdline.isCaret())
				Gfx.DoDrawString("_", 10 + delta + _iconsole_cmdline.getCaretXOffs(), w.getHeight() - ICON_LINE_HEIGHT, 12);
			break;
		}
		case WE_MOUSELOOP:
			if (_iconsole_cmdline.HandleCaret())
				w.SetWindowDirty();
			break;
		case WE_DESTROY:
			_iconsole_win = null;
			_iconsole_mode = IConsoleModes.ICONSOLE_CLOSED;
			break;
		case WE_KEYPRESS:
			e.cont = false;
			switch (e.keycode) {
			case Window.WKC_UP:
				IConsoleHistoryNavigate(+1);
				w.SetWindowDirty();
				break;
			case Window.WKC_DOWN:
				IConsoleHistoryNavigate(-1);
				w.SetWindowDirty();
				break;
			case Window.WKC_SHIFT | Window.WKC_PAGEUP:
				if (_iconsole_scroll - (w.getHeight() / ICON_LINE_HEIGHT) - 1 < 0)
					_iconsole_scroll = 0;
				else
					_iconsole_scroll -= (w.getHeight() / ICON_LINE_HEIGHT) - 1;
				w.SetWindowDirty();
				break;
			case Window.WKC_SHIFT | Window.WKC_PAGEDOWN:
				if (_iconsole_scroll + (w.getHeight() / ICON_LINE_HEIGHT) - 1 > ICON_BUFFER)
					_iconsole_scroll = ICON_BUFFER;
				else
					_iconsole_scroll += (w.getHeight() / ICON_LINE_HEIGHT) - 1;
				w.SetWindowDirty();
				break;
			case Window.WKC_SHIFT | Window.WKC_UP:
				if (_iconsole_scroll <= 0)
					_iconsole_scroll = 0;
				else
					--_iconsole_scroll;
				w.SetWindowDirty();
				break;
			case Window.WKC_SHIFT | Window.WKC_DOWN:
				if (_iconsole_scroll >= ICON_BUFFER)
					_iconsole_scroll = ICON_BUFFER;
				else
					++_iconsole_scroll;
				w.SetWindowDirty();
				break;
			
			case '`':
			case Window.WKC_BACKQUOTE:
			case Window.WKC_ESC:
				IConsoleSwitch();
				break;
			case Window.WKC_RETURN: case Window.WKC_NUM_ENTER:
				IConsolePrintF(_icolour_cmd, "] %s", _iconsole_cmdline.getString());
				IConsoleHistoryAdd(_iconsole_cmdline.getString());

				IConsoleCmdExec(_iconsole_cmdline.getString());
				IConsoleClearCommand();
				break;
			case Window.WKC_CTRL | Window.WKC_RETURN:
				_iconsole_mode = (_iconsole_mode == IConsoleModes.ICONSOLE_FULL) ? IConsoleModes.ICONSOLE_OPENED : IConsoleModes.ICONSOLE_FULL;
				IConsoleResize();
				Hal.MarkWholeScreenDirty();
				break;
			case (Window.WKC_CTRL | 'V'):
				if (Hal.InsertTextBufferClipboard(_iconsole_cmdline)) {
					IConsoleResetHistoryPos();
					w.SetWindowDirty();
				}
			break;
			case (Window.WKC_CTRL | 'L'):
				IConsoleCmdExec("clear");
			break;
			case (Window.WKC_CTRL | 'U'):
				_iconsole_cmdline.DeleteTextBufferAll();
			w.SetWindowDirty();
			break;
			case Window.WKC_BACKSPACE: case Window.WKC_DELETE:
				if (_iconsole_cmdline.DeleteTextBufferChar(e.keycode)) {
					IConsoleResetHistoryPos();
					w.SetWindowDirty();
				}
				break;
			case Window.WKC_LEFT: case Window.WKC_RIGHT: case Window.WKC_END: case Window.WKC_HOME:
				if (_iconsole_cmdline.MoveTextBufferPos(e.keycode)) {
					IConsoleResetHistoryPos();
					w.SetWindowDirty();
				}
				break;
			default:
				if (BitOps.IsValidAsciiChar(e.ascii)) {
					_iconsole_scroll = ICON_BUFFER;
					_iconsole_cmdline.InsertTextBufferChar((char)e.ascii);
					IConsoleResetHistoryPos();
					w.SetWindowDirty();
				} else
					e.cont = true;
				break;
			}
			break;
			
		default:
			break;
		}

	}


}












