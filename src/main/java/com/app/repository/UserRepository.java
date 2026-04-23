package com.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	// ==========================================
	// FIND USER
	// ==========================================
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByMobile(String mobile);

	// ==========================================
	// CHECK USER EXISTS
	// ==========================================
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByMobile(String mobile);

	// ==========================================
	// LOGIN USING USERNAME / EMAIL / MOBILE
	// ==========================================
	@Query("""
			SELECT u
			FROM User u
			WHERE LOWER(u.username) = LOWER(:value)
			   OR LOWER(u.email) = LOWER(:value)
			   OR u.mobile = :value
			""")
	Optional<User> findByUsernameOrEmailOrMobile(@Param("value") String value);

	// ==========================================
	// UPDATE PASSWORD
	// ==========================================
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			UPDATE User u
			SET u.password = :encPwd
			WHERE u.id = :userId
			""")
	int updateUserPwd(@Param("encPwd") String encPwd, @Param("userId") Long userId);
}