package com.app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // ✅ added

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.domain.Appointment; // ✅ added
import com.app.dto.AppointmentDTO;

public interface AppointmentService {

	// ================= CRUD =================
	AppointmentDTO createAppointment(AppointmentDTO dto);

	AppointmentDTO updateAppointment(Long id, AppointmentDTO dto);

	AppointmentDTO getAppointmentById(Long id);

	void deleteAppointment(Long id);

	// ================= FETCH =================
	Page<AppointmentDTO> getAllAppointments(Pageable pageable);

	// ================= DYNAMIC FILTER =================
	Page<AppointmentDTO> searchAppointments(Long doctorId, LocalDate date, Integer minSlots, Pageable pageable);

	// ================= OPTIONAL FILTERS =================
	List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId);

	List<AppointmentDTO> getAppointmentsByDoctorEmail(String email);

	// ================= BUSINESS =================
	void decreaseAvailableSlot(Long appointmentId);

	void increaseAvailableSlot(Long appointmentId);

	// ================= COUNT =================
	long getTotalAppointmentCount();

	// ================= DEFAULT SLOT CREATION =================
	void createDefaultSlots(Long doctorId, LocalDate date);

	// ✅ ADD THIS METHOD (VERY IMPORTANT)
	Optional<Appointment> findById(Long id);
}