package voteapp;

import java.io.IOException;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javaapplication5.WebsocketClient;

public class VotingClient extends WebsocketClient{
	
	public String name;
	
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
						// {service: FFFFF, key1: value1, key2: value2 ...}
						/*
						 *  The user will request:
						 *  1- the list of available votes.
						 *  2- vote for a specific item in the voting list.
						 *  3- current votes.
						 *  
						 * */
						JSONObject jmsg = new JSONObject(msg);
						String service = jmsg.getString("service");
						// {service: 'getvotes'}
						if(service.equals("connect")){
							VotingClient.this.name = jmsg.getString("username");
							
							// TODO: check if this client has voted before or not.
							// if yes, send him his previous vote as he may want to change his mind!
							// if no, send him the list to vote on but don't send him the result of the votes.
							JSONObject o = new JSONObject();
							o.put("list", getVotesItems());
//							o.put("myvotes", "Sunday 10:00 am");
//							o.put("result", "[15, 20, 18, 20]");
							VotingClient.this.sendMessage(o.toString());
						}
						// e.g. {service: 'votefor', username: 'saka', item: 'Satday 03:00 pm'}
						else if(service.equals("votefor")){
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
