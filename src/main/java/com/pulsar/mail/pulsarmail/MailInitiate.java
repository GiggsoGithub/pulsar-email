package com.pulsar.mail.pulsarmail;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;


/**
 * @author Ganga Lakshmanasamy
 *  		Giggso
 *  ravi@giggso.com,ganga@giggso.com
 */
public class MailInitiate {
		
		 public static void main(String[] args) throws IOException {
	    	 Folder folder = null;
	 	    Store store = null;
	 	    Session session = null;
	        try {
	        	
	        	System.out.println("Inside producer");

	        	Properties props = new Properties();
	            props.put("mail.imap.ssl.enable", "true"); // required for Gmail
	            props.put("mail.imap.auth.mechanisms", "XOAUTH2");
	        session = Session.getInstance(props);
	        session.setDebug(false);
	        store = session.getStore("imap");
	        store.connect("imap.gmail.com", "<<emailId>>", "<<accesstoken>>");
	        
		      
		      System.out.println(store.isConnected());
	        folder = store.getFolder("Inbox");
	      /* Others GMail folders :
	       * [Gmail]/All Mail   This folder contains all of your Gmail messages.
	       * [Gmail]/Drafts     Your drafts.
	       * [Gmail]/Sent Mail  Messages you sent to other people.
	       * [Gmail]/Spam       Messages marked as spam.
	       * [Gmail]/Starred    Starred messages.
	       * [Gmail]/Trash      Messages deleted from Gmail.
	       */
	      folder.open(Folder.READ_WRITE);
	      
	      Folder imapFolder = folder;
	      new Thread(() -> {

	      	MailProducer mailProducer = new MailProducer();
	      	try {
	      		mailProducer.iniateProducer("emailId", imapFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	}).start();
	      
	      new Thread(() -> {


	      	MailConsumer mailConsumer = new MailConsumer();
	      	try {
	      		mailConsumer.consumer();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      	}).start();
	    	
	        }
	        catch(Exception e) {
	        	e.printStackTrace();
	        }
	    }
}
