package com.app.mapper;

import com.app.constraints.AppointmentStatus;
import com.app.constraints.PaymentStatus;
import com.app.domain.SlotRequest;
import com.app.dto.SlotRequestResponseDTO;

public class SlotRequestMapper {

	public static SlotRequestResponseDTO toDto(SlotRequest sr) {

		if (sr == null)
			return null;

		var appointment = sr.getAppointment();
		var patient = sr.getPatient();
		var doctor = (appointment != null) ? appointment.getDoctor() : null;

		PaymentStatus paymentStatus = sr.getLatestPaymentStatus();
		AppointmentStatus status = sr.getStatus() != null ? sr.getStatus() : AppointmentStatus.PENDING;

		// ✅ UI LOGIC HERE (CORRECT PLACE)
		boolean showPay = status == AppointmentStatus.ACCEPTED && paymentStatus != PaymentStatus.SUCCESS;
		boolean showCancel = status == AppointmentStatus.PENDING;
		boolean showInvoice = paymentStatus == PaymentStatus.SUCCESS;
		boolean showNoAction = !showPay && !showCancel && !showInvoice;

		return SlotRequestResponseDTO.builder()

				.id(sr.getId()).appointmentId(appointment != null ? appointment.getId() : null)
				.patientId(patient != null ? patient.getId() : null)

				.patientFirstName(patient != null ? patient.getFirstName() : null)
				.patientLastName(patient != null ? patient.getLastName() : null)
				.patientEmail(patient != null ? patient.getEmail() : null)

				.doctorFirstName(doctor != null ? doctor.getFirstName() : null)
				.doctorLastName(doctor != null ? doctor.getLastName() : null)
				.doctorEmail(doctor != null ? doctor.getEmail() : null)

				.appointmentDate(
						appointment != null && appointment.getDate() != null ? appointment.getDate().toString() : null)
				.fee(appointment != null ? appointment.getFee() : 0.0)

				.status(status).paymentStatus(paymentStatus)

				// ✅ UI FLAGS
				.showPayButton(showPay).showCancelButton(showCancel).showInvoiceButton(showInvoice)
				.showNoAction(showNoAction).build();
	}
}
