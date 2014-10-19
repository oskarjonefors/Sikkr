package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import edu.chalmers.sikkr.backend.messages.Message;
import edu.chalmers.sikkr.backend.messages.VoiceMessage;

/**
 * Created by Eric on 2014-10-13.
 */
public final class ServerInterface {

    private final static String SERVER_IP = "46.239.104.32";

    /**
     * Singleton of ServerInterface
     */
    private static ServerInterface singleton;

    /*
     * -------------------- Instance constants, mostly assigned in constructor. --------------------
     */


    private final Context context;

    private final PublicKey SERVER_KEY;
    private final String LOCAL_NUMBER;

    private final DataInputStream INPUT_STREAM;
    private final DataOutputStream OUTPUT_STREAM;

    private final BufferedReader BUFFERED_READER;
    private final BufferedWriter BUFFERED_WRITER;

    private final RSAPrivateKey PRIVATE_KEY;
    private final RSAPublicKey PUBLIC_KEY;

    /**
     * Creates a new ServerInterface.
     * @param context The application context for SiKKr.
     * @throws IOException if connectivity to the server cannot be resolved.
     */
    private ServerInterface(Context context) throws IOException {
        this.context = context;
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        final Socket SOCKET, WRITE_SOCKET;
        final TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final KeyPair key = getKeyPair(context);

        PRIVATE_KEY = (RSAPrivateKey) key.getPrivate();
        PUBLIC_KEY = (RSAPublicKey) key.getPublic();

        //LOCAL_NUMBER = tMgr.getLine1Number(); //use this in release
        LOCAL_NUMBER = "1337"; //Genymotion special ;)
        SOCKET = new Socket(SERVER_IP, 997);
        WRITE_SOCKET = new Socket(SERVER_IP, 999);

        Log.i("ServerInterface", "Connection established with server");

        INPUT_STREAM = new DataInputStream(SOCKET.getInputStream());
        OUTPUT_STREAM = new DataOutputStream(SOCKET.getOutputStream());
        BUFFERED_READER = new BufferedReader(new InputStreamReader(WRITE_SOCKET.getInputStream()));
        BUFFERED_WRITER = new BufferedWriter(new OutputStreamWriter(WRITE_SOCKET.getOutputStream()));

        Log.i("ServerInterface", "The streams to the server are open");

        SERVER_KEY = getServerKey();
        INPUT_STREAM.readFully(new byte[INPUT_STREAM.available()]);
        verify();
    }

    /*
     * -------------------- Static methods ---------------------------------------------------------
     */


    private static KeyPair generateKeyPair(File publicKeyFile, File privateKeyFile) {
        Log.d("ServerInterface", "Generating a key pair");
        KeyPairGenerator keyGen;
        KeyPair key = null;

        if (publicKeyFile.exists()) {
            publicKeyFile.delete();
        }

        if (privateKeyFile.exists()) {
            privateKeyFile.delete();
        }

        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            key = keyGen.genKeyPair();

            if (!publicKeyFile.getParentFile().exists()) {
                publicKeyFile.getParentFile().mkdirs();
            }

            publicKeyFile.createNewFile();
            privateKeyFile.createNewFile();
            saveByteDataToFile(publicKeyFile, key.getPublic().getEncoded());
            saveByteDataToFile(privateKeyFile, key.getPrivate().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return key;
    }

    private static void saveByteDataToFile(File file, byte[] data) throws  IOException {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
        dos.write(data);
        dos.flush();
        dos.close();
    }

    private static byte[] readByteDataFromFile(File file) throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        byte[] read = new byte[dis.available()];
        dis.readFully(read);
        return read;
    }

    private static KeyPair getKeyPair(Context context) {
        final File keyFileDirectory = new File(context.getFilesDir(), "rsa/");
        final File publicKeyFile = new File(keyFileDirectory, "sikkr_pub_key");
        final File privateKeyFile = new File(keyFileDirectory, "sikkr_priv_key");
        KeyPair key;

        if (publicKeyFile.exists() && privateKeyFile.exists()) {
            try {
                key = getKeyPairFromFile(publicKeyFile, privateKeyFile);
            } catch (Exception e) {
                key = generateKeyPair(publicKeyFile, privateKeyFile);
            }
        } else {
            key = generateKeyPair(publicKeyFile, privateKeyFile);
        }
        return key;
    }

