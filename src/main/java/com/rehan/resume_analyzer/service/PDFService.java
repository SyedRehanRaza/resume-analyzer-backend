package com.rehan.resume_analyzer.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class PDFService {

    public String extractText(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        PDDocument document = Loader.loadPDF(bytes);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return text;
    }
}