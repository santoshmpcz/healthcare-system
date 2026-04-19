package com.app.dto;

import com.app.constraints.AppointmentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SlotStatusCountDto {

	private AppointmentStatus status;
	private Long count;

	public SlotStatusCountDto(AppointmentStatus status, Long count) {
		this.status = status;
		this.count = count;
	}
}
