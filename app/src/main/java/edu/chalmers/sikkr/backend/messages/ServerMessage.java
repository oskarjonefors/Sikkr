package edu.chalmers.sikkr.backend.messages;

import java.util.Arrays;

/**
 * Created by ivaldi on 2014-10-27.
 */
public class ServerMessage {

    public final String SENDER, RECEIVER;
    public final byte[] CONTENT;
    public final long TIMESTAMP;
    public final boolean SENT;

    public ServerMessage(final String SENDER, final String RECEIVER, final byte[] CONTENT,
                         final long TIMESTAMP, final boolean SENT) {
        this.SENDER = SENDER;
        this.RECEIVER = RECEIVER;
        this.CONTENT = CONTENT;
        this.TIMESTAMP = TIMESTAMP;
        this.SENT = SENT;
    }

    @Override
    public int hashCode() {
        return 17 * SENDER.hashCode() + 13 * RECEIVER.hashCode() + 23 * Arrays.hashCode(CONTENT) +
                (int) (11L * TIMESTAMP % Integer.MAX_VALUE) + (SENT ? 1 : 0);
    }



}
