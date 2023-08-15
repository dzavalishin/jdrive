package com.dzavalishin.ifaces;

//typedef int32 CommandProc(int x, int y, uint32 flags, uint32 p1, uint32 p2);
@FunctionalInterface
public interface CommandProc {
	int exec(int x, int y, int flags, int p1, int p2);
}
