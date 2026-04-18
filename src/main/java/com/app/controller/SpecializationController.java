package com.app.controller;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.SpecializationDTO;
import com.app.exception.SpecializationNotFoundException;
import com.app.service.SpecializationService;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/spec")
@RequiredArgsConstructor
public class SpecializationController {

	private final SpecializationService service;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

	// ================= REGISTER =================
	@GetMapping("/register")
	public String showRegister(Model model) {
		model.addAttribute("specialization", new SpecializationDTO());
		return "SpecializationRegister";
	}

	// ================= SAVE =================
	@PostMapping("/save")
	public String save(@ModelAttribute("specialization") @Valid SpecializationDTO dto, BindingResult errors,
			RedirectAttributes attributes) {

		if (errors.hasErrors()) {
			return "SpecializationRegister";
		}

		try {
			Long id = service.saveSpecialization(dto);
			attributes.addFlashAttribute("message", "Record (" + id + ") saved successfully");
		} catch (Exception e) {
			attributes.addFlashAttribute("message", e.getMessage());
			return "redirect:/spec/register";
		}

		return "redirect:/spec/all";
	}

	// ================= VIEW ALL =================
	@GetMapping("/all")
	public String viewAll(@PageableDefault(size = 5) Pageable pageable, Model model) {

		Page<SpecializationDTO> page = service.getAllSpecializations(pageable);

		model.addAttribute("list", page.getContent());
		model.addAttribute("page", page);

		return "SpecializationData";
	}

	// ================= DELETE =================
	@GetMapping("/delete")
	public String delete(@RequestParam Long id, RedirectAttributes attributes) {

		try {
			service.removeSpecialization(id);
			attributes.addFlashAttribute("message", "Record (" + id + ") deleted successfully");
		} catch (SpecializationNotFoundException e) {
			attributes.addFlashAttribute("message", e.getMessage());
		}

		return "redirect:/spec/all";
	}

	// ================= EDIT =================
	@GetMapping("/edit")
	public String showEdit(@RequestParam Long id, Model model, RedirectAttributes attributes) {

		try {
			SpecializationDTO dto = service.getOneSpecialization(id);

			model.addAttribute("specialization", dto);
			return "SpecializationEdit";

		} catch (SpecializationNotFoundException e) {
			attributes.addFlashAttribute("message", e.getMessage());
			return "redirect:/spec/all";
		}
	}

	// ================= UPDATE =================
	@PostMapping("/update")
	public String update(@ModelAttribute("specialization") @Valid SpecializationDTO dto, BindingResult errors,
			RedirectAttributes attributes, Model model) {

		if (errors.hasErrors()) {
			model.addAttribute("specialization", dto);
			return "SpecializationEdit";
		}

		try {
			service.updateSpecialization(dto.getId(), dto);

			attributes.addFlashAttribute("message", "Record (" + dto.getId() + ") updated successfully");

		} catch (Exception e) {
			model.addAttribute("message", e.getMessage());
			model.addAttribute("specialization", dto);
			return "SpecializationEdit";
		}

		return "redirect:/spec/all";
	}

	// ================= PDF EXPORT =================
	@GetMapping("/pdf")
	public void exportPdf(HttpServletResponse response) throws Exception {

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=Specialization_Report.pdf");

		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

		writer.setPageEvent(new PdfPageEventHelper() {
			@Override
			public void onEndPage(PdfWriter writer, Document document) {

				com.lowagie.text.Font footerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);

				Phrase footer = new Phrase(
						"Generated on: " + LocalDateTime.now().format(FORMATTER) + " | Page " + writer.getPageNumber(),
						footerFont);

				ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, footer,
						document.right() - 20, document.bottom() - 10, 0);
			}
		});

		document.open();

		com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18,
				com.lowagie.text.Font.BOLD, Color.BLUE);

		Paragraph title = new Paragraph("SPECIALIZATION REPORT", titleFont);
		title.setAlignment(Element.ALIGN_CENTER);
		title.setSpacingAfter(20);
		document.add(title);

		PdfPTable table = new PdfPTable(4);
		table.setWidthPercentage(100);

		addPdfHeader(table, "ID");
		addPdfHeader(table, "CODE");
		addPdfHeader(table, "NAME");
		addPdfHeader(table, "NOTE");

		// ✅ IMPORTANT FIX
		List<SpecializationDTO> list = service.getAllSpecializationsWithMeta();

		for (SpecializationDTO dto : list) {
			table.addCell(String.valueOf(dto.getId()));
			table.addCell(dto.getSpecCode());
			table.addCell(dto.getSpecName());
			table.addCell(dto.getSpecNote());
		}

		document.add(table);
		document.close();
	}

	private void addPdfHeader(PdfPTable table, String text) {

		com.lowagie.text.Font headFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12,
				com.lowagie.text.Font.BOLD);

		PdfPCell cell = new PdfPCell(new Phrase(text, headFont));
		cell.setBackgroundColor(Color.LIGHT_GRAY);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);

		table.addCell(cell);
	}

	// ================= EXCEL EXPORT =================
	@GetMapping("/excel")
	public void exportExcel(HttpServletResponse response) throws IOException {

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		response.setHeader("Content-Disposition", "attachment; filename=Specialization_Report.xlsx");

		// ✅ IMPORTANT FIX
		List<SpecializationDTO> list = service.getAllSpecializationsWithMeta();

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("SPECIALIZATION");

		CellStyle headerStyle = workbook.createCellStyle();
		org.apache.poi.ss.usermodel.Font excelFont = workbook.createFont();
		excelFont.setBold(true);
		headerStyle.setFont(excelFont);

		String[] columns = { "ID", "CODE", "NAME", "NOTE" };

		Row headerRow = sheet.createRow(0);

		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerStyle);
		}

		int rowCount = 1;

		for (SpecializationDTO dto : list) {
			Row row = sheet.createRow(rowCount++);
			row.createCell(0).setCellValue(dto.getId());
			row.createCell(1).setCellValue(dto.getSpecCode());
			row.createCell(2).setCellValue(dto.getSpecName());
			row.createCell(3).setCellValue(dto.getSpecNote());
		}

		for (int i = 0; i < columns.length; i++) {
			sheet.autoSizeColumn(i);
		}

		workbook.write(response.getOutputStream());
		workbook.close();
	}
}