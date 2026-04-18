package com.app.service;

import com.app.domain.Payment;
import com.app.domain.SlotRequest;

public interface PaymentService {

	// ================= CREATE PAYMENT =================
	Payment createPayment(SlotRequest slotRequest, Double amount);

	// ================= SAVE =================
	Payment save(Payment payment);

	// ================= PAYMENT FLOW =================
	Payment markPaymentSuccess(String orderId);

	Payment markPaymentFailed(String orderId);

	// ================= REFUND FLOW =================
	Payment markRefundPending(String orderId);

	Payment markRefunded(String orderId);
}