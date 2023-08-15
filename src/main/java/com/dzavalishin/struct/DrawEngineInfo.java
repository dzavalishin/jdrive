package com.dzavalishin.struct;



public class DrawEngineInfo {

	public final DrawEngineProc engine_proc;
	public final DrawEngineInfoProc info_proc;

	public DrawEngineInfo(DrawEngineProc ep, DrawEngineInfoProc eip) {
		engine_proc = ep;
		info_proc = eip;
	}

}

/*
//typedef void DrawEngineProc(int x, int y, EngineID engine, int image_ormod);
@FunctionalInterface
public interface DrawEngineProc
{
	void accept(int x, int y, EngineID engine, int image_ormod);
}

//typedef void DrawEngineInfoProc(EngineID, int x, int y, int maxw);

@FunctionalInterface
interface DrawEngineInfoProc
{
	void accept(EngineID e, int x, int y, int maxw);
}

*/


