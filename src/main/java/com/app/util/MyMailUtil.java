package com.app.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Component
public class MyMailUtil {

	@Autowired
	private JavaMailSender mailSender;

	// ✅ NEW (for Thymeleaf Template)
	@Autowired
	private TemplateEngine templateEngine;

	// ================= GENERIC MAIL =================
	public boolean send(String to[], String cc[], String bcc[], String subject, String text, Resource files[]) {
		boolean sent = false;
		try {
			MimeMessage message = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(to);

			if (cc != null)
				helper.setCc(cc);

			if (bcc != null)
				helper.setBcc(bcc);

			helper.setSubject(subject);

			// ✅ HTML ENABLED
			helper.setText(text, true);

			// Attachments
			if (files != null) {
				for (Resource file : files) {
					helper.addAttachment(file.getFilename(), file);
				}
			}

			mailSender.send(message);
			sent = true;

		} catch (Exception e) {
			e.printStackTrace();
			sent = false;
		}
		return sent;
	}

	// ================= SIMPLE MAIL =================
	public boolean send(String to, String subject, String text, Resource file) {
		return send(new String[] { to }, null, null, subject, text, file != null ? new Resource[] { file } : null);
	}

	public boolean send(String to, String subject, String text) {
		return send(to, subject, text, null);
	}

	// ================= TEMPLATE MAIL (NEW) =================
	public boolean sendRegistrationMailTemplate(String to, String name, String password, String role) {

		try {

			Context context = new Context();
			context.setVariable("name", name);
			context.setVariable("username", to);
			context.setVariable("password", password);
			context.setVariable("role", role);

			// 🔥 Load Thymeleaf HTML Template
			String htmlContent = templateEngine.process("mail/registration-mail", context);

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(to);
			helper.setSubject(role + " ACCOUNT CREATED");

			// ✅ HTML CONTENT
			helper.setText(htmlContent, true);

			mailSender.send(message);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}