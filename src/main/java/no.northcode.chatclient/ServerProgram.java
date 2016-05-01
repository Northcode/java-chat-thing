package no.northcode.chatclient;

import no.northcode.chatclient.Server;
import no.northcode.chatclient.Client;

class ServerProgram {

    public static void main(String[] args) {
	Server server = new Server(8070);
	server.start();
    }
}
