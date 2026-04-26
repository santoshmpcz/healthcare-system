package com.app.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Apache POI
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Spring
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// App
import com.app.dto.SpecializationDTO;
import com.app.exception.SpecializationNotFoundException;
import com.app.service.SpecializationService;

// iText
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
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

	// ========================= REGISTER =========================
	@GetMapping("/register")
	public String showRegister(Model model) {
		model.addAttribute("specialization", new SpecializationDTO());
		return "SpecializationRegister";
	}

	// ========================= SAVE =========================
	@PostMapping("/save")
	public String save(@ModelAttribute("specialization") @Valid SpecializationDTO dto, BindingResult errors,
			RedirectAttributes attributes) {

		if (errors.hasErrors())
			return "SpecializationRegister";

		try {
			Long id = service.saveSpecialization(dto);
			attributes.addFlashAttribute("message", "Record (" + id + ") saved successfully");
		} catch (Exception e) {
			attributes.addFlashAttribute("message", e.getMessage());
			return "redirect:/spec/register";
		}

		return "redirect:/spec/all";
	}

	// ========================= VIEW =========================
	@GetMapping("/all")
	public String viewAll(@RequestParam(required = false) String keyword, @RequestParam(required = false) String filter,
			@PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable, Model model) {

		Page<SpecializationDTO> page;

		if ((keyword != null && !keyword.trim().isEmpty())) {
			page = service.searchSpecializations(keyword, pageable);
		} else {
			page = service.getAllSpecializations(pageable);
		}

		model.addAttribute("list", page.getContent());
		model.addAttribute("page", page);
		model.addAttribute("keyword", keyword);
		model.addAttribute("filter", filter);

		return "SpecializationData";
	}

	// ========================= DELETE =========================

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

	// ========================= EDIT =========================
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

	// ========================= UPDATE =========================
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

	// ========================= PDF EXPORT =========================
	@GetMapping("/pdf")
	public void exportPdf(HttpServletResponse response) throws Exception {

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=specialization.pdf");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		Document document = new Document(PageSize.A4, 25, 25, 360, 35);
		PdfWriter writer = PdfWriter.getInstance(document, baos);

		final PdfReader reader = new PdfReader(
				new ClassPathResource("static/myres/specialization.pdf").getInputStream());

		writer.setPageEvent(new PdfPageEventHelper() {
			@Override
			public void onEndPage(PdfWriter writer, Document document) {

				PdfContentByte canvas = writer.getDirectContentUnder();
				PdfImportedPage template = writer.getImportedPage(reader, 1);

				// SAME BACKGROUND ON EVERY PAGE
				canvas.addTemplate(template, 0, 0);

				Font footerFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);

				Phrase footer = new Phrase(
						"Generated : " + LocalDateTime.now().format(FORMATTER) + " | Page " + writer.getPageNumber(),
						footerFont);

				ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, footer, document.right(),
						document.bottom() - 10, 0);
			}
		});

		document.open();

		PdfPTable table = new PdfPTable(4);
		table.setWidthPercentage(92);
		table.setWidths(new float[] { 1.2f, 2f, 3f, 4f });
		table.setHeaderRows(1); // repeat header on next pages
		table.setSplitLate(false); // allow row split

		addPdfHeader(table, "S.NO");
		addPdfHeader(table, "CODE");
		addPdfHeader(table, "NAME");
		addPdfHeader(table, "NOTE");

		List<SpecializationDTO> list = service.getAllSpecializationsWithMeta();

		int serialNo = 1;
		for (SpecializationDTO dto : list) {
			addPdfCell(table, String.valueOf(serialNo++)); // serial starts from 1
			addPdfCell(table, dto.getSpecCode());
			addPdfCell(table, dto.getSpecName());
			addPdfCell(table, dto.getSpecNote());
		}

		document.add(table);

		document.close();
		reader.close();

		byte[] pdfBytes = baos.toByteArray();

		response.getOutputStream().write(pdfBytes);
		response.getOutputStream().flush();
	}

	// ========================= PDF HEADER =========================
	private void addPdfHeader(PdfPTable table, String text) {

		Font headFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);

		PdfPCell cell = new PdfPCell(new Phrase(text, headFont));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPadding(8f);
		cell.setBorderWidth(0.8f);

		table.addCell(cell);
	}

	// ========================= PDF CELL =========================
	private void addPdfCell(PdfPTable table, String text) {

		Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

		PdfPCell cell = new PdfPCell(new Phrase(text, bodyFont));
		cell.setPadding(6f);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setBorderWidth(0.8f);

		table.addCell(cell);
	}

	// ========================= EXCEL EXPORT =========================
	@GetMapping("/excel")
	public void exportExcel(HttpServletResponse response) throws IOException {

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=Specialization_Report.xlsx");

		List<SpecializationDTO> list = service.getAllSpecializationsWithMeta();

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("SPECIALIZATION");

		CellStyle headerStyle = workbook.createCellStyle();
		org.apache.poi.ss.usermodel.Font font = workbook.createFont();
		font.setBold(true);
		headerStyle.setFont(font);

		String[] columns = { "S.NO", "CODE", "NAME", "NOTE" };

		org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);

		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerStyle);
		}

		int rowCount = 1;
		int serialNo = 1;

		for (SpecializationDTO dto : list) {
			org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowCount++);
			row.createCell(0).setCellValue(serialNo++);
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