package com.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotRequestDTO {

	@NotNull(message = "Appointment ID is required")
	private Long appointmentId;

	@NotNull(message = "Patient ID is required")
	private Long patientId;
}