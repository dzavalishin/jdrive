package com.dzavalishin.console;

/** 
 * --Hooks--
 * Hooks are certain triggers get get accessed/executed on either
 * access, before execution/change or after execution/change. This allows
 * for general flow of permissions or special action needed in some cases
 */

public class IConsoleHooks
{
	IConsoleHook access; // trigger when accessing the variable/command
	IConsoleHook pre;    // trigger before the variable/command is changed/executed
	IConsoleHook post;   // trigger after the variable/command is changed/executed

	// * ************************* * //
	// * hooking code              * //
	// * ************************* * //
	/**
	 * General internal hooking code that is the same for both commands and variables
	 * @param hooks @IConsoleHooks structure that will be set according to
	 * @param type type access trigger
	 * @param proc function called when the hook criteria is met
	 */
	void IConsoleHookAdd(IConsoleHookTypes type, IConsoleHook proc)
	{
		if (proc == null) return;

		switch (type) {
		case ICONSOLE_HOOK_ACCESS:
			access = proc;
			break;
		case ICONSOLE_HOOK_PRE_ACTION:
			pre = proc;
			break;
		case ICONSOLE_HOOK_POST_ACTION:
			post = proc;
			break;
		default: assert false; //NOT_REACHED();
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
	boolean IConsoleHookHandle(IConsoleHookTypes type)
	{
		IConsoleHook proc = null;


		switch (type) {
		case ICONSOLE_HOOK_ACCESS:
			proc = access;
			break;
		case ICONSOLE_HOOK_PRE_ACTION:
			proc = pre;
			break;
		case ICONSOLE_HOOK_POST_ACTION:
			proc = post;
			break;
		default: assert false; //NOT_REACHED();
		}

		return (proc == null) ? true : proc.accept();
	}
	

}
