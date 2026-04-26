package com.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.app.domain.Specialization;
import com.app.dto.SpecializationDTO;
import com.app.exception.SpecializationNotFoundException;
import com.app.repository.SpecializationRepository;
import com.app.service.SpecializationService;
import com.app.util.MyCollectionUtil;
import com.app.util.SpecializationMetaUtil;

@Service
public class SpecializationServiceImpl implements SpecializationService {

	@Autowired
	private SpecializationRepository repo;

	// ================= SAVE =================
	@Override
	public Long saveSpecialization(SpecializationDTO dto) {

		if (isSpecCodeExist(dto.getSpecCode())) {
			throw new RuntimeException("Specialization Code already exists: " + dto.getSpecCode());
		}

		Specialization spec = convertToEntity(dto);
		return repo.save(spec).getId();
	}

	// ================= UPDATE =================
	@Override
	public void updateSpecialization(Long id, SpecializationDTO dto) {

		Specialization existing = repo.findById(id)
				.orElseThrow(() -> new SpecializationNotFoundException(id + " Not Found"));

		if (isSpecCodeExistForEdit(dto.getSpecCode(), id)) {
			throw new RuntimeException("Specialization Code already exists: " + dto.getSpecCode());
		}

		existing.setSpecCode(dto.getSpecCode());
		existing.setSpecName(dto.getSpecName());
		existing.setSpecNote(dto.getSpecNote());
		existing.setIcon(dto.getIcon());
		existing.setColor(dto.getColor());

		repo.save(existing);
	}

	// ================= GET ONE =================
	@Override
	public SpecializationDTO getOneSpecialization(Long id) {

		Specialization spec = repo.findById(id)
				.orElseThrow(() -> new SpecializationNotFoundException(id + " Not Found"));

		return convertToDTO(spec);
	}

	// ================= DELETE =================
	@Override
	public void removeSpecialization(Long id) {

		Specialization spec = repo.findById(id)
				.orElseThrow(() -> new SpecializationNotFoundException(id + " Not Found"));

		repo.delete(spec);
	}

	// ================= GET ALL =================
	@Override
	public List<SpecializationDTO> getAllSpecializations() {

		return repo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	// ================= PAGINATION =================
	@Override
	public Page<SpecializationDTO> getAllSpecializations(Pageable pageable) {

		return repo.findAll(pageable).map(this::convertToDTO);
	}

	// ================= CHECK SPEC CODE =================
	@Override
	public boolean isSpecCodeExist(String specCode) {
		return repo.getSpecCodeCount(specCode) > 0;
	}

	@Override
	public boolean isSpecCodeExistForEdit(String specCode, Long id) {
		return repo.getSpecCodeCountForEdit(specCode, id) > 0;
	}

	// ================= DROPDOWN MAP =================
	@Override
	public Map<Long, String> getSpecIdAndName() {

		List<Object[]> list = repo.getSpecIdAndName();
		return MyCollectionUtil.convertToMap(list);
	}

	// ================= COUNT =================
	@Override
	public long getSpecializationCount() {
		return repo.count();
	}

	// ================= META LIST =================
	@Override
	public List<SpecializationDTO> getAllSpecializationsWithMeta() {

		return repo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	// ================= DTO → ENTITY =================
	private Specialization convertToEntity(SpecializationDTO dto) {

		return Specialization.builder().id(dto.getId()).specCode(dto.getSpecCode()).specName(dto.getSpecName())
				.specNote(dto.getSpecNote()).icon(dto.getIcon()).color(dto.getColor()).build();
	}

	// ================= ENTITY → DTO =================
	private SpecializationDTO convertToDTO(Specialization spec) {

		return SpecializationDTO.builder().id(spec.getId()).specCode(spec.getSpecCode()).specName(spec.getSpecName())
				.specNote(spec.getSpecNote())

				// Auto icon fallback
				.icon(SpecializationMetaUtil.resolveIcon(spec.getSpecName(), spec.getIcon()))

				// Auto color fallback
				.color(SpecializationMetaUtil.resolveColor(spec.getSpecName(), spec.getColor())).build();
	}

	@Override
	public Page<SpecializationDTO> searchSpecializations(String keyword, Pageable pageable) {
		Page<Specialization> page = repo.search(keyword, pageable);

		return page.map(this::convertToDTO);
	}
}