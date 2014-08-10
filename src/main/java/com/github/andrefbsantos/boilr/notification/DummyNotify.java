package com.github.andrefbsantos.boilr.notification;

import com.github.andrefbsantos.libpricealarm.Notify;

public class DummyNotify implements Notify {
	// Notification just for test.

	private static final long serialVersionUID = 228178154489839207L;

	@Override
	public boolean trigger() {
		System.out.println("hey");
		return false;
	}
}
