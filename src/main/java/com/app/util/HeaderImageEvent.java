package com.app.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class HeaderImageEvent extends PdfPageEventHelper {

    private Image headerImg;

    public HeaderImageEvent(String imagePath) {
        try {
            headerImg = Image.getInstance(imagePath);
            headerImg.scaleToFit(550, 250);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            PdfContentByte cb = writer.getDirectContentUnder();

            // Position image at top
            headerImg.setAbsolutePosition(20, 550);
            cb.addImage(headerImg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}