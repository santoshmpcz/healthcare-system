package com.app.config;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication)
			throws IOException, ServletException {

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		String redirectURL = null;

		for (GrantedAuthority auth : authorities) {

			String role = auth.getAuthority();

			if (role.equals("ROLE_ADMIN")) {
				redirectURL = "/admin/dashboard";
				break;
			} else if (role.equals("ROLE_DOCTOR")) {
				redirectURL = "/doctor/dashboard";
				break;
			} else if (role.equals("ROLE_PATIENT")) {
				redirectURL = "/patient/dashboard";
				break;
			}
		}

		// fallback
		if (redirectURL == null) {
			redirectURL = "/";
		}

		response.sendRedirect(redirectURL);
	}
}