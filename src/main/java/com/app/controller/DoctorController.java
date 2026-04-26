package com.app.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.DoctorDTO;
import com.app.exception.DoctorNotFoundException;
import com.app.service.DoctorService;
import com.app.service.SpecializationService;
import com.app.util.FileStorageUtil;
import com.lowagie.text.pdf.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
@Slf4j
public class DoctorController {

	private final DoctorService doctorService;
	private final SpecializationService specializationService;
	private final FileStorageUtil fileStorageUtil;

	private static final String REGISTER_PAGE = "DoctorRegister";
	private static final String EDIT_PAGE = "DoctorEdit";
	private static final String LIST_PAGE = "DoctorData";

	/* ================= COMMON ================= */

	private void loadUiData(Model model) {
		model.addAttribute("specList", specializationService.getAllSpecializations());
	}

	private boolean hasText(String val) {
		return val != null && !val.trim().isEmpty();
	}

	/* ================= REGISTER ================= */

	@GetMapping("/register")
	public String showRegister(Model model) {

		if (!model.containsAttribute("doctor")) {
			model.addAttribute("doctor", new DoctorDTO());
		}

		loadUiData(model);
		return REGISTER_PAGE;
	}

	/* ================= SAVE ================= */

	@PostMapping("/save")
	public String saveDoctor(@Valid @ModelAttribute("doctor") DoctorDTO dto, BindingResult result,
			@RequestParam(required = false) MultipartFile file, Model model, RedirectAttributes attributes) {

		if (result.hasErrors()) {
			loadUiData(model);
			return REGISTER_PAGE;
		}

		try {
			if (file != null && !file.isEmpty()) {
				dto.setImageUrl(fileStorageUtil.saveFile(file));
			}

			DoctorDTO saved = doctorService.createDoctor(dto);

			attributes.addFlashAttribute("successMessage",
					"Doctor created successfully with ID: " + saved.getDoctorCode());

		} catch (RuntimeException e) {
			attributes.addFlashAttribute("doctor", dto);
			attributes.addFlashAttribute("errorMessage", e.getMessage());

		} catch (Exception e) {
			log.error("Error while saving doctor", e);
			attributes.addFlashAttribute("doctor", dto);
			attributes.addFlashAttribute("errorMessage", "Unable to save doctor");
		}

		return "redirect:/doctor/register";
	}

	/* ================= VIEW ================= */

	@GetMapping("/view")
	public String viewDoctor(@RequestParam Long id, Model model, RedirectAttributes attributes) {

		return doctorService.getDoctorById(id).map(doc -> {
			model.addAttribute("doctor", doc);
			return "doctor-view";
		}).orElseGet(() -> {
			attributes.addFlashAttribute("errorMessage", "Doctor not found");
			return "redirect:/doctor/all";
		});
	}

	/* ================= VIEW ALL ================= */

	@GetMapping({ "/all", "/" })
	public String viewAllDoctors(@RequestParam(required = false) Long specializationId,
			@RequestParam(required = false) String keyword, // ✅ changed
			@RequestParam(required = false) Integer experience, @RequestParam(required = false) String mobile,
			@RequestParam(required = false) String address, @PageableDefault(size = 6, sort = "id") Pageable pageable,
			Model model) {

		boolean isFilterApplied = specializationId != null || hasText(keyword) || experience != null || hasText(mobile)
				|| hasText(address);

		Page<DoctorDTO> pageData;

		if (isFilterApplied) {
			DoctorDTO filter = new DoctorDTO();
			filter.setFirstName(keyword); // using as search key
			filter.setSpecializationId(specializationId);
			filter.setExperience(experience);
			filter.setMobile(mobile);
			filter.setAddress(address);

			pageData = doctorService.searchDoctors(filter, pageable);
		} else {
			pageData = doctorService.getAllDoctors(pageable);
		}

		model.addAttribute("list", pageData.getContent());
		model.addAttribute("currentPage", pageData.getNumber());
		model.addAttribute("totalPages", pageData.getTotalPages());

		//  keep filters in UI
		model.addAttribute("keyword", keyword);
		model.addAttribute("specializationId", specializationId);
		model.addAttribute("experience", experience);
		model.addAttribute("mobile", mobile);
		model.addAttribute("address", address);

		loadUiData(model);
		return LIST_PAGE;
	}

	/* ================= DELETE ================= */

	@PostMapping("/delete/{id}")
	public String deleteDoctor(@PathVariable Long id, RedirectAttributes attributes) {

		try {
			doctorService.deleteDoctor(id);
			attributes.addFlashAttribute("successMessage", "Doctor deleted successfully");

		} catch (Exception e) {
			log.error("Error deleting doctor", e);
			attributes.addFlashAttribute("errorMessage", "Unable to delete doctor");
		}

		return "redirect:/doctor/all";
	}

	/* ================= EDIT ================= */

	@GetMapping("/edit")
	public String showEditPage(@RequestParam Long id, Model model, RedirectAttributes attributes) {

		return doctorService.getDoctorById(id).map(doc -> {
			model.addAttribute("doctor", doc);
			loadUiData(model);
			return EDIT_PAGE;
		}).orElseGet(() -> {
			attributes.addFlashAttribute("errorMessage", "Doctor not found");
			return "redirect:/doctor/all";
		});
	}

