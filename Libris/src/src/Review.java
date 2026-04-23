package src;

import java.time.LocalDate;	//Used to represent date and used in date calculations
//We have to add exception handling here later on!!!

public class Review {

	private int reviewId;	//Id of the review
	private User user;	//Associated user 
	private LibraryItem item;	//Associated item
	
	private int rating;	//Rating for the item
	private String comment;	//Written comment for the item
	private LocalDate reviewDate;	//Date of the review
	
	//Constructor for the class Review
	public Review(int reviewId, User user, LibraryItem item, int rating, String comment) {
		this.reviewId = reviewId;
		this.user = user;
		this.item = item;
		setRating(rating);
		this.comment = comment;
		this.reviewDate = LocalDate.now();
	}


	public int getReviewId() {	//getter for reviewId
		return reviewId;
	}


	public void setReviewId(int reviewId) {	//setter for reviewId
		this.reviewId = reviewId;
	}


	public User getUser() {	//getter for user
		return user;
	}


	public void setUser(User user) {	//setter for user
		this.user = user;
	}


	public LibraryItem getItem() {	//getter for item
		return item;
	}


	public void setItem(LibraryItem item) {	//setter for item
		this.item = item;
	}


	public int getRating() {	//getter for rating
		return rating;
	}


	public void setRating(int rating) {	//setter for item		
		if(rating<1 || rating>5){	
			throw new IllegalArgumentException("Rating must be between 1 and 5. Given: "+rating);
		}
		this.rating = rating;
	}
		
	
	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public LocalDate getReviewDate() {
		return reviewDate;
	}


	public void setReviewDate(LocalDate reviewDate) {
		this.reviewDate = reviewDate;
	}
	
	@Override
	public String toString() {
		return "Review{id: "+reviewId+ ", user: "+user.getName()+ ", item: "+item.getTitle()+ ", rating: "+rating+ "/5, comment: "+comment+ ", date: "+reviewDate+ "}";
	}
}
