
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public final class EncryptedMessage {
	
	public final byte[][] encryptedBytes;
	public final byte[] aeskey;
	public final byte[] iv;
	
	public EncryptedMessage(byte[]... bytes) throws Exception {
		final KeyGenerator kGen = KeyGenerator.getInstance("AES");
		
		encryptedBytes = new byte[bytes.length][];
		SecretKey key = kGen.generateKey();
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec kSpec;
		aeskey = key.getEncoded();
		kSpec = new SecretKeySpec(aeskey, "AES/CBC/PKCS5Padding");
		
		
		cipher.init(Cipher.ENCRYPT_MODE, kSpec);
		iv = cipher.getIV();
		for (int i = 0; i < bytes.length; i++) {
			encryptedBytes[i] = cipher.doFinal(bytes[i]);
		}
	}
	
}
