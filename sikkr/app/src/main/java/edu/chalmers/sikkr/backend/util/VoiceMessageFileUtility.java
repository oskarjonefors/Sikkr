package edu.chalmers.sikkr.backend.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
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

    public static List<Message> readMessages() {
        LogUtility.writeLogFile(TAG, "Reading messages from xml file");
        File file =  new File(new File(getAppPath()), "messages.xml");
        List<Message> messages = new ArrayList<>();


        try {
            if (file.exists()) {
                XmlPullParser reader = inputFactory.newPullParser();
                reader.setInput(new InputStreamReader(new FileInputStream(file)));
                int next;
                while ((next = reader.next()) != XmlPullParser.END_DOCUMENT) {
                    LogUtility.writeLogFile(TAG, "Next type: "+next+"\tName: "+reader.getName());
                    if (next == XmlPullParser.START_TAG
                            && reader.getName().equals("Message")) {
                        String sender = reader.getAttributeValue(0); //Sender
                        String receiver = reader.getAttributeValue(1); //Receiver
                        long time = Long.parseLong(reader.getAttributeValue(2)); //Timestamp
                        boolean sent = Boolean.parseBoolean(reader.getAttributeValue(3)); //sent
                        String path = reader.getAttributeValue(4); //Content path
                        LogUtility.writeLogFile("ATTRS", "Sender: " + sender + " Receiver: " + receiver
                                + " Time: " + time);
                        messages.add(new Message(sender, receiver, Uri.fromFile(new File(getAppPath(), "messages/" + path + ".msg")), time, sent));
                    }
                }
            }
        } catch (Exception e) {
            LogUtility.writeLogFile(TAG, e);
        }
        return messages;
    }

    public static void saveVoiceMessage(VoiceMessage message, String receiver) {
        try {
            saveServerMessage(ServerInterface.convertToServerMessage(message, receiver));
        } catch (IOException e) {
            LogUtility.writeLogFile(TAG, e);
        }
    }

    public static void saveServerMessage(ServerMessage message) {
        try {
            LogUtility.writeLogFile(TAG, "Saving a message");
            XmlSerializer writer = inputFactory.newSerializer();
            Collection<Message> previousMessages = new ArrayList<>();
            File sikkrDirectory, file;
            String sikkopath = getAppPath();


            sikkrDirectory = new File(sikkopath);
            file = new File(sikkrDirectory, "messages.xml");

            if (sikkrDirectory.exists() && !sikkrDirectory.isDirectory()) {
                if (!sikkrDirectory.delete()) {
                    throw new IOException("Could not delete a misnamed file");
                }
            }

            if (!sikkrDirectory.exists()) {
                if (!sikkrDirectory.mkdirs()) {
                    throw new IOException("Could not create sikkr directory");
                }
            }

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Could not create a new file!");
                }
            } else {
                previousMessages.addAll(readMessages());
                if (!file.delete() || !file.createNewFile()) {
                    throw new IOException("Could not create a new file!");
                }
            }

            writer.setOutput(new OutputStreamWriter(new FileOutputStream(file)));
            writer.startDocument("UTF-8", true);
            writer.startTag("", "Messages");

            for (Message msg : previousMessages) {
                writer.startTag("", "Message");
                writer.attribute("", "sender", msg.getSender());
                writer.attribute("", "receiver", msg.getReceiver());
                writer.attribute("", "time", msg.getTimestamp().getTimeInMillis() + "");
                writer.attribute("", "sent", msg.isSent() + "");
                writer.attribute("", "content", msg.getFileUri().getLastPathSegment().replace(".msg", ""));
                writer.endTag("", "Message");
            }

            File contentDir = new File(sikkrDirectory, "messages/");
            File contentFile;
            String path;
            writer.startTag("", "Message");
            writer.attribute("", "sender", message.SENDER);
            writer.attribute("", "receiver", message.RECEIVER);
            writer.attribute("", "time", message.TIMESTAMP + "");
            writer.attribute("", "sent", message.SENT + "");
            writer.attribute("", "content", path = getRandomContentPath(message));
            writer.endTag("", "Message");
            writer.endDocument();
            writer.flush();

            if (contentDir.exists() && !contentDir.isDirectory()) {
                if (!contentDir.delete()) {
                    throw new IOException("Could not delete a misnamed file");
                }
            }

            if (!contentDir.exists()) {
                if (!contentDir.mkdir()) {
                    throw new IOException("Could not create content folder");
                }
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
            path = ""+random.nextLong();
            tmp = new File(dir, path);
        } while (tmp.exists());
        return path;
    }

    private static String getAppPath() {
        String path;
        if(Environment.getExternalStorageState().equals("mounted")){
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            path = Environment.getDataDirectory().getAbsolutePath();
        }
        return path + "/sikkr/";
    }

}
