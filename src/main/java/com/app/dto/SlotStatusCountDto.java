package com.app.dto;

import com.app.constraints.SlotStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // ✅ ADD THIS (important for frameworks)
public class SlotStatusCountDto {

	private SlotStatus status;
	private Long count;
}