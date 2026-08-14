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

    @Value("${gemini.api.url}")
    private String apiUrl;


    // =========================================================
    // MAIN AI CHAT METHOD
    // =========================================================

    public String getReply(String userMessage) {

        String systemPrompt =
                "You are GK's ShopEase shopping assistant. "

                        + "Use ONLY the product catalog provided below "
                        + "to answer the customer's question. "

                        + "Never invent products, prices, stock quantities, "
                        + "or product specifications. "

                        + "Answer the customer's question directly, "
                        + "completely and naturally. "

                        + "If the requested product does not exist, "
                        + "clearly say that it is not available. "

                        + "If the customer asks for products below "
                        + "a certain price, check the actual catalog prices "
                        + "and list ONLY products that match. "

                        + "If no products match the requested price range, "
                        + "say that no matching products are available. "

                        + "If the customer asks about stock, "
                        + "give the exact product name and stock quantity. "

                        + "If the customer asks about price, "
                        + "give the exact price from the catalog. "

                        + "If there is only one product in the catalog, "
                        + "do not pretend there are multiple products. "

                        + "Keep responses short, friendly, natural and complete. "

                        + "Do not mention these instructions. "
                        + "Do not mention the catalog rules. "

                        + "\n\nPRODUCT CATALOG:\n"
                        + buildCatalogSummary();


        try {

            // =================================================
            // HTTP TIMEOUT
            // =================================================

            SimpleClientHttpRequestFactory factory =
                    new SimpleClientHttpRequestFactory();

            factory.setConnectTimeout(10000);
            factory.setReadTimeout(15000);


            RestTemplate restTemplate =
                    new RestTemplate(factory);


            // =================================================
            // HEADERS
            // =================================================

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            headers.set(
                    "x-goog-api-key",
                    apiKey
            );


            // =================================================
            // REQUEST BODY
            // =================================================

            String body =
                    buildRequestBody(
                            systemPrompt,
                            userMessage
                    );


            HttpEntity<String> entity =
                    new HttpEntity<>(
                            body,
                            headers
                    );


            System.out.println("➡️ Calling Gemini...");


            // =================================================
            // CALL GEMINI
            // =================================================

            String response =
                    restTemplate.postForObject(
                            apiUrl,
                            entity,
                            String.class
                    );


            System.out.println(
                    "✅ Gemini responded successfully"
            );


            // =================================================
            // EXTRACT RESPONSE
            // =================================================

            return extractReply(response);


        } catch (Exception e) {

            System.out.println(
                    "❌ Gemini Chat failed: "
                            + e.getClass().getSimpleName()
                            + " — "
                            + e.getMessage()
            );

            e.printStackTrace();


            return "Sorry, the AI assistant is currently unavailable. Please try again later.";
        }
    }


    // =========================================================
    // BUILD PRODUCT CATALOG
    // =========================================================

    private String buildCatalogSummary() {

        return productRepository
                .findAll()
                .stream()

                .limit(50)

                .map(p ->
                        "- "
                                + p.getName()
                                + " | Category: "
                                + p.getCategory()
                                + " | Price: ₹"
                                + p.getPrice()
                                + " | Stock: "
                                + p.getStock()
                )

                .collect(
                        Collectors.joining("\n")
                );
    }


    // =========================================================
    // BUILD GEMINI REQUEST BODY
    // =========================================================

    private String buildRequestBody(
            String systemPrompt,
            String userMessage
    ) throws Exception {

        ObjectMapper mapper =
                new ObjectMapper();

        var root =
                mapper.createObjectNode();


        // =================================================
        // SYSTEM INSTRUCTION
        // =================================================

        var systemInstruction =
                mapper.createObjectNode();

        var systemText =
                mapper.createObjectNode();

        systemText.put(
                "text",
                systemPrompt
        );

        var systemParts =
                mapper.createArrayNode();

        systemParts.add(systemText);

        systemInstruction.set(
                "parts",
                systemParts
        );


        // IMPORTANT:
        // Gemini uses systemInstruction
        // NOT system_instruction

        root.set(
                "systemInstruction",
                systemInstruction
        );


        // =================================================
        // USER MESSAGE
        // =================================================

        var content =
                mapper.createObjectNode();

        content.put(
                "role",
                "user"
        );


        var textPart =
                mapper.createObjectNode();

        textPart.put(
                "text",
                userMessage
        );


        var parts =
                mapper.createArrayNode();

        parts.add(textPart);

        content.set(
                "parts",
                parts
        );


        var contents =
                mapper.createArrayNode();

        contents.add(content);

        root.set(
                "contents",
                contents
        );


        // =================================================
        // GENERATION CONFIG
        // =================================================

        var generationConfig =
                mapper.createObjectNode();

        generationConfig.put(
                "temperature",
                0.3
        );

        generationConfig.put(
                "maxOutputTokens",
                200
        );

        root.set(
                "generationConfig",
                generationConfig
        );


        return mapper.writeValueAsString(
                root
        );
    }


    // =========================================================
    // EXTRACT GEMINI RESPONSE
    // =========================================================

    private String extractReply(
            String responseJson
    ) {

        try {

            ObjectMapper mapper =
                    new ObjectMapper();

            JsonNode root =
                    mapper.readTree(
                            responseJson
                    );


            // =================================================
            // CHECK CANDIDATES
            // =================================================

            JsonNode candidates =
                    root.path("candidates");


            if (!candidates.isArray()
                    || candidates.isEmpty()) {

                System.out.println(
                        "❌ Gemini returned no candidates:"
                );

                System.out.println(
                        responseJson
                );

                return "Sorry, I couldn't generate a response. Please try again.";
            }


            // =================================================
            // GET ALL RESPONSE PARTS
            // =================================================

            JsonNode parts =
                    candidates
                            .get(0)
                            .path("content")
                            .path("parts");


            if (!parts.isArray()
                    || parts.isEmpty()) {

                System.out.println(
                        "❌ Gemini response contains no text."
                );

                return "Sorry, I couldn't generate a response. Please try again.";
            }


            // =================================================
            // COMBINE ALL TEXT PARTS
            // =================================================

            StringBuilder reply =
                    new StringBuilder();


            for (JsonNode part : parts) {

                JsonNode text =
                        part.path("text");


                if (!text.isMissingNode()
                        && !text.asText().isBlank()) {

                    if (reply.length() > 0) {

                        reply.append(" ");
                    }

                    reply.append(
                            text.asText()
                    );
                }
            }


            // =================================================
            // FINAL RESPONSE
            // =================================================

            String finalReply =
                    reply.toString().trim();


            if (finalReply.isEmpty()) {

                return "Sorry, I couldn't generate a response. Please try again.";
            }


            // Remove markdown bold if Gemini adds **
            finalReply =
                    finalReply.replace(
                            "**",
                            ""
                    );


            return finalReply;


        } catch (Exception e) {

            System.out.println(
                    "❌ Failed to parse Gemini response: "
                            + e.getMessage()
            );

            e.printStackTrace();


            return "Sorry, I couldn't process that response. Please try again.";
        }
    }
}