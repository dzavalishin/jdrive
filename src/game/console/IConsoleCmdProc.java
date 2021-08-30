package game.console;

@FunctionalInterface
interface IConsoleCmdProc {
	boolean accept(String ... argv);
}