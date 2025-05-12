package org.example.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public interface AILocator {
    public WebElement findWithFallback(By primary, String description);
}
