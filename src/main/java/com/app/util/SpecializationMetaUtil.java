package com.app.util;

import java.util.Map;

/**
 * ========================================================== Specialization
 * Meta Utility Industry Standard - Hospital Management Systems
 *
 * Responsibilities: ✔ Resolve Specialization Icon ✔ Resolve Specialization
 * Color ✔ Provide Stable UI Branding ✔ Allow DB Override
 *
 * Design: ✔ Immutable ✔ Thread Safe ✔ Centralized Metadata Registry
 * ==========================================================
 */
public final class SpecializationMetaUtil {

	private SpecializationMetaUtil() {
		// Prevent instantiation
	}

	/*
	 * ====================================================== DEFAULT VALUES
	 * ======================================================
	 */
	private static final String DEFAULT_ICON = "fa-solid fa-stethoscope";
	private static final String DEFAULT_COLOR = "#2563eb";

	/*
	 * ====================================================== META CLASS (IMMUTABLE)
	 * ======================================================
	 */
	private static final class Meta {

		private final String icon;
		private final String color;

		private Meta(String icon, String color) {
			this.icon = icon;
			this.color = color;
		}
	}

	/*
	 * ====================================================== MASTER SPECIALIZATION
	 * REGISTRY (Immutable Configuration)
	 * ======================================================
	 */
	private static final Map<String, Meta> META_MAP = Map.ofEntries(

			Map.entry("cardiology", new Meta("fa-solid fa-heart-pulse", "#ef4444")),

			Map.entry("dermatology", new Meta("fa-solid fa-hand-sparkles", "#a855f7")),

			Map.entry("neurology", new Meta("fa-solid fa-brain", "#6366f1")),

			Map.entry("orthopedic", new Meta("fa-solid fa-bone", "#f59e0b")),

			Map.entry("pediatrics", new Meta("fa-solid fa-baby", "#14b8a6")),

			Map.entry("ophthalmology", new Meta("fa-solid fa-eye", "#0ea5e9")),

			Map.entry("ent", new Meta("fa-solid fa-ear-listen", "#0284c7")),

			Map.entry("dental", new Meta("fa-solid fa-tooth", "#10b981")),

			Map.entry("oncology", new Meta("fa-solid fa-ribbon", "#dc2626")),

			Map.entry("pulmonology", new Meta("fa-solid fa-lungs", "#0891b2")),

			Map.entry("hematology", new Meta("fa-solid fa-droplet", "#b91c1c")),

			Map.entry("gastroenterology", new Meta("fa-solid fa-stomach", "#f97316")),

			Map.entry("nephrology", new Meta("fa-solid fa-filter", "#2563eb")),

			Map.entry("gynecology", new Meta("fa-solid fa-person-pregnant", "#ec4899")),

			Map.entry("anesthesia", new Meta("fa-solid fa-syringe", "#9333ea")),

			Map.entry("pathology", new Meta("fa-solid fa-microscope", "#7c3aed")),

			Map.entry("radiology", new Meta("fa-solid fa-x-ray", "#0ea5e9")),

			Map.entry("psychiatry", new Meta("fa-solid fa-head-side-virus", "#8b5cf6")),

			Map.entry("physiotherapy", new Meta("fa-solid fa-person-walking", "#22c55e")),

			Map.entry("emergency", new Meta("fa-solid fa-truck-medical", "#ef4444")),

			Map.entry("surgery", new Meta("fa-solid fa-scissors", "#dc2626")),

			Map.entry("endocrinology", new Meta("fa-solid fa-vials", "#06b6d4")),

			Map.entry("urology", new Meta("fa-solid fa-flask", "#3b82f6")),

			Map.entry("general", new Meta("fa-solid fa-stethoscope", "#2563eb")));

	/*
	 * ====================================================== NORMALIZE
	 * SPECIALIZATION NAME ======================================================
	 */
	private static String normalize(String name) {
		if (name == null)
			return "";
		return name.trim().toLowerCase();
	}

	/*
	 * ====================================================== INTERNAL META RESOLVER
	 * ======================================================
	 */
	private static Meta resolveMeta(String specializationName) {

		String key = normalize(specializationName);

		for (Map.Entry<String, Meta> entry : META_MAP.entrySet()) {
			if (key.contains(entry.getKey())) {
				return entry.getValue();
			}
		}

		return new Meta(DEFAULT_ICON, DEFAULT_COLOR);
	}

	/*
	 * ====================================================== PUBLIC ICON RESOLVER
	 * ======================================================
	 */
	public static String resolveIcon(String name, String iconFromDb) {

		// DB override has highest priority
		if (iconFromDb != null && !iconFromDb.isBlank()) {
			return iconFromDb;
		}

		return resolveMeta(name).icon;
	}

	/*
	 * ====================================================== PUBLIC COLOR RESOLVER
	 * ======================================================
	 */
	public static String resolveColor(String name, String colorFromDb) {

		// DB override has highest priority
		if (colorFromDb != null && !colorFromDb.isBlank()) {
			return colorFromDb;
		}

		return resolveMeta(name).color;
	}
}