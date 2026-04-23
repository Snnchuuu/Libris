package src;

public class EBook extends LibraryItem implements Searchable, Reviewable {	//Concrede class EBook extended from superclass LibraryItem and implemented from interfaces Searchable and Reviewable
	
	private String fileFormat;	//What is the format of the file
	private double fileSize;	//What is the size of the file

	//Constructor for EBook
	public EBook(int id, String title, String author, int publicationYear, int copyCount, String status, String fileFormat, double fileSize) {
		super(id, title, author, publicationYear, copyCount, status);
		this.fileFormat = fileFormat;
		setFileSize(fileSize);
	}

	public String getFileFormat() {	//getter for file format
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {	//setter for file format
		this.fileFormat = fileFormat;
	}

	public double getFileSize() {	//getter for file size
		return fileSize;
	}

	public void setFileSize(double fileSize) {	//setter for file size
		if(fileSize<=0) {
			throw new IllegalArgumentException("File size must be positive.");
		}
		this.fileSize = fileSize;
	}

	@Override
	public double calculatePenalty(int daysDelayed, int totalPreviousDelays) {	//overriden method calculatePenalty from superclass LibraryItem
		return 0.0;	//No penalty because you can not borrow an E-Book physically
	}

	@Override
	public void addReview(String comment, int rating) {	//Overridden method addReview from interface Reviewable
		System.out.println("Digital review added: " + rating + "/5 for eBook " + getTitle());
	}

	@Override
	public void search(String query) {	//Overridden method search from iterface Searchable
		System.out.println("Searching in digital catalog for: " + query);
	}
	@Override
	public String toString() {
		return "EBook{"+super.toString()+", format: "+fileFormat+ ", size: "+fileSize+ "MB}";
	}
	
}
