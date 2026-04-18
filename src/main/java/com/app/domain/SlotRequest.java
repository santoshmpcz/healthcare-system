package com.app.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.app.constraints.AppointmentStatus;
import com.app.constraints.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "appointment", "patient", "payments" })
@EqualsAndHashCode(exclude = { "appointment", "patient", "payments" })
@Entity
@Table(name = "slot_req_tab", uniqueConstraints = {
		@UniqueConstraint(name = "uk_slot_patient", columnNames = { "app_id_fk_col", "patient_id_fk_col" }) })
public class SlotRequest {

	// ================= PRIMARY KEY =================
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "slot_req_seq_gen")
	@SequenceGenerator(name = "slot_req_seq_gen", sequenceName = "slot_req_seq_tab", allocationSize = 1)
	private Long id;

	// ================= RELATIONS =================
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "app_id_fk_col", nullable = false)
	private Appointment appointment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "patient_id_fk_col", nullable = false)
	private Patient patient;

	// ================= SLOT STATUS =================
	@Enumerated(EnumType.STRING)
	@Column(name = "slot_status_col", nullable = false, length = 20)
	private AppointmentStatus status;

	// ================= PAYMENT AMOUNT =================
	@Column(name = "amount", nullable = false)
	private Double amount;

	// ================= PAYMENT RELATION =================
	@OneToMany(mappedBy = "slotRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Payment> payments = new ArrayList<>();

	// ================= DERIVED PAYMENT STATUS =================
	@Transient
	public PaymentStatus getLatestPaymentStatus() {

		if (payments == null || payments.isEmpty()) {
			return PaymentStatus.INITIATED; // default fallback
		}

		return payments.stream().filter(p -> p.getCreatedAt() != null).max(Comparator.comparing(Payment::getCreatedAt))
				.map(Payment::getStatus).orElse(PaymentStatus.INITIATED);
	}

	// ================= HELPER METHODS (VERY IMPORTANT) =================
	public void addPayment(Payment payment) {
		if (payment != null) {
			payments.add(payment);
			payment.setSlotRequest(this);
		}
	}

	public void removePayment(Payment payment) {
		if (payment != null) {
			payments.remove(payment);
			payment.setSlotRequest(null);
		}
	}

	// ================= AUDIT =================
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	// ================= LIFECYCLE =================
	@PrePersist
	private void onCreate() {

		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;

		if (this.status == null) {
			this.status = AppointmentStatus.PENDING;
		}

		if (this.amount == null) {
			this.amount = 0.0;
		}
	}

	@PreUpdate
	private void onUpdate() {
		this.updatedAt = LocalDateTime.now();

		if (this.amount == null) {
			this.amount = 0.0;
		}
	}
}
