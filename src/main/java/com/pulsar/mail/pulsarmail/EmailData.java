package com.pulsar.mail.pulsarmail;

import java.io.Serializable;

public class EmailData  implements Serializable{
	
	/**
	 * @author Ganga Lakshmanasamy
	 *  		Giggso
	 */
	private static final long serialVersionUID = 1012833485490487246L;

	private String from;
	
	private String recipients;
	
	private String CC;
	
	private String subject;
	
	private int messageNumber;
	
	private String members;
	
	private String postedDate;
	
	private String feedText;
	
	private String body;
	
	private String rawString;
	
	private int isSupport;
	
	private String userName;
	
	private String plainText;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public String getCC() {
		return CC;
	}

	public void setCC(String cC) {
		CC = cC;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}

	public String getMembers() {
		return members;
	}

	public void setMembers(String members) {
		this.members = members;
	}

	public String getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(String postedDate) {
		this.postedDate = postedDate;
	}

	public String getFeedText() {
		return feedText;
	}

	public void setFeedText(String feedText) {
		this.feedText = feedText;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getRawString() {
		return rawString;
	}

	public void setRawString(String rawString) {
		this.rawString = rawString;
	}

	public int getIsSupport() {
		return isSupport;
	}

	public void setIsSupport(int isSupport) {
		this.isSupport = isSupport;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPlainText() {
		return plainText;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}
	
	
	

}
