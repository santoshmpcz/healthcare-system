package com.app.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.app.domain.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	Page<Appointment> findByDoctorIdAndDateGreaterThanEqual(Long id, LocalDate date, Pageable pageable);

	Page<Appointment> findByDoctorId(Long id, Pageable pageable);

	Page<Appointment> findByDoctorIdAndNoOfSlotsGreaterThan(Long doctorId, int noOfSlots, Pageable pageable);

	@Query(value = "SELECT a FROM Appointment a WHERE " + "(:doctorId IS NULL OR a.doctor.id = :doctorId) "
			+ "AND (:date IS NULL OR a.date >= :date) " + "AND (:minSlots IS NULL OR a.noOfSlots >= :minSlots)",

			countQuery = "SELECT COUNT(a) FROM Appointment a WHERE " + "(:doctorId IS NULL OR a.doctor.id = :doctorId) "
					+ "AND (:date IS NULL OR a.date >= :date) " + "AND (:minSlots IS NULL OR a.noOfSlots >= :minSlots)")
	Page<Appointment> searchAppointments(Long doctorId, LocalDate date, Integer minSlots, Pageable pageable);

	@Query("SELECT COALESCE(SUM(a.noOfSlots),0) FROM Appointment a "
			+ "WHERE a.doctor.id = :doctorId AND a.date = :date")
	int getTotalSlotsByDoctorAndDate(Long doctorId, LocalDate date);

	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Appointment a SET a.noOfSlots = a.noOfSlots + :count "
			+ "WHERE a.id = :id AND a.noOfSlots + :count >= 0")
	int updateSlotCountForAppointment(Long id, int count);

	boolean existsByDoctor_IdAndDate(Long id, LocalDate date);
}