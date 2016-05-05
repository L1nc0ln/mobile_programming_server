package app_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class ConnectionHandlerRunner extends Thread implements Runnable {
	
	private final int BUFFER_SIZE = 1024;
	private final int SALT_SIZE = 32;
	private final int IV_SIZE = 16;
	private final int ANSWER_SIZE = 26;
	private Socket clientSocket;
	private byte[] salt = new byte[SALT_SIZE];
	private byte[] iv = new byte[IV_SIZE];
	private byte[] clearTextAnswer = new byte[ANSWER_SIZE];
	private String secret;
	private int bytesRead;
	private byte[] readBuffer = new byte[BUFFER_SIZE];
	private byte[] message;

	@Override
	public void run() {
		try(InputStream inputStream = clientSocket.getInputStream();
				OutputStream outputStream = clientSocket.getOutputStream()){
			message = getEncryptedMessage("I have to do something somewhat safe to store the password");
			outputStream.write(message);
			outputStream.flush();
			outputStream.write(salt);
			outputStream.flush();
			outputStream.write(iv);
			outputStream.flush();
			bytesRead = inputStream.read(clearTextAnswer);
			if(evaluateAnswer(secret, clearTextAnswer)){
				System.out.println("works");
			} else{
				System.out.println("doesnt work");
			}
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException 
				| NoSuchPaddingException | InvalidParameterSpecException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
	}

	public ConnectionHandlerRunner(Socket clientSocket){
		this.clientSocket = clientSocket;
	}
	
	
	private byte[] getEncryptedMessage(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{
		/* create a random salt and a random key */
		final SecureRandom saltRandom = new SecureRandom();
		salt = new byte[32];
		saltRandom.nextBytes(salt);
		final SecureRandom secretRandom = new SecureRandom();
		secret = new BigInteger(130, secretRandom).toString(32);
		
		/* Derive the key, given password and salt. */
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
		
		/* Encrypt the message. */
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		AlgorithmParameters params = cipher.getParameters();
		iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		byte[] ciphertext = cipher.doFinal(secret.getBytes("UTF-8"));
		return ciphertext;
	}
	
	private boolean evaluateAnswer(String secret, byte[] answer){
		int counter = 0;
		for(byte currentByte: secret.getBytes()){
			if(answer[counter] != currentByte){
				return false;
			}
			counter++;
		}
		return true;
	}
}
