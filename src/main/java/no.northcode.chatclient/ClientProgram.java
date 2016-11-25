package no.northcode.chatclient;

import no.northcode.chatclient.Server;
import no.northcode.chatclient.Client;

class ClientProgram {

    public static void main(String[] args) {
	// GenSig.m(new String[] {"test.txt"});
	// VerSig.m(new String[] {"suepk","sig","test.txt"});
	Client client = new Client();
	if (args.length < 3) 
	    System.err.println("Usage: client.jar host port keystore:keystorePassword");
	else
	    client.connect(args[0],args[1],args[2]);
    }
}
