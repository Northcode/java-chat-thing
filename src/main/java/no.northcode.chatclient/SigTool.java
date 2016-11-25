package no.northcode.chatclient;

class SigTool {

    public static void main(String[] args) {

	if (args.length < 1) {
	    System.err.println("no action supplied, please run with either 'sign' or 'verify' as first argument");
	}

	String mode = args[0];

	if (mode.equals("sign")) {
	    GenSig.m(args);
	} else if(mode.equals("verify")) {
	    VerSig.m(args);
	}
	
    }
}
