package game.console;

/** --Commands--
 * Commands are commands, or functions. They get executed once and any
 * effect they produce are carried out. The arguments to the commands
 * are given to them, each input word seperated by a double-quote (") is an argument
 * If you want to handle multiple words as one, enclose them in double-quotes
 * eg. 'say "hello sexy boy"'
 */

public class IConsoleCmd {
	String 			name;               // name of command
	//IConsoleCmd 	next; // next command in list

	IConsoleCmdProc proc;    // process executed when command is typed

	// any special trigger action that needs executing
	IConsoleHooks 	hook = new IConsoleHooks();       
} 
