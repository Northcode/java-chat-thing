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

		String[] keystore_parts = args[1].split(":");
		String keystore_path  = keystore_parts[0];
		String keystore_pass  = keystore_parts[1];

		KeyStore ks = KeyStore.getInstance("JKS");

		if (! new File(keystore_path).exists()) {
		    System.err.println("Cannot find keystore at path: " + keystore_path);
		} else {
		    ks.load(new FileInputStream(keystore_path), keystore_pass.toCharArray());
		}

		String alias = args[2];
		String password = args[3];

		Key privkey = ks.getKey(alias, password.toCharArray());
		PublicKey pubkey = ks.getCertificate(alias).getPublicKey();

		Signature rsa = Signature.getInstance("SHA1withRSA");
		rsa.initSign((PrivateKey)privkey);

		FileInputStream fis = new FileInputStream(args[4]);
		BufferedInputStream bufin = new BufferedInputStream(fis);

		byte[] buffer = new byte[1024];
		int len;
	
		while ((len = bufin.read(buffer)) >= 0) {
		    rsa.update(buffer, 0, len);
		};

		bufin.close();

		byte[] realSig = rsa.sign();

		/* save the signature in a file */
		FileOutputStream sigfos = new FileOutputStream(args[4] + ".sig");
		sigfos.write(realSig);
		sigfos.close();

		/* save the public key in a file */
		// byte[] key = pubkey.getEncoded();
		// FileOutputStream keyfos = new FileOutputStream("suepk");
		// keyfos.write(key);
		// keyfos.close();

	    } catch (Exception ex) {
		System.out.println("Error: " + ex.getMessage());
		ex.printStackTrace();
	    }
	
    }
    
}
