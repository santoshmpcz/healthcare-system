package com.app.specification;

import org.springframework.data.jpa.domain.Specification;
import com.app.domain.Doctor;

public class DoctorSpecification {

	// ================= SPECIALIZATION =================
	public static Specification<Doctor> hasSpecialization(Long specializationId) {
		return (root, query, cb) -> specializationId == null ? null
				: cb.equal(root.get("specialization").get("id"), specializationId);
	}

	// ================= FIRST NAME =================
	public static Specification<Doctor> hasFirstName(String name) {
		return (root, query, cb) -> isEmpty(name) ? null
				: cb.like(cb.lower(root.get("firstName")), "%" + name.toLowerCase() + "%");
	}

	// ================= LAST NAME =================
	public static Specification<Doctor> hasLastName(String name) {
		return (root, query, cb) -> isEmpty(name) ? null
				: cb.like(cb.lower(root.get("lastName")), "%" + name.toLowerCase() + "%");
	}

	// ================= EXPERIENCE =================
	public static Specification<Doctor> hasExperience(Integer exp) {
		return (root, query, cb) -> exp == null ? null : cb.equal(root.get("experience"), exp);
	}

	// ================= MOBILE =================
	public static Specification<Doctor> hasMobile(String mobile) {
		return (root, query, cb) -> isEmpty(mobile) ? null : cb.like(root.get("mobile"), "%" + mobile + "%");
	}

	// ================= ADDRESS =================
	public static Specification<Doctor> hasAddress(String address) {
		return (root, query, cb) -> isEmpty(address) ? null
				: cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%");
	}

	// ================= COMMON UTILITY =================
	private static boolean isEmpty(String val) {
		return val == null || val.trim().isEmpty();
	}
}