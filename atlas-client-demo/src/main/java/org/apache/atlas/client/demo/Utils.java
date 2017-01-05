package org.apache.atlas.client.demo;

import java.util.function.Function;

public class Utils {

	public static final Function<Integer, Boolean> WAIT = period -> {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while sleeping", e);
		}
		return true;
	};

}
