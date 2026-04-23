package src;

/**
 * Represents a regular library member.
 * Can borrow items, make reservations, write reviews, and maintain reading lists.
 */

public class Member extends User{	//Concrede class Member extended from superclass User
	
	private double balance;	//Total penalty amount to be paid
	private int totalDelays;//Needed for the penalty calculation

	//Constructor for payment
	public Member(int id, String name, String email, String password) {
		super(id, name, email, password, "MEMBER");
		setBalance(0.0);	
		setTotalDelays(0);	//When a new member is created their penalties and delays have to be 0 because of this we are making them 0 in the constructor
		
	}
	
	public double getBalance() {	//getter for balance
		return balance;
	}

	public void setBalance(double balance) {	//setter for balance
		this.balance = balance;
	}

	public int getTotalDelays() {	//getter for totalDelays
		return totalDelays;
	}
	public void setTotalDelays(int totalDelays) {	//setter for totalDelays
		this.totalDelays = totalDelays;
	}

	@Override
	public void showDashBoard() {	//Overridden method showDashBoard from superclass User
		// Implementation for the Member-specific dashboard (Vaadin UI logic)
		System.out.println("---MEMBER DASHBOARD---");
		System.out.println("User: " + getName());
		System.out.println("Penalty Balance: " + getBalance() + "TL");
		System.out.println("Total Past Delays: " + getTotalDelays());
		
	}

	@Override
	public boolean login(String email, String password) {	//Login function for members
		if(this.getEmail().equals(email) && this.getPassword().equals(password)) {
			System.out.println("Login Successful: Welcome to Libris, " + getName());
			return true;
		}
		System.out.println("Login Failed: Incorrect email or password.");
		return false;
	}
	
	@Override
	public String toString() {
		return "Member{"+super.toString()+ ", balance: "+balance+ "TL, Total Delays: "+totalDelays+ "}";
	}
	

}
