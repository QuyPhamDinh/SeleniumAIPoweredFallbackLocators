package org.example.pages;


import org.example.utils.AILocator;
import org.example.utils.OllamaLocatorHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class LoginPage {
    private final WebDriver driver;
    private final AILocator aiHelper;

    public LoginPage(WebDriver driver, AILocator aiHelper) {
        this.driver = driver;
        this.aiHelper = aiHelper;
    }

    private final By usernameInput = By.id("userName");
    private final By passwordInput = By.id("Password");
    private final By loginButton = By.id("loginin");

    public void login(String username, String password) {
        aiHelper.findWithFallback(usernameInput, "username field").sendKeys(username);
        aiHelper.findWithFallback(passwordInput, "password field").sendKeys(password);
        aiHelper.findWithFallback(loginButton, "login button").click();
    }

    public void loginFullyAI(String username, String password) {

        OllamaLocatorHelper aiHelper = new OllamaLocatorHelper(driver);
        Map<String, String> descriptions = Map.of(
                "username", "username field",
                "password", "password field",
                "login", "log in button"
        );

        Map<String, By> locators = aiHelper.findMultipleWithFallback(descriptions, By.tagName("form"));
        driver.findElement(locators.get("username")).sendKeys(username);
        driver.findElement(locators.get("password")).sendKeys(password);
        driver.findElement(locators.get("login")).click();

    }
}

