package com.dzavalishin.struct;

//typedef void DrawEngineProc(int x, int y, EngineID engine, int image_ormod);
@FunctionalInterface
public interface DrawEngineProc
{
	//void accept(int x, int y, EngineID engine, int image_ormod);
	void accept(int x, int y, int engine, int image_ormod);
}
