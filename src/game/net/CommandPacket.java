package game.net;

import game.TileIndex;
import game.ids.PlayerID;

public class CommandPacket {
	CommandPacket next;
	PlayerID player; /// player that is executing the command
	int cmd;    /// command being executed
	int p1;     /// parameter p1
	int p2;     /// parameter p2
	TileIndex tile; /// tile command being executed on
	char [] text = new char[80];
	int frame;  /// the frame in which this packet is executed
	int callback; /// any callback function executed upon successful completion of the command

}
