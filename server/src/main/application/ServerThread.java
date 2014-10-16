package main.application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import main.util.InformationEvent;
import main.util.InformationListener;

public class ServerThread extends Thread {
	
	/*
	 * ----------------------- Instance constants ------------------
	 */
	
	private final InformationListener listener;
	private final ServerSocket socket;
	private final ServerSocket objectSocket;
	private final ServerSocket writeSocket;
	private final StateHolder stateHolder;
	private final Map<InetAddress, Client> clients;
	private final Set<Client> readyClients;
	
	/*
	 * ----------------------- Initiation --------------------------
	 */
	
	public ServerThread(InformationListener listener) throws IOException {
		super("Server thread");
		this.listener = listener;
		this.socket = new ServerSocket(listener.getPort());
		this.objectSocket = new ServerSocket(listener.getObjectPort());
		this.writeSocket = new ServerSocket(listener.getWritePort());
		this.readyClients = new HashSet<Client>();
		
		this.stateHolder = new StateHolder();
		this.clients = Collections.synchronizedMap(new HashMap<InetAddress, Client>());
	}
	
	/*
	 * ----------------------- Public methods ----------------------
	 */
	
	@Override
	public void run() {
		this.stateHolder.state = State.RUNNING;
		IndividualServerSocketThread standard = null;
		IndividualServerSocketThread object = null;
		IndividualServerSocketThread write = null;
		while (stateHolder.state == State.RUNNING) {
			if (standard == null || !standard.isAlive()) {
				standard = new IndividualServerSocketThread(socket, SocketApplication.STANDARD);
				standard.start();
			}
			
			if (object == null || !object.isAlive()) {
				object = new IndividualServerSocketThread(objectSocket, SocketApplication.OBJECT);
				object.start();
			}
			
			if (write == null || !write.isAlive()) {
				write = new IndividualServerSocketThread(writeSocket, SocketApplication.WRITE);
				write.start();
			}

			try {
				for (Client client : readyClients) {
					client.startNewAssociatedThread(new SocketThread(client, listener));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				sleep(500);
			} catch (InterruptedException e) {
				
			}
		}
		
	}
	
	/*
	 * ----------------------- Inner classes ---------------------
	 */
	
	private final static class StateHolder {
		public State state;
	}

	private enum State {
		RUNNING, NOT_RUNNING;
	}
	
	private enum SocketApplication {
		STANDARD, OBJECT, WRITE
	}
	
	private class IndividualServerSocketThread extends Thread {
		
		private final ServerSocket socket;
		private final SocketApplication application;
		
		public IndividualServerSocketThread(ServerSocket socket, SocketApplication application) {
			super("ServerSocket thread");
			this.socket = socket;
			this.application = application;
		}
		
		public void run() {
			try {
				Socket client = socket.accept();
				InetAddress address = client.getInetAddress();
				
				if (!clients.containsKey(address)) {
					clients.put(address, new Client(address));
				}
				
				switch (application) {
					case STANDARD:
						clients.get(address).setSocket(client);
						break;
					case OBJECT:
						clients.get(address).setObjectSocket(client);
						break;
					case WRITE:
						clients.get(address).setWriteSocket(client);
						break;
				}

				if (clients.get(address).allSocketsSet()) {
					readyClients.add(clients.get(address));
				}
			} catch (IOException e) {
				listener.sendInformation(new InformationEvent(e));
				stateHolder.state = ServerThread.State.NOT_RUNNING;
			}
		}
		
	}
}
