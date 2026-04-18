package com.app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.constraints.UserRoles;
import com.app.domain.Patient;
import com.app.domain.User;
import com.app.repository.PatientRepository;
import com.app.service.PatientService;
import com.app.service.UserService;
import com.app.util.MyMailUtil;
import com.app.util.UserUtil;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

	private final PatientRepository repo;
	private final UserService userService;
	private final UserUtil userUtil;
	private final MyMailUtil mailUtil;

	public PatientServiceImpl(PatientRepository repo, UserService userService, UserUtil userUtil, MyMailUtil mailUtil) {

		this.repo = repo;
		this.userService = userService;
		this.userUtil = userUtil;
		this.mailUtil = mailUtil;
	}

	/*
	 * ===================================================== REGISTER PATIENT
	 * =====================================================
	 */
	@Override
	public void registerPatient(Patient patient) {

		if (patient.getEmail() == null || patient.getEmail().isBlank()) {
			throw new RuntimeException("Email is required");
		}

		String email = patient.getEmail().trim().toLowerCase();

		// Duplicate User Check
		if (userService.existsByUsername(email)) {
			throw new RuntimeException("Email already registered");
		}

		// Duplicate Patient Check
		if (repo.existsByEmail(email)) {
			throw new RuntimeException("Email already exists");
		}

		if (repo.existsByMobileNo(patient.getMobileNo())) {
			throw new RuntimeException("Mobile already exists");
		}

		// Create Login User
		User user = new User();
		user.setDisplayName(patient.getFirstName() + " " + patient.getLastName());
		user.setUsername(email);

		String pwd = userUtil.genPwd();
		user.setPassword(pwd);
		user.setRole(UserRoles.PATIENT);

		Long userId = userService.saveUser(user);

		User dbUser = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		// Attach user
		patient.setUser(dbUser);
		patient.setEmail(email);

		// Save patient
		repo.save(patient);

		// Send mail
		sendPatientEmail(dbUser, pwd);
	}

	/*
	 * ===================================================== UPDATE PATIENT
	 * =====================================================
	 */
	@Override
	public void updatePatient(Patient patient) {

		if (patient.getId() == null) {
			throw new RuntimeException("Patient ID is required");
		}

		Patient dbPatient = repo.findById(patient.getId()).orElseThrow(() -> new RuntimeException("Patient not found"));

		// Keep existing user mapping
		patient.setUser(dbPatient.getUser());

		// Mobile duplicate check
		Optional<Patient> byMobile = repo.findByMobileNo(patient.getMobileNo());

		if (byMobile.isPresent() && !byMobile.get().getId().equals(patient.getId())) {

			throw new RuntimeException("Mobile already exists");
		}

		// Email duplicate check
		Optional<Patient> byEmail = repo.findByEmail(patient.getEmail());

		if (byEmail.isPresent() && !byEmail.get().getId().equals(patient.getId())) {

			throw new RuntimeException("Email already exists");
		}

		repo.save(patient);
	}

	/*
	 * ===================================================== DELETE
	 * =====================================================
	 */
	@Override
	public void deleteById(Long id) {

		if (!repo.existsById(id)) {
			throw new RuntimeException("Patient not found");
		}

		repo.deleteById(id);
	}

	/*
	 * ===================================================== FIND ALL
	 * =====================================================
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> findAll() {
		return repo.findAll();
	}

	/*
	 * ===================================================== PAGINATION
	 * =====================================================
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<Patient> findAll(Pageable pageable) {
		return repo.findAll(pageable);
	}

	/*
	 * ===================================================== FINDERS
	 * =====================================================
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Patient> findById(Long id) {
		return repo.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Patient> findByMobileNo(String mobileNo) {
		return repo.findByMobileNo(mobileNo);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Patient> findByEmail(String email) {
		return repo.findByEmail(email);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Patient> findByUserUsername(String username) {
		return repo.findByUserUsername(username);
	}

	/*
	 * ===================================================== COUNT
	 * =====================================================
	 */
	@Override
	@Transactional(readOnly = true)
	public long count() {
		return repo.count();
	}

	/*
	 * ===================================================== MAIL
	 * =====================================================
	 */
	private void sendPatientEmail(User user, String pwd) {

		String subject = "Patient Account Created";

		String text = "<div style='font-family:Arial,sans-serif'>" + "<h2>Patient Account Created</h2>" + "<p>Dear "
				+ user.getDisplayName() + ",</p>" + "<p>Your account has been created successfully.</p>"
				+ "<p><b>Username:</b> " + user.getUsername() + "</p>" + "<p><b>Password:</b> " + pwd + "</p>"
				+ "<p style='color:red;'>Please change your password after login.</p>" + "<br>"
				+ "<p>Regards,<br><b>Santosh Hospital Team</b></p>" + "</div>";

		mailUtil.send(user.getUsername(), subject, text);
	}
}