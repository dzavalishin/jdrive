package game.struct;

import game.EngineID;

//typedef void DrawEngineInfoProc(EngineID, int x, int y, int maxw);

@FunctionalInterface
public interface DrawEngineInfoProc
{
	//void accept(EngineID e, int x, int y, int maxw);
	void accept(int e, int x, int y, int maxw);
}
