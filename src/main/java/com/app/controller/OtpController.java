package com.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.service.OtpService;

@RestController
@RequestMapping("/otp")
public class OtpController {

	@Autowired
	private OtpService otpService;

	// ✅ SEND OTP
	@PostMapping("/send")
	public ResponseEntity<String> sendOtp(@RequestParam String email) {

		try {
			otpService.generateAndSendOtp(email);
			return ResponseEntity.ok("OTP sent to mobile and email");

		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Failed to send OTP");
		}
	}

	// ✅ VERIFY OTP
	@PostMapping("/verify")
	public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {

		boolean valid = otpService.verifyOtp(email, otp);

		if (valid) {
			return ResponseEntity.ok("OTP Verified Successfully");
		} else {
			return ResponseEntity.badRequest().body("Invalid or Expired OTP");
		}
	}
}