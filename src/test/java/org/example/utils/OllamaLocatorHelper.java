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
import java.util.Map;

public class OllamaLocatorHelper implements AILocator {


    private final WebDriver driver;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL_NAME = "llama3.2-vision:latest"; // Change if needed

    public OllamaLocatorHelper(WebDriver driver) {
        this.driver = driver;
    }

    public WebElement findWithFallback(By primary, String description) {
        try {
            return driver.findElement(primary);
        } catch (NoSuchElementException e) {
            System.out.println("[AI] Primary locator failed. Falling back using Ollama for: " + description);

            String html = driver.getPageSource();
//            if (html.length() > 3000) {
//                html = html.substring(0, 3000); // truncate for token safety
//            }


            // Optional: extract only form block if needed
            html = driver.findElement(By.tagName("form")).getAttribute("outerHTML");

            html = html.replace("\"", "'"); // avoid breaking JSON

            String prompt = "Given this HTML:\n" + html +
                    "\nProvide only a single CSS or XPath selector for: " + description +
                    ". Do not include quotes, backticks, markdown, or explanations. Only output the selector as plain text.";

            String selector = queryOllama(prompt);

            if (selector == null || selector.isBlank()) {
                throw new RuntimeException("[AI] No suggestion received from Ollama");
            }

            selector = selector.trim().replace("`", "");

            System.out.println("[AI] Suggested locator: " + selector);
            By aiLocator = selector.startsWith("//") ? By.xpath(selector) : By.cssSelector(selector);
            return driver.findElement(aiLocator);
        }
    }

    public static String queryOllama(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:11434/api/generate");

            Map<String, Object> request = new HashMap<>();
            request.put("model", MODEL_NAME);
            request.put("prompt", prompt);
            request.put("stream", false); // important: get full response at once

            String jsonRequest = mapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("[AI] Ollama returned status " + response.statusCode());
            }

            Map<?, ?> responseMap = mapper.readValue(response.body(), Map.class);
            Object content = responseMap.get("response");
            if (content instanceof String text) {
                return text.trim();
            }

            throw new RuntimeException("[AI] Unexpected response format from Ollama");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("[AI] Failed to contact Ollama API", e);
        }
    }

    public Map<String, By> findMultipleWithFallback(Map<String, String> keysToDescriptions, By scopeLocator) {
        Map<String, By> result = new HashMap<>();

        String html;
        try {
            html = driver.findElement(scopeLocator).getAttribute("outerHTML");
        } catch (Exception e) {
            System.err.println("[AI] Scope element - " + scopeLocator.toString() + " - not found, falling back to <body>");
            html = driver.findElement(By.tagName("body")).getAttribute("outerHTML");
        }

        // Prompt construction
        StringBuilder promptBuilder = new StringBuilder("Given the following HTML:\n")
                .append(html)
                .append("\nProvide a plain JSON object mapping the following UI elements to their CSS or XPath selectors:\n");

        keysToDescriptions.forEach((key, desc) ->
                promptBuilder.append(key).append(": ").append(desc).append("\n"));

        // Strongly discourage Markdown or formatting
        promptBuilder.append("Respond ONLY with a JSON object. NO markdown (no triple backticks), NO extra explanation, NO quotes around keys if unnecessary, and NO surrounding text.");
//        System.out.println("prompt : " + promptBuilder.toString());
        String response = queryOllama(promptBuilder.toString());
        System.out.println("Ollama raw response: " + response);

        try {
            Map<String, String> selectors = mapper.readValue(response, Map.class);
            for (Map.Entry<String, String> entry : selectors.entrySet()) {
                String selector = entry.getValue().trim().replace("`", "");
                By by = selector.startsWith("//") ? By.xpath(selector) : By.cssSelector(selector);
                result.put(entry.getKey(), by);
            }
        } catch (Exception e) {
            throw new RuntimeException("[AI] Failed to parse JSON from Ollama response", e);
        }

        return result;
    }
}

