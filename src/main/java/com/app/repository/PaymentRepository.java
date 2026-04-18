package com.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.app.constraints.PaymentStatus;
import com.app.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	// =====================================================
	// FIND BY TRANSACTION ID (RAZORPAY / GATEWAY)
	// =====================================================
	Optional<Payment> findByTransactionId(String transactionId);

	boolean existsByTransactionId(String transactionId);

	// =====================================================
	// FETCH LATEST PAYMENT (BEST PRACTICE)
	// =====================================================
	@Query("""
			SELECT p FROM Payment p
			WHERE p.slotRequest.id = :slotId
			ORDER BY p.createdAt DESC
			""")
	List<Payment> findLatestPayment(@Param("slotId") Long slotId, Pageable pageable);

	// =====================================================
	// DERIVED METHOD (RECOMMENDED)
	// =====================================================
	Optional<Payment> findTopBySlotRequest_IdOrderByCreatedAtDesc(Long slotRequestId);

	List<Payment> findBySlotRequest_IdOrderByCreatedAtDesc(Long slotRequestId);

	boolean existsBySlotRequest_IdAndStatusIn(Long slotRequestId, List<PaymentStatus> statuses);

	// =====================================================
	// OPTIONAL: KEEP CUSTOM QUERY (ADVANCED USE)
	// =====================================================
	@Query("""
			SELECT COUNT(p) > 0 FROM Payment p
			WHERE p.slotRequest.id = :slotId
			AND p.status IN :statuses
			""")
	boolean existsActivePayment(@Param("slotId") Long slotRequestId, @Param("statuses") List<PaymentStatus> statuses);
}
