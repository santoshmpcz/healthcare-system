package com.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.app.constraints.UserRoles;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	// ================= AUTH PROVIDER =================
	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	// ================= SECURITY FILTER =================
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.authenticationProvider(authenticationProvider())

				// ================= CSRF =================
				.csrf(csrf -> csrf.ignoringRequestMatchers("/payment/create-order", "/payment/verify"))

				// ================= AUTHORIZATION =================
				.authorizeHttpRequests(auth -> auth

						// ===== PUBLIC =====
						.requestMatchers("/patient/register", "/patient/save", "/user/showForgot", "/user/genNewPwd",
								"/user/login", "/login", "/otp/**")
						.permitAll()

						// ===== OTP FLOW (LOGGED-IN USERS ONLY) =====
						.requestMatchers("/user/setup", "/user/verifyOtpPage", "/user/verifyOtpLogin").authenticated()

						// ===== ADMIN ONLY =====
						.requestMatchers("/doctor/register", "/doctor/save", "/doctor/accept", "/doctor/reject",
								"/spec/**", "/slots/dashboard")
						.hasRole(UserRoles.ADMIN.name())

						// ===== ADMIN + DOCTOR =====
						.requestMatchers("/slots/accept", "/slots/reject")
						.hasAnyRole(UserRoles.ADMIN.name(), UserRoles.DOCTOR.name())

						// ===== DOCTOR =====
						.requestMatchers("/slots/doctor").hasRole(UserRoles.DOCTOR.name())

						// ===== PATIENT =====
						.requestMatchers("/slots/book", "/slots/cancel", "/slots/patient", "/payment/**")
						.hasRole(UserRoles.PATIENT.name())

						// ===== COMMON =====
						.requestMatchers("/appointment/view/**", "/slots/all")
						.hasAnyRole(UserRoles.ADMIN.name(), UserRoles.DOCTOR.name(), UserRoles.PATIENT.name())

						// ===== ANY OTHER =====
						.anyRequest().authenticated())

				// ================= ERROR HANDLING =================
				.exceptionHandling(ex -> ex.accessDeniedHandler((request, response, exception) -> {
					request.getSession().setAttribute("errorMessage",
							"Access Denied: You are not authorized to view this page");
					response.sendRedirect(request.getContextPath() + "/error/403");
				}))

				// ================= LOGIN =================
				.formLogin(form -> form.loginPage("/user/login").loginProcessingUrl("/login")
						.defaultSuccessUrl("/user/setup", true) // ✅ CRITICAL FIX
						.failureUrl("/user/login?error=true").permitAll())

				// ================= LOGOUT =================
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/user/login?logout=true")
						.invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll());

		return http.build();
	}
}