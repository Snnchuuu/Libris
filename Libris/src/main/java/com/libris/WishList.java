package com.libris;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class WishList {
	
	private int listId;
	private Member member;
	private List<LibraryItem> items;
	private LocalDate createdDate;
	
	public WishList(int listId, Member member) {
		setListId(listId);
		this.member = member;
		this.items = new ArrayList<>(); //Start with an empty list
		this.createdDate = LocalDate.now();
	}
	
	public int getListId() {
		return listId;	
	}
	
	public void setListId(int listId) {
		if(listId<=0) {
			throw new IllegalArgumentException("List ID must be positive.");
		}
		this.listId = listId;
	}
	
	public Member getMember() {
		return member;
	}
	
	public void setMember(Member member) {
		this.member = member;
	}
	
	public List<LibraryItem> getItems(){
		return items;
	}
	
	public LocalDate getCreatedDate() {
		return createdDate;
	}
	
	public void addItem(LibraryItem item) {
		if(items.contains(item)) {
			System.out.println(item.getTitle() +" is already in the wishlist.");
			return;
		}
		items.add(item);
		System.out.println(item.getTitle() +" is added to "+member.getName()+ "'s wishlist.");
	}
	
	public void removeItem(LibraryItem item) {
		if (items.remove(item)) { // remove item first and then return true/false, (false means it wasn't on the list before)
			System.out.println(item.getTitle() + " removed from " +member.getName()+ "'s wishlist.");
		} else {
			System.out.println(item.getTitle() +" is not in the wishlist." );
		}
	}
	
	@Override
	public String toString() {
		return "WishList{id: " +listId+ ", member:  " +member.getName()+ ", items: "+items.size()+ ", created: " +createdDate+ "}";
	}

}
