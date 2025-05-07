package org.example.tests;


import org.example.base.BaseTest;
import org.example.pages.LoginPage;
import org.example.utils.AILocator;
import org.example.utils.OllamaLocatorHelper;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class LoginTestOllama extends BaseTest {


    @Test
    public void testLoginHybridAIOllama() {
        driver.get("http://eaapp.somee.com/Account/Login");

        AILocator aiHelper = new OllamaLocatorHelper(driver);

        LoginPage loginPage = new LoginPage(driver, aiHelper);

        loginPage.login("aitest", "8i@Testing");

        assertTrue(driver.getPageSource().contains("Hello aitest!"));
    }

}

