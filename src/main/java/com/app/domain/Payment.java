package com.app.domain;

import java.time.LocalDateTime;

import com.app.constraints.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "slotRequest")
@EqualsAndHashCode(exclude = "slotRequest")
@Entity
@Table(name = "payment_tab")
public class Payment {

	// ================= PRIMARY KEY =================
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ================= PAYMENT DETAILS =================

	@Column(nullable = false)
	private Double amount;

	@Column(nullable = false, length = 10)
	private String currency = "INR";

	@Column(name = "payment_method", length = 30)
	private String paymentMethod; // UPI / CARD / NETBANKING

	// Razorpay / External Transaction ID (UNIQUE)
	@Column(name = "transaction_id", length = 120, unique = true)
	private String transactionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status = PaymentStatus.INITIATED;

	// ================= RELATION =================
	// MANY PAYMENTS → ONE SLOT (FIXED)

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "slot_req_id_fk", nullable = false)
	private SlotRequest slotRequest;

	// ================= AUDIT =================

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}