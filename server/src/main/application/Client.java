package main.application;

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
		return !socket.isClosed() && !writeSocket.isClosed();
	}
	
	public InetAddress getInetAddress() {
		return address;
	}
	
	public Thread getAssociatedThread() {
		return thread;
	}
	
	public synchronized void startNewAssociatedThread(Thread thread) {
		if (this.thread == null && !started) {
			this.thread = thread;
			thread.start();
			started = true;
		}
	}
	
	public boolean equals(Object object) {
		if (!object.getClass().equals(getClass())) {
			return false;
		}
		Client client = (Client) object;
		return socket.equals(client.getSocket())
				&& writeSocket.equals(client.getWriteSocket())
				&& address.equals(client.getInetAddress())
				&& thread.equals(client.getAssociatedThread());
	}
	
	public int hashCode() {
		int hashCode = super.hashCode();
		
		if (socket != null) {
			hashCode += 13*socket.hashCode();
		}
		if (writeSocket != null) {
			hashCode += 23*writeSocket.hashCode();
		}
		if (address != null) {
			hashCode += 5*address.hashCode();
		}
		if (thread != null) {
			hashCode += 7*thread.hashCode();
		}
		return hashCode;
	}
}
