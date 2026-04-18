package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Configuration
public class RazorpayConfig {

	@Value("${RAZORPAY_KEY}")
	private String key;

	@Value("${RAZORPAY_SECRET}")
	private String secret;

	@Bean
	RazorpayClient razorpayClient() throws RazorpayException {
		return new RazorpayClient(key, secret);
	}
}
