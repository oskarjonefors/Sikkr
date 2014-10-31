package edu.chalmers.sikkr.backend.util;

import android.net.Uri;
import android.os.Environment;

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
 * @author Oskar Jönefors
 *
 * Reads and writes voice messages to file.
 *
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

    public static void markMessageAsRead(String fileName) {

        /* Strips away the file extension */
        final String fName = fileName.split("\\.")[0];
        saveServerMessage(null, fName);

    }

    /**
     * Read all the messages in from file. If a message with a file name matching the
     * given parameter is found, it will be marked as read before the list is returned.
     *
     * @param markReadMessageName  A file name without path and file extension. If this is null, or
     *                             no matching message is found, no messages will be altered
     *                             from the way they were previously saved.
     *
     * @return  A list of messages. This may be empty but never null.
     */
    public static List<Message> readMessages(String markReadMessageName) {
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
                        boolean isRead = false;

                        if (path != null && path.equals(markReadMessageName)) {
                            isRead = true;
                        } else if (reader.getAttributeCount() >= 5) {
                            isRead = Boolean.parseBoolean(reader.getAttributeValue(5));
                        }

                        messages.add(new Message(sender, receiver, Uri.fromFile(new File(getAppPath(),
                                "messages/" + path + ".msg")), time, sent, isRead));
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
        saveServerMessage(message, null);
    }

    public static void saveServerMessage(ServerMessage message, String markReadMessageName) {
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
                previousMessages.addAll(readMessages(markReadMessageName));
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
                writer.attribute("", "isread", msg.isRead() + "");
                writer.endTag("", "Message");
            }

            if (message != null) {
                File contentDir = new File(sikkrDirectory, "messages/");
                File contentFile;
                String path;
                writer.startTag("", "Message");
                writer.attribute("", "sender", message.SENDER);
                writer.attribute("", "receiver", message.RECEIVER);
                writer.attribute("", "time", message.TIMESTAMP + "");
                writer.attribute("", "sent", message.SENT + "");
                writer.attribute("", "content", path = getRandomContentPath(message));
                writer.attribute("", "isread", message.READ + "");
                writer.endTag("", "Message");

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
            }

            writer.endDocument();
            writer.flush();

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
