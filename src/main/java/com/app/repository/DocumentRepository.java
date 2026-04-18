package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.domain.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
	
	/*
	 * @Query("SELECT docId,docName, FROM Document") List<Object[]>
	 * getDocumentIdAndName();
	 */
	
	@Query("SELECT d.docId, d.docName FROM Document d")
	List<Object[]> getDocumentIdAndName();

}
