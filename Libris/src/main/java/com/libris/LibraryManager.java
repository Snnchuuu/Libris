package com.libris;

import java.util.ArrayList;	//Importing ArrayList
import java.util.List;	//Importing list
import java.time.LocalDate;	//Importing LocalDate for date calculations
import java.time.temporal.ChronoUnit; //Used to represent standard units of time such as days, hours, minutes, seconds

/*Info: 
 * List is an interface and it contains some methods for us to use but if we want to iplement the methods of list
 * we have to use ArrayList to do so.*/

public class LibraryManager {	//Start of class LibraryManager

	private List<LibraryItem> items;	//items is a List interface with given generic LibraryItem.
	private List<User> users;	//users is a List interface with given generic User.
	private List<BorrowRecord> borrowRecords;	//borrowRecords is a List interface with given generic BorrowRecord.
	private List<Notification> notifications;   //notifications is a List interface with given generic Notification.
	private int nextNotificationId;   //Counter for generating notification IDs
	
	public LibraryManager() {	//Constructor of our class
		this.items = new ArrayList<>();	//Creating an implementation of the interface
		this.users = new ArrayList<>();	//Creating an implementation of the interface
		this.borrowRecords = new ArrayList<>();	//Creating an implementation of the interface
		this.notifications = new ArrayList<>(); //Initialize notifications list
		this.nextNotificationId = 1;  //Start IDs from 1
		
	}
	
	public void addItems(LibraryItem item) {	//Method for adding the LibraryItem objects to our set
		items.add(item);
		System.out.println("Item added: "+ item.getTitle());
	}
	
	public void addUser(User user) {	//Method for adding the User objects to our set
		users.add(user);
		System.out.println("User added: " + user.getName());
	}
	
	public void borrowMaterial(Member member, LibraryItem item) {	//Method for borrowing operations
		if(!(item instanceof Borrowable)) {	//Checking if the given item is an example of the interface Borrowable
			System.err.println("Error! This item (" + item.getTitle() + ") is digital and cannot be borrowed physically!");
			//When the object is a digital object and cannot be borrowed physically
			return;
		}
		//If the item item is free to be borrowed:
		System.out.println("Processing borrow for: " + item.getTitle());
		
		BorrowRecord record = new BorrowRecord(member, borrowRecords.size() + 1, item, 14);
        borrowRecords.add(record);
        
        ((Borrowable) item).borrowItem();
	}

	//Overloaded borrowMaterial: enables to specify a specific loan period
	public void borrowMaterial(Member member, LibraryItem item, int loanPeriodDays) {
		if(!(item instanceof Borrowable)) {
			System.err.println("Error! This item (" +item.getTitle()+ ") is digital and cannot be borrowed physically!");
			return;
		}
		if(loanPeriodDays<=0) {
			throw new IllegalArgumentException("Loan period must be positive.");
		}
		System.out.println("Processing borrow for: " +item.getTitle()+ " (custom period: " +loanPeriodDays+ " days)");
		
		BorrowRecord record = new BorrowRecord(member, borrowRecords.size() + 1, item, loanPeriodDays);
		borrowRecords.add(record);
		
		((Borrowable) item).borrowItem();
	}
	
	public void returnMaterial(BorrowRecord record) {	//Method for returning the borrowed materials
	    record.setReturnDate(java.time.LocalDate.now());
	    
	    // Use the logic we wrote in BorrowRecord to find delay
	    int daysDelayed = record.calculateDaysDelayed();
	    
	    if (daysDelayed > 0) {
	    	
	        Member member = record.getMember();
	        LibraryItem item = record.getItem();
	        
	        //Calculating Penalty: item type + member's total delay history
	        double penaltyAmount = item.calculatePenalty(daysDelayed, member.getTotalDelays());
	        
	        // Apply the penalty to member's account
	        member.setBalance(member.getBalance() + penaltyAmount);
	        member.setTotalDelays(member.getTotalDelays() + 1);
	        
	        System.out.println("LATE RETURN: " + daysDelayed + " days late. Fine: " + penaltyAmount + " TL.");
	    } else {
	    	//If the material that is borrowed is back on time:
	        System.out.println("RETURNED ON TIME: No penalty.");
	    }
	    
	    if(record.getItem() instanceof Borrowable) {
	    	((Borrowable)record.getItem()).returnItem();
	    }
	}

	public void checkAndCreateNotifications() {
		LocalDate today = LocalDate.now();
		
		for (BorrowRecord record : borrowRecords) {
			if (record.getIsReturned()) {
				continue; //Skip returned items, no notification needed
			}
			Member member = record.getMember();
			LibraryItem item = record.getItem();
			long daysUntilDue = ChronoUnit.DAYS.between(today, record.getDueDate());
			
			Notification n = null;
			
			if (daysUntilDue > 1) {
				n = new Notification(nextNotificationId++, member, daysUntilDue+ " day(s) left to return: " +item.getTitle());
			} else if (daysUntilDue == 1) {
				n = new Notification(nextNotificationId++, member, "need to return until tomorrow "+item.getTitle());
			} else if (daysUntilDue == 0) {
				n = new Notification(nextNotificationId++, member, "Last day to return: "+item.getTitle());
			} else if (daysUntilDue < 0) {
				n = new Notification(nextNotificationId++, member, "You are " +Math.abs(daysUntilDue)+ " day(s) overdue: " +item.getTitle());
			}
			
			if (n != null) { //Only add if a notification was created
				notifications.add(n);
				System.out.println("Notification created: " +n.getMessage());
			}
		}
	}

	//Returns all notifications for a specific member
	public List<Notification> getNotificationsForMember(Member member) {
	    List<Notification> result = new ArrayList<>();
	    for (Notification n : notifications) {
	        if (n.getMember().equals(member)) {
	            result.add(n);
	        }
	    }
	    return result;
	}
}
