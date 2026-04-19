package src;

public class Periodical extends LibraryItem implements Borrowable, Searchable, Reviewable{	//Concrede class Periodical extended from superclass LibraryItem and implemented from interfaces Borrowable, Searchable and Reviewable
	
	private int issueNumber;	//The specific edition or sequence number of the periodical
	private  String period;		//Shows the frequency of the publication

	//Constructor for class Periodical
	public Periodical(int id, String title, String author, int publicationYear, int copyCount, String status, int issueNumber, String period) {
		super(id, title, author, publicationYear, copyCount, status);
		setIssueNumber(issueNumber);
		this.period = period;
	}

	public int getIssueNumber() {	//getter for issueNumber
		return issueNumber;
	}

	public void setIssueNumber(int issueNumber) {	//setter for isNumber
		this.issueNumber = issueNumber;
	}

	public String getPeriod() {	//getter for period
		return period;
	}

	public void setPeriod(String period) {	//setter for period
		this.period = period;
	}

	@Override
	public double calculatePenalty(int daysDelayed, int totalPreviousDelays) { //Overriden method from superclass LibraryItem
	    double dailyRate = 5.0; // Rate for periodical items
	    
	    double penalty = daysDelayed * dailyRate;	//Calculating the penalty
	    
	    if (totalPreviousDelays > 5) {
	        penalty *= 1.5; //  If the total number of delays are more than 5 days then the client will have to pay more
	    }
	    
	    return penalty;
	}

	@Override
	public void addReview(String comment, int rating) {	//Overridden method addReview from interface Reviewable
		System.out.println("Periodical review added: " + rating + "/5 for Issue #" + getIssueNumber());
		
	}

	@Override
	public void search(String query) {	//Overridden method search from interface Searchable
		System.out.println("Searching periodicals for: " + query);
		
	}

	@Override
	public void borrowItem() {	//Overridden method borrowItem from interface Borrowable
		System.out.println("Periodical issue #" + issueNumber + " borrowed.");
		
	}

	@Override
	public void returnItem() {	//Overridden method returnItem from interface Borrowable
		System.out.println("Periodical issue #" + issueNumber + " returned.");
		
	}

}
