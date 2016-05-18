package app_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerRunner  extends Thread implements Runnable {
	
	private ServerSocket serverSocket;
	private Socket currentClientSocket;
	private final String password;
	private FileTracker fileTracker;
	private String directory;
	
	public ServerRunner(int port, int backLog, InetAddress adress, String password, FileTracker fileTracker, String directory) throws IOException {
		serverSocket = new ServerSocket(port, backLog, adress);
		this.password = password;
		this.fileTracker = fileTracker;
		this.directory = directory;
	}

	@Override
	public void run() {
		while(true){
			try {
				currentClientSocket = serverSocket.accept();
				ConnectionHandlerRunner connectionHandler = new ConnectionHandlerRunner(currentClientSocket, password, fileTracker, directory);
				connectionHandler.run();
				if(ServerMain.serverShutdown.get()){
					break;
				}
			} catch(SocketException e){
				if(ServerMain.serverShutdown.get()){
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Serversocket shut down");
	}
	
	public ServerSocket getServerSocket(){
		return serverSocket;
	}
	

}
