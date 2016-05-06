package app_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class TestClient {
	
	private static int BUFFER_SIZE = 4096;
	private static int SALT_SIZE = 32;
	private static int IV_SIZE = 16;
	private static byte[] encryptionBuffer = new byte[32];
	private static byte[] iv = new byte[IV_SIZE];
	private static byte[] salt = new byte[SALT_SIZE];
	private static String password = "I have to do something somewhat safe to store the password";

	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException, InvalidKeySpecException,
		NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
		InvalidParameterSpecException {
		try(Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 9002);
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream()){
			inputStream.read(encryptionBuffer);
			inputStream.read(salt);
			inputStream.read(iv);
			
			/* Derive the key, given password and salt. */
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
			
//			Cipher cipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
//			cipher2.init(Cipher.ENCRYPT_MODE, secretKey);
//			AlgorithmParameters params = cipher2.getParameters();
//			iv = params.getParameterSpec(IvParameterSpec.class).getIV();
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			String plaintext = new String(cipher.doFinal(encryptionBuffer), "UTF-8");
			
			outputStream.write(plaintext.getBytes());
			outputStream.flush();
			
			NetworkFileReceiver fileReceiver = new NetworkFileReceiver(inputStream, BUFFER_SIZE);
			fileReceiver.receiveAndWriteFiles();
			
		}

	}
	
	

}
