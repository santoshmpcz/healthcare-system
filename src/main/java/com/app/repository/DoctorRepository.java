package com.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.domain.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {

	// ================= EMAIL =================
	Optional<Doctor> findByEmail(String email);

	// ================= ADDRESS =================
	@Query("""
			SELECT d FROM Doctor d
			WHERE LOWER(d.address) LIKE LOWER(CONCAT('%', :address, '%'))
			""")
	List<Doctor> findDoctorsByAddress(@Param("address") String address);

	// ================= NAME =================
	@Query("""
			SELECT d FROM Doctor d
			WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
			OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
			""")
	List<Doctor> findDoctorsByName(@Param("name") String name);

	// ================= SPECIALIZATION =================
	List<Doctor> findBySpecializationId(Long specId);

	// ================= SALARY RANGE =================
	@Query("""
			SELECT d FROM Doctor d
			WHERE d.salary BETWEEN :min AND :max
			""")
	List<Doctor> findDoctorsBySalaryRange(@Param("min") Double min, @Param("max") Double max);

	// ================= EMAIL OR MOBILE =================
	@Query("""
			SELECT d FROM Doctor d
			WHERE d.email = :value OR d.mobile = :value
			""")
	List<Doctor> findDoctorByEmailOrMobile(@Param("value") String value);

	// ================= GLOBAL SEARCH =================
	@Query("""
			SELECT d FROM Doctor d
			WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :key, '%'))
			OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :key, '%'))
			OR LOWER(d.address) LIKE LOWER(CONCAT('%', :key, '%'))
			""")
	List<Doctor> searchDoctor(@Param("key") String key);

	// ================= ID + NAME =================
	@Query("""
			SELECT d.id, d.firstName, d.lastName
			FROM Doctor d
			""")
	List<Object[]> getDoctorIdAndNames();

	// ================= FETCH WITH SPECIALIZATION =================
	@EntityGraph(attributePaths = { "specialization" })
	@Query("SELECT d FROM Doctor d")
	List<Doctor> findAllDoctorsWithSpec();

	// ================= MAX ID =================
	@Query("SELECT MAX(d.id) FROM Doctor d")
	Long getMaxId();
}