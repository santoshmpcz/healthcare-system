package com.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.domain.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

	/* ================= FINDERS ================= */

	// Find by Mobile Number
	Optional<Patient> findByMobileNo(String mobileNo);

	// Find by Email
	Optional<Patient> findByEmail(String email);

	// Find by Username (Login Mapping)
	Optional<Patient> findByUserUsername(String username);

	/* ================= EXISTS ================= */

	boolean existsByMobileNo(String mobileNo);

	boolean existsByEmail(String email);

	/* ================= PAGINATION ================= */

	Page<Patient> findAll(Pageable pageable);
}