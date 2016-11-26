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

public class GenSig {

    public static final String keystore_path = "keystore.jks";
    public static final String keystore_pass = "pass1234";

    public static void m(String[] args) {

	if (args.length < 4) {
	    System.err.println("Usage: sign keystorePath:keystorePass keyAlias KeyPassword file");
	} else try {

		// get keystore args
		String[] keystore_parts = args[1].split(":");
		String keystore_path  = keystore_parts[0];
		String keystore_pass  = keystore_parts[1];


		// get keystore instance
		KeyStore ks = KeyStore.getInstance("JKS");

		// check if keystore exists and load if it does
		if (! new File(keystore_path).exists()) {
		    System.err.println("Cannot find keystore at path: " + keystore_path);
		} else {
		    ks.load(new FileInputStream(keystore_path), keystore_pass.toCharArray());
		}

		
		// get key args
		String alias = args[2];
		String password = args[3];

		// try to load key from keystore
		Key privkey = ks.getKey(alias, password.toCharArray());

		// get a signature instance of type SHA1 with RSA
		Signature rsa = Signature.getInstance("SHA1withRSA");

		// initialize signing with private key privkey
		rsa.initSign((PrivateKey)privkey);

		// open file to sign for reading
		FileInputStream fis = new FileInputStream(args[4]);
		BufferedInputStream bufin = new BufferedInputStream(fis);

		byte[] buffer = new byte[1024];
		int len;
	
		// read and add file contents to signature
		while ((len = bufin.read(buffer)) >= 0) {
		    rsa.update(buffer, 0, len);
		};

		bufin.close();

		// get signature bytes from signing object
		byte[] realSig = rsa.sign();

		/* save the signature bytes in a file */
		FileOutputStream sigfos = new FileOutputStream(args[4] + ".sig");
		sigfos.write(realSig);
		sigfos.close();

	    } catch (Exception ex) {
		System.out.println("Error: " + ex.getMessage());
		ex.printStackTrace();
	    }
	
    }
    
}
