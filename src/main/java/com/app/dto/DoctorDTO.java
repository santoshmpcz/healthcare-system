package com.app.dto;

import java.io.Serializable;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * ========================================================== Doctor DTO
 * ==========================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DoctorDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	// ================= PRIMARY KEY =================
	private Long id;

	// ================= BASIC DETAILS =================

	@NotBlank(message = "First name is required")
	@Size(max = 50)
	private String firstName;

	@Size(max = 50)
	private String middleName;

	@NotBlank(message = "Last name is required")
	@Size(max = 50)
	private String lastName;

	@Size(max = 20)
	private String doctorCode;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Size(max = 100)
	private String email;

	@NotBlank(message = "Mobile required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Enter valid 10 digit mobile")
	private String mobile;

	@NotNull(message = "Room number required")
	@Positive(message = "Room number must be positive")
	private Integer roomNo;

	@PositiveOrZero(message = "Experience must be valid")
	private Integer experience;

	@Size(max = 255)
	private String address;

	@Size(max = 100)
	private String hobbies;

	@Positive(message = "Salary must be positive")
	private Double salary;

	@Size(max = 100)
	private String degree;

	@Size(max = 10)
	private String gender;

	@Size(max = 50)
	private String block;

	@Size(max = 50)
	private String district;

	@Size(max = 50)
	private String country;

	@Size(max = 50)
	private String nationality;

	@Size(max = 500)
	private String note;

	private String photo;

	// ================= SPECIALIZATION =================

	@NotNull(message = "Specialization is required")
	private Long specializationId;

	private String specializationName;

	// ================= UI META =================

	private String specializationIcon;
	private String specializationColor;

	// ================= SAFE GETTERS =================

	public String getFirstName() {
		return firstName != null ? firstName.trim() : "";
	}

	public String getLastName() {
		return lastName != null ? lastName.trim() : "";
	}

	public String getEmail() {
		return email != null ? email.trim() : "";
	}

	public String getMobile() {
		return mobile != null ? mobile.trim() : "";
	}

	public String getAddress() {
		return address != null ? address.trim() : "";
	}

	public String getSpecializationName() {
		return specializationName != null ? specializationName.trim() : "";
	}

	public String getSpecializationIcon() {
		return specializationIcon != null ? specializationIcon : "";
	}

	public String getSpecializationColor() {
		return specializationColor != null ? specializationColor : "";
	}
}