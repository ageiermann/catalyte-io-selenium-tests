package org.catalyte.io.tests.unit;

import io.qameta.allure.testng.AllureTestNg;
import java.util.List;
import org.catalyte.io.pages.HomePage;
import org.catalyte.io.pages.PageFooter;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Listeners({AllureTestNg.class, TestListener.class})
public class PageFooterTests extends BaseUiTest{

  private PageFooter footer;
  private List<String> warnings;
  private final String startPageUrl = "https://www.catalyte.io/";

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(startPageUrl);
    footer = new PageFooter(driver);
  }

  @BeforeMethod(alwaysRun = true)
  @Override
  protected String startUrlForThisClass() { return startPageUrl; }

  // === Methods For Footer Common To All Pages === //
  @Test
  public void verifyAllFooterElementsPresentAcrossAllPages() {

    SoftAssert softAssert = new SoftAssert();

    for (String url : footer.getPageURLs()) {
      driver.get(url);
      boolean footerFound = waitForFooter();
      softAssert.assertTrue(footerFound, "Footer container not found on page " + url);
      logger.info("Testing page " + url);

      for (By locator : footer.getFooterLocatorsAsList()) {
        boolean present = isPresent(locator);
        softAssert.assertTrue(present, "Missing footer element [" + locator + "] on page " + url);
      }
    }
    softAssert.assertAll("Footer verification issues found on one or more pages.");
  }

  //==== Footer test helpers ==== //
  private boolean isPresent(By locator) { return !driver.findElements(locator).isEmpty(); }

  private boolean waitForFooter() {
    try {
      WebElement footerElement = wait.until(
          ExpectedConditions.presenceOfElementLocated
              (footer.getFooterLocatorsAsList().get(0)));
      return footerElement != null;
    }
    catch (Exception e) {
      return false;
    }
  }
}

