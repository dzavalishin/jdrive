package com.dzavalishin.console;

@FunctionalInterface
interface IConsoleHook {
	boolean accept();
}