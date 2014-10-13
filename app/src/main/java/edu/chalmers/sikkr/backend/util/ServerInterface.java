package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Eric on 2014-10-13.
 */
public final class ServerInterface {

    /**
     * Singleton of ServerInterface
     */
    private static ServerInterface singleton;

    /*
     * -------------------- Instance constants, mostly assigned in constructor. --------------------
     */
    private final String SERVER_IP = "127.0.0.1";
    private final PublicKey SERVER_KEY;
    private final String LOCAL_NUMBER;
    private final Socket SOCKET, OBJECT_SOCKET, WRITE_SOCKET;

    private final InputStream INPUT_STREAM;
    private final OutputStream OUTPUT_STREAM;

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
        final TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final KeyPair key = getKeyPair();

        PRIVATE_KEY = (RSAPrivateKey) key.getPrivate();
        PUBLIC_KEY = (RSAPublicKey) key.getPublic();
        SERVER_KEY = getServerKey();

        LOCAL_NUMBER = tMgr.getLine1Number();
        SOCKET = new Socket(SERVER_IP, 997);
        OBJECT_SOCKET = new Socket(SERVER_IP, 998);
        WRITE_SOCKET = new Socket(SERVER_IP, 999);

        INPUT_STREAM = SOCKET.getInputStream();
        OUTPUT_STREAM = SOCKET.getOutputStream();
        BUFFERED_READER = new BufferedReader(new InputStreamReader(WRITE_SOCKET.getInputStream()));
        BUFFERED_WRITER = new BufferedWriter(new OutputStreamWriter(WRITE_SOCKET.getOutputStream()));
        OBJECT_INPUT_STREAM = OBJECT_SOCKET.getInputStream();
        OBJECT_OUTPUT_STREAM = OBJECT_SOCKET.getOutputStream();


        verify();
    }

    /*
     * -------------------- Static methods ---------------------------------------------------------
     */

    private static KeyPair getKeyPair() {
        KeyPairGenerator keyGen;
        KeyPair key = null;

        return key;
    }

    /**
     * @return a list of new messages from the server.
     */
    public static List<Message> getNewMessages() {
        //TODO
        return null;
    }

    private static PublicKey getServerKey() {
        return null;
    }

    /**
     * Sends a message to somebody through the server.
     * @param number the number that you want to send the message to.
     * @param content the content of the message as a byte array.
     */
    public static void sendMessage(String number, byte[] content) {
        //TODO
    }

    /**
     * Sets up the singleton.
     * @param context a context from the SiKKr application.
     */
    public static void setupSingleton(Context context) {
        if (context == null) {
            throw new UnsupportedOperationException("Cannot create instance of ServerInterface with a null context");
        } else {
            try {
                singleton = new ServerInterface(context.getApplicationContext());
            } catch (Exception e) {
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

    private void verify() {

    }


    public final static class Message {

        public final String SENDER, RECIEVER;
        public final byte[] CONTENT;
        public final int TYPE;
        public final Calendar TIMESTAMP;

        private Message(final String SENDER, final String RECIEVER, final byte[] CONTENT,
                       final int TYPE, final Calendar TIMESTAMP) {
            this.SENDER = SENDER;
            this.RECIEVER = RECIEVER;
            this.CONTENT = CONTENT;
            this.TYPE = TYPE;
            this.TIMESTAMP = TIMESTAMP;
        }
    }

}
