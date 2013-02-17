package javaapplication5;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import voteapp.VotingClient;

/**
 * 
 * @author Anders
 */
public class WebsocketServer implements Runnable {

	private ServerSocket serverSocket;
	private Socket socket;
	public static ArrayList<VotingClient> clients;
	public static ArrayList<String> users;
	public static HashMap<String, Integer> votes;
	public static HashMap<String, String> clientVotes;

	public WebsocketServer() throws IOException {
		clients = new ArrayList<VotingClient>();
		serverSocket = new ServerSocket(2005);
		users = new ArrayList<String>();
		clientVotes = new HashMap<String, String>();
		votes = new HashMap<String, Integer>();
	}

	public static String addVoteFor(String username, String item) {
		if (clientVotes.containsKey(username))
			return clientVotes.get(username);
		clientVotes.put(username, item);
		if (votes.containsKey(item))
			votes.put(item, votes.get(item) + 1);
		else
			votes.put(item, 1);
		broadcast(getCurrentVotingState());
		try {
			FileOutputStream fileOut = new FileOutputStream("votes.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(votes);
			out.close();
			fileOut.close();
			fileOut = new FileOutputStream("clientvotes.ser");
			out = new ObjectOutputStream(fileOut);
			out.writeObject(clientVotes);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
		System.out.println(String
				.format("User %s voted for %s", username, item));
		return null;
	}
	public static boolean addUser(String user){
		if(users.contains(user))
			return false;
		users.add(user);
		try {
			FileOutputStream fileOut = new FileOutputStream("users.ser");
			for (String username : users) {
				fileOut.write(username.getBytes());
				fileOut.write('\n');
			}
			fileOut.flush();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
		return true;
	}

	public static JSONArray getVotesItems() {
		JSONArray list = new JSONArray();
		list.put("Mohamed Morsi");
		list.put("Ahmed Shafiq");
		list.put("Abulfotoh");
		list.put("Hamdeen");
		return list;
	}

	public static String getCurrentVotingState() {
		JSONArray v = new JSONArray();
		JSONArray obj = new JSONArray();
		JSONObject o = new JSONObject();
		Set<String> set = votes.keySet();
		for (String string : set) {
			v.put(string);
			obj.put(votes.get(string));
		}
		try {
			o.put("items", v);
			o.put("votes", obj);
		} catch (JSONException e) {
			System.out.println(e);
		}
		return o.toString();
	}

	public void start() {
		new Thread(this).start();
	}

	public static void broadcast(String msg) {
		for (VotingClient client : clients) {
			try {
				client.sendMessage(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void readStatistics() {
		try {
			FileInputStream fileIn = new FileInputStream("votes.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			votes = (HashMap<String, Integer>) in.readObject();
			in.close();
			fileIn.close();
			fileIn = new FileInputStream("users.ser");
			Scanner s = new Scanner(fileIn);
			users = new ArrayList<String>();
			while(s.hasNext())
				users.add(s.nextLine());
			fileIn.close();
			fileIn = new FileInputStream("clientvotes.ser");
			in = new ObjectInputStream(fileIn);
			clientVotes = (HashMap<String, String>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			return;
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return;
		}
	}

	@Override
	public void run() {
		System.out.println("Listening...");
		try {
			while (true) {
				socket = serverSocket.accept();
				System.out.println("Got connection");
				VotingClient client = new VotingClient(socket);
				if (client.handshake()) {
					clients.add(client);
					client.serve();
				} else {
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
		readStatistics();
		j.start();
	}
}