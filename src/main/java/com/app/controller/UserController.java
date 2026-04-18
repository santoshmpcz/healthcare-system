package com.app.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.domain.User;
import com.app.service.UserService;
import com.app.service.DoctorService;
import com.app.service.PatientService;
import com.app.service.AppointmentService;
import com.app.util.MyMailUtil;
import com.app.util.UserUtil;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

	@Autowired
	private UserService service;

	@Autowired
	private UserUtil util;

	@Autowired
	private MyMailUtil mailUtil;

	// ✅ Dashboard Services
	@Autowired
	private DoctorService doctorService;

	@Autowired
	private PatientService patientService;

	@Autowired
	private AppointmentService appointmentService;

	/*
	 * ================= LOGIN =================
	 */
	@GetMapping("/login")
	public String showLogin() {
		return "UserLogin";
	}

	/*
	 * // 🔁 change to your actual folder
	 * 
	 * @GetMapping("/user-images/{img}")
	 * 
	 * @ResponseBody public org.springframework.core.io.Resource
	 * getUserImage(@PathVariable String img) { return new
	 * org.springframework.core.io.FileSystemResource( "C:/uploads/user/" + img //
	 * 🔁 change to your actual folder ); }
	 */
	/*
	 * ================= PROFILE =================
	 */
	@GetMapping("/profile")
	public String showProfile(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userOb");

		if (user == null) {
			return "redirect:/user/login";
		}

		model.addAttribute("user", user);
		return "UserProfile";
	}

	/*
	 * ================= DASHBOARD =================
	 */
	@GetMapping("/setup")
	public String setup(HttpSession session, Principal p) {

		if (p == null) {
			return "redirect:/user/login";
		}

		String username = p.getName();

		User user = service.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

		session.setAttribute("userOb", user);

		// 🔥 REDIRECT TO DASHBOARD (DO NOT LOAD DATA HERE)
		return "redirect:/user/home";
	}

	/*
	 * ================= HOME DASHBOARD =================
	 */
	@GetMapping("/home")
	public String showDashboard(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userOb");

		if (user == null) {
			return "redirect:/user/login";
		}

		// ✅ FIX: pass user to UI
		model.addAttribute("user", user);

		try {
			model.addAttribute("doctorCount", doctorService.getDoctorCount());
			model.addAttribute("patientCount", patientService.count());
			model.addAttribute("appointmentCount", appointmentService.getTotalAppointmentCount());
			model.addAttribute("bedCount", 0);

		} catch (Exception e) {
			log.error("Dashboard loading error", e);

			model.addAttribute("doctorCount", 0);
			model.addAttribute("patientCount", 0);
			model.addAttribute("appointmentCount", 0);
			model.addAttribute("bedCount", 0);
		}

		return "UserHome";
	}

	/*
	 * ================= PASSWORD UPDATE =================
	 */
	@GetMapping("/showPwdUpdate")
	public String showPwdUpdate(HttpSession session) {

		if (session.getAttribute("userOb") == null) {
			return "redirect:/user/login";
		}

		return "UserPwdUpdate";
	}

	@PostMapping("/pwdUpdate")
	public String updatePdw(@RequestParam String password, HttpSession session, RedirectAttributes ra) {

		User user = (User) session.getAttribute("userOb");

		if (user == null) {
			return "redirect:/user/login";
		}

		service.updateUserPwd(password, user.getId());

		// ✅ Flash message (only once)
		ra.addFlashAttribute("message", "Password Updated!");

		// ✅ Redirect (VERY IMPORTANT)
		return "redirect:/user/showPwdUpdate";
	}

	/*
	 * ================= FORGOT PASSWORD =================
	 */
	@GetMapping("/showForgot")
	public String showForgot() {
		return "UserNewPwdGen";
	}

	@PostMapping("/genNewPwd")
	public String genNewPwd(@RequestParam String email, RedirectAttributes ra) {

		Optional<User> opt = service.findByUsername(email);

		if (opt.isPresent()) {

			User user = opt.get();

			String pwd = util.genPwd();
			service.updateUserPwd(pwd, user.getId());

			// ✅ Flash message (only once)
			ra.addFlashAttribute("message", "Password Updated! Check your inbox!!");

			// ✅ Async mail
			new Thread(() -> {
				try {
					String text = "USERNAME: " + user.getUsername() + " | NEW PASSWORD: " + pwd;
					mailUtil.send(user.getUsername(), "New Password", text);
				} catch (Exception e) {
					log.error("Mail sending failed", e);
				}
			}).start();

		} else {
			// ✅ Flash message for error
			ra.addFlashAttribute("message", "User Not Found!");
			
		}

		// ✅ VERY IMPORTANT (redirect)
		return"redirect:/user/showForgot";
	}
}