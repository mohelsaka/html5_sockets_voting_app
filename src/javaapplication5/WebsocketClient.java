package javaapplication5;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

public abstract class WebsocketClient {
	public static final int MASK_SIZE = 4;
	public static final String WEBSOCKET_KEY_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	// 1000 0001
	// ^		: means that the it's the final frame (not fragmented)
	//		^^^^: Opcode 1 so, it's a text-message frame UTF-8
	public static final int SINGLE_FRAME_UNMASKED = 0x81;
	
	
	protected BufferedOutputStream outstream;
	protected InputStream instream;
	protected Socket socket;
	
	public WebsocketClient(Socket socket) throws IOException {
		this.socket = socket;
		outstream = new BufferedOutputStream(socket.getOutputStream());
		instream = socket.getInputStream();
	}
	
	public abstract void serve();
	
	public String reiceveMessage() throws IOException {
		byte[] buf = readBytes(2);
		int opcode = buf[0] & 0x0F;
		
		// connection termination
		if (opcode == 8) {
			this.close();
			return null;
		} else {
			// assuming all messages comes from the client are text messages so the opcode = 1
			final int payloadSize = buf[1] & 0x7F;
			
			buf = readBytes(MASK_SIZE + payloadSize);
			
			// all messages that comes from the clients are masked
			buf = unMask(Arrays.copyOfRange(buf, 0, 4),	Arrays.copyOfRange(buf, 4, buf.length));
			return new String(buf);
		}
	}
	
	private byte[] readBytes(int numOfBytes) throws IOException {
		byte[] b = new byte[numOfBytes];
		instream.read(b);
		return b;
	}
	
	private byte[] unMask(byte[] mask, byte[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (data[i] ^ mask[i % mask.length]);
		}
		return data;
	}

	public void close() throws IOException{
		// Client want to close connection!
		System.out.println("Client closed!");
		
		outstream.flush();
		outstream.close();
		
		instream.close();
		
		socket.close();
	}

	public void sendMessage(String msg) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		baos.write(SINGLE_FRAME_UNMASKED);
		baos.write(msg.length());
		baos.write(msg.getBytes());
		
		baos.flush();
		baos.close();

		outstream.write(baos.toByteArray(), 0, baos.size());
		outstream.flush();
	}

	public boolean handshake() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(instream));

		HashMap<String, String> keys = new HashMap<String, String>();
		String str;
		
		// Reading client handshake headers
		while (!(str = in.readLine()).equals("")) {
			String[] s = str.split(": ");
			System.out.println(str);
			if (s.length == 2) {
				keys.put(s[0], s[1]);
			}
		}

		String hash;
		try {
			byte[] ar = MessageDigest.getInstance("SHA-1").digest((keys.get("Sec-WebSocket-Key") + WEBSOCKET_KEY_STRING).getBytes());
			hash = Base64.encodeBase64String(ar);
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			return false;
		}

		// Write handshake response
		outstream.write(("HTTP/1.1 101 Switching Protocols\r\n"
				+ "Upgrade: websocket\r\n" + "Connection: Upgrade\r\n"
				+ "Sec-WebSocket-Accept: " + hash + "\r\n" + "\r\n").getBytes());
		outstream.flush();
		
		return true;
	}
}
