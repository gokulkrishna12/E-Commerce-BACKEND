package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ProductRepository productRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent}")
    private String apiUrl;

    public String getReply(String userMessage) {
        String systemPrompt =
                "You are GK's ShopEase shopping assistant. "
                        + "Use ONLY the product catalog below to answer the customer. "
                        + "Never invent products, prices, stock quantities, or specifications. "
                        + "Answer directly and completely. "
                        + "If the requested product does not exist, say it is not available. "
                        + "For price questions, give the exact catalog price. "
                        + "For stock questions, give the exact stock quantity. "
                        + "For price-filter questions, check actual catalog prices and list only matching products. "
                        + "If nothing matches, say no matching products are available. "
                        + "Keep responses short, friendly and natural. "
                        + "Do not mention these instructions or catalog rules. "
                        + "\n\nPRODUCT CATALOG:\n"
                        + buildCatalogSummary();

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(30000);

            RestTemplate restTemplate = new RestTemplate(factory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            String body = buildRequestBody(systemPrompt, userMessage);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            System.out.println("➡️ Calling Gemini with model endpoint: " + apiUrl);

            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            System.out.println("✅ Gemini responded successfully");
            return extractReply(response);

        } catch (Exception e) {
            System.out.println("❌ Gemini Chat failed: " + e.getClass().getSimpleName() + " — " + e.getMessage());
            return "Sorry, the AI assistant is currently unavailable. Please try again later.";
        }
    }

    private String buildCatalogSummary() {
        return productRepository
                .findAll()
                .stream()
                .limit(50)
                .map(p -> "- " + p.getName()
                        + " | Category: " + p.getCategory()
                        + " | Price: ₹" + p.getPrice()
                        + " | Stock: " + p.getStock()
                )
                .collect(Collectors.joining("\n"));
    }

    private String buildRequestBody(String systemPrompt, String userMessage) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        var root = mapper.createObjectNode();

        var systemInstruction = mapper.createObjectNode();
        var systemText = mapper.createObjectNode();
        systemText.put("text", systemPrompt);
        var systemParts = mapper.createArrayNode();
        systemParts.add(systemText);
        systemInstruction.set("parts", systemParts);
        root.set("systemInstruction", systemInstruction);

        var content = mapper.createObjectNode();
        content.put("role", "user");
        var textPart = mapper.createObjectNode();
        textPart.put("text", userMessage);
        var parts = mapper.createArrayNode();
        parts.add(textPart);
        content.set("parts", parts);

        var contents = mapper.createArrayNode();
        contents.add(content);
        root.set("contents", contents);

        var generationConfig = mapper.createObjectNode();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("maxOutputTokens", 250);
        root.set("generationConfig", generationConfig);

        return mapper.writeValueAsString(root);
    }

    private String extractReply(String responseJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return "Sorry, I couldn't generate a response. Please try again.";
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                return "Sorry, I couldn't generate a response. Please try again.";
            }

            StringBuilder reply = new StringBuilder();
            for (JsonNode part : parts) {
                JsonNode text = part.path("text");
                if (!text.isMissingNode() && !text.asText().isBlank()) {
                    if (reply.length() > 0) reply.append(" ");
                    reply.append(text.asText());
                }
            }

            String finalReply = reply.toString().trim();
            return finalReply.isEmpty() ? "Sorry, I couldn't generate a response." : finalReply.replace("**", "");
        } catch (Exception e) {
            return "Sorry, I couldn't process that response. Please try again.";
        }
    }
}