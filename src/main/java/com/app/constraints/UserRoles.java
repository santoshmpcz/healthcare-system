package com.app.constraints;

public enum UserRoles {

	ADMIN,
	DOCTOR,
	NURSE,
	RECEPTIONIST,
	PATIENT;

	// ✅ Convert to display format (UI purpose)
	public String getDisplayName() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}
}