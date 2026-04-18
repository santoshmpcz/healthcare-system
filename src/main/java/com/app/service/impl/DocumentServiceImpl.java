package com.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.domain.Document;
import com.app.repository.DocumentRepository;
import com.app.service.DocumentService;

@Service
public class DocumentServiceImpl implements DocumentService {

	@Autowired
	private DocumentRepository repo;

	@Override
	public void saveDocument(Document doc) {
		repo.save(doc);

	}

	@Override
	public List<Object[]> getDocumentIdAndName() {
		return repo.getDocumentIdAndName();
	}

	@Override
	public void deleteDocumentById(Long id) {

		if (repo.existsById(id))
			repo.deleteById(id);

		else
			throw new RuntimeException("Document does not exist with id");
	}

	@Override
	public Document getDocumentById(Long id) {
		return repo.findById(id).orElseThrow(() -> new RuntimeException("Document does not exist with id"));
	}

}
