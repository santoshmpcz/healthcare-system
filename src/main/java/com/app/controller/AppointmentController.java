package com.app.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.AppointmentDTO;
import com.app.dto.DoctorDTO;
import com.app.service.AppointmentService;
import com.app.service.DoctorService;
import com.app.service.SpecializationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {

	private static final String DEFAULT_PAGE_SIZE = "10";
	private static final int MAX_PAGE_SIZE = 20;

	private final AppointmentService service;
	private final DoctorService doctorService;
	private final SpecializationService specializationService;

	// ================= COMMON UI =================
	private void commonUi(Model model) {
		model.addAttribute("doctors", doctorService.getDoctorIdAndNameMap());
		model.addAttribute("specializations", specializationService.getSpecIdAndName());
	}

	private Pageable pageable(int page, int size, Sort sort) {
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
		return PageRequest.of(safePage, safeSize, sort);
	}

	private boolean isPastDate(LocalDate date) {
		return date != null && date.isBefore(LocalDate.now());
	}

	// ================= REGISTER =================
	@GetMapping("/register")
	public String showRegister(Model model) {
		model.addAttribute("appointment", new AppointmentDTO());
		commonUi(model);
		return "AppointmentRegister";
	}

	// ================= SAVE =================
	@PostMapping("/save")
	public String saveAppointment(@Valid @ModelAttribute("appointment") AppointmentDTO dto, BindingResult errors,
			RedirectAttributes attributes, Model model) {

		if (errors.hasErrors()) {
			commonUi(model);
			return "AppointmentRegister";
		}

		if (dto.getDoctorId() == null) {
			attributes.addFlashAttribute("message", "Please select doctor");
			return "redirect:/appointment/register";
		}

		if (isPastDate(dto.getDate())) {
			attributes.addFlashAttribute("message", "Appointment date cannot be in the past");
			return "redirect:/appointment/register";
		}

		service.createAppointment(dto);

		attributes.addFlashAttribute("message", "Appointment created successfully");
		return "redirect:/appointment/register";
	}

	// ================= VIEW ALL =================
	@GetMapping("/all")
	public String getAllAppointments(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size, Model model) {

		Page<AppointmentDTO> pageData = service.getAllAppointments(pageable(page, size, Sort.by("date").descending()));

		model.addAttribute("page", pageData);
		model.addAttribute("currentPage", pageData.getNumber());
		model.addAttribute("totalPages", pageData.getTotalPages());
		model.addAttribute("size", size);

		return "AppointmentData";
	}

	// ================= DELETE =================
	@PostMapping("/delete")
	public String deleteAppointment(@RequestParam Long id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size, RedirectAttributes attributes) {

		service.deleteAppointment(id);

		attributes.addFlashAttribute("message", "Appointment deleted successfully");

		return "redirect:/appointment/all?page=" + page + "&size=" + size;
	}

	// ================= EDIT =================
	@GetMapping("/edit/{id}")
	public String showEditAppointment(@PathVariable Long id, Model model) {

		AppointmentDTO dto = service.getAppointmentById(id);

		model.addAttribute("appointment", dto);
		commonUi(model);

		return "AppointmentEdit";
	}

	// ================= UPDATE =================
	@PostMapping("/update")
	public String updateAppointment(@RequestParam Long id, @Valid @ModelAttribute("appointment") AppointmentDTO dto,
			BindingResult errors, RedirectAttributes attributes, Model model) {

		if (errors.hasErrors()) {
			commonUi(model);
			return "AppointmentEdit";
		}

		if (dto.getDoctorId() == null) {
			attributes.addFlashAttribute("message", "Doctor is required");
			return "redirect:/appointment/edit/" + id;
		}

		if (isPastDate(dto.getDate())) {
			attributes.addFlashAttribute("message", "Appointment date cannot be in the past");
			return "redirect:/appointment/edit/" + id;
		}

		service.updateAppointment(id, dto);

		attributes.addFlashAttribute("message", "Appointment updated successfully");
		return "redirect:/appointment/all";
	}

	// ================= SEARCH DOCTORS =================
	@GetMapping("/view")
	public String searchDoctors(@RequestParam(required = false) String name,
			@RequestParam(required = false) Long specId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size, Model model) {

		commonUi(model);

		DoctorDTO filter = new DoctorDTO();
		filter.setFirstName(name == null || name.isBlank() ? null : name.trim());
		filter.setSpecializationId(specId);

		Page<DoctorDTO> pageData = doctorService.searchDoctors(filter,
				pageable(page, size, Sort.by("firstName").ascending()));

		model.addAttribute("pageData", pageData);
		model.addAttribute("docList", pageData.getContent());
		model.addAttribute("currentPage", pageData.getNumber());
		model.addAttribute("totalPages", pageData.getTotalPages());

		return "AppointmentSearch";
	}

	// ================= VIEW SLOTS =================
	@GetMapping("/viewSlot")
	public String showSlots(@RequestParam Long id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size, Model model) {

		Page<AppointmentDTO> pageData = service.searchAppointments(id, LocalDate.now(), null,
				pageable(page, size, Sort.by("date").ascending()));

		DoctorDTO doc = doctorService.getDoctorById(id)
				.orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

		model.addAttribute("page", pageData);
		model.addAttribute("doctorId", id); // important for pagination links
		model.addAttribute("doctorName", "Dr. " + doc.getFirstName() + " " + doc.getLastName());
		model.addAttribute("currentPage", pageData.getNumber());
		model.addAttribute("totalPages", pageData.getTotalPages());

		return "AppointmentSlots";
	}

	// ================= CURRENT DOCTOR =================
	@GetMapping("/currentDoc")
	public String getCurrentDocAppointments(Model model, Principal principal) {

		if (principal == null) {
			return "redirect:/login";
		}

		model.addAttribute("list", service.getAppointmentsByDoctorEmail(principal.getName()));

		return "AppointmentForDoctor";
	}
}