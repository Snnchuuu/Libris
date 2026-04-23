package src;

import java.time.LocalDate;	//Used to represent date and used in date calculations

public class Reservation {//Starting point of the class Reservation

	private int reservationId;	//Id of the reservation
	private Member member;	//Associated member 
	private LibraryItem item;	//Associated item
	private LocalDate requestDate;	//Date of the request
	private String status;	//Status of the reservation such as Pending, Fulfilled, Cancelled
	
	//Constructor of the class Reservation
	public Reservation(int reservationId, Member member, LibraryItem item, String status) {
		setReservationId(reservationId);
		this.member = member;
		this.item = item;
		this.requestDate = LocalDate.now();
		this.status = status;
	}

	public int getReservationId() {	//getter for reservationId
		return reservationId;
	}

	public Member getMember() {	//getter for member
		return member;
	}

	public LibraryItem getItem() {	//getter for item
		return item;
	}

	public LocalDate getRequestDate() {	//getter for requestDate
		return requestDate;
	}

	public String getStatus() {	//getter for status
		return status;
	}

	public void setReservationId(int reservationId) {	//setter for reservationId
		if(reservationId<=0) {
			throw new IllegalArgumentException("Reservation ID must be positive.");
		}
		this.reservationId = reservationId;
	}

	public void setMember(Member member) {	//setter for member
		this.member = member;
	}

	public void setItem(LibraryItem item) {	//setter for item
		this.item = item;
	}

	public void setRequestDate(LocalDate requestDate) {	//setter for requestDate
		this.requestDate = requestDate;
	}

	public void setStatus(String status) {	//setter for status
		this.status = status;
	}
	
	public boolean isActive() {	//checks if the reservation is still active
		return "Pending".equals(this.status);
	}
	
	@Override
	public String toString() {
		return "Reservation{id: "+reservationId+ ", member: "+member.getName()+ ", item: "+item.getTitle()+ ", date: "+requestDate+ ", status: "+status+ "}";
	}
}
