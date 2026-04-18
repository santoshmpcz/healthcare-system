package com.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecializationDTO {

	private Long id;

	@NotBlank(message = "Spec Code is required")
	@Size(max = 10, message = "Spec Code must be max 10 characters")
	private String specCode;

	@NotBlank(message = "Spec Name is required")
	@Size(max = 60, message = "Spec Name must be max 60 characters")
	private String specName;

	@NotBlank(message = "Spec Note is required")
	@Size(max = 250, message = "Spec Note must be max 250 characters")
	private String specNote;

	// ✅ ICON (FontAwesome class)
	@Size(max = 50, message = "Icon must be valid FontAwesome class")
	private String icon;

	// ✅ COLOR (HEX validation added)
	@Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be valid HEX value")
	@Size(max = 20, message = "Color must be valid HEX code")
	private String color;
}