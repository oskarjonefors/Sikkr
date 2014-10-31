package edu.chalmers.sikkr.backend.messages;

import java.util.Arrays;

/**
 * @author Eric Bjuhr
 */
public class StorableMessage {

    public final String SENDER, RECEIVER;
    public final byte[] CONTENT;
    public final long TIMESTAMP;
    public final boolean SENT;
    public final boolean READ;

    public StorableMessage(final String SENDER, final String RECEIVER, final byte[] CONTENT,
                           final long TIMESTAMP, final boolean SENT, final boolean READ) {
        this.SENDER = SENDER;
        this.RECEIVER = RECEIVER;
        this.CONTENT = CONTENT;
        this.TIMESTAMP = TIMESTAMP;
        this.SENT = SENT;
        this.READ = READ;
    }

    @Override
    public int hashCode() {
        return 17 * SENDER.hashCode() + 13 * RECEIVER.hashCode() + 23 * Arrays.hashCode(CONTENT) +
                (int) (11L * TIMESTAMP % Integer.MAX_VALUE) + (SENT ? 1 : 0);
    }



}
