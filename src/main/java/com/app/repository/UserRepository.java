package com.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	// ✅ FIXED (must match entity field exactly: username)
	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	// ✅ FIXED (parameter binding added)
	@Modifying
	@Query("UPDATE User u SET u.password = :encPwd WHERE u.id = :userId")
	void updateUserPwd(@Param("encPwd") String encPwd, @Param("userId") Long userId);

}