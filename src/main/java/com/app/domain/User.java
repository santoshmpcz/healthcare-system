package com.app.domain;

import com.app.constraints.UserRoles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_tab")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_tab")
	@SequenceGenerator(name = "user_seq_tab", sequenceName = "user_seq_tab", allocationSize = 1)
	private Long id;

	@Column(name = "usr_display_name_col")
	private String displayName;

	@Column(name = "usr_uname_col", unique = true)
	private String username;

	@Column(name = "usr_email_col", unique = true)
	private String email;

	@Column(name = "usr_mobile_col", unique = true)
	private String mobile;

	@Column(name = "usr_pwd_col")
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "usr_urole_col")
	private UserRoles role;
}