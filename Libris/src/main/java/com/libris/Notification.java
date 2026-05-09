package com.libris;

import java.time.LocalDate;

public class Notification {
	
	private int notificationId;
	private Member member;
	private String message;
	private LocalDate notifyingDate;
	private boolean isRead;
	
	public Notification(int notificationId, Member member, String message) {
		setNotificationId(notificationId);
		this.member = member;
		setMessage(message);
		this.notifyingDate = LocalDate.now();
		this.isRead = false;
	}
	
	public int getNotificationId() {
		return notificationId;
	}
	
	public void setNotificationId(int notificationId) {
		if(notificationId<=0) {
			throw new IllegalArgumentException("Notification ID must be positie.");
		}
		this.notificationId = notificationId;
	}
	
	public Member getMember() {
		return member;
	}
	
	public void setMember(Member member) {
		this.member = member;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		if(message == null || message.isEmpty()) {
			throw new IllegalArgumentException("Notification message cannot be empty.");
		}
		this.message = message;
	}
	
	public LocalDate getNotifyingDate() {
		return notifyingDate;
	}
	
	public void setNotifyingDate(LocalDate notifyingDate) {
		this.notifyingDate = notifyingDate;
	}
	
	public boolean getIsRead() {
		return isRead;
	}
	
	public void setIsRead(boolean isRead) {
		this.isRead = isRead;
	}
	
	public void markAsRead() {
		this.isRead = true;
	}
	
	@Override
	public String toString() {
		return "Notification{id: "+notificationId+ ", member: "+member.getName()+ ", message: "+message+ ", date: "+notifyingDate+", read: "+isRead+ "}";
	}	

} 
