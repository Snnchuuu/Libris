package src;

public class AudioBook extends LibraryItem implements Searchable, Reviewable{	//Concrede class AudioBook extended from superclass LibraryItem and implemented from interfaces Searchable and Reviewable
	
	private int duration;	//Duration of the Audio Book
	private String narrator;	//Narrator of the Audio Book

	//Constructor of the AudioBook
	public AudioBook(int id, String title, String author, int publicationYear, int copyCount, String status, int duration, String narrator) {
		super(id, title, author, publicationYear, copyCount, status);
		setDuration(duration);
		this.narrator = narrator;
	}

	public int getDuration() {	//getter for duration
		return duration;
	}

	public void setDuration(int duration) {	//setter for duration
		if(duration<=0) {
			throw new IllegalArgumentException("Duration must be pozitive.");
		}
		this.duration = duration;
	}

	public String getNarrator() {	//getter for narrator
		return narrator;
	}

	public void setNarrator(String narrator) {	//setter for narrator
		this.narrator = narrator;
	}

	@Override
	public double calculatePenalty(int daysDelayed, int totalPreviousDelays) {	//Overridden method from superclass LibraryItem
		return 0.0;	//No penalty because an Audio Book can not be borrowed physically
	}

	@Override
	public void addReview(String comment, int rating) {	//Overridden method addReview from interface Reviewable
		System.out.println("Audio review saved: " + rating + " stars for narrator " + narrator);
	}

	@Override
	public void search(String query) {	//Overridden method from interface Searchable
		System.out.println("Searching audio library for title/narrator: " + query);
	}
	@Override
	public String toString() {
		return "AudioBook{"+super.toString()+ ", duration: "+duration+"min, narrator: "+narrator+ "}";
	}

}
