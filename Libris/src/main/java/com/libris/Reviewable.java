package com.libris;

public interface Reviewable {
	
	void addReview(String comment, int rating); // Rating should be between 1-5 as per item
}
