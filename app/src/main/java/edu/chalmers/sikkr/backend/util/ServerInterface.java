package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Eric on 2014-10-13.
 */
public final class ServerInterface {

    private final static String SERVER_IP = "127.0.0.1";

    /**
     * Singleton of ServerInterface
     */
    private static ServerInterface singleton;

    /*
     * -------------------- Instance constants, mostly assigned in constructor. --------------------
     */
    private final PublicKey SERVER_KEY;
    private final String LOCAL_NUMBER;

    private final DataInputStream INPUT_STREAM;
    private final DataOutputStream OUTPUT_STREAM;

    private final BufferedReader BUFFERED_READER;
    private final BufferedWriter BUFFERED_WRITER;

    private final InputStream OBJECT_INPUT_STREAM;
    private final OutputStream OBJECT_OUTPUT_STREAM;

    private final RSAPrivateKey PRIVATE_KEY;
    private final RSAPublicKey PUBLIC_KEY;

    /**
     * Creates a new ServerInterface.
     * @param context The application context for SiKKr.
     * @throws IOException if connectivity to the server cannot be resolved.
     */
    private ServerInterface(Context context) throws IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        final Socket SOCKET, OBJECT_SOCKET, WRITE_SOCKET;
        final TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final KeyPair key = getKeyPair();

        PRIVATE_KEY = (RSAPrivateKey) key.getPrivate();
        PUBLIC_KEY = (RSAPublicKey) key.getPublic();

        LOCAL_NUMBER = tMgr.getLine1Number();
        SOCKET = new Socket(SERVER_IP, 997);
        OBJECT_SOCKET = new Socket(SERVER_IP, 998);
        WRITE_SOCKET = new Socket(SERVER_IP, 999);

        INPUT_STREAM = new DataInputStream(SOCKET.getInputStream());
        OUTPUT_STREAM = new DataOutputStream(SOCKET.getOutputStream());
        BUFFERED_READER = new BufferedReader(new InputStreamReader(WRITE_SOCKET.getInputStream()));
        BUFFERED_WRITER = new BufferedWriter(new OutputStreamWriter(WRITE_SOCKET.getOutputStream()));
        OBJECT_INPUT_STREAM = OBJECT_SOCKET.getInputStream();
        OBJECT_OUTPUT_STREAM = OBJECT_SOCKET.getOutputStream();


