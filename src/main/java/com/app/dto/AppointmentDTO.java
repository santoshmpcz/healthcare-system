package com.app.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.app.constraints.SlotStatus;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

	private Long id;

	// ✅ Only Doctor reference ID (Best Practice)
	private Long doctorId;

	// ✅ For UI display only
	private String doctorName;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	private Integer noOfSlots;

	private String details;

	private Double fee;

	private SlotStatus status;
}