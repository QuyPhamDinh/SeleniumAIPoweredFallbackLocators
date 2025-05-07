package org.example.pages;


import org.example.utils.AILocator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
}

