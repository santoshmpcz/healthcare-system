package com.app.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	// ================= VALIDATION ERROR =================
	@ExceptionHandler(IllegalArgumentException.class)
	public String handleValidationException(IllegalArgumentException ex, Model model, HttpServletRequest request) {

		model.addAttribute("errorTitle", "Validation Error");
		model.addAttribute("message", ex.getMessage());
		model.addAttribute("path", request.getRequestURI());

		return "error";
	}

	// ================= NULL POINTER =================
	@ExceptionHandler(NullPointerException.class)
	public String handleNullPointerException(NullPointerException ex, Model model, HttpServletRequest request) {

		model.addAttribute("errorTitle", "Data Missing Error");
		model.addAttribute("message", "Required data is missing. Please try again.");
		model.addAttribute("path", request.getRequestURI());

		return "error";
	}

	// ================= DATABASE ERROR =================
	@ExceptionHandler(DataIntegrityViolationException.class)
	public String handleDatabaseException(DataIntegrityViolationException ex, Model model, HttpServletRequest request) {

		model.addAttribute("errorTitle", "Database Error");
		model.addAttribute("message", "Invalid data or constraint violation.");
		model.addAttribute("path", request.getRequestURI());

		return "error";
	}

	// ================= FORM VALIDATION (Future Use) =================
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public String handleFormValidation(MethodArgumentNotValidException ex, Model model, HttpServletRequest request) {

		String errorMsg = ex.getBindingResult().getFieldError().getDefaultMessage();

		model.addAttribute("errorTitle", "Form Validation Error");
		model.addAttribute("message", errorMsg);
		model.addAttribute("path", request.getRequestURI());

		return "error";
	}

	// ================= SLOT LIMIT ERROR =================
	@ExceptionHandler(RuntimeException.class)
	public String handleRuntimeException(RuntimeException ex, Model model, HttpServletRequest request) {

		model.addAttribute("errorTitle", "Operation Failed");
		model.addAttribute("message", ex.getMessage());
		model.addAttribute("path", request.getRequestURI());

		return "error";
	}

	// ================= GENERAL ERROR (FALLBACK) =================
	@ExceptionHandler(Exception.class)
	public String handleGeneralException(Exception ex, Model model, HttpServletRequest request) {

		model.addAttribute("errorTitle", "Unexpected Error");
		model.addAttribute("message", "Something went wrong. Please try again later.");
		model.addAttribute("path", request.getRequestURI());

		return "error";
	}
}