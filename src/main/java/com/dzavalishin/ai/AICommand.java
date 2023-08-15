package com.dzavalishin.ai;

/* How DoCommands look like for an AI */
class AICommand {
	int tile;
	int p1;
	int p2;
	int procc;

	String text;
	int uid;

	AICommand next;
}