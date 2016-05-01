package no.northcode.chatclient;

import no.northcode.chatclient.Server;
import no.northcode.chatclient.Client;

class ClientProgram {

    public static void main(String[] args) {
	Client client = new Client();
	client.connect("localhost",8070);
    }
}
