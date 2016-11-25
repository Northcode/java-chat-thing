package no.northcode.chatclient;

import no.northcode.chatclient.Server;
import no.northcode.chatclient.Client;

class ServerProgram {

    public static void main(String[] args) {
	if (args.length < 2) {
	    System.err.println("Usage: java -jar server.jar port keystore:keystorePass");
	    return;
	}
	Server server = new Server(Integer.parseInt(args[0]),args[1]);
	server.start();
    }
}
