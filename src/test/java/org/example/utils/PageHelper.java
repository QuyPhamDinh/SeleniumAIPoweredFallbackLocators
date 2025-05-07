package org.example.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PageHelper {

    public static boolean waitForTextAppear(WebDriver driver, String containText, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(driver1 -> driver1.getPageSource().contains(containText));

    }
}
