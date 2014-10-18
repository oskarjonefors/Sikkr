package main.util;

import java.security.interfaces.RSAPublicKey;

import main.model.Contact;

public interface InformationListener {
	
	void addContact(Contact c);

	byte[] decrypt(byte[] input);
	
	Contact getContactByNumber(String number);

	int getPort();
	
	int getWritePort();
	
	RSAPublicKey getPublicKey();
	
	void sendInformation(InformationEvent e);

}
