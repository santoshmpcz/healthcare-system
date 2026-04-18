package com.app.mapper;

import com.app.domain.Doctor;
import com.app.domain.Specialization;
import com.app.dto.DoctorDTO;
import com.app.util.SpecializationMetaUtil;

public class DoctorMapper {

	// ================= ENTITY → DTO =================
	public static DoctorDTO toDTO(Doctor d) {

		if (d == null)
			return null;

		DoctorDTO dto = new DoctorDTO();

		dto.setId(d.getId());
//		dto.setDoctorId(d.getDoctorId());
		dto.setDoctorCode(d.getDoctorCode());

		dto.setFirstName(d.getFirstName());
		dto.setMiddleName(d.getMiddleName());
		dto.setLastName(d.getLastName());

		dto.setEmail(d.getEmail());
		dto.setMobile(d.getMobile());

		dto.setRoomNo(d.getRoomNo());
		dto.setExperience(d.getExperience());

		dto.setAddress(d.getAddress());
		dto.setHobbies(d.getHobbies());

		dto.setSalary(d.getSalary());
		dto.setDegree(d.getDegree());

		dto.setGender(d.getGender());

		dto.setBlock(d.getBlock());
		dto.setDistrict(d.getDistrict());
		dto.setCountry(d.getCountry());
		dto.setNationality(d.getNationality());

		dto.setNote(d.getNote());
		dto.setPhoto(d.getPhoto());

		// ✅ Specialization Mapping + META
		Specialization spec = d.getSpecialization();

		if (spec != null) {

			String name = spec.getSpecName();

			dto.setSpecializationId(spec.getId());
			dto.setSpecializationName(name);

			dto.setSpecializationIcon(SpecializationMetaUtil.resolveIcon(name, spec.getIcon()));

			dto.setSpecializationColor(SpecializationMetaUtil.resolveColor(name, spec.getColor()));
		}

		return dto;
	}

	// ================= DTO → ENTITY =================
	public static Doctor toEntity(DoctorDTO dto) {

		if (dto == null)
			return null;

		Doctor d = new Doctor();

		d.setId(dto.getId());
//		d.setDoctorId(dto.getDoctorId());
		d.setDoctorCode(dto.getDoctorCode());

		d.setFirstName(dto.getFirstName());
		d.setMiddleName(dto.getMiddleName());
		d.setLastName(dto.getLastName());

		d.setEmail(dto.getEmail());
		d.setMobile(dto.getMobile());

		d.setRoomNo(dto.getRoomNo());
		d.setExperience(dto.getExperience());

		d.setAddress(dto.getAddress());
		d.setHobbies(dto.getHobbies());

		d.setSalary(dto.getSalary());
		d.setDegree(dto.getDegree());

		d.setGender(dto.getGender());

		d.setBlock(dto.getBlock());
		d.setDistrict(dto.getDistrict());
		d.setCountry(dto.getCountry());
		d.setNationality(dto.getNationality());

		d.setNote(dto.getNote());
		d.setPhoto(dto.getPhoto());

		// ✅ Specialization Mapping
		if (dto.getSpecializationId() != null) {
			Specialization spec = new Specialization();
			spec.setId(dto.getSpecializationId());
			d.setSpecialization(spec);
		}

		return d;
	}
}