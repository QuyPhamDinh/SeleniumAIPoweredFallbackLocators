package org.example.tests;


import org.example.base.BaseTest;
import org.example.pages.LoginPage;
import org.example.utils.AILocator;
import org.example.utils.GPT4AllLocatorHelper;
import org.example.utils.PageHelper;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class LoginTest extends BaseTest {


    @Test
    public void testLoginHybridAI() {
        driver.get("http://eaapp.somee.com/Account/Login");

//        AILocatorHelper aiHelper = new AILocatorHelper(driver, Constants.OPENAI_API_KEY);
        AILocator aiHelper = new GPT4AllLocatorHelper(driver);
        LoginPage loginPage = new LoginPage(driver, aiHelper);

        String userName = "aitest";
        loginPage.login(userName, "8i@Testing");

        boolean textAppeared = PageHelper.waitForTextAppear(driver, userName, 10);
        assertTrue(textAppeared);
    }

}

