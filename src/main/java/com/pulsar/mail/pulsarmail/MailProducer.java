package com.pulsar.mail.pulsarmail;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageBuilder;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.client.impl.TypedMessageBuilderImpl;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;


import com.sun.mail.imap.IMAPFolder;

/**
 * @author Ganga Lakshmanasamy
 *  		Giggso
 *  ravi@giggso.com,ganga@giggso.com
 */
public class MailProducer {
	
	//Add all the pulsar client details to final variables

	private static final Logger log = Logger.getLogger("MailProducer.class");
	private static final String PULSAR_SERVICE_URL = "pulsar://localhost:6650";
	private static final String TOPIC_NAME = "email_topic";
	private static Producer<EmailData> producer= null;

	
	public void iniateProducer( String userName, Folder folder) throws Exception {

		/*
		 * Get the producer instance from the singleton method. There will be only one connection active
		 */
		Producer<EmailData> mailProducer = MailProducer.getProducer();
		
		//Add a interval in which the email server connection to be refreshed.
		long sleepInterval = 600000;
		
		//Use the folder connection shared from the app server
		IMAPFolder imapFolder = (IMAPFolder) folder;

		String uName = userName;

		long threadId = Thread.currentThread().getId();

		log.severe("thread id :: " + ((Long) threadId).toString());

		System.out.println("startign thread for imap");
		log.severe("startign thread for imap");
		imapFolder.addMessageCountListener(new MessageCountListener() {
			@Override
			public void messagesAdded(MessageCountEvent arg0) {
				log.severe("New message was added." + arg0.getMessages().length);
				javax.mail.Message msg[] = arg0.getMessages();
				for (javax.mail.Message message : msg) {
					try {
						ProcessMail processMail = new ProcessMail();
						// javax.mail.Message msg1 = null;
						System.out.println("Before processing");
						EmailData emailData = processMail.processEMail(message, userName);
						

						// Send each message
						try {

							  MessageId msgId = producer.send(emailData); 

							log.info("Published message with the ID " + msgId);
						} catch (PulsarClientException e) {
							log.severe(e.getMessage());
							e.printStackTrace();
						}
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void messagesRemoved(MessageCountEvent arg0) {

			}

		});
		// ExternalMailController externalMailController = new ExternalMailController();
		try {
			startListening(imapFolder, uName, resourceId, sleepInterval);
		} catch (RuntimeException e) {
			log.severe("Runtime Exception occured in mail sync. So reinitiating process for email :: " + uName);
			e.printStackTrace();
			return;
		} catch (Exception e) {
			log.severe("Exception occured in mail sync. So reinitiating process for email :: " + uName);
			e.printStackTrace();
			return;
		} catch (Throwable e) {
			log.severe("Throwable Exception occured in mail sync. So reinitiating process for email :: " + uName);
			e.printStackTrace();
			return;
		} finally {
			try {
				imapFolder.close(false);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("close thread for imap");
		log.severe("close thread for imap :: " + uName);

	}

	public void startListening(IMAPFolder imapFolder, String emailId, Long resourceId, long sleepInterval)
			throws MessagingException, RuntimeException {
		// We need to create a new thread to keep alive the connection
		Thread t = new Thread(new KeepAliveRunnable(imapFolder, emailId, sleepInterval), "IdleConnectionKeepAlive");

		t.start();

		while (!Thread.interrupted()) {
			System.out.println("Starting IDLE");
			try {
				log.severe("idle process");
				imapFolder.idle();
				log.severe("exiting idle");
			} catch (FolderClosedException e) {
				// if folder closed exception is thrown start the sync manually
				e.printStackTrace();
				
				//Write logic to reinitiate the sync process
				Thread.currentThread().interrupt();
		    	   return;
			} catch (StoreClosedException e) {
				// if folder closed exception is thrown start the sync manually
				e.printStackTrace();
				//Write logic to reinitiate the sync process
				Thread.currentThread().interrupt();
		    	   return;
			} catch (MessagingException e) {
				e.printStackTrace();
				//Write logic to reinitiate the sync process
				Thread.currentThread().interrupt();
		    	   return;
			}
		}

		log.severe("startListening email thread interrupted");
		// Shutdown keep alive thread
		
		 if (t.isAlive()) {
			 t.interrupt(); 
			}
		 
		throw new RuntimeException();

	}

	private static class KeepAliveRunnable implements Runnable {

		// private static final long KEEP_ALIVE_FREQ = 300000; // 5 minutes

		private IMAPFolder imapFolder;

		private String uName;

		private long keepAlive = 600000;

		public KeepAliveRunnable(IMAPFolder folder, String uName, long keepAlive) {
			this.imapFolder = folder;
			this.uName = uName;
			this.keepAlive = keepAlive;
		}

		@Override
		public void run() {

			while (!Thread.interrupted()) {
				try {
					log.severe("trying to keep the folder conn alive  :: " + uName);
					Thread.sleep(keepAlive);
					int count = imapFolder.getMessageCount();
				} catch (FolderClosedException e) {
					// if folder closed exception is thrown start the sync manually
					// e.printStackTrace();
					log.severe("Unexpected FolderClosedException exception while keeping alive the IDLE connection"
							+ uName);
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				} catch (StoreClosedException e) {
					// if folder closed exception is thrown start the sync manually
					// e.printStackTrace();
					log.severe("Unexpected StoreClosedException exception while keeping alive the IDLE connection"
							+ uName);
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				} catch (InterruptedException e) {
					log.severe("InterruptedException " + e.getMessage());
					// e.printStackTrace();
					log.severe("Unexpected InterruptedException exception while keeping alive the IDLE connection");
					Thread.currentThread().interrupt();
					// Ignore, just aborting the thread...
				} catch (MessagingException e) {
					// Shouldn't really happen...
					System.out.println("Unexpected exception while keeping alive the IDLE connection");
					// e.printStackTrace();
					log.severe(
							"Unexpected MessagingException exception while keeping alive the IDLE connection" + uName);
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}

			}
			System.out.println("keep alive exit");

		};
	}
	
	
	public static Producer getProducer() throws PulsarClientException {
		
		if(producer == null) {
			PulsarClient client = PulsarClient.builder().serviceUrl(PULSAR_SERVICE_URL).build();
	
			// You can set the producer specific topics here. You can add topic name and compression type
			producer = client.newProducer(Schema.AVRO(EmailData.class))
	                  // Set the topic
	                  .topic(TOPIC_NAME)
	                  // Enable compression
	                  .compressionType(CompressionType.LZ4)
	                  .create();
	
			//This producer can be used throughout the application
			log.info("Created producer for the topic " + TOPIC_NAME);
		}
		return producer;
	}
}
