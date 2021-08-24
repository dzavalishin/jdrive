package game.console;

/** 
 * <h2>Variables</h2>
 * 
 * <p>Variables are pointers to real ingame variables which allow for
 * changing while ingame. After changing they keep their new value
 * and can be used for debugging, gameplay, etc. It accepts:</p>
 * 
 * <li> no arguments; just print out current value
 * <li> '= <new value>' to assign a new value to the variable
 * <li> '++' to increase value by one
 * <li> '--' to decrease value by one
 */
public class IConsoleVar 
{
	String 				name;               // name of the variable
	//IConsoleVar 		next; // next variable in list

	Object 				value;               // the address where the variable is pointing at
	String 				help;               // the optional help string shown when requesting information
	IConsoleVarTypes 	type;    // type of variable (for correct assignment/output)
	IConsoleCmdProc 	proc;    // some variables need really special handling, use a callback function for that
	IConsoleHooks 		hook = new IConsoleHooks();       // any special trigger action that needs executing

	public void IConsoleVarPrintGetValue() {

		String value;
		/* Some variables need really specific handling, handle this in its
		 * callback function */
		if (proc != null) {
			proc.accept(0); //, null);
			return;
		}

		value = IConsoleVarGetStringValue();
		Console.IConsolePrintF(Console._icolour_warn, "Current value for '%s' is:  %s", name, value);

	}

	/**
	 * Get the value of the variable and put it into a printable
	 * string form so we can use it for printing
	 */
	String IConsoleVarGetStringValue()
	{
		/*
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
		 */
		if( value == null ) return "? (null)";
		return value.toString();
	}

	
	/**
	 * Set a new value to a console variable
	 * 
	 * @param value the new value given to the variable, cast properly
	 */
	public void IConsoleVarSetValue(int value)
	{
		hook.IConsoleHookHandle(IConsoleHookTypes.ICONSOLE_HOOK_PRE_ACTION);
		
		switch (type) {
		case ICONSOLE_VAR_BOOLEAN:
			this.value = value != 0;
			break;
		case ICONSOLE_VAR_BYTE:
		case ICONSOLE_VAR_UINT16:
		case ICONSOLE_VAR_INT16:
		case ICONSOLE_VAR_UINT32:
		case ICONSOLE_VAR_INT32:
			this.value= (Integer)value;
			break;

		default: assert false;// NOT_REACHED();		
		}
		
		hook.IConsoleHookHandle(IConsoleHookTypes.ICONSOLE_HOOK_POST_ACTION);
		
		IConsoleVarPrintSetValue();
	}

	/**
	 * Query the current value of a variable and return it
	 * @param var the variable queried
	 * @return current value of the variable
	 */
	public int IConsoleVarGetValue()
	{
		//int result = 0;

		/*
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
		}*/
		return (Integer)value;
	}
	
	/**
	 * Print out the value of the variable after it has been assigned
	 * a new value, thus giving us feedback on the action
	 */
	public void IConsoleVarPrintSetValue()
	{
		String value = IConsoleVarGetStringValue();
		Console.IConsolePrintF(Console._icolour_warn, "'%s' changed to:  %s", name, value);
	}
	

	
	/**
	 * Set a new value to a string-type variable. Basically this
	 * means to copy the new value over to the container.
	 * @param var the variable in question
	 * @param value the new value
	 */
	public void IConsoleVarSetStringvalue(String value)
	{
		if (type != IConsoleVarTypes.ICONSOLE_VAR_STRING /*|| var.addr == null*/) return;

		hook.IConsoleHookHandle(IConsoleHookTypes.ICONSOLE_HOOK_PRE_ACTION);
		this.value = value;
		hook.IConsoleHookHandle(IConsoleHookTypes.ICONSOLE_HOOK_POST_ACTION);
		IConsoleVarPrintSetValue(); // print out the new value, giving feedback
	}
	
	
	
	
	
} 

