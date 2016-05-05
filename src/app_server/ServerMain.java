package app_server;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class ServerMain {
	
	public static volatile AtomicBoolean serverShutdown = new AtomicBoolean(false);
	
	public static void main(String[] args) {
		ConcurrentHashMap<String, String> fileHashes = new ConcurrentHashMap<String, String>();
		final String directory = "F://Tmp";
		FileHasher fileHasher = new FileHasher();
		FileTracker fileTracker = new FileTracker();
		DirectoryUpdateRunner directoryUpdateRunner = new DirectoryUpdateRunner(directory, fileHashes, 60000, fileHasher, fileTracker);
		directoryUpdateRunner.start();
		try(Scanner scanner = new Scanner(System.in)){
			String readLine = "";
			while(true){
				readLine = scanner.nextLine();
				if(readLine.equals("shutdown")){
					System.out.println("Setting the shutdown signal now");
					serverShutdown.set(true);
					break;
				}
			}
		}
	}
	
}
