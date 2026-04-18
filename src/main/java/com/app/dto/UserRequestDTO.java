package com.app.dto;

import lombok.Data;

@Data
public class UserRequestDTO {

	private String displayName;
	private String username;
	private String password;
	private String role;

}