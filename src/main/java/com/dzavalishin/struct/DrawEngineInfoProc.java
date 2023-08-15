package com.dzavalishin.struct;


@FunctionalInterface
public interface DrawEngineInfoProc
{
	//void accept(EngineID e, int x, int y, int maxw);
	void accept(int e, int x, int y, int maxw);
}
