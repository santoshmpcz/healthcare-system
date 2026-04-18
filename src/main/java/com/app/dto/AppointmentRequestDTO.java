package com.app.dto;

import java.time.LocalDate;

import com.app.constraints.SlotStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequestDTO {

	@NotNull
	private Long doctorId;

	@NotNull
	@FutureOrPresent
	private LocalDate date;

	@NotNull
	@Min(1)
	@Max(200)
	private Integer noOfSlots;

	private String details;

	@DecimalMin(value = "0.0")
	private Double fee;

	private SlotStatus status;
}