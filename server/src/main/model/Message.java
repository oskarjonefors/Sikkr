package main.model;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = -3141530940855537081L;
	private final byte[] content;
	private final String sender, reciever;
	private final int type;
	private final long timeInMillis;
	
	public Message(final byte[] content, final String sender, final String reciever, final int type, final long timeInMillis) {
		this.content = content;
		this.sender = sender;
		this.reciever = reciever;
		this.type = type;
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
	
	public int getType() {
		return type;
	}
	
	public long getTimeInMillis() {
		return timeInMillis;
	}
	
	
	public int hashCode() {
		return 997 * content.hashCode() + 2543 * sender.hashCode() 
				+ 1087 * reciever.hashCode() + 23 * ((int) (timeInMillis % Integer.MAX_VALUE)) + 19 * type;
	}
	
}
