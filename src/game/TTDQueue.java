package game;

public abstract class TTDQueue {

	public abstract void push(OpenListNode new_node, int f);

	public abstract OpenListNode pop();

	public abstract void clear(boolean b);

	public abstract void del(OpenListNode check, int i);

}
