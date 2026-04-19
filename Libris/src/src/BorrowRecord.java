package src;

import java.time.LocalDate;	//Used to represent date and used in date calculations
import java.time.temporal.ChronoUnit;	//Used to represent standard units of time such as days, hours, minutes, seconds

public class BorrowRecord {	//Starting point of class BorrowRecord
	private Member member;	//member object created from class Member who is associated
	private int recordId;	//Record Id for the transaction
	private LibraryItem item;	//Associated item such as book, periodical, etc.
	private LocalDate borrowDate;	//Borrowing date of the item
	private LocalDate dueDate;	//Calculated return date
	private LocalDate returnDate;	//Actual day of return (null if not returned)
	private boolean isReturned;	//Status flag for easy checking
	
	//Constructor for class BorrowRecord
	public BorrowRecord(Member member, int recordId, LibraryItem item, LocalDate returnDate, int loanPeriodDays) {
		setRecordId(recordId);
		this.member = member;
		this.item = item;
		this.borrowDate = LocalDate.now();
		this.dueDate = borrowDate.plusDays(loanPeriodDays);
		this.isReturned = false;
		
	}

	public Member getMember() {	//getter for member
		return member;
	}

	public void setMember(Member member) {	//setter for member
		this.member = member;
	}

	public int getRecordId() {	//getter for recordId
		return recordId;
	}

	public void setRecordId(int recordId) {	//setter for RecordId
		this.recordId = recordId;
	}

	public LibraryItem getItem() {	//getter for item
		return item;
	}

	public void setItem(LibraryItem item) {	//setter for item
		this.item = item;
	}

	public LocalDate getBorrowDate() {	//getter for bborrowDate
		return borrowDate;
	}

	public void setBorrowDate(LocalDate borrowDate) {	//setter for borrowDate
		this.borrowDate = borrowDate;
	}

	public LocalDate getDueDate() {	//getter for dueDate
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {	//setter for dueDate
		this.dueDate = dueDate;
	}

	public LocalDate getReturnDate() {	//getter for returnDate
		return returnDate;
	}

	public void setReturnDate(LocalDate returnDate) {	//setter for returnDate
		this.returnDate = returnDate;
		this.isReturned = true;	//Because the item is returned
	}

	public boolean getIsReturned() {	//getter for isReturned
		return isReturned;
	}

	public void setIsReturned(boolean isReturned) {	//setter for isReturned
		this.isReturned = isReturned;
	}
	
	
	//Calculates how many dates the member is late.
	//Essential for penalty calculations
	public int calculateDaysDelayed() {	
		if(getIsReturned() && getReturnDate() != null) {//Checking if the item is already returned
			if(getReturnDate().isAfter(getDueDate())) {
				return (int) ChronoUnit.DAYS.between(getDueDate(), getReturnDate());
			}
			
		}else if(!getIsReturned() && LocalDate.now().isAfter(getDueDate())) {	//If item is still out, check if today is past the due date
			return (int) ChronoUnit.DAYS.between(getDueDate(), LocalDate.now());
		}
		
		return 0;	//no delay if neither condition is met
	}
	
	
	
	
}
