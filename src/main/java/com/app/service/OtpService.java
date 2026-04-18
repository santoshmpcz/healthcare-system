package com.app.service;

public interface OtpService {

	// send OTP to both email + mobile
	void generateAndSendOtp(String email);

	// verify OTP (any one works)
	boolean verifyOtp(String email, String otp);
}