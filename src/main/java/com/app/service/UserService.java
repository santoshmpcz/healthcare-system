package com.app.service;

import java.util.Optional;

import com.app.domain.User;

public interface UserService {

	Long saveUser(User user);

	Optional<User> findByUsername(String username);

	void updateUserPwd(String pwd, Long userId);

	// ✅ ADD THIS
	Optional<User> findById(Long id);

	boolean existsByUsername(String username);

}