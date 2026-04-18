package com.app.dto;

import java.time.LocalDate;

import com.app.constraints.SlotStatus;

import lombok.Data;

@Data
public class AppointmentResponseDTO {

    private Long id;

    private String doctorCode;

    private String doctorName;   // ✅ REQUIRED FOR UI

    private LocalDate date;

    private Integer noOfSlots;

    private String details;

    private Double fee;

    private SlotStatus status;

    // computed value
    private Integer availableSlots;
}