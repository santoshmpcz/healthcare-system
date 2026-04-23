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
				dto.setPhoto(fileStorageUtil.saveFile(file));
			}

			DoctorDTO saved = doctorService.createDoctor(dto);

			attributes.addFlashAttribute("successMessage",
					"Doctor created successfully with ID: " + saved.getDoctorCode());

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

		try {
			DoctorDTO doctor = doctorService.getDoctorById(id)
					.orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

			model.addAttribute("doctor", doctor);
			return "doctor-view";

		} catch (DoctorNotFoundException e) {
			attributes.addFlashAttribute("errorMessage", "Doctor not found");
			return "redirect:/doctor/all";
		}
	}

	/* ================= VIEW ALL ================= */

	@GetMapping({ "/all", "/" })
	public String viewAllDoctors(@RequestParam(required = false) Long specializationId,
			@RequestParam(required = false) String name, @RequestParam(required = false) Integer experience,
			@RequestParam(required = false) String mobile, @RequestParam(required = false) String address,
			@PageableDefault(size = 6, sort = "id") Pageable pageable, Model model) {

		boolean isFilterApplied = specializationId != null || hasText(name) || experience != null || hasText(mobile)
				|| hasText(address);

		Page<DoctorDTO> pageData;

		if (isFilterApplied) {

			DoctorDTO filter = new DoctorDTO();
			filter.setSpecializationId(specializationId);
			filter.setFirstName(name);
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

		loadUiData(model);

		return LIST_PAGE;
	}

	/* ================= DELETE ================= */

	@PostMapping("/delete/{id}")
	public String deleteDoctor(@PathVariable Long id, RedirectAttributes attributes) {

		try {
			doctorService.deleteDoctor(id);
			attributes.addFlashAttribute("successMessage", "Doctor deleted successfully");

		} catch (DoctorNotFoundException e) {
			attributes.addFlashAttribute("errorMessage", e.getMessage());

		} catch (Exception e) {
			log.error("Error deleting doctor", e);
			attributes.addFlashAttribute("errorMessage", "Unable to delete doctor");
		}

		return "redirect:/doctor/all";
	}

	/* ================= EDIT ================= */

	@GetMapping("/edit")
	public String showEditPage(@RequestParam Long id, Model model, RedirectAttributes attributes) {

		try {
			DoctorDTO doctor = doctorService.getDoctorById(id)
					.orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

			model.addAttribute("doctor", doctor);
			loadUiData(model);
			return EDIT_PAGE;

		} catch (DoctorNotFoundException e) {
			attributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/doctor/all";
		}
	}

	/* ================= UPDATE ================= */

	@PostMapping("/update")
	public String updateDoctor(@Valid @ModelAttribute("doctor") DoctorDTO dto, BindingResult result,
			@RequestParam(required = false) MultipartFile file, Model model, RedirectAttributes attributes) {

		if (dto.getId() == null) {
			attributes.addFlashAttribute("errorMessage", "Invalid Doctor ID");
			return "redirect:/doctor/all";
		}

		DoctorDTO existing = doctorService.getDoctorById(dto.getId())
				.orElseThrow(() -> new DoctorNotFoundException("Doctor Not Found"));

		// ✅ KEEP IMAGE EVEN IF VALIDATION FAILS
		if (result.hasErrors()) {
			dto.setPhoto(existing.getPhoto());
			loadUiData(model);
			return EDIT_PAGE;
		}

		try {

			if (file != null && !file.isEmpty()) {

				// ✅ DELETE OLD IMAGE
				if (existing.getPhoto() != null) {
					fileStorageUtil.deleteFile(existing.getPhoto());
				}

				// ✅ SAVE NEW IMAGE
				dto.setPhoto(fileStorageUtil.saveFile(file));

			} else {
				dto.setPhoto(existing.getPhoto());
			}

			doctorService.updateDoctor(dto.getId(), dto);

			attributes.addFlashAttribute("successMessage", "Doctor updated successfully");

		} catch (Exception e) {
			log.error("Error updating doctor", e);
			attributes.addFlashAttribute("errorMessage", "Update failed");
		}

		return "redirect:/doctor/all";
	}
}