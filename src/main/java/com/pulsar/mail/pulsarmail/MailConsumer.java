package com.pulsar.mail.pulsarmail;

import java.util.logging.Logger;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;

/**
 * @author Ganga Lakshmanasamy
 *         Giggzo
 */
public class MailConsumer {
	
	//Add all the pulsar client details to final variables
	
    private static final Logger log = Logger.getLogger("MailConsumer.class");
    private static final String PULSAR_SERVICE_URL = "pulsar://localhost:6650";
    private static final String TOPIC_NAME = "email_topic";
    private static final String SUBSCRIPTION_NAME = "email-subscription";
    private static Consumer<EmailData> consumer = null;
    private static  PulsarClient client = null;

    
    public void consumer() throws Exception {
    	
    	Consumer<byte[]> mailConsumer = getConsumer();
        do {
            /*
             * Waiting for message to process
             */
        	Message<EmailData> msg = consumer.receive();

            
        	EmailData emailData = msg.getValue(); 
            
            //Here goes your message processing code
            
            // Acknowledge processing of the message so that it can be deleted
            mailConsumer.acknowledge(msg);
        } while (true);
    }
    
    /*
     * This method help you create a common consumer object which can be used across application
     */
    public Consumer getConsumer() throws PulsarClientException {
    	
    	if(consumer==null) {
 	       client = PulsarClient.builder()
 	                .serviceUrl(PULSAR_SERVICE_URL)
 	                .build();
 	        

 	        //Shared subscription allows multiple consumer to process the message sent from producer
 	       consumer = client.newConsumer(Schema.AVRO(EmailData.class))
 	                .topic(TOPIC_NAME)
 	                // Allow multiple consumers to attach to the same subscription
 	                // and get messages dispatched as a queue
 	                .subscriptionType(SubscriptionType.Shared)
 	                .subscriptionName(SUBSCRIPTION_NAME)
 	                .subscribe();
 	        
 	        log.info("Consumer created successfully. Topic name :: "+ TOPIC_NAME);
     	}
     	return consumer;
    }
}
