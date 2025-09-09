package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.util.List;
import org.catalyte.io.pages.HomePage;
import org.catalyte.io.tests.unit.BaseUiTest;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({AllureTestNg.class, TestListener.class})
public class HomePageTests extends BaseUiTest {

  private HomePage page;
  private List<String> warnings;
  private final String homePageUrl = "https://www.catalyte.io/";

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(homePageUrl);
    page = new HomePage(driver);
  }

  @BeforeMethod(alwaysRun = true)
  @Override
  protected String startUrlForThisClass() { return homePageUrl; }

  @Test
  public void verifyAllHeadingElementsPresent() {
    for (WebElement e : page.getHomepageHeadingElements()) {
      checkElement(e::isDisplayed, "One or more heading elements missing");
    }
  }
  @Test
  public void testGetStartedButtonDisplayedAndFunctionsLenient() {
    checkElement(() -> page.getGetStartedButton().isDisplayed(), "'Get Started' button missing");
    page.clickGetStartedButton();
    getWait().until(ExpectedConditions.urlContains("/about/"));
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    org.testng.Assert.assertTrue(
        currentUrl.contains("/about/contact-sales/"),
        "Button did not navigate to expected page. Current URL: " + currentUrl
    );
  }
}
