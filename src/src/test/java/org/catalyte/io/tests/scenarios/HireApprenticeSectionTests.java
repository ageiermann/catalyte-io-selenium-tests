package org.catalyte.io.tests.scenarios;

import java.util.ArrayList;
import java.util.List;
import org.catalyte.io.pages.HirePage;
import org.catalyte.io.utils.ConfigUtil;
import org.catalyte.io.utils.LoggerUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;

public class HireApprenticeSectionTests {

  private WebDriver driver;
  private HirePage hire;
  private static final java.util.logging.Logger logger = LoggerUtil.getLogger(
      HireApprenticeSectionTests.class);

  private List<String> warnings;
  private int totalChecks;

  @BeforeClass
  public void setUp() {
    driver = new ChromeDriver();
    driver.manage().window().maximize();
    driver.get("https://www.catalyte.io/hire-talent/hire-apprentices/");
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

    double threshold = ConfigUtil.getThreshold("accordions.threshold", 0.5);
    assertWithinThreshold(threshold, "Too many accordions missing!");
  }

  @Test
  public void testImagesExistLenient() {
    checkElement(hire::isImage3111Displayed, "Image 3111 missing");
    checkElement(hire::isImage3112Displayed, "Image 3112 missing");
    checkElement(hire::isImage3113Displayed, "Image 3113 missing");

    double threshold = ConfigUtil.getThreshold("images.threshold", 0.5);
    assertWithinThreshold(threshold, "Too many images missing!");
  }

  @Test
  public void testMenuTextItemsLenient() {
    totalChecks++;
    if (hire.getMenuTextItems().isEmpty()) {
      warnings.add("Menu text items missing");
      logger.warning("Menu text items missing");
    }

    double threshold = ConfigUtil.getThreshold("menu.threshold", 0.0);
    assertWithinThreshold(threshold, "Menu completely missing!");
  }

  @AfterClass
  public void tearDown() {
    if (driver != null) {
      driver.quit();
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

  private void assertWithinThreshold(double maxMissingRatio, String failureMessage) {
    double missingRatio = missingRate();
    if (missingRatio > maxMissingRatio) {
      Assert.fail(failureMessage + " Missing ratio=" + missingRatio + " Warnings=" + warnings);
    } else if (!warnings.isEmpty()) {
      logger.info("Test passed with warnings: " + warnings);
    }
  }

  private double missingRate() {
    return totalChecks == 0 ? 0.0 : (double) warnings.size() / totalChecks;
  }

  @FunctionalInterface
  private interface Checkable {
    boolean check();
  }
}