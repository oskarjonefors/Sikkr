package edu.chalmers.sikkr.backend.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.net.Uri;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;

import org.codehaus.stax2.XMLStreamReader2;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.chalmers.sikkr.backend.messages.Message;
import edu.chalmers.sikkr.backend.messages.ServerMessage;
import edu.chalmers.sikkr.backend.messages.VoiceMessage;

/**
 * Created by ivaldi on 2014-10-27.
 */
public class VoiceMessageFileUtility {

    private static String TAG = "VoiceMessageFileUtility";
    private static final XmlPullParserFactory inputFactory = getXmlPullParserFactory();

    private static XmlPullParserFactory getXmlPullParserFactory() {
        try {
            return XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            LogUtility.writeLogFile(TAG, e);
            return null;
        }
    }

    public static List<Message> readMessages(Context context) {
        File file =  new File("messages.xml");
        List<Message> messages = new ArrayList<Message>();


        try {
            if (file.exists()) {
                XmlPullParser reader = inputFactory.newPullParser();
                reader.setInput(new InputStreamReader(new FileInputStream(file)));

                int next;
                while ((next = reader.next()) != XmlPullParser.END_DOCUMENT) {
                    if (next == XmlPullParser.START_TAG
                            && reader.getName().equals("Message")) {
                        String sender = reader.getAttributeValue(0); //Sender
                        String receiver = reader.getAttributeValue(1); //Reciever
                        long time = Long.parseLong(reader.getAttributeValue(2)); //Timestamp
                        boolean sent = Boolean.parseBoolean(reader.getAttributeValue(3)); //sent
                        String path = reader.getAttributeValue(4); //Content path
                        messages.add(new Message(sender, receiver, Uri.fromFile(new File(context.getFilesDir(), "messages/" + path)), time, sent));
                    }
                }
            }
        } catch (Exception e) {
            LogUtility.writeLogFile(TAG, e);
        }
        return messages;
    }

    public static void saveServerMessage(Context context, ServerMessage message) {
        try {
            XmlSerializer writer = inputFactory.newSerializer();
            File contentDir = new File(context.getFilesDir(), "messages/");
            File contentFile;
            String path;
            writer.startTag("Messages", "Message");
            writer.setProperty("sender", message.SENDER);
            writer.setProperty("receiver", message.RECEIVER);
            writer.setProperty("time", message.TIMESTAMP);
            writer.setProperty("sent", message.SENT);
            writer.setProperty("content", path = getRandomContentPath(message));
            writer.endTag("Messages", "Message");

            if (!contentDir.exists()) {
                contentDir.mkdir();
            }

            contentFile = new File(contentDir, path + ".msg");
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(contentFile));
            dos.write(message.CONTENT);
            dos.flush();
            dos.close();
        } catch (IOException | XmlPullParserException e) {
            LogUtility.writeLogFile(TAG, e);
        }

    }

    private static String getRandomContentPath(ServerMessage message) {
        Random random = new Random();
        String path;
        File dir = new File("messages/");
        File tmp;
        do {
            random.setSeed(random.nextLong() * message.hashCode());
            path = ""+random.nextInt();
            tmp = new File(dir, path);
        } while (tmp.exists());
        return path;
    }

}
