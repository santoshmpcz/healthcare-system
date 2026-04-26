package com.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "doctor_tab")
public class Doctor {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doctor_seq_tab")
	@SequenceGenerator(name = "doctor_seq_tab", sequenceName = "doctor_seq_tab", allocationSize = 1)

	private Long id;

	@NotBlank(message = "First name is required")
	@Column(name = "f_name")
	private String firstName;

	@Column(name = "m_name")
	private String middleName;

	@NotBlank(message = "Last name is required")
	@Column(name = "l_name")
	private String lastName;

	@Column(name = "doc_code", unique = true)
	private String doctorCode;

	@Email(message = "Enter valid email")
	@Column(name = "doc_mail_col", unique = true)
	private String email;

	@Pattern(regexp = "[0-9]{10}", message = "Enter valid 10 digit mobile")
	@Column(name = "mobile")
	private String mobile;

	@NotNull(message = "Room number required")
	@Column(name = "room_no")
	private Integer roomNo;

	@Column(name = "doc_exp_col")
	private Integer experience;

	@Column(name = "doc_add_col")
	private String address;

	@Column(name = "hobbies")
	private String hobbies;

	@Positive(message = "Salary must be positive")
	@Column(name = "salary")
	private Double salary;

	@Column(name = "degree")
	private String degree;

	@Column(name = "gender")
	private String gender;

	@Column(name = "block")
	private String block;

	@Column(name = "district")
	private String district;

	@Column(name = "country")
	private String country;

	@Column(name = "nationality")
	private String nationality;

	@Column(name = "doc_note_col")
	private String note;

	/*
	 * @Column(name = "doc_img_col") private String photo;
	 */

	@Column(name = "doc_img_col",length = 500)
	private String imageUrl;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "spec_id_fk_col")
	private Specialization specialization;
}