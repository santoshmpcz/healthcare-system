package com.app.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.app.service.OtpService;

@Service
public class OtpServiceImpl implements OtpService {

	static class OtpData {
		String emailOtp;
		String mobileOtp;
		long expiryTime;
	}

	private Map<String, OtpData> otpStorage = new HashMap<>();

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public void generateAndSendOtp(String email) {

		String emailOtp = generateOtp();
		String mobileOtp = generateOtp();

		OtpData data = new OtpData();
		data.emailOtp = emailOtp;
		data.mobileOtp = mobileOtp;
		data.expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);

		otpStorage.put(email, data);

		// ✅ SEND EMAIL OTP
		sendEmailOtp(email, emailOtp);

		// ❗ For now mobile OTP still console (until SMS API added)
		System.out.println("Mobile OTP: " + mobileOtp);
	}

	@Override
	public boolean verifyOtp(String email, String otp) {

		OtpData data = otpStorage.get(email);

		if (data == null)
			return false;

		if (System.currentTimeMillis() > data.expiryTime) {
			otpStorage.remove(email);
			return false;
		}

		if (otp.equals(data.emailOtp) || otp.equals(data.mobileOtp)) {
			otpStorage.remove(email);
			return true;
		}

		return false;
	}

	private String generateOtp() {
		return String.valueOf(new Random().nextInt(900000) + 100000);
	}

	// ✅ EMAIL METHOD
	private void sendEmailOtp(String to, String otp) {

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject("Your OTP Code");
			message.setText("Your OTP is: " + otp);

			mailSender.send(message);

			System.out.println("Email OTP sent successfully");

		} catch (Exception e) {
			System.out.println("Email sending failed: " + e.getMessage());
		}
	}
}