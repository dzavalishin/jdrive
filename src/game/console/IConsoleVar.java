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
		return value.toString();
	}

}

@FunctionalInterface
interface IConsoleCmdProc {
	boolean accept(int argc, String ... argv);
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

