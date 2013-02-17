package voteapp;

import java.io.IOException;
import java.net.Socket;

import javaapplication5.WebsocketClient;
import javaapplication5.WebsocketServer;

import org.json.JSONException;
import org.json.JSONObject;

public class VotingClient extends WebsocketClient {

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
					while ((msg = reiceveMessage()) != null) {
						JSONObject jmsg = new JSONObject(msg);
						String service = jmsg.getString("service");
						JSONObject o = new JSONObject();
						if (service.equals("connect")) {
							o.put("list", WebsocketServer.getVotesItems());
							sendMessage(o.toString());
						}else if(service.equals("requestresults")){
							WebsocketServer.broadcast(WebsocketServer.getCurrentVotingState());
						}
						else if (service.equals("register")) {
							name = jmsg.getString("name");
							if (!WebsocketServer.addUser(name))
								o.put("response", "User already registered");
							else
								o.put("response",
										"User registered successfully");
							sendMessage(o.toString());
						} else if (service.equals("votefor")) {
							String username = jmsg.getString("username");
							String item = jmsg.getString("item");
							if (!WebsocketServer.users.contains(username))
								o.put("response",
										"you must register in the system first !");
							else {
								String ret = WebsocketServer.addVoteFor(
										username, item);
								if (ret != null)
									o.put("response", "already voted for" + ret);
								else
									o.put("response", "voted successfully for "
											+ item);
							}
							sendMessage(o.toString());
						} else {
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

}
