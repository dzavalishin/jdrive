package com.dzavalishin.exceptions;

import java.io.File;

public class InvalidFileFormat extends Exception
{
	public InvalidFileFormat(File name) {
		super("Invalid file format: "+name);
	}
}