        SERVER_KEY = getServerKey();
        verify();
    }

    /*
     * -------------------- Static methods ---------------------------------------------------------
     */


    private static KeyPair generateKeyPair(File keyFile) {
        KeyPairGenerator keyGen;
        KeyPair key = null;
        ObjectOutputStream oos;

        if (keyFile.exists()) {
            keyFile.delete();
        }

        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(4096);
            key = keyGen.genKeyPair();

            if (!keyFile.getParentFile().exists()) {
                keyFile.getParentFile().mkdir();
            }

            keyFile.createNewFile();
            oos = new ObjectOutputStream(new FileOutputStream(keyFile));
            oos.writeObject(key);
        } catch (NoSuchAlgorithmException e) {
        } catch (IOException e) {
        }

        return key;
    }

    private static KeyPair getKeyPair() {
        final File keyFileDirectory = new File(".rsa/");
        final File keyFile = new File(keyFileDirectory, "sikkr_key_pair");
        KeyPair key = null;

        if (keyFile.exists()) {
            try {
                key = getKeyPairFromFile(keyFile);
            } catch (Exception e) {
                key = generateKeyPair(keyFile);
            }
        } else {
            generateKeyPair(keyFile);
        }
        return key;
    }

    private static KeyPair getKeyPairFromFile(File keyFile) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyFile));
        Object readObject = ois.readObject();
        ois.close();
        return (KeyPair) readObject;
    }

    /**
     * @return a list of new messages from the server.
     */
    public static List<Message> getNewMessages() {
        try {
            return singleton.getNewMessagesFromServer();
        } catch (Exception e) {
            return null;
        }
    }



    /**
     * Sends a message to somebody through the server.
     * @param number the number that you want to send the message to.
     * @param content the content of the message as a byte array.
     */
    public static void sendMessage(String number, byte[] content, int messageType) {
        singleton.sendMessageToServer(number, content, messageType);
    }

    public static boolean serverHasClient(String number) {
        try {
            return singleton.hasClient(number);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sets up the singleton.
     * @param context a context from the SiKKr application.
     */
    public static void setupSingleton(Context context) {
        if (context == null && singleton != null) {
            throw new UnsupportedOperationException("Cannot create instance of ServerInterface with a null context");
        } else {
            try {
                singleton = new ServerInterface(context.getApplicationContext());
            } catch (Exception e) {
                singleton = null;
                Log.e("ServerInterface", "Could not create instance of singleton");
            }
        }
    }

    /**
     * @return the singleton if one exists, or throws an exception if it does not.
     */
    private static ServerInterface getSingleton() {
        if (singleton != null) {
            return singleton;
        } else {
            throw new UnsupportedOperationException("The singleton has to be setup first");
        }
    }

    /*
     * -------------------- Instance methods -------------------------------------------------------
     */

    private byte[] aesDecrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec kSpec = new SecretKeySpec(key, "AES/CBC/PKCS5Padding");

        cipher.init(Cipher.DECRYPT_MODE, kSpec, new IvParameterSpec(iv));
        return cipher.doFinal(data);

    }

    private byte[] decrypt(byte[] bytes) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/None/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
        return cipher.doFinal(bytes);
    }

    private byte[] encrypt(byte[] bytes) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/None/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, SERVER_KEY);
        return cipher.doFinal(bytes);
    }

    private List<Message> getNewMessagesFromServer() throws Exception {
        final List<Message> messages = new ArrayList<Message>();
        final int n;

        writeLine("get_messages");

        n = INPUT_STREAM.readInt();

        for (int i = 0; i < n; i++) {
            final int ivLength = INPUT_STREAM.readInt();
            final int keyLength = INPUT_STREAM.readInt();
            final int senderNumberLength = INPUT_STREAM.readInt();
            final int contentLength = INPUT_STREAM.readInt();
            final byte[] encryptedKey = new byte[keyLength], key;
            final byte[] encryptedIV = new byte[ivLength], iv;
            final byte[] encryptedSenderNumber = new byte[senderNumberLength];
            final byte[] encryptedContent = new byte[contentLength];
            final byte[] content;
            final int type;
            final long time;
            final String senderNumber;
            final Message msg;

            INPUT_STREAM.readFully(encryptedIV);
            INPUT_STREAM.readFully(encryptedKey);
            INPUT_STREAM.readFully(encryptedSenderNumber);
            INPUT_STREAM.readFully(encryptedContent);

            iv = decrypt(encryptedIV);
            key = decrypt(encryptedKey);
            senderNumber = new String(aesDecrypt(encryptedSenderNumber, key, iv));
            content = aesDecrypt(encryptedContent, key, iv);
            type = INPUT_STREAM.readInt();
            time = INPUT_STREAM.readLong();


            msg = new Message(senderNumber, "0737721528", content, type, time);
            messages.add(msg);
        }

        return messages;
    }

    private PublicKey getServerKey() {
        try {
            writeLine("get_server_key");

            ObjectInputStream ois = new ObjectInputStream(OBJECT_INPUT_STREAM);
            return (PublicKey) ois.readObject();
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {

        }
        return null;
    }

    private VerificationType getVerificationMethod() throws IOException {
        String read = BUFFERED_READER.readLine();
        if (read.contains("verification_method ")) {
            return VerificationType.valueOf(read.replace("verification_method ", "").toUpperCase());
        } else {
            return VerificationType.INVALID;
        }
    }

    private void newClientVerification() throws ClassNotFoundException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(PUBLIC_KEY);
        OBJECT_OUTPUT_STREAM.write(baos.toByteArray());
        OBJECT_OUTPUT_STREAM.flush();
    }

    private void sendMessageToServer(String number, byte[] content, int messageType) {
        try {
            EncryptedMessage message = new EncryptedMessage(number.getBytes(), content);
            byte[] encryptedIV = encrypt(message.iv);
            byte[] encryptedKey = encrypt(message.aeskey);
            writeLine("send_message");
            OUTPUT_STREAM.writeInt(encryptedIV.length);
            OUTPUT_STREAM.writeInt(encryptedKey.length);
            OUTPUT_STREAM.writeInt(message.encryptedBytes[0].length);
            OUTPUT_STREAM.writeInt(message.encryptedBytes[1].length);
            OUTPUT_STREAM.writeInt(1);
            OUTPUT_STREAM.writeLong(1000);
            OUTPUT_STREAM.write(encryptedIV);
            OUTPUT_STREAM.write(encryptedKey);
            OUTPUT_STREAM.write(message.encryptedBytes[0]);
            OUTPUT_STREAM.write(message.encryptedBytes[1]);
            OUTPUT_STREAM.flush();

        } catch (Exception e) {
        }
    }

    private boolean hasClient(String number) throws Exception {
        EncryptedMessage msg = new EncryptedMessage(number.getBytes());
        byte[] iv = encrypt(msg.iv), key = encrypt(msg.aeskey);
        byte[] answerIv, answerKey, answer;
        int ivLength, keyLength, answerLength;

        writeLine("is_client");
        OUTPUT_STREAM.writeInt(iv.length);
        OUTPUT_STREAM.writeInt(key.length);
        OUTPUT_STREAM.writeInt(msg.encryptedBytes[0].length);

        OUTPUT_STREAM.write(iv);
        OUTPUT_STREAM.write(key);
        OUTPUT_STREAM.write(msg.encryptedBytes[0]);

        OUTPUT_STREAM.flush();

        ivLength = INPUT_STREAM.readInt();
        keyLength = INPUT_STREAM.readInt();
        answerLength = INPUT_STREAM.readInt();

        iv = new byte[ivLength];
        key = new byte[keyLength];
        answer = new byte[answerLength];

        INPUT_STREAM.readFully(iv);
        INPUT_STREAM.readFully(key);
        INPUT_STREAM.readFully(answer);

        iv = decrypt(iv);
        key = decrypt(key);
        answer = aesDecrypt(answer, key, iv);

        return Boolean.parseBoolean(new String(answer));
    }

    private void useVerificationCode() throws Exception {

        int ivLength = INPUT_STREAM.readInt();
        int keyLength = INPUT_STREAM.readInt();
        int length = INPUT_STREAM.readInt();
        byte[] readBytes = new byte[length];
        byte[] recievedIV = new byte[ivLength], iv, encryptedIV;
        byte[] recievedKey = new byte[keyLength], key, encryptedKey;
        byte[] decryptedBytes;
        EncryptedMessage message;

        INPUT_STREAM.readFully(recievedIV);
        INPUT_STREAM.readFully(recievedKey);
        INPUT_STREAM.readFully(readBytes);

        iv = decrypt(recievedIV);
        key = decrypt(recievedKey);
        decryptedBytes = aesDecrypt(readBytes, key, iv);
        message = new EncryptedMessage(decryptedBytes);
        encryptedIV = encrypt(message.iv);
        encryptedKey = encrypt(message.aeskey);

        OUTPUT_STREAM.writeInt(encryptedIV.length);
        OUTPUT_STREAM.writeInt(encryptedKey.length);
        OUTPUT_STREAM.writeInt(message.encryptedBytes[0].length);

        OUTPUT_STREAM.write(encryptedIV);
        OUTPUT_STREAM.write(encryptedKey);
        OUTPUT_STREAM.write(message.encryptedBytes[0]);
        OUTPUT_STREAM.flush();
    }

    private void verify() {
        try {
            writeLine("verify:"+LOCAL_NUMBER);
            switch (getVerificationMethod()) {
                case NEW_CLIENT:
                    newClientVerification();
                    break;
                case VERIFICATION_CODE:
                    useVerificationCode();
                    break;
                default:
            }
        } catch (Exception e) {

        }
    }

    private void writeLine(String string, boolean flush) throws IOException {
        BUFFERED_WRITER.append(string);
        BUFFERED_WRITER.newLine();
        if (flush) {
            BUFFERED_WRITER.flush();
        }
    }

    private void writeLine(String string) throws IOException {
        writeLine(string, true);
    }

    private enum VerificationType {
        NEW_CLIENT, VERIFICATION_CODE, INVALID;
    }

    public final static class Message {

        public final String SENDER, RECIEVER;
        public final byte[] CONTENT;
        public final int TYPE;
        public final long TIMESTAMP;

        public final static int TYPE_TEXT = 1;
        public final static int TYPE_DATA = 2;

        private Message(final String SENDER, final String RECIEVER, final byte[] CONTENT,
                       final int TYPE, final long TIMESTAMP) {
            this.SENDER = SENDER;
            this.RECIEVER = RECIEVER;
            this.CONTENT = CONTENT;
            this.TYPE = TYPE;
            this.TIMESTAMP = TIMESTAMP;
        }
    }

}
