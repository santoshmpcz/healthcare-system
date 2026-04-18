package com.app.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.constraints.AppointmentStatus;
import com.app.constraints.PaymentStatus;
import com.app.domain.Appointment;
import com.app.domain.Patient;
import com.app.domain.Payment;
import com.app.domain.SlotRequest;
import com.app.dto.SlotRequestResponseDTO;
import com.app.dto.SlotStatusCountDto;
import com.app.mapper.SlotRequestMapper;
import com.app.repository.PaymentRepository;
import com.app.repository.SlotRequestRepository;
import com.app.service.SlotRequestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SlotRequestServiceImpl implements SlotRequestService {

	private final SlotRequestRepository repo;
	private final PaymentRepository paymentRepo;

	// =====================================================
	// COMMON
	// =====================================================
	private SlotRequest getEntity(Long id) {
		return repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("SlotRequest not found with ID: " + id));
	}

	// =====================================================
	// BOOK SLOT
	// =====================================================
	@Override
	public Long bookSlot(Long appointmentId, Long patientId) {

		if (appointmentId == null || patientId == null) {
			throw new IllegalArgumentException("Appointment ID and Patient ID are required");
		}

		if (repo.existsByPatient_IdAndAppointment_Id(patientId, appointmentId)) {
			throw new IllegalStateException("You already requested this appointment slot");
		}

		try {
			int updated = repo.decreaseSlotIfAvailable(appointmentId);

			if (updated == 0) {
				throw new IllegalStateException("No slots available");
			}

			Appointment appointment = new Appointment();
			appointment.setId(appointmentId);

			Patient patient = new Patient();
			patient.setId(patientId);

			SlotRequest slotRequest = new SlotRequest();
			slotRequest.setAppointment(appointment);
			slotRequest.setPatient(patient);
			slotRequest.setStatus(AppointmentStatus.PENDING);

			return repo.save(slotRequest).getId();

		} catch (Exception ex) {
			repo.increaseSlotCount(appointmentId);
			throw new IllegalStateException("Error booking slot", ex);
		}
	}

	// =====================================================
	// UPDATE SLOT STATUS
	// =====================================================
	@Override
	public void updateSlotRequestStatus(Long id, AppointmentStatus newStatus) {

		SlotRequest slotRequest = getEntity(id);
		AppointmentStatus oldStatus = slotRequest.getStatus();

		validateStatusTransition(oldStatus, newStatus);

		slotRequest.setStatus(newStatus);

		Payment latestPayment = paymentRepo.findTopBySlotRequest_IdOrderByCreatedAtDesc(slotRequest.getId())
				.orElse(null);

		// =====================================================
		// ACCEPTED → INITIATE PAYMENT
		// =====================================================
		if (newStatus == AppointmentStatus.ACCEPTED) {

			if (latestPayment == null) {
				Payment payment = new Payment();
				payment.setSlotRequest(slotRequest);
				payment.setAmount(slotRequest.getAmount());
				payment.setStatus(PaymentStatus.INITIATED);
				paymentRepo.save(payment);
			} else {
				latestPayment.setStatus(PaymentStatus.INITIATED);
				paymentRepo.save(latestPayment);
			}
		}

		// =====================================================
		// CANCELLED BY PATIENT
		// =====================================================
		if (newStatus == AppointmentStatus.CANCELLED) {

			handleRefundLogic(slotRequest, latestPayment);

			repo.increaseSlotCount(slotRequest.getAppointment().getId());
		}

		// =====================================================
		// REJECTED BY DOCTOR/ADMIN
		// =====================================================
		if (newStatus == AppointmentStatus.REJECTED) {

			handleRefundLogic(slotRequest, latestPayment);

			repo.increaseSlotCount(slotRequest.getAppointment().getId());
		}
	}

	// =====================================================
	// REFUND LOGIC (INDUSTRY STANDARD)
	// =====================================================
	private void handleRefundLogic(SlotRequest slotRequest, Payment payment) {

		if (payment == null) {
			return;
		}

		switch (payment.getStatus()) {

		case SUCCESS:
			// Payment done → refund required check
			payment.setStatus(PaymentStatus.REFUND_PENDING);
			break;

		case INITIATED:
		case FAILED:
			// Not paid → cancel directly
			payment.setStatus(PaymentStatus.CANCELLED);
			break;

		default:
			// REFUND_PENDING / REFUNDED / NON_REFUNDABLE → no change
			break;
		}

		paymentRepo.save(payment);
	}

	// =====================================================
	// CANCEL SLOT
	// =====================================================
	@Override
	public void cancelSlot(Long id) {
		updateSlotRequestStatus(id, AppointmentStatus.CANCELLED);
	}

	// =====================================================
	// VALIDATION
	// =====================================================
	private void validateStatusTransition(AppointmentStatus oldStatus, AppointmentStatus newStatus) {

		if (oldStatus == AppointmentStatus.REJECTED || oldStatus == AppointmentStatus.CANCELLED) {
			throw new IllegalStateException("Cannot modify finalized slot request");
		}

		if (oldStatus == AppointmentStatus.PENDING
				&& !(newStatus == AppointmentStatus.ACCEPTED || newStatus == AppointmentStatus.REJECTED)) {
			throw new IllegalStateException("Pending request can only be ACCEPTED or REJECTED");
		}

		if (oldStatus == AppointmentStatus.ACCEPTED && newStatus != AppointmentStatus.CANCELLED) {
			throw new IllegalStateException("Accepted request can only be CANCELLED");
		}
	}

	// =====================================================
	// READ METHODS
	// =====================================================
	@Override
	@Transactional(readOnly = true)
	public SlotRequestResponseDTO getSlotRequestById(Long id) {
		return SlotRequestMapper.toDto(getEntity(id));
	}

	@Override
	@Transactional(readOnly = true)
	public SlotRequest getSlotRequestEntity(Long id) {
		return getEntity(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SlotRequestResponseDTO> getAllSlotRequests(Pageable pageable) {
		return repo.findAll(pageable).map(SlotRequestMapper::toDto);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SlotRequestResponseDTO> getSlotsByPatientEmail(String email, Pageable pageable) {
		return repo.findByPatient_Email(email, pageable).map(SlotRequestMapper::toDto);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SlotRequestResponseDTO> getSlotsByDoctorEmail(String email, AppointmentStatus status,
			Pageable pageable) {
		return repo.findByDoctorAndStatus(email, status, pageable).map(SlotRequestMapper::toDto);
	}

	// =====================================================
	// DASHBOARD
	// =====================================================
	@Override
	@Transactional(readOnly = true)
	public List<SlotStatusCountDto> getSlotsStatusAndCount() {
		return repo.getSlotsStatusAndCountDto();
	}
}