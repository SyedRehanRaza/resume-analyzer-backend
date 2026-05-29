package com.rehan.resume_analyzer.controller;

import com.rehan.resume_analyzer.model.Resume;
import com.rehan.resume_analyzer.repository.ResumeRepository;
import com.rehan.resume_analyzer.service.AIService;
import com.rehan.resume_analyzer.service.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "http://localhost:3000")
public class ResumeController {

    @Autowired
    private AIService aiService;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private ResumeRepository resumeRepository;

    // POST - Resume analyze karo
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestParam("file") MultipartFile file) {
        try {
            // 1. PDF se text nikalo
            String resumeText = pdfService.extractText(file);

            // 2. AI se analysis lo
            String aiAnalysis = aiService.analyzeResume(resumeText);

            // 3. ATS Score nikalo
            int atsScore = aiService.extractAtsScore(aiAnalysis);

            // 4. Database mein save karo
            Resume resume = new Resume();
            resume.setFileName(file.getOriginalFilename());
            resume.setExtractedText(resumeText);
            resume.setAiAnalysis(aiAnalysis);
            resume.setAtsScore(atsScore);
            resume.setUploadedAt(LocalDateTime.now());
            resumeRepository.save(resume);

            // 5. Response bhejo
            Map<String, Object> response = new HashMap<>();
            response.put("id", resume.getId());
            response.put("atsScore", atsScore);
            response.put("analysis", aiAnalysis);
            response.put("fileName", file.getOriginalFilename());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // GET - History dekho
    @GetMapping("/history")
    public List<Resume> getHistory() {
        return resumeRepository.findAllByOrderByUploadedAtDesc();
    }
}