package com.healthtech.symptom_analyzer.service;

import com.healthtech.symptom_analyzer.model.ClinicalReport;
import com.healthtech.symptom_analyzer.model.SymptomReport;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.font.FontProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    @Autowired
    private TemplateEngine templateEngine;

    @Value("classpath:static/logo.png")
    private Resource logoResource;

    public byte[] generateClinicalReport(SymptomReport report, ClinicalReport clinicalData) {

        // 1. Thymeleaf context
        Context context = new Context();
        context.setVariable("name", report.getCustomerName());   // ← ADD
        context.setVariable("mobile", report.getCustomerMobile());
        context.setVariable("email", report.getCustomerEmail());
        context.setVariable("age", report.getAge());
        context.setVariable("date", report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        context.setVariable("problemsList", clinicalData.getProblemsList());
        context.setVariable("howToCure", clinicalData.getHowToCure());
        context.setVariable("suggestedTests", clinicalData.getSuggestedTests());
        context.setVariable("clinicalSummary", clinicalData.getClinicalSummary());

        // 2. Render HTML template
        String htmlContent = templateEngine.process("report-template", context);

        // 3. THE REAL FIX: register system fonts + standard PDF fonts so iText7
        //    can resolve "Helvetica", "Arial" etc. from CSS font-family declarations.
        //    Without this, iText7 stores font names as strings → TextRenderer error.
        FontProvider fontProvider = new FontProvider();
        fontProvider.addStandardPdfFonts();   // registers Helvetica, Times, Courier etc.
        fontProvider.addSystemFonts();        // registers Arial, and any OS fonts

        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setFontProvider(fontProvider);

        // 4. First pass: HTML → PDF bytes (errors should be gone now)
        ByteArrayOutputStream firstPass = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, firstPass, converterProperties);

        // 5. Second pass: add logo + text watermarks on every page
        try {
            ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
            PdfDocument pdfDoc = new PdfDocument(
                    new PdfReader(new ByteArrayInputStream(firstPass.toByteArray())),
                    new PdfWriter(finalOut)
            );

            ImageData logoData = ImageDataFactory.create(logoResource.getInputStream().readAllBytes());

            PdfFont font = PdfFontFactory.createFont(
                    StandardFonts.HELVETICA_BOLD,
                    PdfEncodings.WINANSI,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );

            int totalPages = pdfDoc.getNumberOfPages();

            for (int i = 1; i <= totalPages; i++) {
                PdfPage page = pdfDoc.getPage(i);
                Rectangle ps = page.getPageSize();

                PdfCanvas canvas = new PdfCanvas(
                        page.newContentStreamBefore(),
                        page.getResources(),
                        pdfDoc
                );

                // ── LOGO: 5% opacity, centered ──
                canvas.saveState()
                        .setExtGState(new PdfExtGState()
                                .setFillOpacity(0.05f)
                                .setStrokeOpacity(0.05f));

                float logoW = ps.getWidth() * 0.55f;
                float logoH = logoW * ((float) logoData.getHeight() / logoData.getWidth());
                canvas.addImageFittedIntoRectangle(
                        logoData,
                        new Rectangle(
                                (ps.getWidth() - logoW) / 2f,
                                (ps.getHeight() - logoH) / 2f,
                                logoW, logoH
                        ),
                        false
                );
                canvas.restoreState();

                // ── TEXT: "@fatlosswithsahil", 7% opacity, -30° ──
                double rad = Math.toRadians(-30);
                float cos = (float) Math.cos(rad);
                float sin = (float) Math.sin(rad);

                canvas.saveState()
                        .setExtGState(new PdfExtGState().setFillOpacity(0.07f));

                canvas.beginText()
                        .setFontAndSize(font, 36)
                        .setColor(ColorConstants.BLUE, true)
                        .setTextMatrix(
                                cos, sin, -sin, cos,
                                ps.getWidth() / 2f - 100f,
                                ps.getHeight() / 2f - 40f
                        )
                        .showText("@fatlosswithsahil")
                        .endText();

                canvas.restoreState();
                canvas.release();
            }

            pdfDoc.close();
            return finalOut.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return firstPass.toByteArray();
        }
    }
}
