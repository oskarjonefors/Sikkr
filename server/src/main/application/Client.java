package main.application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

	private Socket socket = null, writeSocket = null;
	private final InetAddress address;
	private Thread thread;
	private boolean started = false;
	
	public Client(InetAddress address) {
		this.address = address;
	}
	
	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Socket getWriteSocket() {
		return writeSocket;
	}

	public void setWriteSocket(Socket writeSocket) {
		this.writeSocket = writeSocket;
	}
	
	public boolean allSocketsSet() {
		return socket != null && writeSocket != null;
	}
	
	public boolean allSocketsOpen() {
		return socket.isConnected() && writeSocket.isConnected();
	}

	public InetAddress getInetAddress() {
		return address;
	}
	
	public synchronized void startNewAssociatedThread(Thread thread) {
		if (this.thread == null && !started) {
			this.thread = thread;
			thread.start();
			started = true;
		}
	}

    public void closeAllSockets() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
        if (!writeSocket.isClosed()) {
            writeSocket.close();
        }
    }
}
