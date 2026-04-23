package com.app.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.domain.User;
import com.app.repository.UserRepository;
import com.app.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

	private final UserRepository repo;
	private final BCryptPasswordEncoder passwordEncoder;

	private static final SecureRandom RANDOM = new SecureRandom();

	// OTP STORE (value -> OTP + expiry)
	private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

	// CAPTCHA STORE
	private final Map<String, String> captchaStore = new ConcurrentHashMap<>();

	public UserServiceImpl(UserRepository repo, BCryptPasswordEncoder passwordEncoder) {
		this.repo = repo;
		this.passwordEncoder = passwordEncoder;
	}

	// ================= USER SAVE =================
	@Override
	public Long saveUser(User user) {

		if (user == null) {
			throw new IllegalArgumentException("User cannot be null");
		}

		if (user.getPassword() == null || user.getPassword().isBlank()) {
			throw new IllegalArgumentException("Password cannot be empty");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		return repo.save(user).getId();
	}

	// ================= FIND =================
	@Override
	public Optional<User> findById(Long id) {
		return repo.findById(id);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return repo.findByUsername(username);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return repo.findByEmail(email);
	}

	@Override
	public Optional<User> findByMobile(String mobile) {
		return repo.findByMobile(mobile);
	}

	@Override
	public Optional<User> findByLoginValue(String value) {
		return repo.findByUsernameOrEmailOrMobile(value);
	}

	// ================= EXISTS =================
	@Override
	public boolean existsByUsername(String username) {
		return repo.existsByUsername(username);
	}

	@Override
	public boolean existsByEmail(String email) {
		return repo.existsByEmail(email);
	}

	@Override
	public boolean existsByMobile(String mobile) {
		return repo.existsByMobile(mobile);
	}

	// ================= PASSWORD =================
	@Override
	public void updateUserPwd(String pwd, Long userId) {

		if (!repo.existsById(userId)) {
			throw new RuntimeException("User not found with ID: " + userId);
		}

		repo.updateUserPwd(passwordEncoder.encode(pwd), userId);
	}

	// ================= SPRING SECURITY =================
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = findByLoginValue(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		String role = "ROLE_" + user.getRole().name();

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				Collections.singletonList(new SimpleGrantedAuthority(role)));
	}

	// ================= OTP =================

	@Override
	public boolean sendOtp(String value) {

		Optional<User> opt = findByLoginValue(value);

		if (opt.isEmpty()) {
			return false;
		}

		User user = opt.get();

		// ✅ ALWAYS USE EMAIL AS KEY (FIXED)
		String key = user.getEmail();

		String otp = generateOtp();

		// Expiry 5 minutes
		otpStore.put(key, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));

		System.out.println("OTP Sent To: " + key + " OTP: " + otp);

		return true;
	}

	@Override
	public boolean verifyOtp(String value, String otp) {

		OtpData data = otpStore.get(value);

		if (data == null) {
			return false;
		}

		// Check expiry
		if (LocalDateTime.now().isAfter(data.expiry)) {
			otpStore.remove(value);
			return false;
		}

		if (data.otp.equals(otp)) {
			otpStore.remove(value);
			return true;
		}

		return false;
	}

	// ================= CAPTCHA =================

	@Override
	public String generateCaptcha(String sessionId) {

		String captcha = randomAlphaNumeric(5);
		captchaStore.put(sessionId, captcha);
		return captcha;
	}

	@Override
	public boolean verifyCaptcha(String sessionId, String userCaptcha) {

		String stored = captchaStore.get(sessionId);

		if (stored != null && stored.equalsIgnoreCase(userCaptcha.trim())) {
			captchaStore.remove(sessionId);
			return true;
		}

		return false;
	}

	// ================= PRIVATE =================

	private String generateOtp() {
		return String.valueOf(RANDOM.nextInt(900000) + 100000);
	}

	private String randomAlphaNumeric(int len) {
		String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < len; i++) {
			sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
		}

		return sb.toString();
	}

	// ================= INNER CLASS =================
	private static class OtpData {
		String otp;
		LocalDateTime expiry;

		OtpData(String otp, LocalDateTime expiry) {
			this.otp = otp;
			this.expiry = expiry;
		}
	}
}