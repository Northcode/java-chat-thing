package no.northcode.chatclient;

import no.northcode.chatclient.ChatProtocol;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import javax.net.ssl.*;

public class Client
{
    DataOutputStream toServer;
    DataInputStream fromServer;

    SSLSocket socket;

    Scanner input;

    public Client() {
	input = new Scanner(System.in);
    }

    public void connect(String host, String port, String keystore) {
	connect(host, Integer.parseInt(port),keystore);
    }
    
    public void connect(String host, int port, String keystore) {
	try {
	    String[] keystore_parts = keystore.split(":");
	    final String keystore_path = keystore_parts[0];
	    final String keystore_pass = keystore_parts[1];
	    
	    KeyStore ks = KeyStore.getInstance("JKS");
	    ks.load(new FileInputStream(keystore_path), keystore_pass.toCharArray());

	    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	    kmf.init(ks, keystore_pass.toCharArray());

	    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	    tmf.init(ks);

	    SSLContext sc = SSLContext.getInstance("TLS");
	    TrustManager[] trustManagers = tmf.getTrustManagers();
	    sc.init(kmf.getKeyManagers(), trustManagers, null);

	    SSLSocketFactory ssf = sc.getSocketFactory();
	    socket = (SSLSocket) ssf.createSocket(host,port);
	    // socket = new Socket(host,port);

	    System.out.println("Connected to: " + socket.getInetAddress().getCanonicalHostName());

	    toServer = new DataOutputStream(socket.getOutputStream());
	    fromServer = new DataInputStream(socket.getInputStream());
	    
	    Thread recievethread = new Thread(() -> {
		    try {
			// send handshake
			Random rand = new Random();
			int handshake = rand.nextInt(4096);

			toServer.writeInt(ChatProtocol.HANDSHAKE);
			toServer.writeInt(handshake);

			int type = fromServer.readInt();
			if (type != ChatProtocol.HANDSHAKE) {
			    throw new Exception("Invalid response from server, go die in a hole!");
			}
			
			int recieved = fromServer.readInt();

			if (handshake != recieved) {
			    throw new Exception("Invalid handshake, bailing out!");
			}
			    
			while (true) {
			    type = fromServer.readInt();

			    if (type == ChatProtocol.MESSAGE) {
				String username = fromServer.readUTF();
				String message = fromServer.readUTF();

				System.out.println(String.format("\r\n%s: %s", username, message));
			    }
			    Thread.sleep(250);
			}
		    } catch (IOException ex) {
			ex.printStackTrace();
		    } catch (InterruptedException ex) {
			ex.printStackTrace();
		    } catch (Exception ex) {
			ex.printStackTrace();
		    }
		});

	    Thread sendthread = new Thread(() -> {
		    try {
			while (true) {
			    // if (!input.hasNextLine()) {
			    // 	Thread.sleep(100);
			    // 	continue;
			    // }
			    System.out.print("> ");

			    String message = input.nextLine();

			    if (message.startsWith("/nick ")) {
				String newnick = message.substring(6);
				toServer.writeInt(ChatProtocol.NICK);
				toServer.writeUTF(newnick);
			    } else {
				toServer.writeInt(ChatProtocol.MESSAGE);
				toServer.writeUTF(message);
			    }
			    Thread.sleep(250);
			}
		    } catch (InterruptedException ex) {
			ex.printStackTrace();
		    } catch (IOException ex) {
			ex.printStackTrace();
		    }
		});

	    recievethread.start();
	    sendthread.start();
	    
	} catch (IOException ex) {
	    ex.printStackTrace();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    
}
