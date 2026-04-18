package com.app.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.dto.DoctorDTO;

public interface DoctorService {

	// ================= CREATE =================
	DoctorDTO createDoctor(DoctorDTO dto);

	// ================= UPDATE =================
	DoctorDTO updateDoctor(Long id, DoctorDTO dto);

	// ================= DELETE =================
	boolean deleteDoctor(Long id);

	// ================= READ =================
	Optional<DoctorDTO> getDoctorById(Long id);

	Optional<DoctorDTO> getDoctorByEmail(String email);

	
	Page<DoctorDTO> getAllDoctors(Pageable pageable);

	List<DoctorDTO> getAllDoctorsForExport();

	// ================= SEARCH =================
	Page<DoctorDTO> searchDoctors(DoctorDTO filter, Pageable pageable);

	// ================= UTIL =================
	Map<Long, String> getDoctorIdAndNameMap();

	long getDoctorCount();

	boolean existsById(Long id);
}