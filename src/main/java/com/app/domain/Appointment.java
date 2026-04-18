package com.app.domain;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.app.constraints.SlotStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "appointment_tab")
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appointment_seq_tab")
	@SequenceGenerator(name = "appointment_seq_tab", sequenceName = "appointment_seq_tab", allocationSize = 1)

	private Long id;

	@ManyToOne
	@JoinColumn(name = "app_doc_id_fk", nullable = false)
	private Doctor doctor;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "app_dte_col", nullable = false)
	private LocalDate date;

	@Column(name = "app_slots_col", nullable = false)
	private Integer noOfSlots;

	@Column(name = "app_details_col")
	private String details;

	@Column(name = "app_fee_col")
	private Double fee;

	@Enumerated(EnumType.STRING)
	@Column(name = "app_status_col", nullable = false)
	private SlotStatus status;

	@PrePersist
	@PreUpdate
	public void validateAndSetDefaults() {

		// Default status
		if (status == null) {
			status = SlotStatus.AVAILABLE;
		}

		// Slot validation
		if (noOfSlots == null || noOfSlots <= 0) {
			throw new IllegalArgumentException("Slots must be greater than 0");
		}
	}
}