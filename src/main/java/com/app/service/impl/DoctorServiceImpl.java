package com.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.constraints.UserRoles;
import com.app.domain.Doctor;
import com.app.domain.Specialization;
import com.app.domain.User;
import com.app.dto.DoctorDTO;
import com.app.exception.DoctorNotFoundException;
import com.app.mapper.DoctorMapper;
import com.app.repository.DoctorRepository;
import com.app.repository.SpecializationRepository;
import com.app.service.DoctorService;
import com.app.service.UserService;
import com.app.specification.DoctorSpecification;
import com.app.util.MyCollectionUtil;
import com.app.util.MyMailUtil;
import com.app.util.UserUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorServiceImpl implements DoctorService {

	private final DoctorRepository doctorRepository;
	private final SpecializationRepository specializationRepository;
	private final UserService userService;
	private final UserUtil userUtil;
	private final MyMailUtil mailUtil;
	private final PasswordEncoder passwordEncoder;

	// ================= CREATE =================
	@Override
	public DoctorDTO createDoctor(DoctorDTO dto) {

		if (dto == null) {
			throw new IllegalArgumentException("Doctor data must not be null");
		}

		String email = normalize(dto.getEmail());
		validateEmail(email, null);

		Doctor doctor = DoctorMapper.toEntity(dto);
		doctor.setDoctorCode(null);

		if (dto.getSpecializationId() != null) {
			doctor.setSpecialization(getSpecialization(dto.getSpecializationId()));
		}

		Doctor savedDoctor = doctorRepository.save(doctor);

		if (!hasText(savedDoctor.getDoctorCode())) {
			String docCode = "DOC-" + String.format("%04d", savedDoctor.getId());
			savedDoctor.setDoctorCode(docCode);
			savedDoctor = doctorRepository.save(savedDoctor);
		}

		createUserIfNotExists(savedDoctor);

		return DoctorMapper.toDTO(savedDoctor);
	}

	// ================= UPDATE =================
	@Override
	public DoctorDTO updateDoctor(Long id, DoctorDTO dto) {

		Doctor doctor = doctorRepository.findById(id)
				.orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + id));

		String email = normalize(dto.getEmail());
		validateEmail(email, id);

		updateDoctorFields(doctor, dto);

		return DoctorMapper.toDTO(doctorRepository.save(doctor));
	}

	// ================= DELETE =================
	@Override
	public boolean deleteDoctor(Long id) {
		return doctorRepository.findById(id).map(d -> {
			doctorRepository.delete(d);
			return true;
		}).orElse(false);
	}

	// ================= READ =================
	@Override
	@Transactional(readOnly = true)
	public Optional<DoctorDTO> getDoctorById(Long id) {
		return doctorRepository.findById(id).map(DoctorMapper::toDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DoctorDTO> getAllDoctors(Pageable pageable) {
		return doctorRepository.findAll(pageable).map(DoctorMapper::toDTO);
	}

	@Override
	public List<DoctorDTO> getAllDoctorsForExport() {
		return doctorRepository.findAll().stream().map(DoctorMapper::toDTO).toList();
	}

	// ================= SEARCH (🔥 HYBRID FIXED) =================
	@Override
	@Transactional(readOnly = true)
	public Page<DoctorDTO> searchDoctors(DoctorDTO filter, Pageable pageable) {

		// ✅ GLOBAL KEYWORD (HYBRID SEARCH)
		if (filter != null) {

			String keyword = normalize(filter.getFirstName()); // using firstName as search box

			if (hasText(keyword)) {
				return doctorRepository.searchDoctor(keyword, pageable).map(DoctorMapper::toDTO);
			}
		}

		// ✅ SPECIFICATION FILTERS
		Specification<Doctor> spec = Specification.where(null);

		if (filter != null) {

			if (filter.getSpecializationId() != null) {
				spec = spec.and(DoctorSpecification.hasSpecialization(filter.getSpecializationId()));
			}

			if (filter.getExperience() != null) {
				spec = spec.and(DoctorSpecification.hasExperience(filter.getExperience()));
			}

			if (hasText(filter.getMobile())) {
				spec = spec.and(DoctorSpecification.hasMobile(normalize(filter.getMobile())));
			}

			if (hasText(filter.getAddress())) {
				spec = spec.and(DoctorSpecification.hasAddress(normalize(filter.getAddress())));
			}
		}

		return doctorRepository.findAll(spec, pageable).map(DoctorMapper::toDTO);
	}

	// ================= UTIL =================
	@Override
	public Map<Long, String> getDoctorIdAndNameMap() {
		return MyCollectionUtil.convertToMapIndex(doctorRepository.getDoctorIdAndNames());
	}

	@Override
	public long getDoctorCount() {
		return doctorRepository.count();
	}

	@Override
	public boolean existsById(Long id) {
		return doctorRepository.existsById(id);
	}

	// ================= EXTRA =================
	@Override
	public Optional<DoctorDTO> getDoctorByEmail(String email) {

		String normalizedEmail = normalize(email);

		if (!hasText(normalizedEmail)) {
			return Optional.empty();
		}

		return doctorRepository.findByEmail(normalizedEmail).map(DoctorMapper::toDTO);
	}

	// ================= PRIVATE METHODS =================

	private void validateEmail(String email, Long id) {

		if (!hasText(email))
			return;

		Optional<Doctor> existing = doctorRepository.findByEmail(email);

		if (existing.isPresent() && (id == null || !existing.get().getId().equals(id))) {
			throw new RuntimeException("Email already exists");
		}
	}

	private Specialization getSpecialization(Long id) {
		return specializationRepository.findById(id)
				.orElseThrow(() -> new DoctorNotFoundException("Invalid Specialization ID: " + id));
	}

	private void createUserIfNotExists(Doctor doctor) {

		if (!hasText(doctor.getEmail()))
			return;

		String email = normalize(doctor.getEmail());

		if (userService.findByUsername(email).isPresent())
			return;

		String rawPassword = userUtil.genPwd();

		String fullName = ((doctor.getFirstName() != null ? doctor.getFirstName() : "") + " "
				+ (doctor.getLastName() != null ? doctor.getLastName() : "")).trim();

		User user = new User();
		user.setDisplayName(fullName);
		user.setUsername(email);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setRole(UserRoles.DOCTOR);

		Long userId = userService.saveUser(user);

		if (userId != null) {
			sendAccountEmail(email, rawPassword, fullName);
		}
	}

	private void sendAccountEmail(String email, String password, String name) {
		try {

			String safeName = (name != null && !name.trim().isEmpty()) ? name : "User";

			String text = "<div style='font-family: Arial;'>" + "<h2>Doctor Account Created</h2>" + "<p>Dear "
					+ safeName + ",</p>" + "<p>Username: " + email + "</p>" + "<p>Password: " + password + "</p>"
					+ "<p>Please change your password after login.</p>" + "</div>";

			mailUtil.send(email, "DOCTOR ACCOUNT CREATED", text);

		} catch (Exception e) {
			log.error("Mail failed: {}", email, e);
		}
	}

	private void updateDoctorFields(Doctor doctor, DoctorDTO dto) {

		if (dto.getFirstName() != null)
			doctor.setFirstName(dto.getFirstName());
		if (dto.getMiddleName() != null)
			doctor.setMiddleName(dto.getMiddleName());
		if (dto.getLastName() != null)
			doctor.setLastName(dto.getLastName());
		if (dto.getEmail() != null)
			doctor.setEmail(normalize(dto.getEmail()));
		if (dto.getMobile() != null)
			doctor.setMobile(dto.getMobile());
		if (dto.getRoomNo() != null)
			doctor.setRoomNo(dto.getRoomNo());
		if (dto.getExperience() != null)
			doctor.setExperience(dto.getExperience());
		if (dto.getAddress() != null)
			doctor.setAddress(dto.getAddress());
		if (dto.getHobbies() != null)
			doctor.setHobbies(dto.getHobbies());
		if (dto.getSalary() != null)
			doctor.setSalary(dto.getSalary());
		if (dto.getDegree() != null)
			doctor.setDegree(dto.getDegree());
		if (dto.getGender() != null)
			doctor.setGender(dto.getGender());
		if (dto.getBlock() != null)
			doctor.setBlock(dto.getBlock());
		if (dto.getDistrict() != null)
			doctor.setDistrict(dto.getDistrict());
		if (dto.getCountry() != null)
			doctor.setCountry(dto.getCountry());
		if (dto.getNationality() != null)
			doctor.setNationality(dto.getNationality());
		if (dto.getNote() != null)
			doctor.setNote(dto.getNote());

		if (hasText(dto.getImageUrl())) {
			doctor.setImageUrl(dto.getImageUrl());
		}

		if (dto.getSpecializationId() != null) {
			doctor.setSpecialization(getSpecialization(dto.getSpecializationId()));
		}
	}

	private boolean hasText(String val) {
		return val != null && !val.trim().isEmpty();
	}

	private String normalize(String val) {
		return hasText(val) ? val.trim().toLowerCase() : null;
	}

}