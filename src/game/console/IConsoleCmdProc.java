package game.console;

@FunctionalInterface
interface IConsoleCmdProc {
	boolean accept(int argc, String ... argv);
}