package com.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.constraints.AppointmentStatus;
import com.app.constraints.PaymentStatus;
import com.app.domain.Payment;
import com.app.domain.SlotRequest;
import com.app.repository.PaymentRepository;
import com.app.service.PaymentService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository repo;

	// ================= CREATE PAYMENT =================
	@Override
	public Payment createPayment(SlotRequest slotRequest, Double amount) {

		validatePaymentRequest(slotRequest, amount);

		boolean activeExists = repo.existsActivePayment(slotRequest.getId(),
				List.of(PaymentStatus.INITIATED, PaymentStatus.SUCCESS, PaymentStatus.REFUND_PENDING));

		if (activeExists) {
			throw new IllegalStateException("Active payment already exists for this slot");
		}

		Payment payment = new Payment();
		payment.setSlotRequest(slotRequest);
		payment.setAmount(amount);
		payment.setStatus(PaymentStatus.INITIATED);

		// ✅ maintain relationship
		slotRequest.addPayment(payment);

		Payment saved = repo.save(payment);

		log.info("Payment initiated for SlotRequest ID: {}", slotRequest.getId());

		return saved;
	}

	// ================= SAVE =================
	@Override
	public Payment save(Payment payment) {
		if (payment == null) {
			throw new IllegalArgumentException("Payment cannot be null");
		}
		return repo.save(payment);
	}

	// ================= MARK SUCCESS =================
	@Override
	public Payment markPaymentSuccess(String orderId) {

		Payment payment = getPaymentByOrderId(orderId);

		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			return payment;
		}

		validatePaymentSuccessTransition(payment);

		payment.setStatus(PaymentStatus.SUCCESS);

		log.info("Payment SUCCESS for Order ID: {}", orderId);

		return repo.save(payment);
	}

	// ================= MARK FAILED =================
	@Override
	public Payment markPaymentFailed(String orderId) {

		Payment payment = getPaymentByOrderId(orderId);

		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			throw new IllegalStateException("Cannot mark successful payment as failed");
		}

		if (payment.getStatus() == PaymentStatus.REFUNDED) {
			throw new IllegalStateException("Refunded payment cannot be failed");
		}

		payment.setStatus(PaymentStatus.FAILED);

		log.info("Payment FAILED for Order ID: {}", orderId);

		return repo.save(payment);
	}

	// ================= MARK REFUND PENDING =================
	@Override
	public Payment markRefundPending(String orderId) {

		Payment payment = getPaymentByOrderId(orderId);

		if (payment.getStatus() != PaymentStatus.SUCCESS) {
			throw new IllegalStateException("Only successful payments can be marked refund pending");
		}

		payment.setStatus(PaymentStatus.REFUND_PENDING);

		log.info("Refund pending for Order ID: {}", orderId);

		return repo.save(payment);
	}

	// ================= MARK REFUNDED =================
	@Override
	public Payment markRefunded(String orderId) {

		Payment payment = getPaymentByOrderId(orderId);

		if (payment.getStatus() != PaymentStatus.REFUND_PENDING) {
			throw new IllegalStateException("Refund must be pending before marked refunded");
		}

		payment.setStatus(PaymentStatus.REFUNDED);

		log.info("Payment REFUNDED for Order ID: {}", orderId);

		return repo.save(payment);
	}

	// ================= PRIVATE =================

	private void validatePaymentRequest(SlotRequest slotRequest, Double amount) {

		if (slotRequest == null) {
			throw new IllegalArgumentException("SlotRequest cannot be null");
		}

		if (amount == null || amount <= 0) {
			throw new IllegalArgumentException("Invalid payment amount");
		}

		if (slotRequest.getStatus() != AppointmentStatus.ACCEPTED) {
			throw new IllegalStateException("Payment allowed only for accepted appointments");
		}
	}

	private Payment getPaymentByOrderId(String orderId) {

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID cannot be null");
		}

		return repo.findByTransactionId(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Payment not found for Order ID: " + orderId));
	}

	private void validatePaymentSuccessTransition(Payment payment) {

		if (payment.getSlotRequest() == null || payment.getSlotRequest().getStatus() != AppointmentStatus.ACCEPTED) {
			throw new IllegalStateException("Cannot complete payment for unaccepted appointment");
		}

		if (payment.getStatus() == PaymentStatus.REFUNDED) {
			throw new IllegalStateException("Refunded payment cannot become successful");
		}

		if (payment.getStatus() == PaymentStatus.REFUND_PENDING) {
			throw new IllegalStateException("Refund pending payment cannot become successful");
		}
	}
}
