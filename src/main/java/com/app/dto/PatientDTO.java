package com.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import lombok.Data;

@Data
public class PatientDTO {

	private Long id;

	private String firstName;
	private String middleName;
	private String lastName;

	private String gender;
	private LocalDate dateOfBirth;
	private String maritalStatus;

	private String bloodGroup;
	private String aadhaarNo;

	private String email;
	private String mobileNo;

	private String diseases;
	private Set<String> medicalHistory;

	private Integer wardNo;
	private Integer roomNo;

	private String note;
	private String history;

	private BigDecimal billAmount;

	private String address;
	private String block;
	private String district;
	private String state;
	private String country;

	private String nationality;
	private String religion;

	private Long userId;
	// Optional (safe user info only)
	private String username; // from User
	private String role; // from User
}