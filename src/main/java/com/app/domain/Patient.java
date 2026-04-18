package com.app.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "patient_tab")
public class Patient {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_seq_tab")
	@SequenceGenerator(name = "patient_seq_tab", sequenceName = "patient_seq_tab", allocationSize = 1)

	private Long id;

	@NotBlank(message = "First Name is required")
	@Column(name = "f_name", nullable = false)
	private String firstName;

	@Column(name = "m_name")
	private String middleName;

	@NotBlank(message = "Last Name is required")
	@Column(name = "l_name")
	private String lastName;

	@Pattern(regexp = "^(Male|Female|Other)$", message = "Invalid gender")
	@Column(name = "gender")
	private String gender;

	@NotNull(message = "Date of Birth is required")
	@Column(name = "dob")
	private LocalDate dateOfBirth;

	@Column(name = "marital_status")
	private String maritalStatus;

	@Column(name = "blood_group")
	private String bloodGroup;

	@NotBlank(message = "Aadhaar is required")
	@Pattern(regexp = "\\d{12}", message = "Aadhaar must be 12 digits")
	@Column(name = "aadhaar_no", unique = true, nullable = false)
	private String aadhaarNo;

	@Email(message = "Invalid email format")
	@Column(name = "email", length = 100)
	private String email;

	@Column(name = "diseases")
	private String diseases;

	@Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
	@Column(name = "mob_no", length = 10, unique = true, nullable = false)
	private String mobileNo;

	@Column(name = "ward_no")
	private Integer wardNo;

	@Column(name = "room_no")
	private Integer roomNo;

	@Column(name = "note")
	private String note;

	@Column(name = "history")
	private String history;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "patient_medical_history", joinColumns = @JoinColumn(name = "patient_id"))
	@Column(name = "medical_history")
	private Set<String> medicalHistory;

	@NotNull(message = "Bill amount is required")
	@Positive(message = "Bill amount must be positive")
	@Column(name = "bill_amount")
	private BigDecimal billAmount;

	@Column(name = "address", length = 500)
	private String address;

	@Column(name = "block")
	private String block;

	@Column(name = "district")
	private String district;

	@Column(name = "state")
	private String state;

	@Column(name = "nationality")
	private String nationality;

	@Column(name = "religion")
	private String religion;

	@Column(name = "country")
	private String country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
