package game;

import java.io.FileWriter;

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



	public static final int ICON_BUFFER  = 79;
	public static final int ICON_HISTORY_SIZE = 20;
	public static final int ICON_LINE_HEIGHT = 12;
	public static final int ICON_RIGHT_BORDERWIDTH = 10;
	public static final int ICON_BOTTOM_BORDERWIDTH = 12;
	public static final int ICON_MAX_ALIAS_LINES = 40;
	public static final int ICON_TOKEN_COUNT = 20;

	// ** main console ** //
	static ConsoleWindow _iconsole_win; // Pointer to console window
	static boolean _iconsole_inited;
	static String[]_iconsole_buffer = new String[ICON_BUFFER + 1];
	static int[] _iconsole_cbuffer = new int[ICON_BUFFER + 1];
	static Textbuf _iconsole_cmdline = new Textbuf();
	static byte _iconsole_scroll;

	// ** stdlib ** //
	static byte _stdlib_developer = 1;
	static boolean _stdlib_con_developer = false;
	static FileWriter _iconsole_output_file;

	// ** main console cmd buffer
	static String[] _iconsole_history = new String[ICON_HISTORY_SIZE];
	static int _iconsole_historypos;

	/* *************** */
	/*  end of header  */
	/* *************** */

	static void IConsoleClearCommand()
	{
		_iconsole_cmdline.buf = new String();
		_iconsole_cmdline.length = 0;
		_iconsole_cmdline.width = 0;
		_iconsole_cmdline.caretpos = 0;
		_iconsole_cmdline.caretxoffs = 0;
		SetWindowDirty(_iconsole_win);
	}

	static  void IConsoleResetHistoryPos() {_iconsole_historypos = ICON_HISTORY_SIZE - 1;}


	static final Widget _iconsole_window_widgets[] = {
			{WIDGETS_END}
	};

	static final WindowDesc _iconsole_window_desc = {
			0, 0, 2, 2,
			WC_CONSOLE, 0,
			WDF_STD_TOOLTIPS | WDF_DEF_WIDGET | WDF_UNCLICK_BUTTONS,
			_iconsole_window_widgets,
			null,
	};

	static void IConsoleInit()
	{
		extern final char _openttd_revision[];
		_iconsole_output_file = null;
		_icolour_def  =  1;
		_icolour_err  =  3;
		_icolour_warn = 13;
		_icolour_dbg  =  5;
		_icolour_cmd  =  2;
		_iconsole_scroll = ICON_BUFFER;
		_iconsole_historypos = ICON_HISTORY_SIZE - 1;
		_iconsole_inited = true;
		_iconsole_mode = ICONSOLE_CLOSED;
		_iconsole_win = null;

		/*
		#ifdef ENABLE_NETWORK // * Initialize network only variables 
		_redirect_console_to_client = 0;
		#endif
		*/

		memset(_iconsole_history, 0, sizeof(_iconsole_history));
		memset(_iconsole_buffer, 0, sizeof(_iconsole_buffer));
		memset(_iconsole_cbuffer, 0, sizeof(_iconsole_cbuffer));
		_iconsole_cmdline.buf = calloc(ICON_CMDLN_SIZE, sizeof(*_iconsole_cmdline.buf)); // create buffer and zero it
		_iconsole_cmdline.maxlength = ICON_CMDLN_SIZE - 1;

		IConsolePrintF(13, "OpenTTD Game Console Revision 7 - %s", _openttd_revision);
		IConsolePrint(12,  "------------------------------------");
		IConsolePrint(12,  "use \"help\" for more information");
		IConsolePrint(12,  "");
		IConsoleStdLibRegister();
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
			_iconsole_output_file.write(string);
			_iconsole_output_file.write("\n");
		}
	}

	static boolean CloseConsoleLogIfActive()
	{
		if (_iconsole_output_file != null) 
		{
			IConsolePrintF(_icolour_def, "file output complete");
			_iconsole_output_file.close();
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

	static void IConsoleResize()
	{
		_iconsole_win = FindWindowById(WC_CONSOLE, 0);

		switch (_iconsole_mode) {
		case ICONSOLE_OPENED:
			_iconsole_win.height = _screen.height / 3;
			_iconsole_win.width = _screen.width;
			break;
		case ICONSOLE_FULL:
			_iconsole_win.height = _screen.height - ICON_BOTTOM_BORDERWIDTH;
			_iconsole_win.width = _screen.width;
			break;
		default: break;
		}

		MarkWholeScreenDirty();
	}

	static void IConsoleSwitch()
	{
		switch (_iconsole_mode) {
		case ICONSOLE_CLOSED:
			_iconsole_win = AllocateWindowDesc(&_iconsole_window_desc);
			_iconsole_win.height = _screen.height / 3;
			_iconsole_win.width = _screen.width;
			_iconsole_mode = ICONSOLE_OPENED;
			SETBIT(_no_scroll, SCROLL_CON); // override cursor arrows; the gamefield will not scroll
			break;
		case ICONSOLE_OPENED: case ICONSOLE_FULL:
			DeleteWindowById(WC_CONSOLE, 0);
			_iconsole_win = null;
			_iconsole_mode = ICONSOLE_CLOSED;
			CLRBIT(_no_scroll, SCROLL_CON);
			break;
		}

		MarkWholeScreenDirty();
	}

	static void IConsoleClose() {if (_iconsole_mode == ICONSOLE_OPENED) IConsoleSwitch();}
	static void IConsoleOpen()  {if (_iconsole_mode == ICONSOLE_CLOSED) IConsoleSwitch();}

	/**
	 * Add the entered line into the history so you can look it back
	 * scroll, etc. Put it to the beginning as it is the latest text
	 * @param cmd Text to be entered into the 'history'
	 */
	static void IConsoleHistoryAdd(final String cmd)
	{
		//free(_iconsole_history[ICON_HISTORY_SIZE - 1]);

		memmove(&_iconsole_history[1], &_iconsole_history[0], sizeof(_iconsole_history[0]) * (ICON_HISTORY_SIZE - 1));
		_iconsole_history[0] = new String(cmd);
		IConsoleResetHistoryPos();
	}

	/**
	 * Navigate Up/Down in the history of typed commands
	 * @param direction Go further back in history (+1), go to recently typed commands (-1)
	 */
	static void IConsoleHistoryNavigate(char direction)
	{
		int i = _iconsole_historypos + direction;

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
		assert(_iconsole_history[i] != null && IS_INT_INSIDE(i, 0, ICON_HISTORY_SIZE));
		ttd_strlcpy(_iconsole_cmdline.buf, _iconsole_history[i], _iconsole_cmdline.maxlength);
		UpdateTextBufferSize(_iconsole_cmdline);
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

		if (_network_dedicated) {
			printf("%s\n", string);
			IConsoleWriteToLogFile(string);
			return;
		}

		if (!_iconsole_inited) return;

		/* move up all the strings in the buffer one place and do the same for colour
		 * to accomodate for the new command/message */
		//free(_iconsole_buffer[0]);
		memmove(&_iconsole_buffer[0], &_iconsole_buffer[1], sizeof(_iconsole_buffer[0]) * ICON_BUFFER);
		_iconsole_buffer[ICON_BUFFER] = strdup(string);

		{ // filter out unprintable characters
			char *i;
			for (i = _iconsole_buffer[ICON_BUFFER]; *i != '\0'; i++)
				if (!IsValidAsciiChar((byte)*i)) *i = ' ';
		}

		memmove(&_iconsole_cbuffer[0], &_iconsole_cbuffer[1], sizeof(_iconsole_cbuffer[0]) * ICON_BUFFER);
		_iconsole_cbuffer[ICON_BUFFER] = color_code;

		IConsoleWriteToLogFile(string);

		if(_iconsole_win != null) SetWindowDirty(_iconsole_win);
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
	 * @debug() in debug.c. You need at least a level 2 (developer) for debugging
	 * messages to show up
	 */
	static void IConsoleDebug(final String string)
	{
		if (_stdlib_developer > 1)
			IConsolePrintF(_icolour_dbg, "dbg: %s", string);
	}

	/**
	 * It is possible to print warnings to the console. These are mostly
	 * errors or mishaps, but non-fatal. You need at least a level 1 (developer) for
	 * debugging messages to show up
	 */
	static void IConsoleWarning(final String  string)
	{
		if (_stdlib_developer > 0)
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
	 * @param *value the variable a successfull conversion will be put in
	 * @param *arg the string to be converted
	 * @return Return true on success or false on failure
	 */
	static boolean GetArgumentInteger(int *value, final String arg)
	{
		char *endptr;

		if (strcmp(arg, "on") == 0 || strcmp(arg, "true") == 0) {
			*value = 1;
			return true;
		}
		if (strcmp(arg, "off") == 0 || strcmp(arg, "false") == 0) {
			*value = 0;
			return true;
		}

		*value = strtoul(arg, &endptr, 0);
		return arg != endptr;
	}

	// * ************************* * //
	// * hooking code              * //
	// * ************************* * //
	/**
	 * General internal hooking code that is the same for both commands and variables
	 * @param hooks @IConsoleHooks structure that will be set according to
	 * @param type type access trigger
	 * @param proc function called when the hook criteria is met
	 */
	static private void IConsoleHookAdd(IConsoleHooks *hooks, IConsoleHookTypes type, IConsoleHook *proc)
	{
		if (hooks == null || proc == null) return;

		switch (type) {
		case ICONSOLE_HOOK_ACCESS:
			hooks.access = proc;
			break;
		case ICONSOLE_HOOK_PRE_ACTION:
			hooks.pre = proc;
			break;
		case ICONSOLE_HOOK_POST_ACTION:
			hooks.post = proc;
			break;
		default: NOT_REACHED();
		}
	}

	/**
	 * Handle any special hook triggers. If the hook type is met check if
	 * there is a function associated with that and if so, execute it
	 * @param hooks @IConsoleHooks structure that will be checked
	 * @param type type of hook, trigger that needs to be activated
	 * @return true on a successfull execution of the hook command or if there
	 * is no hook/trigger present at all. False otherwise
	 */
	static private boolean IConsoleHookHandle(final IConsoleHooks *hooks, IConsoleHookTypes type)
	{
		IConsoleHook *proc = null;
		if (hooks == null) return false;

		switch (type) {
		case ICONSOLE_HOOK_ACCESS:
			proc = hooks.access;
			break;
		case ICONSOLE_HOOK_PRE_ACTION:
			proc = hooks.pre;
			break;
		case ICONSOLE_HOOK_POST_ACTION:
			proc = hooks.post;
			break;
		default: NOT_REACHED();
		}

		return (proc == null) ? true : proc();
	}

	/**
	 * Add a hook to a command that will be triggered at certain points
	 * @param name name of the command that the hook is added to
	 * @param type type of hook that is added (ACCESS, BEFORE and AFTER change)
	 * @param proc function called when the hook criteria is met
	 */
	static void IConsoleCmdHookAdd(final String name, IConsoleHookTypes type, IConsoleHook *proc)
	{
		IConsoleCmd *cmd = IConsoleCmdGet(name);
		if (cmd == null) return;
		IConsoleHookAdd(&cmd.hook, type, proc);
	}

	/**
	 * Add a hook to a variable that will be triggered at certain points
	 * @param name name of the variable that the hook is added to
	 * @param type type of hook that is added (ACCESS, BEFORE and AFTER change)
	 * @param proc function called when the hook criteria is met
	 */
	static void IConsoleVarHookAdd(final String name, IConsoleHookTypes type, IConsoleHook *proc)
	{
		IConsoleVar *var = IConsoleVarGet(name);
		if (var == null) return;
		IConsoleHookAdd(&var.hook, type, proc);
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
	static void IConsoleCmdRegister(final String name, IConsoleCmdProc *proc)
	{
		String new_cmd = strdup(name);
		IConsoleCmd item_new = new IConsoleCmd();

		item_new.next = null;
		item_new.proc = proc;
		item_new.name = new_cmd;

		item_new.hook.access = null;
		item_new.hook.pre = null;
		item_new.hook.post = null;

		IConsoleAddSorted(_iconsole_cmds, item_new, IConsoleCmd, "a command");
	}

	/**
	 * Find the command pointed to by its string
	 * @param name command to be found
	 * @return return Cmdstruct of the found command, or null on failure
	 */
	static IConsoleCmd IConsoleCmdGet(final String name)
	{
		IConsoleCmd item;
		//for( IConsoleCmd item : _iconsole_cmds )
		for (item = _iconsole_cmds; item != null; item = item.next) 
			if( item.name.equalsIgnoreCase(name) ) return item;

		return null;
	}

	/**
	 * Register a an alias for an already existing command in the console
	 * @param name name of the alias that will be used
	 * @param cmd name of the command that 'name' will be alias of
	 */
	static void IConsoleAliasRegister(final String name, final String cmd)
	{
		String new_alias = new String(name);
		String cmd_aliased = new String(cmd);
		IConsoleAlias item_new = new IConsoleAlias();

		item_new.next = null;
		item_new.cmdline = cmd_aliased;
		item_new.name = new_alias;

		IConsoleAddSorted(_iconsole_aliases, item_new, IConsoleAlias, "an alias");
	}

	/**
	 * Find the alias pointed to by its string
	 * @param name alias to be found
	 * @return return Aliasstruct of the found alias, or null on failure
	 */
	static IConsoleAlias IConsoleAliasGet(final String name)
	{
		IConsoleAlias item;

		for (item = _iconsole_aliases; item != null; item = item.next) {
			if( item.name.equals(name) ) return item;
		}

		return null;
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
	 * @param *alias is the alias of the command
	 * @param tokencount the number of parameters passed
	 * @param *tokens are the parameters given to the original command (0 is the first param)
	 */
	static void IConsoleAliasExec(final IConsoleAlias alias, byte tokencount, String tokens[])
	{
		final String cmdptr;
		String aliases[ICON_MAX_ALIAS_LINES], aliasstream[ICON_MAX_STREAMSIZE];
		int i;
		uint a_index, astream_i;

		memset(&aliases, 0, sizeof(aliases));
		memset(&aliasstream, 0, sizeof(aliasstream));

		if (_stdlib_con_developer)
			IConsolePrintF(_icolour_dbg, "condbg: requested command is an alias; parsing...");

		aliases[0] = aliasstream;
		for (cmdptr = alias.cmdline, a_index = 0, astream_i = 0; *cmdptr != '\0'; cmdptr++) {
			if (a_index >= lengthof(aliases) || astream_i >= lengthof(aliasstream)) break;

			switch (*cmdptr) {
			case '\'': /* ' will double for "" */
				aliasstream[astream_i++] = '"';
				break;
			case ';': /* Cmd seperator, start new command */
				aliasstream[astream_i] = '\0';
				aliases[++a_index] = &aliasstream[++astream_i];
				cmdptr++;
				break;
			case '%': /* Some or all parameters */
				cmdptr++;
				switch (*cmdptr) {
				case '+': { /* All parameters seperated: "[param 1]" "[param 2]" */
					for (i = 0; i != tokencount; i++) {
						aliasstream[astream_i++] = '"';
						astream_i += IConsoleCopyInParams(&aliasstream[astream_i], tokens[i], astream_i);
						aliasstream[astream_i++] = '"';
						aliasstream[astream_i++] = ' ';
					}
				} break;
				case '!': { /* Merge the parameters to one: "[param 1] [param 2] [param 3...]" */
					aliasstream[astream_i++] = '"';
					for (i = 0; i != tokencount; i++) {
						astream_i += IConsoleCopyInParams(&aliasstream[astream_i], tokens[i], astream_i);
						aliasstream[astream_i++] = ' ';
					}
					aliasstream[astream_i++] = '"';

				} break;
				default: { /* One specific parameter: %A = [param 1] %B = [param 2] ... */
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
	}

	/**
	 * Special function for adding string-type variables. They in addition
	 * also need a 'size' value saying how long their string buffer is.
	 * @param size the length of the string buffer
	 * For more information see @IConsoleVarRegister()
	 */
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
	static void IConsoleVarRegister(final String name, void *addr, IConsoleVarTypes type, final String help)
	{
		String new_cmd = strdup(name);
		IConsoleVar item_new = malloc(sizeof(IConsoleVar));

		item_new.help = (help != null) ? strdup(help) : null;

		item_new.next = null;
		item_new.name = new_cmd;
		item_new.addr = addr;
		item_new.proc = null;
		item_new.type = type;

		item_new.hook.access = null;
		item_new.hook.pre = null;
		item_new.hook.post = null;

		IConsoleAddSorted(_iconsole_vars, item_new, IConsoleVar, "a variable");
	}

	/**
	 * Find the variable pointed to by its string
	 * @param name variable to be found
	 * @return return Varstruct of the found variable, or null on failure
	 */
	static IConsoleVar IConsoleVarGet(final String name)
	{
		IConsoleVar item;
		for (item = _iconsole_vars; item != null; item = item.next) {
			if( item.name.equals(name) ) return item;
		}

		return null;
	}

	/**
	 * Set a new value to a console variable
	 * @param *var the variable being set/changed
	 * @param value the new value given to the variable, cast properly
	 */
	static private void IConsoleVarSetValue(final IConsoleVar var, int value)
	{
		IConsoleHookHandle(&var.hook, ICONSOLE_HOOK_PRE_ACTION);
		switch (var.type) {
		case ICONSOLE_VAR_BOOLEAN:
			*(boolean*)var.addr = (value != 0);
			break;
		case ICONSOLE_VAR_BYTE:
			*(byte*)var.addr = (byte)value;
			break;
		case ICONSOLE_VAR_UINT16:
			*(int*)var.addr = (int)value;
			break;
		case ICONSOLE_VAR_INT16:
			*(int16*)var.addr = (int16)value;
			break;
		case ICONSOLE_VAR_UINT32:
			*(int*)var.addr = (int)value;
			break;
		case ICONSOLE_VAR_INT32:
			*(int32*)var.addr = (int32)value;
			break;
		default: NOT_REACHED();
		}

		IConsoleHookHandle(&var.hook, ICONSOLE_HOOK_POST_ACTION);
		IConsoleVarPrintSetValue(var);
	}

	/**
	 * Set a new value to a string-type variable. Basically this
	 * means to copy the new value over to the container.
	 * @param *var the variable in question
	 * @param *value the new value
	 */
	static private void IConsoleVarSetStringvalue(final IConsoleVar var, String value)
	{
		if (var.type != ICONSOLE_VAR_STRING || var.addr == null) return;

		IConsoleHookHandle(&var.hook, ICONSOLE_HOOK_PRE_ACTION);
		ttd_strlcpy((String )var.addr, (String )value, var.size);
		IConsoleHookHandle(&var.hook, ICONSOLE_HOOK_POST_ACTION);
		IConsoleVarPrintSetValue(var); // print out the new value, giving feedback
		return;
	}

	/**
	 * Query the current value of a variable and return it
	 * @param *var the variable queried
	 * @return current value of the variable
	 */
	static private int IConsoleVarGetValue(final IConsoleVar var)
	{
		int result = 0;

		switch (var.type) {
		case ICONSOLE_VAR_BOOLEAN:
			result = *(boolean*)var.addr;
			break;
		case ICONSOLE_VAR_BYTE:
			result = *(byte*)var.addr;
			break;
		case ICONSOLE_VAR_UINT16:
			result = *(int*)var.addr;
			break;
		case ICONSOLE_VAR_INT16:
			result = *(int16*)var.addr;
			break;
		case ICONSOLE_VAR_UINT32:
			result = *(int*)var.addr;
			break;
		case ICONSOLE_VAR_INT32:
			result = *(int32*)var.addr;
			break;
		default: NOT_REACHED();
		}
		return result;
	}

	/**
	 * Get the value of the variable and put it into a printable
	 * string form so we can use it for printing
	 */
	static private String IConsoleVarGetStringValue(final IConsoleVar var)
	{
		static char tempres[50];
		String value = tempres;

		switch (var.type) {
		case ICONSOLE_VAR_BOOLEAN:
			snprintf(tempres, sizeof(tempres), "%s", (*(boolean*)var.addr) ? "on" : "off");
			break;
		case ICONSOLE_VAR_BYTE:
			snprintf(tempres, sizeof(tempres), "%u", *(byte*)var.addr);
			break;
		case ICONSOLE_VAR_UINT16:
			snprintf(tempres, sizeof(tempres), "%u", *(int*)var.addr);
			break;
		case ICONSOLE_VAR_UINT32:
			snprintf(tempres, sizeof(tempres), "%u",  *(int*)var.addr);
			break;
		case ICONSOLE_VAR_INT16:
			snprintf(tempres, sizeof(tempres), "%i", *(int16*)var.addr);
			break;
		case ICONSOLE_VAR_INT32:
			snprintf(tempres, sizeof(tempres), "%i",  *(int32*)var.addr);
			break;
		case ICONSOLE_VAR_STRING:
			value = (String )var.addr;
			break;
		default: NOT_REACHED();
		}

		return value;
	}

	/**
	 * Print out the value of the variable when asked
	 */
	static void IConsoleVarPrintGetValue(final IConsoleVar var)
	{
		String value;
		/* Some variables need really specific handling, handle this in its
		 * callback function */
		if (var.proc != null) {
			var.proc(0, null);
			return;
		}

		value = IConsoleVarGetStringValue(var);
		IConsolePrintF(_icolour_warn, "Current value for '%s' is:  %s", var.name, value);
	}

	/**
	 * Print out the value of the variable after it has been assigned
	 * a new value, thus giving us feedback on the action
	 */
	static void IConsoleVarPrintSetValue(final IConsoleVar var)
	{
		String value = IConsoleVarGetStringValue(var);
		IConsolePrintF(_icolour_warn, "'%s' changed to:  %s", var.name, value);
	}

	/**
	 * Execute a variable command. Without any parameters, print out its value
	 * with parameters it assigns a new value to the variable
	 * @param *var the variable that we will be querying/changing
	 * @param tokencount how many additional parameters have been given to the commandline
	 * @param *token the actual parameters the variable was called with
	 */
	static void IConsoleVarExec(final IConsoleVar var, byte tokencount, String token[])
	{
		final String tokenptr = token[0];
		byte t_index = tokencount;
		int value;

		if (_stdlib_con_developer)
			IConsolePrintF(_icolour_dbg, "condbg: requested command is a variable");

		if (tokencount == 0) { /* Just print out value */
			IConsoleVarPrintGetValue(var);
			return;
		}

		/* Use of assignment sign is not mandatory but supported, so just 'ignore it appropiately' */
		if (strcmp(tokenptr, "=") == 0) tokencount--;

		if (tokencount == 1) {
			/* Some variables need really special handling, handle it in their callback procedure */
			if (var.proc != null) {
				var.proc(tokencount, &token[t_index - tokencount]); // set the new value
				return;
			}
			/* Strings need special processing. No need to convert the argument to
			 * an integer value, just copy over the argument on a one-by-one basis */
			if (var.type == ICONSOLE_VAR_STRING) {
				IConsoleVarSetStringvalue(var, token[t_index - tokencount]);
				return;
			} else if (GetArgumentInteger(&value, token[t_index - tokencount])) {
				IConsoleVarSetValue(var, value);
				return;
			}

			/* Increase or decrease the value by one. This of course can only happen to 'number' types */
			if (strcmp(tokenptr, "++") == 0 && var.type != ICONSOLE_VAR_STRING) {
				IConsoleVarSetValue(var, IConsoleVarGetValue(var) + 1);
				return;
			}

			if (strcmp(tokenptr, "--") == 0 && var.type != ICONSOLE_VAR_STRING) {
				IConsoleVarSetValue(var, IConsoleVarGetValue(var) - 1);
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
		IConsoleVar var = IConsoleVarGet(name);
		if (var == null) return;
		var.proc = proc;
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
		IConsoleVar   var    = null;

		final String cmdptr;
		String tokens[ICON_TOKEN_COUNT], tokenstream[ICON_MAX_STREAMSIZE];
		uint t_index, tstream_i;

		boolean longtoken = false;
		boolean foundtoken = false;

		if (cmdstr[0] == '#') return; // comments

		for (cmdptr = cmdstr; *cmdptr != '\0'; cmdptr++) {
			if (!IsValidAsciiChar(*cmdptr)) {
				IConsoleError("command contains malformed characters, aborting");
				IConsolePrintF(_icolour_err, "ERROR: command was: '%s'", cmdstr);
				return;
			}
		}

		if (_stdlib_con_developer)
			IConsolePrintF(_icolour_dbg, "condbg: executing cmdline: '%s'", cmdstr);

		memset(&tokens, 0, sizeof(tokens));
		memset(&tokenstream, 0, sizeof(tokenstream));

		/* 1. Split up commandline into tokens, seperated by spaces, commands
		 * enclosed in "" are taken as one token. We can only go as far as the amount
		 * of characters in our stream or the max amount of tokens we can handle */
		for (cmdptr = cmdstr, t_index = 0, tstream_i = 0; *cmdptr != '\0'; cmdptr++) {
			if (t_index >= lengthof(tokens) || tstream_i >= lengthof(tokenstream)) break;

			switch (*cmdptr) {
			case ' ': /* Token seperator */
				if (!foundtoken) break;

				if (longtoken) {
					tokenstream[tstream_i] = *cmdptr;
				} else {
					tokenstream[tstream_i] = '\0';
					foundtoken = false;
				}

				tstream_i++;
				break;
			case '"': /* Tokens enclosed in "" are one token */
				longtoken = !longtoken;
				break;
			default: /* Normal character */
				tokenstream[tstream_i++] = *cmdptr;

				if (!foundtoken) {
					tokens[t_index++] = &tokenstream[tstream_i - 1];
					foundtoken = true;
				}
				break;
			}
		}

		if (_stdlib_con_developer) {
			uint i;
			for (i = 0; tokens[i] != null; i++)
				IConsolePrintF(_icolour_dbg, "condbg: token %d is: '%s'", i, tokens[i]);
		}

		if (tokens[0] == '\0') return; // don't execute empty commands
		/* 2. Determine type of command (cmd, alias or variable) and execute
		 * First try commands, then aliases, and finally variables. Execute
		 * the found action taking into account its hooking code
		 */
		cmd = IConsoleCmdGet(tokens[0]);
		if (cmd != null) {
			if (IConsoleHookHandle(&cmd.hook, ICONSOLE_HOOK_ACCESS)) {
				IConsoleHookHandle(&cmd.hook, ICONSOLE_HOOK_PRE_ACTION);
				if (cmd.proc(t_index, tokens)) { // index started with 0
					IConsoleHookHandle(&cmd.hook, ICONSOLE_HOOK_POST_ACTION);
				} else cmd.proc(0, null); // if command failed, give help
			}
			return;
		}

		t_index--; // ignore the variable-name for comfort for both aliases and variaables
		alias = IConsoleAliasGet(tokens[0]);
		if (alias != null) {
			IConsoleAliasExec(alias, t_index, &tokens[1]);
			return;
		}

		var = IConsoleVarGet(tokens[0]);
		if (var != null) {
			if (IConsoleHookHandle(&var.hook, ICONSOLE_HOOK_ACCESS))
				IConsoleVarExec(var, t_index, &tokens[1]);

			return;
		}

		IConsoleError("command or variable not found");
	}


}








// --------------------------------------------
// More classes
// --------------------------------------------


class ConsoleWindow extends Window
{
	void WindowProc( WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			int i = _iconsole_scroll;
			int max = (w.height / ICON_LINE_HEIGHT) - 1;
			int delta = 0;
			GfxFillRect(w.left, w.top, w.width, w.height - 1, 0);
			while ((i > 0) && (i > _iconsole_scroll - max) && (_iconsole_buffer[i] != null)) {
				DoDrawString(_iconsole_buffer[i], 5,
						w.height - (_iconsole_scroll + 2 - i) * ICON_LINE_HEIGHT, _iconsole_cbuffer[i]);
				i--;
			}
			/* If the text is longer than the window, don't show the starting ']' */
			delta = w.width - 10 - _iconsole_cmdline.width - ICON_RIGHT_BORDERWIDTH;
			if (delta > 0) {
				DoDrawString("]", 5, w.height - ICON_LINE_HEIGHT, _icolour_cmd);
				delta = 0;
			}

			DoDrawString(_iconsole_cmdline.buf, 10 + delta, w.height - ICON_LINE_HEIGHT, _icolour_cmd);

			if (_iconsole_cmdline.caret)
				DoDrawString("_", 10 + delta + _iconsole_cmdline.caretxoffs, w.height - ICON_LINE_HEIGHT, 12);
			break;
		}
		case WE_MOUSELOOP:
			if (HandleCaret(&_iconsole_cmdline))
				SetWindowDirty(w);
			break;
		case WE_DESTROY:
			_iconsole_win = null;
			_iconsole_mode = ICONSOLE_CLOSED;
			break;
		case WE_KEYPRESS:
			e.keypress.cont = false;
			switch (e.keypress.keycode) {
			case WKC_UP:
				IConsoleHistoryNavigate(+1);
				SetWindowDirty(w);
				break;
			case WKC_DOWN:
				IConsoleHistoryNavigate(-1);
				SetWindowDirty(w);
				break;
			case WKC_SHIFT | WKC_PAGEUP:
				if (_iconsole_scroll - (w.height / ICON_LINE_HEIGHT) - 1 < 0)
					_iconsole_scroll = 0;
				else
					_iconsole_scroll -= (w.height / ICON_LINE_HEIGHT) - 1;
				SetWindowDirty(w);
				break;
			case WKC_SHIFT | WKC_PAGEDOWN:
				if (_iconsole_scroll + (w.height / ICON_LINE_HEIGHT) - 1 > ICON_BUFFER)
					_iconsole_scroll = ICON_BUFFER;
				else
					_iconsole_scroll += (w.height / ICON_LINE_HEIGHT) - 1;
				SetWindowDirty(w);
				break;
			case WKC_SHIFT | WKC_UP:
				if (_iconsole_scroll <= 0)
					_iconsole_scroll = 0;
				else
					--_iconsole_scroll;
				SetWindowDirty(w);
				break;
			case WKC_SHIFT | WKC_DOWN:
				if (_iconsole_scroll >= ICON_BUFFER)
					_iconsole_scroll = ICON_BUFFER;
				else
					++_iconsole_scroll;
				SetWindowDirty(w);
				break;
			case WKC_BACKQUOTE:
				IConsoleSwitch();
				break;
			case WKC_RETURN: case WKC_NUM_ENTER:
				IConsolePrintF(_icolour_cmd, "] %s", _iconsole_cmdline.buf);
				IConsoleHistoryAdd(_iconsole_cmdline.buf);

				IConsoleCmdExec(_iconsole_cmdline.buf);
				IConsoleClearCommand();
				break;
			case WKC_CTRL | WKC_RETURN:
				_iconsole_mode = (_iconsole_mode == ICONSOLE_FULL) ? ICONSOLE_OPENED : ICONSOLE_FULL;
				IConsoleResize();
				MarkWholeScreenDirty();
				break;
			case (WKC_CTRL | 'V'):
				if (InsertTextBufferClipboard(&_iconsole_cmdline)) {
					IConsoleResetHistoryPos();
					SetWindowDirty(w);
				}
			break;
			case (WKC_CTRL | 'L'):
				IConsoleCmdExec("clear");
			break;
			case (WKC_CTRL | 'U'):
				DeleteTextBufferAll(&_iconsole_cmdline);
			SetWindowDirty(w);
			break;
			case WKC_BACKSPACE: case WKC_DELETE:
				if (DeleteTextBufferChar(&_iconsole_cmdline, e.keypress.keycode)) {
					IConsoleResetHistoryPos();
					SetWindowDirty(w);
				}
				break;
			case WKC_LEFT: case WKC_RIGHT: case WKC_END: case WKC_HOME:
				if (MoveTextBufferPos(&_iconsole_cmdline, e.keypress.keycode)) {
					IConsoleResetHistoryPos();
					SetWindowDirty(w);
				}
				break;
			default:
				if (IsValidAsciiChar(e.keypress.ascii)) {
					_iconsole_scroll = ICON_BUFFER;
					InsertTextBufferChar(&_iconsole_cmdline, e.keypress.ascii);
					IConsoleResetHistoryPos();
					SetWindowDirty(w);
				} else
					e.keypress.cont = true;
					break;
			}
		}

	}
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
//typedef boolean IConsoleHook();
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
//typedef boolean (IConsoleCmdProc)(byte argc, char *argv[]);

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


