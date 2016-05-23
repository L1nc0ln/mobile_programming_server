package app_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class ServerMain {
	
	public static volatile AtomicBoolean serverShutdown = new AtomicBoolean(false);
	
	public static void main(String[] args) {
		int port = 9002;
		int backLog = 0;
		InetAddress serverAdress;
		try {
			serverAdress = InetAddress.getByName("127.0.0.1");
		
			String password = "I have to do something somewhat safe to store the password";
			ConcurrentHashMap<String, String> fileHashes = new ConcurrentHashMap<String, String>();
			final String directory = "C://Music/A life divided";
			final int sleepTime = 10000;
			FileTracker fileTracker = new FileTracker();

			//start the thread doing the bookkeeping of the specified directory
			DirectoryUpdateRunner directoryUpdateRunner = new DirectoryUpdateRunner(directory, fileHashes, sleepTime, fileTracker);
			directoryUpdateRunner.start();

			//start the thread with the server socket that spawns threads that handle the connections
			ServerRunner serverRunner = new ServerRunner(port, backLog, serverAdress, password, fileTracker, directory);
			ServerSocket serverSocket = serverRunner.getServerSocket();
			serverRunner.start();
			
			//with this we can shut down the server
			try(Scanner scanner = new Scanner(System.in)){
				String readLine = "";
				while(true){
					readLine = scanner.nextLine();
					if(readLine.equals("shutdown")){
						System.out.println("Setting the shutdown signal now");
						serverShutdown.set(true);
						serverSocket.close();
						break;
					}
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	
}
