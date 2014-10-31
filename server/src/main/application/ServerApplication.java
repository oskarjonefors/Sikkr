package main.application;

import java.io.IOException;
import java.security.Security;

import main.ui.ServerConsoleUI;

public class ServerApplication {
	
	public static void main(String[] args) {
		new ServerApplication();
	}
	
	public ServerApplication() {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Thread thread;
		try {
			thread = new ServerThread(new ServerConsoleUI());
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
