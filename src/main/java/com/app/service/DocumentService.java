package com.app.service;

import java.util.List;

import com.app.domain.Document;

public interface DocumentService {

	void saveDocument(Document doc);

	List<Object[]> getDocumentIdAndName();

	void deleteDocumentById(Long id);

	Document getDocumentById(Long id);

}
