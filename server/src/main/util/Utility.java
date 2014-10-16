package main.util;

import java.util.Random;

public final class Utility {
	
	private Utility() {
		throw new UnsupportedOperationException("Cannot create instance");
	}
	
	public static byte[] randomMessage() {
		byte[] b = new byte[4096];
		new Random().nextBytes(b);
		return b;
	}
	
}
