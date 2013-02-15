package javaapplication5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import voteapp.VotingClient;

/**
 * 
 * @author Anders
 */
public class WebsocketServer implements Runnable{

	private ServerSocket serverSocket;
	private Socket socket;
	public ArrayList<VotingClient> clients;
	
	
	public WebsocketServer() throws IOException {
		clients = new ArrayList<VotingClient>();
		serverSocket = new ServerSocket(2005);
	}
	
	public void start(){
		new Thread(this).start();
	}
	
	public void broadcast(String msg){
		for (VotingClient client : clients) {
			try {
				client.sendMessage(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		System.out.println("Listening...");
		try {
			while(true){
				socket = serverSocket.accept();
				System.out.println("Got connection");
				VotingClient client = new VotingClient(socket);
				if (client.handshake()) {
					clients.add(client);
					client.serve();
				}else{
					System.err.println("Failed to handshake a client");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException, NoSuchAlgorithmException {
		
		WebsocketServer j = new WebsocketServer();
		j.start();
	}
}