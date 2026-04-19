package src;

/**
 * Represents a user with administrative privileges.
 * Capable of managing materials, users, and viewing statistics.
 */

public class Admin extends User{	//Concrede class Admin extended from superclass User
	
	//Constructor for Admin
	public Admin(int id, String name, String email, String password) {
		super(id, name, email, password, "ADMIN");
		
	}

	@Override
	public void showDashBoard() {	//Overriden method from superclass User
		// Implementation for the Admin-specific dashboard (Vaadin UI logic)
		System.out.println("--- ADMIN MANAGEMENT PANEL ---");
        System.out.println("Admin: " + getName());
        System.out.println("1. Manage Materials\n2. Manage Members\n3. View Reports");
	}

	@Override
	public boolean login(String email, String password) {	//login function for admins
		if (this.getEmail().equals(email) && this.getPassword().equals(password)) {
            System.out.println("Admin Access Granted: System online.");
            return true;
        }
        return false;
	}
	
	
}
