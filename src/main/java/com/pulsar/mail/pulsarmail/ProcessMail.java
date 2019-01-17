package com.pulsar.mail.pulsarmail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

/**
 * @author Ganga Lakshmanasamy
 *  		Giggso
 *  ravi@giggso.com,ganga@giggso.com
 */
public class ProcessMail {
	public EmailData processEMail(Message msg,String userName) throws MessagingException, IOException {
        /*
        if we don''t want to fetch messages already processed
        if (!msg.isSet(Flags.Flag.SEEN)) {
           String from = "unknown";
           ...
        }
      */
		
		EmailData emailData = new EmailData();
		  System.out.println("Inside process");
		  Map json = new HashMap<>();
      String from = "unknown";
      emailData.setFrom(InternetAddress.toString(msg.getFrom()));
      emailData.setRecipients(InternetAddress.toString(msg.getRecipients(Message.RecipientType.TO)));
      emailData.setCC(InternetAddress.toString(msg.getRecipients(Message.RecipientType.CC)));
      String subject = msg.getSubject();
      if(subject!=null && (subject.startsWith("RE:") || subject.startsWith("FW:") || subject.startsWith("Re:"))) {
      	subject = subject.replaceAll("RE:", "");
      	subject = subject.replaceAll("FW:", "");
      	subject = subject.replaceAll("Re:", "");
      	subject = subject.replaceAll("Fwd:", "");
      	subject = subject.trim();
      }
      
      if(subject!=null && !subject.equals("") && !subject.equalsIgnoreCase("null")) {
      	emailData.setSubject(subject);
      }
      else {
      	emailData.setSubject("nosubject");
      }
      
      emailData.setMessageNumber(msg.getMessageNumber());
     
      System.out.println("replyTo " + msg.getReplyTo());
      System.out.println("TO :::" + InternetAddress.toString(msg.getRecipients(Message.RecipientType.TO)));
      String toList = InternetAddress.toString(msg.getRecipients(Message.RecipientType.TO));
      String ccList = InternetAddress.toString(msg.getRecipients(Message.RecipientType.CC));
      System.out.println("toList " + toList);
      
      //Get the members involved in the mail chain
      List<String> recipients = new ArrayList<>();
      if(toList!=null) {
	        if(toList.contains(",")) {
	        	recipients =  new ArrayList<>(Arrays.asList(toList.split(",")));
	        }
	        else {
	        	recipients.add(toList);
	        }
      }
      
      if(ccList!=null) {
	        if(ccList.contains(",")) {
	        	List<String> cc = new ArrayList<>(Arrays.asList(ccList.split(",")));
	        	recipients.addAll(cc);
	        }
	        else {
	        	recipients.add(ccList);
	        }
      }
      
      Set<String> members = new TreeSet<String>();
      for(String data:recipients) {
      	if(data.contains("@")) {
      	if(data.contains("<") && data.contains(">")) {
      		members.add(data.substring(data.indexOf("<")+1,data.indexOf(">")).trim().toLowerCase());
      		}
      	else {
      			members.add(data.trim().toLowerCase());
      	}
      	}
      }
      
      System.out.println(Arrays.toString(members.toArray()));
      emailData.setMembers(Arrays.toString(members.toArray()));
      System.out.println("CC " + InternetAddress.toString(msg.getRecipients(Message.RecipientType.CC)));
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
      System.out.println("Date " + sdf.format(msg.getReceivedDate()));
      emailData.setPostedDate(sdf.format(msg.getReceivedDate()));
      if (msg.getReplyTo().length >= 1) {
        from = msg.getReplyTo()[0].toString();	          
      }
      else if (msg.getFrom().length >= 1) {
        from = msg.getFrom()[0].toString();
      }
      
      String filename = "c://temp/" +  subject;
      
      	json = saveParts(msg.getContent(), filename,json);
      	
      	 if(!json.containsKey("body")) {
	              emailData.setBody("no body");
	         }
         	
      	return emailData;
	  }
	
	 public Map saveParts(Object content, String filename,Map json)
			  throws IOException, MessagingException
			  {
			    OutputStream out = null;
			    InputStream in = null;
			    try {
			    	List attachmentList = new ArrayList();
			    	
			    	if(json.containsKey("attachments") && json.get("attachments")!=null) {
			    		attachmentList = (List)json.get("attachments");
			    	}
			      if (content instanceof Multipart) {
			        Multipart multi = ((Multipart)content);
			        int parts = multi.getCount();
			        System.out.println("parts count :: " + parts);
			        for (int j=0; j < parts; ++j) {
			          MimeBodyPart part = (MimeBodyPart)multi.getBodyPart(j);
			          
			          System.out.println(part.getContent().toString());
			          
			          if (part.getContent() instanceof Multipart) {
			              // part-within-a-part, do some recursion...
			          	  System.out.println("having attachment");
			              saveParts(part.getContent(), filename,json);
			            }
			          
			          if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			        	  Map<String,String> fileAttach = new HashMap<String, String>();
			        	  fileAttach.put("fileName",part.getFileName());
			        	  attachmentList.add(fileAttach);
			          }
			          else {
			        	  System.out.println("inside else ; " + part.getContentType());
			        	  System.out.println(part.getAllHeaders());
			        	  if(part.getContent()!=null) {
				        	  if (part.isMimeType("text/html")) {
				            	  json.put("body", part.getContent().toString());
					            }
				        	  if(part.isMimeType("text/Plain")) {
					            	
					            	 json.put("body", part.getContent().toString());
						      	 
					            }
			        	  }
			          }
			          
			         json.put("attachments", attachmentList);   	
			    	
			         }
			        	
			        }

			        else if(content instanceof String) {
			          json.put("body", content);
		           	  json.put("feedText", content);
		           	  json.put("rawString",content);
			      }
			      }
			    catch(Exception e) {
			    	e.printStackTrace();
			    }
			    finally {
			      if (in != null) { in.close(); }
			      if (out != null) { out.flush(); out.close(); }
			    }
			    
			    return json;
			  }

}
