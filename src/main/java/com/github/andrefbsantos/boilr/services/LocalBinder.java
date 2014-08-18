package com.github.andrefbsantos.boilr.services;

import java.lang.ref.WeakReference;

import android.os.Binder;

public class LocalBinder<T> extends Binder {
	private WeakReference<T> mService;

	public LocalBinder(T t) {
		mService = new WeakReference<T>(t);
	}

	public T getService() {
		return mService.get();
	}
}
