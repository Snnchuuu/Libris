package src;

public abstract class LibraryItem {	//Starting point of abstract class LibraryItem
	private int id;	//Id number of the item
	private String title;	// Title of the item
	private String author;	//Author of the item
    private int publicationYear;	//The year which the item has been published
    private int copyCount;	//Count of the number of copies of the item
    private String status; // "Available", "Borrowed", "Reserved" etc.
    
    public LibraryItem(int id, String title, String author, int publicationYear, int copyCount, String status) {	//Constructor for LibraryItem
    	setId(id);
    	this.title = title;
    	this.author = author;	//All of the "this" keywords here implifies the instance variables of the class
    	setPublicationYear(publicationYear);	
    	setCopyCount(copyCount);
    	this.status = status;
    }
	public int getId() {	//Getter for id
		return id;
	}
	public void setId(int id) {	//Setter for id
		if(id<0) {
			throw new IllegalArgumentException("ID cannot be negative");
		}
		this.id = id;
	}
	public String getTitle() {	//Getter for title
		return title;
	}
	public void setTitle(String title) {	//Setter for title
		this.title = title;
	}
	public String getAuthor() {	//Getter for author
		return author;
	}
	public void setAuthor(String author) {	//Setter for author
		this.author = author;
	}
	public int getPublicationYear() {	//Getter for publicationYear
		return publicationYear;
	}
	public void setPublicationYear(int publicationYear) {	//Setter for publicationYear
		if(publicationYear<0 || publicationYear > java.time.LocalDate.now().getYear()) {
			throw new IllegalArgumentException("Invalid publication year: "+publicationYear); 
		}
		this.publicationYear = publicationYear;
	}
	public int getCopyCount() {	//Getter for copyCount
		return copyCount;
	}
	public void setCopyCount(int copyCount) {	//Setter for copyCount
		if(copyCount<0) {
			throw new IllegalArgumentException("Copy count cannot be negative.");
		}
		this.copyCount = copyCount;
	}
	public String getStatus() {	//Getter for status
		return status;
	}
	public void setStatus(String status) {	//Setter for status
		this.status = status;
	}
	//Will be overridden with polymorphism
    public abstract double calculatePenalty(int daysDelayed, int totalPreviousDelays);
    
    @Override
    public String toString() { //id title author year copies status
    	return "id: " +id+ ", title: "+title+ ", year: "+publicationYear+ ", copies: "+copyCount+", status: "+status; 
    }
    
}
