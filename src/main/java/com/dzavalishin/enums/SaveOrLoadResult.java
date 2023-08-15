package com.dzavalishin.enums;

public enum SaveOrLoadResult 
{
	SL_OK, // completed successfully
	SL_ERROR, // error that was caught before internal structures were modified
	SL_REINIT, // error that was caught in the middle of updating game state, need to clear it. (can only happen during load)
}
