package main.model;

import java.io.Serializable;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;

public class Contact implements Serializable {

	private static final long serialVersionUID = 9215403420870555414L;
	
	private final String number;
	private final RSAPublicKey key;
	public final List<Message> sentMessages;
	public final List<Message> recievedMessages;

	public Contact(String number, RSAPublicKey key) {
		this.number = number;
		this.key = key;
		this.sentMessages = Collections.synchronizedList(new ArrayList<Message>());
		this.recievedMessages = Collections.synchronizedList(new ArrayList<Message>());
	}
	
	public String getNumber() {
		return number;
	}
	
	public byte[] encryptBytes(final byte[] input) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(input);
	}
	
	public byte[] getPublicKey() {
		return key.getEncoded();
	}
}