	/* ================= UPDATE ================= */

	@PostMapping("/update")
	public String updateDoctor(@Valid @ModelAttribute("doctor") DoctorDTO dto, BindingResult result,
			@RequestParam(required = false) MultipartFile file, Model model, RedirectAttributes attributes) {

		if (dto.getId() == null) {
			attributes.addFlashAttribute("errorMessage", "Invalid Doctor ID");
			return "redirect:/doctor/all";
		}

		try {
			DoctorDTO existing = doctorService.getDoctorById(dto.getId())
					.orElseThrow(() -> new DoctorNotFoundException("Doctor Not Found"));

			if (result.hasErrors()) {
				dto.setImageUrl(existing.getImageUrl());
				loadUiData(model);
				return EDIT_PAGE;
			}

			if (file != null && !file.isEmpty()) {

				if (existing.getImageUrl() != null) {
					fileStorageUtil.deleteFile(existing.getImageUrl());
				}

				dto.setImageUrl(fileStorageUtil.saveFile(file));

			} else {
				dto.setImageUrl(existing.getImageUrl());
			}

			doctorService.updateDoctor(dto.getId(), dto);

			attributes.addFlashAttribute("successMessage", "Doctor updated successfully");

		} catch (RuntimeException e) {
			attributes.addFlashAttribute("errorMessage", e.getMessage());

		} catch (Exception e) {
			log.error("Error updating doctor", e);
			attributes.addFlashAttribute("errorMessage", "Update failed");
		}

		return "redirect:/doctor/all";
	}

	/*
	 * ================= EXPORT CSV =================
	 */
	@GetMapping("/export")
	public void exportDoctors(HttpServletResponse response) throws IOException {

		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=doctors.csv");

		List<DoctorDTO> list = doctorService.getAllDoctorsForExport();

		response.getWriter().write("ID,Name,Email,Mobile,Specialization\n");

		for (DoctorDTO d : list) {

			String name = (d.getFirstName() + " " + d.getLastName()).replace(",", " ");
			String email = hasText(d.getEmail()) ? d.getEmail().replace(",", " ") : "";
			String mobile = hasText(d.getMobile()) ? d.getMobile() : "";
			String spec = hasText(d.getSpecializationName()) ? d.getSpecializationName() : "";

			response.getWriter().write(d.getId() + "," + name + "," + email + "," + mobile + "," + spec + "\n");
		}
	}

	/*
	 * ================= EXPORT PDF =================
	 */
	@GetMapping("/export/pdf")
	public void exportDoctorsPdf(HttpServletResponse response) throws IOException {

		List<DoctorDTO> list = doctorService.getAllDoctorsForExport();

		try {

			PdfReader reader = new PdfReader("src/main/resources/static/myres/asmita.pdf");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfStamper stamper = new PdfStamper(reader, baos);

			BaseFont normalFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			BaseFont boldFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

			float startY = 500f;
			float rowHeight = 16f;
			float footerLimit = 120f;

			int pageNumber = 1;

			PdfContentByte content = stamper.getOverContent(pageNumber);

			float y = startY;

			/* ================= HEADER ================= */

			float headerY = y - 6;

			content.beginText();
			content.setFontAndSize(boldFont, 11);

			content.setTextMatrix(50, headerY);
			content.showText("Sr.");

			content.setTextMatrix(100, headerY);
			content.showText("Name");

			content.setTextMatrix(250, headerY);
			content.showText("Email");

			content.setTextMatrix(420, headerY);
			content.showText("Mobile");

			content.endText();

			y = headerY - (rowHeight * 2);

			/* ================= DATA ================= */

			for (DoctorDTO d : list) {

				if (y <= footerLimit) {

					stamper.insertPage(++pageNumber, reader.getPageSize(1));

					PdfImportedPage importedPage = stamper.getImportedPage(reader, 1);
					PdfContentByte newPage = stamper.getUnderContent(pageNumber);
					newPage.addTemplate(importedPage, 0, 0);

					content = stamper.getOverContent(pageNumber);

					y = startY;
					headerY = y - 6;

					// HEADER AGAIN
					content.beginText();
					content.setFontAndSize(boldFont, 11);

					content.setTextMatrix(50, headerY);
					content.showText("Sr.");

					content.setTextMatrix(100, headerY);
					content.showText("Name");

					content.setTextMatrix(250, headerY);
					content.showText("Email");

					content.setTextMatrix(420, headerY);
					content.showText("Mobile");

					content.endText();

					y = headerY - (rowHeight * 2);
				}

				/* ================= PRINT DATA ================= */

				content.beginText();
				content.setFontAndSize(normalFont, 10);

				content.setTextMatrix(50, y);
				content.showText(String.valueOf(d.getId()));

				content.setTextMatrix(100, y);
				content.showText((d.getFirstName() + " " + d.getLastName()).trim());

				content.setTextMatrix(250, y);
				content.showText(hasText(d.getEmail()) ? d.getEmail() : "");

				content.setTextMatrix(420, y);
				content.showText(hasText(d.getMobile()) ? d.getMobile() : "");

				content.endText();

				y -= rowHeight;
			}

			stamper.close();
			reader.close();

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=asmita.pdf");

			response.getOutputStream().write(baos.toByteArray());

		} catch (Exception e) {
			log.error("PDF export error", e);
		}
	}
}