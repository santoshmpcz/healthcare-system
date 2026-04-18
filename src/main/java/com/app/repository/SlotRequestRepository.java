package com.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.app.constraints.AppointmentStatus;
import com.app.domain.SlotRequest;
import com.app.dto.SlotStatusCountDto;

public interface SlotRequestRepository extends JpaRepository<SlotRequest, Long> {

	// =====================================================
	// CHECK DUPLICATE BOOKING
	// =====================================================
	boolean existsByPatient_IdAndAppointment_Id(Long patientId, Long appointmentId);

	// =====================================================
	// FETCH SINGLE WITH REQUIRED DETAILS (NO PAYMENTS HERE)
	// =====================================================
	@Override
	@EntityGraph(attributePaths = { "patient", "appointment", "appointment.doctor" })
	Optional<SlotRequest> findById(Long id);

	// =====================================================
	// FETCH ALL (ADMIN)
	// =====================================================
	@Override
	@EntityGraph(attributePaths = { "patient", "appointment", "appointment.doctor" })
	Page<SlotRequest> findAll(Pageable pageable);

	// =====================================================
	// PATIENT VIEW
	// =====================================================
	@EntityGraph(attributePaths = { "patient", "appointment", "appointment.doctor" })
	Page<SlotRequest> findByPatient_Email(String email, Pageable pageable);

	// =====================================================
	// DOCTOR VIEW
	// =====================================================
	@EntityGraph(attributePaths = { "patient", "appointment", "appointment.doctor" })
	@Query("""
			SELECT sr FROM SlotRequest sr
			WHERE sr.appointment.doctor.email = :doctorMail
			AND sr.status = :status
			""")
	Page<SlotRequest> findByDoctorAndStatus(@Param("doctorMail") String doctorMail,
			@Param("status") AppointmentStatus status, Pageable pageable);

	// =====================================================
	// DASHBOARD COUNTS
	// =====================================================
	@Query("""
			SELECT new com.app.dto.SlotStatusCountDto(sr.status, COUNT(sr))
			FROM SlotRequest sr
			GROUP BY sr.status
			""")
	List<SlotStatusCountDto> getSlotsStatusAndCountDto();

	// =====================================================
	// ATOMIC SLOT DECREMENT (SAFE CHECK)
	// =====================================================
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			UPDATE Appointment a
			SET a.noOfSlots = a.noOfSlots - 1
			WHERE a.id = :id
			AND a.noOfSlots > 0
			""")
	int decreaseSlotIfAvailable(@Param("id") Long id);

	// =====================================================
	// SLOT RESTORE
	// =====================================================
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			UPDATE Appointment a
			SET a.noOfSlots = a.noOfSlots + 1
			WHERE a.id = :id
			""")
	int increaseSlotCount(@Param("id") Long id);
}
