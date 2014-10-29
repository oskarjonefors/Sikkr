package edu.chalmers.sikkr.backend.util;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

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
import java.security.Key;
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
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import edu.chalmers.sikkr.backend.ProgressListenable;
import edu.chalmers.sikkr.backend.messages.ServerMessage;
import edu.chalmers.sikkr.backend.messages.TheInbox;
import edu.chalmers.sikkr.backend.messages.VoiceMessage;

/**
 * Created by Eric on 2014-10-13.
 */
public final class ServerInterface implements ProgressListenable {

    private final static String SERVER_IP = "sikkr.ddns.net";
    private final static int standardPort = 1123;
    private final static int writePort = 1124;

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

    private final RSAPrivateKey PRIVATE_KEY;
    private final RSAPublicKey PUBLIC_KEY;

    private final Collection<ProgressListener> listeners;

    private final Socket SOCKET, WRITE_SOCKET;
    private final Context context;

    /**
     * Creates a new ServerInterface.
     * @param context The application context for SiKKr.
     * @throws IOException if connectivity to the server cannot be resolved.
     */
    private ServerInterface(Context context) throws IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        final TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final KeyPair key = getKeyPair(context);
        String localnbr = tMgr.getLine1Number();
        final MessageNotificationThread thread;

        PRIVATE_KEY = (RSAPrivateKey) key.getPrivate();
        PUBLIC_KEY = (RSAPublicKey) key.getPublic();


