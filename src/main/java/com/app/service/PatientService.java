package com.app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.domain.Patient;

public interface PatientService {

	/* ================= CREATE ================= */
	void registerPatient(Patient patient);

	/* ================= UPDATE ================= */
	void updatePatient(Patient patient);

	/* ================= DELETE ================= */
	void deleteById(Long id);

	/* ================= READ ================= */

	// Existing list
	List<Patient> findAll();

	// Pagination Support
	Page<Patient> findAll(Pageable pageable);

	Optional<Patient> findById(Long id);

	Optional<Patient> findByMobileNo(String mobileNo);

	Optional<Patient> findByEmail(String email);

	Optional<Patient> findByUserUsername(String username);

	/* ================= COUNT ================= */
	long count();
}