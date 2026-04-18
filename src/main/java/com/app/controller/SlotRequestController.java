package com.app.controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.app.constraints.AppointmentStatus;
import com.app.constraints.PaymentStatus;
import com.app.domain.SlotRequest;
import com.app.dto.AppointmentDTO;
import com.app.dto.SlotRequestResponseDTO;
import com.app.service.*;
import com.app.util.AdminDashboardUtil;
import com.app.view.InvoiceSlipPdfView;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/slots")
@RequiredArgsConstructor
public class SlotRequestController {

	private static final int PAGE_SIZE = 10;
	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	private final SlotRequestService service;
	private final AppointmentService appointmentService;
	private final PatientService patientService;
	private final DoctorService doctorService;
	private final SpecializationService specializationService;
	private final AdminDashboardUtil util;
	private final InvoiceSlipPdfView invoiceSlipPdfView;

	// ================= COMMON =================
	private Pageable getPageable(int page) {
		return PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
	}

	private boolean isAdmin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.getAuthorities().stream().anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
	}

	private String redirectLogin(Principal principal) {
		return principal == null ? "redirect:/login" : null;
	}

	// ================= BOOK =================
	@PostMapping("/book")
	public String bookSlot(@RequestParam Long appid, Principal principal, Model model) {

		String redirect = redirectLogin(principal);
		if (redirect != null)
			return redirect;

		try {
			Long patientId = patientService.findByEmail(principal.getName()).map(p -> p.getId())
					.orElseThrow(() -> new IllegalArgumentException("Patient not found"));

			service.bookSlot(appid, patientId);

			AppointmentDTO dto = appointmentService.getAppointmentById(appid);

			String dateStr = dto != null && dto.getDate() != null
					? dto.getDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
					: "N/A";

			model.addAttribute("message", "Slot booked successfully for " + dateStr);

		} catch (Exception ex) {
			model.addAttribute("message", ex.getMessage());
		}

		return "SlotRequestMessage";
	}

	// ================= ADMIN =================
	@GetMapping("/all")
	public String viewAllReq(@RequestParam(defaultValue = "0") int page, Principal principal, Model model) {

		String redirect = redirectLogin(principal);
		if (redirect != null)
			return redirect;

		if (!isAdmin()) {
			throw new IllegalStateException("Only admin can view all requests");
		}

		Page<SlotRequestResponseDTO> data = service.getAllSlotRequests(getPageable(page));

		model.addAttribute("list", data.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", data.getTotalPages());

		return "SlotRequestData";
	}

	// ================= PATIENT =================
	@GetMapping("/patient")
	public String viewMyReqPatient(@RequestParam(defaultValue = "0") int page, Principal principal, Model model) {

		String redirect = redirectLogin(principal);
		if (redirect != null)
			return redirect;

		Page<SlotRequestResponseDTO> data = service.getSlotsByPatientEmail(principal.getName(), getPageable(page));

		model.addAttribute("list", data.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", data.getTotalPages());

		return "SlotRequestDataPatient";
	}

	// ================= DOCTOR =================
	@GetMapping("/doctor")
	public String viewMyReqDoc(@RequestParam(defaultValue = "0") int page, Principal principal, Model model) {

		String redirect = redirectLogin(principal);
		if (redirect != null)
			return redirect;

		Page<SlotRequestResponseDTO> data = service.getSlotsByDoctorEmail(principal.getName(),
				AppointmentStatus.PENDING, getPageable(page));

		model.addAttribute("list", data.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", data.getTotalPages());

		return "SlotRequestDataDoctor";
	}

	// ================= ACCEPT =================
	@PostMapping("/accept")
	public String acceptSlot(@RequestParam Long id, Principal principal) {

		String redirect = redirectLogin(principal);
		if (redirect != null)
			return redirect;

		SlotRequest sr = service.getSlotRequestEntity(id);

		if (!isAdmin() && !principal.getName().equals(sr.getAppointment().getDoctor().getEmail())) {
			throw new IllegalStateException("Unauthorized doctor action");
		}

		service.updateSlotRequestStatus(id, AppointmentStatus.ACCEPTED);

		return isAdmin() ? "redirect:/slots/all" : "redirect:/slots/doctor";
	}

	// ================= REJECT =================
	@PostMapping("/reject")
	public String rejectSlot(@RequestParam Long id, Principal principal) {

		String redirect = redirectLogin(principal);
		if (redirect != null)
			return redirect;

		SlotRequest sr = service.getSlotRequestEntity(id);

		if (!isAdmin() && !principal.getName().equals(sr.getAppointment().getDoctor().getEmail())) {
			throw new IllegalStateException("Unauthorized doctor action");
		}

		service.updateSlotRequestStatus(id, AppointmentStatus.REJECTED);

		return isAdmin() ? "redirect:/slots/all" : "redirect:/slots/doctor";
	}

	// ================= CANCEL =================
	@PostMapping("/cancel")
	public String cancelSlot(@RequestParam Long id, Principal principal) {

		String redirect = redirectLogin(principal);
		if (redirect != null)
			return redirect;

		SlotRequest sr = service.getSlotRequestEntity(id);

		if (!principal.getName().equals(sr.getPatient().getEmail())) {
			throw new IllegalStateException("Unauthorized patient access");
		}

		service.cancelSlot(id);

		return "redirect:/slots/patient";
	}

	// ================= DASHBOARD =================
	@GetMapping("/dashboard")
	public String adminDashboard(Model model, Principal principal) {

		String redirect = redirectLogin(principal);
		if (redirect != null)
			return redirect;

		if (!isAdmin()) {
			throw new IllegalStateException("Only admin can access dashboard");
		}

		model.addAttribute("doctorCount", doctorService.getDoctorCount());
		model.addAttribute("patientCount", patientService.count());
		model.addAttribute("appointmentCount", appointmentService.getTotalAppointmentCount());
		model.addAttribute("specializationCount", specializationService.getSpecializationCount());

		String path = System.getProperty("user.dir");

		List<Object[]> list = service.getSlotsStatusAndCount().stream()
				.map(dto -> new Object[] { dto.getStatus(), dto.getCount() }).toList();

		util.generateBar(path, list);
		util.generatePie(path, list);

		return "AdminDashboard";
	}

	// ================= INVOICE =================
	@GetMapping("/invoice")
	public void generateInvoice(@RequestParam Long id, Principal principal, HttpServletResponse response) {

		if (principal == null) {
			throw new IllegalStateException("Please login first");
		}

		SlotRequest sr = service.getSlotRequestEntity(id);

		if (!principal.getName().equals(sr.getPatient().getEmail())) {
			throw new IllegalStateException("Unauthorized access");
		}

		PaymentStatus status = sr.getLatestPaymentStatus();

		if (status == PaymentStatus.INITIATED || status == PaymentStatus.FAILED) {
			throw new IllegalStateException("Invoice not available");
		}

		try {
			invoiceSlipPdfView.generateInvoice(SlotRequestResponseDTO.builder().id(sr.getId())
					.patientEmail(sr.getPatient().getEmail()).paymentStatus(status).build(), response);
		} catch (Exception ex) {
			throw new RuntimeException("Error generating invoice", ex);
		}
	}
}