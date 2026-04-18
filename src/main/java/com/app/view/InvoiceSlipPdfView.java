package com.app.view;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.app.dto.SlotRequestResponseDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class InvoiceSlipPdfView {

	public void generateInvoice(SlotRequestResponseDTO dto, HttpServletResponse response) throws Exception {

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=INVOICE_SLIP.pdf");

		try {

			// ================= TEMPLATE =================
			InputStream is = getClass().getClassLoader().getResourceAsStream("static/myres/patient_invoice.pdf");

			if (is == null) {
				throw new RuntimeException("PDF template not found!");
			}

			PdfReader reader = new PdfReader(is);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfStamper stamper = new PdfStamper(reader, baos);

			PdfContentByte canvas = stamper.getOverContent(1);

			// ================= FONTS =================
			BaseFont bold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

			BaseFont normal = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

			Font tableHeader = new Font(Font.HELVETICA, 12, Font.BOLD);
			Font tableValue = new Font(Font.HELVETICA, 11);

			// ================= LAYOUT =================
			float stripBottomY = 520;
			float headerStartY = stripBottomY - 30;

			// ================= HEADER =================
			canvas.beginText();

			canvas.setFontAndSize(bold, 14);
			canvas.setTextMatrix(80, headerStartY);
			canvas.showText("SANTOSH HOSPITAL");

			canvas.setFontAndSize(normal, 10);
			canvas.setTextMatrix(80, headerStartY - 15);
			canvas.showText("Harrahawa, Singrauli - 486886");

			canvas.setTextMatrix(80, headerStartY - 30);
			canvas.showText("Contact: 9575385110");

			canvas.setTextMatrix(80, headerStartY - 45);
			canvas.showText("GST No: FGXPS2234G1S1");

			canvas.setFontAndSize(bold, 10);
			canvas.setTextMatrix(350, headerStartY - 15);
			canvas.showText("Invoice No: INV-" + System.currentTimeMillis());

			canvas.endText();

			// ================= TABLE START =================
			float tableStartY = headerStartY - 80;

			// ================= PATIENT TABLE =================
			PdfPTable leftTable = new PdfPTable(2);
			leftTable.setTotalWidth(320);
			leftTable.setLockedWidth(true);

			addCell(leftTable, "Appointment Date", tableHeader);
			addCell(leftTable, safe(dto.getAppointmentDate()), tableValue);

			addCell(leftTable, "Patient Name", tableHeader);
			addCell(leftTable, safe(dto.getPatientFirstName()), tableValue);

			addCell(leftTable, "Doctor Name", tableHeader);
			addCell(leftTable, safe(dto.getDoctorName()), tableValue);

			float leftHeight = leftTable.getTotalHeight();
			leftTable.writeSelectedRows(0, -1, 80, tableStartY, canvas);

			// ================= BILLING =================
			double fee = dto.getFee() != null ? dto.getFee() : 0.0;
			double gst = fee * 0.06;
			double total = fee + (2 * gst);

			PdfPTable rightTable = new PdfPTable(2);
			rightTable.setTotalWidth(250);
			rightTable.setLockedWidth(true);

			addCell(rightTable, "Booking Fee", tableHeader);
			addCell(rightTable, String.format("%.2f", fee), tableValue);

			addCell(rightTable, "CGST (6%)", tableHeader);
			addCell(rightTable, String.format("%.2f", gst), tableValue);

			addCell(rightTable, "SGST (6%)", tableHeader);
			addCell(rightTable, String.format("%.2f", gst), tableValue);

			addCell(rightTable, "Total Amount", tableHeader);
			addCell(rightTable, String.format("%.2f", total), tableValue);

			float rightY = tableStartY - leftHeight - 20;
			rightTable.writeSelectedRows(0, -1, 80, rightY, canvas);

			float rightHeight = rightTable.getTotalHeight();

			// ================= FOOTER =================
			float footerY = rightY - rightHeight - 20;

			ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
					new Phrase(
							"Generated on: "
									+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
							new Font(Font.HELVETICA, 9)),
					300, footerY, 0);

			// ================= CLOSE =================
			stamper.close();
			reader.close();

			response.getOutputStream().write(baos.toByteArray());
			response.getOutputStream().flush();

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating invoice PDF", e);
		}
	}

	// ================= SAFE =================
	private String safe(String val) {
		return val != null ? val : "N/A";
	}

	private void addCell(PdfPTable table, String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setPadding(8f);
		cell.setBorderWidth(0.7f);
		table.addCell(cell);
	}
}