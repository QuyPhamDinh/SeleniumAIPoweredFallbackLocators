package org.example.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPT4AllLocatorHelper implements AILocator {
    private final WebDriver driver;
    private static final String GPT4ALL_API_URL = "http://localhost:4891/v1/chat/completions";
    // Change if needed

    public GPT4AllLocatorHelper(WebDriver driver) {
        this.driver = driver;
    }

    public WebElement findWithFallback(By primary, String description) {
        try {
            return driver.findElement(primary);
        } catch (NoSuchElementException e) {
            System.out.println("[AI] Primary locator failed, querying GPT4All for: " + description);
            String html = driver.getPageSource(); // Use first 3k chars of page

            // Optional: extract only form block if needed
            html = driver.findElement(By.tagName("form")).getAttribute("outerHTML");

            String prompt = "Given this HTML:\n" + html + "\nReturn only a raw CSS or XPath selector for " + description + " . Do not include any quotes, backticks, code block, explanation, or extra characters in your response.";

            String suggestion = queryGpt4All(prompt);
            if (suggestion == null || suggestion.isEmpty()) {
                throw new RuntimeException("[AI] No suggestion received from GPT4All");
            }

            suggestion = suggestion.replace("`", "");
            System.out.println("[AI] Suggested locator: " + suggestion);
            By aiLocator = suggestion.trim().startsWith("//") ? By.xpath(suggestion.trim()) : By.cssSelector(suggestion.trim());

            return driver.findElement(aiLocator);
        }
    }

    public static String queryGpt4All(String prompt) {
        try {
            // Set up HTTP request
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:4891/v1/chat/completions");

            // Prepare request body
            Map<String, Object> json = new HashMap<>();
            json.put("model", "Mistral OpenOrca");  // Use exact model name from /v1/models
            json.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            json.put("temperature", 0.7);
            json.put("stream", false);

            // Serialize to JSON
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(json);

            // Send request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("[AI] GPT4All returned status code: " + response.statusCode());
            }

            System.out.println("[AI] Raw response: " + response.body());

            // Parse response
            Map<?, ?> responseMap = mapper.readValue(response.body(), Map.class);
            List<?> choices = (List<?>) responseMap.get("choices");

            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("[AI] No choices returned from GPT4All");
            }

            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            if (message == null || message.get("content") == null) {
                throw new RuntimeException("[AI] No content found in GPT4All response");
            }

            return message.get("content").toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("[AI] Failed to contact GPT4All API", e);
        }
    }


}

