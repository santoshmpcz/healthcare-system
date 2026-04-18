package com.app.mapper;

import org.springframework.stereotype.Component;

import com.app.domain.Appointment;
import com.app.domain.Doctor;
import com.app.dto.AppointmentDTO;

@Component
public class AppointmentMapper {

	public AppointmentDTO toDTO(Appointment appt) {

		if (appt == null) {
			return null;
		}

		AppointmentDTO dto = new AppointmentDTO();

		dto.setDoctorId(appt.getDoctor() != null ? appt.getDoctor().getId() : null);
		dto.setDate(appt.getDate());
		dto.setNoOfSlots(appt.getNoOfSlots());
		dto.setDetails(appt.getDetails());
		dto.setFee(appt.getFee());
//		dto.setStatus(appt.getStatus());
		dto.setStatus(appt.getStatus());

		return dto;
	}

	public Appointment toEntity(AppointmentDTO dto, Doctor doctor) {

		if (dto == null) {
			return null;
		}

		Appointment appt = new Appointment();

		appt.setDoctor(doctor);
		appt.setDate(dto.getDate());
		appt.setNoOfSlots(dto.getNoOfSlots());
		appt.setDetails(dto.getDetails());
		appt.setFee(dto.getFee());
		appt.setStatus(dto.getStatus());

		return appt;
	}
}