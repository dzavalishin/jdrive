package com.dzavalishin.game;

import java.io.Serializable;

/**
 * Replaces union in C Vehicle struct
 * 
 * @author dz
 *
 */
public abstract class VehicleChild implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	abstract void clear();
}
