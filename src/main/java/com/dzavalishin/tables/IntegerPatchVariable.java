package com.dzavalishin.tables;

public class IntegerPatchVariable  extends PatchVariable 
{
	private int value;

	public IntegerPatchVariable() {
		value = 0;
	}

	public IntegerPatchVariable(int i) {
		value = i;
	}

	@Override
	public void setValue(int value) { this.value = value; }

	@Override
	public int getValue() { return value; }

	public void set(int value) { this.value = value; }
	public int get() { return value; }

}
