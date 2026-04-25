package com.libris;

public class Book extends LibraryItem implements Borrowable, Searchable, Reviewable{	//Concrede class Book extended from class LibraryItem and implemented from interfaces Borrowable, Searchable, Reviewable

	private String isbn;	//International Standard Book Number
	private int pageCount;	//How much page does the book have
	private String genre;	//What is the genre of the book
	
	//Constructor for the class book
	public Book(int id, String title, String author, int publicationYear, int copyCount, String status, String isbn, int pageCount, String genre) {
		super(id, title, author, publicationYear, copyCount, status);
		this.isbn = isbn;
		setPageCount(pageCount);
		this.genre = genre;
	}


	public String getIsbn() {	//getter for isbn
		return isbn;
	}


	public void setIsbn(String isbn) {	//setter for isbn
		this.isbn = isbn;
	}


	public int getPageCount() {	//getter for pageCount
		return pageCount;
	}


	public void setPageCount(int pageCount) {	//setter for pageCount
		if(pageCount<=0) {
			throw new IllegalArgumentException("Page count must be positive.");
		}
		this.pageCount = pageCount;
	}


	public String getGenre() {	//getter for genre
		return genre;
	}


	public void setGenre(String genre) {	//setter for genre
		this.genre = genre;
	}
	
	@Override
	public double calculatePenalty(int daysDelayed, int totalPreviousDelays) {	//Overriden method from superclass to calculate the penalty cost
		double baseRate = 2.0; //2 TL for each day delayed
	    
	    double multiplier = 1.0 + (totalPreviousDelays * 0.1); //We wanted to have a multiplier for our penalty system
	    return daysDelayed * baseRate * multiplier;
	}

	@Override
	public void addReview(String comment, int rating) {	//Overridden merhod addReview from interface Reviewable
		System.out.println("Review added: " + rating + "/5 - " + comment);
		
	}
	@Override
	public void search(String query) {	//Overridden method search from interface Searchable
		System.out.println("Searching for book with: " + query);
		
	}
	@Override
	public void borrowItem() {	//Overridden method borrowItem from interface Borrowable
		System.out.println("Book borrowed: " + getTitle());
		
	}
	@Override
	public void returnItem() {	//Overridden method returnItem from interface Borrowable
		System.out.println("Book returned: " + getTitle());
		
	}
	@Override
	public String toString() {
		return "Book{" +super.toString()+ ",isbn: "+isbn+ ", pages: "+pageCount+ ", genre: "+genre+"}";
	}

}
