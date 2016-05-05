package app_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestServer {

	public static void main(String[] args) throws UnknownHostException, IOException {
		
		
		ServerRunner serverRunner = new ServerRunner(9002, 0, InetAddress.getByName("127.0.0.1"));
		serverRunner.run();

	}

}
