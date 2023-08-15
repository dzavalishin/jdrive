package com.dzavalishin.tables;

import java.util.function.Consumer;

import com.dzavalishin.util.BitOps;

public class PatchEntry {
	private PatchVariable var;
	private int flags;						// selector flags

	//StringID 
	private int str;						// string with descriptive text
	private String console_name;			// the name this patch has in console

	public final int min, max;		// range for spinbox setting
	public final int step;			// step for spinbox

	//PatchButtonClick* click_proc;	// callback procedure
	private Consumer<PatchVariable> proc; // Unused?

	public PatchEntry(int flags, int str, String name, PatchVariable var, 
			int min, int max, int step, Consumer<PatchVariable> proc) 
	{
		this.flags = flags;
		this.str = str;
		console_name = name;
		this.var = var;
		this.min = min;
		this.max = max;
		this.step = step;
		this.proc = proc;
	}

	public PatchVariable getVariable() { return var; }
	public int getString() { return str; }



	public boolean nameIs(String name) { return console_name.equals(name); }


	public boolean isNetworkOnly() {
		return 0 != (flags & SettingsTables.PF_NETWORK_ONLY);
	}

	public boolean isPlayerBased() {
		return 0 != (flags & SettingsTables.PF_PLAYERBASED);
	}

	public boolean isNoComma() {
		return 0 != (flags & SettingsTables.PF_NOCOMMA);
	}

	public boolean isMultistring() {
		return 0 != (flags & SettingsTables.PF_MULTISTRING);
	}

	public boolean zeroIsDisable() {
		return 0 == (flags & SettingsTables.PF_0ISDIS);
	}

	public void onClick() {
		if (proc != null) proc.accept(var);
	}

	public boolean isBoolean() {		
		return var instanceof BooleanPatchVariable;
	}

	public boolean isCurrency() {
		return var instanceof CurrencyPatchVariable;
	}

	public int ReadPE() 
	{
		// TODO in CurrencyPatchVariable case PE_CURRENCY:  return (*(int*)pe.variable) * _currency.rate;

		return var.getValue(); 
	}

	public void WritePE(int value) 
	{ 
		var.setValue(value); 

		if (zeroIsDisable() && value <= 0) {
			var.setValue(0);
			return;
		}

		// "clamp" 'disabled' value to smallest type
		if(isBoolean())
			var.setValue(value);
		else
			var.setValue(BitOps.clamp(value, min, max));	
	}

}
