package voteapp;

import java.io.IOException;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javaapplication5.WebsocketClient;

public class VotingClient extends WebsocketClient{

	public VotingClient(Socket socket) throws IOException {
		super(socket);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void serve() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String msg;
				try {
					while((msg = reiceveMessage()) != null){
						// let's define our protocol,
						// Our protocol depends on JSON objects
						// {function: FFFFF, key1: value1, key2: value2 ...}
						/*
						 *  The user will request:
						 *  1- the list of available votes.
						 *  2- vote for a specific item in the voting list.
						 *  3- current votes.
						 *  
						 * */
						JSONObject jmsg = new JSONObject(msg);
						String function = jmsg.getString("function");
						// {function: 'getvotes'}
						if(function.equals("getvotes")){
							VotingClient.this.sendMessage(getVotesItems().toString());
						}
						// e.g. {function: 'votefor', username: 'saka', item: 'Satday 03:00 pm'}
						else if(function.equals("votefor")){
							String username = jmsg.getString("username");
							String item = jmsg.getString("item");
							addVoteFor(username, item);
						}else{
							System.out.println("Client said: " + msg);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static JSONArray getVotesItems(){
		JSONArray list = new JSONArray();
		list.put("Sunday 10:00 am");
		list.put("Monday 12:00 pm");
		list.put("Friday 01:00 pm");
		list.put("Satday 03:00 pm");
		return list;
	}
	
	public static String getCurrentVotingState(){
		JSONObject votes = new JSONObject();
		try {
			votes.put("Sunday 10:00 am", 10);
			votes.put("Monday 12:00 pm", 15);
			votes.put("Friday 01:00 pm", 12);
			votes.put("Satday 03:00 pm", 7);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return votes.toString();
	}
	
	public static void addVoteFor(String username, String item){
		System.out.println(String.format("User %s voted for %s", username, item));
	}
}
