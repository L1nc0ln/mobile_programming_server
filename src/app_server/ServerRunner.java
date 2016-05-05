package app_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerRunner  extends Thread implements Runnable {
	
	private ServerSocket serverSocket;
	private Socket currentClientSocket;
	
	public ServerRunner(int port, int backLog, InetAddress adress) throws IOException {
		serverSocket = new ServerSocket(port, backLog, adress);
	}

	@Override
	public void run() {
		while(true){
			try {
				currentClientSocket = serverSocket.accept();
				ConnectionHandlerRunner connectionHandler = new ConnectionHandlerRunner(currentClientSocket);
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
	}	
	

}
