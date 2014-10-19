import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TestClientApplication {
	
	private final static Logger LOG = Logger.getGlobal();

	private final RSAPrivateKey privateKey;
	private final RSAPublicKey publicKey;
	private RSAPublicKey serverKey;
	

	
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	
	private TestClientApplication() {

		final KeyPair key = getKeyPair();
		this.publicKey = (RSAPublicKey) key.getPublic();
		this.privateKey = (RSAPrivateKey) key.getPrivate();

		Socket writeSocket;
		Socket socket;
		try {
			socket = new Socket("46.239.104.32", 997);
			writeSocket = new Socket("46.239.104.32", 998);
            Logger.getGlobal().info("We have successfully opened the sockets");
			
			this.inputStream = new DataInputStream(socket.getInputStream());
			this.outputStream = new DataOutputStream(socket.getOutputStream());
			this.bufferedReader = new BufferedReader(new InputStreamReader(writeSocket.getInputStream()));
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(writeSocket.getOutputStream()));
			Logger.getGlobal().info("We have successfully opened the streams and readers");


			setServerKey();
			
			testServerWithSocket();
			testServerWithSocket();
			
			Thread.sleep(3000);
			
			if (serverHasClient("666")) {
				sendMessage("666", "Hello World!".getBytes());
				sendMessage("666", "Hello Another World!".getBytes());
				testGetMessages();
			} else {
				LOG.info("Server does not have a client with the number 666");
			}
			
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LOG.log(Level.INFO, "Done!");
	}
	
	public static void main(String[] args) {

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		new TestClientApplication();
	}
	
	private void testServerWithSocket() throws Exception {
		writeLine("verify:0737721528");
		switch (getVerificationMethod()) {
		case "new_client":
			newClientVerification();
			break;
		case "verification_code":
			useVerificationCode();
			break;
		default:
		}
	}
	
	private void setServerKey() {
        try {
            Logger.getGlobal().info("Setting server public key");
            writeLine("get_server_key");
            Logger.getGlobal().info("We have asked the server for a public key");
            int keyLength = inputStream.readInt();
            byte[] keyBytes = new byte[keyLength];
            inputStream.readFully(keyBytes);
            Logger.getGlobal().info("We have received the server's public key!");
            serverKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Logger.getGlobal().info("We have successfully created an RSAPublicKey from the received key");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	private String getVerificationMethod() throws IOException {
		String read = bufferedReader.readLine();
		if (read.contains("verification_method ")) {
			return read.replace("verification_method ", "");
		} else {
			return "invalid";
		}
	}

    private void newClientVerification() throws Exception {
        sendEncryptedDataToServer(publicKey.getEncoded());
    }

    private void sendEncryptedDataToServer(byte[]... bytes) throws Exception {
        EncryptedMessage msg = new EncryptedMessage(bytes);
        byte[] encryptedIV = encrypt(msg.iv);
        byte[] encryptedKey = encrypt(msg.aeskey);

        outputStream.writeInt(encryptedIV.length);
        outputStream.writeInt(encryptedKey.length);
        for (byte[] data : msg.encryptedBytes) {
            outputStream.writeInt(data.length);
        }

        outputStream.write(encryptedIV);
        outputStream.write(encryptedKey);

        for (byte[] data : msg.encryptedBytes) {
            outputStream.write(data);
        }

        outputStream.flush();
    }
	
	private void useVerificationCode() throws Exception {
		LOG.log(Level.INFO, "Server is expecting us to verify using a verification code!");
		int ivLength = inputStream.readInt();
        int keyLength = inputStream.readInt();
        int length = inputStream.readInt();
        byte[] readBytes = new byte[length];
        byte[] receivedIV = new byte[ivLength], iv, encryptedIV;
        byte[] receivedKey = new byte[keyLength], key, encryptedKey;
        byte[] decryptedBytes;
        EncryptedMessage message;
		
		LOG.log(Level.INFO, "Finished reading from server!");
		inputStream.readFully(receivedIV);
		inputStream.readFully(receivedKey);
        inputStream.readFully(readBytes);
        
        iv = decrypt(receivedIV);
        key = decrypt(receivedKey);
        decryptedBytes = aesDecrypt(readBytes, key, iv);
        message = new EncryptedMessage(decryptedBytes);
        encryptedIV = encrypt(message.iv);
        encryptedKey = encrypt(message.aeskey);
        
        outputStream.writeInt(encryptedIV.length);
        outputStream.writeInt(encryptedKey.length);
		outputStream.writeInt(message.encryptedBytes[0].length);
		
		outputStream.write(encryptedIV);
		outputStream.write(encryptedKey);
		outputStream.write(message.encryptedBytes[0]);
		outputStream.flush();
		
		LOG.log(Level.INFO, "Finished sending to server!");
	}
	
	private void sendMessage(String number, byte[] content) {
        try {
        	LOG.info("Sending message to server");
            EncryptedMessage message = new EncryptedMessage(number.getBytes(), content);
            byte[] encryptedIV = encrypt(message.iv);
            byte[] encryptedKey = encrypt(message.aeskey);
            writeLine("send_message");
            outputStream.writeInt(encryptedIV.length);
            outputStream.writeInt(encryptedKey.length);
            outputStream.writeInt(message.encryptedBytes[0].length);
            outputStream.writeInt(message.encryptedBytes[1].length);
            outputStream.writeInt(1);
            outputStream.writeLong(1000);
            outputStream.write(encryptedIV);
            outputStream.write(encryptedKey);
            outputStream.write(message.encryptedBytes[0]);
            outputStream.write(message.encryptedBytes[1]);
            outputStream.flush();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
    private void writeLine(String string, boolean flush) throws IOException {
        bufferedWriter.append(string);
        bufferedWriter.newLine();
        if (flush) {
        	bufferedWriter.flush();
        }
    }

    private void writeLine(String string) throws IOException {
        writeLine(string, true);
    }

    private byte[] encrypt(byte[] bytes) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, serverKey);
        return cipher.doFinal(bytes);
    }
    
    private byte[] decrypt(byte[] bytes) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(bytes);
    }
    
    private void testGetMessages() throws Exception {
        final List<Message> messages = new ArrayList<Message>();
        final int n;

        writeLine("get_sent_messages");

        n = inputStream.readInt();
        LOG.info("Reading " + n +" messages");
        
        for (int i = 0; i < n; i++) {
        	final int ivLength = inputStream.readInt();
        	final int keyLength = inputStream.readInt();
            final int senderNumberLength = inputStream.readInt();
            final int receiverNumberLength = inputStream.readInt();
            final int contentLength = inputStream.readInt();
            final byte[] encryptedKey = new byte[keyLength], key;
            final byte[] encryptedIV = new byte[ivLength], iv;
            final byte[] encryptedSenderNumber = new byte[senderNumberLength];
            final byte[] encryptedReceiverNumber = new byte[receiverNumberLength];
            final byte[] encryptedContent = new byte[contentLength];
            final byte[] content;
            final int type;
            final long time;
            final String senderNumber;
            final String receiverNumber;
            final Message msg;

            inputStream.readFully(encryptedIV);
            inputStream.readFully(encryptedKey);
            inputStream.readFully(encryptedSenderNumber);
            inputStream.readFully(encryptedReceiverNumber);
            inputStream.readFully(encryptedContent);

            iv = decrypt(encryptedIV);
            key = decrypt(encryptedKey);
            senderNumber = new String(aesDecrypt(encryptedSenderNumber, key, iv));
            receiverNumber = new String(aesDecrypt(encryptedReceiverNumber, key, iv));
            content = aesDecrypt(encryptedContent, key, iv);
            type = inputStream.readInt();
            time = inputStream.readLong();


            msg = new Message(senderNumber, receiverNumber, content, type, time);
            messages.add(msg);
        }
        
        LOG.info("Finished downloading messages");
        
        for (Message msg : messages) {
        	LOG.info("Message (Sender="+msg.SENDER+"; Receiver="+msg.RECEIVER+"; Content=\""+new String(msg.CONTENT)+"\"; Type="+msg.TYPE+"; Timestamp="+msg.TIMESTAMP+")");
        }
    }
    
	private byte[] aesDecrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec kSpec = new SecretKeySpec(key, "AES/CBC/PKCS5Padding");
		
		cipher.init(Cipher.DECRYPT_MODE, kSpec, new IvParameterSpec(iv));
		return cipher.doFinal(data);
		
	}
    
	private KeyPair getKeyPair() {
		KeyPair key;
		try {
			key = getKeyFromFile();
		} catch (ClassNotFoundException | IOException e1) {
			key = null;
		}
		if (key == null) {
			final KeyPairGenerator keyGen;
			
			try {
				keyGen = KeyPairGenerator.getInstance("RSA");
		        keyGen.initialize(4096);
		        key = keyGen.genKeyPair();
		        saveKeyToFile(key);
			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return key;
	}
	
	private void saveKeyToFile(KeyPair key) throws IOException {
		File keyFile = new File("key");
		ObjectOutputStream oos;
		if (keyFile.exists()) {
			keyFile.delete();
		}
		keyFile.createNewFile();
		
		oos = new ObjectOutputStream(new FileOutputStream(keyFile));
		oos.writeObject(key);
		oos.flush();
		oos.close();
	}
	
	private KeyPair getKeyFromFile() throws ClassNotFoundException, IOException {
		ObjectInputStream dis = new ObjectInputStream(new FileInputStream("key"));
		Object read = dis.readObject();
		dis.close();
		return (KeyPair) read;
	}
	
	private boolean serverHasClient(String number) throws Exception {
		EncryptedMessage msg = new EncryptedMessage(number.getBytes());
		byte[] iv = encrypt(msg.iv), key = encrypt(msg.aeskey);
		byte[] answerIv, answerKey, answer;
		int ivLength, keyLength, answerLength;
		
		writeLine("is_client");
		outputStream.writeInt(iv.length);
		outputStream.writeInt(key.length);
		outputStream.writeInt(msg.encryptedBytes[0].length);
		
		outputStream.write(iv);
		outputStream.write(key);
		outputStream.write(msg.encryptedBytes[0]);
		
		outputStream.flush();
		
		ivLength = inputStream.readInt();
		keyLength = inputStream.readInt();
		answerLength = inputStream.readInt();
		
		iv = new byte[ivLength];
		key = new byte[keyLength];
		answer = new byte[answerLength];
		
		inputStream.readFully(iv);
		inputStream.readFully(key);
		inputStream.readFully(answer);
		
		iv = decrypt(iv);
		key = decrypt(key);
		answer = aesDecrypt(answer, key, iv);
		
		return Boolean.parseBoolean(new String(answer));
	}
	
    public final static class Message {

        public final String SENDER, RECEIVER;
        public final byte[] CONTENT;
        public final int TYPE;
        public final long TIMESTAMP;

        public final static int TYPE_TEXT = 1;
        public final static int TYPE_DATA = 2;

        private Message(final String SENDER, final String RECIEVER, final byte[] CONTENT,
                       final int TYPE, final long TIMESTAMP) {
            this.SENDER = SENDER;
            this.RECEIVER = RECIEVER;
            this.CONTENT = CONTENT;
            this.TYPE = TYPE;
            this.TIMESTAMP = TIMESTAMP;
        }
    }
	
}
