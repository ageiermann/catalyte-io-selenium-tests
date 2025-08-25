package org.catalyte.io.tests.unit;

import org.catalyte.io.pages.HomePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Lenient default tests for Base Page object using the LenientTestBase class.
 */
public class BaseTests extends LenientTestHelper {

  private WebDriver driver;
  private HomePage home;

  @BeforeClass
  public void setUp() {
    driver = new ChromeDriver();
    driver.get("https://catalyte.io");
    home = new HomePage(driver);
  }

  @Test
  public void testHomePageSections() {
    checkElement(home::isAccordionTalentVisible, "Accordion talent section missing");
    checkElement(home::isIndustriesVisible, "Industries accordion missing");
    checkElement(home::isHireAccordionVisible, "Hire accordion missing");

    // Allow up to 30% missing before failing
    assertWithinThreshold(0.3, "Too many HomePage sections failed");
  }

  @AfterClass
  public void tearDown() {
    driver.quit();
  }
}