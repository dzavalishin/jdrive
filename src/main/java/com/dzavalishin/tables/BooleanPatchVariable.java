package com.dzavalishin.tables;

public class BooleanPatchVariable extends PatchVariable 
{
	private boolean value;

	public BooleanPatchVariable() {
		value = false;
	}

	public BooleanPatchVariable(boolean b) {
		value = b;
	}

	@Override
	public void setValue(int value) { this.value = value != 0; }

	@Override
	public int getValue() { return value ? 1 : 0; }

	public void set(boolean value) { this.value = value; }
	public boolean get() { return value; }
	
}
