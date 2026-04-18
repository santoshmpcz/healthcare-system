package com.app.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.app.constraints.SlotStatus;
import com.app.domain.Appointment;

import jakarta.persistence.criteria.Predicate;

public class AppointmentSpecification {

	public static Specification<Appointment> filter(Long doctorId, LocalDate date, Integer minSlots) {

		return (root, query, cb) -> {

			Predicate p = cb.conjunction();

			if (doctorId != null) {
				p = cb.and(p, cb.equal(root.get("doctor").get("id"), doctorId));
			}

			if (date != null) {
				p = cb.and(p, cb.greaterThanOrEqualTo(root.get("date"), date));
			}

			if (minSlots != null) {
				p = cb.and(p, cb.greaterThanOrEqualTo(root.get("noOfSlots"), minSlots));
			}

			// ✅ Business logic (IMPORTANT)
			p = cb.and(p, cb.equal(root.get("status"), SlotStatus.AVAILABLE));
			p = cb.and(p, cb.greaterThan(root.get("noOfSlots"), 0));

			return p;
		};
	}
}