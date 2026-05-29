package com.rehan.resume_analyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class AIService {

    @Value("${ai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeResume(String resumeText) {

        String prompt = "You are an expert ATS resume analyzer for Java Developer roles.\n\n" +
                "Analyze this resume and provide your response in EXACTLY this format:\n\n" +
                "ATS Score: [NUMBER]/100\n\n" +
                "Key Strengths:\n" +
                "- [point 1]\n" +
                "- [point 2]\n\n" +
                "Skills Gap:\n" +
                "- [missing skill 1]\n\n" +
                "Improvements:\n" +
                "- [suggestion 1]\n\n" +
                "Overall Feedback:\n" +
                "[feedback]\n\n" +
                "Resume:\n" + resumeText;

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", List.of(textPart));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(parts));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        List<Map> candidates = (List<Map>) response.getBody().get("candidates");
        Map content = (Map) candidates.get(0).get("content");
        List<Map> resParts = (List<Map>) content.get("parts");
        return (String) resParts.get(0).get("text");
    }

    public int extractAtsScore(String aiAnalysis) {
        try {
            String[] lines = aiAnalysis.split("\n");
            for (String line : lines) {
                if (line.toLowerCase().contains("ats score")) {
                    String[] parts = line.split("[:(/]");
                    for (String part : parts) {
                        part = part.trim().replaceAll("[^0-9]", "");
                        if (!part.isEmpty()) {
                            int score = Integer.parseInt(part);
                            if (score >= 0 && score <= 100) return score;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return 50;
        }
        return 50;
    }
}