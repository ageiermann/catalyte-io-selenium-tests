package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.util.ArrayList;
import java.util.List;
import org.catalyte.io.pages.HirePage;
import org.catalyte.io.tests.unit.BaseUiTest;
import org.catalyte.io.utils.LoggerUtil;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.Listeners;

@Listeners({AllureTestNg.class, TestListener.class})
public class HireApprenticeSectionTests extends BaseUiTest {

  private static final java.util.logging.Logger logger = LoggerUtil.getLogger(
      HireApprenticeSectionTests.class);
  private HirePage hire;
  private List<String> warnings;
  private int totalChecks;

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {   // <-- different name; not overriding anything
    // BaseUiTest.setUpBase() runs first and creates 'driver'
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open("https://www.catalyte.io/hire-talent/hire-apprentices/");
    hire = new HirePage(driver);
  }

  @BeforeMethod
  public void resetWarnings() {
    warnings = new ArrayList<>();
    totalChecks = 0;
  }

  @Test
  public void testAccordionsExistLenient() {
    checkElement(() -> hire.getAccordionTalent().isDisplayed(), "Talent accordion missing");
    checkElement(() -> hire.getAccordionIndustries().isDisplayed(), "Industries accordion missing");
    checkElement(() -> hire.getAccordionHire().isDisplayed(), "Hire accordion missing");
  }

  @Test
  public void testImagesExistLenient() {
    checkElement(hire::isImage3111Displayed, "Image 3111 missing");
    checkElement(hire::isImage3112Displayed, "Image 3112 missing");
    checkElement(hire::isImage3113Displayed, "Image 3113 missing");
  }

  @Test
  public void testVideosExistLenient() throws InterruptedException {
    checkElement(() -> hire.getPRVideo().isDisplayed(), "Apprenticeships PR video iframe missing");
    checkElement(() -> hire.getTestimonialsVideo().isDisplayed(), "Apprenticeships testimonials video iframe missing");
  }

  @Test
  public void testWorkWithUsButtonDisplayedAndFunctionsLenient() {
    checkElement(() -> hire.getWorkWithUsButton().isDisplayed(), "Main workflow button missing");
    hire.getWorkWithUsButton().click();
    getWait().until(ExpectedConditions.urlContains("https://www.catalyte.io/about/"));
    String currentUrl = driver.getCurrentUrl();
    Assert.assertTrue(currentUrl.contains("https://www.catalyte.io/about/contact-sales/"),
        "Button did not navigate to the expected page! Current URL: " + currentUrl);
  }

  @Test
  public void testMenuTextItemsLenient() {
    totalChecks++;
    if (hire.getMenuTextItems().isEmpty()) {
      warnings.add("Menu text items missing");
      logger.warning("Menu text items missing");
    }
  }

  // ===== Utility methods =====
  private void checkElement(Checkable condition, String warningMessage) {
    totalChecks++;
    try {
      if (!condition.check()) {
        warnings.add(warningMessage);
        logger.warning(warningMessage);
      }
    } catch (Exception e) {
      warnings.add(warningMessage + " (exception: " + e.getMessage() + ")");
      logger.warning(warningMessage);
    }
  }

  @FunctionalInterface
  public interface Checkable { boolean check(); }
}