package com.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.domain.Specialization;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

	// Check duplicate SpecCode while saving
	@Query("SELECT COUNT(s.specCode) FROM Specialization s WHERE s.specCode = :specCode")
	Integer getSpecCodeCount(@Param("specCode") String specCode);

	// Check duplicate SpecCode while updating
	@Query("SELECT COUNT(s.specCode) FROM Specialization s WHERE s.specCode = :specCode AND s.id != :id")
	Integer getSpecCodeCountForEdit(@Param("specCode") String specCode, @Param("id") Long id);

	// Get Specialization Id and Name for dropdown
	@Query("SELECT s.id, s.specName FROM Specialization s")
	List<Object[]> getSpecIdAndName();

	// ================= DROPDOWN DATA =================
	List<Specialization> findAllByOrderBySpecNameAsc();

	// ✅ NEW (Recommended)

	// Fetch specialization by name (optional future use)
	Optional<Specialization> findBySpecName(String specName);

	// Fetch specialization with icon & color ordered
	@Query("SELECT s FROM Specialization s ORDER BY s.specName ASC")
	List<Specialization> findAllWithUiDetails();
}