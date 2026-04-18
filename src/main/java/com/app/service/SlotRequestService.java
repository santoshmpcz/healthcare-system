package com.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.constraints.AppointmentStatus;
import com.app.domain.SlotRequest;
import com.app.dto.SlotRequestResponseDTO;
import com.app.dto.SlotStatusCountDto;

public interface SlotRequestService {

	// ================= BOOK SLOT =================
	Long bookSlot(Long appointmentId, Long patientId);

	// ================= READ =================
	SlotRequestResponseDTO getSlotRequestById(Long id);

	SlotRequest getSlotRequestEntity(Long id);

	// ================= ADMIN =================
	Page<SlotRequestResponseDTO> getAllSlotRequests(Pageable pageable);

	// ================= USER VIEWS =================
	Page<SlotRequestResponseDTO> getSlotsByPatientEmail(String patientEmail, Pageable pageable);

	Page<SlotRequestResponseDTO> getSlotsByDoctorEmail(String doctorEmail, AppointmentStatus status, Pageable pageable);

	// ================= STATUS =================
	void updateSlotRequestStatus(Long id, AppointmentStatus status);

	void cancelSlot(Long slotRequestId);

	// ================= DASHBOARD =================
	List<SlotStatusCountDto> getSlotsStatusAndCount();
}
