package com.app.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // ✅ IMPORTANT
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.app.domain.Document;
import com.app.service.DocumentService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/doc")
public class DocumentController {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);

	@Autowired
	private DocumentService service;

	// ================= VIEW PAGE =================
	@GetMapping("/all")
	public String showDocs(Model model) {
		model.addAttribute("idVal", System.currentTimeMillis());

		List<Object[]> list = service.getDocumentIdAndName();
		model.addAttribute("list", list);

		return "Documents";
	}

	// ================= UPLOAD (FIXED) =================
	@PostMapping("/upload") // ✅ FIXED
	public String uploadDoc(@RequestParam("docId") Long docId, @RequestParam("docOb") MultipartFile docOb,
			Model model) {

		try {
			if (docOb.isEmpty()) {
				model.addAttribute("message", "Please select a file!");
				return "redirect:all";
			}

			Document doc = new Document();
			doc.setDocId(docId);
			doc.setDocName(docOb.getOriginalFilename());
			doc.setDocData(docOb.getBytes());

			service.saveDocument(doc);

			LOG.info("File uploaded successfully: {}", docOb.getOriginalFilename());

		} catch (Exception e) {
			LOG.error("Error uploading file", e);
		}

		return "redirect:all";
	}

	// ================= DOWNLOAD =================
	@GetMapping("/download")
	public void downloadDoc(@RequestParam Long id, HttpServletResponse response) {

		try {
			Document doc = service.getDocumentById(id);

			response.setHeader("Content-Disposition", "attachment;filename=" + doc.getDocName());

			FileCopyUtils.copy(doc.getDocData(), response.getOutputStream());

		} catch (Exception e) {
			LOG.error("Error downloading file", e);
		}
	}

	// ================= DELETE =================
	@GetMapping("/delete")
	public String deleteDoc(@RequestParam Long id) {

		try {
			service.deleteDocumentById(id);
			LOG.info("Document deleted: {}", id);

		} catch (RuntimeException e) {
			LOG.error("Error deleting document", e);
		}

		return "redirect:all";
	}
}