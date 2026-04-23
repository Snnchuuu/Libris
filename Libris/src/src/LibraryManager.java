package src;

import java.util.ArrayList;	//Importing ArrayList
import java.util.List;	//Importing list
import java.time.LocalDate;	//Importing LocalDate for date calculations

/*Info: 
 * List is an interface and it contains some methods for us to use but if we want to iplement the methods of list
 * we have to use ArrayList to do so.*/

public class LibraryManager {	//Start of class LibraryManager

	private List<LibraryItem> items;	//items is a List interface with given generic LibraryItem.
	private List<User> users;	//users is a List interface with given generic User.
	private List<BorrowRecord> borrowRecords;	//borrowRecords is a List interface with given generic BorrowRecord.
	
	public LibraryManager() {	//Constructor of our class
		this.items = new ArrayList<>();	//Creating an implementation of the interface
		this.users = new ArrayList<>();	//Creating an implementation of the interface
		this.borrowRecords = new ArrayList<>();	//Creating an implementation of the interface
		
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
		
		BorrowRecord record = new BorrowRecord(member, borrowRecords.size() + 1, item, null, 14);
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
}
