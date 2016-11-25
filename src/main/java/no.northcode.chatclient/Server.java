package no.northcode.chatclient;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.Random;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import javax.net.ssl.*;

import no.northcode.chatclient.ChatProtocol;

public class Server
{
    int port;

    ArrayList<ServerClient> clients;
    ServerSocket serverSocket;

    String keystore_path, keystore_pass;

    public Server(int port, String keystore) {
	this.port = port;
	clients = new ArrayList<ServerClient>();

	String[] keystore_parts = keystore.split(":");
	keystore_path  = keystore_parts[0];
	keystore_pass  = keystore_parts[1];
    }

    public void start() {

	print("Starting server on port:" + port);

	Thread serverthread = new Thread(() -> {
		try {
		    // serverSocket = new ServerSocket(port);
	    
		    KeyStore ks = KeyStore.getInstance("JKS");
		    ks.load(new FileInputStream(keystore_path), keystore_pass.toCharArray());

		    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		    kmf.init(ks, keystore_pass.toCharArray());

		    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		    tmf.init(ks);

		    SSLContext sc = SSLContext.getInstance("TLS");
		    TrustManager[] trustManagers = tmf.getTrustManagers();
		    sc.init(kmf.getKeyManagers(), trustManagers, null);

		    SSLServerSocketFactory ssf = sc.getServerSocketFactory();
		    serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

		    while (true) {
			// wait for connection...
			SSLSocket clientSocket = (SSLSocket)serverSocket.accept();

			ServerClient client = new ServerClient(clientSocket, clients.size(),this);
			clients.add(client);
			print("New client connected from: " + client.getConnectionInfo());
			client.start();

		    }

		} catch (IOException ex) {
		    ex.printStackTrace();
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    });
	serverthread.start();
    }

    public void broadcastMessage(String message) {
	for (ServerClient client : clients) {
	    client.broadcastMessage(message);
	}
    }

    public void sendMessage(ServerClient from, String message) {
	for (ServerClient client : clients) {
	    if (client.getClientId() != from.getClientId()) {
		print(String.format("Sending message: %s to client %d", message, client.getClientId()));
		client.sendMessage(from,message);
	    }
	}
    }
    
    public void print(String text) {
	System.out.println(text);
    }

    class ServerClient {
	private SSLSocket socket;
	private Thread clientthread;
	private HandleClientThread clienthandler;
	private int clientid;
	private Server parent;

	public String nick;

	public ServerClient(SSLSocket clientSocket, int clientid, Server parent) {
	    this.socket = clientSocket;
	    this.clientid = clientid;
	    this.parent = parent;
	    this.nick = String.format("client%d",clientid);
	}

	public void start() {
	    if (clientthread == null) {
		clienthandler = new HandleClientThread(this);
		clientthread = new Thread(clienthandler);
	    }
	    clientthread.start();

	}

	public void sendMessage(ServerClient client,String message) {
	    clienthandler.sendMessage(client,message);
	}

	public void broadcastMessage(String message) {
	    clienthandler.broadcastMessage(message);
	}

	public Socket getSocket() {
	    return this.socket;
	}

	public int getClientId() {
	    return this.clientid;
	}

	public String getConnectionInfo() {
	    InetAddress addr = socket.getInetAddress();
	    return String.format("client: %d from: %s(%s)", clientid, addr.getHostName(), addr.getHostAddress());
	}
    }
    
    class HandleClientThread implements Runnable {

	private Socket socket;
	private ServerClient parent;

	private DataInputStream inputFromClient;
	private DataOutputStream outputToClient;

	private boolean recievedHandshake;

	public HandleClientThread(ServerClient parent) {
	    this.parent = parent;
	    this.socket = parent.getSocket();
	}

	public void sendMessage(ServerClient parent, String message) {
	    try {
		outputToClient.writeInt(ChatProtocol.MESSAGE);
		outputToClient.writeUTF(parent.nick);
		outputToClient.writeUTF(message);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}

	public void broadcastMessage(String message) {
	    try {
		outputToClient.writeInt(ChatProtocol.MESSAGE);
		outputToClient.writeUTF("server");
		outputToClient.writeUTF(message);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}

	public void run() {
	    recievedHandshake = false;
	    System.out.println(String.format("started thread for client: %d", parent.getClientId()));
	    try {
		inputFromClient = new DataInputStream(socket.getInputStream());
		outputToClient = new DataOutputStream(socket.getOutputStream());
		
		while (true) {
		    int msg_type = inputFromClient.readInt();

		    System.out.println(String.format("Got response: %d",msg_type));

		    if (msg_type == ChatProtocol.HANDSHAKE) {

			int handshake = inputFromClient.readInt();

			outputToClient.writeInt(ChatProtocol.HANDSHAKE);
			outputToClient.writeInt(handshake);
			
			recievedHandshake = true;
			parent.parent.broadcastMessage(String.format("Client %d joined", parent.getClientId()));
		    }
		    if (recievedHandshake) {
			if (msg_type == ChatProtocol.NICK) {
			    String nick = inputFromClient.readUTF();
			    String oldnick = parent.nick;
			    parent.nick = nick;
			    parent.parent.broadcastMessage(String.format("%s is now known as %s", oldnick, nick));
			} else if (msg_type == ChatProtocol.MESSAGE) {
			    String message = inputFromClient.readUTF();
			    System.out.println(String.format("Recieved message: %s, from client %d", message, parent.getClientId()));
			    parent.parent.sendMessage(parent, message);
			}
		    } else {
			System.out.println("Recieved message before handshake");
		    }
		    

		    Thread.sleep(250);
		}

	    } catch (EOFException ex) {
		System.out.println("Got EOF, client disconnected!");
	    } catch (IOException ex) {
		ex.printStackTrace();
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    } finally {
		System.out.println("trying to remove client");

		if(parent.parent.clients.remove(parent)) {
		    System.out.println("Sucess!");
		} else {
		    System.out.println("Failed!");
		    System.out.println("Server will most likely fail now because it will try to send a message to a client that doesn't exist....");
		}
	    }
	    
	}

    }
}
