/**
 *
 */
package com.github.andrefbsantos.boilr.database;

import java.util.Timer;

/**
 * @author andre
 *
 */
public class Singleton {

	// Singleton to hold Timer
	private static Timer instance = null;

	private Singleton() {
	}

	public static synchronized Timer getInstance() {
		if (instance == null) {
			instance = new Timer();
		}
		return instance;
	}
}
