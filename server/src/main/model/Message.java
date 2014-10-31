package main.model;

import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {

	private static final long serialVersionUID = -3141530940855537081L;
	private final byte[] content;
	private final String sender, reciever;
	private final long timeInMillis;
	
	public Message(final byte[] content, final String sender, final String reciever, final long timeInMillis) {
		this.content = content;
		this.sender = sender;
		this.reciever = reciever;
		this.timeInMillis = timeInMillis;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public String getReciever() {
		return reciever;
	}
	
	public String getSender() {
		return sender;
	}
	
	public long getTimeInMillis() {
		return timeInMillis;
	}
	
	
	public int hashCode() {
		return 997 * Arrays.hashCode(content) + 2543 * sender.hashCode()
				+ 1087 * reciever.hashCode() + 23 * ((int) (timeInMillis % Integer.MAX_VALUE));
	}
	
}
