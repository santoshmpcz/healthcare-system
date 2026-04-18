package com.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "doc_tab")
public class Document {

	@Id

	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doc_seq_tab")
	@SequenceGenerator(name = "doc_seq_tab", sequenceName = "doc_seq_tab")

	private Long docId;

	@Column(name = "doc_name_col")
	private String docName;

	@Lob
	@Column(name = "doc_data_ol")
	private byte[] docData;

}
