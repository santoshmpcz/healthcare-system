package com.app.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.constraints.SlotStatus;
import com.app.domain.Appointment;
import com.app.domain.Doctor;
import com.app.dto.AppointmentDTO;
import com.app.repository.AppointmentRepository;
import com.app.repository.DoctorRepository;
import com.app.repository.SlotRequestRepository;
import com.app.service.AppointmentService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {

	private final AppointmentRepository repo;
	private final DoctorRepository doctorRepo;
	private final SlotRequestRepository slotsRepo;

	// ================= DTO -> ENTITY =================
	private Appointment mapToEntity(AppointmentDTO dto) {

		Doctor doctor = doctorRepo.findById(dto.getDoctorId())
				.orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + dto.getDoctorId()));

		Appointment appt = new Appointment();
		appt.setDoctor(doctor);
		appt.setDate(dto.getDate());
		appt.setNoOfSlots(dto.getNoOfSlots());
		appt.setDetails(dto.getDetails());
		appt.setFee(dto.getFee());
		appt.setStatus(dto.getStatus());

		return appt;
	}

	// ================= ENTITY -> DTO =================
	private AppointmentDTO mapToDTO(Appointment appt) {

		AppointmentDTO dto = new AppointmentDTO();
		dto.setId(appt.getId());

		if (appt.getDoctor() != null) {
			Doctor doc = appt.getDoctor();

			dto.setDoctorId(doc.getId());

			String fullName = doc.getFirstName() + " "
					+ (doc.getMiddleName() != null && !doc.getMiddleName().isBlank() ? doc.getMiddleName() + " " : "")
					+ doc.getLastName();

			dto.setDoctorName(fullName.trim());
		}

		dto.setDate(appt.getDate());
		dto.setNoOfSlots(appt.getNoOfSlots());
		dto.setDetails(appt.getDetails());
		dto.setFee(appt.getFee());
		dto.setStatus(appt.getStatus());

		return dto;
	}

	// ================= CREATE =================
	@Override
	@Transactional
	public AppointmentDTO createAppointment(AppointmentDTO dto) {

		Appointment saved = repo.save(mapToEntity(dto));

		log.info("Appointment created with ID: {}", saved.getId());

		return mapToDTO(saved);
	}

	// ================= UPDATE =================
	@Override
	@Transactional
	public AppointmentDTO updateAppointment(Long id, AppointmentDTO dto) {

		Appointment existing = repo.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

		Doctor doctor = doctorRepo.findById(dto.getDoctorId())
				.orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

		existing.setDoctor(doctor);
		existing.setDate(dto.getDate());
		existing.setNoOfSlots(dto.getNoOfSlots());
		existing.setDetails(dto.getDetails());
		existing.setFee(dto.getFee());
		existing.setStatus(dto.getStatus());

		return mapToDTO(existing);
	}

	// ================= GET =================
	@Override
	public AppointmentDTO getAppointmentById(Long id) {

		return repo.findById(id).map(this::mapToDTO)
				.orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
	}

	// ================= DELETE =================
	@Override
	@Transactional
	public void deleteAppointment(Long id) {

		if (!repo.existsById(id)) {
			throw new EntityNotFoundException("Appointment not found");
		}

		repo.deleteById(id);
	}

	// ================= FETCH =================
	@Override
	public Page<AppointmentDTO> getAllAppointments(Pageable pageable) {
		return repo.findAll(pageable).map(this::mapToDTO);
	}

	@Override
	public Page<AppointmentDTO> searchAppointments(Long doctorId, LocalDate date, Integer minSlots, Pageable pageable) {

		return repo.searchAppointments(doctorId, date, minSlots, pageable).map(this::mapToDTO);
	}

	@Override
	public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) {

		return repo.searchAppointments(doctorId, null, null, Pageable.unpaged()).getContent().stream()
				.map(this::mapToDTO).collect(Collectors.toList());
	}

	@Override
	public List<AppointmentDTO> getAppointmentsByDoctorEmail(String email) {

		return doctorRepo.findByEmail(email)
				.map(doc -> repo.findByDoctorIdAndNoOfSlotsGreaterThan(doc.getId(), 0, Pageable.unpaged()).getContent()
						.stream().map(this::mapToDTO).collect(Collectors.toList()))
				.orElseGet(List::of);
	}

	// ================= SLOT DECREASE =================
	@Override
	@Transactional
	public void decreaseAvailableSlot(Long appointmentId) {

		int updated = slotsRepo.decreaseSlotIfAvailable(appointmentId);

		if (updated == 0) {
			throw new IllegalStateException("No slots available for appointment ID: " + appointmentId);
		}

		log.info("Slot decreased for appointment ID: {}", appointmentId);
	}

	// ================= SLOT INCREASE =================
	@Override
	@Transactional
	public void increaseAvailableSlot(Long appointmentId) {

		int updated = repo.updateSlotCountForAppointment(appointmentId, 1);

		if (updated == 0) {
			throw new EntityNotFoundException("Appointment not found for slot increase: " + appointmentId);
		}

		log.info("Slot increased for appointment ID: {}", appointmentId);
	}

	// ================= COUNT =================
	@Override
	public long getTotalAppointmentCount() {
		return repo.count();
	}

	// ================= DEFAULT SLOT CREATION =================
	@Override
	@Transactional
	public void createDefaultSlots(Long doctorId, LocalDate date) {

		if (repo.existsByDoctor_IdAndDate(doctorId, date)) {
			return;
		}

		Doctor doctor = doctorRepo.findById(doctorId)
				.orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

		Appointment appt = new Appointment();
		appt.setDoctor(doctor);
		appt.setDate(date);
		appt.setNoOfSlots(200);
		appt.setStatus(SlotStatus.AVAILABLE);
		appt.setDetails("Default Slots");
		appt.setFee(500.0);

		repo.save(appt);
	}

	// ================= FIND =================
	@Override
	public Optional<Appointment> findById(Long id) {
		return repo.findById(id);
	}
}