package main.application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.logging.Level;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import main.model.Contact;
import main.model.Message;
import main.util.EncryptedMessage;
import main.util.InformationEvent;
import main.util.InformationListener;
import main.util.Utility;

public class SocketThread extends Thread {
	
	/*
	 * ------------------- Instance constants ----------------------------
	 */
	
	private final Client client;
	private final InformationListener listener;
	
	/*
	 * ------------------- Instance variables ----------------------------
	 */
	
	private boolean verifiedClient = false;
	private Contact contact;
	
	private final DataInputStream inputStream;
	private final DataOutputStream outputStream;
	
	private final BufferedReader bufferedReader;
	private final BufferedWriter bufferedWriter;
	
	private final InputStream objectInputStream;
	private final OutputStream objectOutputStream;
	
	/*
	 * ------------------- Initiation ------------------------------------
	 */
	
	public SocketThread(Client client, InformationListener listener) throws IOException {
		super("Socket thread");
		this.client = client;
		this.listener = listener;
		this.inputStream = new DataInputStream(client.getSocket().getInputStream());
		this.outputStream = new DataOutputStream(client.getSocket().getOutputStream());
		this.bufferedReader = new BufferedReader(new InputStreamReader(client.getWriteSocket().getInputStream()));
		this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getWriteSocket().getOutputStream()));

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
		this.objectInputStream = client.getObjectSocket().getInputStream();
		this.objectOutputStream = client.getObjectSocket().getOutputStream();
	}
	
	/*
	 * ------------------- Public methods --------------------------------
	 */
	
	@Override
	public void run() {
		listener.sendInformation(new InformationEvent(Level.INFO, "Connection established to client: "+client.getInetAddress()));

		try {	
			while (client.allSocketsOpen()) {
				String command = bufferedReader.readLine();
				if (command.toLowerCase().startsWith("verify:")) {
					verify(command.replace("verify:", ""));
				} else if (command.toLowerCase().equals("get_recieved_messages")) {
					sendRecievedMessages();
				} else if (command.toLowerCase().equals("get_sent_messages")) {
					sendSentMessages();
				} else if (command.toLowerCase().equals("send_message")) {
					beginRecieveMessage();
				} else if (command.toLowerCase().equals("get_server_key")) {
					sendObject(listener.getPublicKey());
				} else if (command.toLowerCase().equals("is_client")) {
					checkIfServerHasClient();
				}
			}
		} catch (Exception e) {
			listener.sendInformation(new InformationEvent(e));
			System.exit(0);
		}
	}
	
	public boolean isClientVerifiedContact() {
		return verifiedClient;
	}
	
	/*
	 * ------------------- Private methods -------------------------------
	 */
	
	private RSAPublicKey askForPublicKey() throws IOException {
		listener.sendInformation(new InformationEvent(Level.INFO, "Asking client for a public key"));
		RSAPublicKey key;
		try {
			key = (RSAPublicKey) getObjectFromClient();
		} catch (ClassNotFoundException e) {
			listener.sendInformation(new InformationEvent(e));
			key = null;
		} 
		
		return key;
	}
	
	private void beginRecieveMessage() throws Exception {
		if (isClientVerifiedContact()) {
			listener.sendInformation(new InformationEvent(Level.INFO, "Recieving message from client: "+contact.getNumber()));
			
			final int ivLength = inputStream.readInt();
			final int keyLength = inputStream.readInt();
			final int numberLength = inputStream.readInt();
			final int contentLength = inputStream.readInt();
			final int type = inputStream.readInt();
			final long time = inputStream.readLong();
			final byte[] encryptedIV = new byte[ivLength], decryptedIV;
			final byte[] encryptedKey = new byte[keyLength], decryptedKey;
			final byte[] encryptedNumber = new byte[numberLength];
			final byte[] encryptedContent = new byte[contentLength], content;
			final String number;
			final Message message;
			
			inputStream.readFully(encryptedIV);
			inputStream.readFully(encryptedKey);
			inputStream.readFully(encryptedNumber);
			inputStream.readFully(encryptedContent);
			
			decryptedIV = listener.decrypt(encryptedIV);
			decryptedKey = listener.decrypt(encryptedKey);
			
			number = new String(aesDecrypt(encryptedNumber, decryptedKey, decryptedIV));
			content = aesDecrypt(encryptedContent, decryptedKey, decryptedIV);
			message = new Message(content, contact.getNumber(), number, type, time);
			listener.getContactByNumber(number).recievedMessages.add(message);
			contact.sentMessages.add(message);
			listener.sendInformation(new InformationEvent(Level.INFO, "Recieved message \""+new String(content)+"\" to "+number));
		}
	}
	
	
	private byte[] getClientVerificationAnswer(Contact c, EncryptedMessage message) throws Exception {
		int ivLength;
		int keyLength;
		int answerLength;
		byte[] answer;
		byte[] answerIV;
		byte[] answerKey;
		byte[] encryptedIV = c.encryptBytes(message.iv);
		byte[] encryptedKey = c.encryptBytes(message.aeskey);
		byte[] decryptedIV;
		byte[] decryptedKey;
		listener.sendInformation(new InformationEvent(Level.INFO, "Acquiring decrypted bytes from client, using aes key: "+new String(message.aeskey)));
		outputStream.writeInt(encryptedIV.length);
		outputStream.writeInt(encryptedKey.length);
		outputStream.writeInt(message.encryptedBytes[0].length);
		outputStream.write(encryptedIV);
		outputStream.write(encryptedKey);
		outputStream.write(message.encryptedBytes[0]);
		outputStream.flush();
		
		ivLength = inputStream.readInt();
		keyLength = inputStream.readInt();
		answerLength = inputStream.readInt();
		
		answerIV = new byte[ivLength];
		answerKey = new byte[keyLength];
		answer = new byte[answerLength];
		
		inputStream.readFully(answerIV);
		inputStream.readFully(answerKey);
		inputStream.readFully(answer);
		
		decryptedIV = listener.decrypt(answerIV);
		decryptedKey = listener.decrypt(answerKey);
		return aesDecrypt(answer, decryptedKey, decryptedIV);
	}
	
	private void sendRecievedMessages() throws Exception {
		if (verifiedClient && contact != null) {
			sendMessagesToClient(contact.recievedMessages);
		}
	}
	
	private void sendSentMessages() throws Exception {
		if (verifiedClient && contact != null) {
			sendMessagesToClient(contact.sentMessages);
		}
	}
	
	private void sendMessagesToClient(List<Message> list) throws Exception {
		if (isClientVerifiedContact()) {
			listener.sendInformation(new InformationEvent(Level.INFO, "Sending messages to client: "+contact.getNumber()));
			//DataOutputStream dos = new DataOutputStream(outputStream);
			outputStream.writeInt(list.size());
			
			for (Message message : list) {
				EncryptedMessage msg = new EncryptedMessage(message.getSender().getBytes(), message.getReciever().getBytes(), message.getContent());
				byte[] encryptedIV = contact.encryptBytes(msg.iv);
				byte[] encryptedKey = contact.encryptBytes(msg.aeskey);
				int type = message.getType();
				long time = message.getTimeInMillis();
				
				outputStream.writeInt(encryptedIV.length);
				outputStream.writeInt(encryptedKey.length);
				outputStream.writeInt(msg.encryptedBytes[0].length);
				outputStream.writeInt(msg.encryptedBytes[1].length);
				outputStream.writeInt(msg.encryptedBytes[2].length);
				outputStream.write(encryptedIV);
				outputStream.write(encryptedKey);
				outputStream.write(msg.encryptedBytes[0]);
				outputStream.write(msg.encryptedBytes[1]);
				outputStream.write(msg.encryptedBytes[2]);
				outputStream.writeInt(type);
				outputStream.writeLong(time);
				outputStream.flush();
			}
		}
	}
	
	private void verify(String number) throws IOException {
		listener.sendInformation(new InformationEvent(Level.INFO, "Verifying client: "+number));
		final Contact c = listener.getContactByNumber(number);
		if (c == null) {
			bufferedWriter.append("verification_method new_client");
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			contact = new Contact(number, askForPublicKey());
			listener.addContact(contact);
		} else if (verifyContact(c)) {
			listener.sendInformation(new InformationEvent(Level.INFO, "Client verified: "+client.getInetAddress()+" as "+c.getNumber()));
			contact = c;
			verifiedClient = true;
		} else {
			listener.sendInformation(new InformationEvent(Level.INFO, "Client failed verification!"));
		}
	}
	
	private boolean verifyContact(Contact c) throws IOException {
		bufferedWriter.append("verification_method verification_code");
		bufferedWriter.newLine();
		bufferedWriter.flush();
		
		final byte[] bytes = Utility.randomMessage();
		final byte[] answer;
		try {
			EncryptedMessage message = new EncryptedMessage(bytes);
			answer = getClientVerificationAnswer(c, message);
		} catch (Exception e) {
			listener.sendInformation(new InformationEvent(e));
			return false;
		}
		
		return (new String(bytes)).equals(new String(answer));
	}
	
	private Object getObjectFromClient() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(objectInputStream);
		return ois.readObject();
	}
	
	private void sendObject(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		objectOutputStream.write(baos.toByteArray());
		objectOutputStream.flush();
	}
	
	private byte[] aesDecrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec kSpec = new SecretKeySpec(key, "AES/CBC/PKCS5Padding");
		
		cipher.init(Cipher.DECRYPT_MODE, kSpec, new IvParameterSpec(iv));
		return cipher.doFinal(data);
		
	}
	
	private void checkIfServerHasClient() throws Exception {
		if (contact != null) {
			String number = getNumber();
			String hasClient = "" + (listener.getContactByNumber(number) != null);
			EncryptedMessage answer = new EncryptedMessage(hasClient.getBytes());
			byte[] iv = contact.encryptBytes(answer.iv);
			byte[] key = contact.encryptBytes(answer.aeskey);

			listener.sendInformation(new InformationEvent(Level.INFO, "Check if server has a client with number "+number+" returned: "+hasClient));
			outputStream.writeInt(iv.length);
			outputStream.writeInt(key.length);
			outputStream.writeInt(answer.encryptedBytes[0].length);
			
			outputStream.write(iv);
			outputStream.write(key);
			outputStream.write(answer.encryptedBytes[0]);
			
			outputStream.flush();
			listener.sendInformation(new InformationEvent(Level.INFO, "Sent information about number "+number+" to client"));
		}
	}
	
	private String getNumber() throws Exception {
		listener.sendInformation(new InformationEvent(Level.INFO, "Checking if server has a client"));
		final int ivLength = inputStream.readInt();
		final int keyLength = inputStream.readInt();
		final int numberLength = inputStream.readInt();

		final byte[] encryptedIV = new byte[ivLength], decryptedIV;
		final byte[] encryptedKey = new byte[keyLength], decryptedKey;
		final byte[] encryptedNumber = new byte[numberLength];
		
		inputStream.readFully(encryptedIV);
		inputStream.readFully(encryptedKey);
		inputStream.readFully(encryptedNumber);
		
		decryptedIV = listener.decrypt(encryptedIV);
		decryptedKey = listener.decrypt(encryptedKey);
		
		return new String(aesDecrypt(encryptedNumber, decryptedKey, decryptedIV));
	}
	

}
