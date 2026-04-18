package com.app.dto;

import com.app.constraints.AppointmentStatus;
import com.app.constraints.PaymentStatus;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotRequestResponseDTO {

	// ================= IDENTIFIERS =================
	private Long id;
	private Long appointmentId;
	private Long patientId;

	// ================= APPOINTMENT =================
	private String appointmentDate;
	private Double fee;

	// ================= PATIENT =================
	private String patientFirstName;
	private String patientLastName;
	private String patientEmail;

	// ================= DOCTOR =================
	private String doctorFirstName;
	private String doctorLastName;
	private String doctorEmail;

	// ================= STATUS =================
	private AppointmentStatus status;
	private PaymentStatus paymentStatus;

	// ================= UI FLAGS =================
	private boolean showPayButton;
	private boolean showCancelButton;
	private boolean showInvoiceButton;
	private boolean showNoAction;

	// ================= UI BADGES =================
	private String statusBadgeClass;
	private String paymentBadgeClass;

	// ================= COMPUTED FIELDS =================

	public String getPatientName() {
		return ((patientFirstName != null ? patientFirstName : "") + " "
				+ (patientLastName != null ? patientLastName : "")).trim();
	}

	public String getDoctorName() {
		return ((doctorFirstName != null ? doctorFirstName : "") + " " + (doctorLastName != null ? doctorLastName : ""))
				.trim();
	}

	// ================= MAIN UI LOGIC (VERY IMPORTANT) =================
	public void computeUiFields() {

		// ---------- DEFAULT SAFETY ----------
		if (status == null)
			status = AppointmentStatus.PENDING;
		if (paymentStatus == null)
			paymentStatus = PaymentStatus.INITIATED;

		// ---------- STATUS BADGE ----------
		switch (status) {
		case ACCEPTED -> statusBadgeClass = "status-accepted";
		case REJECTED -> statusBadgeClass = "status-rejected";
		case PENDING -> statusBadgeClass = "status-pending";
		case CANCELLED -> statusBadgeClass = "status-cancelled";
		default -> statusBadgeClass = "status-pending";
		}

		// ---------- PAYMENT BADGE ----------
		switch (paymentStatus) {
		case SUCCESS -> paymentBadgeClass = "pay-paid";
		case FAILED -> paymentBadgeClass = "pay-failed";
		case REFUNDED -> paymentBadgeClass = "pay-refunded";
		case REFUND_PENDING -> paymentBadgeClass = "pay-refunded";
		case CANCELLED -> paymentBadgeClass = "pay-cancelled";
		default -> paymentBadgeClass = "pay-unpaid";
		}

		// ---------- BUTTON LOGIC ----------
		showPayButton = false;
		showCancelButton = false;
		showInvoiceButton = false;
		showNoAction = false;

		if (status == AppointmentStatus.ACCEPTED) {

			if (paymentStatus == PaymentStatus.INITIATED || paymentStatus == PaymentStatus.FAILED) {

				showPayButton = true;
				showCancelButton = true;

			} else if (paymentStatus == PaymentStatus.SUCCESS) {

				showInvoiceButton = true;

			} else {
				showNoAction = true;
			}

		} else {
			showNoAction = true;
		}
	}
}
