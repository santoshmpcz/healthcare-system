package com.app.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.app.constraints.AppointmentStatus;
import com.app.constraints.PaymentStatus;
import com.app.domain.Payment;
import com.app.domain.SlotRequest;
import com.app.dto.SlotRequestResponseDTO;
import com.app.service.PaymentService;
import com.app.service.SlotRequestService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

	private final SlotRequestService slotService;
	private final PaymentService paymentService;
	private final RazorpayClient razorpayClient;

	private static final String RAZORPAY_SECRET = "YOUR_SECRET"; // 🔴 move to config

	// ================= OPEN PAYMENT PAGE =================
	@GetMapping("/pay")
	public String openPaymentPage(@RequestParam Long id, Principal principal, Model model) {

		if (principal == null)
			return "redirect:/login";

		SlotRequestResponseDTO sr = slotService.getSlotRequestById(id);

		validateSlotOwnership(sr, principal.getName());

		if (sr.getStatus() != AppointmentStatus.ACCEPTED) {
			throw new IllegalStateException("Payment allowed only for accepted appointments");
		}

		if (sr.getPaymentStatus() == PaymentStatus.SUCCESS) {
			throw new IllegalStateException("Payment already completed");
		}

		model.addAttribute("slot", sr);
		model.addAttribute("amount", sr.getFee());

		return "payment-page";
	}

	// ================= CREATE ORDER =================
	@PostMapping("/create-order")
	@ResponseBody
	public Map<String, Object> createOrder(@RequestParam Long slotId, Principal principal) {

		if (principal == null) {
			throw new IllegalStateException("Unauthorized access");
		}

		// ✅ Single fetch (ENTITY only)
		SlotRequest slot = slotService.getSlotRequestEntity(slotId);

		if (!principal.getName().equals(slot.getPatient().getEmail())) {
			throw new IllegalStateException("Unauthorized slot access");
		}

		if (slot.getStatus() != AppointmentStatus.ACCEPTED) {
			throw new IllegalStateException("Payment allowed only for accepted appointments");
		}

		Double amount = slot.getAppointment().getFee();

		if (amount == null || amount <= 0) {
			throw new IllegalArgumentException("Invalid payment amount");
		}

		try {
			int amountInPaisa = (int) Math.round(amount * 100);

			// ✅ Create payment FIRST
			Payment payment = paymentService.createPayment(slot, amount);

			JSONObject options = new JSONObject();
			options.put("amount", amountInPaisa);
			options.put("currency", "INR");
			options.put("receipt", "payment_" + payment.getId());

			Order order = razorpayClient.orders.create(options);

			// ✅ Store Razorpay orderId as transactionId
			payment.setTransactionId(order.get("id").toString());
			paymentService.save(payment);

			Map<String, Object> response = new HashMap<>();
			response.put("status", "CREATED");
			response.put("orderId", order.get("id").toString());
			response.put("amount", amountInPaisa);
			response.put("currency", "INR");

			return response;

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to create payment order", ex);
		}
	}

	// ================= VERIFY PAYMENT =================
	@PostMapping("/verify")
	@ResponseBody
	public Map<String, Object> verifyPayment(@RequestBody Map<String, String> data) {

		String orderId = data.get("razorpayOrderId");
		String paymentId = data.get("razorpayPaymentId");
		String signature = data.get("razorpaySignature");

		if (orderId == null || paymentId == null || signature == null) {
			throw new IllegalArgumentException("Invalid payment response");
		}

		Map<String, Object> response = new HashMap<>();

		try {
			// ✅ VERIFY SIGNATURE (CRITICAL)
			JSONObject options = new JSONObject();
			options.put("razorpay_order_id", orderId);
			options.put("razorpay_payment_id", paymentId);
			options.put("razorpay_signature", signature);

			Utils.verifyPaymentSignature(options, RAZORPAY_SECRET);

			// ✅ Idempotency handled inside service
			paymentService.markPaymentSuccess(orderId);

			response.put("status", "SUCCESS");
			response.put("message", "Payment completed successfully");

		} catch (Exception ex) {

			paymentService.markPaymentFailed(orderId);

			response.put("status", "FAILED");
			response.put("message", "Payment verification failed");
		}

		return response;
	}

	// ================= PRIVATE HELPERS =================
	private void validateSlotOwnership(SlotRequestResponseDTO sr, String email) {

		if (sr == null) {
			throw new IllegalArgumentException("Slot request not found");
		}

		if (sr.getPatientEmail() == null || !email.equals(sr.getPatientEmail())) {
			throw new IllegalStateException("Unauthorized slot access");
		}
	}
}
