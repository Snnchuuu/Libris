package com.libris;

/**
 * Represents a user with administrative privileges.
 * Capable of managing materials, users, and viewing statistics.
 */

public class Admin extends User{	//Concrede class Admin extended from superclass User
	
	//Constructor for Admin
	public Admin(int id, String username, String name, String email, String password) {
		super(id, username,  name, email, password, "ADMIN");
		
	}

	@Override
	public void showDashBoard() {	//Overriden method from superclass User
		// Implementation for the Admin-specific dashboard (Vaadin UI logic)
		System.out.println("--- ADMIN MANAGEMENT PANEL ---");
        System.out.println("Admin: " + getName());
        System.out.println("1. Manage Materials\n2. Manage Members\n3. View Reports");
	}

	@Override
	public boolean login(String username, String password) {
		if (this.getUsername().equals(username) && this.getPassword().equals(password)) {
			System.out.println("Admin Access Granted");
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Admin{"+super.toString()+ "}";
	}
	
	
}
