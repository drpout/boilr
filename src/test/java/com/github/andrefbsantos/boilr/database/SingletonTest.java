package com.github.andrefbsantos.boilr.database;

import java.util.Timer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SingletonTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Timer t1 = Singleton.getInstance();
		Timer t2 = Singleton.getInstance();
		Assert.assertEquals(t1, t2);
	}
}
