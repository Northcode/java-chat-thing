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

public class VerSig {

    public static void m(String[] args) {
	if (args.length < 5) {
	    System.out.println("Usage: verify keystorePath:keystorePassword keyAlias signatureFile fileToVerify");
	}
	else try{

		String[] keystore_parts = args[1].split(":");
		String keystore_path  = keystore_parts[0];
		String keystore_pass  = keystore_parts[1];

		String alias = args[2];

		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(keystore_path), keystore_pass.toCharArray());

		PublicKey pubKey = ks.getCertificate(alias).getPublicKey();

		/* input the signature bytes */
		FileInputStream sigfis = new FileInputStream(args[3]);
		byte[] sigToVerify = new byte[sigfis.available()]; 
		sigfis.read(sigToVerify);

		sigfis.close();

		/* create a Signature object and initialize it with the public key */
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initVerify(pubKey);

		/* Update and verify the data */

		FileInputStream datafis = new FileInputStream(args[4]);
		BufferedInputStream bufin = new BufferedInputStream(datafis);

		byte[] buffer = new byte[1024];
		int len;
		while (bufin.available() != 0) {
		    len = bufin.read(buffer);
		    sig.update(buffer, 0, len);
		};

		bufin.close();


		boolean verifies = sig.verify(sigToVerify);

		System.out.println("signature verifies: " + verifies);


	    } catch (Exception e) {
		System.err.println("Caught exception " + e.toString());
		e.printStackTrace();
	    }
    }
    
}
