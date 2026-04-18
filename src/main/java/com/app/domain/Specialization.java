package com.app.domain;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "specialization_tab")
public class Specialization {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "specialization_seq_tab")
	@SequenceGenerator(name = "specialization_seq_tab", sequenceName = "specialization_seq_tab", allocationSize = 1)
	private Long id;

	@Column(name = "spec_code_col", length = 10, nullable = false, unique = true)
	private String specCode;

	@Column(name = "spec_name_col", length = 60, nullable = false, unique = true)
	private String specName;

	@Column(name = "spec_not_col", length = 250, nullable = false)
	private String specNote;

	@Column(name = "icon_col", length = 50)
	private String icon;

	@Column(name = "color_col", length = 20)
	private String color;

	// optional bidirectional mapping
	@OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
	private List<Doctor> doctors;

}