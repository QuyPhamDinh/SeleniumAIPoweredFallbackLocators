package org.example.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public interface AILocator {
    WebElement findWithFallback(By primary, String description);
}
