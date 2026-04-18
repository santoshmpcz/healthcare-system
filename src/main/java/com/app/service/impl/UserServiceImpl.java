package com.app.service.impl;

import java.util.Collections;
import java.util.Optional;

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
public class UserServiceImpl implements UserService, UserDetailsService {

	private final UserRepository repo;
	private final BCryptPasswordEncoder passwordEncoder;

	// ✅ Constructor Injection (Best Practice)
	public UserServiceImpl(UserRepository repo, BCryptPasswordEncoder passwordEncoder) {
		this.repo = repo;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Long saveUser(User user) {

		String pwd = user.getPassword();

		if (pwd == null || pwd.isBlank()) {
			throw new IllegalArgumentException("Password cannot be empty");
		}

		String encPwd = passwordEncoder.encode(pwd);
		user.setPassword(encPwd);

		return repo.save(user).getId();
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return repo.findByUsername(username);
	}

	@Override
	@Transactional
	public void updateUserPwd(String pwd, Long userId) {

		if (!repo.existsById(userId)) {
			throw new RuntimeException("User not found with id: " + userId);
		}

		String encPwd = passwordEncoder.encode(pwd);
		repo.updateUserPwd(encPwd, userId);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		// ✅ FIXED (ENUM → STRING)
		String role = "ROLE_" + user.getRole().name();

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				Collections.singletonList(new SimpleGrantedAuthority(role)));
	}

	@Override
	public Optional<User> findById(Long id) {
		return repo.findById(id);
	}

	@Override
	public boolean existsByUsername(String username) {
		return repo.existsByUsername(username);
	}
}