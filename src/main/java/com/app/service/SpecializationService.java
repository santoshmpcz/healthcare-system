package com.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.dto.SpecializationDTO;

public interface SpecializationService {

	// ================= CREATE =================
	Long saveSpecialization(SpecializationDTO dto);

	// ================= UPDATE =================
	void updateSpecialization(Long id, SpecializationDTO dto);

	// ================= READ ONE =================
	SpecializationDTO getOneSpecialization(Long id);

	// ================= DELETE =================
	void removeSpecialization(Long id);

	// ================= READ ALL =================
	List<SpecializationDTO> getAllSpecializations();

	// Pagination Support
	Page<SpecializationDTO> getAllSpecializations(Pageable pageable);

	// ================= VALIDATION =================
	boolean isSpecCodeExist(String specCode);

	boolean isSpecCodeExistForEdit(String specCode, Long id);

	// ================= DROPDOWN =================
	Map<Long, String> getSpecIdAndName();

	// ================= DASHBOARD =================
	long getSpecializationCount();

	// ✅ NEW (Recommended for your requirement)
	// Returns specialization including icon + color metadata
	List<SpecializationDTO> getAllSpecializationsWithMeta();

	Page<SpecializationDTO> searchSpecializations(String keyword, Pageable pageable);
}