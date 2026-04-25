package com.libris;

public abstract class User {	//Abstract Class User
	private int id;	//User id
	private String name;	//User name
	private String email;	//User email address
	private String password;	//password of the use
	private String role;	//role of the user. Ex: Admin, Member
	
	//constructor for User
	public User(int id, String name, String email, String password, String role) {
		setId(id);
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
	}
	
	public int getId() {	//getter for id
		return id;
	}
	
	public void setId(int id) {	//setter for id
		if(id<0) {
			throw new IllegalArgumentException("User ID cannot be negative.");
		}
		this.id = id;
	}

	public String getName() {	//getter for name
		return name;
	}

	public void setName(String name) {	//setter for name
		this.name = name;
	}

	public String getEmail() {	//getter for email
		return email;
	}

	public void setEmail(String email) {	//setter for email
		this.email = email;
	}

	public String getPassword() {	//getter for password
		return password;
	}

	public void setPassword(String password) {	//setter for password
		this.password = password;
	}

	public String getRole() {	//getter for role
		return role;
	}

	public void setRole(String role) {	//setter for role
		this.role = role;
	}
	
	public abstract void showDashBoard();	//Abstract method showDashBoard. Will be overriden in concrede classes
	
	public abstract boolean login(String email, String password);	//For login system
	
	@Override
	public String toString() {
		return "id: "+id+ ", name: "+name+ ", email: "+email+ ", role: "+role;	
	}

}