        if (localnbr == null || localnbr.isEmpty()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(context.getFilesDir(), "number"))));
            localnbr = reader.readLine();
            reader.close();
        }
        LOCAL_NUMBER = localnbr;
        SOCKET = new Socket(SERVER_IP, standardPort);
        WRITE_SOCKET = new Socket(SERVER_IP, writePort);

        Log.i("ServerInterface", "Connection established with server");

        INPUT_STREAM = new DataInputStream(SOCKET.getInputStream());
        OUTPUT_STREAM = new DataOutputStream(SOCKET.getOutputStream());
        BUFFERED_READER = new BufferedReader(new InputStreamReader(WRITE_SOCKET.getInputStream()));
        BUFFERED_WRITER = new BufferedWriter(new OutputStreamWriter(WRITE_SOCKET.getOutputStream()));

        Log.i("ServerInterface", "The streams to the server are open");

        try {
            final int waitDuration = 500; //Milliseconds
            Thread.sleep(waitDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SERVER_KEY = (PublicKey) getServerKey();
        INPUT_STREAM.readFully(new byte[INPUT_STREAM.available()]);
        verify();

        listeners = new ArrayList<>();
        this.context = context;
        thread = new MessageNotificationThread();
        thread.start();
    }

    /*
     * -------------------- Static methods ---------------------------------------------------------
     */


    private static KeyPair generateKeyPair(File publicKeyFile, File privateKeyFile) throws IOException {
        Log.d("ServerInterface", "Generating a key pair");
        KeyPairGenerator keyGen;
        KeyPair key = null;
        final int keySize = 4096;

        if (publicKeyFile.exists()) {
            if (!publicKeyFile.delete()) {
                throw new IOException("Could not delete existing public key file");
            }
        }

        if (privateKeyFile.exists()) {
            if (!privateKeyFile.delete()) {
                throw new IOException("Could not delete existing private key file");
            }
        }

        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize);
            key = keyGen.genKeyPair();

            if (!publicKeyFile.getParentFile().exists()) {
                if (!publicKeyFile.getParentFile().mkdirs()) {
                    throw new IOException("Could not create rsa key folder");
                }
            }

            if (!publicKeyFile.createNewFile()) {
                throw new IOException("Could not create public key file");
            }

            if (!privateKeyFile.createNewFile()) {
                throw new IOException("Could not create private key file");
            }

            saveByteDataToFile(publicKeyFile, key.getPublic().getEncoded());
            saveByteDataToFile(privateKeyFile, key.getPrivate().getEncoded());
        } catch (NoSuchAlgorithmException e) {
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

    private static KeyPair getKeyPair(Context context)  throws IOException {
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
    public static List<ServerMessage> getReceivedMessages() throws Exception {
        return getSingleton().getReceivedMessagesFromServer();
    }

    /**
     * Sends a message to somebody through the server.
     * @param number the number that you want to send the message to.
     * @param content the content of the message as a byte array.
     */
    public static void sendMessage(Activity toastActivity, String number, byte[] content, long time) {
        getSingleton().sendMessageToServer(toastActivity, number, content, time);
    }

    public static void sendVoiceMessage(Activity toastActivity, String number, VoiceMessage message) {
        try {
            LogUtility.toastInActivityThread(toastActivity, "Reading byte data from voice message", Toast.LENGTH_SHORT);
            byte[] content = readByteDataFromFile(new File(message.getFileUri().getPath()));
            LogUtility.toastInActivityThread(toastActivity, "Finished reading byte data from voice message", Toast.LENGTH_SHORT);
            sendMessage(toastActivity, number, content, message.getTimestamp().getTimeInMillis());
            LogUtility.toastInActivityThread(toastActivity, "Finished sending message data to server", Toast.LENGTH_SHORT);
        } catch (IOException e) {
            LogUtility.toastInActivityThread(toastActivity, "Could not send message via server", Toast.LENGTH_SHORT);
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
        } else if (context != null) {
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

    public static void addSingletonProgressListener(ProgressListener listener) {
        singleton.addProgressListener(listener);
    }

    @SuppressWarnings("unused")
    public static void removeSingletonProgressListener(ProgressListener listener) {
        singleton.removeProgressListener(listener);
    }

    /*
     * -------------------- Instance methods -------------------------------------------------------
     */

    private byte[] aesDecrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        Key kSpec = new SecretKeySpec(key, "AES/CBC/PKCS5Padding");

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

    private List<ServerMessage> getReceivedMessagesFromServer() throws Exception {
        writeLine("get_received_messages");
        return getMessagesFromServer(false);
    }

    private List<ServerMessage> getMessagesFromServer(boolean sent) throws Exception {
        final List<ServerMessage> messages = new ArrayList<>();
        final int n = INPUT_STREAM.readInt();
        final double numberOfOperations = 8D;



        double step = 1D / (n * numberOfOperations);

        for (int i = 0; i < n; i++) {
            final int ivLength = INPUT_STREAM.readInt();
            final int keyLength = INPUT_STREAM.readInt();
            final int senderNumberLength = INPUT_STREAM.readInt();
            final int receiverNumberLength = INPUT_STREAM.readInt();
            final int contentLength = INPUT_STREAM.readInt();
            final long time = INPUT_STREAM.readLong();
            final byte[] encryptedKey = new byte[keyLength], key;
            final byte[] encryptedIV = new byte[ivLength], iv;
            final byte[] encryptedSenderNumber = new byte[senderNumberLength];
            final byte[] encryptedReceiverNumber = new byte[receiverNumberLength];
            final byte[] encryptedContent = new byte[contentLength];
            final byte[] content;
            final String senderNumber;
            final String receiverNumber;
            final ServerMessage msg;

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


            msg = new ServerMessage(senderNumber, receiverNumber, content, time, sent);
            messages.add(msg);
            notifyListeners(step, "Loading " + (sent ? "sent " : "incoming") + "web messages");
        }

        return messages;
    }

    private Key getServerKey() {
        Log.i("ServerInterface", "Getting the server's public key");
        try {
            writeLine("get_server_key");
            Log.i("ServerInterface", "We have asked for the public key, waiting for response");
            int keyLength = INPUT_STREAM.readInt();
            byte[] keyBytes = new byte[keyLength];
            INPUT_STREAM.readFully(keyBytes);
            Log.i("ServerInterface", "We have received the public key from the server");
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    private VerificationType getVerificationMethod() throws IOException {
        int read = INPUT_STREAM.readInt();
        return VerificationType.values()[read];
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

    private void sendMessageToServer(Activity toastActivity, String number, byte[] content, long time) {
        try {
            ServerMessage savedMsg = new ServerMessage(LOCAL_NUMBER, number, content, time, true);
            EncryptedMessage message = new EncryptedMessage(number.getBytes(), content);
            byte[] encryptedIV = encrypt(message.iv);
            byte[] encryptedKey = encrypt(message.aeskey);
            VoiceMessageFileUtility.saveServerMessage(toastActivity, savedMsg);
            writeLine("send_message");
            OUTPUT_STREAM.writeInt(encryptedIV.length);
            OUTPUT_STREAM.writeInt(encryptedKey.length);
            OUTPUT_STREAM.writeInt(message.encryptedBytes[0].length);
            OUTPUT_STREAM.writeInt(message.encryptedBytes[1].length);
            OUTPUT_STREAM.writeLong(time);
            OUTPUT_STREAM.write(encryptedIV);
            OUTPUT_STREAM.write(encryptedKey);
            OUTPUT_STREAM.write(message.encryptedBytes[0]);
            OUTPUT_STREAM.write(message.encryptedBytes[1]);
            OUTPUT_STREAM.flush();

        } catch (Exception e) {
            LogUtility.toastInActivityThread(toastActivity, "Exception", Toast.LENGTH_SHORT);
            LogUtility.writeLogFile("ServerInterface", e);
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

        answerIv = new byte[ivLength];
        answerKey = new byte[keyLength];
        answer = new byte[answerLength];

        INPUT_STREAM.readFully(answerIv);
        INPUT_STREAM.readFully(answerKey);
        INPUT_STREAM.readFully(answer);

        answerIv = decrypt(answerIv);
        answerKey = decrypt(answerKey);
        answer = aesDecrypt(answer, answerKey, answerIv);

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

    private void writeLine(CharSequence line, boolean flush) throws IOException {
        BUFFERED_WRITER.append(line);
        BUFFERED_WRITER.newLine();
        if (flush) {
            BUFFERED_WRITER.flush();
        }
    }

    private void writeLine(CharSequence line) throws IOException {
        writeLine(line, true);
    }

    @Override
    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeProgressListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void notifyListeners(double progress, String taskMsg) {
        for (ProgressListener listener : listeners) {
            listener.notifyProgress(progress, "ServerInterface", taskMsg);
        }
    }

    private enum VerificationType {
        NEW_CLIENT, VERIFICATION_CODE, INVALID
    }

    private class MessageNotificationThread extends Thread {

        public MessageNotificationThread() {
            super("Message notification thread");
        }

        public void run() {
            final int sleepyTime = 500;
            String msg;

            while(WRITE_SOCKET.isConnected() && SOCKET.isConnected()) {
                try {
                    msg = BUFFERED_READER.readLine();

                    if (msg != null && TheInbox.getInstance() != null) {
                        TheInbox.getInstance().loadInbox(null);
                    }
                    sleep(sleepyTime);
                } catch (Throwable e) {
                    LogUtility.writeLogFile("MessageNotificationThread", e, context);
                }
            }
        }

    }


}