    private static KeyPair getKeyPairFromFile(File publicKeyFile, File privateKeyFile) throws Exception {
        Log.d("ServerInterface", "Getting the key pair from file");
        byte[] publicKey = readByteDataFromFile(publicKeyFile);
        byte[] privateKey = readByteDataFromFile(privateKeyFile);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(publicKey));
        RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        return new KeyPair(pubKey, privKey);
    }

    /**
     * @return a list of recieved messages from the server.
     */
    public static List<Message> getReceivedMessages() {
        try {
            return getSingleton().getReceivedMessagesFromServer();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return a list of sent messages from the server.
     */
    public static List<Message> getSentMessages() {
        try {
            return getSingleton().getSentMessagesFromServer();
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
        getSingleton().sendMessageToServer(number, content, messageType);
    }

    public static void sendTextMessage(String number, String text) {
        sendMessage(number, text.getBytes(), Message.TYPE_TEXT);
    }

    public static void sendVoiceMessage(String number, VoiceMessage message) {
        try {
            byte[] content = readByteDataFromFile(new File(message.getFileUri().getPath()));
            sendMessage(number, content, Message.TYPE_DATA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean serverHasClient(String number) {
        try {
            return getSingleton().hasClient(number);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sets up the singleton.
     * @param context a context from the SiKKr application.
     */
    public static void setupSingleton(Context context) {
        Log.i("ServerInterface", "Setting up an interface to the server");
        if (context == null && singleton != null) {
            throw new UnsupportedOperationException("Cannot create instance of ServerInterface with a null context");
        } else {
            try {
                singleton = new ServerInterface(context.getApplicationContext());
            } catch (Exception e) {
                singleton = null;
                Log.e("ServerInterface", "Could not create instance of singleton");
                e.printStackTrace();
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
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
        return cipher.doFinal(bytes);
    }

    private byte[] encrypt(byte[] bytes) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, SERVER_KEY);
        return cipher.doFinal(bytes);
    }

    private List<Message> getReceivedMessagesFromServer() throws Exception {
        writeLine("get_received_messages");
        return getMessagesFromServer(false);
    }

    private List<Message> getSentMessagesFromServer() throws Exception {
        writeLine("get_sent_messages");
        return getMessagesFromServer(true);
    }

    private List<Message> getMessagesFromServer(boolean sent) throws Exception {
        final List<Message> messages = new ArrayList<Message>();
        final int n;

        n = INPUT_STREAM.readInt();

        for (int i = 0; i < n; i++) {
            final int ivLength = INPUT_STREAM.readInt();
            final int keyLength = INPUT_STREAM.readInt();
            final int senderNumberLength = INPUT_STREAM.readInt();
            final int receiverNumberLength = INPUT_STREAM.readInt();
            final int contentLength = INPUT_STREAM.readInt();
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

            INPUT_STREAM.readFully(encryptedIV);
            INPUT_STREAM.readFully(encryptedKey);
            INPUT_STREAM.readFully(encryptedSenderNumber);
            INPUT_STREAM.readFully(encryptedReceiverNumber);
            INPUT_STREAM.readFully(encryptedContent);

            iv = decrypt(encryptedIV);
            key = decrypt(encryptedKey);
            senderNumber = new String(aesDecrypt(encryptedSenderNumber, key, iv));
            receiverNumber = new String(aesDecrypt(encryptedReceiverNumber, key, iv));
            content = aesDecrypt(encryptedContent, key, iv);
            type = INPUT_STREAM.readInt();
            time = INPUT_STREAM.readLong();


            msg = new Message(context, senderNumber, receiverNumber, content, type, time, sent);
            messages.add(msg);
        }

        return messages;
    }

    private RSAPublicKey getServerKey() {
        Log.i("ServerInterface", "Getting the server's public key");
        try {
            writeLine("get_server_key");
            Log.i("ServerInterface", "We have asked for the public key, waiting for response");
            int keyLength = INPUT_STREAM.readInt();
            byte[] keyBytes = new byte[keyLength];
            INPUT_STREAM.readFully(keyBytes);
            Log.i("ServerInterface", "We have received the public key from the server");
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
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

    private void newClientVerification() throws Exception {
        sendEncryptedDataToServer(PUBLIC_KEY.getEncoded());
    }

    private void sendEncryptedDataToServer(byte[]... bytes) throws Exception {
        Log.i("ServerInterface", "The server wants our public key");
        EncryptedMessage msg = new EncryptedMessage(bytes);
        byte[] encryptedIV = encrypt(msg.iv);
        byte[] encryptedKey = encrypt(msg.aeskey);

        OUTPUT_STREAM.writeInt(encryptedIV.length);
        OUTPUT_STREAM.writeInt(encryptedKey.length);
        for (byte[] data : msg.encryptedBytes) {
            OUTPUT_STREAM.writeInt(data.length);
        }

        OUTPUT_STREAM.write(encryptedIV);
        OUTPUT_STREAM.write(encryptedKey);

        for (byte[] data : msg.encryptedBytes) {
            OUTPUT_STREAM.write(data);
        }

        OUTPUT_STREAM.flush();
        Log.i("ServerInterface", "We gave the server our public key");
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
        Log.i("ServerInterface", "Verifying using verification code");
        int ivLength = INPUT_STREAM.readInt();
        int keyLength = INPUT_STREAM.readInt();
        int length = INPUT_STREAM.readInt();
        byte[] recievedIV = new byte[ivLength], iv, encryptedIV;
        byte[] recievedKey = new byte[keyLength], key, encryptedKey;
        byte[] readBytes = new byte[length], decryptedBytes;
        EncryptedMessage message;

        INPUT_STREAM.readFully(recievedIV);
        INPUT_STREAM.readFully(recievedKey);
        INPUT_STREAM.readFully(readBytes);

        Log.i("ServerInterface", "Received verification code");

        iv = decrypt(recievedIV);
        key = decrypt(recievedKey);

        Log.i("ServerInterface", "Key: "+new String(key)+"\tiv"+new String(iv));

        decryptedBytes = aesDecrypt(readBytes, key, iv);


        Log.i("ServerInterface", "Decrypted verification code");

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


        Log.i("ServerInterface", "Send our reply to the verification code");
    }

    private void verify() {
        try {
            writeLine("verify:"+LOCAL_NUMBER);
            Log.i("ServerInterface", "Getting verification method");
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
            e.printStackTrace();
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

}
