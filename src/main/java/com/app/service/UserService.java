package com.app.service;

import java.util.Optional;

import com.app.domain.User;

public interface UserService {

	// ==========================================
	// USER MANAGEMENT
	// ==========================================

	// Save User
	Long saveUser(User user);

	// Find by ID
	Optional<User> findById(Long id);

	// Find by Username
	Optional<User> findByUsername(String username);

	// Find by Email
	Optional<User> findByEmail(String email);

	// Find by Mobile
	Optional<User> findByMobile(String mobile);

	// Login using Username / Email / Mobile
	Optional<User> findByLoginValue(String value);

	// ==========================================
	// VALIDATION
	// ==========================================

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByMobile(String mobile);

	// ==========================================
	// PASSWORD
	// ==========================================

	void updateUserPwd(String pwd, Long userId);

	// ==========================================
	// OTP FEATURES
	// ==========================================

	boolean sendOtp(String value);

	boolean verifyOtp(String value, String otp);

	// ==========================================
	// CAPTCHA FEATURES
	// ==========================================

	String generateCaptcha(String sessionId);

	boolean verifyCaptcha(String sessionId, String userCaptcha);
}