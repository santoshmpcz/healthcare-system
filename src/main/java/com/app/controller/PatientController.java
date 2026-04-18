package com.app.controller;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.domain.Patient;
import com.app.service.PatientService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/patient")
public class PatientController {

	private final PatientService service;

	public PatientController(PatientService service) {
		this.service = service;
	}

	// ================= REGISTER PAGE =================
	@GetMapping("/register")
	public String registerPage(Model model) {

		if (!model.containsAttribute("patient")) {
			model.addAttribute("patient", new Patient());
		}

		return "PatientRegister";
	}

	// ================= SAVE =================
	@PostMapping("/save")
	public String savePatient(@Valid @ModelAttribute("patient") Patient patient, BindingResult result,
			RedirectAttributes attributes, Model model) {

		if (result.hasErrors()) {
			model.addAttribute("patient", patient);
			return "PatientRegister";
		}

		try {
			service.registerPatient(patient);

			attributes.addFlashAttribute("successMessage",
					"Patient Registered Successfully! Login details sent to your email.");

		} catch (RuntimeException e) {

			attributes.addFlashAttribute("errorMessage", e.getMessage());
			attributes.addFlashAttribute("patient", patient);
		}

		return "redirect:/patient/register";
	}

	// ================= VIEW WITH PAGINATION =================
	@GetMapping({ "/all", "/list" })
	public String viewAllPatients(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, Model model) {

		Pageable pageable = PageRequest.of(page, size);

		Page<Patient> pageData = service.findAll(pageable);

		model.addAttribute("pageData", pageData);
		model.addAttribute("list", pageData.getContent());

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", pageData.getTotalPages());
		model.addAttribute("totalItems", pageData.getTotalElements());
		model.addAttribute("size", size);

		return "PatientData";
	}

	// ================= DELETE =================
	@GetMapping("/delete")
	public String deletePatient(@RequestParam Long id, RedirectAttributes attributes) {

		try {
			service.deleteById(id);
			attributes.addFlashAttribute("successMessage", "Patient deleted successfully");

		} catch (RuntimeException e) {
			attributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/patient/all";
	}

	// ================= EDIT =================
	@GetMapping("/edit")
	public String editPatient(@RequestParam Long id, Model model, RedirectAttributes attributes) {

		try {
			Patient patient = service.findById(id).orElseThrow(() -> new RuntimeException("Patient Not Found"));

			model.addAttribute("patient", patient);
			return "PatientEdit";

		} catch (RuntimeException e) {

			attributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/patient/all";
		}
	}

	// ================= UPDATE =================
	@PostMapping("/update")
	public String updatePatient(@Valid @ModelAttribute("patient") Patient patient, BindingResult result,
			RedirectAttributes attributes, Model model) {

		if (result.hasErrors()) {
			model.addAttribute("patient", patient);
			return "PatientEdit";
		}

		try {

			Patient existing = service.findById(patient.getId())
					.orElseThrow(() -> new RuntimeException("Patient not found"));

			// Preserve user mapping
			patient.setUser(existing.getUser());

			// Preserve DOB if empty
			if (patient.getDateOfBirth() == null) {
				patient.setDateOfBirth(existing.getDateOfBirth());
			}

			boolean isChanged = !Objects.equals(existing.getFirstName(), patient.getFirstName())
					|| !Objects.equals(existing.getMiddleName(), patient.getMiddleName())
					|| !Objects.equals(existing.getLastName(), patient.getLastName())
					|| !Objects.equals(existing.getGender(), patient.getGender())
					|| !Objects.equals(existing.getDateOfBirth(), patient.getDateOfBirth())
					|| !Objects.equals(existing.getMaritalStatus(), patient.getMaritalStatus())
					|| !Objects.equals(existing.getBloodGroup(), patient.getBloodGroup())
					|| !Objects.equals(existing.getAadhaarNo(), patient.getAadhaarNo())
					|| !Objects.equals(existing.getEmail(), patient.getEmail())
					|| !Objects.equals(existing.getMobileNo(), patient.getMobileNo())
					|| !Objects.equals(existing.getDiseases(), patient.getDiseases())
					|| !Objects.equals(existing.getWardNo(), patient.getWardNo())
					|| !Objects.equals(existing.getRoomNo(), patient.getRoomNo())
					|| !Objects.equals(existing.getBillAmount(), patient.getBillAmount())
					|| !Objects.equals(existing.getBlock(), patient.getBlock())
					|| !Objects.equals(existing.getDistrict(), patient.getDistrict())
					|| !Objects.equals(existing.getState(), patient.getState())
					|| !Objects.equals(existing.getNationality(), patient.getNationality())
					|| !Objects.equals(existing.getReligion(), patient.getReligion())
					|| !Objects.equals(existing.getCountry(), patient.getCountry())
					|| !Objects.equals(existing.getMedicalHistory(), patient.getMedicalHistory())
					|| !Objects.equals(existing.getHistory(), patient.getHistory())
					|| !Objects.equals(existing.getNote(), patient.getNote())
					|| !Objects.equals(existing.getAddress(), patient.getAddress());

			if (isChanged) {

				service.updatePatient(patient);
				attributes.addFlashAttribute("successMessage", "Patient updated successfully!");

			} else {

				attributes.addFlashAttribute("infoMessage", "No changes detected, record remains same.");
			}

		} catch (RuntimeException e) {

			attributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/patient/all";
	}
}