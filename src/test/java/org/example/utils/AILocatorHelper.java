package org.example.utils;


import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AILocatorHelper implements AILocator {
    private final WebDriver driver;
    private final OpenAiService aiService;

    public AILocatorHelper(WebDriver driver, String apiKey) {
        this.driver = driver;
        this.aiService = new OpenAiService(apiKey);
    }

    public WebElement findWithFallback(By primary, String description) {
        try {
            return driver.findElement(primary);
        } catch (NoSuchElementException e) {
            System.out.println("[AI] Primary locator failed, querying AI for: " + description);

            String html = driver.getPageSource().substring(0, 3000);
            String prompt = "Given this HTML: \n" + html +
                    "\nFind a CSS or XPath selector for: " + description;

            CompletionRequest request = CompletionRequest.builder()
                    .prompt(prompt)
                    .model("gpt-3.5-turbo-instruct")
                    .temperature(0.3)
                    .maxTokens(100)
                    .build();

            String suggestion = aiService.createCompletion(request)
                    .getChoices().get(0).getText().trim();

            System.out.println("[AI] Suggested locator: " + suggestion);

            By aiLocator = suggestion.startsWith("//") ? By.xpath(suggestion) : By.cssSelector(suggestion);
            return driver.findElement(aiLocator);
        }
    }
}

