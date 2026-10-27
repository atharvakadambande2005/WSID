
package com.atharva.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    public String getRecommendation(String problem) {

        // Step 1: Validate the user's problem
        if (problem == null || problem.trim().isEmpty()) {
            return "Please describe your situation so I can help you.";
        }

        // Step 2: Prepare the prompt for Gemini
        String prompt = """
                You are WSID (What Should I Do?), a helpful
                decision-making assistant.

                Understand the user's exact situation and give
                specific, practical advice in simple English.

                Do not assume facts that the user has not provided.
                If important information is missing, ask a useful
                follow-up question.

                Format your answer using these sections:

                1. Understanding the Situation
                2. My Recommendation
                3. Why This May Help
                4. Other Options
                5. Next Steps

                Adapt your answer to the user's actual problem.

                User's situation:
                %s
                """.formatted(problem.trim());

        System.out.println("WSID received problem: " + problem);
        System.out.println("Starting Gemini API request...");

        // Step 3: Prepare the API request
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        // Step 4: Send the request and retry temporary failures
        String modelUrl =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent";

        for (int attempt = 1; attempt <= 3; attempt++) {

            try {
                Map<?, ?> response = restClient.post()
                        .uri(modelUrl)
                        .header("x-goog-api-key", apiKey)
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

                System.out.println("Gemini API response received.");

                // Step 5: Extract the AI's answer
                if (response == null) {
                    return "Gemini returned an empty response. Please try again.";
                }

                Object candidatesObject = response.get("candidates");

                if (!(candidatesObject instanceof List<?> candidates)
                        || candidates.isEmpty()) {
                    return "Gemini returned no recommendation. Please try again.";
                }

                Object candidateObject = candidates.get(0);

                if (!(candidateObject instanceof Map<?, ?> candidate)) {
                    return "Gemini returned an invalid response.";
                }

                Object contentObject = candidate.get("content");

                if (!(contentObject instanceof Map<?, ?> content)) {
                    return "Gemini returned no response content.";
                }

                Object partsObject = content.get("parts");

                if (!(partsObject instanceof List<?> parts)
                        || parts.isEmpty()) {
                    return "Gemini returned no answer text.";
                }

                StringBuilder answer = new StringBuilder();

                for (Object partObject : parts) {
                    if (partObject instanceof Map<?, ?> part
                            && part.get("text") instanceof String text) {
                        answer.append(text);
                    }
                }

                if (answer.isEmpty()) {
                    return "Gemini did not return a text recommendation.";
                }

                System.out.println("Recommendation generated successfully.");

                return answer.toString();

            } catch (RestClientResponseException e) {

                System.err.println(
                        "Gemini API HTTP status: " + e.getStatusCode()
                );

                System.err.println(
                        "Gemini API error response: " + e.getResponseBodyAsString()
                );

                // Retry only temporary server errors
                if (e.getStatusCode().is5xxServerError()
                        && attempt < 3) {

                    System.err.println(
                            "Temporary server error. Attempt "
                                    + attempt + " of 3."
                    );

                    try {
                        Thread.sleep(1500L * attempt);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        return "Request interrupted. Please try again.";
                    }

                } else {
                    return "Gemini API request failed with HTTP status "
                            + e.getStatusCode()
                            + ". Check the STS console for details.";
                }

            } catch (Exception e) {

                System.err.println(
                        "Gemini request failed: "
                                + e.getClass().getSimpleName()
                                + " - " + e.getMessage()
                );

                return "Could not connect to Gemini. Check the STS console and try again.";
            }
        }

        return "Gemini is temporarily unavailable. Please try again later.";
    }
}
