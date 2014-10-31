package main.application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
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
		this.writeSocket = new ServerSocket(listener.getWritePort());
		this.readyClients = new HashSet<>();
		
		this.stateHolder = new StateHolder();
		this.clients = new HashMap<>();
	}
	
	/*
	 * ----------------------- Public methods ----------------------
	 */
	
	@Override
	public void run() {
		this.stateHolder.state = ServerState.RUNNING;
        HashSet<Client> tmp = new HashSet<>();
		IndividualServerSocketThread standard = new IndividualServerSocketThread(socket, SocketApplication.STANDARD);
		IndividualServerSocketThread write = new IndividualServerSocketThread(writeSocket, SocketApplication.WRITE);

        standard.start();
        write.start();
		while (stateHolder.state == ServerState.RUNNING) {
            readyClients.forEach((client) -> startClientThread(client, tmp));
            readyClients.removeAll(tmp);
            tmp.forEach((client) -> clients.remove(client.getInetAddress()));
            tmp.clear();
            try {
                sleep(500);
            } catch (InterruptedException e) {
                listener.sendInformation(new InformationEvent(e));
            }
        }
	}

    private void startClientThread(Client client, Collection<Client> tmp) {
        try {
            client.startNewAssociatedThread(new SocketThread(client, listener));
            tmp.add(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	/*
	 * ----------------------- Inner classes ---------------------
	 */
	
	private final static class StateHolder {
		public ServerState state;
	}

	private enum ServerState {
		RUNNING, NOT_RUNNING
	}
	
	private enum SocketApplication {
		STANDARD, WRITE
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
				while(stateHolder.state == ServerState.RUNNING) {
                    new ClientSetupThread(socket.accept(), application).start();
                }
			} catch (IOException e) {
				listener.sendInformation(new InformationEvent(e));
				stateHolder.state = ServerState.NOT_RUNNING;
			}
		}
		
	}

    private class ClientSetupThread extends Thread {

        final Socket socket;
        final SocketApplication application;

        public ClientSetupThread(Socket socket, SocketApplication application) {
            super("Client setup thread");
            this.socket = socket;
            this.application = application;
        }

        public void run() {
            InetAddress address = socket.getInetAddress();
            Client client;
            if (!clients.containsKey(address)) {
                client = new Client(address);
                clients.put(address, client);
            } else {
                client = clients.get(address);
            }

            switch (application) {
                case STANDARD:
                    clients.get(address).setSocket(socket);
                    break;
                case WRITE:
                    clients.get(address).setWriteSocket(socket);
                    break;
            }

            if (client.allSocketsSet() && client.allSocketsOpen()) {
                readyClients.add(client);
            }
        }
    }
}
